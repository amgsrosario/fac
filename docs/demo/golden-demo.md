# TUULI AIR — Golden Demo

Guia operacional do ambiente Ubuntu em `/home/arosario/Projetos desenvolvimento/fac`.
Para reposição do cenário aprovado, seguir este guia; recomendações antigas de reset
noutros documentos não substituem o procedimento GOLDEN abaixo.

**Ciclo:** FRESH → revisão humana → GOLDEN → utilização → safety backup automático
→ restore GOLDEN → cenário aprovado novamente.

## Repor a demo

**GOLDEN Restore é o procedimento normal para recuperar uma demo já usada.**
Substitui os dados atuais pelo cenário aprovado; execute numa janela sem utilização
nem backups, resets ou deploys concorrentes.

Pré-requisitos: Docker/Compose v2 acessíveis, serviço `db` healthy no projeto
`fac-demo`, `.env.demo` existente e sem symlink, profile `demo`, database `fac_demo`,
GOLDEN e metadados disponíveis. O script requer `flock`, Python 3, `sha256sum`,
`realpath` e terminal interativo. Nunca contorne as guardas.

```bash
cd "/home/arosario/Projetos desenvolvimento/fac"
./scripts/demo/linux/restore-demo-golden.sh
```

Quando solicitado, escrever **exatamente**:

```text
REPOR fac_demo
```

O script valida ambiente, GOLDEN, metadados, SHA-256 e catálogo PostgreSQL com as
12 tabelas essenciais. Cria e valida um safety backup obrigatório do estado atual
antes de pedir confirmação. Depois para backend/frontend, termina sessões e
recria exclusivamente `fac_demo`, restaura o GOLDEN e inicia a aplicação.
O container PostgreSQL e o volume `fac-demo-postgres-data` são preservados.

O backend valida o histórico Flyway e pode aplicar migrations posteriores se o
código estiver à frente do GOLDEN. O script aguarda healthchecks e executa os
checks oficiais. Usa lock entre reposições GOLDEN, temporário único com limpeza e
logs sem credenciais em `backups/logs/golden-restore.*.log`.

Validação prévia sem backup nem reposição:

```bash
./scripts/demo/linux/restore-demo-golden.sh --dry-run
```

Confirmação manual adicional, embora já executada pelo restore:

```bash
./scripts/demo/linux/status-demo.sh
./scripts/demo/linux/check-demo.sh
```

## Fresh Demo

Comando destrutivo: `./scripts/demo/linux/reset-demo.sh`.

Elimina exclusivamente `fac_demo`, recria a database vazia, aplica migrations
Flyway e executa o seed oficial. Usar para reconstruir o cenário a partir do
código/seed ou provar que a demo nasce do zero, não para reposição habitual.

**Perde as alterações feitas na demo.** Preserva o volume PostgreSQL, mas não cria
safety backup automaticamente. Nunca executar sem backup validado e proteção
adequada do estado atual; respeitar a autorização exigida pelo script.

## GOLDEN current e cenário aprovado

Referências estáveis, fora do repositório:

- Backup: `/home/arosario/tuuli-backups/golden/fac_demo_golden_current.backup`
- Metadados: `/home/arosario/tuuli-backups/golden/fac_demo_golden_current.metadata.json`

São symlinks para um par de ficheiros históricos concretos. Nunca editar o dump.
Não apagar históricos automaticamente. O restore resolve os symlinks e valida os
ficheiros reais; a existência de CURRENT, por si só, não prova integridade.

| Conteúdo aprovado | Estado |
|---|---:|
| Clientes | 5 |
| Artigos | 8 |
| Utilizadores demo | 3 |
| Séries | 2 |
| Documentos comerciais | 6 |
| Documentos financeiros | 2 |
| Flyway | V1–V11 |
| Checks oficiais | OK |

Utilizadores oficiais: `admin.demo`, `operador.demo` e `consulta.demo`.
As passwords do seed vêm de `.env.demo`, através de `FAC_DEMO_PASSWORD_ADMIN`,
`FAC_DEMO_PASSWORD_OPERADOR` e `FAC_DEMO_PASSWORD_CONSULTA`. Não as publicar.
O GOLDEN conserva as credenciais existentes na sua criação; alterar `.env.demo`
não altera as passwords contidas no dump. Contas adicionais criadas depois da
aprovação não reaparecem ao restaurar esse GOLDEN.

## Criar uma nova GOLDEN

A promoção deve ser deliberada: proteger o estado atual, criar Fresh Demo, rever
humanamente, fazer alterações intencionais ao cenário se necessário e confirmar
funcionamento e checks. Depois executar `./scripts/demo/linux/backup-demo.sh`,
validar metadados, checksum, catálogo e tabelas essenciais, e copiar dump e
metadados para o diretório externo `golden`.

Validar também a cópia externa. Só após aprovação atualizar os dois symlinks
CURRENT para o novo par histórico. Preservar originais e GOLDEN anteriores;
nunca promover automaticamente um backup apenas porque é o mais recente.

## Safety backups

Cada restore GOLDEN cria primeiro um backup em `backups/demo/`. Se o backup ou a
sua validação falhar, não avança para DROP. Guardar o caminho e checksum indicados
no relatório/log. O backup precede a confirmação: alterações feitas depois do
snapshot não ficam protegidas por esse backup.

Estes ficheiros estão dentro do checkout, ignorados pelo Git, e podem desaparecer
com `git clean -fdx`. O GOLDEN oficial reside fora do repositório, mas no mesmo
host; copiar backups importantes e respetivos metadados para armazenamento
externo independente. Não executar backups em paralelo com a reposição.

## Atualizar código sem reset

Atualizar código e repor a base demo são operações diferentes. Para instalar uma
versão nova: atualizar Git, reconstruir os containers necessários e preservar
`fac_demo`. O backend pode aplicar migrations normais. Não usar `update-demo.sh`
para uma simples atualização de código: o fluxo atual inclui reset.

## Não fazer

- Executar `docker compose down -v` ou remover manualmente `fac-demo-postgres-data`.
- Executar `git clean -fdx` sem avaliar a perda de backups locais ignorados.
- Usar `update-demo.sh` quando se pretende apenas atualizar código.
- Usar `restore-demo.sh` para restaurar `fac_demo`: permanece reservado a `fac_restore_test`.
- Editar o dump GOLDEN ou apagar backups históricos automaticamente.
- Colocar `.env.demo`, passwords ou tokens no Git.
- Executar reposições, resets, backups ou deploys concorrentes.

## Recuperação de emergência

Se o restore falhar depois do DROP, não executar imediatamente outro reset.
Preservar logs, identificar o safety backup automático e diagnosticar a fase que
falhou, incluindo backend/Flyway, antes de nova operação destrutiva. Não existe
rollback automático; uma falha pode deixar a demo indisponível.

## Histórico do GOLDEN inicial

Aprovado em **2026-09-09**, com PostgreSQL **16.3**, branch
`feature/visual-redesign` e commit da aplicação **`995a14d`**.

SHA-256 inicial:

```text
5637cb6ddbe8a643de4edbe52e8acb8255a5e228a89564db22e1241ed5a1ba4a
```

Futuras GOLDEN terão metadados e checksum próprios. Consultar sempre o par
resolvido por CURRENT; este checksum histórico não deve validar versões futuras.
