# OP19 — Matriz de aceitação

Versão 1.1 · 09-10-2026. Todos os testes abaixo são especificações futuras,
NÃO executados nesta OP e NÃO provados pela regressão histórica de RIVA.
Montantes em EUR, salvo indicação. Cada cenário verifica snapshots, movimentos,
componentes de caixa, auditoria e ausência de alterações aos originais.

| ID / requisito | Preparação e ação | Pós-condições verificáveis |
| --- | --- | --- |
| A01 F02/F10 | Emitir FT1000 e NC1000, sem afectação | D1000/C1000/S0; duas abertas; caixa0; nenhuma afectação implícita |
| A02 F06 | A01; compensar1000 | D0/C0; duas esgotadas; afectação1000; entradas/saídas0; documento condicionado ao gate L02 |
| A03 F04/F06 | FT1000, NC800; compensar800 e receber200 | D0/C0; caixa entrada200; parcelas800+200, sem duplicar NC |
| A04 F06 | FT1000, NC800; compensar800 | D200/C0/S200; FT parcial; NC esgotada; caixa0 |
| A05 F07 | FT1000; desconto20 e caixa980 | D0; dívida extinta1000, desconto20, entrada980; nenhuma NC automática; gate L03 |
| A06 F08 | FT800; receber1000 | Após gate L04: D0/C200/S−200, adiantamento distinto de NC, entrada1000; antes do gate fluxo bloqueado sem efeitos |
| A07 F05/F06 | NC500 e FT300; devolver200 e compensar300 | D0/C0; saída200/entrada0; consumos distintos200+300 com origens; gate L01 |
| A08 concorrência | D100; dois pedidos100 simultâneos, chaves distintas | Apenas um commit; outro conflito; consumo total100, residual0; sem número/caixa duplicado |
| A09 idempotência | Confirmar100, perder resposta e repetir mesma chave/pedido | Mesmo ID/numero/resultado; uma operação; chave com valor diferente rejeitada |
| A10 F09 | Criar adiantamento e consumi-lo; pedir estorno da criação | Bloqueado com dependência indicada; estornar consumo e depois criação; movimentos inversos únicos e originais preservados |
| A11 rectificação | Fatura com bases100 às taxas6/13/23; rectificar bases10 por grupo | IVA calculado0,60/1,30/2,30 conforme snapshots históricos; totais por linha; não usar taxa atual; não liquidado distinto se aplicável |
| A12 moedas | Posições EUR100 e USD100 | Leituras separadas, sem totalEUR200; operação entre moedas recusada sem conversão autorizada; emissão nãoEUR continua bloqueada |
| A13 monetário | Quantidade3 × preço0,335, desconto10%, IVA23% normal | Bruto1,01; desconto0,10; base0,91; IVA0,21; total1,12; preview/gravação/PDF iguais |
| A14 sucessivas | Fixture normal EUR: FT base100,00, taxa23%, IVA calculado/liquidado23,00, total123,00; NC1 base30,00, IVA6,90, total36,90; NC2 base20,00, IVA4,60, total24,60; sem afectação | D123,00/C61,50/S61,50; base rectificada50,00 e imposto11,50; original inalterado; limites específicos e gate L05 conforme detalhe abaixo |
| A15 parcial | FT1000; pagamentos100 e200 independentes; estornar primeiro | Residual700 antes,800 depois; segundo pagamento preservado; não bloquear por data apenas |
| A16 histórico | EmitidoV1/V2 com mestres/taxas posteriormente alterados | Original não recalculado; origem fiscal incerta bloqueia rectificação automática; leitura legacy preservada |
| A17 F02 | FT1000 já paga; emitir NC1000 | Crédito1000 autónomo; dívida continua0; nenhum reembolso automático |
| A18 F03 | FR100 e retry de emissão | Uma faturação/um recebimento, D0/C0, caixa100; sem RC duplicado |
| A19 modo livre | Rappel de conjunto de faturas com suporte e referências congeladas | Sem enumeração manual universal; rastreabilidade reproduzível; falta de elemento legal bloqueia emissão |
| A20 concorrência | Liquidação e estorno simultâneos, duas séries/posições com ordem oposta | Sem perda de atualização/double restore; ordem de locks comum; resultado serializável ou conflito integral |
| A21 duplicados | Mesmo ID de posição repetido no pedido | Normalização determinística ou rejeição documentada antes de escrita; estorno próprio possível |
| A22 snapshot | Alterar nome/sinal/tipo/cliente após confirmação | Extrato/PDF histórico e caixa mantêm significado e valores congelados |
| A23 atomicidade | Falhar depois de numerar mas antes do último movimento | Sem operação/posição/caixa parcial; retry único; auditoria de recusa sem segredo |
| A24 migração | Importar dataset demonstrável duas vezes e outro com NC ambígua | Primeira reconcilia por cliente/moeda/origem; segunda não duplica; ambiguidade isolada e preservada |
| A25 F02/F06 | Fixture independente: FT1000/NC800, sem afectação (D1000/C800/S200); confirmar compensação X300 | D700/C500/S200; caixa0; NC parcial e aberta, nunca encerrada com residual500; afectação X identificada |
| A26 F02/F06 | Nova fixture: FT1000/NC800; X300 confirmada; confirmar Y200 usando crédito ainda disponível | D500/C300/S200; caixa0; X e Y únicas, NC parcial e aberta; Y referencia posições originais e não recurso criado por X |
| A27 F09 | Nova fixture: FT1000/NC800, X300 e Y200 confirmadas independentemente; estornar apenas X | D800/C600/S200; caixa0; Y200 permanece ativa; inversos de X restauram apenas300 por posição; NC aberta; dependências verificadas |
| A28 F09 | Fixture alternativa com dependência efetiva comprovada: Y consome recurso criado por X; pedir estorno X | Bloqueio sem alteração enquanto Y ativa; estornar Y primeiro e X depois, com inversos únicos; não assumir D800/C600 para este grafo |
| A29 F01 | ND normal base100,00, IVA calculado/liquidado23,00, total123,00; suporte suficiente no cenário L05 validado | Posição devedora123,00, residual123,00; nenhuma afectação ou caixa automática; snapshot próprio |
| A30 F02 | NC assistida de uma FT normal base100/IVA23/total123; rectificar base10/IVA2,30/total12,30 | Crédito autónomo12,30; FT continua D123; origem fiscal congelada; sem compensação automática; gate L05 |
| A31 F02 | NC assistida de duas FT normais (cada base100/IVA23/total123); rectificar base10 de cada | NC base20/IVA4,60/total24,60, crédito24,60; duas origens congeladas, dívidas246 intactas; sem seleção manual universal; gate L05 |
| A32 F01 | ND assistida de uma FT: aumento base10/IVA2,30/total12,30 num cenário L05 validado | Nova dívida12,30, referência e snapshot próprios; dívida da FT não reescrita; caixa0 |
| A33 F01 | ND assistida de duas FT: aumento base10 em cada/IVA total4,60/total24,60 num cenário validado | Nova dívida24,60; duas origens rastreáveis, documentos originais intactos; caixa0 |
| A34 F02 | NC livre/rappel global: base20/IVA calculado/liquidado4,60/total24,60; conjunto e suporte congelados, cenário L05 validado | Crédito autónomo24,60; suporte reproduzível sem enumeração manual universal; caixa0; prova funcional não confirma formalismos legais |
| A35 F01 | ND livre: base20/IVA calculado/liquidado4,60/total24,60; fundamento e suporte suficientes no cenário L05 validado | Dívida autónoma24,60; rastreabilidade e snapshot próprios, caixa0; modo livre não dispensa formalismos |
| A36 L05 | Para NC/ND, modos assistido uma/múltiplas origens e livre: retirar fundamento ou elemento documental legalmente exigido na fixture validada | Emissão bloqueada sem posição/movimento/consumo definitivo de número; motivo identificável; enquanto L05 pendente provar gate, não aprovação jurídica |

## Fixture A14 e limites de rectificação

Valores das NC são magnitudes positivas de posições credoras; bases não são
totais. Tributação normal a23%, sem desconto nem retenção, seguindo o motor
RIVA existente. Para esta fixture de redução de uma única base original,
registar acumulados base50,00/IVA11,50/total61,50 e remanescentes elegíveis
base50,00/IVA11,50/total61,50. O limite económico do cenário é base100,00,
IVA23,00 e total123,00, sem reutilizar a parcela já rectificada. Uma terceira
redução base60,00 excede esse limite e deve ser recusada neste cenário
específico previamente validado. Isto é um oráculo técnico condicionado a
L05, não uma regra jurídica universal de NC/ND, rappels ou aumentos por ND.
Antes da validação do cenário legal, testar bloqueio da emissão fiscal, não
considerar a especificação prova de conformidade.

A25–A27 têm fixtures autónomas reproduzíveis; a sequência X/Y está explicitada
na preparação de cada caso. Compartilhar posições não implica que Y dependa
de X: ambos consomem a origem FT/NC, sem consumir recurso criado pelo outro.
A28 testa separadamente o grafo com dependência real; sua preparação deverá
identificar o recurso criado por X e consumido por Y. Todos os casos zero-caixa
preservam L02: comportamento do motor não presume documento RCzero válido.
A29–A36 distinguem resultados funcionais sob fixture legal validada de emissão
bloqueada quando o enquadramento ainda não foi validado. Não foram executados.

## Método de prova futuro

A08/A20 requerem barreiras entre transações reais em PostgreSQL descartável,
não apenas chamadas sequenciais. A09 inclui timeout após commit e concorrência
na mesma chave. A10 inclui estorno repetido e tentativa de dependência cíclica.
A15 inclui operações independentes noutras posições para provar não bloqueio.
A13 compara decimal exato; acrescentar resíduos repartidos por várias afectações,
com soma igual ao valor fechado e desempate determinístico por ID.

Por caso guardar commit, versão de schema/política, fixture, comando, relatório,
resultado e limitações. Testes unitários do motor não substituem integração
transacional; mocks UI não demonstram fiscalidade; prova interna não certifica.
Contratos de códigos/RCzero/adiantamentos só têm prova positiva depois da
validação legal correspondente; antes disso provar a recusa sem efeito.
