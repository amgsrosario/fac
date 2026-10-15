package com.ar2lda.fac.financial;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class ReceiptPayloadCompatibilityTests {
    final ObjectMapper json=new ObjectMapper();
    @Test void preValueDatePayloadReplaysOnlyWithDefaultDate() {
        String stored="{\"dataEmissao\":\"2026-02-01\",\"amount\":30}";
        assertThat(FinancialReceiptIssuer.samePayload(json,"{\"dataValor\":\"2026-02-01\",\"dataEmissao\":\"2026-02-01\",\"amount\":30}",stored)).isTrue();
        assertThat(FinancialReceiptIssuer.samePayload(json,"{\"dataValor\":\"2026-01-10\",\"dataEmissao\":\"2026-02-01\",\"amount\":30}",stored)).isFalse();
    }
    @Test void compatibilityCannotHideDifferentFinancialContent() {
        assertThat(FinancialReceiptIssuer.samePayload(json,"{\"dataValor\":\"2026-02-01\",\"dataEmissao\":\"2026-02-01\",\"amount\":300}","{\"dataEmissao\":\"2026-02-01\",\"amount\":30}")).isFalse();
    }

    @Test void largeDecimalsCannotCollapseToTheSameFloatingPointValue() {
        String stored="{\"dataEmissao\":\"2026-02-01\",\"amount\":99999999999999999.98}";
        assertThat(FinancialReceiptIssuer.samePayload(json,"{\"dataEmissao\":\"2026-02-01\",\"amount\":99999999999999999.99,\"dataValor\":\"2026-02-01\"}",stored)).isFalse();
        assertThat(FinancialReceiptIssuer.samePayload(json,"{\"dataEmissao\":\"2026-02-01\",\"amount\":99999999999999999.98,\"dataValor\":\"2026-02-01\"}",stored)).isTrue();
    }
    @Test void legacyCompatibilityPreservesStrictDecimalRepresentation() {
        String stored="{\"dataEmissao\":\"2026-02-01\",\"amount\":30.00}";
        assertThat(FinancialReceiptIssuer.samePayload(json,"{\"dataEmissao\":\"2026-02-01\",\"amount\":30.0,\"dataValor\":\"2026-02-01\"}",stored)).isFalse();
        assertThat(FinancialReceiptIssuer.samePayload(json,"{\"dataEmissao\":\"2026-02-01\",\"amount\":30.00,\"dataValor\":\"2026-02-01\"}",stored)).isTrue();
    }
}
