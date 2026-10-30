package com.ar2lda.fac.service;

import com.ar2lda.fac.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.Base64;
import static org.assertj.core.api.Assertions.*;

class FiscalQrIdentityTests {
    private static final String SIGNATURE=Base64.getEncoder().encodeToString(new byte[128]);
    @Test void extractsOneBasedPositionsWithoutHyphenOrGlobalDefault() {
        byte[] bytes=new byte[128];for(int i=0;i<bytes.length;i++) bytes[i]=(byte)i;
        String signature=Base64.getEncoder().encodeToString(bytes);
        var result=FiscalQrIdentity.derive(signature,"1234");
        assertThat(result.hashCharacters()).isEqualTo(""+signature.charAt(0)+signature.charAt(10)+signature.charAt(20)+signature.charAt(30));
        assertThat(result.certificateNumber()).isEqualTo("1234");
        assertThat(result.hashCharacters()).doesNotContain("-");
    }
    @ParameterizedTest @NullAndEmptySource @ValueSource(strings={"BAD","not a signature","YhGV"})
    void missingOrInvalidSignatureNeverFallsBack(String signature) {
        assertThatThrownBy(()->FiscalQrIdentity.derive(signature,"1234")).isInstanceOf(BadRequestException.class);
    }
    @ParameterizedTest @NullAndEmptySource @ValueSource(strings={"0","0000","text","12345"," 1234 "})
    void certificateMustComeFromAnExplicitValidSource(String certificate) {
        assertThatThrownBy(()->FiscalQrIdentity.derive(SIGNATURE,certificate)).isInstanceOf(BadRequestException.class);
    }
}
