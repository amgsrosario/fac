Estou a desenvolver o projeto FAC, uma aplicação de faturação simples, realista e certificável, construída com Spring Boot no backend e React no frontend.

Quero dar ao frontend React um posicionamento visual e funcional inovador, sem transformar o FAC num ERP pesado.

## Objetivo principal

Criar uma interface moderna, impactante e diferenciadora, inspirada na ideia de “workspace empresarial”, e não num ERP clássico cheio de menus.

O FAC deve transmitir:

- simplicidade;
- rapidez operacional;
- organização;
- sensação de produto moderno;
- foco na tarefa;
- ausência de complexidade desnecessária.

## Referências visuais analisadas

Foram analisados dois exemplos:

1. InvoiceExpress
    - interface limpa;
    - muito espaço em branco;
    - sensação SaaS moderna;
    - simples, mas por vezes demasiado minimalista.

2. Moloni
    - muito completo;
    - operacionalmente poderoso;
    - mas visualmente pesado;
    - menus extensos;
    - sensação de ERP tradicional.

## Direção pretendida para o FAC

O FAC não deve copiar nenhum dos dois.

A visão é:

> simplicidade visual do InvoiceExpress
> +
> potência operacional escondida do Moloni
> =
> FAC Workspace UI

O FAC deve parecer uma secretária digital de trabalho, onde o utilizador trabalha sobre objetos concretos:

- cliente;
- fatura;
- artigo;
- recibo;
- série;
- documento.

Não quero uma aplicação baseada em menus profundos. Quero uma aplicação baseada em contexto.

## Conceito central

A interface deve organizar-se à volta de um workspace.

Estrutura base:

- sidebar compacta;
- topbar com pesquisa global;
- área central de trabalho;
- separadores inteligentes;
- painel lateral contextual;
- command palette.

## Sidebar

A sidebar deve ser simples e curta:

- Dashboard
- Clientes
- Artigos
- Documentos
- Recebimentos
- Tesouraria
- Configuração

Evitar menus extensos.

A sidebar serve apenas para entrar nos grandes domínios da aplicação.

## Workspace com separadores

Quando o utilizador abre um cliente, uma fatura ou um artigo, deve surgir um separador no workspace.

Exemplo:

[Dashboard] [Cliente: Construções Silva] [FT 2026/123] [Artigo: Consultoria]

Isto deve dar uma sensação semelhante a:

- VS Code;
- browser;
- Figma;
- Notion;
- Linear.

O utilizador deve conseguir alternar rapidamente entre objetos de trabalho.

## Painel lateral contextual

Cada objeto aberto deve ter um painel lateral de ações.

Exemplo para cliente:

- Nova Fatura
- Novo Recibo
- Ver Conta Corrente
- Ver Documentos
- Editar Dados
- Enviar Email

Exemplo para fatura:

- Emitir
- Pré-visualizar PDF
- Enviar por Email
- Duplicar
- Gerar Recibo
- Anular

As ações não devem estar escondidas em menus genéricos. Devem aparecer no contexto certo.

## Command Palette

Adicionar uma command palette, acessível por atalho de teclado e botão na topbar.

Exemplos de comandos:

- Criar fatura
- Procurar cliente
- Abrir artigo
- Emitir recibo
- Ver documentos pendentes
- Abrir série FT
- Ir para configuração ATCUD

A command palette é uma das peças centrais de inovação do FAC.

O utilizador não deve ter de decorar menus. Deve poder escrever o que pretende fazer.

## Experiência por objeto

O FAC deve tratar cada entidade como um dossiê de trabalho.

### Cliente

Ao abrir um cliente, não quero apenas um formulário.

Quero uma ficha organizada com:

- cabeçalho com nome, NIF, estado e saldo;
- dados principais;
- documentos recentes;
- conta corrente;
- moradas;
- contactos;
- ações rápidas.

### Fatura

Ao abrir uma fatura, não quero apenas uma grelha de campos.

Quero um documento operacional:

- cabeçalho do documento;
- cliente;
- linhas;
- totais sempre visíveis;
- estado fiscal;
- ações laterais;
- pré-visualização ou resumo visual.

### Artigo

Ao abrir um artigo, quero uma ficha comercial:

- dados do artigo;
- preço;
- IVA;
- códigos de identificação;
- histórico de utilização;
- estado ativo/inativo.

## Princípio de inovação

A inovação do FAC não está em ter muitas funcionalidades.

Está em:

> fazer poucas funcionalidades essenciais, mas com uma experiência de utilização muito superior.

O FAC deve evitar o erro dos ERPs tradicionais: acumular menus, submenus e ecrãs técnicos.

O objetivo é criar uma aplicação de faturação que pareça moderna, focada e profissional.

## Stack pretendida

Frontend:

- React
- TypeScript
- PrimeReact
- React Router
- Context API ou Zustand, se fizer sentido
- CSS modular ou estrutura simples de estilos

Backend existente:

- Spring Boot
- API REST
- DTOs
- Services
- Repositories
- PostgreSQL

## Pedido ao Codex

Quero que me ajudes a desenhar e implementar a arquitetura inicial do frontend React para esta visão.

Começa por propor:

1. estrutura de pastas;
2. layout principal;
3. componentes base;
4. sistema de sidebar;
5. topbar;
6. workspace com tabs;
7. painel lateral contextual;
8. command palette;
9. exemplo funcional com Cliente e Fatura mockados;
10. recomendações para manter o projeto simples e não deixar escalar.

Muito importante:

Não transformar isto num ERP completo.

Manter o FAC realista, dimensionado e focado em faturação.

A prioridade é criar uma base visual e funcional diferenciadora, moderna e sustentável.