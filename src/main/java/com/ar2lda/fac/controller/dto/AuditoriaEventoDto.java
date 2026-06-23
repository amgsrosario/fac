package com.ar2lda.fac.controller.dto;

import com.ar2lda.fac.model.ResultadoAuditoria;
import com.ar2lda.fac.model.TipoAuditoriaEvento;
import java.time.OffsetDateTime;

public record AuditoriaEventoDto(Long id, OffsetDateTime dataHora, TipoAuditoriaEvento tipoEvento,
        String entidadeTipo, String entidadeId, String utilizadorId, String utilizadorNome,
        ResultadoAuditoria resultado, String descricao, String dadosEssenciais) {
}
