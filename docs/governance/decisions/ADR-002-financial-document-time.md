# ADR-002 — TUULI-FIN-TIME-001: efeito financeiro documental

12-10-2026 · **DECISÃO APROVADA PELO PRODUCT OWNER** na continuação OP22-A.
Complementa [ADR-001](ADR-001-financial-source.md) e GOV-ALT-001.

**Regra vigente: emenda OP22-B2.2 aprovada pelo Product Owner em 14-10-2026,
na secção final. Os blocos anteriores conservam o histórico, não regras
concorrentes. Aprovação arquitectural; implementação e activação não autorizadas.**

## Decisão original de 12-10-2026 — histórico, emendada abaixo

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

## Limites da decisão original — histórico

Timestamp de registo permanece independente, imutável e auditável, sem equivalência
com SystemEntryDate documental do SAF-T (PT) ou garantia de instante de commit.
A decisão não aprova retrodatação arbitrária e não dispensa regras legais de
emissão. Estornos/anulações exigem identificar semântica legal e funcional própria:
nem o registo técnico do estorno nem a emissão original definem automaticamente
a sua data de efeito. Essas decisões permanecem pendentes.

## Estado efectivo à data da OP22-A

V19 vincula posição FT a documento_comercial com emissão/vencimento existentes.
Consulta corrente deriva financialEffectDate dessa emissão sem coluna adicional.
SETTLE não possui vínculo RC e REVERSE não possui semântica documental temporal
aprovada. Saldo/movimentos a uma data financeira ficam suspensos. Nenhuma nova
estrutura ou data autónoma foi implementada; alterações estruturais requerem
comparação e aprovação prévia GOV-ALT-001. Não activar RC nem alterar anulações.
[Contratos e limitações](../../fiscal/op22-a/QUERY_CONTRACTS.md).

## Estado efectivo à data da OP22-B1 — implementação parcial, inactiva

V20 identifica RC/SETTLE imutavelmente; emissão RC já consultável como
financialEffectDate do SETTLE vinculado, sem data financeira autónoma. Origem FT
mantém regra anterior. REVERSE e saldo/movimentos a uma data financeira continuam
sem implementação documental temporal: a decisão sobre anulação ainda é necessária.
B1 não interpreta timestamp, data original ou data de registo como efeito do estorno.
[Contrato e testes](../../fiscal/op22-b1/RECEIPT_INTEGRATION.md).


## Emenda OP22-B2.2 — aprovada em 14-10-2026

Decisão expressa do Product Owner: DV-01 a DV-09, M1 + F2 + T1, anulação
autónoma e substituição vinculada. Fonte: ordem de formalização e retoma
aprovadas em 14-10-2026; proposta externa integral e parecer final consultados.
M1 conserva Data Valor no RC, sem entidade autónoma de recebimento nesta fase.
A Data Valor nunca substitui a Data de Emissão.

### 2. Requisitos funcionais consolidados

| Decisão | Contrato aprovado                                                                                                                                         |
| ------- | --------------------------------------------------------------------------------------------------------------------------------------------------------- |
| DV-01   | Data Valor própria do RC, distinta de `dataHoraOperacao`; preservar e clarificar a informação técnica existente                                           |
| DV-02   | Por defeito, Data Valor igual à emissão                                                                                                                   |
| DV-03   | Checkbox desmarcada; campo inicialmente igual à emissão quando apresentado; desmarcar repõe igualdade; checkbox apenas de interface                       |
| DV-04   | Para obrigações de débito elegíveis: máxima data de nascimento das obrigações ≤ Data Valor ≤ emissão RC; elegibilidade confirmada, adiantamentos excluídos                                           |
| DV-05   | Persistência backend e imutabilidade após emissão em todos os RC, incluindo legado                                                                        |
| DV-06   | Emissão sempre identificada; Data Valor consultável e apresentada no PDF; sequência documental preservada                                                 |
| DV-07   | Substituto com número e emissão próprios, Data Valor conservada quando verdadeira e rastreabilidade da substituição                                       |
| DV-08   | Históricos sem data efectiva conhecida não recebem Data Valor inventada                                                                                   |
| DV-09   | Substituto pode corrigir montante para mais ou menos, com motivo e auditoria, sem dupla liquidação, excesso de afectação ou dinheiro sem afectação válida |

Não fica criada uma permissão adicional nem uma obrigação geral de justificar toda a Data Valor diferente da emissão. Essas regras não foram aprovadas. As correcções abrangidas por DV-09 exigem motivo expresso.

### 3. Comparação das alternativas

#### Modelação

| Alternativa               | Benefícios                                                                        | Custos e riscos                                                                                | Recomendação                                  |
| ------------------------- | --------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------- | --------------------------------------------- |
| M1 — Data Valor no RC     | Menor intervenção; autoridade única; consulta pelo vínculo documental             | Exige invariantes completos de substituição; não representa dinheiro não afectado              | Aprovada para o âmbito actual              |
| M2 — Recebimento autónomo | Identidade do recebimento independente dos documentos; suporta futuras afectações | Maior complexidade, transição e novos contratos; não justificada apenas pelos cenários actuais | Reservada para âmbito futuro que a justifique |

M1 foi aprovada para o âmbito actual e é suficiente como contrato para recebimentos integralmente afectados e substituições com correcção fundamentada. Dinheiro excedente sem dívida válida não deve ser acomodado silenciosamente: o caso é recusado ou encaminhado para futura decisão sobre adiantamentos/créditos.

#### Efeito financeiro

| Alternativa            | Benefícios                                            | Custos e riscos                                                            |
| ---------------------- | ----------------------------------------------------- | -------------------------------------------------------------------------- |
| F1 — Emissão           | Conserva o contrato vigente                           | Não representa a orientação funcional para pagamentos anteriores à emissão |
| F2 — Data Valor        | Representa o pagamento efectivo na dívida rectificada | Exige emenda temporal, reversões coerentes e corte coordenado              |
| F3 — Efeitos separados | Modelo mais abrangente para dinheiro e afectações     | Introduz contratos adicionais e complexidade fora da primeira fatia        |

F2 foi aprovada pelo Product Owner em 14-10-2026. F1 permanece como comportamento operacional legado durante a transição. F3 fica diferida, sem aprovação nesta emenda.

#### Temporalidade da reversão

| Alternativa              | Benefícios                                                                                                  | Riscos                                                                                                                 |
| ------------------------ | ----------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------- |
| T1 — Data Valor original | Neutraliza o efeito original na história rectificada; substituto aplica o efeito correcto na sua Data Valor | Altera resultados de consultas passadas; não reproduz informação anteriormente conhecida                               |
| T2 — Data do acto        | Conserva uma cronologia de reversão posterior                                                               | Com substituto retroactivo, pode duplicar a liquidação no intervalo anterior à reversão; exige outro contrato temporal |

Exemplo: original de 300 € com Data Valor 01/10, anulado em 14/10 e substituído pelo mesmo valor e Data Valor.

- T1: `−300 +300 −300 = −300` na dívida desde 01/10.
- T2: antes de 14/10, original e substituto podem produzir `−600`; a reversão só actua em 14/10.

T1 foi aprovada pelo Product Owner em 14-10-2026 no contexto F2. T2 exigiria regras adicionais para evitar essa dupla incidência.

### 4. Emenda expressa aprovada a TUULI-FIN-TIME-001

**Cláusulas aprovadas pelo Product Owner em 14-10-2026. A vigência institucional
não modifica automaticamente código, saldos ou consumidores operacionais.**

#### 4.1. Substituição das disposições incompatíveis

Substituem-se, no ADR-002, as disposições:

- Efeito financeiro obrigatoriamente igual à emissão para todos os documentos.
- Liquidação RC obrigatoriamente na emissão.
- Proibição absoluta de uma data de efeito diferente da emissão.

Pelo seguinte contrato:

> A originação financeira por FT e NC conserva como referência a emissão do documento originador. As demais regras não afectadas por esta emenda permanecem vigentes.
>
> No contrato-alvo FinancialCore, a liquidação originada por RC com Data Valor persistida produz efeito nessa Data Valor.
>
> A Data Valor do RC constitui a excepção expressamente prevista à igualdade entre efeito financeiro e emissão documental. Não é uma data de emissão nem uma data financeira adicional livremente editável depois da emissão.
>
> Para documentos de débito efectivamente elegíveis, respeita:
>
> `máxima data de nascimento das obrigações liquidadas ≤ Data Valor ≤ emissão do RC`
>
> Para FT, a data de efeito da obrigação é a emissão. Tipos não suportados ou
> com natureza/temporalidade indeterminada não recebem elegibilidade presumida.
> Mantém-se a proibição de qualquer outra data de efeito financeiro autónoma ou editável; a Data Valor do RC é a única excepção.
>
> Nem `dataHoraOperacao` nem o timestamp técnico substituem a Data Valor ou determinam automaticamente o efeito financeiro.
>
> A emissão continua a governar a identidade, numeração e sequência documental.
>
> No contrato-alvo aprovado, a reversão integral de SETTLE vinculado a RC produz efeito na Data Valor original, obtida pelo vínculo à operação e ao RC revertidos. O acto documental de anulação conserva a sua própria data de realização e auditoria.
>
> Os históricos sem Data Valor registada utilizam a emissão exclusivamente como referência de compatibilidade dos cálculos existentes. A apresentação identifica a Data Valor como «não registada».

A reserva anterior relativa à temporalidade de anulações de RC é substituída por T1 no contrato-alvo aprovado; a implementação permanece pendente. Outras anulações, estornos e tipos documentais conservam as suas reservas próprias.

#### 4.2. Âmbito documental

A orientação FIN-HIST-02 complementa DV-04: o limite inferior é a máxima
data de nascimento das obrigações de débito efectivamente elegíveis liquidadas.
Para FT, a originação financeira existente usa emissão; a fonte de nascimento
deve ser demonstrada na implementação, não inferida do nome do campo.
A Data Valor não é forçada à emissão em
recibos não-FT ou mistos; a proposta DV-10 foi retirada pelo Product Owner.

O legado cria Pendente para documento emitido/numerado, de total positivo,
sem liquidação imediata; o emissor valida cliente, moeda e saldo positivo,
sem filtrar o código FT. Isso é aceitação técnica, não prova da natureza devedora
ou de elegibilidade fiscal de cada tipo configurável. O adaptador FinancialFtOrigin
origina apenas FT EUR na fronteira actual; RC novo exige origem vinculada válida.
ND e demais tipos não têm integração documental demonstrada nesta fronteira.
NC não se transforma em dívida apenas porque o legado admite um pendente positivo.
Tipos sem efeito de obrigação inequívoco requerem decisão/validação própria.
Não restringir operacionalmente RC legados elegíveis por esta formalização;
adiantamentos, habilitação de novos tipos e migração não ficam autorizados.
Ver [inventário e cenários](../../fiscal/op22-b2-2/FINANCIAL_HISTORY.md).

### 5. Persistência, limites e imutabilidade

Requisitos aprovados para futura implementação, que exige missão própria:

- Data Valor autoritativa no RC; o núcleo obtém-na pelo vínculo, sem cópia independente editável.
- Novos RC no âmbito autorizado persistem uma Data Valor efectiva.
- Históricos sem conhecimento da data mantêm ausência persistida, sem backfill fictício.
- Backend e base de dados protegem os limites aplicáveis.
- Limite inferior considera todas as obrigações de débito elegíveis efectivamente liquidadas, pelas respectivas datas de nascimento; validar o conjunto final de linhas e a elegibilidade, sem presumir tipos.
- Data Valor não pode ser alterada após emissão, incluindo SQL directo e RC legados.
- Sequência de criação deve permitir montar documento e linhas sem bloquear indevidamente operações ainda incompletas.
- Payload idempotente inclui Data Valor normalizada; omissão e igualdade explícita com emissão são equivalentes.
- Evolução por migration aditiva, sem editar V18/V20/V21/V22 integradas.

A forma concreta de distinguir históricos de novas emissões e de representar o acto de anulação será detalhada e revista antes da implementação. A identificação, declaração/registo e validação da situação monetária real antes da anulação autónoma são requisitos de implementação pendentes: o sistema não dispõe hoje de prova suficiente dessa situação. Não autorizar reembolso automático ou saldo não afectado.

### 6. Anulação e substituição

#### 6.1. Vínculos e invariantes

A futura persistência deve garantir:

- Acto de anulação ligado imutavelmente ao RC original.
- REVERSE ligado ao SETTLE original e ao acto.
- Na substituição, RC substituto ligado ao original, ao acto e ao seu novo SETTLE.
- Um RC original não pode ter duas anulações efectivas nem duas substituições directas efectivas.
- Cada afectação original tem exactamente a reposição correspondente na reversão integral.
- Outras afectações independentes permanecem preservadas.
- Montantes positivos, precisão aprovada e residual dentro dos limites.
- Nenhuma extensão tardia incompatível do SETTLE vinculado.
- Exclusão bidireccional legado/FinancialCore preservada.

#### 6.2. Atomicidade e concorrência

Anulação autónoma: acto, estado documental, REVERSE, reposição e auditoria confirmam ou revertem conjuntamente.

Substituição conjunta: acrescenta a emissão do substituto, numeração, novo SETTLE e vínculos à mesma unidade transaccional.

A operação deve:

- Validar autorização e contexto.
- Serializar pedidos e recursos comuns em ordem determinística.
- Verificar idempotência antes de reservar numeração.
- Impedir duplicação mesmo com chaves diferentes para o mesmo original.
- Devolver o resultado persistido em retry equivalente.
- Rejeitar reutilização da chave com conteúdo diferente.
- Garantir rollback integral perante violação.

Não se repõem snapshots antigos de saldo por cima de movimentos posteriores.

#### 6.3. Montantes e Data Valor

Por defeito, o substituto conserva montante e Data Valor.

DV-09 permite corrigir ambos, respeitando DV-04, com motivo expresso e auditoria. O substituto não pode ultrapassar as dívidas disponíveis após a reversão do original e preservação das restantes afectações.

Pagamentos reais em momentos distintos são identificados como potencialmente fora da substituição simples. Não são condensados artificialmente numa Data Valor.

#### 6.4. Invariante monetário e exemplo DV-09

Se o original regista `O` e o substituto regista `S`, o resultado rectificado deve ser:

`O − O + S = S`

Para original de **30 €**, pagamento real de **300 €** e substituto de **300 €**:

- Original: `CASH_IN 30`.
- Contra-lançamento: `CASH_OUT 30`, com `reversed=true` e vínculo ao `CASH_IN` original; neutraliza o lançamento original e não representa saída efectiva de dinheiro.
- Substituto: `CASH_IN 300`.
- Resultado líquido rectificado: **300 €, nunca 330 €**.

Se a FT era de 1.000 €, sem outras afectações, o residual final será **700 €**.

A reversão monetária integral conserva o montante e o vínculo de cada componente original; as afectações correspondentes são repostas por `RESTORE` ligado às afectações originais.

O contra-lançamento é identificado como reversão, não como reembolso efectivo. O original continua no histórico; não se exige ausência de várias entradas brutas, mas ausência de duplicação líquida e interpretação monetária inequívoca.

Dinheiro recebido que não possa ser integralmente afectado validamente no âmbito aprovado impede a substituição simples. Não se cria saldo não afectado, adiantamento ou crédito implícito.

Anulação autónoma não exige substituto. Porém, não pode ser usada para ocultar dinheiro real que ficaria sem tratamento válido no âmbito actual; esses casos exigem resolução própria antes da operação.

### 7. História financeira

T1 define história rectificada, não reprodução histórica.

Exemplo: FT de 1.000 €, RC de 300 € com Data Valor 01/10:

- Após registo, consulta rectificada a 10/10: 700 €.
- Após anulação autónoma com T1: a mesma consulta rectificada pode apresentar 1.000 €.
- Após substituição por 300 € com Data Valor 03/10: dívida aberta até 02/10 e residual de 700 € desde 03/10.

Informação conhecida num instante e reprodução exacta de extractos permanecem capacidades distintas, não aprovadas por esta emenda.

### 8. Estado actual e transição

**Actual:** legado operacional por emissão; FinancialCore interno, RC novo inactivo; anulação de RC vinculado bloqueada; `balanceAt()` suspenso.

**Alvo aprovado, não implementado:** M1 + F2 + T1, substituição vinculada e anulação coerente.

A aprovação institucional não muda o comportamento existente. Os extractos e consumidores legados conservam os contratos actuais até ao corte autorizado.

Não activar parcialmente emissão, anulação, leitura ou consumidores. Preparação antecipada permanece desligada da operação. O corte exige consistência de dados, testes, revisão, verificação da demo e autorização própria.

### 9. Fiscalidade e aprovação

Permanecem pendentes:

- SAF-T Payments e representação das datas.
- Emissão tardia.
- IVA de caixa.
- Representação documental e eventual relevância para QR/comunicação.
- Formalismos de anulação e substituição.

Nenhuma conclusão fiscal é declarada.

Formalização autorizada pelo Product Owner em 14-10-2026. Proposta externa revista por agente Codex independente: APPROVE WITH CONDITIONS, BLOCKER 0 / MAJOR 0 / MINOR 2; os dois esclarecimentos estão incorporados nesta emenda. Não atribuir este parecer ao Claude. A entrega canónica permanece sujeita a revisão independente e integração documental humana. Implementação exige missão própria.

A formalização desta emenda pode preceder a validação fiscal enquanto contrato interno inactivo. A activação operacional depende de validação fiscal expressa dos pontos aplicáveis acima, registada com responsável e evidência, e de autorização própria do Product Owner.

### FIN-HIST-01 a FIN-HIST-03 — orientação aprovada do Product Owner

FIN-HIST-01: obrigações, liquidações, reversões e correcções conservam identidade,
vínculos, datas e auditoria; rectificação nunca elimina movimentos originais.
FIN-HIST-02: liquidação na Data Valor, entre nascimento das obrigações de débito
elegíveis e emissão RC; confirmar tipos suportados sem inferência fiscal.
FIN-HIST-03: extractos, conta corrente e consumidores financeiros usam no alvo
as datas de efeito para movimentos/saldos e conservam emissão e vínculos.
Apresentar ambas as datas quando diferentes. Não mudar consumidores actuais
nem activar cálculos isolados; corte conjunto permanece não autorizado.

### Rastreabilidade das revisões e do complemento

- Claude, proposta original, 14-10-2026: REQUEST CHANGES, BLOCKER 0,
  MAJOR 3 / MINOR 5; correcções incorporadas na proposta revista.
- Revisor Codex separado, proposta revista: APPROVE WITH CONDITIONS,
  BLOCKER 0 / MAJOR 0 / MINOR 2; gate fiscal e contra-lançamento incorporados.
- Claude, commit canónico 486b999: APPROVE WITH CONDITIONS,
  BLOCKER 0 / MAJOR 0 / MINOR 5. Parecer completo consultado:
  TUULI_OP22-B2.2_Claude_revisao_commit_486b999_2026-10-14.txt.
- Complemento autorizado pelo PO: cinco MINOR tratados por remissão OP22-A,
  proibição residual, FIN-HIST-02 sem DV-10, situação monetária por implementar
  e este registo. [Matriz de resposta](../../fiscal/op22-b2-2/FINANCIAL_HISTORY.md).

A entrega complementar exige nova revisão Claude antes da publicação;
nenhum parecer anterior é aprovação desta versão nem validação jurídica.

### Decisão final PO — nascimento da obrigação (MINOR-N1)

Aprovado pelo Product Owner no fecho N1/N2: a Data Valor de uma liquidação
não pode ser anterior à data de nascimento da obrigação liquidada, nem
posterior à emissão do respectivo recibo. Para várias obrigações, o limite
inferior é a data mais recente de nascimento. Aplica-se independentemente
do tipo documental elegível, sem restrição artificial aos RC não-FT/mistos
nem presunção de elegibilidade fiscal. Esta precisão prevalece sobre referências
anteriores a «efeito da obrigação» como limite inferior.

Forma técnica ainda não implementada: PendenteService.java:143 copia emissão
para Pendente.dataDocumento; isso prova origem do campo, não nascimento para
todos os tipos. FinancialFtOrigin limita origem documental a FT EUR, e a decisão
FT de originação na emissão suporta esse caso. O legado admite outros tipos
sem filtro de natureza devedora; não há prova universal de equivalência.
Pendência técnica BLOQUEANTE da implementação da validação universal: confirmar
por tipo elegível a data de nascimento e a fonte persistida inequívoca; não usar
silenciosamente dataDocumento como substituto. Não bloqueia este fecho documental.
