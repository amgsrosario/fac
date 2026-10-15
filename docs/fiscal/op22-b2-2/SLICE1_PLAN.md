# Data Valor — reconhecimento pós-merge e plano Fatia 1

15-10-2026. Base fc5717763e95c40a8b1361b60984254d401f2416, PR #26;
486b999, 31eee0e e cb872f3 integrados por ancestralidade Git. Fetch manual
confirmado pelo PO, sem fetch/reparação SSH nesta missão. Nenhuma implementação.
[Contrato aprovado](../../governance/decisions/ADR-002-financial-document-time.md).

## Matriz de nascimento — factos e lacunas

Caminhos abaixo relativos a src/main/java/com/ar2lda/fac. Não foi consultada
base operacional; não é possível enumerar o catálogo configurado em cada ambiente.
TipoDocumento possui codigoFiscal, sinalContabilistico e liquidacaoImediata;
classes de tipos configuráveis não equivalem a uma lista fiscal habilitada.

| Tipo/classe | Criação/origem/data persistida | Nascimento e validade de dataDocumento |
| --- | --- | --- |
| FT EUR emitida, positiva, não imediata | service/DocumentoComercialService.java:387–391 grava EMITIDO e chama PendenteService e FinancialFtOrigin; PendenteService.java:118–146 copia emissão comercial para dataDocumento; origem FT vinculada no núcleo | Originação financeira na emissão demonstrada; não prova nascimento jurídico/funcional universal. Confirmar contrato de nascimento para elegibilidade antes de usar campo como limite |
| FT noutra moeda | Emissão comercial actual recusa não-EUR; adaptador núcleo também exclui | Não habilitar por este plano; dados anteriores não provam semântica |
| Outros tipos não imediatos, positivos e emitidos (incluindo eventual ND configurada) | Mesmo método genérico, sem whitelist por código; validatePendente valida cliente/moeda/saldo, não natureza de obrigação | Emissão persistida, sem campo nascimento. Tipo pode ser tecnicamente aceite mas semântica não confirmada. Pendência bloqueante por tipo; não impor DV-10 nem proibir RC elegível por inferência |
| NC ou tipo credor que satisfaça predicados legados | Caminho genérico não distingue natureza; pode gerar Pendente positivo | Não interpretar como dívida. NC é crédito segundo OP19; actual aceitação técnica não habilita liquidação de débito |
| Tipo com liquidação imediata, incluindo eventual FR | criarDeDocumento retorna null; não tem dívida Pendente criada por esse caminho | Fora da selecção de pendentes; não inventar nascimento/adiantamento |
| Rascunho, sem número ou total não positivo | Criação de pendente recusada | Não é obrigação elegível demonstrada |
| Históricos/SQL/importação anterior | Colunas emissão/dataDocumento existem; ausência de origem/versionamento pode impedir reconstrução | Não provar nascimento pelo valor coincidente; não reconstruir nem backfill silencioso |

Evidências: PendenteService.java:118–146; DocumentoFinanceiroService.java:393–404;
financial/FinancialFtOrigin.java:28–43; model/TipoDocumento.java; baseline V1
(DocumentoComercial.data_emissao e Pendente.data_documento), sem data_nascimento.
DemoScenarioService usa FT, mas demo não prova todos os tipos em operação.

**Bloqueio técnico da validação inferior:** inexiste fonte inequivocamente
validada de nascimento para todos os tipos que o legado pode aceitar. Resolver
com inventário autorizado do catálogo/dados e contrato por tipo, distinguindo
facto técnico (copiar emissão) de decisão semântica (quando nasce a obrigação).
Se o PO confirmar emissão para um tipo, documentar explicitamente; se não,
apresentar alternativas de fonte/vínculo/persistência para aprovação. Não usar
created_at, momentoEmissao, vencimento ou dataDocumento como atalhos.

## Plano executável proposto — Fatia 1

A execução exige autorização posterior e resolução do bloqueio acima para a
validação completa; não é possível prometer essa validação universal já resolvida.
Não restringir silenciosamente RC não-FT/mistos. Um pacote de persistência/UI
sem limite inferior não satisfaz sozinho todos os critérios aprovados.

1. **Persistência:** model/DocumentoFinanceiro.java, migration nova candidata
   V23__receipt_value_date.sql, apenas se esse número continuar livre no início
   da implementação. Coluna data_valor DATE nullable para preservar históricos,
   sem UPDATE de backfill ou default SQL que fabrique datas anteriores.
   Serviço normaliza omissão para emissão nas novas emissões. Migration define
   protecção para exigir valor em novos RC, limite superior e imutabilidade em
   todos os RC, incluindo legado. Guard V20 evolui aditivamente para incluir a
   coluna; não editar migrations integradas. Regra inferior consulta a fonte
   aprovada por tipo e valida o conjunto final de linhas transaccionalmente,
   compatível com gravação em fases e constraints imediatas/diferidas.
2. **API/backend:** controller/dto/DocumentoFinanceiroCreateDto.java recebe
   dataValor opcional; DocumentoFinanceiroDto devolve dataValor nullable.
   mapper/DocumentoFinanceiroMapper.java preserva ausência histórica.
   service/DocumentoFinanceiroService.create valida data efectiva antes da
   numeração, sob locks de dívida em ordem estável, máximo nascimento e emissão.
   Mesmo request não pode contornar guarda por SQL directo. Não criar endpoint
   de edição do emitido. Listagem resumo só muda se precisar mostrar o campo;
   não trocar filtros documentais por Data Valor.
3. **Emissor interno:** FinancialReceiptIssuer normaliza Data Valor no payload
   antes de replay/numeração, grava campo e mantém precisão/guards. Não ligar o
   emissor a endpoint; não mudar FinancialQueries.financialEffectDate nem saldos.
   A eventual nova exigência SQL deve ser compatível com o caminho interno
   existente, seus testes e todos os construtores DTO usados em fixtures.
4. **React:** frontend/src/PendentesView.tsx, ReceiptForm/emptyReceiptForm,
   receiptFormKey, validateReceipt, payload e detalhe. Checkbox desmarcada;
   ao marcar começa na emissão; ao desmarcar data efectiva segue emissão;
   com campo explícito e alteração de emissão, revalidar, não corrigir data
   explicitamente escolhida sem aviso. API backend é autoridade. Detalhe mostra
   Data Valor ou «não registada», sempre com emissão separada. Checkbox não vai
   para persistência nem identidade financeira; só a data normalizada.
5. **PDF:** service/DocumentoFinanceiroPdfService.java (cabecalho/documento),
   testes DocumentoFinanceiroPdfServiceTests: emissão e Data Valor distintas,
   histórico «não registada». dataHoraOperacao preservada separadamente; não
   reutilizada como Data Valor. Rótulo técnico clarificado sem falsificar origem:
   DTO actual permite fornecer dataHoraOperacao; momentoEmissao/created_at têm
   papéis distintos. Não chamar automaticamente todos eles timestamp imutável
   de registo nem alterar o significado dos valores existentes.
6. **Auditoria:** registar emissão/dataValor efectiva e autor no evento existente;
   não substituir timestamp por data recebida do cliente; snapshot de datas
   emitidas imutável. Sem novos efeitos financeiros ou fiscais automáticos.

## Testes e critérios de aceitação — futuros, NÃO executados

- Unitários: normalização omissão/igualdade, limites e máximo entre obrigações,
  diferentes tipos elegíveis após semântica aprovada; data anterior recusada,
  igualdade aceite, data posterior à emissão recusada; cliente/moeda preservados.
- PostgreSQL descartável 16: upgrade V22→nova migration e segundo arranque;
  históricos continuam NULL; novos RC gravam data; SQL directo não altera datas
  emitidas nem contorna limites via linhas tardias; constraints em ambos modos;
  rollback não deixa número/documento parcialmente gravado.
- Emissão legada: montantes, descontos, saldo Pendente e sequência invariantes;
  não-F2 em extractos; concorrência, autorização e bloqueio entre circuitos.
- FinancialReceiptIntegrationTests/MigrationTests: replay omissão=igualdade,
  mesma chave/data diferente recusa, atomicidade e V18/V20/V21/V22 preservadas;
  novo emissor continua sem endpoint/activação.
- Frontend: checkbox/default/toggle, mudança de emissão, payload efectivo,
  detalhe emitido/histórico e erro backend; PDF contém duas datas/ausência honesta.
- TestDatabaseSafetyValidatorTests, package e build frontend proporcionais na
  implementação. Nenhum teste de aplicação necessário para este fecho documental.

Aceitação: campo persistido/imutável, default correto, fonte de nascimento
confirmada para cada tipo abrangido, limite completo validado em serviço/SQL,
PDF/API/UI coerentes, histórico preservado, saldos/extractos/numeração inalterados.

## Fatias seguintes e corte

Fatia 2: F2/T1 no núcleo, actos e vínculos de anulação/substituição, identificação
monetária real e DV-09; leitores financeiros preparados inactivos. Não habilitar
balanceAt parcialmente nem converter contra-lançamento em reembolso automático.

Corte operacional: autorização específica, dataset coerente, demo/Flyway verificados
em missão própria, validação fiscal com responsável/evidência, testes e revisão;
activar emissão, liquidação, anulação e leitores conjuntamente. Fatia 1 conserva
saldo legado como autoridade operacional, sem segundo livro concorrente.

## Decisões versus verificações pendentes

- Técnica: inventariar tipos reais autorizadamente, demonstrar fonte/nascimento,
  sequência JPA/JDBC, número migration e cobertura de todos os escritores DTO.
- Produto: semântica do nascimento nos tipos indeterminados, caso não resulte
  de contrato existente; eventual nova fonte estrutural compara alternativas
  GOV-ALT-001. Não é necessária nova decisão para checkbox/DV-01–09 já aprovadas.
- Fiscal: emissão tardia, Payments, IVA de caixa, representação e substituição
  permanecem por validar; esta análise não estabelece conclusões jurídicas.
- Implementação e corte: ambas exigem autorização posterior; nada iniciado aqui.
