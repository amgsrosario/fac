package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.AvaliacaoEmissaoDto;
import com.ar2lda.fac.controller.dto.AvaliacaoEmissaoDto.Motivo;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.model.*;
import com.ar2lda.fac.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

/** Read-only preflight. Never reserves numbers, signs, or mutates managed entities. */
@Service
@RequiredArgsConstructor
public class EmissionReadinessService {
    private final DocumentoComercialRepository documents;
    private final SerieRepository series;
    private final EmpresaRepository companies;
    private final MotorFiscalService fiscal;
    private final QrFiscalPayloadBuilder qr;
    private final ObjectProvider<FiscalDocumentSignatureSource> sources;
    private final Clock clock;
    @Value("${fac.fiscal.qr.enabled:true}") private boolean qrEnabled;

    private static boolean changed(BigDecimal previous, BigDecimal projected) {
        return previous == null || previous.compareTo(projected) != 0;
    }

    public AvaliacaoEmissaoDto evaluate(DocumentoComercial document, List<LinhaDocumentoComercial> lines) {
        var blockers = new ArrayList<Motivo>();
        var warnings = new ArrayList<Motivo>();
        if (document.getEstado() != EstadoDocumentoComercial.RASCUNHO)
            return new AvaliacaoEmissaoDto(clock.instant(),
                    List.of(new Motivo("ESTADO_INCOMPATIVEL", "O documento já foi emitido ou está num estado incompatível.")),
                    List.of(), false, document.getValorTotal(), null);
        if (document.isAnulado())
            blockers.add(new Motivo("DOCUMENTO_ANULADO", "O documento encontra-se anulado."));
        if (lines.stream().noneMatch(l -> l.getTipoLinha() == TipoLinhaDocumento.COMERCIAL))
            blockers.add(new Motivo("SEM_LINHAS_COMERCIAIS", "É necessária pelo menos uma linha comercial."));
        var last = documents.findUltimaDataEmissao(document.getTipoDocumento().getId(), document.getSerie());
        if (last != null && document.getDataEmissao().isBefore(last))
            blockers.add(new Motivo("DATA_EMISSAO_ANTERIOR_SERIE", "Data anterior ao último documento emitido da série."));
        if (!"EUR".equals(document.getMoeda().getId()))
            blockers.add(new Motivo("MOEDA_NAO_HABILITADA", "Emissão fiscal não EUR ainda não habilitada."));
        var serie = series.findById(new SerieId(document.getTipoDocumento().getId(), document.getSerie()));
        if (serie.isEmpty())
            blockers.add(new Motivo("SERIE_AUSENTE", "Série não encontrada."));
        else if (!serie.get().temCodigoAt())
            blockers.add(new Motivo("CODIGO_AT_AUSENTE", "A série não possui código de validação atribuído pela AT."));
        else if (serie.get().getCodigoAt().trim().regionMatches(true, 0, "ATCUD:", 0, 6))
            blockers.add(new Motivo("CODIGO_AT_COM_PREFIXO_INVALIDO", "O código AT não pode incluir o prefixo ATCUD:."));
        if (!qrEnabled) blockers.add(new Motivo("QR_DESACTIVADO", "A geração do QR fiscal está desactivada."));
        long sourceCount = sources.stream().limit(2).count();
        if (sourceCount == 0) blockers.add(new Motivo("FONTE_ASSINATURA_AUSENTE", "Fonte de assinatura fiscal indisponível."));
        if (sourceCount > 1) blockers.add(new Motivo("FONTES_ASSINATURA_AMBIGUAS", "Existem várias fontes de assinatura fiscal."));
        var company = companies.findById(Empresa.EMPRESA_ID);
        if (company.isEmpty()) blockers.add(new Motivo("EMPRESA_AUSENTE", "Empresa proprietária não encontrada."));
        else {
            try {
                qr.validateKnownCommercialInputs(company.get().getNif(), document.getCliente().getNif(), document.getCliente().getPais().getId(),
                        document.getTipoDocumento().getCodigoFiscal(), document.getTipoDocumento().getId());
            } catch (BadRequestException invalid) {
                blockers.add(new Motivo("DADOS_QR_INVALIDOS", invalid.getMessage()));
            }
        }
        BigDecimal total = BigDecimal.ZERO;
        boolean confirmation = document.isFiscalRecalculoAviso();
        boolean legacy = document.getFiscalMotorVersion() == null;
        boolean calculated = true;
        if (document.getValorRetencao() != null && document.getValorRetencao().signum() != 0)
            blockers.add(new Motivo("RETENCAO_NAO_HABILITADA", "Retenção documental exige distribuição ainda não habilitada."));
        for (var line : lines) {
            if (line.getTipoLinha() == TipoLinhaDocumento.TEXTO) continue;
            try {
                var result = fiscal.calcular(document.getRiva(), line.getArtigo(), line.getTipoTaxaIva(),
                        document.getDataEmissao(), line.getQuantidade(), line.getPrecoUnitario(),
                        line.getTipoDesconto(), line.getDesconto()).valores();
                total = total.add(result.totalLinha());
                if (legacy && (changed(line.getValorBruto(), result.valorBruto())
                        || changed(line.getValorDesconto(), result.valorDesconto())
                        || changed(line.getPercentagemIva(), result.taxaAplicavel()))) confirmation = true;
            } catch (BadRequestException invalid) {
                blockers.add(new Motivo("CALCULO_FISCAL_INVALIDO", invalid.getMessage()));
                calculated = false;
            }
        }
        if (calculated) {
            if (legacy && document.getValorTotal() != null && changed(document.getValorTotal(), total)) confirmation = true;
            if (!document.getTipoDocumento().isLiquidacaoImediata() && total.signum() <= 0)
                blockers.add(new Motivo("TOTAL_NAO_POSITIVO_PARA_PENDENTE", "Documento sem valor não gera pendente."));
            if (changed(document.getValorTotal(), total))
                warnings.add(new Motivo("TOTAL_PROJECTADO_DIFERENTE", "O total projectado difere do total apresentado. Reveja os valores."));
        }
        if (confirmation)
            warnings.add(new Motivo("RECALCULO_POR_CONFIRMAR", "Confirme os valores recalculados antes de emitir."));
        return new AvaliacaoEmissaoDto(clock.instant(), List.copyOf(blockers), List.copyOf(warnings),
                confirmation, document.getValorTotal(), calculated ? total : null);
    }
}
