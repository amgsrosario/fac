package com.ar2lda.fac.model;

import java.util.EnumSet;
import java.util.Set;

public enum PapelUtilizador {
    ADMINISTRADOR(EnumSet.allOf(PermissaoFuncional.class)),
    OPERADOR(EnumSet.of(
            PermissaoFuncional.DOCUMENTO_CONSULTAR,
            PermissaoFuncional.DOCUMENTO_CRIAR,
            PermissaoFuncional.DOCUMENTO_EDITAR_RASCUNHO,
            PermissaoFuncional.DOCUMENTO_ELIMINAR_RASCUNHO,
            PermissaoFuncional.DOCUMENTO_EMITIR,
            PermissaoFuncional.DOCUMENTO_OBTER_PDF,
            PermissaoFuncional.SERIE_CONSULTAR,
            PermissaoFuncional.MESTRES_GERIR,
            PermissaoFuncional.TESOURARIA_GERIR
    )),
    CONSULTA(EnumSet.of(
            PermissaoFuncional.DOCUMENTO_CONSULTAR,
            PermissaoFuncional.DOCUMENTO_OBTER_PDF,
            PermissaoFuncional.SERIE_CONSULTAR
    ));

    private final Set<PermissaoFuncional> permissoes;

    PapelUtilizador(Set<PermissaoFuncional> permissoes) {
        this.permissoes = Set.copyOf(permissoes);
    }

    public Set<PermissaoFuncional> permissoes() {
        return permissoes;
    }
}
