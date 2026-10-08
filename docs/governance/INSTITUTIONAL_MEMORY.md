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
