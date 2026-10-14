# OP22-A — Contratos internos de consulta financeira

12-10-2026 · Base observada: 12a3aed7f35085ab0f852c48530ee21a467c490c.
Implementação local, sem publicação ou activação operacional.

## Fronteira implementada

FinancialQueries/JdbcFinancialQueries são contratos internos de leitura. Não há
controller, consumidor operacional novo ou escrita. Pendente continua fonte de
saldos dos circuitos existentes; RC legado ainda não afecta o núcleo. O residual
consultado é o do FinancialCore e não deve ser apresentado como conta corrente
operacional enquanto a fronteira FT/RC estiver incompleta.

Scope reúne empresa, cliente e moeda; todos os SQL filtram esses três campos.
É contexto resolvido por um consumidor autorizado, não uma credencial nem uma
permissão. Não expor Scope recebido de payload sem validação de autorização.
As leituras de IDs fora do contexto falham sem revelar o contexto proprietário;
listas/saldos sem posições devolvem vazio/zero. Não há agregação de moedas.

| Contrato | Resultado e ordenação |
| --- | --- |
| positions | Página por ID ascendente, limite 1..500, offset >=0; natureza opcional, ALL/OPEN/CLOSED; total global do mesmo filtro |
| position | Identidade, natureza, montante original, residual e estado individual |
| byCommercialDocument | Vínculo V19 explícito; vazio quando ausente ou fora do contexto, sem backfill |
| balance | Dívida aberta, crédito disponível, saldo devedor líquido assinado e contagens abertas/encerradas |
| allocations | Página por operation_id/id ascendente, montante/effect, reverse_of de afectação e operação, timestamp técnico |
| operation | Operação, autor/motivo, reverse_of/reversed_by, origem documental de CREATE quando vinculada e componentes monetários com IDs de estorno |
| balanceAt | Contrato suspenso: lança UnsupportedOperationException com motivo, nunca devolve saldo corrente nem usa timestamp como fallback |

OPEN/PARTIAL mantêm residual positivo; EXHAUSTED tem residual zero;
CANCELLED é estado explícito de anulação da posição. CLOSED inclui EXHAUSTED e
CANCELLED. O saldo líquido zero não encerra posições nem gera compensação.
Saldo líquido devedor = dívida aberta - crédito disponível; pode ser negativo.
Dívida e crédito permanecem positivos e separados. Totais não dependem da página.
As páginas de posições/afectações calculam count e linhas numa só instrução SQL,
incluindo páginas vazias além do fim. Cada chamada tem o seu snapshot SQL;
páginas pedidas em transações diferentes não prometem snapshot de exportação
imutável perante escritas concorrentes. A ordenação por IDs resolve empates,
mas não define cronologia financeira nem ordem de commit.

## Identificação documental e precisão

Reutilizam-se financial_document_origin e IDs/links imutáveis de V18/V19.
A referência emitida é a reference conservada na posição; datas de emissão e
vencimento vêm de documento_comercial. CREATE conhece o seu documento pela
posição e vínculo; SETTLE ainda não possui vínculo a documento_financeiro.
DocumentLinkStatus.NOT_RECORDED declara essa ausência; não se deduz um RC por
cliente, montante, data, request_key ou texto de origem. REVERSE conserva o ID da
operação estornada; não toma a emissão do documento original como data do estorno.
A utilização destes vínculos em RC exige desenho/autorização posterior.

Os valores numeric(19,2) são lidos por BigDecimal sem MotorMonetario, setScale ou
conversão. Montantes RC legados de seis casas não entram nestas consultas.
A política de passagem RC6 → núcleo2 permanece reservada ao Product Owner.
Histórico de componentes distingue CASH_IN/CASH_OUT/DISCOUNT; ausência de caixa
não fabrica movimento zero. Os DTOs de página/componentes copiam as listas.

## TUULI-FIN-TIME-001 — contrato histórico OP22-A, emendado pela OP22-B2.2

[ADR temporal](../../governance/decisions/ADR-002-financial-document-time.md).
**Regra-alvo emendada em 14-10-2026 pela OP22-B2.2:** RC no FinancialCore
liquida na Data Valor e reverte na Data Valor original (M1 + F2 + T1).
FIN-HIST-01–03 preservam movimentos e vínculos e orientam consumidores por efeito.
O texto abaixo conserva o estado à data OP22-A, não uma regra concorrente vigente.
Código permanece sem F2/T1; legado conserva emissão até corte coordenado autorizado.
Data Valor é a única excepção aprovada à proibição de datas de efeito autónomas.

Data de efeito financeiro documental = data de emissão do documento. Não há
segunda data editável. PositionView e OperationView de CREATE vinculado expõem
financialEffectDate derivada da emissão. recordedAt permanece timestamp técnico
imutável do núcleo, distinto de SystemEntryDate SAF-T; não é instante de commit.

**Não implementado:** saldo/movimentos a uma data financeira. O esquema permite
obter emissão FT inequivocamente mas não emissão RC para SETTLE, nem data de efeito
de REVERSE. Somar CREATE por emissão e SETTLE/REVERSE por recordedAt produziria
um histórico incoerente. Mesmo uma FT sem consumos hoje pode ter estorno cuja
semântica temporal esteja por decidir. Não expor subconjunto como saldo completo.
O histórico de afectações implementado é auditável, não uma consulta financeira
por data nem uma consulta de visibilidade por instante de commit.

## Persistência e decisões futuras

Nenhuma alteração estrutural, migration ou índice nesta OP. O esquema V19 basta
para consultas correntes; não basta para o histórico documental completo.
Paginação é limitada; EXPLAIN/cargas de produção e índices de acesso por contexto
ficam para validação proporcional à futura activação. Não presumir capacidade
ilimitada dos scans/agregações atuais.

Antes de alterar a persistência, apresentar ao PO alternativas para vínculos de
liquidações/estornos, por exemplo vínculo documental imutável dedicado versus
extensão da operação com FK documental tipada. O primeiro preserva tabelas de
operações existentes e permite cardinalidades explícitas, mas acrescenta joins e
guards; o segundo simplifica leitura, mas exige alteração da operação e definição
do tipo/cardinalidade documental. Ambos exigem integridade de contexto, emissão,
imutabilidade e definição legal/funcional do estorno; nenhum está escolhido ou
implementado. Uma data financeira autónoma editável é excluída pela decisão.
Não reutilizar financial_document_origin para RC como se fosse uma criação FT.

Não se alteraram emissão/anulação, frontend, API, fiscalidade, SAF-T, precisão,
Pendente, dados históricos, demo, Docker ou CI. NC/ND/NL, compensações documentais,
RCzero, descontos e adiantamentos conservam os gates OP19. Não certifica o produto.
