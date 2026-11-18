package com.ar2lda.fac.financial;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "M03_DISPOSABLE", matches = "true")
class ProjectedFrLegacyContentionTests {
    private final String url = System.getenv("FAC_TEST_DATASOURCE_URL");
    private final String user = System.getenv("FAC_TEST_DATASOURCE_USERNAME");
    private final String password = System.getenv("FAC_TEST_DATASOURCE_PASSWORD");

    @Test void compareLegacyEmissionWritesBeforeAndAfterV31() throws Exception {
        assertThat(url).matches("jdbc:postgresql://127\\.0\\.0\\.1:[0-9]+/tuuli_fr_[0-9]+_test");
        try (var c = connect(); var s = c.createStatement();
             var r = s.executeQuery("select system_identifier::text from pg_control_system()")) {
            assertThat(r.next()).isTrue();
            assertThat(r.getString(1)).isEqualTo(System.getenv("M03_EXPECTED_CLUSTER"));
        }
        Flyway.configure().dataSource(url,user,password).target("30").load().migrate();
        fixture();
        measureVersion(30);
        Flyway.configure().dataSource(url,user,password).load().migrate();
        measureVersion(31);
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url,user,password);
    }

    private void fixture() throws SQLException {
        try (var c = connect(); var s = c.createStatement()) {
            s.execute("""
                insert into codpostal(id,nome) values('1000-020','Lisboa') on conflict do nothing;
                insert into transporte(id,nome) values('LCT','Teste');
                insert into armazem(id,nome,morada,id_codpostal,localidade,id_pais)
                values('LCA','Teste','Rua','1000-020','Lisboa','PT');
                insert into cliente(id,nome,morada,id_codpostal,nif,id_moeda,id_riva,id_transporte,id_pais)
                values(900001,'Teste','Rua','1000-020','999999990','EUR','CON','LCT','PT');
                insert into tipodocumento(id,descricao,area_gestao,entidade,sinal_contabilistico,codigo_fiscal,liquidacao_imediata)
                values('LFA','Teste',2,1,1,'FT',true),('LFB','Teste',2,1,1,'FT',true);
                insert into serie(id_tipo_documento,serie,nome,numerador,codigo_at,data_codigo_at)
                values('LFA','SHARED','Teste',0,'LCAT0001','2026-01-01'),
                      ('LFB','SHARED','Teste',0,'LCAT0002','2026-01-01'),
                      ('LFA','OTHER','Teste',0,'LCAT0003','2026-01-01'),
                      ('LFB','OTHER','Teste',0,'LCAT0004','2026-01-01');
                """);
        }
    }

    private void measureVersion(int version) throws Exception {
        measure(version,"same-series","LFB","SHARED");
        measure(version,"same-type","LFA","OTHER");
        measure(version,"distinct","LFB","OTHER");
    }

    private void measure(int version,String scenario,String type,String series) throws Exception {
        var pool = Executors.newSingleThreadExecutor();
        try (var first = connect(); var second = connect(); var monitor = connect()) {
            int a = pid(first), b = pid(second);
            first.setAutoCommit(false);
            second.setAutoCommit(false);
            int number = advance(first,"LFA","SHARED");
            String firstSeriesLocks = advisory(monitor,a);
            emit(first,"LFA","SHARED",number);
            String firstEmissionLocks = advisory(monitor,a);
            var started = new CountDownLatch(1);
            var result = pool.submit(() -> {
                long start = System.nanoTime();
                started.countDown();
                try {
                    int next = advance(second,type,series);
                    long afterSeries = System.nanoTime();
                    emit(second,type,series,next);
                    second.commit();
                    return "series_ms="+ms(start,afterSeries)+" total_ms="+ms(start,System.nanoTime())+" result=COMMIT number="+next;
                } catch (SQLException e) {
                    second.rollback();
                    return "total_ms="+ms(start,System.nanoTime())+" error="+e.getSQLState()+" "+e.getMessage();
                }
            });
            try {
                assertThat(started.await(3,TimeUnit.SECONDS)).isTrue();
                String wait = "";
                long deadline = System.nanoTime()+TimeUnit.SECONDS.toNanos(2);
                while (!result.isDone() && System.nanoTime()<deadline) {
                    try (var s = monitor.createStatement(); var r = s.executeQuery(
                        "select wait_event,pg_blocking_pids(pid)::text from pg_stat_activity where pid="+b)) {
                        if (r.next() && r.getString(2).equals("{"+a+"}")) {
                            wait = r.getString(1);
                            break;
                        }
                    }
                    Thread.sleep(10);
                }
                String secondLocks = advisory(monitor,b);
                if (!wait.isEmpty()) Thread.sleep(200);
                boolean finishedBeforeRelease = result.isDone();
                first.commit();
                String outcome = result.get(8,TimeUnit.SECONDS);
                System.out.printf("V%d %s first_series=[%s] first_emit=[%s] second=[%s] wait=%s before_release=%s first=COMMIT number=%d %s%n",
                    version,scenario,firstSeriesLocks,firstEmissionLocks,secondLocks,wait,finishedBeforeRelease,number,outcome);
                assertThat(outcome).contains("result=COMMIT");
                if (version==31 && !scenario.equals("distinct")) assertThat(wait).isEqualTo("advisory");
                if (scenario.equals("distinct") || (version==30 && scenario.equals("same-series"))) assertThat(finishedBeforeRelease).isTrue();
            } finally {
                first.rollback();
            }
        } finally {
            pool.shutdownNow();
        }
    }

    private static long ms(long from,long to) {
        return TimeUnit.NANOSECONDS.toMillis(to-from);
    }

    private static int pid(Connection c) throws SQLException {
        try (var s=c.createStatement();var r=s.executeQuery("select pg_backend_pid()")) {
            r.next();
            return r.getInt(1);
        }
    }

    private static String advisory(Connection c,int pid) throws SQLException {
        try (var s=c.createStatement();var r=s.executeQuery("""
            select k, granted from pg_locks
            join (select k,hashtextextended(k,0) h from unnest(array[
                'FISCAL_SERIES:SHARED','FISCAL_SERIES:OTHER','FISCAL_AT:LCAT0001','FISCAL_AT:LCAT0002',
                'FISCAL_AT:LCAT0003','FISCAL_AT:LCAT0004','FISCAL_CODE:LFA','FISCAL_CODE:LFB']) k) keys
            on classid::bigint=(h >> 32 & 4294967295) and objid::bigint=(h & 4294967295)
            where locktype='advisory' and pid=
            """+pid+" order by k")) {
            var locks = new java.util.ArrayList<String>();
            while(r.next()) locks.add(r.getString(1)+":"+r.getBoolean(2));
            return locks.stream().collect(Collectors.joining(","));
        }
    }

    private static int advance(Connection c,String type,String series) throws SQLException {
        try (var s=c.prepareStatement("update serie set numerador=numerador+1 where id_tipo_documento=? and serie=? returning numerador")) {
            s.setString(1,type);
            s.setString(2,series);
            try(var r=s.executeQuery()) {r.next();return r.getInt(1);}
        }
    }

    private static void emit(Connection c,String type,String series,int number) throws SQLException {
        try(var s=c.prepareStatement("""
            insert into documento_comercial(id_tipo_documento,serie,numero_documento,estado,data_emissao,id_cliente,
              id_armazem_carga,id_moeda,id_riva,id_ppagamento,id_transporte,cliente_nome,cliente_nif,cliente_morada,
              cliente_cod_postal,cliente_pais,data_carga,carga_nome,carga_morada,carga_cod_postal,carga_localidade,carga_pais,
              data_vencimento,valor_total,numero_documento_completo,tipo_documento_codigo,tipo_documento_codigo_fiscal)
            values(?,?,?,'EMITIDO','2026-01-01',900001,'LCA','EUR','CON','P30','LCT','Teste','999999990','Rua',
              '1000-020','PT','2026-01-01','Teste','Rua','1000-020','Lisboa','PT','2026-01-31',12.30,?,?,'FT')
            """)) {
            s.setString(1,type);s.setString(2,series);s.setInt(3,number);
            s.setString(4,type+" "+series+"/"+number);s.setString(5,type);
            s.executeUpdate();
        }
    }
}
