# Regras de trabalho do Codex no projeto FAC

## Objetivo geral

Executar alterações de forma focada, incremental e verificável, evitando
análises desnecessariamente amplas do repositório.

## Âmbito da análise

1. Antes de começar, identificar os ficheiros estritamente necessários.
2. Não analisar todo o repositório, salvo pedido expresso.
3. Não abrir pastas ou módulos sem relação direta com a tarefa.
4. Quando o âmbito não estiver claro, perguntar antes de expandir a análise.
5. Reutilizar o contexto já obtido na tarefa e evitar reler ficheiros
   sem necessidade.

## Execução das tarefas

1. Dividir tarefas extensas em etapas pequenas e verificáveis.
2. Implementar apenas o que foi solicitado.
3. Não executar refatorizações laterais ou melhorias não pedidas.
4. Não alterar ficheiros fora do âmbito identificado.
5. Antes de modificar vários módulos, apresentar um plano breve.
6. Depois de cada etapa, resumir:
    - ficheiros alterados;
    - alterações realizadas;
    - testes executados;
    - pontos ainda pendentes.

## Testes e comandos

1. Executar primeiro os testes diretamente relacionados com a alteração.
2. Não executar toda a bateria de testes sem necessidade ou pedido expresso.
3. Não repetir comandos que já tenham produzido resultado válido.
4. Não instalar dependências sem justificar previamente.
5. Não executar comandos destrutivos ou irreversíveis sem autorização.

## Gestão do contexto

1. Evitar respostas excessivamente longas.
2. Não reproduzir ficheiros completos quando bastar apresentar a alteração.
3. Não repetir explicações já dadas.
4. Quando uma tarefa começar a tornar-se demasiado ampla, parar e propor
   a sua divisão em novas tarefas.
5. Para alterações independentes, recomendar uma nova tarefa em vez de
   prolongar indefinidamente a tarefa atual.

## Segurança

1. Trabalhar apenas dentro da pasta do projeto.
2. Não aceder a outras pastas do computador sem autorização.
3. Não utilizar acesso à rede salvo necessidade concreta e autorizada.
4. Não modificar configurações globais do sistema.

## Regra final

Privilegiar precisão, controlo e alterações mínimas em vez de uma análise
global ou de uma implementação excessivamente abrangente.