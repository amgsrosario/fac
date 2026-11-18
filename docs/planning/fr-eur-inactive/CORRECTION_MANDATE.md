# FR/EUR — proveniência da decisão funcional de correcção

Fonte primária: mensagens da Administração e do executor disponibilizadas neste chat Codex Desktop, sem identificador externo disponível. Registo em 18/11/2026. Os excertos abaixo preservam as determinações relevantes e a instrução que escolheu o SETTLE.

## Determinações da Administração

> **AR2 | TUULI AIR — Ordem ao Codex Desktop para executar o incremento FR**
>
> Parte do merge `7008ad8ebdc56967b6403888c0e868a06d0ce802`, em branch isolada. Confere no código e nos testes se a quitação já produz um recibo associado, identificável e anulável. Reutiliza o que cumprir a regra aprovada e corrige o que faltar.
>
> 1. **Emissão.** Na mesma transacção, regista a facturação e gera automaticamente o recibo associado. A conta corrente deve apresentar os dois efeitos separadamente, com resultado equivalente ao de uma FT seguida do respectivo recibo e com uma única quitação.
> 2. **Anulação.** Recusa a anulação da FR enquanto o recibo associado estiver activo. Exige primeiro a anulação lógica do recibo e só depois permite anular a componente de facturação. Preserva a identidade fiscal e os registos originais, ligando cada correcção ao acto que lhe deu origem, com motivo, utilizador e instante. Se não houve entrada efectiva de fundos, a anulação do recibo não gera `CASH_OUT` nem devolução.
>
> A data de efeito permanece pendente apenas para as consultas históricas afectadas. Prossegue com o restante trabalho e torna explícita essa limitação nas consultas, sem adoptar uma data por omissão.

A delimitação anterior também exclui a devolução de dinheiro efectivamente recebido e mantém o circuito inactivo. A ordem R1 reafirma: «A regra funcional está decidida.»

## Instrução de execução que adoptou o SETTLE

> **Codex Desktop:** parte de `7008ad8` em branch isolada e reutiliza o `SETTLE` ligado à FR como registo do recibo associado. Mostra a facturação, a quitação e as respectivas anulações em movimentos separados no extracto. Exige a anulação lógica do recibo antes de permitir a anulação da FR, com registo auditável e sem gerar `CASH_OUT` quando não houve entrada de fundos.
>
> Testa em PostgreSQL descartável a sequência completa, a concorrência, a repetição sem duplicação, o rollback e as regressões FT/RC. Mantém a FR inactiva e deixa a data de efeito em aberto apenas nas consultas históricas afectadas. Entrega o commit e as provas ao **Claude Code** para revisão independente.

## Escolhas de implementação

A confirmação expressa de que os fundos nunca foram recebidos é exigida a um operador autenticado com permissão de anulação. A aceitação da declaração é um controlo do adaptador inactivo. O sistema preserva motivo, utilizador e instante e não infere ausência de fundos da factura, nem consulta uma fonte externa de tesouraria. As duas reversões usam referências e chaves estáveis vinculadas ao acto inicial, com guardas V32 e auditoria equivalente à dos percursos existentes. A data técnica de registo não fixa a futura data de efeito.
