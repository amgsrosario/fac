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
OB §11 reserva arquitectura, matriz completa e política ao PO; D-CAT-2A abaixo
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
ou implementação técnica do catálogo. D-CAT-1 (arquitectura) e D-CAT-3
(versionamento/transição) continuam pendentes de ratificação expressa.
