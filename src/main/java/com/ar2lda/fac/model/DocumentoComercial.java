package com.ar2lda.fac.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "documento_comercial")
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class DocumentoComercial {

    public static final int CURRENT_FISCAL_SNAPSHOT_VERSION = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_documento", nullable = false)
    @Setter
    private TipoDocumento tipoDocumento;

    @Column(length = 10, nullable = false)
    @Setter
    @ToString.Include
    private String serie;

    @Column(name = "numero_documento")
    @Setter
    @ToString.Include
    private Long numeroDocumento;

    @Column(name = "numero_documento_completo", length = 80)
    private String numeroDocumentoCompleto;

    @Column(name = "codigo_validacao_at", length = 100)
    private String codigoValidacaoAt;

    @Column(name = "atcud", length = 150)
    private String atcud;

    @Column(name = "qr_payload", columnDefinition = "TEXT")
    private String qrPayload;

    @Column(name = "qr_payload_version", length = 20)
    private String qrPayloadVersion;

    @Column(name = "fiscal_snapshot_version")
    private Integer fiscalSnapshotVersion;

    @Column(name = "emitente_nome", length = 100)
    private String emitenteNome;

    @Column(name = "emitente_nif", length = 20)
    private String emitenteNif;

    @Column(name = "emitente_morada", length = 60)
    private String emitenteMorada;

    @Column(name = "emitente_morada1", length = 60)
    private String emitenteMorada1;

    @Column(name = "emitente_cod_postal", length = 20)
    private String emitenteCodPostal;

    @Column(name = "emitente_localidade", length = 50)
    private String emitenteLocalidade;

    @Column(name = "emitente_pais", length = 3)
    private String emitentePais;

    @Column(name = "emitente_email", length = 120)
    private String emitenteEmail;

    @Column(name = "emitente_web", length = 120)
    private String emitenteWeb;

    @Column(name = "emitente_capital_social", precision = 19, scale = 2)
    private BigDecimal emitenteCapitalSocial;

    @Column(name = "emitente_matricula_registo", length = 100)
    private String emitenteMatriculaRegisto;

    @Column(name = "emitente_cae", length = 10)
    private String emitenteCae;

    @Column(name = "emitente_descricao_cae", length = 100)
    private String emitenteDescricaoCae;

    @Column(name = "tipo_documento_codigo", length = 3)
    private String tipoDocumentoCodigo;

    @Column(name = "tipo_documento_codigo_fiscal", length = 2)
    private String tipoDocumentoCodigoFiscal;

    @Column(name = "tipo_documento_descricao", length = 50)
    private String tipoDocumentoDescricao;

    @Column(name = "serie_descricao", length = 50)
    private String serieDescricao;

    @Column(name = "moeda_codigo", length = 3)
    private String moedaCodigo;

    @Column(name = "moeda_simbolo", length = 5)
    private String moedaSimbolo;

    @Column(name = "moeda_casas_decimais")
    private Integer moedaCasasDecimais;

    @Column(name = "taxa_cambio", precision = 20, scale = 10)
    private BigDecimal taxaCambio;

    @Column(name = "regime_iva_codigo", length = 3)
    private String regimeIvaCodigo;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private EstadoDocumentoComercial estado = EstadoDocumentoComercial.RASCUNHO;

    @Column(nullable = false)
    @Setter
    private LocalDate dataEmissao;

    @Column(nullable = false)
    @Setter
    private LocalDate dataVencimento;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    @Setter
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_morada_envio")
    @Setter
    private Morada moradaEnvio;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_armazem_carga", nullable = false)
    @Setter
    private Armazem armazemCarga;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_moeda", nullable = false)
    @Setter
    private Moeda moeda;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_riva", nullable = false)
    @Setter
    private RIva riva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mpagamento")
    @Setter
    private MPagamento mPagamento;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ppagamento", nullable = false)
    @Setter
    private PPagamento pPagamento;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_transporte", nullable = false)
    @Setter
    private Transporte transporte;

    @Column(length = 100, nullable = false)
    @Setter
    private String clienteNome;

    @Column(length = 20, nullable = false)
    @Setter
    private String clienteNif;

    @Column(length = 60, nullable = false)
    @Setter
    private String clienteMorada;

    @Column(length = 60)
    @Setter
    private String clienteMorada1;

    @Column(length = 20, nullable = false)
    @Setter
    private String clienteCodPostal;

    @Column(length = 50)
    @Setter
    private String clienteLocalidade;

    @Column(length = 3, nullable = false)
    @Setter
    private String clientePais;

    @Column(length = 80)
    @Setter
    private String envioNome;

    @Column(length = 60)
    @Setter
    private String envioMorada;

    @Column(length = 60)
    @Setter
    private String envioMorada1;

    @Column(length = 20)
    @Setter
    private String envioCodPostal;

    @Column(length = 50)
    @Setter
    private String envioLocalidade;

    @Column(length = 3)
    @Setter
    private String envioPais;

    @Column(nullable = false)
    @Setter
    private LocalDate dataCarga;

    @Setter
    private LocalTime horaCarga;

    @Column(length = 100)
    @Setter
    private String matricula;

    @Column(length = 100, nullable = false)
    @Setter
    private String cargaNome;

    @Column(length = 60, nullable = false)
    @Setter
    private String cargaMorada;

    @Column(length = 60)
    @Setter
    private String cargaMorada1;

    @Column(length = 20, nullable = false)
    @Setter
    private String cargaCodPostal;

    @Column(length = 50, nullable = false)
    @Setter
    private String cargaLocalidade;

    @Column(length = 3, nullable = false)
    @Setter
    private String cargaPais;

    @Setter
    private LocalDate dataDescarga;

    @Setter
    private LocalTime horaDescarga;

    @Column(length = 60)
    @Setter
    private String descargaMorada;

    @Column(length = 60)
    @Setter
    private String descargaMorada1;

    @Column(length = 20)
    @Setter
    private String descargaCodPostal;

    @Column(length = 50)
    @Setter
    private String descargaLocalidade;

    @Column(length = 3)
    @Setter
    private String descargaPais;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorBruto = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorDesconto = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorIsento = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorSujeitoReduzida = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorSujeitoIntermedia = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorSujeitoNormal = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorIvaReduzida = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorIvaIntermedia = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorIvaNormal = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorRetencao = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorIvaTotal = BigDecimal.ZERO;

    @Column(precision = 19, scale = 6, nullable = false)
    @Setter
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(precision = 15, scale = 3)
    @Setter
    private BigDecimal peso;

    @Column(length = 250)
    @Setter
    private String observacoes;

    @Setter
    private OffsetDateTime momentoEmissao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_emissor")
    @Setter
    private Utilizador emissor;

    @Column(nullable = false)
    private boolean anulado = false;

    @Column(name = "motivo_anulacao", length = 500)
    private String motivoAnulacao;

    @Column(name = "data_hora_anulacao")
    private OffsetDateTime dataHoraAnulacao;

    @Column(name = "anulado_por_utilizador_id", length = 20)
    private String anuladoPorUtilizadorId;

    @Column(name = "anulado_por_nome", length = 100)
    private String anuladoPorNome;

    @Column(nullable = false)
    @Setter
    private boolean impresso = false;

    @Column(nullable = false)
    @Setter
    private boolean liquidado = false;

    public void setEstado(EstadoDocumentoComercial estado) {
        this.estado = java.util.Objects.requireNonNull(estado);
        this.anulado = estado == EstadoDocumentoComercial.ANULADO;
    }

    /** Compatibilidade temporaria; o estado continua a ser a fonte oficial. */
    public void setAnulado(boolean anulado) {
        if (anulado) {
            this.estado = EstadoDocumentoComercial.ANULADO;
        } else if (this.estado == EstadoDocumentoComercial.ANULADO) {
            throw new IllegalStateException("Um documento anulado nao pode regressar a emitido");
        }
        this.anulado = anulado;
    }

    public boolean isAnulado() {
        return estado == EstadoDocumentoComercial.ANULADO;
    }

    public void anular(String motivo, Utilizador utilizador, OffsetDateTime momento) {
        if (estado != EstadoDocumentoComercial.EMITIDO) {
            throw new IllegalStateException("Apenas documentos emitidos podem ser anulados");
        }
        this.estado = EstadoDocumentoComercial.ANULADO;
        this.anulado = true;
        this.motivoAnulacao = motivo;
        this.dataHoraAnulacao = momento;
        this.anuladoPorUtilizadorId = utilizador.getCodigo();
        this.anuladoPorNome = utilizador.getNome();
    }

    public void atribuirAtcud(String codigoValidacaoAt, String atcud) {
        if (this.atcud != null || this.codigoValidacaoAt != null) {
            throw new IllegalStateException("O ATCUD do documento comercial já foi atribuído");
        }
        if (codigoValidacaoAt == null || codigoValidacaoAt.isBlank() || atcud == null || atcud.isBlank()) {
            throw new IllegalArgumentException("Código de validação da AT e ATCUD são obrigatórios");
        }
        this.codigoValidacaoAt = codigoValidacaoAt.trim();
        this.atcud = atcud.trim();
    }

    public void atribuirNumeroDocumentoCompleto(String numeroDocumentoCompleto) {
        if (this.numeroDocumentoCompleto != null) {
            throw new IllegalStateException("O número completo do documento comercial já foi atribuído");
        }
        if (numeroDocumentoCompleto == null || numeroDocumentoCompleto.isBlank()) {
            throw new IllegalArgumentException("O número completo do documento comercial é obrigatório");
        }
        this.numeroDocumentoCompleto = numeroDocumentoCompleto.trim();
    }

    public void atribuirQrFiscal(String qrPayload, String qrPayloadVersion) {
        if (this.qrPayload != null || this.qrPayloadVersion != null) {
            throw new IllegalStateException("O payload QR do documento comercial já foi atribuído");
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

    public void consolidarSnapshotFiscal(Empresa empresa, Serie serieUtilizada) {
        if (fiscalSnapshotVersion != null) {
            throw new IllegalStateException("O snapshot fiscal do documento comercial já foi consolidado");
        }
        if (empresa == null || serieUtilizada == null || moeda == null || tipoDocumento == null || riva == null) {
            throw new IllegalArgumentException("Dados mestres obrigatórios para o snapshot fiscal estão em falta");
        }
        emitenteNome = empresa.getNome();
        emitenteNif = empresa.getNif();
        emitenteMorada = empresa.getMorada();
        emitenteMorada1 = empresa.getMorada1();
        emitenteCodPostal = empresa.getCodPostal().getId();
        emitenteLocalidade = empresa.getLocalidade();
        emitentePais = empresa.getPais().getId();
        emitenteEmail = empresa.getEmail();
        emitenteWeb = empresa.getWeb();
        emitenteCapitalSocial = empresa.getCapitalSocial();
        emitenteMatriculaRegisto = empresa.getMatriculaRegistoComercial();
        emitenteCae = empresa.getCae();
        emitenteDescricaoCae = empresa.getDescricaoCae();
        tipoDocumentoCodigo = tipoDocumento.getId();
        tipoDocumentoCodigoFiscal = tipoDocumento.getCodigoFiscal();
        tipoDocumentoDescricao = tipoDocumento.getDescricao();
        serieDescricao = serieUtilizada.getNome();
        moedaCodigo = moeda.getId();
        moedaSimbolo = moeda.getSimbolo();
        moedaCasasDecimais = moeda.getNdecimais();
        // O FAC ainda não aplica uma taxa de câmbio no cálculo documental.
        // A coluna fica preparada, mas não se persiste como aplicada uma taxa do mestre.
        taxaCambio = null;
        regimeIvaCodigo = riva.getId();
        fiscalSnapshotVersion = CURRENT_FISCAL_SNAPSHOT_VERSION;
    }

    public boolean isFiscalmenteConsolidado() {
        return (estado == EstadoDocumentoComercial.EMITIDO || estado == EstadoDocumentoComercial.ANULADO)
                && Integer.valueOf(CURRENT_FISCAL_SNAPSHOT_VERSION).equals(fiscalSnapshotVersion)
                && numeroDocumento != null
                && numeroDocumentoCompleto != null
                && !numeroDocumentoCompleto.isBlank()
                && atcud != null
                && !atcud.isBlank()
                && temQrFiscal()
                && hasText(qrPayloadVersion)
                && hasText(codigoValidacaoAt)
                && momentoEmissao != null
                && hasText(emitenteNome)
                && hasText(emitenteNif)
                && hasText(emitenteMorada)
                && hasText(emitenteCodPostal)
                && hasText(emitentePais)
                && hasText(clienteNome)
                && hasText(clienteNif)
                && hasText(clienteMorada)
                && hasText(clienteCodPostal)
                && hasText(clientePais)
                && hasText(tipoDocumentoCodigo)
                && hasText(serie)
                && hasText(serieDescricao)
                && hasText(moedaCodigo)
                && hasText(moedaSimbolo)
                && moedaCasasDecimais != null
                && hasText(regimeIvaCodigo);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
