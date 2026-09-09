package com.ar2lda.fac;

import com.ar2lda.fac.service.PostalCodesImporter;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostalCodesImporterTests {
    @Test
    void agrupaLocalidadesEEscolhePrincipalDeterministicamente() throws Exception {
        PostalCodesImporter.Report report = new PostalCodesImporter(null).inspect(zip("""
                PT\t7800-001\tBeja\tBeja\t02\tBeja\t0202\tBeja\t020201\t38\t-7\t1
                PT\t7800-001\tBairro\tBeja\t02\tBeja\t0202\tBeja\t020201\t38\t-7\t1
                PT\t7800-001\tBairro\tBeja\t02\tBeja\t0202\tBeja\t020201\t38\t-7\t1
                PT\t4000-001\tPorto\tPorto\t13\tPorto\t1306\tSé\t130612\t41\t-8\t1
                """));
        assertThat(report.importable()).isTrue();
        assertThat(report.uniqueCodes()).isEqualTo(2);
        assertThat(report.multiLocalityCodes()).isEqualTo(1);
        assertThat(report.localityAssociations()).isEqualTo(3);
        assertThat(report.duplicateLocalities()).isEqualTo(1);
        PostalCodesImporter.Group beja = report.groups().stream().filter(g -> g.code().equals("7800-001")).findFirst().orElseThrow();
        assertThat(beja.primaryLocality()).isEqualTo("Bairro");
        assertThat(beja.localities()).containsExactly("Bairro", "Beja");
    }

    @Test
    void rejeitaColisaoGeografica() throws Exception {
        PostalCodesImporter.Report report = new PostalCodesImporter(null).inspect(zip("""
                PT\t1000-001\tA\tLisboa\t11\tLisboa\t1106\tA\t1\t38\t-9\t1
                PT\t1000-001\tB\tPorto\t13\tPorto\t1306\tB\t2\t41\t-8\t1
                """));
        assertThat(report.importable()).isFalse();
        assertThat(report.geographicCollisions()).hasSize(1);
    }

    @Test
    void rejeitaLinhasEstruturaisInvalidas() throws Exception {
        PostalCodesImporter.Report report = new PostalCodesImporter(null).inspect(zip("""
                PT\t123\tSem formato\tD\t1\tC\t2\tF\t3\t0\t0\t1
                ES\t1000-001\tEspanha\tD\t1\tC\t2\tF\t3\t0\t0\t1
                PT\t1000-001\t\tD\t1\tC\t2\tF\t3\t0\t0\t1
                PT\t1000-001\tSó\tD\t1\tC
                """));
        assertThat(report.importable()).isFalse();
        assertThat(report.invalidCodes()).isEqualTo(1);
        assertThat(report.nonPortugalLines()).isEqualTo(1);
        assertThat(report.emptyNames()).isEqualTo(1);
        assertThat(report.malformedLines()).isEqualTo(1);
    }

    @Test
    void exigePtTxtNoZip() throws Exception {
        Path path = Files.createTempFile("postal", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(path))) {
            out.putNextEntry(new ZipEntry("README.txt")); out.write("x".getBytes()); out.closeEntry();
        }
        try {
            assertThatThrownBy(() -> new PostalCodesImporter(null).inspect(path))
                    .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("PT.txt");
        } finally { Files.deleteIfExists(path); }
    }

    @Test
    void preservaAcentosUtf8() throws Exception {
        PostalCodesImporter.Report report = new PostalCodesImporter(null).inspect(zip(
                "PT\t9000-001\tFunchal\tMadeira\t10\tFunchal\t1003\tSé\t100303\t32\t-16\t1\n"));
        PostalCodesImporter.Group group = report.groups().get(0);
        assertThat(group.primaryLocality()).isEqualTo("Funchal");
        assertThat(group.parish()).isEqualTo("Sé");
    }

    private Path zip(String content) throws Exception {
        Path path = Files.createTempFile("postal", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(path))) {
            out.putNextEntry(new ZipEntry("PT.txt"));
            out.write(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            out.closeEntry();
        }
        return path;
    }
}
