package com.ar2lda.fac.financial;
import org.junit.jupiter.api.Test;
import org.flywaydb.core.Flyway;
import java.sql.*;
import java.net.URI;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
class FinancialCoreMigrationTests {
 @Test void additiveUpgradeV17AndSecondStart()throws Exception {
  String url=System.getenv("FAC_TEST_DATASOURCE_URL");assertThat(url).isNotBlank();
  URI u=URI.create(url.substring(5));assertThat(u.getPath()).endsWith("_test");
  String user=System.getenv().getOrDefault("FAC_TEST_DATASOURCE_USERNAME","postgres"),password=System.getenv().getOrDefault("FAC_TEST_DATASOURCE_PASSWORD","postgres");
  String name="op20_upgrade_"+UUID.randomUUID().toString().replace("-","")+"_test";
  try(var admin=DriverManager.getConnection(url,user,password);var stmt=admin.createStatement()){
   stmt.execute("create database "+name);
   String fixture="jdbc:postgresql://"+u.getRawAuthority()+"/"+name;
   try{
    var previous=Flyway.configure().dataSource(fixture,user,password).target("17").cleanDisabled(true).load();previous.migrate();
    try(var c=DriverManager.getConnection(fixture,user,password);var s=c.createStatement()){s.execute("update riva set nome='OP20 preserve legacy' where id='CON'");}
    var current=Flyway.configure().dataSource(fixture,user,password).cleanDisabled(true).load();assertThat(current.migrate().migrationsExecuted).isEqualTo(3);current.validate();assertThat(current.info().current().getVersion().getVersion()).isEqualTo("20");assertThat(current.migrate().migrationsExecuted).isZero();
    try(var c=DriverManager.getConnection(fixture,user,password);var s=c.createStatement();var rs=s.executeQuery("select nome,(select count(*) from financial_position) from riva where id='CON'")){assertThat(rs.next()).isTrue();assertThat(rs.getString(1)).isEqualTo("OP20 preserve legacy");assertThat(rs.getLong(2)).isZero();}
   }finally{stmt.execute("drop database "+name+" with (force)");}
  }
 }
}
