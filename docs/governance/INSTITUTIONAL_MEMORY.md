# TUULI AIR — Memória Institucional

Fonte canónica das decisões duradouras do projeto. O estado corrente encontra-se em [OPERATIONAL_STATE.md](OPERATIONAL_STATE.md); o histórico detalhado permanece no Git. Decisões novas exigem fundamento e autorização dentro do âmbito aplicável.

## Identidade e propósito

TUULI AIR é a aplicação de faturação do universo TUULI, desenvolvida no repositório historicamente denominado FAC. Destina-se a micro e pequenas empresas, profissionais independentes e organizações que necessitam de emitir documentos comerciais e fiscais, acompanhar recebimentos e consultar informação operacional. Deve resolver estas necessidades sem se transformar num ERP generalista.

A identidade TUULI estende-se à interface. AR2 é a referência da metodologia de desenvolvimento e governação adotada; não se inferem outras relações organizacionais ou integrações técnicas.

## Arquitetura e linha produtiva

- Backend: Java 21, Spring Boot 3, Spring Security e Spring Data JPA, organizado em Controller → Service → Repository.
- Persistência: PostgreSQL e Flyway. Hibernate valida o esquema; as migrations governam a sua evolução.
- Frontend: React, TypeScript e Vite, com primitivas e tokens partilhados. Preservar Visual V2 e terminologia pt-PT.
- Reporting: preparação de dados separada da lógica de negócio; PDFs e exportações devem preservar contratos, filtros, ordem, totais e conteúdo funcional.
- A linha produtiva vigente é `feature/visual-redesign`. Selecionar a base a partir da referência remota atualizada enquanto esta decisão vigorar; não assumir `main` nem uma branch local desatualizada.

## Regras funcionais e fiscais consolidadas

- Privilegiar simplicidade, crescimento controlado e integridade dos dados. Identificadores funcionais são imutáveis após criação.
- Preservar os fluxos de clientes, artigos/serviços, documentos, recebimentos, pendentes e reporting sem alterações laterais não autorizadas.
- Na emissão, conservar os snapshots fiscais históricos e a rastreabilidade documental. Dados legados não recebem valores fiscais inventados.
- Para documentos fiscalmente consolidados, a impressão utiliza os valores persistidos de identificação, ATCUD, QR e totais, em vez de os reconstruir a partir de mestres atuais.
- Antes de migrar uma base real existente, efetuar backup e ensaiar numa cópia. Preservar as proteções contra uso destrutivo de bases persistentes nos testes.
- Certificação AT é um objetivo do projeto. Estas decisões não declaram o produto certificado, fiscalmente encerrado ou pronto para certificação.

## Segurança e invalidação JWT

Utilizar a identidade existente `Utilizador`, passwords com BCrypt e autenticação Bearer JWT através do Resource Server do Spring Security. Preservar a distinção entre 401 (autenticação) e 403 (autorização), as autorizações vigentes e a auditoria existente.

A validade criptográfica e temporal de um JWT não basta para manter uma sessão válida: a autenticação considera também o utilizador existente, o seu estado ativo e o estado de segurança persistente `token_version`.

- A emissão inclui a claim `token_version`. A validação aceita apenas valores desserializados como `Integer` ou `Long` e exige igualdade com a versão persistida; claims ausentes, textuais, fracionárias ou divergentes são rejeitadas.
- O reset administrativo de password incrementa a versão e invalida os tokens anteriores. A decisão consolidada é invalidar tokens perante alteração/reset de password; não foi criado um fluxo de alteração pelo próprio utilizador.
- A desativação incrementa a versão. A reativação não ressuscita tokens anteriores.
- Qualquer alteração efetiva do perfil invalida as sessões JWT anteriormente emitidas para esse utilizador, incrementando a mesma versão persistida. Um pedido que conserva o perfil não invalida sessões. O novo login recebe as capacidades do novo perfil.
- A invalidação é aplicada centralmente no backend a pedidos autenticados pelo Resource Server, preservando a validação criptográfica e temporal existente.
- A versão é persistida em PostgreSQL, não exclusivamente em memória volátil. Um restart não repõe uma versão anterior; a aceitação do token continua dependente também da assinatura e expiração.
- `row_version`, mapeado com `@Version`, protege contra gravações desatualizadas que poderiam restaurar uma versão de sessão, password ou estado anteriores.

O mecanismo não introduz armazenamento individual de JWT, refresh tokens, novas funcionalidades de logout server-side ou um novo modelo de permissões. O logout client-side e o tratamento central de 401 existente são preservados. A missão JWT não encerrou, isoladamente, o capítulo global de Segurança; o fecho formal posterior encontra-se registado abaixo.

## Fundação de Permissões

Preservar o modelo existente: perfil → capacidades funcionais → authorities JWT → autorização no backend. Existem três perfis (ADMINISTRADOR, OPERADOR, CONSULTA) e 16 capacidades em PermissaoFuncional. Não introduzir hierarquias adicionais, novas capacidades, ACLs ou infraestrutura de identidade sem decisão própria.

- O administrador recebe deliberadamente todas as capacidades do enum, incluindo futuras capacidades; uma nova capacidade deve ter o impacto explicitamente avaliado. Operador conserva dez capacidades e Consulta quatro, segundo o mapa exato de PapelUtilizador.
- O backend aplica o contrato tipado comum nos filtros, nas anotações e nas verificações de serviços. A anulação comercial conserva a autorização no serviço para preservar a auditoria de recusa.
- O frontend usa um helper comum e um catálogo tipado, verificado contra o enum backend. Estes controlos pertencem à apresentação; a autorização efetiva continua no backend.
- O PDF financeiro exige explicitamente DOCUMENTO_OBTER_PDF, tal como o comercial; os três perfis atuais conservam o acesso.
- Consultas financeiras, pendentes, dashboard, listagens e extratos conservam os seus requisitos atuais de autenticação. Não criar gates novos por inferência.
- As regras de último administrador e de autoelevação continuam a ser regras sobre perfis. row_version conserva a proteção contra gravações desatualizadas, incluindo mudanças de perfil.

## Segurança operacional e exposição pública

TUULI AIR suporta operação pública na Internet. Produção pública exige HTTPS;
TLS termina na infraestrutura. Redireccionamento, certificados/renovação, HSTS
no edge, firewall e gestão de secrets pertencem ao deployment. HTTP interno
só é adequado dentro da arquitectura protegida; o produto não exige VPN.

- A aplicação possui protecção mínima própria contra abuso do login, por instância,
  com limites configuráveis, estado limitado, recuperação automática e resposta 429.
  Não depende exclusivamente do proxy nem introduz Redis/serviço externo.
- Bypass de segurança é proibido em operação. Segurança desactivada é confinada ao
  contexto de teste: perfil exclusivo, marcador e infraestrutura de testes ausentes do artefacto operacional.
- Secrets operacionais JWT são obrigatórios e validados no arranque. Geração
  aleatória segura, continuidade entre instâncias/reinícios e rotação são deveres
  operacionais. SHA-256 não demonstra entropia. HS256 é preservado.
- Contrato JWT exige subject, expiração, issuer e tipos de claims de autorização,
  antes de validar a identidade e versão persistida. Não se acrescenta audience.
- Parsing de importações e imagens tem limites anteriores à materialização pesada;
  headers/CSP mitigam a superfície browser sem mudar a arquitectura de sessão.
- JWT em localStorage, logout local, ausência de refresh tokens/sessões individuais
  e auditoria selectiva de recusas são riscos residuais aceites para esta versão.
  Não equivalem a garantia de ausência de XSS ou revogação ao fazer logout.

O [contrato de operação do Security Gate](../security/SECURITY_GATE.md) detalha
limites e responsabilidades. Estas decisões estão aprovadas, implementadas e
integradas. O capítulo Segurança está formalmente encerrado por decisão do Executivo,
confirmada na OP Fundação Fiscal RIVA. Manutenção futura de dependências npm
é não bloqueante e não reabre o Security Gate. Os resíduos aceites e deveres
de deployment mantêm-se. Este fecho não antecipa o encerramento fiscal.

## Fundação fiscal RIVA e política monetária

- Categoria própria do artigo, taxa aplicável, enquadramento da operação e tratamento de liquidação são conceitos distintos. Não liquidação não converte uma taxa própria de 6% em 0%.
- Preservar IVA calculado e IVA liquidado separadamente. O total da linha usa o liquidado; os totais documentais são somas das linhas fiscalmente fechadas.
- Quantidade/preço conservam até seis casas. Bruto, desconto percentual ou absoluto, base e imposto calculado fecham por linha a duas casas, HALF_UP, na sequência aprovada. Não recalcular IVA agregado sobre bases de maior precisão.
- Backend é autoridade fiscal. Preview e gravação usam o mesmo motor; a interface não infere zero perante configuração ausente.
- Alteração de RIVA reavalia todas as linhas comerciais do rascunho. Rascunhos anteriores passam pelo novo motor na abertura/edição relevante; diferenças exigem aviso e confirmação antes da emissão.
- Emitidos nunca são recalculados. Snapshot V3 conserva categoria, taxa, bruto/desconto/base, calculado/liquidado, total e enquadramento/fundamento. V1/V2 continuam legíveis, sem backfill de verdade fiscal não demonstrada.
- Portugal é a primeira jurisdição; território fiscal (Continente/Açores/Madeira), mercado, moeda e país do cliente permanecem independentes. A arquitetura multi-moeda é preservada; emissão não EUR aguarda conversão fiscal normativa, fora desta OP.
- MIsencao usa referência oficial AT, preservando códigos/designações legacy. Disponibilidade no catálogo não habilita automaticamente uma combinação: fundamentos e projeções não demonstrados são rejeitados.
- Taxas territoriais são referência atual com estrutura de vigência extensível, sem reconstrução legislativa histórica. Matrizes continentais existentes conservam valores explícitos; zero não representa não liquidação.
- PDF/QR de emitidos consomem snapshots persistidos. Modelo interno rico e projeção AT são separados; esta fundação não declara conformidade SAF-T, assinatura ou certificação AT.

## Contrato comercial e financeiro aprovado — OP19

As decisões funcionais da OP19 estão aprovadas; a arquitetura é proposta e a
implementação nova depende de outra OP. [Contrato e dossier](../fiscal/op19/README.md).
FT/ND podem gerar dívida; NC gera crédito autónomo, com a fatura aberta ou paga.
Saldo líquido zero não liquida posições. Compensação total/parcial é expressa,
sem encontro automático, e pode não movimentar dinheiro. Desconto financeiro
no RC distingue dívida extinta e caixa, sem NC automática. NC/ND admitem modo
assistido por uma/várias faturas e livre fundamentado, sem obrigação universal
de selecionar manualmente cada linha; preservar suporte e fiscalidade histórica.
Créditos NC e adiantamentos têm naturezas distintas. Estornos respeitam
as dependências efetivas e preservam originais/auditoria. Classificação NL,
RCzero, adiantamentos e tratamento fiscal dos descontos exigem validação
específica antes da ativação; estas decisões não declaram conformidade AT.

## Fundação financeira isolada — OP20

A OP20 autoriza uma fundação interna de posições, afectações, caixa separado,
idempotência e estornos, sem ativação nos circuitos documentais atuais. Reutilizar
o motor monetário RIVA e preservar dados emitidos. Integração, migração histórica,
novos endpoints e decisões fiscais exigem OP própria; núcleo não é certificação.
[Contrato técnico](../fiscal/op20/FINANCIAL_CORE.md).

## Originação FT — OP21

À data da OP21, a integração aprovada limitava-se à originação de dívida para
novas FT EUR e ao estorno dessa criação quando a anulação comercial existente
permitia. Vínculo e operações eram históricos e atómicos com documento/Pendente.
Naquele estado, Pendente e conta corrente mantinham autoridade operacional e o
RC legado não escrevia no núcleo. Uma posição não afectada após RC legado era
uma diferença deliberada, sem sincronização silenciosa nem apresentação como
saldo corrente. Originação FT integrada pelo PR #20; a afirmação de liquidação
RC ainda não integrada refere-se exclusivamente àquele momento da OP21.

Evolução posterior: PR #23 integrou a infraestrutura interna RC, mantendo-a
inactiva; PR #24 integrou a protecção bidireccional. A liquidação RC operacional
no FinancialCore ainda não foi activada. O circuito legado continua a não escrever
no núcleo; estas capacidades posteriores não são atribuídas à OP21.
[Limites e rastreabilidade](../fiscal/op21/FT_ORIGINATION.md). Integração NC/ND,
RC/NL e migração histórica exigem autorização própria; não declara certificação.

## GOV-ALT-001 — Transparência e avaliação de alternativas

Sempre que uma decisão técnica, funcional, arquitectónica, operacional ou
estratégica admita alternativas materialmente distintas:

1. Identificar expressamente a existência de alternativas.
2. Apresentar as alternativas viáveis, com benefícios, custos, riscos, limitações e consequências futuras.
3. Explicitar os pressupostos de cada alternativa, distinguindo factos confirmados de hipóteses.
4. Apresentar uma recomendação fundamentada, separada da decisão.
5. Submeter a decisão ao Product Owner antes de emitir ordens de implementação.
6. Não interpretar uma recomendação técnica como aprovação.
7. Registar decisões duradouras e respectivos fundamentos nas fontes institucionais.

A profundidade da comparação deve ser proporcional ao impacto e à reversibilidade.
**O Executivo Operacional recomenda; o Product Owner decide.** Esta regra aplica-se
ao TUULI. Incorporação no TaxIA exige missão separada no respetivo repositório;
não foi executada a partir do TUULI.

## Arquitetura financeira — alternativa B aprovada

Por decisão expressa do Product Owner na OP22-GOV, foi aprovada a substituição
progressiva do legado. TUULI AIR está em desenvolvimento/demo; os dados atuais
são descartáveis e não existem históricos financeiros de produção a preservar.
Não desenvolver compatibilidade histórica apenas para conservar essa demo.
Isto não autoriza migração/eliminação efetiva de dados nem enfraquece a preservação
fiscal e auditável dos documentos emitidos pelo produto.

Destino: FinancialCore como única fonte de verdade financeira; Pendente deixará
de manter saldo independente e poderá subsistir como interface/projeção derivada.
Primeira fronteira a preparar: FT/RC, com escrita, leitura, anulação e consistência.
Não ativar fronteiras incompletas como circuitos estabilizados. A orientação é
futura: os saldos operacionais atuais ainda são legados. [ADR-001](decisions/ADR-001-financial-source.md)
conserva alternativas, fundamentos, consequências, riscos e decisões reservadas.
A aprovação B não aprova regras fiscais, precisão de recebimentos, evolução de
estornos, NC/ND/NL, descontos, adiantamentos, compensações/RC sem caixa, migração
ou eliminação de dados, SAF-T ou conformidade/certificação AT; requerem autorização própria.

## TUULI-FIN-TIME-001 — Emenda Data Valor OP22-B2.2

Decisão original OP22-A de 12-10-2026 preservada historicamente no
[ADR-002](decisions/ADR-002-financial-document-time.md). Em 14-10-2026, o
Product Owner aprovou expressamente DV-01 a DV-09 e M1 + F2 + T1.
A regra geral de igualdade obrigatória com emissão e a regra RC na emissão
são substituídas, no contrato-alvo FinancialCore, pela liquidação na Data Valor.
FT/NC conservam originação pela emissão; outras regras não afectadas mantêm-se.
Data Valor é a excepção aprovada, persistida no RC sem duplicação de autoridade.
Não altera emissão, identidade, numeração ou sequência documental.
FIN-HIST-02 complementa DV-04: máxima data de nascimento das obrigações de débito elegíveis
≤ Data Valor ≤ emissão RC; para FT, efeito da obrigação = emissão.
Legado admite pendentes não-FT sem filtro de código; isso não prova natureza
devedora/fiscal. Núcleo origina FT EUR; outros tipos com semântica indeterminada
exigem decisão. DV-10 retirada: não forçar igualdade em RC não-FT/mistos,
nem restringir emissão legada elegível. Adiantamentos permanecem excluídos.
FIN-HIST-01 preserva historial; FIN-HIST-03 orienta consumidores por efeito,
com datas documentais/auditoria conservadas, sem activação parcial.
Nem dataHoraOperacao nem timestamp técnico determinam automaticamente o efeito;
auditoria permanece independente e distinta de SystemEntryDate SAF-T.

T1 aprovada: reversão integral na Data Valor original; acto de anulação conserva
sua data/auditoria. Substituição vinculada e atómica permite correcção justificada
de montante/Data Valor (DV-09), preservando afectações independentes e dívida
válida. Anulação autónoma não exige substituto, mas não pode ocultar dinheiro
real sem tratamento válido. Identificação e validação da situação monetária real
são requisitos de implementação pendentes, não informação já demonstrada pelo
sistema. Contra-lançamento não é reembolso efectivo.
Históricos conservam Data Valor ausente; emissão só para cálculo compatível.

M2 diferida: complexidade/identidade autónoma não necessárias ao âmbito sem
excedentes não afectados. F1 preservada no legado durante transição; F3 diferida.
T2 não escolhida: substituto retroactivo pode duplicar efeito antes do acto.
Alternativas, fundamentos, invariantes e exemplo 30→300 constam do ADR-002.
À data da formalização, o núcleo continuava com datas de emissão, REVERSE RC
bloqueado e balanceAt suspenso. Evolução interna posterior: Fatia 1b abaixo;
legado e extractos operacionais mantidos.
Corte coordenado e validação fiscal expressa requerem autorização própria.
Nenhuma activação, migration, dados ou demo autorizados por esta formalização.

## OP22-B1 — preparação FT/RC aprovada, ainda inactiva

PO aprovou API existente como projecção do FinancialCore, EUR e cêntimos exactos,
sem descontos financeiros na primeira activação, e preparação de corte limpo da
demo sem eliminação/migração de dados. Esta decisão delimita a precisão da fatia
nova; não altera RC legado nem aprova outras moedas/descontos/adiantamentos.
B1 está integrada pelo PR #23, sem activação operacional. Implementa emissor
interno idempotente e vínculos aditivos V20, com V21 a
selar componentes/afectações após o vínculo RC; projecções
isoladas e diagnóstico de dados, sem chamadas nos endpoints operacionais.
Timestamp técnico separado; efeito de SETTLE vinculado deriva da emissão RC.

À data B1, anulação nova/histórico suspensos por semântica temporal pendente;
ninguém repõe snapshots antigos para desfazer afectações independentes. Nenhuma
activação antes de emissão/leitura/anulação/dados coerentes. B2 tem de completar
consumidores e decisões temporais; Pendente permanece autoridade operacional
apenas enquanto o circuito legado estiver activo. Na futura fronteira nova,
Pendente poderá manter identidade/metadados, mas não saldo financeiro independente.
[Contrato e limites](../fiscal/op22-b1/RECEIPT_INTEGRATION.md).

## OP22-B2.1 — defesa bidireccional integrada

PO autorizou protecção transaccional e persistente contra liquidação da mesma
dívida pelos circuitos legado e núcleo. A origem FT isolada não atribui exclusividade;
considerar linhas legadas activas e consumo financeiro efectivo não estornado.
Identidade Pendente pode serializar escritores sem fornecer o saldo ao núcleo.
OP22-B2.1 integrada pelo PR #24 em feature/visual-redesign; aprovação humana de
merge concluída. Segunda revisão Claude sem findings e auditoria Flyway favorável
comunicadas pelo Executivo. A entrega não activa RC, não transfere consumidores,
não altera precisão ou regras de anulação/efeito temporal nem reconcilia dados.
[Contrato](../fiscal/op22-b2-1/CIRCUIT_PROTECTION.md). Evidência PostgreSQL local e
CI limitado são distintos; não constituem certificação fiscal ou validação da demo.

Demo Ubuntu permanece por verificar: antes de qualquer actualização, confirmar
estado real e histórico/checksums Flyway. A aprovação de publicação não autoriza
deployment. Novo circuito RC permanece inactivo até completar a fronteira FT/RC.
Anulação de RC vinculado, efeito temporal de estornos, consultas históricas,
consumidores, chave idempotente pública e corte exigem ordens/decisões próprias.
GOV-ALT-001 mantém a apresentação de alternativas antes de decisões materiais.

## Metodologia e governação

Aplicar o AR2 AI Development System através de uma Ordem de Produção aprovada: descoberta focada → implementação dentro do âmbito → testes proporcionais → revisão independente quando prevista → correções legítimas → diff e staging seletivo → commit → push da branch da missão → PR contra a base vigente → CI → gate humano.

O Executor atua autonomamente dentro da OP aprovada, incluindo escolhas locais coerentes, testes, correções de BLOCKER/MAJOR e falhas próprias de CI. Não enfraquece controlos para obter verde, não faz force-push, não publica diretamente nas branches base e não amplia materialmente o âmbito.

Escalar decisões materiais de produto, arquitetura, identidade ou segurança não cobertas; infraestrutura nova; risco para dados persistentes, demo ou produção; conflito relevante de base; necessidade de force-push; e blockers técnicos ou externos reais. Operações normais já autorizadas não exigem nova aprovação.

O merge permanece sempre reservado à decisão humana de António. PR tecnicamente pronto e CI verde não constituem autorização de merge.

## Continuidade documental

- Refletir decisões duradouras nesta memória institucional.
- Manter o estado operacional atualizado, substituindo informação obsoleta em vez de criar um diário infinito.
- Cada sucessão de Executivo ou Executor parte destas duas fontes e de um handoff explícito. Conversas anteriores são contexto suplementar, nunca a única fonte de continuidade.
- `AGENTS.md` aponta para as fontes canónicas e não duplica o seu conteúdo.

## Referências de fundamento

- [Princípios basilares](../01%20-%20FAC-PRINCIPIOS-BASILARES.md).
- [Modelo fiscal histórico e baseline Flyway](../12%20-%20Modelo%20fiscal%20historico%20e%20baseline%20Flyway.md).
- [Direção Visual V2](../ui/TUULI-VISUAL-V2-DIRECTION.md).
- Ordens de Produção e instrução de institucionalização aprovadas pelo Executivo nesta missão; implementação JWT integrada e respetivos testes no Git.

## OP22-B2.2 — precisão final nascimento da obrigação

PO aprovou no fecho MINOR-N1/N2: Data Valor entre a máxima data de nascimento
das obrigações elegíveis liquidadas e a emissão do RC. Regra independente do
tipo, sem DV-10 nem elegibilidade fiscal presumida. Pendente.dataDocumento copia
emissão; à data N1/N2, equivalência universal com nascimento não demonstrada.
A pendência semântica então bloqueante foi posteriormente resolvida por E1/E2,
registadas abaixo; não foi resolvida por inferência sobre o nome do campo.
Inventário inclui estado comercial/anulação, frontend RC e demo. Só documentação;
circuito novo inactivo, legado preservado, nova revisão Claude complementar pendente.

## Integração documental OP22-B2.2 — PR #26

Confirmada em 15-10-2026 na referência local actualizada manualmente pelo PO:
merge fc5717763e95c40a8b1361b60984254d401f2416, contendo 486b999, 31eee0e,
cb872f3. Arquitectura e contratos integrados; implementação Data Valor não iniciada.
Uniformização ADR distingue originação FT pela emissão da confirmação da data
de nascimento da obrigação. Não cria regra funcional nem elimina a pendência.
[Plano técnico](../fiscal/op22-b2-2/SLICE1_PLAN.md) separa Fatia 1, evolução
financeira e corte autorizado. Não altera a autoridade operacional legada.

## Data Valor — decisões E1/E2 e Fatia 1a

15-10-2026: PO confirmou nascimento financeiro FT na emissão; todos os documentos
que originam obrigação de débito elegível criam Pendente nessa emissão. Receber
apenas em pendente existente; máximo emissão das origens ≤ Data Valor ≤ emissão
RC. Vencimento independente. E1/E2 encerram a pendência semântica anterior;
elegibilidade técnica permanece sem whitelist transitória por tipo. Credores
não elegíveis; não alterar transversalmente criação legada de pendentes agora.
Fatia 1a integrada pelo PR #27: campo/default/imutabilidade/validações/UI/PDF; históricos NULL,
sem backfill. Nova rejeição cronológica antes de numeração; nenhuma alteração
financeira operacional, F2/T1 ou corte. [Evidência](../fiscal/op22-b2-2/SLICE1A_VALIDATION.md).

## Fatia 1b — F2/T1 integrados pelo PR #28, circuito RC inactivo

PR #28 integrado em feature/visual-redesign, merge observado
2a0f8b08f9d79715dd4cd93a5888b9e6e79773eb, incluindo 8754ed3 e d2c5a7d,
confirmado no Git e API GitHub em 15-10-2026. PR #27 também integrado. Data Valor
permanece no RC (M1); efeito SETTLE e REVERSE deriva desse valor, separando
emissão e instante técnico. V24 aditiva vincula acto de anulação, REVERSE e
substituto, exige atomicidade e preserva RESTORE/CASH_OUT reversed=true com
vínculos aos originais. Não é reembolso. Anulação interna sem substituto exige
declaração auditável de pagamento não ocorrido; comprovação real e autorização
pública não estão implementadas. Substituição atómica cobre DV-09 sem dupla
liquidação. Históricos NULL preservados, T1 sem data conhecida recusado.
Consulta balanceAt interna limitada a âmbitos com datas documentais suportadas;
não usa timestamp para inventar efeitos. Legado, saldos/extractos e consumidores
permanecem inalterados. Integração não autoriza corte ou actualização da demo.
[Evidência e limites](../fiscal/op22-b2-2/SLICE1B_VALIDATION.md).

Revisão final Claude APPROVE, sem findings pendentes, comunicada pelo PO na
ordem de fecho; não se declara nova leitura do parecer integral pelo Executor.
CI PR #28 confirmado directamente como success no HEAD d2c5a7d: safety validator
e package sem testes, sem suite PostgreSQL. Evidência local PostgreSQL e respectivos
limites constam do documento da Fatia 1b; não somar fases como testes distintos.

O legado permanece a única autoridade operacional de liquidações/saldos/extractos;
a originação FT já integrada não significa activação do circuito RC do núcleo.
Na preparação pós-PR #28, Fatia 1c ainda não estava aprovada. [Proposta e alternativas](../fiscal/op22-b2-2/SLICE1C_PROPOSAL.md)
recomendavam diagnóstico interno de reconciliação só leitura, sem corrigir dados.
A recomendação não é decisão: PO decide âmbito, temporalidade FT, política de
históricos, eventual migração/perímetro e corte/reversão, após análise própria.


## Fatia 1c — diagnóstico interno integrado pelo PR #30

Após integração documental PR #29 (f06c09d), PO autorizou a opção 1c-B:
diagnóstico de comparação FT/RC apenas de leitura. FinancialReconciliation
identifica valores e vínculos, explica diferenças observadas sem declarar erro
contabilístico e torna explícita cobertura incompleta/histórica. Não corrige dados,
não escolhe temporalidade FT nem transfere autoridade financeira. O diagnóstico
recusa outro contexto empresarial que não o singleton 1 do modelo documental.
Valores ausentes não são zero; totais referem apenas subconjunto comparável.
Integrada pelo PR #30, merge cf0561562d74ddd3e84786c66ab2a78409811229,
incluindo 279b702 e e9bc4a943008a1652dd822656070abde61bd7096; ancestralidade
confirmada no Git local. Pareceres Claude arquivados e lidos, de 15-10-2026:
revisão inicial com dois MINOR e verificação final de e9bc4a9 APPROVE, 0/0/0,
ambos resolvidos; análise estática, sem execução própria de testes pelo revisor.
[Referências e hashes](OPERATIONAL_STATE.md#fatia-1c--diagnóstico-interno-integrado-pelo-pr-30).
Não se declara nova consulta de CI. [Contrato e evidência histórica](../fiscal/op22-b2-2/SLICE1C_VALIDATION.md)
conservam o contexto das entregas anteriores à integração.
Legado, saldos/extractos, demo e circuito RC novo continuam inalterados/inactivo.

## Fatia 1d — aceitação institucional do diagnóstico, 21-10-2026

O Product Owner aceitou formalmente a Fatia 1d como diagnóstico de prontidão
concluído em 21-10-2026, por decisão expressa comunicada na ordem do Executivo II
«OP22 — Fatia 1d | Registo institucional da aceitação».
[Texto da decisão disponibilizada, transcrito literalmente](records/2026-10-21-slice1d-po-acceptance.md).
Resultado mantido:
**FinancialCore não pronto para transição operacional**. Esta aceitação não
aprova cutover, deployment ou desenvolvimento seguinte. **Fatia 1e suspensa**.
Este registo foi integrado pelo PR #31 (merge 1a53ddc); a aceitação não
permanece pendente de publicação.

Evidência da base cf05615/PR #30: 142 testes financeiros + 2 comerciais = 144,
11 cenários e package aprovados na execução anterior, em PostgreSQL descartável.
Não é suite integral nem novo CI; não houve repetição dos testes neste registo.
Os 16 artefactos foram preservados, sem alterar originais, em arquivo externo
reservado Relatorios-AR2/TUULI/Arquivo-Reservado/OP22-Fatia1d, com manifesto de
nomes/tamanhos/SHA-256. O parecer Claude de 21-10-2026 foi **GO COM RESERVAS**
para fecho institucional, por leitura das evidências, sem executar testes.
[Estado, fontes exactas e gates](OPERATIONAL_STATE.md#fatia-1d--diagnóstico-concluído-e-aceite-pelo-po-em-21-10-2026).
A referência documental autónoma à autorização original 1d permanece pendente;
não inventar fonte nem confundir a aceitação actual com essa autorização original.

Lição institucional: igualdade de saldos correntes não prova reconciliação histórica
ou prontidão; NULL não significa zero e indicadores sobrepostos não são parcelas
aditivas. Diferenças esperadas entre circuitos não sincronizados não demonstram
por si sós erro contabilístico. O legado mantém autoridade operacional e o RC
novo permanece inactivo até decisão e corte coordenado próprios.

Permanecem os três BLOCKER de transição (autoridade/leitores, perímetro de dados,
fiscalidade/contrato público) e três MAJOR (cobertura histórica, cobertura funcional,
reversibilidade operacional). Aceitar o diagnóstico não resolve esses gates,
a temporalidade FT ou decisões arquitectónicas/fiscais pendentes; não autoriza
alterar dados, demo, emissores, consumidores ou extractos.


## OP22 / OP22-B — protecção classificativa integrada (22-10-2026)

PR #32 integrado em feature/visual-redesign, merge observado
6abf4ceda62528ed6fef91dc35383599b248ea79, incluindo e6aa3d6 e V25.
A V25 torna permanentes os atributos fiscais/financeiros do tipo já utilizado;
não reconstrói classificações anteriores à sua instalação.

A verificação delimitada OP22-B reproduziu em PostgreSQL descartável uma lacuna:
um financeiro não-RC podia trocar id_tipo_documento, passando a outro mestre
mesmo sem alterar os atributos protegidos pela V25. V26 integrada pelo PR #33 (merge 3cd75225d615ea27d90ad603e430d258f777cbe2,
commit 41253ae7a26e05b9824143635d15cab0b721866d) impede
essa troca do vínculo após persistência; não cria snapshot ou autoridade paralela.
Não existe estado de rascunho no modelo DocumentoFinanceiro. A protecção preserva
o vínculo existente, sem atribuir retrospectivamente a classificação de emissão.

Não altera leitores, extractos, agregações, saldos ou o circuito legado. Descrição
continua metadado editável, não uma classificação fiscal congelada. A semântica
de históricos anteriores sem prova permanece reservada: qualquer mudança de
fallback, classificação desconhecida ou recusa de leitura exige decisão do PO.
RC novo inactivo, Fatia 1e suspensa. Incremento integrado; parecer Claude APPROVE, 0 BLOCKER / 0 MAJOR / 0 MINOR,
confirmado por leitura do parecer arquivado de 22-10-2026.


Fecho institucional de OP22 (PR #32/V25, imutabilidade do mestre) e OP22-B
(PR #33/V26, imutabilidade do vínculo): concluídos estes incrementos, não a
transição financeira global. V25/V26 não reconstroem classificações anteriores
sem prova, não escolhem catálogo fiscal nem activam consumidores. Evidência
local arquivada e CI são distintos; nenhum teste foi repetido neste fecho.
A condição demo-base.sql/V25 continua aplicável antes de qualquer actualização.

O próximo objectivo do plano OB §10 é introduzir o catálogo canónico, depois da
preservação histórica. O princípio já aprovado não equivale à escolha de modelo:
OB §11 reservava arquitectura, matriz completa e política ao PO; D-CAT-1 abaixo
resolve a escolha de arquitectura. D-CAT-2A abaixo
aprova parte do modelo conceptual da matriz, sem resolver as restantes reservas. Mantêm-se GOV-ALT-001,
gate fiscal antecipado para valorização cambial e suspensão 1e; não transformar
esta sequência de referência numa autorização de desenvolvimento.


## D-CAT-2A — Tipologias fiscais e especializações

**Decisão funcional aprovada em 22-10-2026 pela Administração AR2 / Product Owner**,
comunicada directamente na ordem de registo institucional D-CAT-2A. Este registo
preserva a decisão; não constitui validação normativa independente nem implementação.

O comportamento fiscal e financeiro-base de cada Tipo de Documento TUULI é
determinado pela respectiva tipologia fiscal. Não são permitidas configurações
independentes que contrariem a natureza ou efeitos-base dessa tipologia. Só são
admitidas especializações expressamente previstas no modelo fiscal correspondente.

### Facturação — FT e FR

| Tipo TUULI | Código Fiscal | Liquidação imediata | Efeito funcional aprovado |
| --- | --- | --- | --- |
| FT | FT | Não | Factura que constitui dívida do cliente |
| FR | FT | Sim | Factura liquidada no momento da emissão |

FT e FR partilham o Código Fiscal FT; a distinção funcional é a especialização
de liquidação imediata. Os restantes atributos classificativos e efeitos-base
são determinados pela tipologia fiscal FT. Não inferir desta decisão uma alteração
automática dos tipos/configurações ou documentos já existentes.

### Liquidações — RC e RL

RC e RL seguem exclusivamente o modelo da respectiva tipologia fiscal, sem
especialização adicional de liquidação imediata. RC é documento de recebimento
de cliente para registar a liquidação de dívida existente. RL é documento interno
TUULI que regista pagamento ou devolução da empresa ao cliente, nomeadamente
para liquidar crédito resultante de nota de crédito.

São movimentos financeiros distintos; não presumir equivalência fiscal entre
RC e RL. A correspondência fiscal exacta de RL e a matriz de efeitos de cada
tipologia permanecem sujeitas a confirmação documental e validação antes da
implementação. O nome interno RL não estabelece o seu Código Fiscal.

### Consequências para D-CAT-2 e limites

A futura matriz deverá representar explicitamente tipologia e efeitos-base,
especializações permitidas (incluindo liquidação imediata para FT), comportamentos
próprios RC/RL sem opções redundantes de liquidação imediata, e combinações válidas
de área de gestão, Entidade e sinal fundamentadas no modelo aprovado. Classificação
fiscal, validade da configuração e habilitação operacional são dimensões distintas.

Mantêm-se V25 (imutabilidade da configuração após primeira emissão), V26
(protecção da associação financeira ao tipo), históricos sem reclassificação
inferida e todos os limites de activação vigentes. A decisão aprova o modelo
funcional conceptual acima, não a correspondência fiscal RL, valores ainda não
demonstrados da matriz completa, activação RC/RL, migração/reclassificação histórica
ou implementação técnica do catálogo. D-CAT-1 foi posteriormente aprovada,
conforme o registo abaixo; D-CAT-3 (versionamento/transição) foi posteriormente aprovada, conforme abaixo.


## D-CAT-1 — Arquitectura híbrida governada aprovada

22-10-2026: Administração AR2/PO aprovou expressamente uma definição semântica
única, versionada e governada do catálogo canónico, materializada em PostgreSQL
por migrations controladas e utilizada pela aplicação Java, com verificações de
coerência entre ambas. Não haverá edição livre da semântica fiscal pelos
utilizadores nem autoridades classificativas independentes.

[ADR-003](decisions/ADR-003-fiscal-catalog.md) conserva fundamento, alternativas
e limites. Implementa como contrato-alvo a D-CAT-2A, incluindo FT/FR com Código
Fiscal FT e liquidação posterior/imediata, e RC/RL sem especialização adicional
de liquidação imediata. RL mantém a correspondência fiscal por confirmar.
A aprovação da arquitectura não aprovou por si a matriz completa, implementação
ou activação; D-CAT-3 recebeu aprovação posterior própria. V25/V26, históricos e autoridade operacional legada preservados.


## D-CAT-3 — Revisões imutáveis e transição histórica aprovadas

22-10-2026: Administração AR2, na ordem «Continuidade D-CAT-2 / D-CAT-3»,
confirmou aprovação posterior ao relatório das 15h10: revisões fiscais imutáveis,
vínculo estável e transição sem reclassificação histórica automática. Esta é a
fonte expressa disponibilizada; não se reconstrói um acto anterior indisponível.

A aprovação resolve estes princípios de versionamento, não a matriz D-CAT-2 nem
a correspondência fiscal RL. A forma física das revisões e a representação de
campos sem especialização são escolhas técnicas dentro do contrato, desde que
não alterem efeitos nem históricos. Nenhum vínculo antigo recebe versão inferida
e nenhuma leitura passa a reinterpretar emitidos através da revisão mais recente.
V25/V26 preservadas; sem activação, migração histórica automática ou execução
funcional nesta entrega. [Preparação da primeira fatia](../fiscal/catalog/FIRST_SLICE.md).


### Catálogo fiscal — execução local da fundação inactiva

Por ordem posterior «Implementação da primeira fatia do catálogo fiscal», foram
implementadas revisões imutáveis, materialização verificada pelo leitor Java e
vínculos opcionais estáveis apenas na criação de novas configurações. V27 aditiva;
manifesto governado sem definições enquanto D-CAT-2 não está homologada. Nenhum
código fiscal, matriz de efeitos ou correspondência RL foi criado em produção.

A infraestrutura não altera CRUD, leitores ou emissão operacionais. V25/V26 e
históricos preservados sem backfill. Testes sintéticos demonstram mecanismo,
não aprovação fiscal. Parecer inicial Claude APROVADO COM RESERVAS recebido;
observações tratadas localmente e confirmação final Claude APROVADO,
0 BLOCKER / 0 MAJOR / 0 MINOR, sobre e54a967..cb76001. Parecer arquivado lido
na preparação de integração; revisão estática, sem nova execução de testes
pelo Revisor. A aprovação não activa o catálogo nem homologa D-CAT-2. [Evidência local e pedido de revisão](../fiscal/catalog/VALIDATION.md).


## D-CAT-2/H1.1 — Homologação semântica FT/FR/RC

23-10-2026. Fonte: ordem executiva D-CAT-2/H1.1 expressamente disponibilizada
pela Administração AR2. FT é factura de liquidação posterior e constitui dívida
do cliente; FR é factura-recibo de liquidação imediata, liquidada na emissão;
RC liquida dívida existente, reduzindo-a ou extinguindo-a. FT/FR partilham a
tipologia interna FT, diferenciadas pela liquidação imediata; RC não possui
especialização adicional. Esta homologação é semântica: não homologa todas as
combinações técnicas, códigos SAF-T ou projecções QR/PDF.

D-CAT-2/02 e parecer incremental Claude de 22-10-2026 foram lidos integralmente
no arquivo Relatorios-AR2/TUULI. Relatório:
2026-10-22_TUULI_D-CAT-2-02_matriz-homologacao-corrigida_Codex.md,
SHA-256 1b5cacaac93d9c4ea1259099f72a02a037e538da226a2078a082fad6f0ff1447.
Parecer: 2026-10-22_TUULI_D-CAT-2-02_verificacao-incremental_Claude.md,
APROVADO, zero achados. Não é revisão da presente especificação.

A [especificação H1.1](../fiscal/catalog/H1_1_FT_SPEC.md) prepara apenas FT
não imediata; não implementada nesta missão. FR/FT imediata, RC (até prova do
enquadramento fiscal), RL/NC/ND e restantes tipos excluídos. RL sem
correspondência homologada. V25/V26/V27 preservadas, sem backfill, alteração
histórica ou activação. Legado mantém autoridade; RC novo inactivo e 1e suspensa.
As reservas de combinações e projecções dos registos anteriores mantêm-se;
a pendência anterior de homologação da semântica FT/FR/RC foi superada por esta
ordem, não a matriz integral. Revisão Claude desta alteração ainda pendente.


### H1.1-R1 — rastreabilidade e reservas da especificação

Fonte primária disponibilizada nesta conversa: [ordem H1.1 e homologação H1
nela declarada](sources/2026-10-23_D-CAT-2-H1.1_ordem-recebida.md).
SHA-256 do arquivo: `8eed0d8bfae8d06420712d6ae54fc65a3214040fccdad66830b4d472d3545051`.
Transcrição documental da mensagem do utilizador em 23-10-2026, com proveniência
no próprio arquivo; nenhum acto H1 autónomo ou assinatura foi fornecido.
Não atribuir aprovação integral da matriz, projecções ou política de entrada.

R1 especifica compatibilidade material de configurações vinculadas em Java e
SQL para INSERT/UPDATE durante todo o ciclo de vida, inclusive antes da primeira
emissão; tipos legados sem vínculo inalterados. A política de entrada continua
pendente: A, obrigatória para todas as novas FT não imediatas, ou B, adesão
explícita com criação legada preservada. Recomendação técnica A, não decisão
aprovada. [Comparação e testes futuros](../fiscal/catalog/H1_1_FT_SPEC.md).
Parecer inicial comunicado na ordem R1: 1 MAJOR e 2 MINOR; correcções documentais
preparadas para revisão incremental Claude, ainda não aprovada. Sem implementação,
activação ou alteração histórica. Mantêm-se todas as restantes reservas H1.1.

## H2-03 / issue #39 — execução da política A em infraestrutura governada

27-10-2026. O estado A/B pendente no registo histórico H1.1 acima foi superado
pela [deliberação H2-02](sources/2026-10-23_H2-02_deliberacao-administracao-AR2.md):
política A aprovada para novas FT não imediatas, integridade Java/PostgreSQL,
sem guard de transição destinado a compatibilidade histórica da demo.
H2-03/R1 e o respectivo parecer incremental aprovado foram lidos integralmente;
fontes, hash da especificação e contrato em
[H2_03_IMPLEMENTATION.md](../fiscal/catalog/H2_03_IMPLEMENTATION.md).

A ordem/handoff do arquivo AR2-Executive-Reports no commit
`4032d5742ac4a59cd3075f9d2dad85949a5a70ef` autorizou a implementação controlada
na issue #39. Base comprovada: PR #37,
`c17a9d8af9cf2d197c8c8c21f1f780a38efee0d9`. Trabalho local isolado em
`codex/h2-03-document-type-contract`; ainda não integrado nem publicado.

Implementados vínculo persistido/leitura, resolução explícita de revisão de
criação, validação de tuplos completos e V28 aditiva contra escritas incompatíveis.
V25/V26/V27 intactas; sem revisão atribuída a históricos e sem classificação por
ID/nome. RC/RL/guias fora do perímetro mantêm as regras anteriores. O inventário
interno de prontidão não constitui habilitação fiscal ou activação operacional.

Mantém-se o manifesto vazio, sem combinação 2/1/1 ou projecção QR/PDF publicada.
As definições usadas em testes são sintéticas e não decisões da Administração.
Sem alterações à base histórica, demo, ambiente preparado #38, emissão, numeração,
saldos ou consumidores. Legado como autoridade operacional; FinancialCore
inactivo e Fatia 1e suspensa. [Validação local](../fiscal/catalog/H2_03_VALIDATION.md),
distinta do CI; revisão independente Claude da implementação ainda pendente.

Preservação paralela, sem alteração do conteúdo: parecer Claude R2 da issue #38
arquivado em AR2-Executive-Reports, commit
`353fed87899456b240d1af5d2982f5baf5daea97`, caminho
`TUULI/2026/10/2026-10-27_TUULI_Issue38_R2_revisao-independente_Claude.md`.
Original com 8454 bytes, SHA-256
`a8f3cd7681f07cff703ae977183b795370eea47e09cba133c5fd985c2a131411`,
conferido com o original local e com o conteúdo devolvido pelo GitHub.
Não representa nova revisão nem repetição dos trabalhos #38.


### H2-03 — resolução incremental do parecer de 28-10-2026

Parecer Claude original APPROVE WITH CONDITIONS, 0 BLOCKER / 0 MAJOR / 3 MINOR,
10711 bytes, SHA-256
`b2475e9a09ebeda4c6170f6f57958648770297f5b018a2cd42d9d397ed3db74c`,
arquivado sem alteração no repositório privado AR2-Executive-Reports, commit
`b3234d47243707935b4c92cbb7d793951b17dc15`, pasta `TUULI/2026/10/`.
A antiga indicação de revisão da implementação pendente fica superada por este
parecer condicionado; a confirmação incremental das correcções continua pendente.

MINOR-001: reexecução das mesmas 15 classes sobre `6c32e283`, com HEAD/worktree
limpos e hashes do código antes/depois. A tentativa numa base partilhada teve
316 aprovações e duas falhas de contagem por dados entre suites; preservada.
Sem alterar código, fixtures ou asserções, a consolidação com base descartável
por classe aprovou os 318 testes, zero falhas/erros/ignorados. Evidência e
manifestos em [H2_03_VALIDATION.md](../fiscal/catalog/H2_03_VALIDATION.md).

MINOR-002/003: clarificação documental de comportamentos existentes, sem mudança
semântica: RC/RL/guias sem identidade explícita mantêm a prontidão fechada como
pendência fiscal; a incoerência integral do catálogo recusa 409 mesmo para edição
só descritiva de tipo vinculado. Não se criou fallback ou exigência universal
para tipos fora do perímetro. Gates 2/1/1, QR/PDF e AT preservados; sem cutover,
publicação fiscal, activação FinancialCore ou alteração à origem/demo.

### H2-03 — aprovação incremental final e preparação da publicação

28-10-2026. Parecer independente Claude integralmente recebido e lido:
**APPROVE, 0 BLOCKER / 0 MAJOR / 0 MINOR**; os três MINOR estão resolvidos sobre
`6c32e283` e `2dcb797c`. Supera a pendência de confirmação acima. O Revisor
verificou hashes, XML e diff, sem executar testes. Original arquivado sem alterações
em AR2-Executive-Reports, commit `a9992b9c8e79d850402b3e8c37be903bf24d9de8`,
ficheiro `TUULI/2026/10/2026-10-28_TUULI_Issue39_H2-03_MINORS_verificacao-incremental_Claude.md`,
SHA-256 `bfe568045999fa4984d12839935357c1343f3f92dd918a31224c497520eff0a3`.
A Administração autorizou preparar publicação controlada, PR e CI. Autenticação
SSH indisponível neste executor; nenhuma alteração de credenciais ou reconstrução
dos commits. Infraestrutura aprovada permanece local até publicação efectiva.
Manifesto vazio; gates fiscais/cutover/FinancialCore e Fatia 1e preservados.
Norma central consultada: AI_DEVELOPMENT_SYSTEM.md, commit
`2f6cd4a83aa9e552d1daa32c51a99a7bedd0b4e5`; decisão de merge de António.


### H2-05 / issue #41 — implementação delimitada e dependência fiscal

28-10-2026. Base integrada `91da67bc5e799d30abfd1c569ec7ef22fbd5f703`, PR #40,
H2-03. Supera a indicação histórica de publicação bloqueada acima. Ordem issue
#41 autoriza ATCUD em todas as páginas, geometria QR T12 e derivação Q/R, sem
homologação, activação, cutover ou alteração a históricos. H2-04/R1 foi lida no
commit documental `64c6a2d1a33cd5926bcd67b5c50c2fc995c126c2`, não integrado
nesta base.

Implementados cabeçalhos ATCUD, Byte/ECC M/versão ≥9 e margem física, extracção
Q das posições 1/11/21/31 e R de fonte autorizada por documento. Removidos os
defaults fictícios. Não existe fonte produtiva de assinatura persistida; a nova
fronteira é um contrato interno, não um assinador ou certificado AT. Ausência
recusa nova emissão comercial com rollback; reimpressões preservam payloads
existentes. Esta dependência impede declarar a emissão comercial pronta e exige
identificação do mecanismo fiscal autorizado, sem invenção de chaves/valores.

231 testes finais locais aprovados (57 dirigidos e 174 PostgreSQL descartável),
package e PDF final verificados. Não se somam execuções intermédias nem se
confunde evidência local com CI. Revisão Claude solicitável pelo
[pedido preparado](../fiscal/h2-05/REVIEW_REQUEST.md), ainda pendente.
[Implementação, alternativas técnicas e reservas](../fiscal/h2-05/IMPLEMENTATION.md).
V25–V28, dados históricos, manifesto e circuitos mantidos; nenhuma activação
de catálogo, FinancialCore ou Fatia 1e. Gates fiscais e FAQ 4443 não resolvidos.


### H2-05-R1 — alternativa B autorizada para demonstração isolada

28-10-2026. Parecer Claude sobre `3262390`: APPROVE WITH CONDITIONS,
0 BLOCKER / 1 MAJOR / 3 MINOR, original SHA-256
`4540cbf2ec6e1a2a238b98d4dd60374f555822d36326bf4ad2de0844cb38bb5a`.
A ordem executiva H2-05-R1 escolheu expressamente fornecedores demo/produção
separados. Implementado fornecedor sintético opt-in, perfil demo exclusivo,
cluster/base/séries autorizados e chave externa não versionada. Guard anterior
a Flyway/JPA recusa destinos impróprios e séries reais. QR demo tem formato
não fiscal e origem persistida `TUULI-DEMO-1`, Q prefixado DEMO e R textual
DEMO-SEM-CERTIFICADO; PDF marcado sem validade fiscal mesmo na reimpressão.
Não há certificado AT fictício ou relaxamento da validação produtiva.

ATCUD vazio omitido. ZXing 3.5.3 fixado e smoke empacotado preparado.
Fixture da matriz de permissões adaptada ao vínculo governado V28 já exigido;
sem alteração às regras fiscais ou asserções de permissões. O seed novo inclui
apenas as duas referências postais canónicas que já utilizava. Não se aplicou
a qualquer demo/base persistente. Resultados efectivos e confirmação Claude
da R1 são tratados separadamente da revisão original: [contrato R1](../fiscal/h2-05/R1.md).

Registo explícito: a H2-05 inicial retirou o QR fictício dos novos recibos
legados sem fonte e do fallback de impressão; QR opcional pode ficar ausente.
R1 só acrescenta payload não fiscal a RC demo. RC/RG e enquadramento IVA de
caixa permanecem gate fiscal próprio. Assinador/certificado reais em falta;
produção sem fonte continua 409. Não há activação de catálogo, novo RC,
FinancialCore ou Fatia 1e, nem homologação, cutover, push, PR ou merge.


### H2-05-R2 — regressão preexistente de permissões e condições de integração

28-10-2026. Parecer independente Claude R1: APPROVE WITH CONDITIONS,
0 BLOCKER / 0 MAJOR / 3 MINOR (A/B/C), SHA-256
`087ebd0d521649547d45c753ab50125a5cf27c855ecc04582513ca05151732cf`.
Supera a indicação de confirmação R1 pendente acima; a confirmação R2 é distinta.

**MINOR-C — facto de continuidade:** na base integrada H2-03/PR #40,
`91da67bc5e799d30abfd1c569ec7ef22fbd5f703`, a fixture de
PermissionMatrixIntegrationTests criava DCT com código FT sem vínculo governado.
O código nesse commit foi conferido; a execução R1 reproduziu seis erros SQL
23514, «New or reclassified FT deferred configuration requires a governed binding»,
por efeito da V28. Era regressão preexistente da fixture, não consequência da
separação de assinadores H2-05. R1 (`203276f`) passou a usar GovernedTypeFixtures.bind
e a fonte exclusiva de testes, sem alterar protecções ou asserções de permissões;
os seis testes passaram. Não se afirma que o CI limitado tenha detectado essa
regressão. Em futuras migrations que afectem tipodocumento, inventariar e testar
as classes SpringBootTest que criam tipos ou emitem documentos, além das suites
directamente alteradas.

MINOR-B: 15 classes repetidas e 254 testes aprovados em HEAD limpo `203276f`,
tree `a370beef85c53ca025e8c17c4e4ef94dd212359c`, com metadados por execução e XML.
O complemento R2 exige nova execução no seu próprio commit final; o resultado
pós-commit é preservado no relatório externo, sem encadear commits autorreferenciais.
MINOR-A: procedimento de instalação demo nova e novo GOLDEN documentado, sem
actualizar a Ubuntu ou aprovar qualquer GOLDEN. R-1/R-2: diagnóstico interno de
marcadores demo e rótulo QR adequado, sem alterar autoridade financeira ou gates.
[Contrato, fontes e revisão R2](../fiscal/h2-05/R2.md). Produção sem fonte real
continua 409; manifesto vazio, FinancialCore inactivo e Fatia 1e suspensa.


### H2-05 integrada e H2-06 — proposta produtiva sujeita a decisão

30-10-2026. PR #42 integrado em `ac07247da7f4dce973b471e5334e2b86bb4e97e4`, incluindo H2-05 `e1e9e63`. Parecer final Claude APPROVE 0/0/0, SHA-256 `d377b2f572fb9f3c95e1b19dc2bc06e3cd7f0eb663842290086b325c21d6d5af`, original conferido. Supera a pendência R2 anterior. Os 286 testes locais/17 classes e o CI success (safety tests/package) têm âmbitos distintos; não foram repetidos na H2-06.

Issue #43 autoriza análise, não integração funcional: [levantamento H2-06](../fiscal/h2-06/CURRENT_CONTRACT.md), [alternativas e decisões propostas](../fiscal/h2-06/PROPOSAL.md), [fontes/revisão](../fiscal/h2-06/SOURCES_REVIEW.md). Separados assinatura documental, certificado do programa, chave/versão, ATCUD, Q/R e credenciais de operação. Identificadas lacunas de persistência, instante técnico antes do QR, cadeia e validação criptográfica; não apresentadas como novas decisões do PO nem como defeitos reabertos da H2-05.

A proposta compara custódia local e serviço não exportável, sem escolher custódia produtiva pela Administração. Perfil normativo, proveniência legítima, reservas fiscais e revisão Claude H2-06 continuam pendentes. Nenhuma implementação, chave, emissão, migration ou acesso a base nesta missão. Preservados 409 sem fonte, gates fiscais, isolamento demo, legado como autoridade financeira e suspensão da Fatia 1e.


### H2-06-R1 — correcção documental pós-revisão

30-10-2026. Revisão H2-06: APROVADO COM RESERVAS, 0 BLOCKER / 1 MAJOR / 2 MINOR, conforme [registo executivo #43](https://github.com/amgsrosario/fac/issues/43#issuecomment-6530199084). Supera a indicação de revisão inicial pendente; confirmação incremental ainda pendente. Corrigidas a caracterização dos requisitos criptográficos do exemplar do Despacho 8632/2014 (separada da confirmação de vigência actual), a alegação não demonstrada de arquivo integral FAQ 4443 e a delimitação da cadeia série/tipo fiscal, plurianualidade e datas. Actualizados A/B e T02/T03/T04.

[Fontes e pedido incremental Claude](../fiscal/h2-06/SOURCES_REVIEW.md). D-SIGN-01 a D-SIGN-05 suspensas até confirmação; não há aprovação independente desta correcção. Parecer original não arquivado nesta R1 por não estar disponibilizado; não reconstruído a partir do resumo executivo. Nenhum código, teste executável, chave ou base alterado; sem emissão, chamadas operacionais AT, despesa, deploy, activação ou merge. Mantidos 409 e todos os gates fiscais/financeiros.


### H2-07 / issue #44 — primeiro mecanismo de assinatura, experimental e inactivo

30-10-2026. A issue #44 regista aprovação final Claude H2-06 (0/0/0 em `10483f8`) e D-SIGN-01 parcialmente aprovada para FT posterior/EUR, série nova e material de ensaio. Supera a confirmação incremental H2-06 pendente acima; D-SIGN-02..05 não foram aprovadas. A retoma autorizada criou `codex/h2-07-signature-isolated` sobre a integração `ac07247` e incorporou apenas a documentação H2-06 aprovada. Trabalho de autoria não comprovada em `h2-07-signature-inactive`/`7ce573f` preservado e não reutilizado.

[Contrato H2-07](../fiscal/h2-07/CONTRACT.md): biblioteca sem bean/endpoint/fonte produtiva, canonicalização, assinador efémero, provas experimentais imutáveis, cadeia e replay. Schema Flyway de ensaio separado; não cria V29 produtiva nem toca V25–V28. [Validação e reservas](../fiscal/h2-07/VALIDATION.md): 29 testes novos e 165 de regressão aprovados em execuções documentadas, não uma suite integral nem uma única execução sem erros. PostgreSQL 16.3 descartável; fases com erros de fixture/expectativa preservadas.

Revisão Claude H2-07 pendente. Não é emissor fiscal, prova de custódia real, certificação ou activação. SQL não verifica RSA; leitura/append recusam prova forjada. Preservados produção sem fonte HTTP 409, gates 2/1/1, QR/PDF, AT, RC/RG, FAQ 4443, catálogo vazio, FinancialCore inactivo e Fatia 1e suspensa. Origem, demo e #38 sem alterações. Sem deploy, cutover ou merge.


### H2-07-R1 — reservas Claude e isolamento em âmbito de testes

02-11-2026. Conforme ordem #44, parecer sobre `eff724f`: APROVADO COM RESERVAS, 0 BLOCKER / 0 MAJOR / 3 MINOR; supera a revisão inicial pendente acima. R1 move biblioteca e SQL experimentais para src/test, retirando-os do artefacto produtivo, e explicita ausência de âncora independente para a chave da própria prova. O desenho produtivo exige registo confiável separado para chave/keyVersion/âmbito, sem implementar custódia nesta missão.

[Procedimento R1](../fiscal/h2-07/VALIDATION.md): runner para execução única das dez classes/194 casos sobre commit fixo e limpo, HEAD/tree/status antes/depois, logs/XML/hashes e inspecção do JAR. O SHA testado e resultados efectivos são preservados no relatório externo R1 e na issue #44, sem commits autorreferenciais; os resultados de 30-10 não substituem esta execução. Confirmação incremental Claude pendente.

Worktree desconhecida/`7ce573f` preservada. Mantidos HTTP 409, D-SIGN-02..05 pendentes, gates fiscais e financeiros, ausência de chaves reais, emissão produtiva, activação, deploy ou merge.


### H2-07 integrada / H2-08 — preparação das decisões produtivas

02-11-2026. PR #45 integrado em `f7e8f8ad405a0e5714af0e9fc7264a9e581ff0de`, incluindo `e7eb142`; Git/API/ancestralidade conferidos. [Parecer H2-07-R1 registado](https://github.com/amgsrosario/fac/issues/44#issuecomment-6580103638): APROVADO 0/0/0, por leitura e evidências, sem testes executados pelo revisor. [Integração autorizada](https://github.com/amgsrosario/fac/issues/44#issuecomment-6580136463). Supera as indicações de confirmação/integração pendentes acima. H2-07 encerrada, ensaio em src/test, sem assinatura produtiva; não reaberta nesta missão. Arquivo byte a byte do parecer original não é presumido a partir do comentário executivo.

Issue #46 autoriza análise H2-08, não aprovação D-SIGN-02..05. [Dossier de decisão](../fiscal/h2-08/DECISIONS.md), [integração/riscos/testes futuros](../fiscal/h2-08/INTEGRATION_TESTS.md), [fontes](../fiscal/h2-08/SOURCES.md) e [sucessão/pedido Claude](../fiscal/h2-08/REVIEW_HANDOFF.md). Comparadas custódia local, HSM local e serviço remoto; A/B1/B2 sem preferência até confirmação D-SIGN-04 e suporte real (R1); sem escolha/contratação. Registo de confiança independente, proveniência, perfil actual e operação futura sujeitos às decisões/validação indicadas. Revisão Claude H2-08 pendente.

Nenhuma alteração funcional, acesso a bases, chave real, teste de aplicação, serviço pago ou operação AT. Exemplares normativos públicos arquivados com hashes; não são confirmação integral de vigência/aplicabilidade ou certificação. Preservados 409, gates fiscais/financeiros, manifesto, demo, origem/#38 e worktree desconhecida `7ce573f`. Sem activação, deploy, cutover ou merge.


### H2-08-R1 — correcção documental após parecer 0/1/2

02-11-2026. [Ordem #46/6583447094](https://github.com/amgsrosario/fac/issues/46#issuecomment-6583447094): parecer Claude APROVADO COM RESERVAS sobre `882381f`, 0 BLOCKER / 1 MAJOR / 2 MINOR; supera a revisão inicial pendente, sem declarar R1 aprovada. Original local lido e hash conferido: `cfde064651675a9898bf4316ef9f188bad2b4dd0e5fce14618cbc4a0ae92fca2`, 14805 bytes. Não se presume publicação do original no arquivo privado.

R1 liga proveniência de cada keyVersion à chave pública comunicada por Modelo 24/upload e mantém âncora independente; âmbito da chave é matéria normativa do produtor/programa, não opção livre por cliente. Gate prévio de rotação e validação jurídica de custódia B2/exclusivo conhecimento explicitados. Retirada preferência B; alternativas sem preferência até perfil actual e suporte demonstrados. Acrescentados persistência/regime de cifragem, Hash/HashControl SAF-T, política de backups, contador de reposições e detecção de manipulações a N02/D-SIGN-05/T08/T11/T12/T16. Reservas de vigência/aplicabilidade e fornecedores mantidas.

[Pedido incremental](../fiscal/h2-08/REVIEW_HANDOFF.md). Confirmação Claude R1 pendente, D-SIGN-02..05 reservadas à Administração. Só documentação: H2-07 não reaberta, nenhum código/chave/base/gate alterado, PR #47 draft, sem implementação, activação, deploy ou merge.


### Repriorização executiva — levantamento de pendências em 02-11-2026

[Mandato #46/6584848804](https://github.com/amgsrosario/fac/issues/46#issuecomment-6584848804). H2-08 integrada pelo PR #47 em `051b1fc`, incluindo `2523095`; confirmação original Claude R1 APROVADO 0/0/0 lida e hash conferido (`518ef2c1c58a04a223ef2042b84ef3e6f42bc33885e8babcd6315a8226b237e7`). Supera as indicações anteriores de revisão/integração pendentes; não decide D-SIGN-02..05, agora reservadas à fase final. H2-07/H2-08 não reabertas.

[Mapa de pendências](../planning/reprioritization-2026-11-02/PENDING_MAP.md), [fontes](../planning/reprioritization-2026-11-02/EVIDENCE.md) e [missões propostas/revisão](../planning/reprioritization-2026-11-02/MISSIONS.md). #38 fechada quanto à preparação e restauro: R2 confirmou a prova V27, errata cc67e3f resolveu metadado temporal. Reserva operacional MINOR-003R persiste; H2-03 já foi integrada (V28/PR #40), não continua por implementar. Delta da base exacta/runtime actual ainda não comprovado neste inventário. Só metadata Docker lida, sem SQL ou credenciais.

Prioridade proposta de execução futura: M01 verificar/demonstrar delta da base limpa, M02 capacidade segura de primeiro provisionamento, M03 jornada comercial não fiscal, M04 evidências fiscais independentes e M05 piloto isolado. São propostas para decisão, não novas missões iniciadas. JWT/permissões/Security Gate continuam encerrados; ensaio da instalação e inputs operacionais são pendências distintas. Não presumir prontidão por issue fechada ou ausência de implementação por issue aberta.

Este levantamento é documental, sem testes de aplicação ou alteração de bases. Revisão Claude pendente. Mantidos 409, origem/demo/#38, worktree desconhecida `7ce573f`, manifesto/gates, FinancialCore inactivo e 1e suspensa; sem chaves, contratação, activação, deploy, cutover ou merge.


### PR #48-R1 — preservação das pendências posteriores

03-11-2026. [Revisão/ordem 6600046074](https://github.com/amgsrosario/fac/pull/48#issuecomment-6600046074): APROVADO COM RESERVAS 0 BLOCKER / 0 MAJOR / 1 MINOR sobre `fdacf90`; supera revisão inicial pendente, sem declarar R1 aprovada. Correcção documental preserva expressamente NC/ND e RC/NL sujeito a nova OP, gates legais NL/RCzero/descontos/adiantamentos, Auditoria Funcional Final e Preparação/Certificação AT como adiados/reservados, não cancelados. Certificação posterior às fases preparatórias e dependências de assinatura; nenhum gate ou ordem M01–M05 alterados.

O-1: autoria do R2 #38 atestada por Claude Code no parecer original PR #48, SHA-256 `e84c4fd27cda2439b2936f465d5240a2eb292aa1ddeca94a78ceaf2b69de6d1c`, conferido com sidecar. EVIDENCE.md regista a atestação e a atribuição anterior errada, sem repetir ensaios ou presumir publicação privada. O-2 apenas explicita menor privilégio/uso único/auditoria/ausência de canal remoto como critérios futuros M02. [Pedido incremental](../planning/reprioritization-2026-11-02/MISSIONS.md) pendente. PR draft, sem código, bases, gates, implementação, activação, cutover ou merge.


### M01 — Delta isolado da preparação #38, 03-11-2026

[Ordem #46/6609266825](https://github.com/amgsrosario/fac/issues/46#issuecomment-6609266825). Base integrada confirmada `472cf5847949c9c7d2ad6b75ae5dd444a60bf9f8` (PR #48); não confundida com o checkout principal antigo. [Relatório M01](../planning/m01-clean-base-readiness/REPORT.md) e [validação](../planning/m01-clean-base-readiness/VALIDATION.md). Resultado executor **M01_PASS para o delta isolado**, revisão independente Claude **pendente**.

Preparação conservada comprovada em V27 por READ ONLY, 42 controlos de tabelas iguais a R2 e antes/depois. Snapshot autorizado reutilizado apenas para clone tmpfs: V28 aplicada, validate/rearranques sem migration adicional, 84 FKs/zero órfãos, exclusões preservadas; 7 casos de leitura aprovados em três execuções finais e 1 regressão HTTP 409 sem consumir número em banco sintético separado. São oito casos distintos, não 22 testes únicos. Tentativas iniciais do novo teste (compilação e default de segurança do pom) registadas e corrigidas no teste, sem alteração da aplicação.

Arquivo restrito `tuuli-backups/m01/2026-11-03/`, 32 artefactos e manifesto SHA-256 `9800d974d4f793954c4082b8a68b0b506cee8b07e4160702b627c6b25128b0e8`. Recursos efémeros removidos com guards; segredo invalidado, nenhuma credencial arquivada. Não se repetiu a importação/restaurabilidade #38; não se actualizou a preparação, que permanece V27. Runtime demonstrado é contexto de teste, não deploy/prontidão comercial.

Sem mudança a origem, Ubuntu/GOLDEN, worktree `7ce573f`, código operacional, emissores, manifesto ou gates. Legado autoridade operacional, FinancialCore inactivo e 1e suspensa. M02–M05 não iniciadas. Emitente, provisionamento, configurações/séries legítimas e validação operacional/fiscal mantêm-se pendentes; assinatura reservada à fase final. Trabalhos adiados, auditoria final e certificação AT não cancelados. Sem cutover, activação ou merge. [Encaminhamento efectivo ao revisor](../planning/m01-clean-base-readiness/REVIEW_HANDOFF.md) depende da Coordenação se não houver runtime Claude; comentário GitHub não o activa.


### M01-R1 — Reservas de qualidade da prova, 03-11-2026

Parecer original Claude sobre `bf80a2f`: **APROVADO COM RESERVAS 0/0/2**, SHA-256 `9bd30872d2399cc941385460c8e3ec4d77ae8f326b4335e726d3a14ed533e719`, original conferido/preservado. Supera a indicação anterior de revisão M01 ainda não recebida; não implica confirmação R1.

MINOR-001 corrigido no teste: estado efectivo de `SecuritySettings.enabled()`, filtros Bearer/Authorization e resposta 401 ao GET anónimo `/clientes`. Apenas esse percurso, não prova universal de segurança/bypass. MINOR-002: ensaios originais anteriores ao commit e sem estado Git por execução explicitamente documentados; nova captura antes/depois implementada. Validação adicional: **1 caso/1 aprovado**, exclusivamente em banco sintético tmpfs, HEAD limpo `1bf2f135074b1141913afdc1dffd723516951cb9`, tree `55dd470b30b41fd77b05511e196ce2c8ce8c5ca1`, invariantes antes/depois. Sem repetir #38 ou o delta do snapshot V27→V28; sem acesso às bases conservadas. Recursos próprios/segredo efémero eliminados.

[Validação R1](../planning/m01-clean-base-readiness/VALIDATION.md), arquivo separado `tuuli-backups/m01-r1/2026-11-03/`, manifesto SHA-256 `a6c480f9eccc7fecdee7d736b3da77fba1bdaac94194d8457fcfcd4a26c1563e`; originais M01 mantidos. [Pedido incremental](../planning/m01-clean-base-readiness/REVIEW_HANDOFF.md) será encaminhado pelo PO, conforme indicação expressa; confirmação Claude **pendente**, sem presumir activação pelo GitHub. Sem M02, alterações operacionais, bases, gates, push, PR, merge, cutover ou activação.


### M02 — Primeiro administrador local, implementação isolada de 03-11-2026

[Ordem #46/6611272547](https://github.com/amgsrosario/fac/issues/46#issuecomment-6611272547). Base `ca1e802`/PR #49 confirma a M01 encerrada após Claude 0/0/0 e CI verde, superando as indicações históricas de revisão M01 pendente. M01 e o Security Gate não reabertos. Nova branch `codex/m02-local-admin-provisioning`; checkout principal e worktrees conservados.

Reconhecimento dirigido não localizou bootstrap comercial local aprovado: CRUD é autenticado e runners/seeds de demo não constituem esse mecanismo. [Desenho M02](../planning/m02-local-admin-provisioning/DESIGN.md): CLI local explícita sem Spring/endpoint, autorização 0600 e identidade base/cluster/UUID/role, passwords só por consola sem eco. V29 aditiva cria comprovativo/UUID e funções sem EXECUTE PUBLIC, sem contas/roles/grants automáticos. Operador técnico restrito só executa funções; criação de ADMINISTRADOR activo + auditoria existente + consumo único atómicos, locks ordenados e receipt imutável. Não é recuperação/reset de administradores existentes.

[Validação dirigida](../planning/m02-local-admin-provisioning/VALIDATION.md): **99 casos distintos aprovados** (22 novos M02 + 77 regressões), package e smoke do JAR aprovados, com Git limpo capturado por classe. Duas tentativas de testes com expectativas incorrectas/desactualizadas preservadas e corrigidas, sem mudança aos contratos de autenticação ou fiscais. Código funcional idêntico nos commits ensaiados `3a5f49b`, `7fc4e28`, `077a44a`; fecho posterior só documental. Não é suite integral nem CI.

Evidência restrita `tuuli-backups/m02/2026-11-03/`, 100 entradas + manifesto geral SHA-256 `4cdcbd11d90c1c69c35fc6badf130f47f8e0fc550860418d5530f0ed622f83b0`. Recursos e segredos efémeros M02 removidos. Nenhuma base conservada/histórica, Ubuntu/GOLDEN ou worktree `7ce573f` operada; V29 não instalada na preparação conservada (V27 conforme M01). Nenhum histórico fiscal reclassificado, saldo, consumidor ou manifesto alterado.

**Estado M02: implementada/testada em isolamento, revisão independente Claude pendente** — [handoff](../planning/m02-local-admin-provisioning/REVIEW_HANDOFF.md). Autorizações/identidades comerciais, roles/HBA reais e operação numa instalação real dependem de ordem própria. Sem push, PR, merge, deploy, cutover ou activação. Legado autoridade operacional; FinancialCore inactivo, 1e suspensa, gates mantidos e M03–M05 não iniciadas.


### M02-R1 — Correcção incremental após revisão Claude, 03-11-2026

[Ordem #46/6612496711](https://github.com/amgsrosario/fac/issues/46#issuecomment-6612496711). Primeiro parecer original Claude sobre `5ae9e50`: **APROVADO COM RESERVAS 0/0/2**, SHA-256 `790a988f0b9d5af3f0e037387c02a91cd389057d26338d151cfb3aa5bd52744a` conferido com sidecar e igual à cópia recebida pela Coordenação. Supera a indicação anterior de primeira revisão ainda pendente; não é aprovação R1.

[R1](../planning/m02-local-admin-provisioning/R1.md): MINOR-001 com guarda efectiva de terminal compatível Java 21/reflexão Java 22+, recusa própria 3 e teste stdin não vazio; MINOR-002 com ownership de objectos relevantes/schema/CREATE/DML e casos não-superuser; O-1 com READ COMMITTED explícito em Java/SQL. V29 ainda local corrigida, sem outra migration. **33 casos dirigidos aprovados**, package/smoke aprovados no commit limpo `b3eb249fb1d13795af72fadc32dade62b5686896`, tree `a5462552377b318c7afef448e1d427817d61c45d`, Git antes/depois igual. Não repetidos os 99 casos, M01/#38 ou testes não afectados.

Arquivo separado `tuuli-backups/m02-r1/2026-11-03/`, 41 artefactos + manifesto de 7698 bytes, SHA-256 `56bc1818a820f8f054b74f17416a8de90865e8dff7f07015f5270035f2771f91`. Original M02: 100 entradas/manifests conferidos e intactos; parecer/sidecar preservados. Recursos R1 descartados com guards, sem segredos nos artefactos. Nenhuma base conservada/histórica, Ubuntu/GOLDEN ou worktree 7ce573f operada.

O-2 dono SECURITY DEFINER não-superuser continua limite de prova de instalação; testes de owners são de **recusa**. O-3 localhost depende de resolução para loopback, preferir 127.0.0.1. O-4 dívida de testes históricos fora do incremento; O-5 colisão V29 condicional a eventual mandato futuro sobre worktree desconhecida, sem presumir integração ou reabrir H2-07. Java nativo disponível é 21; caminhos pós-21 simulados e guarda implementada, não declarada execução nativa inexistente. O-1 mantém classificação não condicionante original.

**Confirmação incremental Claude pendente**, [handoff actualizado](../planning/m02-local-admin-provisioning/REVIEW_HANDOFF.md) para transmissão efectiva pela Administração. Sem auto-aprovação, push, PR, merge, contas reais, cutover, deploy, activação ou M03–M05. Legado autoridade operacional, FinancialCore inactivo, 1e suspensa, manifesto/gates preservados.


### M03 — Jornada comercial não fiscal, 04-11-2026

[Ordem #46/6613324580](https://github.com/amgsrosario/fac/issues/46#issuecomment-6613324580). Base integrada confirmada `e6bb33f76523a268b39978733e099c96e98d7999`, merge PR #50/M02. Final M02 `e09433b` incluído; revisão incremental Claude 0/0/0, original/sidecar conferidos, SHA-256 `3a4f9110e2d0a8492e49a5a3bd53bddcf0c70baef98354c8aaba51c15cdd94a3`. CI PR #50 safety/package verde; não representa suite PostgreSQL. Supera as indicações históricas de revisão/integração M02 pendente. Observações O2–O9 e limites de instalação continuam válidos.

[Levantamento M03](../planning/m03-commercial-journey/COVERAGE.md) reutilizou cobertura existente e limitou a lacuna à jornada HTTP autenticada sem fonte/revisão fiscal. Branch isolada `codex/m03-commercial-journey`, sem alterar principal ou outras worktrees. **27 casos distintos aprovados** (9 novos de jornada +18 seleccionados) e package no HEAD limpo `7acfc9ec71d9f5c46b61b162ec19a48c3e76c843`, tree `dfc0a646f7d348e7d687c137640669f0d9fc0746`; antes/depois iguais por classe. PG16.3 novo/tmpfs V1–V29, sem reutilizar dados conservados; não repetiu M01/#38. Duas tentativas iniciais de preparação do teste preservadas, corrigidas no teste, não somadas aos casos únicos. Nenhum defeito funcional demonstrado nem alteração de src/main/migrations.

[Validação e limites](../planning/m03-commercial-journey/VALIDATION.md): rascunhos/linhas/mestres/parâmetros, auditoria/perfis e exportações CSV/XLSX não fiscais; 409 sem assinatura/manifesto, rollback da numeração e ausência de movimentos financeiros. Fixtures não homologam configuração fiscal; não é browser, SAF-T, deploy ou prontidão comercial. Arquivo restrito `tuuli-backups/m03/2026-11-04/`, 87 ficheiros + manifesto global SHA-256 `1af9232f2bc54289fcf490806dc995f7017930520f25c0cd1e3a68cce2791cd3`, cópias conferidas e originais preservados. Recursos/segredos efémeros próprios removidos.

**M03 preparada para revisão independente, não aprovada/integrada.** [Handoff](../planning/m03-commercial-journey/REVIEW_HANDOFF.md) a transmitir pelo PO/Coordenação; comentário GitHub não activa Claude. Sem push/PR/merge, contas reais, bases conservadas, demo/GOLDEN ou 7ce573f operadas. Mantidos manifesto vazio, HTTP 409 e gates fiscais; legado autoridade operacional, FinancialCore inactivo, Fatia 1e suspensa. M04/M05 não iniciadas; assinatura reservada à fase final, trabalhos fiscais adiados/auditoria final/certificação AT não cancelados.


### M03-R1 — Reservas de cobertura, 04-11-2026

[Ordem #46/6627651498](https://github.com/amgsrosario/fac/issues/46#issuecomment-6627651498). Parecer original Claude sobre `97db276`: **APROVADO COM RESERVAS 0/0/2**, SHA-256 `34d9bd8b279705b8bde4c975b5d6495718d169b58ef766bb6e048e7c739453b7`, original/sidecar conferidos e cópia inalterada preservada. Supera a indicação histórica de primeira revisão M03 pendente; não é confirmação da R1.

MINOR-001: descrição do artigo efectivamente alterada e relida; retirada alegação de propagação do mestre às linhas/totais. MINOR-002: PUT/DELETE da linha com leituras persistidas e totais 24,60→30,75→12,30, sem mudar código funcional. [Matriz](../planning/m03-commercial-journey/COVERAGE.md) e [validação R1](../planning/m03-commercial-journey/VALIDATION.md) delimitam os resultados. **Apenas nove casos CommercialJourneyTests, nove aprovados/zero falhas/erros/ignorados**, no HEAD limpo `49b1623d34c1dae4061313a5476bab98cbd08261`, tree `9b33526066b69397db29de1b2c0a2ede19f6238b`, antes/depois iguais. Não somar aos 27 como casos únicos; não repetir as 18 regressões/package, M01/#38.

Arquivo restrito separado `tuuli-backups/m03-r1/2026-11-04/`, 16 ficheiros + manifesto de 3062 bytes, SHA-256 `8c67239595d917a9ccd341b49b2cccb131f8341d190aeb9916b6a5b22d93a378`. Originais M03/87 ficheiros mantidos. PG16.3 sintético/tmpfs e recursos/segredo próprios descartados. Apenas teste/documentação, nenhuma base conservada/Ubuntu/GOLDEN/7ce573f operada. O-1–O-5 Claude e limites M02 mantidos. **Confirmação incremental Claude pendente**, [handoff](../planning/m03-commercial-journey/REVIEW_HANDOFF.md) para transmissão pelo PO. Sem push/PR/merge, contas reais, activação, deploy, cutover ou M04/M05; gates 409, manifesto vazio, FinancialCore inactivo e 1e suspensa.


### M04 — Matriz fiscal independente da assinatura, 04-11-2026

[Ordem #46/6628280906](https://github.com/amgsrosario/fac/issues/46#issuecomment-6628280906). Base confirmada por API/Git/HTTPS `ff9d2ec92e2419cbe6fec9be22d65afa5520e84b`, merge PR #51; `41a73bb` ancestral. **M03 encerrada e integrada**, confirmação Claude 0/0/0 original/sidecar SHA-256 `186a75d90ecd53df62dea5e531a4424fc8c0086b4f2225c130d098f32d87f7de` conferidos. CI PR #51 run 41812144559 safety/package success, não suite PostgreSQL. Supera as indicações anteriores de confirmação M03 pendente. O-6: temporário removido após conferir cópia arquivada, sem alterar evidência. Checkout principal/worktrees preservados.

Branch isolada `codex/m04-fiscal-evidence`. [Matriz M04](../planning/m04-fiscal-evidence/MATRIX.md) liga decisões/fontes/provas/lacunas/assinatura/reservas/próximos passos para FT/FR/RC, QR/PDF/estados e SAF-T. [Fontes](../planning/m04-fiscal-evidence/SOURCES.md): reutilizadas H1.1/H2-03/H2-04/H2-05/M03, sem repetir testes. Hashes dos 50 artefactos H2-03 e 124 H2-05 conferidos; XML H2-05 final 17 classes/286 aprovados são prova anterior, não execução M04. Não foi executado SQL, Docker, testes de aplicação ou exportador.

**FAQ4443 recuperada integralmente** na rota oficial faqs-00084, HTTP 200 em 04-11, SHA-256 `01dccd91844986d3e86c86b68475c870b8038c400649b4e1d4cd514f913ddd47`; antigo 00879 continua404. Lacuna de obtenção da fonte suprida, sem fechar homologação QR/PDF. XSD oficial SAF-T1.04_01 obtido, SHA-256 `292c0ab4611e3f5a0cdf2abb4e62d9bd41254dc2e76a1fae4d35a8b132d79350`. [Contrato SAF-T proposto](../planning/m04-fiscal-evidence/SAFT_CONTRACT.md) separa estrutura/mestres/estados de Hash/HashControl produtivos e não generaliza esses campos a Payments; sem implementação.

Arquivo M04 restrito `tuuli-backups/m04/2026-11-04/`, 6 ficheiros + manifesto de 955 bytes, SHA-256 `1536e33418d9d9bd40d43ef7bb6cb2953663776074f946918536746d93af3468`. Fontes originais, metadados e prova de reutilização preservados, sem dados pessoais/credenciais.

**M04 preparada documentalmente para revisão, não homologada/integrada.** [Handoff Claude](../planning/m04-fiscal-evidence/REVIEW_HANDOFF.md) para transmissão efectiva pelo PO; revisão não presumida.2/1/1 continua pendente de validação fiscal expressa; projecções FT/FR/RC, enquadramento RC/RG, dados/séries/AT e perfil SAF-T mantêm reservas. Assinatura D-SIGN-02..05 reservada à fase final; F01/NC/ND/RC/NL/RL nos mandatos próprios, auditoria final/certificação adiadas e mantidas. Sem código/migrations/manifesto, base conservada/demo/GOLDEN/7ce573f, emissão real, chaves, cutover, push/PR/merge, activação ou M05. Legado autoridade operacional, FinancialCore inactivo e 1e suspensa.


### M04-R1 — Rastreabilidade de HashControl e prova 2/1/1, 04-11-2026

[Ordem #46/6628777871](https://github.com/amgsrosario/fac/issues/46#issuecomment-6628777871). Parecer original Claude sobre `ddd563b`: **APROVADO COM RESERVAS 0/0/2**, SHA-256 `e09a436ba2818678a8fd2a82d94b5b42bd83f5020eb18357ad7201d7f6b888f0`, original/sidecar/cópia recebida conferidos. Supera a indicação de primeira revisão M04 pendente; não é confirmação da R1.

MINOR-001: [M4-14 e fontes N08/E06](../planning/m04-fiscal-evidence/MATRIX.md) passam a citar Portaria363/2010 original, H2-07/CONTRACT e H2-08/DECISIONS com linhas exactas. Signature não expõe versão de chave e não fornece actualmente a origem do HashControl; integração produtiva permanece por demonstrar, sem alterar contratos ou escolher D-SIGN. MINOR-002: [C211/M4-02](../planning/m04-fiscal-evidence/SOURCES.md) separa seed demonstrativo com tuplo completo, FinancialFtOrigin que não lê área/entidade/sinal, extracto que usa sinal e UI que filtra área. **2/1/1 pendente de validação fiscal expressa.**

Exemplar Portaria363 já arquivado na H2-06: 47074 bytes, SHA-256 `6bbfc8e1e4405542c0fa1533964d3ee0d235d363ba3adf0e83414edb2db2a5b3`, conferido e copiado sem alterações. Arquivo separado `tuuli-backups/m04-r1/2026-11-04/`, 4 ficheiros + manifesto 743 bytes, SHA-256 `d5063556d61cc7948388492c145fd00730f147a4c91718c5c8ff8a3aa7b4e805`. Preservados parecer/sidecar e todos os originais M04/FAQ/XSD/evidências anteriores.

**R1 documental preparada, confirmação incremental Claude pendente**, [handoff](../planning/m04-fiscal-evidence/REVIEW_HANDOFF.md) para transmissão efectiva pelo PO. O-1–O-4 registadas como não condicionantes, sem fecho declarado. Nenhum teste/SQL/Docker, código, migration, base, demo, manifesto, assinatura real, activação, push/PR/merge ou M05. Gates fiscais, financeiros, D-SIGN/fase final, legado autoridade operacional e FinancialCore inactivo mantidos.


### M05 — Instalação e recuperação não produtivas, 04-11-2026

[Ordem #46/6629520296](https://github.com/amgsrosario/fac/issues/46#issuecomment-6629520296). Base confirmada API/Git/HTTPS `d17de43f714a18c7f7bb7b1fc719f71d77b4e14f`, merge PR #52/M04; 8b34294 ancestral. M04 aprovada Claude 0/0/0, original/sidecar 0d4a8037fab67059a4d0dcc1f24030345c8324ba171a0b89bbd7343cd6eba658 conferidos na publicação; integração não homologa 2/1/1, projecções ou outras reservas. CI PR #52 run 41819790670 safety/package success, não suite PostgreSQL. Supera o registo anterior de confirmação M04 pendente.

Branch/worktree isolada `codex/m05-isolated-pilot`. [Runbook](../planning/m05-isolated-pilot/RUNBOOK.md), [validação](../planning/m05-isolated-pilot/VALIDATION.md) e [handoff](../planning/m05-isolated-pilot/REVIEW_HANDOFF.md). **47/47 verificações de execução aprovadas**, não casos JUnit, no HEAD limpo 244abddbfd91ee9091becd87a060ab0d81551c33/tree 1f24738f59141c21dc72aa1ff93b06bcb284a5dc, antes/depois iguais. JAR construído em c0968cd e reutilizado por hash/código funcional igual; apenas runner/documentos modificados, nada src/main/migrations/manifesto. Duas tentativas falhadas de harness preservadas, não defeitos de produto nem sucessos ocultos.

Dois clusters PG16.3 novos/tmpfs, CLI M02 local real sem eco, owner de funções não-superuser e operador restrito, conta/admin sintéticos; runtime Java 21.0.13/prod não-root/read-only, HTTP real de jornada/permissões/auditoria. Emissão bloqueada 400 por série sem código AT; criação FT governada 409 por catálogo vazio. **Não é nova prova HTTP 409 de assinatura**: reutilizada M03 sem fabricar código AT ou revisão fiscal. Gates/efeitos financeiros preservados e limite explícito.

Backup sintético 237662 bytes SHA-256 `7a58aa67f8da916a9c58a38082c34266c23d2e11cf3713e994e8924cdcc75e6a`; restauro em cluster distinto preservou 43 tabelas/15 sequences e 86 FKs sem órfãos, validou 29 migrations sem novas alterações, recusou reabrir bootstrap e permitiu login/leitura restaurados. Roles/segredos técnicos/HBA configurados separadamente, sem reset de UUID/comprovativo.

Arquivo `tuuli-backups/m05/2026-11-04/`, 54 ficheiros + manifesto 9438 bytes SHA-256 `b016d9fccaf74b2d19d2b58ca2eded52326914b4ff4d82c6c4878d49d37516b3`; JAR/logs/dump sintético/controlos/runner preservados, cópias conferidas. Recursos próprios/segredos removidos; nenhuma origem/preparação/Ubuntu/GOLDEN/7ce573f operada. **M05 preparada para revisão independente, não piloto comercial pronto**. Sem push/PR/merge, activação ou cutover. Condições reais de instalação, fiscalização/certificação, assinatura/fase final, SAF-T e F01 mantêm mandatos próprios; legado autoridade operacional, FinancialCore inactivo e 1e suspensa.

### M05-R1 — Reprodutibilidade e alcance, 04-11-2026

Ordem #46/6631045763; parecer Claude original/sidecar SHA-256 e511c301b4894aeb5869f98776720bdc81fc0b53afc45f797837428117cb4ffb, APROVADO COM RESERVAS 0/0/3. Hash de cópia executiva diferente, não equiparado ao original. Runner com branch/Maven parametrizados, imagens por digest e runbook de obtenção/descarte. Diagnóstico podeEmitir=true não cobre pré-condições fiscais e coexistiu com emissão400; correcção do produto exige mandato próprio. M03 demonstra409 assinatura apenas MockMvc/test/fixtureSQL; M05 observa400 emissão semAT e409 criaçãoFT semcatálogo. Aceitação da reutilização/novo negativo runtime/adiamento reservados à Administração. O-1–O-6 preservadas. Confirmação independente R1 realizada. Sem activação/publicação/cutover ou bases conservadas.

Ensaio R1 por omissão concluído:48/48 checks (inclui a conclusão do package incremental), HEAD limpo4d4d9e47aa273a34e286ec364b1a4453a4c5d861/tree cee9b134a340a903eb0771398c240ffa59c8eabd. Arquivo separado m05-r1/2026-11-04,35 ficheiros+manifesto SHA-25672a931d520966f8cf9b2a86a25b1dd788af8320c2683e5ee7060a15b4a2b3ddb; recursos/segredos próprios removidos. Não prova execução noutra máquina. Confirmação independente R1 realizada. A decisão sobre a prova de assinatura em runtime permanece pendente.

### M05-R2 — Proveniência do ensaio e do artefacto

A execução R1 `20261104-125327-6ca8de` correu sem argumentos, com Git limpo em `4d4d9e47aa273a34e286ec364b1a4453a4c5d861` (tree `cee9b134a340a903eb0771398c240ffa59c8eabd`). O comando `mvn -o -B -DskipTests package`, resolvido pelo PATH, terminou com sucesso sobre um `target/` existente. O log regista classes actualizadas, testes omitidos e a fase de reempacotamento Spring Boot. O JAR utilizado conservou os mesmos bytes e SHA-256 `a1e73e8f67aeaf813775587b64e69ae9a0230118116fcd0bce8efcad544e8b09` do artefacto produzido em `c0968cd49fc3f76cd9b3c1232a9f36cb0dc61373` (tree `a42ec72f2b25dcceb1b35cd408e53c1433e771db`). Em `jar.json`, `git` identifica o ensaio e `reused_package=false` indica a ausência da opção `--reuse-package`. A prova cobre o package incremental e o percurso de instalação e restauro com o binário existente. Falta uma execução integral de raiz, da compilação com `target/` vazio ao restauro.

[A ordem #46/6633281690](https://github.com/amgsrosario/fac/issues/46#issuecomment-6633281690) regista a confirmação R1 com 0 BLOCKER, 0 MAJOR e 1 MINOR. Os sidecars conferem o parecer inicial vinculativo (`e511c301b4894aeb5869f98776720bdc81fc0b53afc45f797837428117cb4ffb`) e o parecer incremental (`2c66fd9c1a01b548d10cc3c9cbfbe553eb5f0566b24c753a7468c737f390d278`). Segundo a atestação do revisor, `4b54cac3171ecb84627ca0f3d2d1d7baddcdfcb059a43bb76416d62f32014cd8` identifica a primeira versão do parecer inicial, anterior a dois acertos textuais. O-1 a O-8 permanecem registadas. A decisão sobre a prova de assinatura 409 em runtime foi tomada separadamente, conforme o registo abaixo. O parecer independente R2, sobre `22fc4c6`, encerrou a reserva de proveniência e registou um novo MINOR histórico (0 BLOCKER, 0 MAJOR, 1 MINOR). Original e sidecar conferidos, SHA-256 `8fe0f69250ecda47b5a0186e29d11119fdbe51c8234797ce3cd0b954608940ae`. A R3 repõe a frase histórica H2-07-R1 e corrige o estado da confirmação R1 (O-9). Confirmação incremental R3 pendente.

### M05 — Fecho técnico do MINOR-003, 04-11-2026

A [deliberação da Administração AR2](https://github.com/amgsrosario/fac/issues/46#issuecomment-6637180973) encerra tecnicamente o MINOR-003 no cenário sintético `M5N`. O JAR em execução, no ensaio `20261104-165749-6ad7e3` sobre `1d1c63da79340c9df3b6c07dd81dcc8156d68ff7`, devolveu HTTP 409 por ausência de fonte de assinatura. O rascunho permaneceu em `RASCUNHO`, o numerador ficou a zero e os controlos das 43 tabelas e 15 sequências coincidiram antes e depois. A cobertura é específica do tipo sintético sem classificação fiscal `M5N`. A emissão de FT governada e a certificação fiscal ficam fora desta prova.

Claude Code confirmou a evidência por leitura e emitiu APROVADO, 0 BLOCKER, 0 MAJOR e 0 MINOR. O original `2026-11-04_TUULI_M05-MINOR-003_ensaio-assinatura_Claude.md` e o respectivo `.sha256` estão preservados em `Relatorios-AR2/TUULI`. O SHA-256 calculado sobre o original coincide com o sidecar, `962394e0fc752564b705d6c57ce0e4e146f4a2070205168b322e581721dd6b64`. Evidências do executor no arquivo restrito `tuuli-backups/m05-signature/2026-11-04/20261104-165749-6ad7e3/`, manifesto SHA-256 `01f64449965549e99887113ce0b6f4358f69a77b105bd5ea4131eef30a3d0389`. Conferência local concluída. Identificação preparada para registo na issue #46.

**M05 pendente de fecho global.** A prova de instalação e restauro existente usa o binário previamente produzido em `c0968cd`. Falta demonstrar, numa única execução descartável, a compilação com `target/` vazio, o uso do JAR daí produzido e o percurso até ao restauro verificado num segundo destino. Em separado, a Administração deve decidir o significado de `podeEmitir` e a sua apresentação ao operador. No ensaio original, o diagnóstico devolveu `true` com bloqueios vazios e a emissão do mesmo rascunho foi recusada com HTTP 400 por ausência de código AT. A eventual alteração desse sinal exige mandato de produto próprio.

### M05 — Prova integral de raiz, 04-11-2026

A [deliberação AR2 #46/6637438647](https://github.com/amgsrosario/fac/issues/46#issuecomment-6637438647) reconhece o fecho técnico do MINOR-003 em M5N e autoriza a prova integral descartável. A execução `20261104-173954-ef7da4` terminou com 48/48 verificações. HEAD limpo `1d1c63da79340c9df3b6c07dd81dcc8156d68ff7`, tree `54ae587c1af32f2ab91d900e5401cb0b33cb40d5`. Com `target/` inicialmente vazio, Maven 3.9.16 e JDK 21.0.12.1 executaram `mvn -o -B -DskipTests package`. O SHA-256 do novo JAR, `09d39157b41db26526984ec72f6218d828688a08b8b7526dfbc5e49a380f8446`, foi confirmado dentro dos quatro arranques em Temurin 21.0.13. O backup sintético tem SHA-256 `7096274615377b118bac2822a2375c1bc5d080fa4ef5ee1b392b74354ffb8807`. O restauro no segundo PostgreSQL 16.3 confirmou equivalência das 43 tabelas e 15 sequências, 86 FKs validadas e zero órfãos. Recursos próprios, ficheiros privados e worktree descartável removidos.

Evidência restrita em `tuuli-backups/m05-clean/2026-11-04/20261104-173954-ef7da4/`. Manifesto conferido em 37/37 entradas, SHA-256 `d295e03cfe80cc0a8d7411b2973418b8b619aa5f8a95739b2932b38e1761a77c`. O JAR está arquivado e conferido separadamente. Pedido Claude em `tuuli-backups/m05-clean/2026-11-04/PEDIDO_CLAUDE.md`, limitado à cadeia de compilação, JAR, backup, restauro e isolamento. A revisão independente confirmou a cadeia e registou o MINOR-001 documental sobre a preparação e invocação do runner. O fecho global da M05 permanece reservado à Administração.

A deliberação também definiu a regra de produto de `podeEmitir`, superando a pendência de definição registada acima. O valor deve ser verdadeiro quando todas as pré-condições obrigatórias verificáveis na consulta estiverem satisfeitas. A falta conhecida deve produzir falso com motivo compreensível. A validação transaccional continua obrigatória no acto de emitir. A proposta técnica de contrato/API, erros e regressões cabe a mandato próprio. Implementação pendente.

O relatório do executor `tuuli-backups/m05-clean/2026-11-04/2026-11-04_TUULI_M05-prova-integral_relatorio_Codex.md` regista os comandos recuperados e distingue a reconstrução para reprodução. SHA-256 `c80279b6224df9dee399b97cffe66315e256226112db4b008e4648ed03d5dee9`. Parecer original e sidecar conferidos, SHA-256 `146b14b8444a904f6f8a7f2b230288e816d61fcc1906ba9eca2953bb79495f37`. Confirmação incremental do MINOR-001 pendente.

### M05 — Fecho global, 04-11-2026

A Administração AR2 aprovou o fecho técnico global, condicionado à conferência e ao registo versionado agora concluídos. **Estado: `M05_CLOSED`.** Claude Code confirmou o fecho do MINOR-001 da prova integral com 0 BLOCKER, 0 MAJOR e 0 MINOR. O delta revisto, sobre `1d1c63da79340c9df3b6c07dd81dcc8156d68ff7`, corresponde à correcção nos três documentos. SHA-256 de `MINOR-001.delta.diff`: `d17618270d925628fa1d1dc241290d5dbbb19927197e78c3354df52d9f3f752b`. [Deliberação #46/6638075940](https://github.com/amgsrosario/fac/issues/46#issuecomment-6638075940)

Original `2026-11-04_TUULI_M05-prova-integral-MINOR-001_confirmacao-incremental_Claude.md` e sidecar `2026-11-04_TUULI_M05-prova-integral-MINOR-001_confirmacao-incremental_Claude.md.sha256` preservados em `Relatorios-AR2/TUULI`. O SHA-256 calculado sobre o original coincide com o sidecar, `ec77d8f7e7a3a56a264ed4f748eec278f9dd06ba7a560310c6802f83d4e4f1d4`. O MINOR-003 está encerrado no cenário sintético M5N. A implementação de `podeEmitir` segue em mandato próprio. O fecho refere-se à validação técnica da M05 em isolamento.

### M4-02 — FT diferida, 04-11-2026

**Correspondência funcional aprovada** pela Administração AR2 para `2/1/1,false`. A FT diferida constitui dívida do cliente pelo total, mantém esse montante em aberto e não regista recebimento na emissão. Esta decisão actualiza as indicações históricas de correspondência funcional pendente em M04. [Deliberação #46/6638699008](https://github.com/amgsrosario/fac/issues/46#issuecomment-6638699008)

**Prova integrada pendente.** O seed e a leitura dos serviços sustentam partes do critério. O primeiro gate observado é o HTTP 409 ao criar o tipo FT governado sem revisão aprovada disponível. A prova fica dependente de decisão própria para a revisão de ensaio e das pré-condições de emissão. Continuam aplicáveis os gates fiscais, de publicação e integração. [Matriz e evidências M4-02](../planning/m04-fiscal-evidence/MATRIX.md)

### M4-02 — Ensaio integrado autorizado, 04-11-2026

A revisão sintética exclusiva da FT diferida permitiu criar o tipo governado com HTTP 201 e vínculo FT/1. A emissão do rascunho recusou com HTTP 409 por ausência de fonte de assinatura. Rascunho, numerador e estado financeiro conservaram-se. **Correspondência funcional aprovada. Prova integrada pendente.** O requisito de assinatura é o próximo ponto para decisão própria. Evidência e revisão dirigida referenciadas na matriz. [Autorização #46/6638892326](https://github.com/amgsrosario/fac/issues/46#issuecomment-6638892326) · [Matriz M4-02](../planning/m04-fiscal-evidence/MATRIX.md)


### M4-02 — Prova MockMvc positiva, 05-11-2026

**Correspondência funcional aprovada. Prova positiva MockMvc confirmada, com fonte sintética de assinatura exclusivamente de teste.** [Mandato #46/6654412431](https://github.com/amgsrosario/fac/issues/46#issuecomment-6654412431). FT/1 sintética `2/1/1,false` e FiscalSignatureTestConfiguration exclusivamente em testes. Emissão EMITIDO, total12,30, dívida e saldo em aberto12,30, zero recebimentos/afectações/SETTLE. Repetição409 preserva a dívida única. Recusas de assinatura e série sem código conservam snapshots financeiros. Leituras persistidas fora de transacção exterior de teste.

Um teste dirigido aprovado no HEAD limpo `e090a4add3fe084bad2244d2e66b76de6ab63b35`, tree `bd75df3aadd8d456753c340f8ea8db9965f53a62`. O arquivo contém três tentativas anteriores de preparação, logs/XML e resultados. Recursos PostgreSQL16.3 tmpfs próprios removidos e segredos efémeros retirados. Arquivo `tuuli-backups/m4-02-positive/2026-11-05/`,70 ficheiros+manifesto `d0e36a81e967a4c8b35e0ff7188197d11b7637981c6ef3f83ee0d48558e8e08f`. Parecer anterior original e sidecar `2026-11-05_TUULI_M4-02_ensaio-FT-diferida_revisao-independente_Claude.md` conferidos, SHA-256 `2478a7d7c383fc40d232ee35579c3078a8a6f92b03b4f1c170ad0d4b522b3ea8`. [Validação](../planning/m4-02-positive/VALIDATION.md). Os gates fiscais, de publicação e integração permanecem aplicáveis.


### M4-02 — Correcção documental MINOR-001, 05-11-2026

**Correspondência funcional aprovada. Prova positiva MockMvc confirmada. Matérias produtivas pendentes.** O parecer sobre `835c8fc` confirmou os seis critérios no alcance descrito e identificou uma reserva documental na matriz (0 BLOCKER, 0 MAJOR, 1 MINOR). A linha M4-02 explicita a fonte sintética e a dependência de assinatura na emissão. A emissão positiva pelo JAR, a assinatura real e as validações fiscais fora da correspondência aprovada pela deliberação #46/6638699008 continuam pendentes. O original `2026-11-05_TUULI_M4-02-positivo_revisao-independente_Claude.md` coincide com o sidecar, SHA-256 `e212d533b3ef1f75a0fcd5b169c16bdbb733a11117884464c800e18e6d51ab33`. Confirmação incremental da correcção documental pendente. Os seis critérios MockMvc e as evidências anteriores mantêm o alcance reconhecido pelo revisor.


### M4-02 — Aprovação independente R1, 05-11-2026

A confirmação incremental sobre `c24348a8cc8857957e276ed97c679951ed3a6657`, tree `d7dfaae27e2be43429a8ff1ac14b4030359bdecd`, é **APROVADO, 0 BLOCKER, 0 MAJOR e 0 MINOR**. O MINOR-001 documental está encerrado. A prova positiva MockMvc está confirmada com fonte sintética exclusivamente de teste. **Correspondência funcional aprovada. Prova positiva MockMvc confirmada. Matérias produtivas pendentes.** Emissão positiva pelo JAR, assinatura real e validações fiscais exteriores à correspondência aprovada continuam dependentes de mandatos e evidências próprios. [Seguimento #46/6655672950](https://github.com/amgsrosario/fac/issues/46#issuecomment-6655672950)

Original `2026-11-05_TUULI_M4-02-positivo-R1_confirmacao-incremental_Claude.md` e sidecar `2026-11-05_TUULI_M4-02-positivo-R1_confirmacao-incremental_Claude.md.sha256` preservados em `Relatorios-AR2/TUULI` e copiados byte a byte para `tuuli-backups/m4-02-positive-r1/2026-11-05/`. SHA-256 calculado sobre o original e coincidente com o sidecar, `04b22642b57115efaf5a50a234e1eb20d5bda17fc7a4d1da426dd87b7bd048d9`. O-28 tratada na sequência da matriz, com a aprovação funcional explícita, e O-29 tratada no estado da revisão em VALIDATION.md. As restantes observações conservam o alcance do parecer.


### Continuidade M05/M4-02 e M04, 05-11-2026

Base remota consultada em leitura, d17de43f714a18c7f7bb7b1fc719f71d77b4e14f. M05 em 828eefa é ancestral da M4-02 em 43bea3f, que contém os 17 commits necessários. Proposta de entrega única pela sequência consolidada, sem conflito por ancestralidade face à base consultada. Alterações documentais locais da worktree M05 preservadas e já representadas na entrega M4-02, sem reaplicação. A proposta mantém as revisões e arquivos existentes. [Preparação de integração](../planning/continuity-2026-11-05/INTEGRATION.md)

Levantamento actualizado dos 17 requisitos, com provas no respectivo perímetro, lacunas e dependências fiscais/assinatura. M4-10 recomendado como próxima fatia dirigida de leitura/reimpressão e preservação do QR persistido, com mandato próprio. A proposta de podeEmitir descreve contrato actual/aditivo, avaliação pura das pré-condições conhecidas e regressões, preservando a validação transaccional noPOST. Decisões de publicação/integração e execução das próximas fatias reservadas à Administração. [Estado M04](../planning/continuity-2026-11-05/M04_STATUS.md) · [Proposta podeEmitir](../planning/continuity-2026-11-05/PODE_EMITIR_PROPOSAL.md) · [Ordem #46/6656010004](https://github.com/amgsrosario/fac/issues/46#issuecomment-6656010004)


### Continuidade — Correcções documentais para revisão incremental, 05-11-2026

Parecer original `2026-11-05_TUULI_continuidade-M05-M4-02-M04_revisao-independente_Claude.md` conferido com o sidecar, SHA-256 `dfa2be3f749a73ffb26cb7aa288df6ba5fe7d6bde77a3d4454c70eb7b61f9335`, APROVADO COM RESERVAS, 0 BLOCKER, 0 MAJOR e 6 MINOR. A correcção identifica `feature/visual-redesign` como destino proposto e inclui o commit de preparação na lista de entrega. Delimita M4-10 como prova parcial do documento comercial, com transição real para ANULADO e auditoria permitida na reimpressão. Homologação QR/PDF e matérias produtivas continuam pendentes.

A proposta podeEmitir trata a confirmação de recálculo como aviso, define cálculo projectado sem persistência e explicita o limite actual dos métodos que gravam. Mapeia as recusas verificáveis do POST e as exclusões dinâmicas, sem impor um gate de catálogo novo. Define os três consumidores UI, a reavaliação após guardar e a separação entre elegibilidade documental e permissão do actor. Referências, salvaguardas e reservas O-31 a O-39 alinhadas. A confirmação incremental das seis correcções está pendente. A implementação continua dependente dos mandatos próprios.


### Continuidade — Correcções documentais MINOR-007/008, 05-11-2026

O parecer incremental R1 encerrou MINOR-001 a MINOR-006 e identificou duas reservas novas, documentais (0 BLOCKER, 0 MAJOR, 2 MINOR). Original `2026-11-05_TUULI_continuidade-M05-M4-02-M04-R1_confirmacao-incremental_Claude.md` conferido com o sidecar e com o digest comunicado, `8498183e33dcfa736ed59760cdbac9190b4f2ae610033961cff9971577427d3a`. A matriz passa a descrever M4-10 como prova comercial parcial, com fixture apenas na preparação e transição real para ANULADO. A reserva de homologação QR/PDF permanece.

O aviso de recálculo da proposta usa `fiscalRecalculoAviso` persistido ou a activação projectada num legado pelas comparações do POST. Indicador activo exige confirmação mesmo com totais iguais; uma diferença em documento não legado com indicador inactivo não cria a exigência por si. O aviso isolado permite prosseguir para confirmação. O-40 acrescenta a recusa verificável do prefixo ATCUD: e O-41 actualiza o delta cumulativo. Confirmação incremental limitada a MINOR-007/008 pendente. A implementação funcional continua dependente dos mandatos próprios.

## 2026-11-05 — podeEmitir, implementação local após PR #53

Base integrada `5ec635686a15c19f2603ba26f048bc5ab8c10b26`. Mandato #46/6661589044: diagnóstico sem escritas, impedimentos/avisos e totais projectados, confirmação conforme fiscalRecalculoAviso, reavaliação dos três consumidores após gravar. POST e controlos de emissão preservados. Backend 51/51 e browser 11/11 aprovados, em commits distintos identificados em [VALIDATION](../planning/emission-readiness/VALIDATION.md); TypeScript/build/package aprovados com proveniência delimitada. Implementação local, pendente de revisão Claude e gates próprios de publicação. Sem activação, alterações às bases conservadas ou homologação fiscal.

## 2026-11-06 — podeEmitir R1, três achados corrigidos localmente

Parecer inicial sobre 1814d86: REQUEST CHANGES, 0 BLOCKER / 1 MAJOR / 2 MINOR. Original e sidecar iguais, SHA-256 ff8142b6757b57e5551d699b5f5462331eb4373df550b911870c7ca3273a0d61, preservados sem alteração. R1 repõe paragem após novo aviso de recálculo na gravação, limita avaliação/mensagens de emissão a rascunhos e amplia regressões, com limites documentados. Testes afectados: 14 backend em 8b5e341 e 26 browser em a431f81, todos aprovados; TypeScript/build/package aprovados, Git e códigos de saída capturados. Provas anteriores reutilizadas apenas onde aplicável, sem declarar suite integral ou ensaio no HEAD documental final. [Validação e arquivo](../planning/emission-readiness/VALIDATION.md). Confirmação incremental Claude pendente. Branch isolada, sem publicação, PR, merge, activação ou alterações às bases conservadas. POST e gates fiscais preservados.

### M4-10 — Prova comercial parcial após PR #53, 05-11-2026

Mandato #46/6661589044. Base `5ec635686a15c19f2603ba26f048bc5ab8c10b26`. Claude Code: APPROVE, 0/0/0, limitado ao HEAD `217e89fe6ef2f60cb473fd3466867fee7af9b9ca`. Original e sidecar conferidos: SHA-256 `ba7b8bd5acd76ecc0ca28d35a48af8c9108094d18420dcebc462ae52d40ce36f`. Prova HTTP por MockMvc, com fonte sintética; não prova JAR nem emissão produtiva. Anulação pelo POST e QR preservado nas três reimpressões comerciais; apenas auditoria permitida nas tabelas comparadas. Numerador coberto, estado das sequências não demonstrado. Evidências e primeira tentativa preservadas em tuuli-backups/m4-10/2026-11-05. Sem novos ensaios neste fecho. PDF financeiro, homologação QR/PDF, assinatura real e impressão física pendentes. Entrega preparada para decisão de publicação; sem autorização implícita de push, PR, merge ou activação. [Validação](../planning/m4-10-commercial/VALIDATION.md).

## 2026-11-06 — Revalidação Docker e recursos M4-10/podeEmitir

Pendência operacional de acessibilidade Docker encerrada por consulta na mesma distribuição WSL Ubuntu (Ubuntu 26.04.1 LTS). `docker version`, `docker context show` e `docker info` concluíram com código 0: cliente e servidor Engine 29.8.0, Docker Desktop 4.91.0, contexto default. O sandbox recusou inicialmente o socket; a consulta autorizada fora do sandbox teve sucesso, sem alterar permissões ou configuração.

Labels exactas conferidas nos runners: `ar2.mission=M410-commercial` e `ar2.mission=Emission-readiness`. Para cada uma, `docker ps -a` e `docker network ls` filtrados devolveram zero registos, com código 0. Confirmação actual, exclusivamente de leitura; não se eliminaram recursos nem repetiram testes. A nota anterior de indisponibilidade é histórica e fica superada por esta verificação.

Evidência: arquivo reservado `tuuli-backups/docker-revalidation/2026-11-06/results.json`, SHA-256 `1ffc48c0c22ba061c8a8f68350b4e781133a7d8a38720259e0197469203b149f`. Relatório `2026-11-06_TUULI_Docker_revalidacao_Codex.md` em Relatorios-AR2/TUULI, com sidecar. Esta verificação não altera aprovação, âmbito dos ensaios ou gates de publicação, PR, merge e activação.

## 2026-11-06 — podeEmitir R1 aprovado; preparação de publicação

Claude Code: APPROVE, 0 BLOCKER / 0 MAJOR / 0 MINOR, no HEAD `7c96e5819ee2ee8c47f39363675323f611943b73`, tree `e6a227563846659c162185eecf53bf00d001ff8b`. MAJOR-001 e MINOR-001/002 encerrados pela confirmação incremental original `2026-11-06_TUULI_podeEmitir-R1_confirmacao-incremental_Claude.md`, SHA-256 `ec8905c589068024c375285ad9b509f28ca7c815b778bb3a479cabf1eb4ad4a0`, conferido com o sidecar e preservado sem alteração em Relatorios-AR2/TUULI e tuuli-backups/reviews/2026-11-06. A aprovação está limitada ao conteúdo e às provas identificadas, não autoriza publicação, merge ou activação.

Após o parecer, a revalidação Docker já registada encerrou a pendência operacional: cliente/servidor acessíveis, contexto default, zero contentores/redes para `ar2.mission=M410-commercial` e `ar2.mission=Emission-readiness`. Evidência `1ffc48c0c22ba061c8a8f68350b4e781133a7d8a38720259e0197469203b149f`. O parecer conserva fielmente a indisponibilidade observada pelo revisor; a consulta posterior do executor não é uma nova revisão independente.

Melhoria pendente antes de utilização produtiva (O-1/O-2): mostrar explicitamente bruto, IVA e total no diálogo final, inclusive quando o aviso era conhecido antes da gravação ou o operador está no cabeçalho. Recomendação registada, não implementada nesta missão documental; não condicionou o APPROVE recebido. Mantêm-se reservas de assinatura real, disponibilidade dinâmica da fonte, integração browser/backend real e homologação fiscal. Entrega preparada para decisão administrativa de publicação, com deltas documentais posteriores à revisão identificados no relatório conjunto.



## 2026-11-06 — Reconciliação podeEmitir após integração M4-10

[Registo executivo #46/6683385862](https://github.com/amgsrosario/fac/issues/46#issuecomment-6683385862): M4-10 integrada pelo PR #54, merge `41dbe8539df9626db8d941ffd688d8c2e754a420`, incluindo o HEAD autorizado `cc8041c2b3e1fd994e6fa1867c43b92d10e5b7dd`, com CI 42205131937 success. A base remota foi reconfirmada por HTTPS e incorporada por merge local na branch `codex/emission-readiness-pr53`, preservando as duas histórias. Sem merge remoto de podeEmitir.

Conflitos limitados a INSTITUTIONAL_MEMORY.md e OPERATIONAL_STATE.md: preservados os registos de implementação/revisão de podeEmitir e da prova parcial M4-10, com uma única secção de revalidação Docker. O estado corrente substitui as menções históricas a revisão/publicação pendente: M4-10 está integrada; podeEmitir tem APPROVE 0/0/0 em 7c96e58 e continua local, preparada para decisão de publicação sobre novo HEAD exacto. Pareceres originais/sidecars reconferidos: M4-10 `ba7b8bd5acd76ecc0ca28d35a48af8c9108094d18420dcebc462ae52d40ce36f`; podeEmitir R1 `ec8905c589068024c375285ad9b509f28ca7c815b778bb3a479cabf1eb4ad4a0`.

Código funcional de podeEmitir preservado relativamente ao HEAD revisto `7c96e5819ee2ee8c47f39363675323f611943b73`; o merge acrescenta apenas os testes/runner/documentação M4-10 já integrados, além da reconciliação documental. Não se repetiram testes nem se presume revisão independente de um novo código funcional. Relatório de entrega identifica HEAD/tree finais, base e delta do futuro PR. Mantêm-se a melhoria de bruto/IVA/total no diálogo antes de uso produtivo, os limites MockMvc/sintéticos e todos os gates fiscais, de publicação, PR, merge e activação. Bases conservadas, demo, checkout principal e restantes worktrees preservados.

## 2026-11-06 — M4-09: montantes QR da FT diferida/EUR, prova dirigida

Base integrada reconfirmada 5342255e329f3a2ebe6d7b5ad97e9b6f8c09cf07 (PR #55/podeEmitir), posterior à M4-10 integrada no PR #54. Deliberação da Administração AR2 de 06/11/2026, transmitida nesta conversa, AR2-GOV-03; fonte original preservada em sources/2026-11-06_M4-09_deliberacao-administracao.md. Nova branch/worktree codex/m4-09-qr-amounts; registo local df71e68 preservado na worktree anterior, sem reescrita ou incorporação indevida.

Cinco cenários MockMvc com FT/1,2/1/1,false, EUR/tributação normal e fonte FiscalSignatureTestConfiguration exclusivamente de teste: múltiplas linhas, fracções, descontos percentual/em valor e arredondamentos discriminantes. Expectativas anteriores ao ensaio por aritmética racional independente; respostas consolidadas, SQL persistido, snapshots e QR I7/I8/N/O coincidiram. HEAD ensaiado 066a98490a4db43698ef25d937cd1d16e33cd91e, tree be657782f0a1554717944320fda11ed1122d96e1, limpo antes/depois. 5 aprovados, zero falhas/erros/ignorados; nenhuma divergência funcional e nenhuma alteração da aplicação.

PostgreSQL 16.3 novo/tmpfs, rede interna e proxy loopback próprios, removidos com conferência de IDs/labels; consulta posterior ar2.mission=M409-qr-amounts confirmou 0 contentores/0 redes. Arquivo tuuli-backups/m4-09/2026-11-06/20261106-124452-5cbed5, 92 ficheiros conferidos; manifesto original aa6e66af7956d00067f9baf7cf737363a55636ca56f5d9500fcfff4a78d2f5e3 e global 8ee5a6e42b2c9c51af89eb25ae242c470476318be091a1eae5939a22b6d61020. [Validação e limites](../planning/m4-09-qr-amounts/VALIDATION.md).

Revisão independente Claude pendente. A prova técnica dos cinco casos não homologa QR/PDF, taxas/regimes, outros tipos/territórios/moedas ou assinatura produtiva; não demonstra JAR positivo. Não se repetiram suites anteriores. Sem src/main, migrations, manifesto, activação, emissão real, bases conservadas/demo, push, PR ou merge. Bruto/IVA/total no diálogo permanece melhoria pendente antes de utilização produtiva, não implementada aqui.

## 2026-11-06 — M4-09 R1: dois MINOR tratados, confirmação pendente

Parecer original Claude APPROVE WITH CONDITIONS, 0 BLOCKER/0 MAJOR/2 MINOR, sobre 09ddfd6: original e sidecar iguais, SHA-256 d01eb6ef1c9dad0d03d793a5dbd123ff2a6cf9ce3981677471de56f676fdcd16, preservados sem alteração. A prova anterior de 5 casos em 066a984 mantém-se intacta e não foi repetida.

MINOR-001: M409-06 adicional, quantidade 1/preço 1,005/desconto 50%, expectativa independente anterior ao ensaio: bruto 1,01/desconto 0,51/base 0,50/IVA 0,12/total 0,62. Consolidados, persistência e QR concordantes. Execução 20261106-144506-2844e5, HEAD 1528b1f7e70eca43fb7f9da704b7e4e330e19c8c / tree 391899d11863c0bddf2df01729460bf7f912b4d8, limpo antes/depois; apenas 1 teste aprovado, zero falhas/erros/ignorados. A alternativa incorrecta daria desconto 0,50/total 0,63. Sem divergência funcional nem alteração da aplicação.

MINOR-002: repostas nas duas linhas canónicas as reservas não-EUR, isenções, não sujeição, retenção, liquidação imediata e recibos, com proibição de generalizar. As seis expectativas disponíveis resultam de duas execuções distintas (5 anteriores e 1 R1), não de nova suite integral. Arquivo R1 separado, 33 ficheiros, manifesto fc7a901527c14cc7c7a005a4e96ea220c7f07b34c051008f63fcb58eb5ee7080; consulta posterior confirmou 0 contentores/redes da label M409-qr-amounts. [Validação](../planning/m4-09-qr-amounts/VALIDATION.md).

Autorização identificada como **deliberação da Administração**. Textos integrais recebidos preservados, sem reconstrução: [original](sources/2026-11-06_M4-09_deliberacao-administracao.md), SHA-256 bbdec00fa43ba1a2ff167164d438c1627b5a40e6bc32e1ea4083e238c57543d4; [R1](sources/2026-11-06_M4-09-R1_deliberacao-administracao.md), SHA-256 4d89ba3c7585ade0a7ccf13e05c2917da47a9358f617c4af403ba2196f704d0c. Proveniência em metadados separados, sem inventar referência de issue/acto externo. Não foi emitida nova ordem nem solicitada decisão administrativa adicional.

Confirmação incremental Claude pendente, limitada aos dois MINOR. Sem regras novas, homologação fiscal, assinatura produtiva, JAR positivo, bases conservadas, push, PR, merge ou activação. Bruto/IVA/total no diálogo permanece melhoria pré-produtiva pendente, fora desta correcção.

## 2026-11-06 — M4-09: confirmação R1 aprovada e fecho documental local

**APPROVE, 0 BLOCKER / 0 MAJOR / 0 MINOR**, Claude Code sobre a79e86d89ed60981332cfa1261a7d722bd8dc061, tree 305405c943d593955d6bdd680a1400445c8dfa47. Novo original 2026-11-06_TUULI_M4-09-R1_confirmacao-incremental_Claude.md e sidecar conferidos, SHA-256 **921303c688390d528885423dd8c7f37afada2511c2e6271af74ff0835d4bd881**, preservados sem alterações. Não confundir com d01eb6ef…cd16, do parecer inicial 0/0/2. Ambos os MINOR encerrados no alcance técnico parcial.

Provas distintas: cinco casos em 066a984 e só M409-06 em 1528b1f, MockMvc, FT/1, 2/1/1,false, EUR normal sintético de 23%, fonte de assinatura exclusivamente de teste. Não é suite conjunta, JAR positivo, assinatura real, prova PDF/imagem ou homologação fiscal. Reservados não-EUR, isenções, não sujeição, retenção, liquidação imediata e recibos; não generalizar. Nenhum novo ensaio ou alteração funcional neste fecho.

Documentadas a selecção do runner (actual fixa M409-06) e a correlação XML/selection.json; espaços corrigidos. [Proveniência](sources/2026-11-06_M4-09_PROVENANCE.md): ordens executivas recebidas preservadas integralmente, distintas da autorização administrativa anterior mencionada, cujo texto autónomo ainda não foi localizado. Pendência de exemplar original e respectivo hash expressa, sem reconstrução ou atribuição fictícia. Registo consolidado para a issue #46 preparado no dossier, não publicado. A revisão não autenticou a origem administrativa.

Entrega documental local para decisão de publicação sobre HEAD final exacto, com pendência de proveniência administrativa explícita. Sem push, PR, merge, activação ou repetição de testes. Bases conservadas, demo e registo df71e68 preservados. Mantida a melhoria de bruto/IVA/total no diálogo antes de uso produtivo, fora desta tarefa.
