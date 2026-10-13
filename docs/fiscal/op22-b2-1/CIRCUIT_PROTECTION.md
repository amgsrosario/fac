# OP22-B2.1 — Exclusão de liquidações incompatíveis

13-10-2026. Base integrada observada: 549e902bca8e53a9e23b47afbb2ab405fe7bcc8d.
OP22-B1 integrada pelo PR #23. Novo emissor RC permanece interno/inactivo.

## Invariante e estado efectivo

Uma dívida documental não pode ter simultaneamente linhas financeiras legadas
activas (documento não anulado, sem financial_receipt) e afectações CONSUME do
FinancialCore não estornadas. A simples origem FT não constitui consumo nem
atribuição exclusiva a um circuito. Valida-se o estado final da transacção.
Montantes/precisão e contratos DTO/API não mudam; sem sincronizar saldos.

FinancialCircuitGuard verifica ambas as direcções depois de bloquear Pendente.
Legado e emissor novo bloqueiam todas as identidades por ID ascendente antes da
numeração; novo bloqueia depois as posições por ID. A numeração e cronologia
continuam transaccionais; replay novo é verificado antes de bloqueios/numeração.
FinancialCore.settle usa a mesma ordem identidades→posições para dívidas vinculadas;
operações sem origem documental conservam o comportamento genérico.

## Protecção persistente V22

Migration aditiva, sem reescrever V18–V21 e sem DML de reconciliação.
Triggers imediatos serializam inserções/alterações de linhas, afectações, origem
e mudança efectiva de estado documental. Não classificam uma linha ainda incompleta.
Triggers diferidos validam linhas, afectações, origens, identidade e documento,
incluindo SQL directo e associação documental tardia. O vínculo novo é criado
por último: uma emissão legítima completa não é confundida com RC legado.
Forçar IMMEDIATE pode validar mais cedo; voltar a DEFERRED não elimina eventos
novos nem permite confirmar mistura de circuitos. O esquema V18/V20 já exige
montagem transaccional diferida do ledger/documento; não se promete emitir um RC
com todas as constraints imediatas desde a primeira instrução.

A serialização usa Pendente como identidade, sem ler o seu saldo para o núcleo.
O guard SQL faz UPDATE id=id (valores inalterados): cria versão MVCC, para que
um escritor REPEATABLE READ com snapshot obsoleto falhe por serialização em vez
de aceitar uma vista antiga. Uma origem documental nova e o consumo de posição
já vinculada exigem identidade
comum existente; ausência não equivale a exclusividade do núcleo. A originação FT
actual já cria Pendente antes da origem. Inserir/reassociar identidade bloqueia
a posição mapeada, evitando uma vista obsoleta após recriação. Não cria identidades
nem reconcilia vínculos ausentes.
Não altera montantes; acrescenta custo de escrita
por identidade. SQL arbitrário que bloqueie várias dívidas em ordem inversa pode
sofrer deadlock e rollback; os serviços usam ordem determinística.

## Limites preservados

Sem activação pública, migração de consumidores ou dados, nova anulação/REVERSE,
semântica temporal, precisão nova do legado ou mudança à governação central.
Observa-se o estado documental existente para impedir contornar a exclusão;
não se implementa um novo acto de anulação. Guards V20 de anulação de RC novo
permanecem. Origem FT/RC legado não é prova de reconciliação.

V22 não varre nem repara incompatibilidades já persistidas; valida escritas
abrangidas. Dataset de demonstração continua a exigir inventário e autorização
próprios antes de corte. Fonte operacional Pendente e consumidores mantidos.

## Revisão independente Claude

Rever diff completo desde a base acima, com foco em: classificação diferida de
RC; ambas as direcções; concorrência/multi-FT/ordem de locks; MVCC em RR; vínculos
tardios; SQL directo; rollback/numeração; idempotência; V22 aditiva; preservação
de precisão/anulação/consumidores/inactividade. Classificar BLOCKER/MAJOR/MINOR.
Revisão só leitura, sem bases persistentes, commit/push/PR ou alterações AR2.
Ler AGENTS.md, fontes canónicas TUULI, ADR-001/002 e
AI_DEVELOPMENT_SYSTEM.md central exclusivamente em leitura. Rever também os
pressupostos de identidade obrigatória e triggers de inserção/reassociação.
A revisão Claude B1 não abrange B2.1. Revisão B2.1 ainda não executada.

[Evidência de validação](VALIDATION.md).
