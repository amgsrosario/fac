# Relatorio de ensaios da demonstracao

## Ensaio 1 - Roteiro completo

Objetivo: executar o percurso completo com reset, checks, backup, verificacao e restauro.

Resultado a registar:

- duracao: 4 min 01 s em validacao tecnica local, somando preparacao com reset, backup, verificacao, restauro e checks sobre a base restaurada.
- `FAC_DEMO_CHECK OK`: sim, em `fac_demo` e novamente em `fac_restore_test`.
- `FAC_COMMERCIAL_DEMO_CHECK OK`: sim, em `fac_demo` e novamente em `fac_restore_test`.
- backup: `backups/demo/fac_demo_fac_demo_2026-06-25_165920_1f78d16-dirty.backup`, 104334 bytes, SHA-256 `a2db825451404ab288d355452606ae46dd13985e5fc961094168706cb301f1c3`.
- restauro: `FAC_BACKUP_RESTORE_OK` para `fac_restore_test`, seguido de checks funcional e comercial OK.
- falhas: nenhuma bloqueante; avisos de fontes PDFBox considerados normais no ambiente container.
- hesitacoes: nenhuma no percurso tecnico; o restauro generico exigiu parametros e foi substituido pelo wrapper demo documentado.
- melhorias realizadas: o prepare comercial passou a arrancar backend e frontend antes de anunciar a URL; o logo demo foi trocado para JPEG valido para evitar avisos de imagem no renderer PDF.

## Ensaio 2 - Roteiro curto

Objetivo: demonstrar login, dados mestres, emissao/PDF, pagamento, extrato, auditoria, importacao/exportacao e backup em 7 a 10 minutos.

Resultado a registar:

- duracao: 1 min 18,2 s com base ja preparada, checks funcional/comercial e confirmacao de servicos.
- passos omitidos: reset, backup e restauro; o foco ficou no circuito narrativo e na comprovacao de prontidao.
- mensagens mais eficazes: "perfil demo", "base fac_demo", "6 documentos comerciais", "2 recebimentos", "snapshots fiscais v2" e "auditoria=11".
- riscos encontrados: nenhum; URL final respondeu HTTP 200.
- melhorias realizadas: relatorio passou a separar roteiro completo, roteiro curto e recuperacao para facilitar decisao no dia da apresentacao.

## Ensaio 3 - Roteiro completo com falha simulada

Falha sugerida: parar o servico `db`, confirmar indisponibilidade, recuperar com o script de preparacao e continuar.

Resultado a registar:

- duracao: 1 min 15,5 s desde a execucao do prepare apos parar a base.
- falha simulada: `docker-compose stop db`.
- diagnostico: base indisponivel, aplicacao recuperavel por reexecucao do prepare comercial.
- comando de recuperacao: `scripts/demo/prepare-commercial-demo.ps1 -SkipBackup`.
- tempo ate recuperar: 75,5 s, com `db`, `backend` e `frontend` novamente healthy.
- mensagem usada perante o parceiro: "paramos a base para simular uma falha local; o procedimento sobe o servico, valida a integridade funcional e comercial, e so depois devolve a URL".
- melhorias realizadas: confirmacao final de que apenas `fac_demo` permaneceu entre `fac`, `fac_demo` e `fac_restore_test`.

## Registo de referencia MD31

Este ficheiro deve ser atualizado no dia da demonstracao real com tempos finais. Durante a validacao tecnica do MD31, a evidencia recolhida foi:

- `FAC_DEMO_CHECK OK` em `fac_demo` e `fac_restore_test`.
- `FAC_COMMERCIAL_DEMO_CHECK OK` em `fac_demo` e `fac_restore_test`.
- frontend build OK (`tsc -b` e `vite build`).
- suite Maven OK (`mvn -q test`).
- backup/verificacao/restauro OK.
- confirmacao de que a base `fac` nao foi usada; consulta final devolveu apenas `fac_demo`.
- servicos finais healthy: `db`, `backend`, `frontend`.
