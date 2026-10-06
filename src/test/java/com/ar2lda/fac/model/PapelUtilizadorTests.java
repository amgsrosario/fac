package com.ar2lda.fac.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PapelUtilizadorTests {
    @Test
    void administradorRecebeTodasAsCapacidadesIncluindoFuturas() {
        assertThat(PapelUtilizador.ADMINISTRADOR.permissoes()).containsExactlyInAnyOrderElementsOf(
                java.util.EnumSet.allOf(PermissaoFuncional.class));
    }

    @Test
    void operadorMantemMapaExato() {
        assertThat(PapelUtilizador.OPERADOR.permissoes()).containsExactlyInAnyOrder(
                PermissaoFuncional.DOCUMENTO_CONSULTAR, PermissaoFuncional.DOCUMENTO_CRIAR,
                PermissaoFuncional.DOCUMENTO_EDITAR_RASCUNHO, PermissaoFuncional.DOCUMENTO_ELIMINAR_RASCUNHO,
                PermissaoFuncional.DOCUMENTO_EMITIR, PermissaoFuncional.DOCUMENTO_OBTER_PDF,
                PermissaoFuncional.SERIE_CONSULTAR, PermissaoFuncional.MESTRES_GERIR,
                PermissaoFuncional.DADOS_MESTRES_EXPORTAR, PermissaoFuncional.TESOURARIA_GERIR);
    }

    @Test
    void consultaMantemMapaExato() {
        assertThat(PapelUtilizador.CONSULTA.permissoes()).containsExactlyInAnyOrder(
                PermissaoFuncional.DOCUMENTO_CONSULTAR, PermissaoFuncional.DOCUMENTO_OBTER_PDF,
                PermissaoFuncional.SERIE_CONSULTAR, PermissaoFuncional.DADOS_MESTRES_EXPORTAR);
    }

    @Test
    void contratoFrontendContemExatamenteOCatalogoBackend() throws Exception {
        String frontend = java.nio.file.Files.readString(java.nio.file.Path.of("frontend/src/permissions.ts"));
        var matcher = java.util.regex.Pattern.compile("\"([A-Z_]+)\"").matcher(frontend);
        var names = new java.util.HashSet<String>();
        while (matcher.find()) names.add(matcher.group(1));
        assertThat(names).containsExactlyInAnyOrderElementsOf(
                java.util.Arrays.stream(PermissaoFuncional.values()).map(Enum::name).toList());
    }
}
