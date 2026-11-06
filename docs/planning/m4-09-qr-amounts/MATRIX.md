# M4-09 — Prova dirigida de montantes QR

Deliberação da Administração AR2, transmitida neste chat em 06/11/2026, AR2-GOV-03. [Texto original preservado](../../governance/sources/2026-11-06_M4-09_deliberacao-administracao.md); [deliberação R1](../../governance/sources/2026-11-06_M4-09-R1_deliberacao-administracao.md). Base remota confirmada `5342255e329f3a2ebe6d7b5ad97e9b6f8c09cf07` (PR #55). Branch/worktree `codex/m4-09-qr-amounts` / `.worktrees/m4-09-qr-amounts`. O registo documental local `df71e68d017c2ee730211c40584fc8ac5f06ef11` permanece intacto na worktree anterior; não é cherry-picked nem apresentado como integrado.

## Cobertura reutilizada, não reexecutada

- `service/MotorMonetarioTests`: HALF_UP por linha, três cêntimos, desconto percentual sobre bruto fechado, desconto em valor e limites/precisão. Unidade, não percurso persistido/QR.
- `service/MotorFiscalServiceTests`: distinção imposto calculado/liquidado, enquadramento e recusas. Unidade; não homologa regimes adicionais nesta missão.
- `service/QrFiscalPayloadBuilderTests`: payload sintético simples (base100/IVA23/total123), identidade e presença de Q/R. Sem múltiplas linhas persistidas.
- `fiscal/catalog/EmissionReadinessJourneyTests`, `GovernedFtPositiveTests` e `CommercialReprintJourneyTests`: preparação FT/1, segurança e emissor sintético reutilizáveis. Não executar essas classes nem repetir M4-02/M4-10.

Acrescenta-se apenas `QrAmountsJourneyTests`: cinco cenários MockMvc de FT diferida/EUR, tributação normal CON já suportada, revisão FT/1 e classificação 2/1/1,false. Fixture derivada da preparação revista existente. Usa a mesma FiscalSignatureTestConfiguration de src/test, sem produção, chaves persistentes ou alteração do manifesto produtivo. A revisão sintética é materializada apenas no PostgreSQL descartável autorizado.

## Oráculo independente

Regra existente: duas casas, HALF_UP por linha, fechar bruto, desconto, base e IVA antes de somar. Fonte contratual já aprovada: `docs/fiscal/op19/CONTRACT.md`, secção monetária, e testes existentes. Não se afirma nova conclusão jurídica ou homologação fiscal. Taxa23 é dado sintético da fixture CON existente; não é uma nova decisão sobre taxas.

`independent_oracles.py` usa números racionais exactos (fractions.Fraction), quociente inteiro para cêntimos e nenhuma importação/chamada Java ou float. Para x≥0, HALF_UP em cêntimos é floor(100x+1/2). Desconto percentual parte do bruto já fechado; desconto em valor é fechado isoladamente. Base=bruto−desconto; IVA=fecho(base×23/100). Total=base+IVA; cabeçalho soma valores fechados das linhas. Não usar o serviço da aplicação para obter resultados esperados.

`src/test/resources/m409/expected.json` é fixado antes do ensaio. Contém entradas, resultados esperados por linha e documento e fracções exactas dos passos intermédios. `--check` exige reprodução byte a byte. Casos 01 e 05 têm também totais literais independentes no gerador. No caso05, cada 0,025 fecha a0,03 e IVA0,0069 fecha a0,01: três linhas dão base0,09/IVA0,03/total0,12. Fechar bruto agregado daria0,08; aplicar IVA à base agregada daria0,02: ambos são discriminados e não são a regra vigente.

| Caso | Cenário | Bruto | Desconto | Base | IVA | Total |
| --- | --- | ---: | ---: | ---: | ---: | ---: |
| M409-01 | multiple-lines | 25.00 | 0.00 | 25.00 | 5.75 | 30.75 |
| M409-02 | fractional-six-decimals | 16.08 | 0.00 | 16.08 | 3.70 | 19.78 |
| M409-03 | percentage-discounts | 21.18 | 1.82 | 19.36 | 4.45 | 23.81 |
| M409-04 | absolute-discount-half-cent | 3.67 | 1.02 | 2.65 | 0.61 | 3.26 |
| M409-05 | line-versus-document-rounding | 0.09 | 0.00 | 0.09 | 0.03 | 0.12 |

## Confrontos e registo

Criar documento/linhas e emitir exclusivamente por MockMvc, com autenticação e controlos normais. Reler documento e linhas após emissão por GET e por SQL de leitura. Comparar valores correntes/consolidados da resposta, snapshots persistidos por linha, cabeçalho e payload QR persistido/retornado: I7=base normal, I8=IVA normal, N=IVA total, O=total. Os restantes grupos monetários e retenção não devem ser introduzidos nestes casos normais. Confirmar FT/1 e configuração não imediata. Arquivar entradas/saídas sem cabeçalhos de autenticação, expectativas, persistência e campos QR.

Não se chama directamente um serviço de emissão para contornar HTTP, não se usa JAR positivo nem se chama fornecedor produtivo. Leitura SQL serve a prova; apenas fixture/pré-condições sintéticas são preparadas directamente. Não corrigir aplicação perante divergência: preservar o resultado e escalar à Administração.

## Limites

Esta prova não cobre outros regimes, taxas, isenções, retenções, territórios, não-EUR, FR/RC/RL/NC/ND, SAF-T, assinatura real, equipamento físico ou homologação QR/PDF. Não altera a regra monetária nem os controlos produtivos. Não activa FinancialCore/RC/1e. Revisão independente Claude pendente. O teste usa perfil test e fonte sintética; não confundir com percurso produtivo pelo JAR.

## R1 — MINOR-001, expectativa fixada antes da execução

Parecer inicial APPROVE WITH CONDITIONS, 0 BLOCKER / 0 MAJOR / 2 MINOR, sobre 09ddfd6. Original/sidecar conferidos, SHA-256 d01eb6ef1c9dad0d03d793a5dbd123ff2a6cf9ce3981677471de56f676fdcd16. Não se alteram as cinco provas anteriores.

Novo caso M409-06: quantidade1 × preço1,005 = bruto cru1,005; HALF_UP fecha bruto1,01. Desconto50% sobre1,01 =0,505, fechado0,51; base0,50; IVA0,50×23%=0,115, fechado0,12; total0,62. Aplicar desconto ao bruto cru daria0,5025, fechado0,50; base0,51 e total0,63. O oráculo racional calcula esta alternativa, em vez de a aceitar por mera referência textual.

| Caso novo | Bruto | Desconto | Base | IVA | Total | QR I7/I8/N/O esperado |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| M409-06 | 1.01 | 0.51 | 0.50 | 0.12 | 0.62 | 0.50 / 0.12 / 0.12 / 0.62 |

As expectativas independentes ficam no mesmo recurso versionado, com os cinco casos anteriores inalterados. Para a R1, o runner fixa M409_CASE_FILTER=M409-06 e exige um único caso executado, zero ignorados. O Java filtra o MethodSource; não se repetem M409-01 a05. A nova execução e a confirmação incremental serão registadas separadamente em VALIDATION.md.

## Resultado R1

M409-06 aprovado isoladamente em 1528b1f, execução 20261106-144506-2844e5: 1 caso, zero falhas/erros/ignorados. Desconto0,51 e QR0,50/0,12/0,12/0,62 confirmados, sem repetir as cinco provas anteriores. [Validação/proveniência completas](VALIDATION.md). Parecer inicial 0/0/2; confirmação incremental pendente. As duas linhas canónicas conservam expressamente não-EUR, isenções, não sujeição, retenção, liquidação imediata e recibos, sem generalização.
