package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.*;
import com.ar2lda.fac.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FiscalQrServiceTests {
    @SuppressWarnings("unchecked")
    private FiscalQrService service(FiscalDocumentSignatureSource... sources) {
        ObjectProvider<FiscalDocumentSignatureSource> provider=mock(ObjectProvider.class);
        when(provider.stream()).thenAnswer(x->Arrays.stream(sources));
        var result=new FiscalQrService(new QrFiscalPayloadBuilder(new QrFiscalValidator()),provider);
        ReflectionTestUtils.setField(result,"enabled",true);return result;
    }
    private DocumentoComercialImpressaoDto document(long id) {
        var d=mock(DocumentoComercialDto.class);var company=mock(EmitenteFiscalSnapshotDto.class);
        when(company.nif()).thenReturn("500000000");when(d.id()).thenReturn(id);when(d.numeroDocumento()).thenReturn(id);
        when(d.tipoDocumentoCodigoFiscal()).thenReturn("FT");when(d.numeroDocumentoCompleto()).thenReturn("FT TEST/"+id);
        when(d.atcud()).thenReturn("TEST0001-"+id);when(d.dataEmissao()).thenReturn(LocalDate.of(2026,10,28));
        when(d.clientePais()).thenReturn("PT");when(d.valorTotal()).thenReturn(new BigDecimal("123.00"));
        return new DocumentoComercialImpressaoDto(company,d,List.of());
    }
    private FiscalDocumentSignatureSource.Signature signature(DocumentoComercialImpressaoDto d) {
        byte[] bytes=new byte[128];new Random(d.documento().id()).nextBytes(bytes);
        return new FiscalDocumentSignatureSource.Signature(d.documento().id(),d.documento().numeroDocumentoCompleto(),d.documento().atcud(),Base64.getEncoder().encodeToString(bytes),"1234");
    }
    @Test void noSourceDoesNotUseFormerDemoProperties() {
        assertThatThrownBy(()->service().buildDocumentoComercial(document(10))).isInstanceOf(ConflictException.class);
    }
    @Test void ambiguousSourceFailsClosed() {
        FiscalDocumentSignatureSource source=d->Optional.of(signature(d));
        assertThatThrownBy(()->service(source,source).buildDocumentoComercial(document(10))).isInstanceOf(ConflictException.class);
    }
    @Test void qrUsesTheRecordedSignatureAndCertificateOfEachDocument() {
        FiscalDocumentSignatureSource source=d->Optional.of(signature(d));var service=service(source);
        for(long id:List.of(10L,11L)) {
            var d=document(id);var signature=signature(d);String full=signature.fullSignature();
            String expected=""+full.charAt(0)+full.charAt(10)+full.charAt(20)+full.charAt(30);
            assertThat(service.buildDocumentoComercial(d).payload()).contains("*Q:"+expected+"*R:1234");
        }
    }
    @Test void signatureOfAnotherDocumentCannotBeBorrowed() {
        var wrong=signature(document(11));FiscalDocumentSignatureSource source=d->Optional.of(wrong);
        assertThatThrownBy(()->service(source).buildDocumentoComercial(document(10))).isInstanceOf(ConflictException.class);
    }
    @Test void absentSignatureIsAControlledRefusal() {
        assertThatThrownBy(()->service(d->Optional.empty()).buildDocumentoComercial(document(10))).isInstanceOf(ConflictException.class);
    }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings={"number","atcud"})
    void changedIdentityCannotReuseARecordedSignature(String field) {
        var original=signature(document(10));
        var wrong=new FiscalDocumentSignatureSource.Signature(original.documentId(),
                field.equals("number")?"FT OTHER/10":original.documentNumber(),
                field.equals("atcud")?"OTHER000-10":original.atcud(),original.fullSignature(),original.softwareCertificateNumber());
        assertThatThrownBy(()->service(d->Optional.of(wrong)).buildDocumentoComercial(document(10))).isInstanceOf(ConflictException.class);
    }
    @Test void financialOptionalPathDoesNotInventIdentityWhenSourceIsAbsent() {
        var input=new DocumentoFinanceiroImpressaoDto(null,null,mock(DocumentoFinanceiroDto.class));
        assertThat(service().buildDocumentoFinanceiro(input)).isEmpty();
    }
    @Test void invalidInputIsControlledWithoutConsultingAProvider() {
        assertThatThrownBy(()->service().buildDocumentoComercial(null)).isInstanceOf(com.ar2lda.fac.exception.BadRequestException.class);
        assertThat(service().buildDocumentoFinanceiro(null)).isEmpty();
    }
    @Test void disabledQrDoesNotInvokeSigningSource() {
        var service=service();ReflectionTestUtils.setField(service,"enabled",false);
        assertThatThrownBy(()->service.buildDocumentoComercial(document(10))).isInstanceOf(com.ar2lda.fac.exception.BadRequestException.class);
    }
}
