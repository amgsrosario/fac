package com.ar2lda.fac.installation;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.flywaydb.core.Flyway;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

/** Synthetic databases only; no dependency on #38 or any restored snapshot. */
@EnabledIfEnvironmentVariable(named="M02_DISPOSABLE",matches="true")
class FirstAdministratorProvisioningTests {
    String url; String database; String operator=System.getenv("M02_OPERATOR");
    String password=System.getenv("M02_OPERATOR_PASSWORD");
    static final String SECRET="Synth9!LongRandom";
    FirstAdministratorAuthorization authorization;
    FirstAdministratorProvisioner tool=new FirstAdministratorProvisioner();
    final FirstAdministratorProvisioner.Administrator admin=new FirstAdministratorProvisioner.Administrator("M02ADMIN","Synthetic","m02@example.test");
    Connection owner() throws Exception {return DriverManager.getConnection(url,System.getenv("FAC_TEST_DATASOURCE_USERNAME"),System.getenv("FAC_TEST_DATASOURCE_PASSWORD"));}
    Connection local() throws Exception {return DriverManager.getConnection(url,operator,password);}
    @BeforeEach void setup(TestInfo info) throws Exception {
        String base=System.getenv("FAC_TEST_DATASOURCE_URL");database="m02_"+UUID.randomUUID().toString().replace("-","")+"_test";
        try(var c=DriverManager.getConnection(base,System.getenv("FAC_TEST_DATASOURCE_USERNAME"),System.getenv("FAC_TEST_DATASOURCE_PASSWORD"));var q=c.createStatement()) {q.execute("create database "+database);}
        url=base.substring(0,base.lastIndexOf('/')+1)+database;
        var configuration=Flyway.configure().dataSource(url,System.getenv("FAC_TEST_DATASOURCE_USERNAME"),System.getenv("FAC_TEST_DATASOURCE_PASSWORD"));
        if(info.getTestMethod().orElseThrow().getName().startsWith("upgradeDoesNot")) {
            configuration.target("28").load().migrate();
            try(var c=owner();var q=c.createStatement()) {
                q.execute("insert into utilizador(codigo,nome,email,password_hash,inativo) values('OLD','Old','old@example.test','!not-login!',false)");
            }
            var upgrade=Flyway.configure().dataSource(url,System.getenv("FAC_TEST_DATASOURCE_USERNAME"),System.getenv("FAC_TEST_DATASOURCE_PASSWORD")).load();
            assertThat(upgrade.migrate().migrationsExecuted).isEqualTo(1);
        } else configuration.load().migrate();
        try(var c=owner();var q=c.createStatement()) {
            q.execute("grant usage on schema public to "+operator);
            q.execute("grant execute on function public.local_admin_identity() to "+operator);
            q.execute("grant execute on function public.provision_first_administrator(text,text,uuid,text,text,text,timestamptz,text,text,text,text) to "+operator);
        }
        try(var c=local();var q=c.createStatement();var r=q.executeQuery("select * from public.local_admin_identity()")) {
            assertThat(r.next()).isTrue();authorization=new FirstAdministratorAuthorization(url,operator,database,r.getString("cluster_id"),r.getObject("installation_id",UUID.class),"SYNTHETIC-M02","Synthetic operator",Instant.now().plusSeconds(600));
        }
    }
    @AfterEach void discardDatabase() throws Exception {
        if(url==null)return;
        try(var c=DriverManager.getConnection(System.getenv("FAC_TEST_DATASOURCE_URL"),System.getenv("FAC_TEST_DATASOURCE_USERNAME"),System.getenv("FAC_TEST_DATASOURCE_PASSWORD"));var q=c.createStatement()) {q.execute("drop database "+database+" with(force)");}
    }
    long provision() throws Exception {try(var c=local()){return tool.provision(c,authorization,admin,SECRET.toCharArray());}}
    long count(String table) throws Exception {try(var c=owner();var q=c.createStatement();var r=q.executeQuery("select count(*) from "+table)){r.next();return r.getLong(1);}}
    FirstAdministratorAuthorization change(String db,String cluster,UUID id,String role,Instant expires) {
        return new FirstAdministratorAuthorization(url,role,db,cluster,id,authorization.reference(),authorization.actor(),expires);
    }
    @Test void createsExactlyOneActiveAdministratorAuditAndDurableReceipt() throws Exception {
        long id=provision();assertThat(id).isPositive();assertThat(count("utilizador")).isEqualTo(1);assertThat(count("auditoria_evento")).isEqualTo(1);
        try(var c=owner();var q=c.createStatement();var r=q.executeQuery("select u.papel,u.inativo,u.password_hash,p.audit_event_id,p.consumed_at from utilizador u cross join local_admin_provisioning p")) {
            r.next();assertThat(r.getString(1)).isEqualTo("ADMINISTRADOR");assertThat(r.getBoolean(2)).isFalse();
            assertThat(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().matches(SECRET,r.getString(3))).isTrue();
            assertThat(r.getLong(4)).isEqualTo(id);assertThat(r.getObject(5)).isNotNull();
        }
    }
    @Test void repeatedAttemptCannotAddOrReplaceAdministrator() throws Exception {
        provision();assertThatThrownBy(this::provision).isInstanceOf(SQLException.class);assertThat(count("utilizador")).isEqualTo(1);assertThat(count("auditoria_evento")).isEqualTo(1);
    }
    @Test void existingInactiveAdministratorIsNotReplaced() throws Exception {
        try(var c=owner();var q=c.createStatement()) {q.execute("insert into utilizador(codigo,nome,email,password_hash,inativo) values('OLD','Old','old@example.test','!not-login!',true)");}
        assertThatThrownBy(this::provision).isInstanceOf(SQLException.class);assertThat(count("utilizador")).isEqualTo(1);assertThat(count("auditoria_evento")).isZero();
    }
    @Test void existingNonAdministratorAlsoPreventsInstallationBootstrap() throws Exception {
        try(var c=owner();var q=c.createStatement()) {q.execute("insert into utilizador(codigo,nome,email,password_hash,inativo,papel) values('OLD','Old','old@example.test','!not-login!',false,'CONSULTA')");}
        assertThatThrownBy(this::provision).isInstanceOf(SQLException.class);
    }
    @Test void wrongClusterInstallationAndOperatorAreRejected() throws Exception {
        var original=authorization;
        for(var a:List.of(change(database,"1",original.installation(),operator,original.expiresAt()),
                         change(database,original.cluster(),UUID.randomUUID(),operator,original.expiresAt()),
                         change(database,original.cluster(),original.installation(),"other",original.expiresAt()))) {
            try(var c=local()){assertThatThrownBy(()->tool.provision(c,a,admin,SECRET.toCharArray())).isInstanceOf(SQLException.class);}
        }
        assertThat(count("utilizador")).isZero();assertThat(count("auditoria_evento")).isZero();
    }
    @Test void wrongDatabaseAndExpiredAuthorizationAreRejected() throws Exception {
        var wrong=new FirstAdministratorAuthorization(url.replace(database,"wrong_test"),operator,"wrong_test",authorization.cluster(),authorization.installation(),"SYNTHETIC","Synthetic",authorization.expiresAt());
        try(var c=local()){assertThatThrownBy(()->tool.provision(c,wrong,admin,SECRET.toCharArray())).isInstanceOf(SQLException.class);}
        authorization=change(database,authorization.cluster(),authorization.installation(),operator,Instant.now().minusSeconds(1));
        assertThatThrownBy(this::provision).isInstanceOf(IllegalArgumentException.class);assertThat(count("utilizador")).isZero();
    }
    @Test void concurrentCallsCommitExactlyOnce() throws Exception {
        var start=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> work=()->{start.await();try{provision();return true;}catch(SQLException expected){return false;}};
            var a=pool.submit(work);var b=pool.submit(work);start.countDown();
            assertThat(List.of(a.get(15,TimeUnit.SECONDS),b.get(15,TimeUnit.SECONDS))).containsExactlyInAnyOrder(true,false);
            assertThat(count("utilizador")).isEqualTo(1);assertThat(count("auditoria_evento")).isEqualTo(1);
        } finally {pool.shutdownNow();}
    }
    @Test void concurrentOrdinaryInsertIsObservedBeforeProvisioning() throws Exception {
        var pool=Executors.newSingleThreadExecutor();
        try(var writer=owner();var q=writer.createStatement()) {
            writer.setAutoCommit(false);
            q.execute("insert into utilizador(codigo,nome,email,password_hash,inativo) values('RACE','Race','race@example.test','!not-login!',false)");
            var future=pool.submit(()->{try{provision();return true;}catch(SQLException expected){return false;}});
            boolean waiting=false;
            for(int n=0;n<80;n++) {
                try(var c=owner();var inspect=c.createStatement();var r=inspect.executeQuery("select exists(select 1 from pg_stat_activity where usename='"+operator+"' and wait_event_type='Lock')")) {r.next();waiting=r.getBoolean(1);}
                if(waiting)break;Thread.sleep(25);
            }
            assertThat(waiting).isTrue();writer.commit();assertThat(future.get(10,TimeUnit.SECONDS)).isFalse();
            assertThat(count("utilizador")).isEqualTo(1);assertThat(count("auditoria_evento")).isZero();
        } finally {pool.shutdownNow();}
    }
    @Test void privilegedOperatorIsRefused() throws Exception {
        String owner=System.getenv("FAC_TEST_DATASOURCE_USERNAME");
        var privileged=change(database,authorization.cluster(),authorization.installation(),owner,authorization.expiresAt());
        try(var c=owner()){assertThatThrownBy(()->tool.provision(c,privileged,admin,SECRET.toCharArray())).isInstanceOf(SQLException.class);}
        assertThat(count("utilizador")).isZero();
    }
    @Test void auditFailureRollsBackUserAndConsumptionAndAllowsRetry() throws Exception {
        try(var c=owner();var q=c.createStatement()) {q.execute("create function public.m02_test_fail_audit() returns trigger language plpgsql as $$ begin raise exception 'Synthetic audit failure'; end $$");q.execute("create trigger m02_fail before insert on auditoria_evento for each row execute function public.m02_test_fail_audit()");}
        assertThatThrownBy(this::provision).isInstanceOf(SQLException.class);assertThat(count("utilizador")).isZero();
        try(var c=owner();var q=c.createStatement();var r=q.executeQuery("select consumed_at from local_admin_provisioning")){r.next();assertThat(r.getObject(1)).isNull();}
        try(var c=owner();var q=c.createStatement()) {q.execute("drop trigger m02_fail on auditoria_evento");}
        assertThat(provision()).isPositive();
    }
    @Test void operatorHasNoDirectUserOrReceiptWritePermission() throws Exception {
        try(var c=local();var q=c.createStatement()) {
            assertThatThrownBy(()->q.execute("delete from public.utilizador")).isInstanceOf(SQLException.class);
            assertThatThrownBy(()->q.execute("update public.local_admin_provisioning set consumed_at=null")).isInstanceOf(SQLException.class);
        }
    }
    @Test void consumedReceiptCannotBeResetDeletedOrTruncated() throws Exception {
        provision();try(var c=owner();var q=c.createStatement()) {
            assertThatThrownBy(()->q.execute("update public.local_admin_provisioning set consumed_at=null,administrator_code=null,audit_event_id=null")).isInstanceOf(SQLException.class);
            assertThatThrownBy(()->q.execute("delete from public.local_admin_provisioning")).isInstanceOf(SQLException.class);
            assertThatThrownBy(()->q.execute("truncate public.local_admin_provisioning")).isInstanceOf(SQLException.class);
        }
    }
    @Test void ungrantedRoleCannotInvokeProvisioning() throws Exception {
        try(var c=owner();var q=c.createStatement()) {q.execute("revoke execute on function public.provision_first_administrator(text,text,uuid,text,text,text,timestamptz,text,text,text,text) from "+operator);}
        assertThatThrownBy(this::provision).isInstanceOf(SQLException.class);assertThat(count("utilizador")).isZero();
    }
    @Test void passwordPolicyAndMemoryClearingArePreserved() throws Exception {
        char[] secret="password123!".toCharArray();try(var c=local()) {
            assertThatThrownBy(()->tool.provision(c,authorization,admin,secret)).isInstanceOf(RuntimeException.class);
        }assertThat(secret).containsOnly('\0');assertThat(count("utilizador")).isZero();
    }
    @Test void upgradeDoesNotCreateUserAndExistingUsersPermanentlyDisableBootstrap() throws Exception {
        assertThat(count("utilizador")).isEqualTo(1);assertThat(count("auditoria_evento")).isZero();
        try(var c=owner();var q=c.createStatement();var r=q.executeQuery("select eligible from local_admin_provisioning")) {
            r.next();assertThat(r.getBoolean(1)).isFalse();
        }
        assertThatThrownBy(this::provision).isInstanceOf(SQLException.class);
        try(var c=owner();var q=c.createStatement()) {q.execute("delete from public.utilizador");}
        assertThatThrownBy(this::provision).isInstanceOf(SQLException.class);
        var flyway=Flyway.configure().dataSource(url,System.getenv("FAC_TEST_DATASOURCE_USERNAME"),System.getenv("FAC_TEST_DATASOURCE_PASSWORD")).load();
        flyway.validate();assertThat(flyway.migrate().migrationsExecuted).isZero();
    }
}
