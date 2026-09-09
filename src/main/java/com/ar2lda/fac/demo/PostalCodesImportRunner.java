package com.ar2lda.fac.demo;

import com.ar2lda.fac.service.PostalCodesImporter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Arrays;

@Component
@ConditionalOnProperty(name = "fac.postal-import.enabled", havingValue = "true")
@RequiredArgsConstructor
public class PostalCodesImportRunner implements ApplicationRunner {
    private final PostalCodesImporter importer;
    private final ConfigurableApplicationContext context;

    @Override
    public void run(ApplicationArguments args) {
        String file = context.getEnvironment().getProperty("fac.postal-import.file", "");
        boolean dryRun = context.getEnvironment().getProperty("fac.postal-import.dry-run", Boolean.class, true);
        boolean confirmed = context.getEnvironment().getProperty("fac.postal-import.confirm", Boolean.class, false);
        if (file.isBlank()) throw new IllegalArgumentException("Defina fac.postal-import.file");
        PostalCodesImporter.Report report = dryRun ? importer.inspect(Path.of(file)) : runImport(Path.of(file), confirmed);
        print(report, dryRun);
        context.close();
    }

    private PostalCodesImporter.Report runImport(Path path, boolean confirmed) {
        if (!confirmed) throw new IllegalArgumentException("A importação real exige fac.postal-import.confirm=true");
        if (!"import".equals(context.getEnvironment().getProperty("fac.postal-import.mode", "")))
            throw new IllegalArgumentException("A importação real exige fac.postal-import.mode=import");
        if (!Arrays.asList(context.getEnvironment().getActiveProfiles()).contains("demo"))
            throw new IllegalArgumentException("A importação real exige spring.profiles.active=demo");
        return importer.importData(path);
    }

    private void print(PostalCodesImporter.Report r, boolean dryRun) {
        System.out.printf("POSTAL_IMPORT %s | linhas=%d válidas=%d inválidas=%d códigos=%d localidades=%d colisões=%d%n",
                dryRun ? "DRY_RUN" : "COMPLETED", r.linesRead(), r.validLines(), r.invalidLines(), r.uniqueCodes(), r.localityAssociations(), r.geographicCollisions().size());
        System.out.printf("  duplicados_localidade=%d multi_localidade=%d max_multiplicidade=%d comprimentos=%d/%d/%d/%d%n",
                r.duplicateLocalities(), r.multiLocalityCodes(), r.maxLocalityMultiplicity(), r.maxLocalityLength(),
                r.maxDistrictLength(), r.maxMunicipalityLength(), r.maxParishLength());
        r.groups().stream().filter(g -> g.localities().size() > 1).limit(5)
                .forEach(g -> System.out.println("  multi-localidade: " + g.code() + " -> " + g.localities()));
        if (!r.errors().isEmpty()) r.errors().stream().limit(10).forEach(e -> System.out.println("  erro: " + e));
    }
}
