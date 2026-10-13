# OP22-B1 — Infraestrutura FT/RC inactiva

12-10-2026 · Base observada 70e82c122ec477ceb804e6c837f5bb529120acd7, PR #22 merged.
Governação AR2 v1 consultada no commitace6e54a613ce25c959f31accae3ec7b129b307e:
README, STRATEGY, AI_DEVELOPMENT_SYSTEM, PROJECTS, DECISION_LOG, CURRENT_STATE e
SUCCESSION_PROTOCOL. Estados centrais são fotografias históricas, não inventário
actual TUULI. Repositório central e missão GOV-TUULI-002 não alterados.

## Decisões aprovadas e fronteira

PO aprovou API existente como projecção, EUR/cêntimos exactos, sem descontos na
primeira activação e preparação de corte limpo sem apagar/migrar dados. B1 prepara
componentes isolados; **nenhuma activação operacional**. Não existe feature flag,
endpoint novo ou chamada do circuito legado ao emissor novo. Emissão/anulação RC,
Pendente e leitores operacionais existentes mantêm o comportamento anterior.

FinancialReceiptIssuer.issue(requestKey, dto) emite um RC interno numa transacção
Spring/JPA/JDBC. FinancialReceiptProjections oferece leitura compatível de dívidas
em PendenteDto e totais do núcleo; não foi injectado nos serviços operacionais.
Migração V20 é aditiva: duas tabelas de identidade/vínculos e guards de integridade,
sem backfill, conversão de valores ou alterações às migrations V18/V19.
V21 sela componentes/afectações depois do vínculo RC, sem alterar a V20.

## Pedido, documento e ledger

RequestKey persistente é fornecido explicitamente pelo futuro consumidor. Não é
gerado de novo a cada retry nem substituído por ID documental após a emissão.
V20 financial_receipt conserva chave/payload, documento, operação e contexto;
unicidade empresa/chave, documento e operação. advisory lock serializa chaves
ainda não existentes. Verificação de replay acontece ANTES da reserva de número.
Payload normalizado conserva contexto/autor efectivo, documento, datas, pagamento,
observações e ordem de linhas. Valores equivalentes10/10.000000 normalizam a10.00;
reutilização com conteúdo/contexto diferente é rejeitada. Mudança da ordem das
linhas altera o conteúdo documental; não se oculta como replay equivalente.

Utilizador é resolvido por CurrentUserService: identidade autenticada prevalece
sobre fallback de testes existente. Scope do núcleo é empresa1, cliente da emissão
validado, EUR; IDs recebidos não são credenciais. Future endpoint tem de conservar
TESOURARIA_GERIR, validação de payload e autorização dos leitores existentes.
B1 não cria permissão nem API pública e não altera segurança/autenticação.

Após lock de série, revalida última emissão dessa série, antes de gravar RC.
Identidades Pendente são bloqueadas por ID ascendente; posições por ID ascendente.
Saldo Pendente não é lido nem actualizado. Valida documento/cliente/moeda/vínculo,
residual e ausência de RC legado activo sem vínculo. Nenhum fallback ou importação.
Pedido repetido devolve o mesmo RC/numeração; `RC_DOCUMENT:<id>` protege SETTLE.
Unicidade SQL não substitui a identidade de pedido anterior à numeração.

Liquidação parcial/múltipla usa core.settle e montantes positivos exactos. Linhas
conservam snapshots anteriores/posteriores retirados do núcleo, ordem original
do pedido e vínculo exclusivo a financial_allocation. Total = dívida extinta =
CASH_IN nesta fronteira; sem CASH_OUT/DISCOUNT. Empresa/cliente/moeda/documento,
montantes, linha/afectação e bijecção são verificados também por guards SQL.
V21 impede qualquer nova componente/afectação de operação já vinculada a RC,
por triggers BEFORE INSERT não diferíveis, incluindo a transacção criadora.
O emissor monta o ledger antes de criar o vínculo; V20 valida esse estado inicial.
Consumir constraints com IMMEDIATE e regressar a DEFERRED não reabre a operação.
Operações sem RC conservam os guards e comportamento genérico V18.
Documento, número/ATCUD, linhas, ledger, vínculos e auditoria confirmam/revertem juntos.
Retries de pedidos que falharam não reutilizam documentos ou números parcialmente
persistidos. Os sequences técnicos podem ter lacunas por rollback, sem impor
contiguidade aos IDs. Numeração documental transaccional é preservada.

Precisão: setScale(2, UNNECESSARY) antes de qualquer consumo;1.001 falha, não arredonda.
Desconto absoluto/percentual não zero falha no novo emissor. RC legado de seis casas
mantém descontos e comportamento antigo enquanto inactivo. O total tem de caber
na precisão documental19,6 existente. Não se alterou política fiscal RIVA.
ATCUD/QR/auditoria reutilizam serviços existentes. O QR financeiro conserva o
comportamento existente de retorno opcional; esta OP não prova certificação.

## Efeito financeiro e anulação

TUULI-FIN-TIME-001: financialEffectDate de SETTLE vinculado deriva de
DocumentoFinanceiro.dataEmissao, nunca de recordedAt/dataHoraOperacao. Timestamp
tecnico continua imutável e separado de SystemEntryDate SAF-T. V20 congela a
identidade/montantes/datas do RC vinculado. O vínculo não armazena segunda data.
CREATE FT conserva efeito da emissão FT. REVERSE não recebe data presumida.

Anulação nova e consultas históricas financeiras continuam **SUSPENSAS**. B1 não
repõe snapshots anteriores. Guards impedem anulação pelo serviço legado ou reverse
isolado de SETTLE vinculado: exigem futura definição documental/temporal B2.
RC legados sem vínculo continuam anuláveis pelo circuito existente. Marcação de
impressão continua permitida em RC interno vinculado. Não apagar originais/links.
B2 terá de definir acto/documento da anulação, data de efeito, dependências reais
e atomicidade do estorno. Regra adicional sobre RC anterior à FT não foi inventada;
cronologia implementada limita-se à regra de série existente revalidada sob lock.

## Projecções preparadas versus consumidores adiados

Projecções isoladas: selecção de dívidas/conta corrente por cliente em PendenteDto,
totais e estados por núcleo, paginação determinística com count no mesmo SQL.
Identidade/metadados documentais vêm de Pendente/vínculo FT; valores exclusivamente
de financial_position. Não há sincronização persistida do saldo Pendente.

**Ainda não transferidos:** endpoints Pendente/conta corrente, clientes com saldo,
dashboard/indicadores, diagnósticos operacionais, listagens/exportações correntes,
extrato por documentos e leituras históricas. Não se declara igualdade entre saldo
operacional legado e saldo novo após testes internos. Igualdade testada abrange
somente projecção isolada de dívidas e totais do núcleo no contexto FT preparado.
Completar a fronteira e as decisões de anulação em B2 antes de ligar qualquer
componente. Uma tabela Pendente com saldo obsoleto NÃO pode ficar com consumidores
activos desse campo após o futuro cutover.

## Condição impeditiva de activação OP22-B2

O RC legado pode liquidar uma FT já parcialmente liquidada pelo emissor novo,
porque consulta o saldo armazenado em Pendente. A OP22-B2 deve impedir esta
coexistência de escritas antes de activar o novo circuito. Não basta validar
os contadores de inspectCutover(); esta condição exige controlo da fronteira
operacional. Nenhuma alteração funcional ao legado é introduzida neste fecho.

## Preparação do corte da demo

inspectCutover() conta origens FT EUR ausentes, RC activos sem vínculo e posições
sem vínculo documental/identidade de compatibilidade. dataConsistent() indica
apenas esses três diagnósticos; não certifica fiscalidade nem prontidão operacional.
operationalActivationPermitted() é sempre false em B1, mesmo com contadores zero.
Emissão interna recusa cada FT sem origem ou já afectada por RC legado activo.
Uma FT com posição e residual diferente do legado não é declarada reconciliada.

Sequência futura, dependente de autorização: inventariar dataset/circuitos excluídos;
aprovar regras temporais e corte; validar todos os consumidores em cópia descartável;
planear eventual nova demo com autorização própria e backup; comprovar origens,
recebimentos e saldos coerentes; só então autorizar activação. B1 não apaga,
reconstrói ou migra dados, nem fornece um comando destrutivo de corte.

Riscos restantes: mistura de dados legados, histórico/anulação, consumidores por
transferir, retries estáveis no frontend futuro, autorização do endpoint futuro,
QR/snapshots financeiros e gates AT existentes. Sem NC/ND/NL, adiantamentos,
multimoeda, descontos novos ou conformidade/certificação presumida.

[Evidência](VALIDATION.md). Decisões institucionais constam da memória, estado e ADRs.
