# podeEmitir — implementação e validação

## Mandato e contrato

Ordem #46/6661589044, pontos 2 e 3. Base integrada PR #53: `5ec635686a15c19f2603ba26f048bc5ab8c10b26`. Branch `codex/emission-readiness-pr53`.

O diagnóstico JSON/HTML calcula impedimentos, avisos, total apresentado, total projectado e confirmação de recálculo sem escrever entidades, reservar numeração ou chamar a assinatura. As leituras usam transacção read-only REPEATABLE READ. A elegibilidade é informativa e não substitui os controlos transaccionais do POST de emissão, que permanece inalterado.

EmissionReadinessService reutiliza MotorFiscalService e validadores conhecidos de identidade QR. Discrimina estado, linhas, cronologia, moeda, série/código AT, QR desactivado, ausência/ambiguidade de fontes, empresa/identidade, retenção, cálculo e total. CALCULO_FISCAL_INVALIDO mantém a mensagem concreta. Não cria um gate novo de catálogo.

A confirmação respeita fiscalRecalculoAviso persistido e o cálculo legado usado pelo POST. Uma diferença entre totais é mostrada ao operador; isoladamente não cria confirmação adicional obrigatória. Não há assinatura nem reserva de números durante o diagnóstico. A contagem de fontes não demonstra disponibilidade dinâmica de chave, certificado ou assinatura por documento: essa validação continua no POST.

Os três consumidores são DocumentosView legado, DocumentsView V2 e DraftDocumentEditor. Reavaliam antes de emitir. O editor grava antes de obter nova avaliação; alterações por gravar usam «Guardar e conferir». Confirmação explícita envia recalculoConfirmado. Um diagnóstico desconhecido não equivale a autorização. A interface mostra impedimentos/avisos/diferença de totais, elimina avaliações obsoletas e fecha a sobreposição de detalhe no percurso legado.

## Evidência efectiva

- Backend: execução `20261105-170018-92c768`, HEAD `5327efebd637edce0761457df829ca1830732d7e`, tree `3682c3f8afb2996f073a33f5f58d42c97001a091`, estado limpo antes/depois. 51 testes: EmissionReadinessServiceTests 3, EmissionReadinessJourneyTests 1, DocumentoComercialControllerTests 46, GovernedFtPositiveTests 1. Todos aprovados; zero falhas, erros ou ignorados. Manifesto original SHA-256 `a0ac5030ab5f3f2cbb0d3e6569d01d01c83b9616d356e6241881a8138ea3bae7`.
- Jornada API: leituras persistidas antes/depois do diagnóstico, ausência de assinatura/escritas, total projectado, confirmação persistida/legada, avisos, recusas AT/QR/retenção/cálculo e emissão sintética após confirmação. Não representa uma suite integral nem assinatura produtiva.
- Browser: HEAD `48b7edabd92f1ade1b7e22daabda581ab698b234`, tree `b953e885a90a2b51fb3364af1f3e1af5bd3c92f2`. 11/11 aprovados em browser-release.log: sete casos novos dos três consumidores e quatro regressões fiscal-riva. API simulada; verificam pedidos e confirmação, não integração browser/backend real.
- TypeScript e build frontend aprovados no mesmo HEAD 48b7eda, logs typescript-final.log/frontend-build-final.log. Package backend aprovado em `81f897da1eb35b787037605769187fa90c52c254`, estado limpo documentado; `mvn -o -B -DskipTests package` não executa testes. Java não mudou desde 5327efe. Alterações posteriores foram testes browser e rótulo do editor, não recompilação backend ensaiada.
- Comando backend: `python3 docs/planning/emission-readiness/run_validation.py`. Browser: Node 22.16.0, Playwright 1.63.0, testes emission-readiness.spec.ts e fiscal-riva.spec.ts, Chromium existente. Não foram instaladas dependências. Dependências frontend reutilizadas de outra worktree; avisos de fontes PrimeIcons fora da allowlist e bundle grande não invalidaram os testes, mas não demonstram fidelidade visual das fontes.

PostgreSQL 16.3 novo, tmpfs, rede interna e proxy loopback; dados e contas sintéticos. isolation.json/disposal.json identificam recursos, ausência de volumes conservados e remoção de contentores/redes/segredo temporário. O runner reutiliza nomes internos M03, com label própria Emission-readiness. Origem, demo, GOLDEN e bases conservadas não são alvos.

A primeira tentativa backend desta base foi interrompida por estado Git alterado por artefactos browser; a execução final acima foi limpa. Tentativas browser anteriores detectaram sobreposição do detalhe e expectativas/fixtures antigas; foram preservadas e corrigidas, não somadas aos casos finais. A versão Java ensaiada coincide com a final; o delta posterior é verificável por Git. Os logs originais não foram reconstruídos.

Arquivo reservado: `/home/arosario/tuuli-backups/emission-readiness/2026-11-05/`. Manifesto suplementar e relatório externo identificam ficheiros, tamanhos e hashes; os manifestos originais permanecem intactos.

## Limites e revisão

Sem migration, alteração do POST, manifesto fiscal, assinatura real, activação ou publicação. FinancialCore continua inactivo e o legado mantém autoridade operacional. Não demonstra disponibilidade dinâmica de fornecedor, autorização fiscal, integração produtiva ou todos os erros possíveis de persistência. Revisão independente Claude pendente; pedidos serão encaminhados pelo PO. Não existe auto-aprovação.
