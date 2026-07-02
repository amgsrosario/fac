# Estrutura candidata da UI documental

Data: 2026-07-02

Ambito: laboratorio `/documentos-lab`, nova variante `Estrutura candidata`. Nao houve alteracoes ao backend, base de dados, DTOs, entidades, migrations, contratos REST ou modulo real `/documentos`.

## Estrutura proposta

A estrutura candidata aproxima o laboratorio da futura UI documental de produto:

1. sidebar comercial existente;
2. barra superior compacta da aplicacao;
3. workspace documental;
4. painel LAB recolhivel;
5. conteudo da fase ativa.

## Decisoes de hierarquia

As ferramentas laboratoriais deixam de ocupar espaco fixo. A prioridade visual passa a ser:

1. conteudo da fase;
2. acoes principais do documento;
3. contexto curto do documento;
4. ferramentas de laboratorio.

## Acoes

`Guardar`, `Emitir`, `...` e `LAB` ficam na barra superior global. Na fase 2 candidata, a barra de contexto fica reservada para voltar ao cabecalho, identificar documento/cliente/prazo/moeda, contar linhas e adicionar linha de texto. Isto evita repetir Guardar/Emitir em dois pontos.

## Painel LAB

O painel LAB fica fechado por defeito. Abre pelo botao `LAB` e fecha por `X`, `Escape` ou clique fora do painel. Contem troca de experiencia, densidade, geracao de linhas, metricas, aviso local e reset de estado.

## Fase 1

A fase 1 comeca logo abaixo da barra de aplicacao. Mantem tipo, serie, data, cliente, moeda, pagamento, transporte, morada, armazem por defeito, observacoes, origem dos valores e `Continuar para linhas`. Nao mostra metricas nem ferramentas laboratoriais fora do painel LAB.

## Fase 2

A fase 2 reutiliza a composicao compacta validada, mas no modo candidato fica presa a altura util do viewport. A grelha usa scroll interno coerente, com totais compactos no fundo.

## Medicoes

- `1366x768`, fase 1 LAB fechado: appbar 52 px; LAB fechado; sem overflow horizontal; primeiro conteudo util visivel no primeiro viewport.
- `1366x768`, fase 2 LAB fechado: appbar 52 px; grelha 524 px; 8 linhas visiveis; 21 linhas no total; sem overflow horizontal.
- `1366x768`, fase 2 LAB aberto: appbar 52 px; grelha 524 px; 8 linhas visiveis; painel LAB sobreposto sem ocupar espaco fixo.
- `1440x900`, fase 2: grelha 656 px; 10 linhas visiveis; sem overflow horizontal.
- `1024x768`, fase 2: grelha 524 px; 8 linhas visiveis; sem overflow horizontal.

Comparacao: na experiencia unificada atual, em `1024x768`, o cabecalho/laboratorio permanente mediu cerca de 245 px. Na candidata, a barra global mede 52 px e o workspace da fase 2 comeca a cerca de 112 px.

## Capturas

- `fase-1-lab-fechado-1366x768.png`
- `fase-1-lab-aberto-1366x768.png`
- `fase-2-lab-fechado-1366x768.png`
- `fase-2-lab-aberto-1366x768.png`
- `fase-2-lab-fechado-1440x900.png`
- `fase-2-lab-fechado-1024x768.png`
- `comparacao-antes-unificada-atual-1024x768.png`

## Limitacoes

A variante ainda esta no laboratorio e nao deve ser consolidada em `/documentos` antes de validacao manual.

## Evolucao adicional

- O painel LAB passou a fechar tambem por clique exterior, mantendo o comportamento por `X` e `Escape`.
- A regra responsiva da appbar candidata passou a libertar a altura fixa quando os controlos empilham em viewport estreito; a altura desktop validada de 52 px fica preservada.
