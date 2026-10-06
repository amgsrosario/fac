package com.ar2lda.fac.security;

import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.regex.*;
import static org.assertj.core.api.Assertions.*;

class SecurityHeadersConfigTests {
    @Test void everyLocationInheritsTheRequiredHeadersAndCspDoesNotAllowInlineScripts() throws Exception {
        for(String file : new String[]{"deploy/demo/nginx.conf","deploy/nginx.conf"}) {
            String config=Files.readString(Path.of(file));
            for(String header : new String[]{"X-Content-Type-Options","X-Frame-Options","Referrer-Policy","Cache-Control","Content-Security-Policy"})
                assertThat(config).containsPattern("add_header "+header+" [^\n]+ always;");
            var locations=Pattern.compile("location[^\\{]+\\{([^}]+)}",Pattern.DOTALL).matcher(config);
            int count=0;
            while(locations.find()) {count++;assertThat(locations.group(1)).doesNotContain("add_header");}
            assertThat(count).isEqualTo(3);
            assertThat(config).contains("script-src 'self'").doesNotContain("script-src 'self' 'unsafe-inline'");
            assertThat(config).doesNotContain("Access-Control-Allow-Origin");
        }
    }
}
