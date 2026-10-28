package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.DocumentoComercialImpressaoDto;
import com.ar2lda.fac.controller.dto.DocumentoFinanceiroImpressaoDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FiscalQrService {

    private final QrFiscalPayloadBuilder payloadBuilder;
    private final ObjectProvider<FiscalDocumentSignatureSource> signatureSources;

    @Value("${fac.fiscal.qr.enabled:true}")
    private boolean enabled;

    public QrFiscal buildDocumentoComercial(DocumentoComercialImpressaoDto impressao) {
        if (impressao==null || impressao.documento()==null)
            throw new BadRequestException("Documento comercial é obrigatório para gerar QR");
        if (!enabled) {
            throw new BadRequestException("A geração do payload QR fiscal está desativada");
        }
        var document=impressao.documento();
        var signature=source().commercial(impressao).orElseThrow(FiscalQrService::unavailable);
        var identity=identity(signature,document.id(),document.numeroDocumentoCompleto(),document.atcud());
        return new QrFiscal(
                payloadBuilder.buildDocumentoComercial(impressao, identity.hashCharacters(), identity.certificateNumber()),
                payloadBuilder.payloadVersion()
        );
    }

    public Optional<QrFiscal> buildDocumentoFinanceiro(DocumentoFinanceiroImpressaoDto impressao) {
        if (impressao==null || impressao.documento()==null) return Optional.empty();
        if (!enabled) {
            return Optional.empty();
        }
        try {
            var document=impressao.documento();
            var signature=source().financial(impressao).orElseThrow(FiscalQrService::unavailable);
            var identity=identity(signature,document.id(),document.tipoDocumentoId()+" "+document.serie()+"/"+document.numeroDocumento(),document.atcud());
            return Optional.of(new QrFiscal(
                    payloadBuilder.buildDocumentoFinanceiro(impressao, identity.hashCharacters(), identity.certificateNumber()),
                    payloadBuilder.payloadVersion()
            ));
        } catch (BadRequestException | ConflictException exception) {
            return Optional.empty();
        }
    }

    private FiscalDocumentSignatureSource source() {
        var candidates=signatureSources.stream().limit(2).toList();
        if (candidates.size()!=1) throw unavailable();
        return candidates.getFirst();
    }
    private FiscalQrIdentity identity(FiscalDocumentSignatureSource.Signature signature,Long id,String number,String atcud) {
        if (id==null || number==null || atcud==null || !Objects.equals(id,signature.documentId())
                || !Objects.equals(number,signature.documentNumber()) || !Objects.equals(atcud,signature.atcud()))
            throw new ConflictException("Assinatura fiscal não corresponde ao documento numerado");
        return FiscalQrIdentity.derive(signature.fullSignature(),signature.softwareCertificateNumber());
    }
    private static ConflictException unavailable() {
        return new ConflictException("Fonte de assinatura fiscal e certificado por documento indisponível; emissão QR bloqueada");
    }

    public record QrFiscal(String payload, String version) {
    }
}
