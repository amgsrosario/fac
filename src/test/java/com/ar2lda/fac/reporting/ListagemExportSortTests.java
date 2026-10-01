package com.ar2lda.fac.reporting;

import com.ar2lda.fac.reporting.listagens.ListagemTabularExporter;
import com.ar2lda.fac.service.ListagensService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ListagemExportSortTests {
    @Test
    void exportSortUsesTotalKeysForEveryAnalyticalSource() throws Exception {
        Method method = ListagemTabularExporter.class.getDeclaredMethod("exportSort", String.class);
        method.setAccessible(true);
        var exporter = new ListagemTabularExporter(mock(ListagensService.class));
        assertThat(properties(method, exporter, "documentos-comerciais")).containsExactly("dataEmissao", "id");
        assertThat(properties(method, exporter, "linhas-comerciais")).containsExactly("documentoComercial.dataEmissao", "documentoComercial.id", "id");
        assertThat(properties(method, exporter, "documentos-financeiros")).containsExactly("dataEmissao", "id");
        assertThat(properties(method, exporter, "linhas-financeiras")).containsExactly("documentoFinanceiro.dataEmissao", "documentoFinanceiro.id", "id");
    }

    private java.util.List<String> properties(Method method, Object target, String source) throws Exception {
        return ((Sort) method.invoke(target, source)).stream().map(Sort.Order::getProperty).toList();
    }
}
