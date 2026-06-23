package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.DocumentoComercialImpressaoDto;
import com.ar2lda.fac.controller.dto.DocumentoFinanceiroImpressaoDto;
import com.ar2lda.fac.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FiscalQrService {

    private final QrFiscalPayloadBuilder payloadBuilder;

    @Value("${fac.fiscal.qr.enabled:true}")
    private boolean enabled;

    @Value("${fac.fiscal.qr.hash-characters:YhGV}")
    private String hashCharacters;

    @Value("${fac.fiscal.software-certificate-number:9999}")
    private String certificateNumber;

    public QrFiscal buildDocumentoComercial(DocumentoComercialImpressaoDto impressao) {
        if (!enabled) {
            throw new BadRequestException("A geração do payload QR fiscal está desativada");
        }
        return new QrFiscal(
                payloadBuilder.buildDocumentoComercial(impressao, hashCharacters, certificateNumber),
                payloadBuilder.payloadVersion()
        );
    }

    public Optional<QrFiscal> buildDocumentoFinanceiro(DocumentoFinanceiroImpressaoDto impressao) {
        if (!enabled) {
            return Optional.empty();
        }
        try {
            return Optional.of(new QrFiscal(
                    payloadBuilder.buildDocumentoFinanceiro(impressao, hashCharacters, certificateNumber),
                    payloadBuilder.payloadVersion()
            ));
        } catch (BadRequestException exception) {
            return Optional.empty();
        }
    }

    public record QrFiscal(String payload, String version) {
    }
}
