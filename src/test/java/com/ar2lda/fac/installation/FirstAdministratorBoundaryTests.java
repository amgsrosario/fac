package com.ar2lda.fac.installation;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.flywaydb.core.Flyway;
import java.sql.*;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

/** R1: non-superuser ownership/membership and SQL isolation, only in fresh synthetic databases. */
@EnabledIfEnvironmentVariable(named="M02_DISPOSABLE",matches="true")
class FirstAdministratorBoundaryTests {
    static final String FUNCTION="public.provision_first_administrator(text,text,uuid,text,text,text,timestamptz,text,text,text,text)";
    String base=System.getenv("FAC_TEST_DATASOURCE_URL"),url,database;
    String operator=System.getenv("M02_OPERATOR"),password=System.getenv("M02_OPERATOR_PASSWORD");
    FirstAdministratorAuthorization auth;
    Connection owner() throws Exception {return DriverManager.getConnection(url,System.getenv("FAC_TEST_DATASOURCE_USERNAME"),System.getenv("FAC_TEST_DATASOURCE_PASSWORD"));}
    Connection local() throws Exception {return DriverManager.getConnection(url,operator,password);}
    void sql(String command) throws Exception {try(var c=owner();var q=c.createStatement()){q.execute(command);}}
    boolean flag(String query) throws Exception {try(var c=owner();var q=c.createStatement();var r=q.executeQuery(query)){r.next();return r.getBoolean(1);}}
    @BeforeEach void setup() throws Exception {
        database="m02r1_"+UUID.randomUUID().toString().replace("-","")+"_test";
        try(var c=DriverManager.getConnection(base,System.getenv("FAC_TEST_DATASOURCE_USERNAME"),System.getenv("FAC_TEST_DATASOURCE_PASSWORD"));var q=c.createStatement()){q.execute("create database "+database);}
        url=base.substring(0,base.lastIndexOf('/')+1)+database;
        Flyway.configure().dataSource(url,System.getenv("FAC_TEST_DATASOURCE_USERNAME"),System.getenv("FAC_TEST_DATASOURCE_PASSWORD")).load().migrate();
        sql("grant usage on schema public to "+operator);
        sql("grant execute on function public.local_admin_identity() to "+operator);
        sql("grant execute on function "+FUNCTION+" to "+operator);
        try(var c=local();var q=c.createStatement();var r=q.executeQuery("select * from public.local_admin_identity()")) {
            r.next();auth=new FirstAdministratorAuthorization(url,operator,database,r.getString("cluster_id"),r.getObject("installation_id",UUID.class),"M02-R1-SYNTHETIC","Synthetic",Instant.now().plusSeconds(600));
        }
        assertThat(flag("select rolsuper or rolcreatedb or rolcreaterole from pg_roles where rolname='"+operator+"'")).isFalse();
        assertThat(flag("select pg_get_userbyid(datdba)='"+operator+"' from pg_database where datname=current_database()" )).isFalse();
    }
    @AfterEach void dispose() throws Exception {
        try(var c=DriverManager.getConnection(base,System.getenv("FAC_TEST_DATASOURCE_USERNAME"),System.getenv("FAC_TEST_DATASOURCE_PASSWORD"));var q=c.createStatement()){q.execute("drop database "+database+" with(force)");}
    }
    long provision(Connection c) throws Exception {
        return new FirstAdministratorProvisioner().provision(c,auth,
            new FirstAdministratorProvisioner.Administrator("R1ADMIN","Synthetic","r1@example.test"),"Synthetic8!Unique".toCharArray());
    }
    void rejectedPrivilege() throws Exception {
        try(var c=local()) {
            assertThatThrownBy(()->provision(c)).isInstanceOfSatisfying(SQLException.class,e->{
                assertThat(e.getSQLState()).isEqualTo("P0001");
                assertThat(e.getMessage()).contains("restricted local provisioning operator");
            });
        }
        assertThat(flag("select not exists(select 1 from utilizador) and not exists(select 1 from auditoria_evento) and (select consumed_at is null from local_admin_provisioning)")).isTrue();
    }
    @Test void restrictedOperatorStillWorksAtReadCommitted() throws Exception {try(var c=local()){assertThat(c.getTransactionIsolation()).isEqualTo(Connection.TRANSACTION_READ_COMMITTED);assertThat(provision(c)).isPositive();}}
    @Test void membershipWithoutSuperuserIsRejected() throws Exception {
        String group="m02_group_"+UUID.randomUUID().toString().replace("-","");
        sql("create role "+group+" nologin");
        try {sql("grant "+group+" to "+operator);rejectedPrivilege();}
        finally {sql("revoke "+group+" from "+operator);sql("drop role "+group);}
    }
    @ParameterizedTest @ValueSource(strings={"utilizador","local_admin_provisioning","auditoria_evento"})
    void tableOwnerIsRejectedEvenAfterRevokingDml(String table) throws Exception {
        sql("alter table public."+table+" owner to "+operator);
        sql("revoke all on table public."+table+" from "+operator);
        assertThat(flag("select has_schema_privilege('"+operator+"','public','CREATE')")).isFalse();
        assertThat(flag("select has_table_privilege('"+operator+"','public."+table+"','INSERT,UPDATE,DELETE,TRUNCATE')")).isFalse();
        rejectedPrivilege();
    }
    @ParameterizedTest @ValueSource(strings={"public.local_admin_identity()","public.local_admin_receipt_guard()",FUNCTION})
    void functionOwnerIsRejectedBeforePrivilegedReads(String function) throws Exception {
        sql("alter function "+function+" owner to "+operator);
        assertThat(flag("select has_schema_privilege('"+operator+"','public','CREATE')")).isFalse();
        rejectedPrivilege();
    }
    @Test void schemaOwnerIsRejectedEvenWithoutCreateGrant() throws Exception {
        sql("alter schema public owner to "+operator);sql("revoke create on schema public from "+operator);
        assertThat(flag("select has_schema_privilege('"+operator+"','public','CREATE')")).isFalse();rejectedPrivilege();
    }
    @Test void schemaCreateWithoutOwnershipIsRejected() throws Exception {sql("grant create on schema public to "+operator);rejectedPrivilege();}
    @ParameterizedTest @ValueSource(strings={"utilizador","local_admin_provisioning","auditoria_evento"})
    void directDmlGrantIsRejected(String table) throws Exception {sql("grant insert on table public."+table+" to "+operator);rejectedPrivilege();}
    @ParameterizedTest @ValueSource(ints={Connection.TRANSACTION_READ_UNCOMMITTED,Connection.TRANSACTION_REPEATABLE_READ,Connection.TRANSACTION_SERIALIZABLE})
    void javaRejectsNonReadCommitted(int isolation) throws Exception {
        try(var c=local()){c.setTransactionIsolation(isolation);assertThatThrownBy(()->provision(c)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("READ COMMITTED");}
    }
    @ParameterizedTest @ValueSource(ints={Connection.TRANSACTION_READ_UNCOMMITTED,Connection.TRANSACTION_REPEATABLE_READ,Connection.TRANSACTION_SERIALIZABLE})
    void directSqlTransactionCannotBypassReadCommitted(int isolation) throws Exception {
        try(var c=local()) {
            c.setTransactionIsolation(isolation);c.setAutoCommit(false);
            try(var q=c.prepareStatement("select public.provision_first_administrator(?,?,?::uuid,?,?,?,?::timestamptz,?,?,?,?)")) {
                q.setString(1,auth.database());q.setString(2,auth.cluster());q.setObject(3,auth.installation());q.setString(4,auth.operator());
                q.setString(5,auth.reference());q.setString(6,auth.actor());q.setString(7,auth.expiresAt().toString());
                q.setString(8,"R1ADMIN");q.setString(9,"Synthetic");q.setString(10,"r1@example.test");q.setString(11,"not-a-hash");
                assertThatThrownBy(q::executeQuery).isInstanceOfSatisfying(SQLException.class,e->{assertThat(e.getSQLState()).isEqualTo("25001");assertThat(e.getMessage()).contains("READ COMMITTED");});
            } finally {c.rollback();}
        }
        assertThat(flag("select not exists(select 1 from utilizador) and not exists(select 1 from auditoria_evento) and (select consumed_at is null from local_admin_provisioning)")).isTrue();
    }
}
