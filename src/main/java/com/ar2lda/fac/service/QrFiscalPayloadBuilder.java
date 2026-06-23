package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.DocumentoComercialDto;
import com.ar2lda.fac.controller.dto.DocumentoComercialImpressaoDto;
import com.ar2lda.fac.controller.dto.DocumentoFinanceiroDto;
import com.ar2lda.fac.controller.dto.DocumentoFinanceiroImpressaoDto;
import com.ar2lda.fac.controller.dto.EmpresaDto;
import com.ar2lda.fac.controller.dto.EmitenteFiscalSnapshotDto;
import com.ar2lda.fac.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QrFiscalPayloadBuilder {

    private static final DateTimeFormatter QR_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private final QrFiscalValidator validator;

    public String buildDocumentoComercial(
            DocumentoComercialImpressaoDto impressao,
            String hashCharacters,
            String certificateNumber
    ) {
        if (impressao == null || impressao.empresa() == null || impressao.documento() == null) {
            throw new BadRequestException("Documento comercial é obrigatório para gerar o QR fiscal");
        }

        EmitenteFiscalSnapshotDto empresa = impressao.empresa();
        DocumentoComercialDto documento = impressao.documento();
        validateDocumento(documento);

        List<String> fields = new ArrayList<>();
        add(fields, "A", requiredDigits(empresa.nif(), "NIF do emitente"));
        add(fields, "B", adquirenteNif(documento.clienteNif()));
        add(fields, "C", required(documento.clientePais(), "País do adquirente"));
        add(fields, "D", required(codigoFiscal(documento.tipoDocumentoCodigoFiscal(), documento.tipoDocumentoId()), "Tipo de documento fiscal"));
        add(fields, "E", "N");
        add(fields, "F", documento.dataEmissao().format(QR_DATE));
        add(fields, "G", required(documento.numeroDocumentoCompleto(), "Número completo do documento"));
        add(fields, "H", required(documento.atcud(), "ATCUD"));
        add(fields, "I1", "PT");
        addMoneyIfPositive(fields, "I2", documento.valorIsento());
        addMoneyIfPositive(fields, "I3", documento.valorSujeitoReduzida());
        addMoneyIfPositive(fields, "I4", documento.valorIvaReduzida());
        addMoneyIfPositive(fields, "I5", documento.valorSujeitoIntermedia());
        addMoneyIfPositive(fields, "I6", documento.valorIvaIntermedia());
        addMoneyIfPositive(fields, "I7", documento.valorSujeitoNormal());
        addMoneyIfPositive(fields, "I8", documento.valorIvaNormal());
        add(fields, "N", money(documento.valorIvaTotal()));
        add(fields, "O", money(documento.valorTotal()));
        addMoneyIfPositive(fields, "P", documento.valorRetencao());
        add(fields, "Q", required(hashCharacters, "4 caracteres do hash"));
        add(fields, "R", required(certificateNumber, "Número do certificado"));

        String payload = String.join("*", fields);
        validator.validate(payload);
        return payload;
    }

    public String payloadVersion() {
        return QrFiscalValidator.PAYLOAD_VERSION;
    }

    public String buildDocumentoFinanceiro(
            DocumentoFinanceiroImpressaoDto impressao,
            String hashCharacters,
            String certificateNumber
    ) {
        if (impressao == null || impressao.empresa() == null || impressao.cliente() == null || impressao.documento() == null) {
            throw new BadRequestException("Documento financeiro é obrigatório para gerar o QR fiscal");
        }

        DocumentoFinanceiroDto documento = impressao.documento();
        validateDocumentoFinanceiro(documento);
        List<String> fields = new ArrayList<>();

        add(fields, "A", requiredDigits(impressao.empresa().nif(), "NIF do emitente"));
        add(fields, "B", adquirenteNif(impressao.cliente().nif()));
        add(fields, "C", required(impressao.cliente().paisId(), "País do adquirente"));
        add(fields, "D", required(codigoFiscal(documento.tipoDocumentoCodigoFiscal(), documento.tipoDocumentoId()), "Tipo de documento fiscal"));
        add(fields, "E", "N");
        add(fields, "F", documento.dataEmissao().format(QR_DATE));
        add(fields, "G", documento.tipoDocumentoId() + " " + documento.serie() + "/" + documento.numeroDocumento());
        add(fields, "H", required(documento.atcud(), "ATCUD"));
        add(fields, "I1", "PT");
        add(fields, "N", money(BigDecimal.ZERO));
        add(fields, "O", money(documento.valorPagamentoLiquido()));
        add(fields, "Q", required(hashCharacters, "4 caracteres do hash"));
        add(fields, "R", required(certificateNumber, "Número do certificado"));

        String payload = String.join("*", fields);
        validator.validate(payload);
        return payload;
    }

    private void validateDocumento(DocumentoComercialDto documento) {
        if (documento.numeroDocumento() == null || documento.numeroDocumento() <= 0) {
            throw new BadRequestException("Documento comercial deve estar emitido para gerar o QR fiscal");
        }
        if (documento.dataEmissao() == null) {
            throw new BadRequestException("Data de emissão é obrigatória para gerar o QR fiscal");
        }
    }

    private void validateDocumentoFinanceiro(DocumentoFinanceiroDto documento) {
        if (documento.numeroDocumento() == null || documento.numeroDocumento() <= 0) {
            throw new BadRequestException("Documento financeiro deve estar emitido para gerar o QR fiscal");
        }
        if (documento.dataEmissao() == null) {
            throw new BadRequestException("Data de emissão é obrigatória para gerar o QR fiscal");
        }
    }

    private void add(List<String> fields, String code, String value) {
        fields.add(code + ":" + value);
    }

    private void addMoneyIfPositive(List<String> fields, String code, BigDecimal value) {
        if (value != null && value.compareTo(BigDecimal.ZERO) > 0) {
            add(fields, code, money(value));
        }
    }

    private String required(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(label + " é obrigatório para gerar o QR fiscal");
        }
        return value.trim();
    }

    private String requiredDigits(String value, String label) {
        String normalized = required(value, label).replaceAll("\\s+", "");
        if (!normalized.matches("\\d+")) {
            throw new BadRequestException(label + " deve conter apenas dígitos para gerar o QR fiscal");
        }
        return normalized;
    }

    private String adquirenteNif(String nif) {
        if (nif == null || nif.isBlank()) {
            return "999999990";
        }
        return nif.trim().replaceAll("\\s+", "");
    }

    private String codigoFiscal(String codigoFiscal, String fallback) {
        return codigoFiscal == null || codigoFiscal.isBlank() ? fallback : codigoFiscal;
    }

    private String money(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
