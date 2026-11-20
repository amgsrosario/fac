# RUNNER-01 — Evidências em PostgreSQL descartável

O runner em vigor após integração é `tools/evidence/run_validation.py`. As nove cópias `docs/planning/*/run_validation.py` são históricas e integram a proveniência das evidências anteriores. A execução SAF-T F/FT-EUR R1 de referência usou um wrapper não versionado aplicado ao runner arquivado de 19/11. A Administração aceitou esta reserva na deliberação de 20/11/2026 na issue #46.

Executar de uma worktree dedicada, limpa e sem outros ensaios Maven concorrentes. Disponibilizar Docker local, as duas imagens identificadas por digest no runner, Java e Maven com dependências já em cache. Para SAF-T, disponibilizar o XSD arquivado por `SAFT_XSD_PATH` e as versões fixadas do validador por `PYTHONPATH`. Exemplo para uma missão de percurso comercial:

```sh
python3 tools/evidence/run_validation.py --mission M03 --branch codex/m03-commercial-journey \
  --docker-label m03-commercial --db-prefix tuuli_m03_ CommercialJourneyTests
```

Em WSL, se necessário, usar `--docker-command` com o comando Docker Desktop local devidamente citado. Usar `--maven` para indicar o executável Maven. Executar exclusivamente com classes e binários confiáveis. Os dados de teste têm de ser sintéticos.

O manifesto e sidecar são escritos em `target/evidence-runner/<execução>/`. Um resultado FAIL devolve código diferente de zero. Para verificar o sidecar, executar `sha256sum -c MANIFEST_SHA256.json.sha256` na pasta da execução. Em caso de falha de remoção, consultar os IDs e a label própria da execução em `disposal.json` e conferir a identidade antes de qualquer intervenção manual.
