# OP22-B2.2 — Historial financeiro, consumidores e resposta à revisão

14-10-2026. Complemento exclusivamente documental, base ad2d28a, entrega anterior
486b999. FIN-HIST-01–03 autorizados pelo Product Owner. Implementação, migrations,
activação, demo e corte não autorizados. Todos os cenários abaixo são aceitação
futura, não testes executados. Contrato em [ADR-002](../../governance/decisions/ADR-002-financial-document-time.md).

## Elegibilidade observada e limites

PendenteService.criarDeDocumento/novoPendente aceita documento emitido/numerado,
total positivo e sem liquidação imediata. DocumentoFinanceiroService.validatePendente
valida cliente/moeda/saldo, não código FT nem natureza fiscal. Originação
FinancialFtOrigin limita-se a FT EUR; FinancialReceiptIssuer exige origem válida.
Logo, legado pode aceitar tipos configuráveis não-FT e mistos; essa aceitação
não prova elegibilidade de dívida. NC é crédito segundo OP19, não dívida porque
existe Pendente positivo. ND e outros tipos não têm integração documental no núcleo
demonstrada; confirmar natureza/efeito e regras antes de habilitar. Não bloquear
RC legados elegíveis nem impor Data Valor igual à emissão (DV-10 retirada).
Limite-alvo: máxima data de nascimento das obrigações elegíveis ≤ Data Valor ≤ emissão RC.
Adiantamentos, dinheiro não afectado e habilitação de novos tipos permanecem fora.

## Inventário técnico — factos de código e alvo

Caminhos relativos a src/main/java/com/ar2lda/fac, salvo frontend explicitado.
Todos os consumidores financeiros podem ser preparados isoladamente, mas não
ligados à operação antes do corte comum. Campos documentais não devem ser
substituídos silenciosamente por datas financeiras.

| Consumidor / evidência | Fonte e data actuais | Contrato-alvo / dependências | Risco e preparação | Aceitação futura |
| --- | --- | --- | --- | --- |
| service/ExtratoClienteService; repository/DocumentoFinanceiroRepository.findExtratoAnterior/findExtratoMovimentos | Documentos comerciais/financeiros; emissão, filtros anulado=false | Obrigações por efeito; RC por Data Valor; RESTORE na Data Valor original; conservar original/acto/emissão | Excluir anulados elimina a cronologia; preparar novo provider inactivo; emissão/anulação/reversão completas | A/C/D/E; limites de período e outras liquidações independentes |
| service/PendenteService.contaCorrente e diagnosticoContaCorrenteCliente; repository/PendenteRepository | Pendente.valorPendente, dataDocumento/vencimento; linhas RC por emissão | Residual do núcleo para estado corrente; movimentos por efeito no historial | Datas de origem/vencimento mantêm função; não basta trocar uma ordenação; projecção inactiva com todos os vínculos | B/F; paginação, saldos, reabertura e ordem determinística |
| service/ListagensService; repository/DocumentoFinanceiroRepository.findResumos/findAnaliticos; frontend/src/ListagensView.tsx | Documentos/linhas por emissão; pendentes legados | Listagem documental conserva emissão; listagem financeira por efeito identifica Data Valor | Nem toda listagem exige trocar filtro; distinguir contratos e preparar fonte financeira isolada | A/D/F; filtros por emissão distintos de efeito, totais e fronteiras |
| service/ClienteService.listComPendentes; repository/ClienteRepository.findComPendentesAbertos | Contagens e valores Pendente; estado corrente, sem data histórica própria | Residual corrente do núcleo; sem inventar parâmetro de data | Mistura de núcleos gera clientes/saldos errados; trocar provider só no corte | C/F; cliente sem saldo, parcial, moedas e reversão |
| service/DashboardComercialService.consultar/evolucao/clientesComMaiorSaldo | SQL documentos por data_emissao; pendente corrente e vencimento | Vendas documentais mantêm emissão; recebimentos financeiros por efeito; saldos pelo núcleo | Métricas distintas não podem partilhar data por mera substituição global; preparar agregados desligados | A/C/F; períodos, vendas invariantes e recebimentos rectificados |
| reporting/extrato/ExtratoClienteReportDataService, ExtratoClienteExcelExporter/PdfExporter | DTO preparado pelo serviço de extractos | Mesma semântica financeira e duas datas identificadas; original/reversão preservados | Não duplicar cálculo no exportador; reproduzir artefacto anterior é capacidade distinta | A/C/D/E; equivalência API/XLSX/PDF e sinais |
| reporting/pendentes/PendentesReportDataService/ExcelExporter/PdfExporter; repository/PendentesListagemRepository | Saldo corrente Pendente ou histórico calculado por RC não anulados até data_emissao | Núcleo corrente/história rectificada por efeito; vencimento continua vencimento | Reconstrução legada por exclusão de anulados não representa T1 auditável; provider inactivo | A/C/E/F; data referência, dívida e totais globais |
| reporting/listagens/ListagemTabularExporter; service/DocumentoFinanceiroPdfService | DTOs/documento financeiro; emissão e dados técnicos | PDF RC acrescentará Data Valor identificada, mantendo emissão; exports seguem contrato do dataset | Exportador não decide temporalidade; PDF Data Valor ainda não implementado | DV-06; datas distintas e snapshots/numeração preservados |
| service/DocumentoFinanceiroService.getDiagnostico; service/PendenteService.diagnosticoContaCorrenteCliente | Snapshots de linhas, Pendente e estado anulado | Dependências efectivas e residual do núcleo; histórico de actos | Snapshot não deve repor saldo sobre movimentos independentes; diagnóstico isolado até corte | C/D; não ocultar dinheiro real, erros controlados |
| financial/FinancialQueries, JdbcFinancialQueries, FinancialReceiptProjections | Posições/ledger; CREATE emissão FT, SETTLE emissão RC, REVERSE sem efeito; balanceAt suspenso | F2/T1 por vínculos; historial imutável, timestamp distinto; histórico sem Data Valor usa compatibilidade | Projecções já isoladas não autorizam activação; require emissão/anulação/data/guards completos | A–F; concorrência, SQL directo e consulta rectificada |

Não se assume que cada exportador ou listagem precisa de novo cálculo: primeiro
classificar consulta documental versus financeira. Vencimento e emissão não são
removidos. Auditoria por instante e reprodução exacta de extracto permanecem fora.

## Complemento MINOR-N1/N2 — evidência e consumidores

N1: regra PO usa nascimento da obrigação, máximo entre várias, nunca data
posterior à emissão RC. PendenteService.java:118–143 cria pendentes para tipos
sem liquidação imediata e copia dataEmissao. Não prova equivalência semântica
para todos os tipos aceites; FinancialFtOrigin.java:28–41 demonstra fronteira FT
EUR. Fonte inequívoca de nascimento por tipo elegível é pendência técnica
BLOQUEANTE da implementação universal. Não substituir nascimento por dataDocumento.
Teste futuro: duas obrigações com datas distintas; nascimento diferente da
emissão quando houver tipo aprovado nesse caso; pedido inválido faz rollback.

| Consumidor adicional / evidência | Fonte actual | Contrato-alvo | Risco/preparação e testes futuros |
| --- | --- | --- | --- |
| service/DocumentoComercialService.java:242–245,400–429; repository/DocumentoComercialRepository.java:32 | liquidado persistido; podeAnular usa estado, linhas RC activas e saldo Pendente; revalidação no serviço | Estado compatível derivado de afectações/residual/dependências efectivas do núcleo, sem inventar nova semântica do booleano | Não confiar em saldo Pendente obsoleto; preparar atrás da fronteira inactiva; testar RC parcial/total, RESTORE, anulação FT bloqueada por dependência e concorrência |
| frontend/src/DocumentosView.tsx:672,777 | DTO.liquidado apresentado como Sim/Não | Mesma apresentação alimentada por contrato backend coerente com núcleo no corte | Não trocar significado só no frontend; testar parcial/total/revertido e igualdade resumo/detalhe; decisão sobre semântica de estado por explicitar se mudar |
| frontend/src/PendentesView.tsx:77,266,633 | ReceiptForm dataEmissao; saldo Pendente para selecção/montantes; POST documentos-financeiros legado | Interface Data Valor aprovada e dívidas/residuais coerentes do núcleo; nascimento validado server-side | Formulário não prova elegibilidade/nascimento; preparar API/projecção inactivas; testar checkbox, vários tipos elegíveis, máximo nascimento, retries e rejeição concorrente sem número |
| demo/DemoScenarioService.java:34,67–77,103–104 | Emite comerciais e RC através de DocumentoFinanceiroService legado, lê Pendente e usa datas fixas | Dataset autorizado com obrigações/recebimentos/vínculos/datas coerentes no corte | Gerador actual não prova reconciliação; não executar nem alterar agora; futura preparação isolada e autorizada, teste de origens/RC/saldos e nenhuma coexistência incompatível |

N2 cobre também estado comercial e preparação de demo, não apenas saldos exibidos.
Nenhuma execução da demo, reconstrução, alteração de consumidor ou regra funcional.
Revisão Claude do complemento pendente; N1/N2 têm resposta documental, não prova
por testes. O cenário B passa a verificar nascimento de todas as obrigações,
independentemente do tipo elegível, sem presumir elegibilidade de tipos indefinidos.

## Cenários de aceitação — NÃO EXECUTADOS

A. FT emitida 01/10, 1.000; pagamento 05/10, 300; RC emitido 14/10, Data Valor
05/10. História rectificada liquida em 05/10, residual 700 desde essa data;
RC conserva emissão 14/10. Comparar filtros documentais e financeiros.

B. Várias obrigações de débito elegíveis: Data Valor não precede nenhum nascimento de obrigação,
não ultrapassa emissão RC; cada afectação rastreável, montantes/residuais próprios.
Uma obrigação com data posterior invalida o pedido inteiro. Não inferir que NC
ou tipo sem semântica confirmada é elegível. Não impor DV-10 em recibo misto.

C. Anulação autónoma: RESTORE na Data Valor original, acto com data própria,
original intacto, outras afectações independentes preservadas. Identificar e
validar situação monetária real antes; sistema actual não prova essa informação.
Sem solução válida para dinheiro efectivamente recebido, não executar anulação
que o oculte; não autorizar reembolso automático nem saldo não afectado.

D. RC original 30, substituto 300 com motivo: 30-30+300=300, nunca 330.
Contra-lançamento CASH_OUT reversed=true ligado ao CASH_IN original; RESTORE
ligado às afectações; nova afectação não excede dívida válida. Rollback conjunto,
retry único e concorrência impedem duas substituições. Reembolso real é distinto.

E. Histórico sem Data Valor conhecida permanece sem data efectiva persistida;
emissão só para compatibilidade, apresentação não inventa pagamento. Sem backfill.

F. Legado permanece operacional segundo contrato actual até corte autorizado.
Nenhuma nova leitura/listagem activada isoladamente; preparar consumidores atrás
da mesma fronteira, com datasets descartáveis em futura missão autorizada.

## Matriz dos cinco MINOR — resposta do implementador

| Finding Claude / 486b999 | Correcção | Reserva |
| --- | --- | --- |
| MINOR-001 OP22-A | QUERY_CONTRACTS.md remete para emenda vigente e delimita texto histórico/alvo/transição | Código não implementa F2/T1 |
| MINOR-002 proibição residual | ADR-002 §4.1 declara Data Valor como única excepção | Nenhuma data autónoma adicional |
| MINOR-003 não-FT | ADR §4.2 e FIN-HIST-02 precisado por N1: nascimento das obrigações elegíveis; inventário técnico; DV-10 retirada | Aceitação legada não prova elegibilidade fiscal/natureza; tipos indeterminados aguardam decisão |
| MINOR-004 dinheiro real | ADR §5 exige identificação/declaração/registo/validação antes de anular | Mecanismo e evidência monetária ainda inexistentes/por implementar |
| MINOR-005 revisões | ADR rastreia Claude original 3MAJOR/5MINOR, Codex separado 2MINOR e Claude 486b999 5MINOR | Revisão Claude complementar pendente; não declarar zero findings antecipadamente |

## Sequência proposta — sem autorização de implementação

1. Rever este complemento pelo Claude e integrar documentalmente após decisão PO.
2. Confirmar tipos elegíveis/efeitos, fiscalidade e mecanismo de situação monetária.
3. Missão autorizada para Data Valor/guards/idempotência e anulação/substituição
   internas, preservando V18/V20/V21/V22 e circuito novo inactivo.
4. Preparar consumidores isolados e testes A–F; verificar demo/inventário e plano
   de corte em missão autorizada, sem presumir dados reconciliados.
5. Corte conjunto apenas após revisão, validação fiscal com responsável/evidência
   e autorização PO: emissão, liquidação, anulação, leitura e consumidores.

Não foram executados testes, migrations ou operações sobre bases. Nenhuma
conclusão jurídica nova: SAF-T Payments, emissão tardia, IVA de caixa e formalismos
continuam por validar. Não foi alterado AR2-Governance.
