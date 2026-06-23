# Bloco 3 — anulação, permissões e auditoria

O estado `ANULADO` é a fonte oficial. A coluna histórica `anulado` é mantida temporariamente por compatibilidade e protegida por uma restrição que impede divergências. Uma anulação preserva número, série, ATCUD, payload QR e snapshot fiscal; guarda motivo, instante, código e nome do utilizador.

## Papéis

- `ADMIN`: todas as permissões.
- `OPERADOR`: consulta, criação/edição/eliminação de rascunhos, emissão, anulação, PDF e consulta de séries.
- `CONSULTA`: consulta documental, PDF e consulta de séries.

As permissões são derivadas do papel no login e incluídas no JWT. Alterações de papel exigem novo login/renovação do token. A migração atribui `ADMIN` aos utilizadores existentes para não quebrar o ambiente atual; esta atribuição deve ser revista antes de produção.

## Auditoria

`auditoria_evento` é append-only pela API: não existem endpoints de alteração ou eliminação. Eventos de sucesso funcional são gravados na mesma transação da operação. `dados_essenciais` usa JSON controlado e versionado, sem documento integral, passwords ou tokens.

## Payload QR na anulação

**VALIDAÇÃO OFICIAL EXTERNA NECESSÁRIA.** Até existir validação fiscal oficial, a anulação não reconstrói nem altera o payload QR emitido. O PDF reutiliza o payload persistido e acrescenta separadamente a marca e os dados da anulação.
