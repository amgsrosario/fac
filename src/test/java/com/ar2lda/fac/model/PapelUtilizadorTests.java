package com.ar2lda.fac.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PapelUtilizadorTests {
    @Test
    void matrizMinimaDoMd14EAplicada() {
        assertThat(PapelUtilizador.ADMINISTRADOR.permissoes()).contains(PermissaoFuncional.DOCUMENTO_ANULAR, PermissaoFuncional.AUDITORIA_CONSULTAR);
        assertThat(PapelUtilizador.OPERADOR.permissoes()).contains(PermissaoFuncional.DOCUMENTO_CRIAR, PermissaoFuncional.DOCUMENTO_EMITIR, PermissaoFuncional.DOCUMENTO_OBTER_PDF)
                .doesNotContain(PermissaoFuncional.DOCUMENTO_ANULAR, PermissaoFuncional.AUDITORIA_CONSULTAR);
        assertThat(PapelUtilizador.CONSULTA.permissoes()).contains(PermissaoFuncional.DOCUMENTO_CONSULTAR, PermissaoFuncional.DOCUMENTO_OBTER_PDF)
                .doesNotContain(PermissaoFuncional.DOCUMENTO_CRIAR, PermissaoFuncional.DOCUMENTO_EMITIR, PermissaoFuncional.DOCUMENTO_ANULAR);
    }
}
