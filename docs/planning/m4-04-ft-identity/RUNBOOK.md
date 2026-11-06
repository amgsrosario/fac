# M4-04 — Reprodução dirigida, só quando autorizada

Não é necessária nova execução para rever o dossier. Reutilizar primeiro logs/XML/PDF e manifestos em backups/test/m4-04/20261106-182822-392888. [Resultados e limites](VALIDATION.md).

## Pré-condições

- Linux/WSL2 com Docker cliente e servidor acessíveis, Maven 3.9.16 no caminho fixado pelo runner, JDK21 e dependências Maven já locais. Runner usa `-o` e não instala dependências.
- Imagens PostgreSQL16.3 e Node20 presentes nos digests da validação; falta de imagem provoca recusa, não download implícito.
- Worktree isolada, branch `codex/m4-04-ft-identity`, limpa. Para reproduzir exactamente a prova original, usar o commit ensaiado b9ce8ea2b4d3abf91595dd679ac2a53286e1942e numa worktree autorizada, respeitando a guarda de branch do runner. Não alterar a worktree conservada para repetir a revisão.
- Sem fornecer credenciais reais, URL de base conservada ou material de assinatura. Credenciais efémeras são geradas pelo runner. Fontes de dados do perfil test são substituídas pelo runner por URL loopback/base recém-criada.

## Comando original

Na worktree isolada:

```sh
python3 docs/planning/m4-04-ft-identity/run_validation.py
```

O runner regista HEAD/tree/status, copia o oráculo antes dos pedidos, exige estado limpo e executa exactamente FtIdentityJourneyTests (um teste). Não selecciona classes M4-02/M4-09/M4-10. Maven/Java compilam a aplicação; nenhum JAR é arrancado. Resultado só é PASS se Maven=0, teste=1 e nenhum ignorado; falhas são preservadas, nunca substituídas.

Recursos próprios: nomes aleatórios `tuuli-m404-*`, label exacta `ar2.mission=M404-ft-identity`, tmpfs PostgreSQL, rede interna e proxy loopback. Guarda de cluster/nome; sem mounts conservados. Finalmente remove apenas IDs/labels criados e a senha efémera. Em interrupção externa que impeça finally, identificar primeiro os IDs no isolation.json e conferir labels; não executar limpezas globais ou remover recursos desconhecidos.

## Conferência sem executar aplicação

Conferir hashes/tamanhos de MANIFEST_SHA256.json e ARCHIVE_MANIFEST.json; comparar git-before/after, command.json, result.json e XML. Associar o método único M404-01 ao oráculo. Comparar expected-before-execution.json com expected.json e com o blob no HEAD ensaiado. Conferir configuração, snapshots, nova leitura HTTP, D/G/H persistidos e extraídos, PDF/texto/render. Q/R são sintéticos e não fazem parte da homologação.

A cópia de preservação não depende de target para sobreviver a builds posteriores. A transcrição de setup em supplement foi feita posteriormente a partir dos comandos recuperáveis desta sessão, distinta dos comandos registados automaticamente pelo runner.
