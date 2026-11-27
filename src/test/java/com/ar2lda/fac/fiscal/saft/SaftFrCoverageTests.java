package com.ar2lda.fac.fiscal.saft;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SaftFrCoverageTests {
    private JdbcTemplate db; private SaftFtExport exporter;
    private Map<String,Object> invoice,payment; private Element totals;
    @BeforeEach void setup() throws Exception {
        db=mock(JdbcTemplate.class);exporter=new SaftFtExport(db);
        var document=DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        totals=document.createElementNS(SaftFtExport.NAMESPACE,"DocumentTotals");document.appendChild(totals);
        invoice=new HashMap<>(Map.of("id",7L,"estado","EMITIDO","liquidado",true,"id_cliente",1101L,
                "id_mpagamento","CCD","valor_total",new BigDecimal("123.00"),"data_emissao",Date.valueOf("2026-11-26")));
        payment=new HashMap<>(Map.of("received",new BigDecimal("123.00"),"payment_method","CCD","cliente_id",1101L,
                "moeda_id","EUR","available",BigDecimal.ZERO,"cash_count",1L,"allocation_count",1L));
        when(db.queryForList(anyString(),any(Object[].class))).thenAnswer(invocation -> List.of(payment));
    }
    @Test void creditCardIsOnePaymentWithinInvoiceTotals() {
        exporter.addImmediatePayment(totals,invoice);
        assertThat(totals.getElementsByTagNameNS(SaftFtExport.NAMESPACE,"PaymentMechanism").item(0).getTextContent()).isEqualTo("CC");
        assertThat(totals.getElementsByTagNameNS(SaftFtExport.NAMESPACE,"PaymentAmount").item(0).getTextContent()).isEqualTo("123.00");
    }
    @Test void partialOrMismatchedPaymentBlocks() {
        payment.put("received",new BigDecimal("100.00"));
        assertThatThrownBy(() -> exporter.addImmediatePayment(totals,invoice)).isInstanceOf(SaftFtExport.Refusal.class).hasMessageContaining("total");
    }
    @Test void missingProofBlocks() {
        when(db.queryForList(anyString(),any(Object[].class))).thenReturn(List.of());
        assertThatThrownBy(() -> exporter.addImmediatePayment(totals,invoice)).isInstanceOf(SaftFtExport.Refusal.class).hasMessageContaining("prova original");
    }
    @Test void unknownPaymentAndUnsettledInvoiceBlock() {
        payment.put("payment_method","UNK");invoice.put("id_mpagamento","UNK");
        assertThatThrownBy(() -> exporter.addImmediatePayment(totals,invoice)).isInstanceOf(SaftFtExport.Refusal.class).hasMessageContaining("modo de pagamento");
        invoice.put("liquidado",false);
        assertThatThrownBy(() -> exporter.addImmediatePayment(totals,invoice)).isInstanceOf(SaftFtExport.Refusal.class).hasMessageContaining("quitação");
    }
}
