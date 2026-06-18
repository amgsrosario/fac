package com.ar2lda.fac.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "documento_financeiro")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class DocumentoFinanceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(length = 10, nullable = false)
    @ToString.Include
    private String serie;

    @Column(nullable = false)
    @ToString.Include
    private Long numeroDocumento;

    @Column(name = "codigo_validacao_at", length = 100)
    @Setter(AccessLevel.NONE)
    private String codigoValidacaoAt;

    @Column(name = "atcud", length = 150)
    @Setter(AccessLevel.NONE)
    private String atcud;

    @Column(name = "qr_payload", columnDefinition = "TEXT")
    @Setter(AccessLevel.NONE)
    private String qrPayload;

    @Column(name = "qr_payload_version", length = 20)
    @Setter(AccessLevel.NONE)
    private String qrPayloadVersion;

    @Column(nullable = false)
    private LocalDate dataEmissao;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_moeda", nullable = false)
    private Moeda moeda;

    @Column(precision = 19, scale = 6, nullable = false)
    private BigDecimal valorPagamentoBruto = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    private BigDecimal valorDescontoFinanceiro = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    private BigDecimal valorPagamentoLiquido = BigDecimal.ZERO;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mpagamento", nullable = false)
    private MPagamento mPagamento;

    @Column(nullable = false)
    private OffsetDateTime dataHoraOperacao;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_emissor", nullable = false)
    private Utilizador emissor;

    @Column(nullable = false)
    private OffsetDateTime momentoEmissao;

    @Column(length = 250)
    private String observacoes;

    @Column(nullable = false)
    private boolean anulado = false;

    @Column(nullable = false)
    private boolean impresso = false;

    public void atribuirAtcud(String codigoValidacaoAt, String atcud) {
        if (this.atcud != null || this.codigoValidacaoAt != null) {
            throw new IllegalStateException("O ATCUD do documento financeiro já foi atribuído");
        }
        if (codigoValidacaoAt == null || codigoValidacaoAt.isBlank() || atcud == null || atcud.isBlank()) {
            throw new IllegalArgumentException("Código de validação da AT e ATCUD são obrigatórios");
        }
        this.codigoValidacaoAt = codigoValidacaoAt.trim();
        this.atcud = atcud.trim();
    }

    public void atribuirQrFiscal(String qrPayload, String qrPayloadVersion) {
        if (this.qrPayload != null || this.qrPayloadVersion != null) {
            throw new IllegalStateException("O payload QR do documento financeiro já foi atribuído");
        }
        if (qrPayload == null || qrPayload.isBlank() || qrPayloadVersion == null || qrPayloadVersion.isBlank()) {
            throw new IllegalArgumentException("Payload e versão do QR fiscal são obrigatórios");
        }
        this.qrPayload = qrPayload.trim();
        this.qrPayloadVersion = qrPayloadVersion.trim();
    }

    public boolean temQrFiscal() {
        return qrPayload != null && !qrPayload.isBlank();
    }
}
