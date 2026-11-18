# FR/EUR — matriz de testes proposta

R1 documental de 18/11/2026: I04/I07/I13 explicitados para MINOR-001/002; nenhum caso executado ou acrescentado ao código. Mantêm-se os 17 IDs, com subcasos delimitados. Nenhum teste executado nesta preparação. Expectativas fixadas em PLAN.md §4, independentes do código da aplicação. São critérios para um mandato futuro, não provas já realizadas.

## Cobertura existente reutilizável

- PR #58: FinancialFrIntegrationTests (12) e FinancialFrIsolationTests (2), FinancialCore (15), integridade comercial (26), permissões (6): 61 sobre `678e3f3`. Provas anteriores FT/RC/consultas/upgrade e controller têm outros HEADs; ver fr-eur-inactive/VALIDATION.md. A identidade/projecção externa FR **não foi demonstrada** por estes testes.
- M4-04: FtIdentityJourneyTests, método governedFtIdentityMatchesPersistedSnapshotAndActualPdfQr, cobre ID interno diferente de FT, snapshots, G/H e leitura do PDF/QR real. Reutilizar o método e PdfQrInspection; não apresentar o oráculo FT como validação FR.
- M4-09 cobre seis cenários monetários em duas execuções; reutilizar limites EUR/descontos/arredondamentos sem atribuir homologação geral.
- Classes de catálogo/configuração SQL/Java, Series, QrFiscalPayloadBuilderTests, FiscalQrIdentityTests e PDFs são base de regressão. Seleccionar só afectados pelo futuro diff; nenhuma nova suite integral por rotina.

## Casos adicionais e critérios

| ID | Ensaio sintético futuro | Aceitação verificável |
| --- | --- | --- |
| I01 | Contexto por omissão e manifesto produtivo vazio | Sem bean FR, rota ou consumidor novo; arranque legado preservado; criação/projectar FR sem revisão recusa em fechado |
| I02 | Revisão FR sintética Java/PostgreSQL e leitura estável | Mesma definição/materialização; especialização/projecção explícitas; revisões antigas preservadas; sem max/latest ou edição livre |
| I03 | INSERT/UPDATE/UPSERT/conversão por CRUD, SQL directo e cliente antigo | Configuração FR projectada incoerente ou sem vínculo recusada, mesmo antes da primeira emissão; V25/V26/V27/V28 mantidos; nenhum histórico reclassificado |
| I04 | (a) Emissão ordinária de nova FR governada; (b) criação de **nova FT imediata sem vínculo** e emissão ordinária com pré-condições normais | (a) Recusa antes de consumir número/consolidar documento, sem FR sem quitação. (b) Comportamento legado mantido neste incremento: emissão D=FT sem acto FR/quitação, verificado por leituras persistidas e comparação dos efeitos financeiros; **C6 continua aberto**. Não transformar esta regressão em prova de FR nem impor vínculo/bloqueio sem decisão administrativa separada |
| I05 | Percurso interno FR por MockMvc, em controlador **só de teste**, com fonte FiscalSignatureTestConfiguration | Documento único EMITIDO, quitação integral, vínculo V30; tipo interno FT e código interno FRE distintos da projecção externa FR |
| I06 | Série TESTFR e duas emissões sequenciais | FRE TESTFR/1 e /2; ATCUD TESTFR01-1 e -2; D=FR, G=número persistido, H=ATCUD; numeração/snapshot apresentados iguais no PDF |
| I07 | Alias/série fiscal duplicada, código/série de outro tipo, falta/incoerência de validação, e **reutilização ou reclassificação de código interno já usado**: FT→FR e uso noutro tipo/tabela, por CRUD/SQL directo/UPSERT e chamadas concorrentes | Recusar a atribuição de outro tipo externo ao mesmo código em todo o histórico, incluindo entre documento_comercial/documento_financeiro e famílias aplicáveis, mesmo após anulação ou tentativa de eliminar/recriar configuração. Usar histórico sintético válido, sem fabricar dados incompatíveis. Prova Java e SQL independentes; nenhum documento/snapshot/numerador/efeito alterado. Sem segundo contador ou código AT inferido |
| I08 | Nova fonte de assinatura vê snapshot final e identidade fiscal do documento | Dados de pagamento e projecção consolidados antes de assinar; Q/R derivados da fonte sintética ligada ao documento; sem defaults ou fonte real |
| I09 | Fonte ausente, revisão/coerência ausentes, falha durante emissão/SETTLE/vínculo ou commit diferido | Recusa/rollback de documento, série, posição, caixa, auditoria e projecção. Exigir causa esperada, não apenas uma RuntimeException genérica |
| I10 | Replay idêntico; payload/actor diferentes; duas sessões mesma chave e chaves diferentes | Um acto, um recebimento, um número; igualdade exacta ou recusa fundamentada; não duplicar caixa/dívida |
| I11 | Comparação financeira e consulta histórica | Original/recebido/afectado 12,30; residual 0, nenhum RC/Pendente; datas conforme PLAN; leituras persistidas após pedidos, sem concluir por flags UI |
| I12 | PDF longo e reimpressões, revisão posterior do catálogo/mestres | Título Fatura-recibo e quitação válidos; mesmo número, H e QR; ATCUD em todas as páginas, geometria preservada; nenhum novo movimento/numeração. Só auditoria/marca impresso permitidas pelo contrato existente |
| I13 | FT diferida, RC e documentos antigos, incluindo FT/imediata sem nova projecção | Identidades/payloads antigos inalterados; não usar booleano actual ou revisão mais recente para reimprimir como FR. Código IFR comprovadamente usado com D=FT não pode ser reclassificado/reutilizado para FR; exigir código novo para o novo percurso. Nenhuma nova regra RC/RG ou restrição ao caminho legado FT imediato sem vínculo; C6 mantido |
| I14 | Anulação/reversão avulsa e alterações pós-FR | Continuam recusadas; este incremento não cria acto de anulação nem altera datas de reversão |
| I15 | SQL tenta gravar projecção FR sem acto integral, mudar projecção/serie/revisão ou acrescentar efeitos | Commit recusado; vínculos/identidade/montantes imutáveis, protecção independente da aplicação |
| I16 | Upgrade aditivo desde V30 e segundo arranque | Apenas migrations novas; dados/snapshots e definições antigas preservados, sem backfill ou manifesto publicado; segundo arranque 0 migrations |
| I17 | Mapeamento futuro SAF-T revisto | InvoiceType FR separado de InvoiceNo interno; fonte de pagamento explícita, Hash integral/HashControl não extraídos de Q/R. Apenas teste de mapper em futuro mandato que o inclua; nenhum XML/exportador nesta preparação |

I05 não autoriza um endpoint em src/main. A identidade de ensaio FRE/TESTFR/TESTFR01 é exclusivamente sintética e não corresponde a comunicação real. A taxa/cliente/meio de pagamento da fixture não aprovam a matriz comercial, IVA de caixa, retenção ou moeda diferente de EUR.

## Protocolo a anexar ao futuro mandato

PostgreSQL descartável com cluster novo e labels/IDs próprios; nenhuma montagem/cópia de bases conservadas ou demo/GOLDEN. Fonte de assinatura de teste apenas no classpath de testes; sem chaves/certificados reais ou chamadas à AT. Fixar expectativas antes da execução, HEAD/tree/status limpos e hash das fontes; capturar código de saída, logs/XML, pedidos/respostas e leituras financeiras. Arquivar falhas separadamente, manifestos SHA-256 e prova de remoção dos recursos. Não somar fases/HEADs como testes únicos.

Comparar o PDF efectivamente produzido e descodificar o QR desse PDF; uma asserção sobre o payload em memória não prova a impressão. Identificar o conjunto de campos que compõe a assinatura de teste e os limites dessa prova. Sem emissão positiva pelo JAR ou homologação produtiva por arrasto.
