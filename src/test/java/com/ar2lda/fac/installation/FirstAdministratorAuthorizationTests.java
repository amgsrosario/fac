package com.ar2lda.fac.installation;

import org.junit.jupiter.api.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class FirstAdministratorAuthorizationTests {
    Path directory,file;
    @BeforeEach void setup() throws Exception {
        Files.createDirectories(Path.of("target"));directory=Files.createTempDirectory(Path.of("target").toAbsolutePath(),"m02-authorization-");
        file=directory.resolve("authorization.properties");
        Files.writeString(file,"jdbcUrl=jdbc:postgresql://127.0.0.1:25432/synthetic_test\noperator=synthetic\ndatabase=synthetic_test\ncluster=123\ninstallation="+UUID.randomUUID()+"\nreference=SYNTHETIC\nactor=Synthetic operator\nexpiresAt="+Instant.now().plusSeconds(600)+"\n");
        Files.setPosixFilePermissions(file,PosixFilePermissions.fromString("rw-------"));
    }
    @AfterEach void dispose() throws Exception {Files.deleteIfExists(directory.resolve("link"));Files.deleteIfExists(directory.resolve("redirected-input"));Files.deleteIfExists(file);Files.deleteIfExists(directory);}
    @Test void protectedExactAuthorizationLoads() throws Exception {assertThat(FirstAdministratorAuthorization.load(file).database()).isEqualTo("synthetic_test");}
    @Test void readableByOtherUsersIsRejected() throws Exception {
        Files.setPosixFilePermissions(file,PosixFilePermissions.fromString("rw-r--r--"));
        assertThatThrownBy(()->FirstAdministratorAuthorization.load(file)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void symbolicLinkIsRejected() throws Exception {
        Path link=directory.resolve("link");Files.createSymbolicLink(link,file);
        assertThatThrownBy(()->FirstAdministratorAuthorization.load(link)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void duplicateAndUnexpectedFieldsAreRejected() throws Exception {
        Files.writeString(file,"operator=other\n",StandardOpenOption.APPEND);
        assertThatThrownBy(()->FirstAdministratorAuthorization.load(file)).isInstanceOf(IllegalArgumentException.class);
        Files.writeString(file,"password=not-a-secret\n",StandardOpenOption.APPEND);
        assertThatThrownBy(()->FirstAdministratorAuthorization.load(file)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void nonLoopbackUrlAndUrlParametersAreRejected() {
        for(String url:new String[]{"jdbc:postgresql://other:5432/synthetic_test","jdbc:postgresql://127.0.0.1:5432/synthetic_test?password=bad"}) {
            assertThatThrownBy(()->new FirstAdministratorAuthorization(url,"synthetic","synthetic_test","123",UUID.randomUUID(),"ref","actor",Instant.now().plusSeconds(300))).isInstanceOf(IllegalArgumentException.class);
        }
    }
    @Test void cliWithoutConsoleFailsWithoutStartingApplication() throws Exception {
        Path redirected=directory.resolve("redirected-input");
        String confirmation="PROVISIONAR "+FirstAdministratorAuthorization.load(file).installation();
        Files.writeString(redirected,confirmation+"\nSYNTHETIC-NOT-A-CREDENTIAL\nADMIN\nSynthetic\nadmin@example.test\nSynthetic8!\nSynthetic8!\n");
        var process=new ProcessBuilder(Path.of(System.getProperty("java.home"),"bin","java").toString(),"-cp",System.getProperty("java.class.path"),FirstAdministratorCli.class.getName(),"--authorization",file.toString()).redirectInput(redirected.toFile()).redirectErrorStream(true).start();
        String message=new String(process.getInputStream().readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
        assertThat(process.waitFor()).isEqualTo(3);
        assertThat(message).contains("Consola terminal local obrigatoria").doesNotContain("Confirmar:","Password do operador","Started","jdbc:","SYNTHETIC-NOT-A-CREDENTIAL");
    }
}
