# FAC - MD31: Fecho e ensaio integral da demonstracao comercial

## Diagnostico inicial

A demonstracao ja tinha uma narrativa operacional coerente desde o MD25: empresa Alentejo Sabores, Lda., tres perfis, cinco clientes, oito artigos, seis faturas, dois recebimentos, documento anulado, documento parcialmente pago, PDF multipagina, extrato e auditoria.

O MD26 consolidou a execucao demo com Docker Compose, servicos privados para backend/PostgreSQL e publicacao apenas do frontend na porta 8088. O MD28 acrescentou configuracao da empresa, logotipo e utilizadores. O MD29 acrescentou importacao/exportacao de dados mestres. O MD30 acrescentou backup, metadados, checksum, verificacao e restauro controlado.

Os pontos em falta eram o fecho documental, um comando de preparacao final e um verificador comercial complementar ao `FAC_DEMO_CHECK`. Nao foi identificada necessidade de migracoes, novas areas funcionais ou redesenho do frontend.

## Implementacao MD31

Criado verificador comercial de leitura:

```text
FAC_COMMERCIAL_DEMO_CHECK OK
```

O verificador confirma a base demo ou restaurada, perfil `demo`, empresa correta com logotipo, tres perfis ativos, documentos, pagamentos, snapshots fiscais v2, ATCUD, QR, auditoria comercial e ausencia da base `fac` no servidor demo.

Criados scripts de preparacao:

```text
scripts/demo/prepare-commercial-demo.ps1
scripts/demo/linux/prepare-commercial-demo.sh
```

Os scripts validam o ambiente, arrancam PostgreSQL, executam `FAC_DEMO_CHECK`, executam `FAC_COMMERCIAL_DEMO_CHECK`, criam backup de referencia salvo opcao contraria e terminam com:

```text
FAC_COMMERCIAL_DEMO_READY
```

## Documentacao criada

- `docs/demo/roteiro-demonstracao-completo.md`
- `docs/demo/roteiro-demonstracao-curto.md`
- `docs/demo/checklist-final-pre-demonstracao.md`
- `docs/demo/plano-contingencia-demonstracao.md`
- `docs/demo/relatorio-ensaios-demonstracao.md`

## Decisao sobre frontend

Nao foi redesenhada a interface. A aplicacao ja identifica `FAC Demo Partner Edition`, mostra a empresa no contexto, esconde Auditoria e Configuracao quando faltam permissoes e tem area propria para Importar/Exportar.

## Limites

Nao foram implementados SAF-T, comunicacao AT, multiempresa, stocks, dashboards avancados, dominio publico, HTTPS, tunel, cloud ou novas integracoes externas.

## Criterio de prontidao

A demo pode ser considerada pronta quando a execucao local confirmar:

- `FAC_DEMO_CHECK OK`
- `FAC_COMMERCIAL_DEMO_CHECK OK`
- frontend build OK
- suite Maven OK
- backup/verificacao/restauro OK
- base `fac` inexistente ou nao utilizada
- tres ensaios registados no relatorio
