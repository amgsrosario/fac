package com.ar2lda.fac.fiscal.saft;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.xml.parsers.DocumentBuilderFactory;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SaftReceiptCoverageTests {
    private JdbcTemplate db;
    private SaftFtExport exporter;
    private SaftFtExport.Evidence evidence;
    private Map<String,Object> invoice,receipt,line;
    private final LocalDate date=LocalDate.of(2026,11,26);
    private SaftFtExport.Installation installation;

    @BeforeEach void setUp() {
        db=mock(JdbcTemplate.class); exporter=new SaftFtExport(db); evidence=mock(SaftFtExport.Evidence.class);
        when(evidence.verifiesReceipt(3)).thenReturn(true);
        var day=Date.valueOf(date);
        invoice=new HashMap<>(Map.of("id",7L,"estado","EMITIDO","id_cliente",1101L,"numero_documento",7L,
                "serie","DEMO26","data_emissao",day,"valor_total",money("14.13"),"numero_documento_completo","FT DEMO26/7"));
        receipt=new HashMap<>(Map.of("id_cliente",1101L,"id_tipo_documento","RC","serie","DEMO26","numero_documento",3L,
                "data_emissao",day,"atcud","DEMO-RC-3","id_emissor","admin.demo",
                "momento_emissao",Timestamp.from(Instant.parse("2026-11-26T10:00:00Z")),
                "valor_pagamento_bruto",money("14.13"),"valor_pagamento_liquido",money("14.13")));
        receipt.put("valor_desconto_financeiro",BigDecimal.ZERO);
        line=new HashMap<>(Map.of("id_documento_comercial",7L,"id_moeda","EUR","id_tipo_documento","FT",
                "numero_documento",7L,"serie_documento","DEMO26","data_documento",day,
                "valor_documento",money("14.13"),"valor_pendente_antes",money("14.13"),
                "valor_pagamento_bruto",money("14.13"),"valor_pagamento_liquido",money("14.13")));
        line.put("valor_pendente",BigDecimal.ZERO); line.put("novo_valor_pendente",BigDecimal.ZERO);
        line.put("valor_desconto_financeiro",BigDecimal.ZERO);
        when(db.queryForMap(anyString(),any(Object[].class))).thenReturn(receipt);
        when(db.queryForList(anyString(),any(Object[].class))).thenReturn(List.of(line));
        installation=new SaftFtExport.Installation(date,date,date,ZoneId.of("Europe/Lisbon"),"Global","id","509999999","demo","1",1,Map.of("FT","FT"),Set.of(),false);
    }
    private BigDecimal money(String value) { return new BigDecimal(value); }
    private List<SaftFtExport.ReceiptReconciliation> run() throws Exception {
        var document=DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        var source=document.createElementNS(SaftFtExport.NAMESPACE,"SourceDocuments"); document.appendChild(source);
        return exporter.receipts(source,List.of(new SaftFtExport.Item("financial",3,"RC",null,"EUR","EMITIDO")),List.of(invoice),evidence,installation);
    }
    @Test void fullReceiptIsLinkedToInvoice() throws Exception {
        assertThat(run()).containsExactly(new SaftFtExport.ReceiptReconciliation("RC DEMO26/3","FT DEMO26/7",money("14.13")));
    }
    @Test void missingEvidenceBlocks() {
        when(evidence.verifiesReceipt(3)).thenReturn(false);
        assertThatThrownBy(this::run).isInstanceOf(SaftFtExport.Refusal.class).hasMessageContaining("prova original");
    }
    @Test void partialReceiptBlocks() {
        line.put("valor_pagamento_liquido",money("1.00"));
        assertThatThrownBy(this::run).isInstanceOf(SaftFtExport.Refusal.class).hasMessageContaining("parcial");
    }
    @Test void discountBlocks() {
        line.put("valor_desconto_financeiro",money("1.00"));
        assertThatThrownBy(this::run).isInstanceOf(SaftFtExport.Refusal.class).hasMessageContaining("desconto");
    }
    @Test void mismatchedInvoiceBlocks() {
        line.put("id_documento_comercial",8L);
        assertThatThrownBy(this::run).isInstanceOf(SaftFtExport.Refusal.class).hasMessageContaining("ligação");
    }
    @Test void inventoryRejectsVoidedOrForeignCurrencyReceipts() {
        when(evidence.supportsFullReceipts()).thenReturn(true);
        assertThat(SaftFtExport.isCovered(new SaftFtExport.Item("financial",3,"RC",null,"EUR","ANULADO"),installation,evidence)).isFalse();
        assertThat(SaftFtExport.isCovered(new SaftFtExport.Item("financial",3,"RC",null,"USD","EMITIDO"),installation,evidence)).isFalse();
        assertThat(SaftFtExport.isCovered(new SaftFtExport.Item("financial",3,"OTHER",null,"EUR","EMITIDO"),installation,evidence)).isFalse();
    }
}
