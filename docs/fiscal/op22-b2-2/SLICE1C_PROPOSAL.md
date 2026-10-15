# Fatia 1c — proposta para decisão do Product Owner

15-10-2026 · **Proposta histórica integrada pelo PR #29; opção 1c-B posteriormente
aprovada pelo PO na OP Fatia 1c.** Comparação/critério originais conservados abaixo;
1c-A/1c-C não autorizadas. Estado da implementação local e testes em
[SLICE1C_VALIDATION.md](SLICE1C_VALIDATION.md). A proposta, por si só, não autorizava
implementação; a ordem posterior é a fonte da autorização.
Base observada: `2a0f8b08f9d79715dd4cd93a5888b9e6e79773eb`, PR #28,
com PR #27 e V23/V24 integrados. Não autoriza activação, deployment ou dados.

## 1. Factos observados e problema restante

- FinancialFtOrigin origina FT EUR e reverseLinkedOrigin já reverte CREATE na
  anulação comercial. DocumentoComercialService.anular também zera Pendente e
  conserva motivo/data_hora_anulacao/actor. Isto não define a data financeira do
  estorno FT: balanceAt recusa o âmbito com CREATE revertido. T1 aprovado para
  RC não se estende automaticamente à FT.
- FinancialReceiptIssuer/Reversal são internos e não chamados pelos controllers,
  serviços operacionais ou demo. FinancialReceiptProjections oferece debts/totals
  isolados e inspectCutover com três contadores; operationalActivationPermitted
  devolve sempre false. Não existe comparação detalhada de saldos por FT.
- DocumentoFinanceiroService legado actualiza Pendente.valorPendente, snapshots
  e liquidado; não escreve SETTLE no núcleo. Exemplo factual de arquitectura:
  FT 100 com RC legado 30 pode apresentar Pendente 70 e FinancialCore 100.
  Uma divergência assim não é, por si só, corrupção: pode ser esperada na transição.
- Consumidores: selecção/formulário RC, conta corrente, ClienteRepository/
  ClienteService, DashboardComercialService, ListagensService, extractos e
  exportadores, estado liquidado/podeAnular e DemoScenarioService permanecem
  legados. O inventário anterior em FINANCIAL_HISTORY.md é histórico; a Fatia 1a
  já acrescentou Data Valor ao formulário/detalhe/PDF e a 1b implementou F2/T1
  interno. Nenhuma dessas integrações transferiu a autoridade operacional.
- RC históricos NULL permanecem NULL: emissão é compatibilidade explícita de
  leitura, não data real conhecida. Reversão T1 desses RC continua recusada;
  balanceAt recusa também efeitos genéricos sem data e CREATE revertido.
- V22 impede consumo incompatível entre circuitos; não reconcilia automaticamente
  históricos nem demonstra equivalência dos saldos. V24 não habilita corte.

Fontes de código: FinancialReceiptProjections.inspectCutover, JdbcFinancialQueries.
balanceAt, FinancialFtOrigin.reverseLinkedOrigin, DocumentoComercialService.anular,
DocumentoFinanceiroService.create/anular e migrations V22–V24, no HEAD acima.
Nenhuma base de negócio foi consultada: não se afirma que existam estes casos
na demo nem se quantificam incompatibilidades reais.

## 2. Alternativas para a próxima fatia — GOV-ALT-001

| Alternativa | Benefício | Custos e limitações | Riscos/dependências | Reversibilidade |
| --- | --- | --- | --- | --- |
| 1c-A — completar primeiro a temporalidade da anulação FT | Alarga história rectificada a FT anuladas | Complexidade média; contrato temporal e testes de períodos/concorrência | Exige decisão PO e análise fiscal/documental; não resolve saldos divergentes legados | Código pode ficar inactivo, mas semântica histórica duradoura exige decisão prévia |
| 1c-B — diagnóstico interno detalhado de reconciliação FT/RC, só leitura | Evidência por dívida para preparar decisões e corte; evolução útil do inspectCutover | Complexidade baixa/média, queries paginadas e classificação explícita; não reconcilia dados nem completa história FT | Evitar classificar diferença esperada como erro ou aprovar corte por igualdade aparente | Alta: sem escritas nem troca de consumidores |
| 1c-C — preparar já um consumidor do núcleo atrás de fronteira inactiva | Reduz trabalho futuro de adaptação dos DTOs | Complexidade média/alta e superfície maior de regressão; restantes leitores continuam legados | Exige contratos equivalentes, origem de dados coerente e corte conjunto; risco de ligação acidental/dupla autoridade | Razoável enquanto desligado; reduzida depois de aceitar novas escritas |

**Recomendação técnica: 1c-B**, sujeita à aprovação expressa do PO. Produz informação
útil sem precisar de escolher a data do estorno FT, inventar Data Valor histórica
ou ligar consumidores. 1c-A continua necessária antes de ampliar consultas históricas;
1c-C fica para uma missão seguinte, com dados e contratos conhecidos. Esta ordem
não impede análise documental de 1c-A em paralelo, mas não autoriza implementá-la.

Pressupostos confirmados: núcleo inactivo, legado operacional, dados de demo
considerados descartáveis pela alternativa B arquitectónica. Hipóteses a verificar:
volume/composição efectiva da demo e existência/causa de diferenças. Dados
serem descartáveis não autoriza apagá-los nem dispensa preservação fiscal/auditoria.

## 3. Objectivo funcional proposto para 1c

Disponibilizar um relatório **interno e apenas de leitura**, por FT e com resumo
global do âmbito, que compare o saldo Pendente com o residual FinancialCore,
explique os vínculos e identifique impedimentos de uma futura transição.
Não devolver uma nova fonte de saldo operacional nem corrigir o que encontrar.

### Âmbito incluído proposto

- Empresas/clientes/moeda explicitamente delimitados; primeira fronteira FT EUR.
- Páginas estáveis por identidade documental, limite máximo 500; nenhuma colecção
  integral, N+1 ou resumo calculado apenas sobre a página.
- Identificar documento/Pendente/posição, saldo legado, residual núcleo, diferença
  exacta sem arredondamento implícito, linhas legadas activas, CONSUME/RESTORE
  vinculados e estado anulado. Preservar precisão de seis casas do legado e
  cêntimos do núcleo, sem converter a comparação numa política monetária nova.
- Classificar separadamente: origem/Pendente ausente, tipo/moeda fora do âmbito,
  RC legado activo, RC vinculado, RC NULL, CREATE revertido e efeito genérico sem
  semântica temporal. Sinalizadores podem coexistir; não ocultar casos por prioridade.
- Distinguir diferença numérica observada de causa comprovada e de causa desconhecida.
  Sem reproduzir o cálculo de saldo operacional a partir de uma soma ingénua de RC.
- Resumo e página na mesma fotografia SQL; páginas de pedidos separados podem
  mudar com novas operações, sem promessa de snapshot histórico persistido.
- Resultado sempre diagnóstico, sem escrever, activar ou inferir autorização de corte.
  Reutilizar/expor apenas no pacote financeiro interno; sem endpoint público ou UI.

### Exclusões expressas

Sem activar RC, consumidores ou feature flags; sem migração, backfill, limpeza,
repair, reconstrução de demo, novos documentos, precisão monetária nova, anulação
FT/RC adicional, F2/T1 novos, saldos/extractos operacionais ou regras fiscais.
Não implementar uma segunda autoridade nem copiar Data Valor para outra fonte.
Não mudar as recusas de balanceAt. Sem acesso à demo ou bases persistentes.
Não se prevê migration para esta consulta; se necessária alteração estrutural,
voltar ao PO com alternativas antes de a executar.

## 4. Critérios de aceitação propostos — não executados

1. FT sem liquidação: valores comparáveis iguais e vínculos rastreáveis.
2. FT 100/RC legado 30: apresentar 70 versus 100, assinalar legado activo e
   diferença observada; não declarar o núcleo errado nem tratar o caso como reconciliado.
3. FT com RC interno e saldo Pendente intocado: classificar preparação inactiva,
   sem apresentar residual interno como autoridade operacional.
4. RC T1/substituição DV-09 preservam identidades; comparar valores sem duplicar
   CASH_IN/RESTORE ou multiplicar resultados por joins de várias linhas.
5. NULL histórico continua NULL; CREATE revertido/efeito genérico ficam
   explicitamente fora da história suportada, sem data inventada ou total parcial.
6. Origem ausente e natureza/moeda fora do âmbito aparecem como cobertura incompleta,
   nunca como saldo zero conhecido; âmbito vazio tem contagem zero inequívoca.
7. Isolamento por empresa/cliente/moeda; pedido inválido recusado; sem fuga de dados.
8. 500/501 FT, empates, duas leituras estáveis sem escrita: ordem, zero duplicação/
   omissão, totais globais e página fora do intervalo correctos.
9. Escrita concorrente: resumo/página de cada pedido coerentes na mesma fotografia;
   não exigir igualdade artificial entre fotografias de pedidos distintos.
10. Evidência antes/depois confirma ausência de escritas, números reservados,
    alterações de esquema e chamadas aos adaptadores de emissão/anulação.
    operationalActivationPermitted continua false; leitores operacionais intocados.

## 5. Testes e revisão necessários na missão futura

PostgreSQL descartável com V24; testes dos dez critérios, SQL real/fixtures sem
contornar invariantes, regressão FinancialQueries/receipts e safety validator;
package backend e diff check. Os cenários aqui listados são futuros, não testes
executados nesta missão documental. Não confundir CI limitado com integração SQL.

Revisão independente Claude obrigatória antes de publicação: joins/totais,
precisão, completude das classificações, isolamento, snapshots, limites de memória,
ausência de escritas/activação e fidelidade das evidências. Corrigir BLOCKER/MAJOR;
PO conserva publicação/merge e a aprovação desta proposta.

Riscos de regressão: falso positivo de reconciliação, dupla contagem por joins,
comparação de precisões distintas, dados mudarem entre pedidos e ampliação indevida
da cobertura. Mitigar com testes negativos, fotografia SQL única por pedido,
valores originais separados e indicadores explícitos de cobertura.

## 6. Decisões reservadas ao PO

- **Agora:** aprovar 1c-B como Fatia 1c delimitada, escolher 1c-A/1c-C, ou pedir alteração.
  Não existe autorização de implementação nesta entrega.
- **Temporalidade FT:** alternativas materiais para futura decisão: efeito na
  emissão original (história rectificada remove dívida desde a origem) ou no acto
  de anulação (dívida persiste até ao acto), mantendo recusa enquanto não decidido.
  Exemplo FT 100 emitida dia 1 e anulada dia 20, sem recebimentos: consulta dia 10
  teria 0 na primeira alternativa e 100 na segunda. Benefício/custo: rectificação
  retroactiva versus conservação do intervalo de dívida; ambas exigem regras,
  vínculo imutável e testes próprios. data_hora_anulacao existe, mas não autoriza
  derivar automaticamente dia/fuso ou efeito financeiro. Recomenda-se manter a
  recusa actual até decisão funcional/fiscal fundamentada; não aplicar T1 RC à FT.
- **Dados/corte futuro:** avaliar exclusão controlada dos históricos incompatíveis
  versus migração/reconciliação validada. Primeiro caminho é mais simples para
  demo descartável mas exige dataset/perímetro aprovados; segundo preserva
  continuidade e custa mais, com risco de inferência. Não escolher nem executar
  qualquer um antes de inventário e autorização próprios.
- **RC NULL:** conservar ausência e compatibilidade actual é o estado aprovado;
  eventual política de entrada no perímetro novo exige decisão. Sem backfill fictício.
- **Activação/reversão de corte:** aprovar a fronteira conjunta, cobertura de tipos,
  permissões/chave pública, tratamento de dinheiro real e plano de dados/demo.
  Antes de escritas novas, retirar preparação inactiva é reversível; depois delas,
  voltar ao legado não é simples alternância de flag: pode reintroduzir saldos
  desactualizados e exige plano específico aprovado. Sem plano automático de rollback.
- **Fiscal/documental:** gate com responsável/evidência para SAF-T Payments, emissão
  tardia, IVA de caixa e formalismos de anulação/substituição; sem declarar conformidade.

Nenhuma decisão acima foi tomada pelo Executor. Recomendação 1c-B limita a próxima
intervenção a software de diagnóstico interno testável e preserva todos os gates.
