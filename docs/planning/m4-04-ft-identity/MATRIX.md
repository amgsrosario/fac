# M4-04 — Oráculo fixado antes do ensaio

Base remota integrada `f2fa46765fbd1a2c0ab65d3978f321ef050c3424`, confirmada por ls-remote HTTPS em 06/11/2026. Branch/worktree isolada `codex/m4-04-ft-identity` / `.worktrees/m4-04-ft-identity`. Proposta aprovada `0fcb2d6` preservada por cherry-pick documental, com o fecho pós-PR56 de `bb5ef0e`; nenhum código funcional alterado.

[Ordem integral recebida](../../governance/sources/2026-11-06_M4-04_ordem-execucao.md), [proveniência](../../governance/sources/2026-11-06_M4-04_ordem-execucao.provenance.json). Não foi disponibilizado exemplar separado da deliberação administrativa original; não atribuir à minuta a proveniência de acto original. A ordem recebida autoriza a execução no perímetro fixado.

## Expectativas anteriores à execução

Cenário único **M404-01**; recurso versionado `src/test/resources/m404/expected.json`. Valores literais definidos pelas fontes e pela fixture aprovada, sem chamar formatadores/builders da aplicação para os calcular.

| Elemento | Expectativa | Fundamento / distinção |
| --- | --- | --- |
| Configuração e vínculo | GFT, codigoFiscal FT, FT/1, 2/1/1,false, EUR | D-CAT-2A/H1, ADR-003, aprovação funcional M4-02; identidade interna distinta da fiscal |
| Série | M404A, numerador inicial 0; código sintético SYNM404A | Fixture explicitamente proposta e autorizada; não corresponde a comunicação ou código AT real |
| Sequência emitida | 1 | Série nova sem documento prévio; sem reexecutar concorrência/locks |
| Número completo | GFT M404A/1 | N05, Anexo I §4.1.4.1, código interno/série/sequência |
| ATCUD | SYNM404A-1 | N03 art.3; oito caracteres sintéticos no código, hífen e mesma sequência |
| QR D/G/H persistido e extraído do PDF | FT / GFT M404A/1 / SYNM404A-1 | N04 §4 D/G/H; N05 InvoiceType/InvoiceNo. G não deriva de D; H não leva rótulo ATCUD |
| PDF | Identidade GFT M404A/1, rótulo ATCUD: SYNM404A-1 e descrição configurada preservada | N03 art.4 e critério técnico de igualdade entre representações. Descrição literal não homologada |

[Fundamentos e hashes N03/N04/N05](../m04-fiscal-evidence/M4_04_HANDOFF.md), [proposta completa](../m04-fiscal-evidence/NEXT_INCREMENT.md). Comparação com configuração SQL, snapshots e nova leitura HTTP após emitir, payload persistido, imagem QR embebida no PDF e página renderizada a 300 dpi. PDFBox/ZXing reutilizados da M4-10; uma emissão MockMvc, não JAR.

Cobertura reutilizada: M4-02 configuração/assinatura de teste; M4-09 montantes e capturas D/G/H; M4-10 extracção PDF e preservação. Não repetir estas provas. Um segundo documento não é necessário para esta lacuna: a prova existente de locks/numeração/rollback é reutilizada, sem nova alegação de concorrência.

## Limites

Fonte `FiscalSignatureTestConfiguration` existente, exclusivamente src/test; chave efémera sintética. Controlos normais e segurança activos; revisão FT/1 só na base descartável. Sem homologação QR/PDF, assinatura real, emissão positiva pelo JAR, SAF-T, comunicação AT ou activação. Reservados não-EUR, isenções, não sujeição, retenção, liquidação imediata e recibos; FR/RL/NC/ND excluídos. Melhorias bruto/IVA/total no diálogo e decisões D-SIGN mantidas nos mandatos próprios. Divergência funcional deverá ser reportada sem corrigir a aplicação.

Estado no commit prévio b9ce8ea: cenário e oráculo preparados antes do ensaio.

## Resultado posterior — 20261106-182822-392888

M404-01 aprovado (1/0/0/0), HEAD ensaiado b9ce8ea2b4d3abf91595dd679ac2a53286e1942e, limpo antes/depois. Todas as correspondências da tabela coincidiram em configuração, snapshots, leitura HTTP, QR persistido e PDF/imagem descodificada. Nenhuma divergência funcional; revisão independente pendente. [Validação exacta e arquivos](VALIDATION.md). Mantêm-se integralmente as reservas; uma prova parcial não conclui M4-04 global.
