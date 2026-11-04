# M05 — Runbook do ensaio temporário não produtivo

Mandato #46/6629520296. Base d17de43f714a18c7f7bb7b1fc719f71d77b4e14f/PR #52. Branch/worktree codex/m05-isolated-pilot. Este executor só cria dados sintéticos e recursos descartáveis próprios; não recebe nome/endereço/snapshot de instalação conservada. Não é deploy ou procedimento autorizado de cutover comercial.

## Preparação

Ler AGENTS, memória/estado, M01–M04 e o contrato M05 em MISSIONS.md. Confirmar branch e worktree limpa e fixar commit/tree antes da execução. Reutilizar as provas M01/#38 de preparação/restauro e M02/M03 de mecanismos; não repetir essas suites. Package novo é necessário para ensaiar o artefacto exacto da M05.

Requisitos: Docker e Maven/JDK21 existentes. Imagens locais postgres:16.3 e node:20-alpine; eclipse-temurin:21-jre oficial obtida especificamente para a CLI/JAR deste ensaio, sem instalar componentes no host. Registar ID/digest/versão efectiva, não inferir versão exacta da tag. Não usar latest implícito nem pull durante o runner.

A CLI M02 recusa autorização cujo caminho tenha ancestrais de dono diferente de root/utilizador ou permissões inseguras. Neste executor /home e /run apresentam dono nobody; não mudar a guarda nem permissões do host. Executar a CLI num contentor com tmpfs privado root0700 em /run/m05, autorização0600 de dono root e JAR read-only. É uma consola local efémera, não endpoint remoto ou runner Spring. A aplicação funciona em contentor distinto, UID10001 e rootfs read-only.

## Executar e observar

`python3 docs/planning/m05-isolated-pilot/run_pilot.py`

1. Runner exige branch M05 limpa, captura HEAD/tree/status e constrói JAR offline com testes omitidos; guarda hash/log. Cria PostgreSQL A novo em tmpfs/rede interna, recusa IDs dos clusters conservados, não monta dados persistentes nem publica5432. Proxy HTTP apenas127.0.0.1; HBA restringe TCP da base ao loopback. Nenhuma rede/contentor alheio é alterado.
2. Role migradora/dona não-superuser, runtime com DML sem CREATE/ownership/bootstrap e operador temporário só com EXECUTE nas duas funções M02. Fase local separada de aplicação aplica migrations; a aplicação é parada antes do provisionamento. Runtime posterior usa só credencial m05_app e Flyway desactivada, JPA validate preservado. Credenciais de migração não ficam no runtime corrente.
3. CLI real do JAR via PropertiesLauncher e PTY local: confirmar UUID e ler passwords sem eco. Não gravar transcript do terminal nem valores das passwords; registar apenas resultado/quantidade de prompts/no-echo e comprovativo/auditoria. Usar ADMINISTRADOR sintético, @example.test, nunca conta real. Retirar autorização/container e revogar grants/LOGIN/password do operador após consumo.
4. Arranque da aplicação com perfil prod para comprovar configuração de segurança, mas exclusivamente instalação sintética não operacional. HTTP real de health/login/perfis/CRUD/rascunho/consulta/exportação/auditoria. Nenhuma emissão bem-sucedida, fornecedor fiscal ou revisão. Tipo não classificado de teste e série sem código AT não são configuração fiscal homologada.
5. Não fabricar código AT para atingir a assinatura: emissão deve recusar400 na barreira anterior de código ausente; criar FT governada com catálogo vazio deve recusar409. Reutilizar a prova específica de409 por ausência de assinatura da M03, com a limitação explícita de que não é nova emissão HTTP M05. Registar causa/estado/numerador e ausência dos efeitos financeiros abrangidos, sem converter erro de pré-condição em alegado teste de assinatura.
6. Parar aplicação A, medir contagens/digests de todas as tabelas e estados das sequences, verificar FKs e validar ausência de movimentos/revisões/códigos AT. Gerar backup custom exclusivamente sintético; hash e tamanho, sem pg_dumpall/roles globais/segredos em texto. Arquivo permanece restrito.
7. Criar cluster B independente, roles/segredos novos e grants mínimos necessários; pg_restore transaccional conserva ownership apropriado. Comparar todas as tabelas/sequences e FKs antes de login ou escritas. Arranque de validação Flyway deve manter dados e não aplicar migrations novas. Runtime B com role restrita deve autenticar e ler o rascunho; comprovativo M02 restaurado deve recusar novo primeiro administrador. Não resetar UUID/comprovativo ou criar conta de recuperação por SQL.
8. Remover exclusivamente IDs/labels M05 registados, em ordem inversa; retirar ficheiros secretos efémeros. Guardar originais/logs/JSON/backup/manifesta e código exacto em target/m05-evidence e arquivo restrito independente de target. Se falhar, preservar tentativa e parar dependências; não corrigir bases conservadas ou alterar produto para esconder falha de fixture.

## Interpretação

Resultado efectivo: [VALIDATION.md](VALIDATION.md), execução 47/47 verificações aprovadas no perímetro delimitado. Não tratar checks do runner como casos JUnit ou suite integral. Digests pré/pós-restauro são comparados antes do login B, pois login escreve legitimamente auditoria/último acesso. Role/passwords/HBA/JWT são configuração externa do ensaio, não segredos transportados pelo backup; recuperação comercial requer custódia/autorização próprias.

Mesmo com êxito, permanecem emitente/regime/identidades comerciais, séries legítimas/comunicação AT, homologação2/1/1 e projecções FT/FR/RC, RC/RG, perfil/implementação SAF-T, assinatura/certificação e ordens de activação/deploy. Base limpa não elimina F01, autoridade única, anulações e recuperação futuras. Não iniciar essas matérias, novos consumidores, Fatia1e ou promoção de GOLDEN. Ubuntu/origem/preparação conservada/worktree7ce573f ficam fora do ensaio. Revisão independente Claude e decisão da Administração precedem qualquer publicação ou integração.

## Repetição governada e identificação do artefacto

Na primeira tentativa, package foi aprovado em `c0968cd49fc3f76cd9b3c1232a9f36cb0dc61373`. A prontidão inicial PG por socket Unix foi insuficiente; runner corrigido para TCP127.0.0.1. Uma tentativa seguinte mostrou que o JAR600 não era legível pelo UID10001; só o modo do artefacto compilado passou a0644, mantendo os bytes. Código funcional não foi alterado. Logs/manifestos das três tentativas preservados.

Para uma continuação nesta mesma worktree, o executor admite `--reuse-package <directório-da-tentativa>` apenas dentro de target/m05-evidence: exige hash do JAR e identidade original limpa, mais diff vazio em src/frontend/pom. O ensaio final usou `--reuse-package 20261104-114708-4295c0`. Não usar esta opção se houver alteração funcional ou falta do artefacto/proveniência. Não confundir metadados do ensaio actual com o commit original do build. Um build posterior pode ter hash diferente; reprodutibilidade dos passos não equivale a build binário reproduzível.

Recuperação aqui é backup custom e restauro transaccional em **cluster distinto**, com roles existentes recriadas legitimamente e segredos técnicos novos. Não existe procedimento automático de produção nem reset do comprovativo. Os identificadores do cluster/BD mudam; o UUID/comprovativo da instalação é preservado pelo backup. Os grants globais, HBA e passwords não são transportados por pg_dump; são configuração explícita do destino. Não copiar credenciais ou reabrir bootstrap para resolver falhas.

Imagens oficiais ficam na cache Docker; não são removidas globalmente. Todos os contentores/redes criados pelo ensaio foram removidos; as imagens/cache não contêm credenciais deste ensaio. Nenhum ficheiro secreto ou transcript CLI persistido no arquivo. O JAR apenas contém a aplicação e dependências normais, não passwords/autorizações do piloto.

## R1 — Reprodução noutro executor e remoção manual

Requer Linux x86_64, Python3 stdlib, Docker, JDK21 e Maven disponível em PATH. Alternativas explícitas: `M05_MAVEN=/caminho/bin/mvn` ou `--maven /caminho/bin/mvn`; `M05_BRANCH=codex/outra-branch` ou `--branch codex/outra-branch`. Continua obrigatório HEAD identificado, branch codex/ esperada e worktree limpa. O script não instala ferramentas nem altera configurações globais. Maven corre offline: preparar previamente a cache de dependências aprovada; se faltar, abortar e registar a dependência, sem activar downloads implícitos. O percurso por omissão constrói o JAR, sem --reuse-package.

Obter as imagens oficiais antes do ensaio, numa máquina com rede autorizada (ou transferir com docker save/load e conferir os mesmos digests); o runner usa --pull=never:

```bash
docker pull postgres@sha256:d0f363f8366fbc3f52d172c6e76bc27151c3d643b870e1062b4e8bfe65baf609
docker pull node@sha256:fb4cd12c85ee03686f6af5362a0b0d56d50c58a04632e6c0fb8363f609372293
docker pull eclipse-temurin@sha256:0b99b3376fbb50dbf9c0af8535fdd5b8eb0d3fdfc8b1e4967f383b6919f6901a
python3 docs/planning/m05-isolated-pilot/run_pilot.py
```

São digests fixos da plataforma ensaiada; outra arquitectura exige validação própria, não substituição automática de imagem. Execução noutra máquina não foi realizada; a R1 demonstra o comando por omissão neste executor descartável, sem caminho Maven fixo ou reutilização de package.

Se SIGKILL/interrupção impedir finally: não executar prune ou remoção por nomes genéricos. Identificar o sufixo exclusivo da tentativa em target/m05-evidence/<timestamp-sufixo>, confrontar isolation.json e listar candidatos com `docker ps -a --filter label=ar2.mission=M05 --no-trunc` e `docker network ls --filter label=ar2.mission=M05 --no-trunc`. Inspeccionar cada ID com `docker inspect <ID>`: confirmar nome tuuli-m05-<sufixo>-*, labels ar2.mission=M05/ar2.disposable=true, IDs PG/proxy arquivados e ausência de mounts conservados. Contentores transitórios sem ID persistido exigem confirmar nome, label, imagem e namespace exclusivo desta tentativa; perante dúvida, não remover. Remover apenas IDs assim conferidos, primeiro apps/CLI/proxy, depois PG, por `docker rm -f <ID-conferido>`. Redes: conferir label, sufixo e Containers vazio por `docker network inspect <ID>`, depois `docker network rm <ID-conferido>`. Não usar substituição automática de todos os recursos M05, pois pode haver outra execução concorrente.

Só depois de terminar os processos próprios, conferir que target/m05-private/<timestamp-sufixo> é directório real desta worktree (sem symlinks); apagar exclusivamente os .env regulares dessa tentativa e remover esse directório vazio. Não imprimir o conteúdo. Autorizações da CLI vivem no tmpfs do contentor removido. Preservar target/m05-evidence e os originais; copiar para arquivo restrito com manifesto/hash antes de qualquer limpeza futura de target. Remoção de ficheiros não prova eliminação física de memória/disco. Não apagar imagens ou volumes globais.

## R1 — Observabilidade e alcance das recusas

O diagnóstico do rascunho responde 200 e `podeEmitir=true`, bloqueios vazios; a emissão do mesmo documento recusa 400 por ausência do código AT. O diagnóstico actual avalia estado/linhas/totais, não todas as pré-condições fiscais: não é autorização de emissão nem prova de prontidão. Registar esta divergência como condição aberta; corrigir o diagnóstico/UI exige mandato próprio.

Sinais disponíveis: health UP (apenas prontidão, detalhes desligados), respostas HTTP e permissões, diagnóstico JSON, mensagens dos gates, logs de arranque/runtime, comprovativo/auditoria CLI e auditoria documental, contagens financeiras/numeradores, digests e FKs de restauro, Git e manifests. Logs foram recolhidos, não submetidos a asserções automatizadas de ausência de erros; não há métricas, alertas, tracing, TLS ou observabilidade comercial demonstrados. Auditoria visível não prova imutabilidade da role runtime; O-1–O-6 do parecer mantêm-se não condicionantes e não resolvidas nesta R1.

M03: 409 de assinatura em MockMvc/perfil test, fixture SQL com marcador de série sintético; igualdade de fonte sustenta reutilização da lógica, não prova runtime prod. M05: emissão sem código AT=400; criação de tipo FT com catálogo vazio=409, operação distinta. Não foi observado 409 de assinatura no JAR em runtime. Administração deve decidir entre aceitar a prova reutilizada com limites, autorizar negativo adicional em runtime descartável com marcador sintético, ou adiar para assinatura; nenhuma opção escolhida aqui.
