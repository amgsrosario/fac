# Fonte institucional — deliberação da Administração AR2

Autoridade: Administração AR2, identificada expressamente pela ordem R1. Data de recepção: 06/11/2026. Origem: mensagem integral disponibilizada pelo utilizador neste chat TUULI AIR; não é reconstrução de histórico privado nem transcrição de uma fonte GitHub não disponibilizada. Arquivo técnico: Codex. Os metadados são separados do texto recebido, preservado integralmente abaixo. A referência «autorização administrativa acima» pertence ao original; não se inventa outro identificador de acto.

---

## Ordem ao Codex Desktop — Executar M4-09

Executa a **M4-09 — Prova dirigida dos montantes QR da FT diferida em EUR**, conforme a autorização administrativa acima e a AR2-GOV-03.

1. Confirma a base integrada e preserva o registo documental local `df71e68`. Trabalha numa branch/worktree isolada, fixando a base, o HEAD ensaiado e o delta.
2. Levanta a cobertura existente e acrescenta apenas os cenários necessários: múltiplas linhas, preços e quantidades fraccionários, descontos aplicáveis e arredondamentos discriminantes.
3. Define os resultados esperados por cálculo independente do código da aplicação. Confronta-os com os valores consolidados, os valores persistidos e os campos monetários correspondentes do payload QR.
4. Executa exclusivamente por **MockMvc**, em PostgreSQL descartável, com dados sintéticos, revisão `FT/1`, classificação `2/1/1,false` e a fonte de assinatura exclusivamente de teste existente. Mantém os controlos normais activos.
5. Arquiva matriz, cálculos esperados, resultados, comandos, versões, proveniência Git e manifestos SHA-256. Comprova o isolamento e a remoção dos recursos; preserva separadamente eventuais tentativas falhadas.
6. Actualiza M4-09, memória institucional, estado e handoff. Prepara o pedido de revisão independente Claude Code, identificando exactamente o código ensaiado e o HEAD final.

**Se surgir uma divergência funcional, documenta-a sem corrigir a aplicação e apresenta o impedimento à Administração.**

**Sem alterações funcionais ou fiscais, assinatura produtiva, emissão positiva pelo JAR, bases conservadas, push, PR, merge ou activação.**
