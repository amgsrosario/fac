# FAC - Contrato do Frontend Workspace

## Estado do Documento

Versao: 1.0
Data: 08-06-2026
Estado: Ativo
Objetivo: consolidar as decisoes aprendidas no pseudo frontend antes de evoluir para React.

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

O prototipo estatico servido pelo Spring Boot mostrou que a direcao visual e funcional e adequada:

- sidebar curta;
- topbar simples;
- workspace central;
- listas operacionais;
- painel contextual a direita;
- acoes visiveis no contexto certo;
- diagnostico HTML/JSON como ferramenta de conferencia;
- estados visuais nos pendentes.

Esta abordagem aproxima o FAC de uma aplicacao moderna de faturacao sem o transformar num ERP.

---

## 3. Estrutura funcional inicial

Na primeira fase do frontend, os dominios principais devem ser:

- Dashboard;
- Documentos;
- Pendentes;
- Recebimentos;
- Clientes;
- Artigos.

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
- command palette funcional;
- tabs persistentes complexas;
- workflow de aprovacao;
- reporting fiscal definitivo;
- geracao de PDF oficial;
- envio de documentos por email;
- recebimento de pendente sem desenho funcional previo.

Estas funcionalidades podem ter valor no futuro, mas nao sao necessarias para validar a base do produto.

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
