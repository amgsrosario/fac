# Deploy não destrutivo no Ubuntu

Este é o fluxo oficial para atualizar a TUULI AIR Demo Partner sem repor a base de dados. A fonte de verdade é a branch `feature/visual-redesign` no GitHub.

## Fluxo normal

No portátil HP:

1. desenvolver e testar;
2. criar commits focados;
3. publicar em `origin/feature/visual-redesign`.

No Ubuntu, dentro do checkout existente:

```bash
git switch feature/visual-redesign
./scripts/demo/linux/deploy-demo.sh
```

O Ubuntu recebe alterações publicadas. Não é usado para desenvolver, criar commits ou fazer push.

## O que o script faz

O `deploy-demo.sh`:

- exige a branch `feature/visual-redesign`;
- bloqueia alterações tracked, staged ou unstaged;
- reporta e preserva ficheiros untracked;
- executa `git fetch origin` e aceita apenas um avanço direto;
- cria e valida um backup de `fac_demo` em `/home/arosario/tuuli-backups/predeploy/`;
- executa `git pull --ff-only origin feature/visual-redesign`;
- reconstrói `backend` e `frontend`, preserva a base de dados e atualiza os serviços com Docker Compose sem remover volumes;
- aguarda pelos healthchecks dos três serviços;
- compara contagens funcionais antes e depois, aceitando contagens iguais a zero;
- apresenta o HEAD e o caminho do backup usados no deploy.

O check funcional associado a uma demo seeded não corre por defeito. Para o pedir explicitamente:

```bash
./scripts/demo/linux/deploy-demo.sh --check-demo
```

## Dry run

```bash
./scripts/demo/linux/deploy-demo.sh --dry-run
```

O modo dry-run mostra branch, estado tracked, HEAD, referência remota conhecida, commits pendentes, serviços a reconstruir e destino do backup. Não faz fetch, pull, backup ou alterações Docker.

## O que não faz

O deploy normal nunca executa automaticamente:

- `git reset`, `git stash` ou `git clean`;
- commits ou push no Ubuntu;
- remoção de volumes ou `docker compose down -v`;
- seed, loaders integrais ou reposição da demo;
- restore, golden restore ou alteração do golden;
- `update-demo.sh` ou `reset-demo.sh`.

`update-demo.sh` e `reset-demo.sh` são fluxos destrutivos de reconstrução/reposição e não fazem parte do deploy normal da demo personalizada.

## Falhas e diagnóstico

Uma falha no backup termina o processo antes do pull e do rebuild. Uma falha de healthcheck apresenta `docker compose ps` e os logs recentes dos serviços, sem tentar corrigir dados através de reset.

Para operação inicial, restauro e contingência, consultar [manual-operacional-ubuntu.md](manual-operacional-ubuntu.md) e [guia-backup-restauro.md](../operacao/guia-backup-restauro.md).
