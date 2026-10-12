# OP21 — Validação, revisão e gate CI

12-10-2026. Validação local concluída; commit local preparado ao abrigo da OP21, sem push
ou PR desta missão. O hash final deve ser consultado no Git.

## Diagnóstico CI do PR19, exclusivamente de leitura

PR19 merged em feature/visual-redesign, HEAD e8da26b4, merge91ef3c42.
Zero check-runs, zero statuses e zero Actions runs associados a esses hashes.
Inspecionadas todas as18 execuções disponíveis no repositório; nenhuma do PR19.
Actions habilitadas, allowed_actions=all; workflow piloto ativo. PR atualmente
não Draft; timeline prova mudança de base08:45:36UTC e ready_for_review08:49:50UTC,
seguido de merge08:50:05UTC. Não há skip annotation na mensagem do commit.

Workflow usa pull_request com filtrosmain/feature/visual-redesign, sem types,
sem filtro de caminhos e sem condição de Draft. Não foi alterado. A configuração
não inclui ready_for_review ou edited, pelo que estas transições posteriores
não desencadeiam validação. A razão de a abertura inicial não ter produzido run
não ficou demonstrada (incluindo eventual conflito/base anterior).
Não atribuir causa ao Draft isoladamente. Branch consultada protected=false,
sem rulesets de repositório retornados; endpoint de proteção devolveu404.
Nenhuma proteção ou permissão foi alterada.

A [documentação GitHub](https://docs.github.com/en/actions/reference/workflows-and-actions/events-that-trigger-workflows#pull_request)
confirma triggers por omissão opened/synchronize/reopened e ausência de execução
pull_request quando há conflito. A timeline não demonstra, por si só, conflito
na abertura do PR19. Resultado: CI ausente; não verde, não falhado.

Proposta se o PO quiser alterar CI, em aprovação separada: explicitar types para
incluir ready_for_review e edited além dos atuais, avaliando execuções adicionais.
Não executada nesta OP. Para publicação OP21: abrir PR sem conflitos diretamente
contra feature/visual-redesign, verificar a execução apropriada; se continuar
sem CI, obter decisão expressa do PO sobre alternativa. Testes locais não são CI.
A entrega local não constitui autorização nem prontidão para merge.

## Testes e revisão

Primeiro ensaio:67/68; falha em fixture por modo de pagamento inexistente.
Segundo ensaio:68/70; duas fixtures corrigidas (flush transacional para leitura
SQL e emissor ausente no documento histórico). Os dois casos afetados foram
repetidos e aprovados2/2. Nenhuma regra de aplicação ou expectativa foi enfraquecida.
Regressão final:97/97, zero falhas/erros/skips;25 integridade emissão,45
controller,6 matriz de permissões,1 schema,3 integridade referencial,1 upgrade
fiscal histórico,2 PDF financeiro,10 extrato e4 safety.
Regressão do núcleo:22/22, zero falhas/erros/skips (15 integração,2 monetários,
1 upgrade com segunda inicialização e4 safety). Bases de núcleo e documentos
separadas para não contaminar lookups. Package: mvn -o -DskipTests package,
BUILD SUCCESS. Revisão independente por op21_review: APROVAR,
BLOCKER0/MAJOR0/MINOR0; revisão estática de código/testes/migration/documentação
e inspeção dos logs do Executor, sem execução própria de testes pelo revisor.
Nenhuma base persistente/demo foi usada. git diff --check, staged diff e links
locais aprovados. Gate de merge não satisfeito: requer CI apropriado ou
alternativa expressamente aprovada pelo Product Owner.

Logs locais ignorados: target/op21-validation na worktree da missão; relatórios
Surefire e logs são evidência do Executor, não CI nem certificação.
