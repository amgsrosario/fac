# FAC - Contrato de Execucao do Frontend Workspace

## Estado do Documento

Versao: 1.0
Data: 08-06-2026
Estado: Ativo
Objetivo: orientar a execucao do frontend real do FAC, usando o pseudo frontend como validacao pratica inicial da visao definida no documento 02.

---

## 0. Relacao com o documento 02

Este documento nao substitui o `02 - FRONTEND-BASILARES.md`.

O documento 02 define a visao aspiracional do frontend FAC:

- React;
- TypeScript;
- PrimeReact;
- React Router;
- workspace empresarial;
- separadores inteligentes;
- painel lateral contextual;
- command palette;
- experiencia por objeto.

Este documento 03 transforma essa visao num contrato de execucao para o frontend real, com uma primeira fase operacional e limites claros para evitar que o FAC se torne um ERP pesado antes de provar o fluxo principal.

A regra e:

- o documento 02 define a direcao;
- o documento 03 define como executar essa direcao por fases, com seguranca e foco.

---

## 1. Principio central

O frontend do FAC nao deve ser um reflexo direto das tabelas da base de dados.

O frontend deve representar trabalho real de faturacao:

- consultar documentos;
- perceber pendentes;
- conferir recebimentos;
- abrir diagnosticos;
- preparar a emissao e a cobranca com seguranca.

Uma entidade so deve ter ecra proprio quando uma PME real a usa frequentemente e com intencao clara.

---

## 2. O que o pseudo frontend provou

O prototipo estatico servido pelo Spring Boot nao e o frontend definitivo.

Ele serviu para testar rapidamente a direcao do documento 02 com dados reais do backend.

Mostrou que a direcao visual e funcional e adequada:

- sidebar curta;
- topbar simples;
- workspace central;
- listas operacionais;
- painel contextual a direita;
- acoes visiveis no contexto certo;
- diagnostico HTML/JSON como ferramenta de conferencia;
- estados visuais nos pendentes.

Esta abordagem aproxima o FAC de uma aplicacao moderna de faturacao sem o transformar num ERP.

O aspeto visual do pseudo frontend deve ser tratado como referencia inicial para o futuro frontend React.

---

## 3. Estrutura funcional inicial

O documento 02 define a sidebar ideal:

- Dashboard;
- Clientes;
- Artigos;
- Documentos;
- Recebimentos;
- Tesouraria;
- Configuracao.

Na primeira fase operacional, nem todos estes dominios precisam de ter funcionalidade completa.

A prioridade deve ser:

- Dashboard;
- Documentos;
- Pendentes;
- Recebimentos.

Clientes e Artigos devem entrar cedo, mas primeiro como consulta e contexto de trabalho, nao como CRUD completo.

Tesouraria e Configuracao devem existir como direcao futura, mas nao devem dominar a primeira fase.

Ficam fora da navegacao principal nesta fase:

- paises;
- moedas;
- regimes de IVA;
- tipos de IVA;
- modos de pagamento;
- prazos de pagamento;
- transportes;
- tipos de documento;
- series;
- parametros;
- empresa;
- armazens.

Estes dados existem no backend, mas devem aparecer como suporte, configuracao ou selecao contextual, nao como ecras principais.

Quando a sidebar React for criada, pode respeitar a estrutura do documento 02, mas com alguns itens inicialmente discretos ou desativados ate terem valor operacional real.

---

## 4. Padrao de ecras

O padrao aprovado para os primeiros ecras e:

- lista principal a esquerda;
- objeto selecionado a direita;
- acoes contextuais junto do objeto;
- diagnostico acessivel sem navegar para menus tecnicos;
- informacao operacional antes de formularios pesados.

Este padrao deve ser usado inicialmente em:

- documentos comerciais;
- pendentes;
- documentos financeiros.

Este padrao e uma versao inicial da experiencia por objeto descrita no documento 02.

Mais tarde, em React, este padrao deve evoluir para:

- tabs reais de objetos abertos;
- painel lateral contextual mais rico;
- acoes rapidas por objeto;
- pesquisa global;
- command palette.

A evolucao deve ser incremental.

---

## 5. Documentos comerciais

O ecra deve permitir:

- listar documentos;
- distinguir rascunho, emitido e anulado;
- ver cliente, data, total e moeda;
- abrir detalhe contextual;
- ver linhas principais;
- abrir diagnostico HTML;
- abrir diagnostico JSON.

Nesta fase, ainda nao deve assumir:

- impressao fiscal definitiva;
- PDF oficial;
- envio por email;
- assinatura fiscal;
- edicao completa via frontend.

---

## 6. Pendentes

O ecra de pendentes e essencial porque liga faturacao a recebimentos.

Deve mostrar estados visuais:

- Em aberto;
- Parcial;
- Liquidado;
- Vencido.

O detalhe deve mostrar:

- documento de origem;
- cliente;
- data do documento;
- data de vencimento;
- valor documento;
- valor pendente;
- valor ja liquidado;
- acesso ao diagnostico do documento comercial.

A acao "receber pendente" fica adiada ate ser desenhada com cuidado, porque cria documento financeiro e altera valores pendentes.

---

## 7. Documentos financeiros

O ecra deve permitir:

- listar documentos financeiros;
- ver cliente, data, modo de pagamento e valor liquido;
- abrir detalhe contextual;
- ver linhas liquidadas;
- abrir diagnostico HTML;
- abrir diagnostico JSON.

Nesta fase, os documentos financeiros continuam a ser criados pela API.

---

## 8. O que nao fazer agora

Para evitar escalar o FAC, nao implementar ainda:

- CRUD completo para todos os catalogos;
- ecras por entidade tecnica;
- permissao avancada;
- dashboards analiticos complexos;
- command palette funcional completa;
- tabs persistentes complexas;
- workflow de aprovacao;
- reporting fiscal definitivo;
- geracao de PDF oficial;
- envio de documentos por email;
- recebimento de pendente sem desenho funcional previo.

Estas funcionalidades podem ter valor no futuro, mas nao sao necessarias para validar a base do produto.

Isto nao invalida a visao do documento 02.

Significa apenas que a visao deve ser implementada por camadas, com cada camada a provar valor real.

---

## 9. React

React continua a ser a direcao pretendida para o frontend definitivo.

Contudo, a entrada em React deve acontecer apenas quando o contrato operacional estiver suficientemente claro.

Quando for criado, o frontend React deve preservar:

- a estetica limpa do pseudo frontend;
- a sidebar curta;
- o workspace por objeto;
- os paineis contextuais;
- a separacao entre consulta e acao;
- o foco no ciclo documento -> pendente -> recebimento.

O pseudo frontend nao e descartavel: serve como referencia visual e funcional.

Stack pretendida para a fase React:

- React;
- TypeScript;
- PrimeReact;
- React Router;
- CSS simples ou modular;
- Context API ou Zustand apenas se houver necessidade concreta.

Zustand, ou qualquer outro gestor de estado, so deve ser adotado se o estado de tabs, objeto aberto, command palette ou sessao de trabalho justificar.

Nao criar arquitetura frontend pesada antecipadamente.

---

## 9.1 Workspace, tabs e command palette

A visao do documento 02 mantem-se:

- o FAC deve parecer uma secretaria digital;
- o utilizador trabalha sobre objetos;
- os menus nao devem ser profundos;
- a command palette deve permitir agir por intencao.

Na primeira fase React, contudo, estas ideias devem entrar com alcance controlado:

- tabs podem comecar como tabs visuais simples;
- painel contextual deve existir desde cedo;
- command palette pode comecar com comandos de navegacao e abertura;
- comandos que alteram dados devem esperar validacoes e seguranca adequadas.

Exemplos de comandos iniciais seguros:

- abrir documentos comerciais;
- abrir pendentes;
- procurar documento;
- abrir cliente;
- abrir artigo;
- ver recebimentos.

Exemplos a adiar:

- emitir fatura;
- anular documento;
- receber pendente;
- enviar email;
- gerar PDF oficial.

---

## 10. Proximo passo recomendado

Antes de criar o frontend React, o proximo passo funcional recomendado e desenhar a acao:

Receber pendente

Questoes a fechar:

- o utilizador escolhe um ou varios pendentes?
- o recibo pode liquidar parcialmente?
- como confirmar modo de pagamento?
- que validacoes devem aparecer antes de emitir?
- o documento financeiro e emitido imediatamente ou deve haver uma revisao previa?

Esta decisao mexe com valores financeiros e nao deve nascer apenas de um botao.

---

## 11. Regra de ouro do frontend

Sempre que houver duvida entre:

- criar mais um ecra;

ou

- melhorar o fluxo principal de faturacao;

a decisao padrao deve ser:

melhorar o fluxo principal de faturacao.

O frontend do FAC deve parecer simples porque a experiencia foi bem desenhada, nao porque faltam funcionalidades importantes.
