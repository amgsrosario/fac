package com.ar2lda.fac.fiscal.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommercialServiceDateTests extends ProjectedFrFixture {
    @Test void issuesWithLaterServiceDateAndProtectsThePersistedValue() throws Exception {
        long id = create("SYNTH", admin);
        var payload = (ObjectNode) json.readTree(draft("SYNTH", "M3ART", 1)).get("documento");
        payload.put("dataPrestacao", "invalid");
        http.perform(auth(put(doc(id)), admin).contentType("application/json").content(payload.toString()))
                .andExpect(status().isBadRequest());
        payload.put("dataPrestacao", "2026-11-05");
        var saved = body(http.perform(auth(put(doc(id)), admin).contentType("application/json").content(payload.toString()))
                .andExpect(status().isOk()).andReturn());
        assertThat(saved.path("dataPrestacao").asText()).isEqualTo("2026-11-05");

        var issued = issue(id);
        assertThat(issued.path("dataPrestacao").asText()).isEqualTo("2026-11-05");
        assertThat(pdf(id)).contains("Data de prestação 05/11/2026");
        http.perform(auth(put(doc(id)), admin).contentType("application/json").content(payload.toString()))
                .andExpect(status().isConflict());
        assertThatThrownBy(() -> db.update("update documento_comercial set data_prestacao=null where id=?", id))
                .isInstanceOf(DataIntegrityViolationException.class).hasMessageContaining("Data de prestação imutável");
        assertThatThrownBy(() -> db.update("update documento_comercial set data_prestacao=date '2026-11-06' where id=?", id))
                .isInstanceOf(DataIntegrityViolationException.class).hasMessageContaining("Data de prestação imutável");
        assertThat(db.queryForObject("select data_prestacao from documento_comercial where id=?", LocalDate.class, id))
                .isEqualTo(LocalDate.of(2026,11,5));
    }

    @Test void hidesAbsentAndEqualServiceDatesWithoutChangingPrintedTextOrTotals() throws Exception {
        long absent = create("SYNTH", admin);
        var payload = (ObjectNode) json.readTree(draft("SYNTH", "M3ART", 1));
        ((ObjectNode) payload.get("documento")).put("dataPrestacao", "2026-11-03");
        long equal = body(http.perform(auth(post("/documentos-comerciais"), admin).contentType("application/json")
                .content(payload.toString())).andExpect(status().isCreated()).andReturn()).get("id").asLong();
        var absentIssue = issue(absent);
        var equalIssue = issue(equal);
        assertThat(db.queryForObject("select data_prestacao from documento_comercial where id=?", LocalDate.class, absent))
                .isNull();
        assertThat(absentIssue.path("valorTotal")).isEqualTo(equalIssue.path("valorTotal"));
        assertThat(absentIssue.path("valorIvaTotal")).isEqualTo(equalIssue.path("valorIvaTotal"));
        assertThatThrownBy(() -> db.update("update documento_comercial set data_prestacao=date '2026-11-02' where id=?", absent))
                .isInstanceOf(DataIntegrityViolationException.class).hasMessageContaining("Data de prestação imutável");
        assertThat(normalizeIdentity(pdf(absent), absentIssue)).doesNotContain("Data de prestação")
                .isEqualTo(normalizeIdentity(pdf(equal), equalIssue));
    }

    private JsonNode issue(long id) throws Exception {
        return body(http.perform(auth(post(doc(id) + "/emitir"), admin).contentType("application/json")
                .content("{\"emissorId\":\"M3ADMIN\"}")).andExpect(status().isOk()).andReturn());
    }

    private String pdf(long id) throws Exception {
        var bytes = http.perform(auth(get(doc(id) + "/pdf"), admin))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        try (var document = Loader.loadPDF(bytes)) {
            return new PDFTextStripper().getText(document);
        }
    }

    private String normalizeIdentity(String text, JsonNode issued) {
        return text.replace(issued.path("numeroDocumentoCompleto").asText(), "[NÚMERO]")
                .replace(issued.path("atcud").asText(), "[ATCUD]")
                .replace(issued.path("momentoEmissao").asText(), "[INSTANTE]");
    }
}
