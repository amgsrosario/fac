package com.ar2lda.fac;

import com.ar2lda.fac.service.*;
import com.ar2lda.fac.model.*;
import com.ar2lda.fac.repository.*;
import org.junit.jupiter.params.ParameterizedTest;
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
}
