package com.ar2lda.fac;

import com.ar2lda.fac.service.*;
import com.ar2lda.fac.model.*;
import com.ar2lda.fac.repository.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.IntStream;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class EmissionReadinessServiceTests {
    @ParameterizedTest
    @CsvSource({"0,true,FONTE_ASSINATURA_AUSENTE", "2,true,FONTES_ASSINATURA_AMBIGUAS", "1,false,QR_DESACTIVADO"})
    void detectsKnownSourceAndQrGatesWithoutCallingSigner(int count, boolean enabled, String code) {
        var documents=mock(DocumentoComercialRepository.class);
        var series=mock(SerieRepository.class);
        var companies=mock(EmpresaRepository.class);
        var motor=mock(MotorFiscalService.class);
        var qr=mock(QrFiscalPayloadBuilder.class);
        @SuppressWarnings("unchecked") ObjectProvider<FiscalDocumentSignatureSource> sources=mock(ObjectProvider.class);
        var source=mock(FiscalDocumentSignatureSource.class);
        when(sources.stream()).thenAnswer(invocation->IntStream.range(0,count).mapToObj(i->source));
        var service=new EmissionReadinessService(documents,series,companies,motor,qr,sources,Clock.systemUTC());
        ReflectionTestUtils.setField(service,"qrEnabled",enabled);
        var document=mock(DocumentoComercial.class,RETURNS_DEEP_STUBS);
        when(document.getEstado()).thenReturn(EstadoDocumentoComercial.RASCUNHO);
        when(document.getTipoDocumento().getId()).thenReturn("FT");
        when(document.getSerie()).thenReturn("A");
        when(document.getMoeda().getId()).thenReturn("EUR");
        when(document.getValorTotal()).thenReturn(BigDecimal.ZERO);
        var result=service.evaluate(document,List.of());
        assertThat(result.elegivel()).isFalse();
        assertThat(result.impedimentos()).extracting(m->m.codigo()).contains(code);
        verifyNoInteractions(source);
    }

    @ParameterizedTest
    @ValueSource(strings={"EMITIDO","ANULADO"})
    void nonDraftsNeverRecalculateOrDisplayIssuanceWarnings(String state) {
        var documents=mock(DocumentoComercialRepository.class);
        var series=mock(SerieRepository.class);
        var companies=mock(EmpresaRepository.class);
        var motor=mock(MotorFiscalService.class);
        var qr=mock(QrFiscalPayloadBuilder.class);
        @SuppressWarnings("unchecked") ObjectProvider<FiscalDocumentSignatureSource> sources=mock(ObjectProvider.class);
        var service=new EmissionReadinessService(documents,series,companies,motor,qr,sources,Clock.systemUTC());
        var document=mock(DocumentoComercial.class);
        when(document.getEstado()).thenReturn(EstadoDocumentoComercial.valueOf(state));
        when(document.getValorTotal()).thenReturn(new BigDecimal("12.30"));
        var result=service.evaluate(document,List.of());
        assertThat(result.elegivel()).isFalse();
        assertThat(result.impedimentos()).extracting(m->m.codigo()).containsExactly("ESTADO_INCOMPATIVEL");
        assertThat(result.avisos()).isEmpty();
        assertThat(result.confirmacaoRecalculoNecessaria()).isFalse();
        assertThat(result.totalProjectado()).isNull();
        verifyNoInteractions(documents,series,companies,motor,qr,sources);
    }

    @ParameterizedTest
    @ValueSource(strings={"SEM_LINHAS_COMERCIAIS","DATA_EMISSAO_ANTERIOR_SERIE","MOEDA_NAO_HABILITADA","SERIE_AUSENTE","EMPRESA_AUSENTE","DADOS_QR_INVALIDOS","TOTAL_NAO_POSITIVO_PARA_PENDENTE","DOCUMENTO_ANULADO"})
    void coversRemainingKnownBlockers(String code) {
        var documents=mock(DocumentoComercialRepository.class);
        var series=mock(SerieRepository.class);
        var companies=mock(EmpresaRepository.class);
        var motor=mock(MotorFiscalService.class);
        var qr=mock(QrFiscalPayloadBuilder.class);
        @SuppressWarnings("unchecked") ObjectProvider<FiscalDocumentSignatureSource> sources=mock(ObjectProvider.class);
        var source=mock(FiscalDocumentSignatureSource.class);
        when(sources.stream()).thenAnswer(invocation->java.util.stream.Stream.of(source));
        var service=new EmissionReadinessService(documents,series,companies,motor,qr,sources,Clock.systemUTC());
        ReflectionTestUtils.setField(service,"qrEnabled",true);
        var document=mock(DocumentoComercial.class,RETURNS_DEEP_STUBS);
        when(document.getEstado()).thenReturn(EstadoDocumentoComercial.RASCUNHO);
        when(document.getTipoDocumento().getId()).thenReturn("FT");
        when(document.getSerie()).thenReturn("A");
        when(document.getMoeda().getId()).thenReturn("EUR");
        when(document.getDataEmissao()).thenReturn(LocalDate.of(2026,11,5));
        when(document.getValorTotal()).thenReturn(BigDecimal.ZERO);
        if(code.equals("DATA_EMISSAO_ANTERIOR_SERIE")) when(documents.findUltimaDataEmissao("FT","A")).thenReturn(LocalDate.of(2026,11,6));
        if(code.equals("MOEDA_NAO_HABILITADA")) when(document.getMoeda().getId()).thenReturn("USD");
        if(code.equals("DOCUMENTO_ANULADO")) when(document.isAnulado()).thenReturn(true);
        if(code.equals("DADOS_QR_INVALIDOS")) {
            var company=mock(Empresa.class);
            when(companies.findById(Empresa.EMPRESA_ID)).thenReturn(Optional.of(company));
            doThrow(new com.ar2lda.fac.exception.BadRequestException("NIF sintético inválido")).when(qr).validateKnownCommercialInputs(any(),any(),any(),any(),any());
        }
        var result=service.evaluate(document,List.of());
        assertThat(result.elegivel()).isFalse();
        assertThat(result.impedimentos()).extracting(m->m.codigo()).contains(code);
        verifyNoInteractions(source);
    }
}
