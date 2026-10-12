# ADR-002 — TUULI-FIN-TIME-001: efeito financeiro documental

12-10-2026 · **DECISÃO APROVADA PELO PRODUCT OWNER** na continuação OP22-A.
Complementa [ADR-001](ADR-001-financial-source.md) e GOV-ALT-001.

## Decisão e alternativas

O PO definiu: **a data de efeito financeiro de uma operação originada por um
documento é obrigatoriamente a data de emissão desse documento**. FT origina
dívida na emissão FT; RC liquida na emissão RC; NC origina crédito na emissão NC;
outros documentos seguem o princípio sob as respetivas regras legais/funcionais.
Não existe data financeira autónoma editável que possa divergir da documental.

Foram distinguidas consulta por timestamp de registo e consulta por data financeira.
O timestamp permite rastreabilidade técnica com o esquema atual, mas não exprime
o efeito financeiro de documentos emitidos em data diferente do registo. O PO
escolheu o efeito documental para saldos financeiros. Auditoria por registo é
funcionalidade distinta; a decisão não elimina nem altera o timestamp técnico.

## Limites

Timestamp de registo permanece independente, imutável e auditável, sem equivalência
com SystemEntryDate documental do SAF-T (PT) ou garantia de instante de commit.
A decisão não aprova retrodatação arbitrária e não dispensa regras legais de
emissão. Estornos/anulações exigem identificar semântica legal e funcional própria:
nem o registo técnico do estorno nem a emissão original definem automaticamente
a sua data de efeito. Essas decisões permanecem pendentes.

## Estado efetivo OP22-A

V19 vincula posição FT a documento_comercial com emissão/vencimento existentes.
Consulta corrente deriva financialEffectDate dessa emissão sem coluna adicional.
SETTLE não possui vínculo RC e REVERSE não possui semântica documental temporal
aprovada. Saldo/movimentos a uma data financeira ficam suspensos. Nenhuma nova
estrutura ou data autónoma foi implementada; alterações estruturais requerem
comparação e aprovação prévia GOV-ALT-001. Não activar RC nem alterar anulações.
[Contratos e limitações](../../fiscal/op22-a/QUERY_CONTRACTS.md).
