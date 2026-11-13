# M4-04 — Dossier para decisão de publicação

13-11-2026. **Fecho documental preparado; publicação e integração não autorizadas por esta entrega.** Branch `codex/m4-04-ft-identity`, worktree `/home/arosario/Projetos/fac/.worktrees/m4-04-ft-identity`.

## Estado efectivamente confirmado

- Base integrada/remota: **f2fa46765fbd1a2c0ab65d3978f321ef050c3424**, PR #56, ls-remote HTTPS em 13-11-2026. Tracking local origin antigo não foi usado para inferir o estado remoto.
- HEAD revisto R2 e de partida desta retoma: **d80ad6a26d5345668ef7ab2461865ce8a6754759**, tree **9e1d92bad80f5650cc51a225303b1738e49056f2**.
- **Nenhuma actividade Git posterior:** HEAD coincidente, zero commits posteriores e worktree limpa antes da actualização. Nenhuma divergência ou trabalho local inesperado incorporado. Novo parecer externo R2 identificado e preservado; arquivos anteriores verificados, sem alteração.
- Nenhuma referência remota `codex/m4-04-ft-identity` encontrada. Não foi feito push, PR ou merge. Sem CI novo nesta entrega; CI histórico do PR #56 não é validação desta branch.
- Checkout principal, docker-compose.yml modificado e .worktrees/ preexistente preservados. Nenhuma base, serviço, demo ou GOLDEN acedido.

## Sequência cumulativa e delta

Seis commits anteriores preservados, mais **um novo commit exclusivamente documental de fecho** nesta entrega:

| Commit anterior | Objectivo |
| --- | --- |
| `3ed0f192b43182b7fdea80d1bd8f48721218702e` | docs: record integrated M4-09 proof and reconcile next M04 increment |
| `d05128150d06d046fe6a6c16aa0cd68538ef1172` | docs: prepare M4-04 identity mandate and traceable expectations |
| `b9ce8ea2b4d3abf91595dd679ac2a53286e1942e` | test: fix M4-04 synthetic FT identity oracle and MockMvc journey |
| `12c252a7bce4d2d94b854b3465ae88e3eca8133e` | docs: record M4-04 identity evidence and prepare independent review |
| `1426a9d7fe2d98f742301b902d000aadeca3341f` | docs: qualify M4-04 ATCUD label analysis and preserve external evidence |
| `d80ad6a26d5345668ef7ab2461865ce8a6754759` | docs: preserve initial M4-04 administrative act for MINOR-001 review |

Os dois primeiros são cherry-picks documentais de bb5ef0e e 0fcb2d6, respectivamente: reconciliação pós-M4-09 e proposta autorizada. Não são novos ensaios ou reabertura M4-09. O único novo teste desta sequência é FtIdentityJourneyTests e o recurso/runner M4-04; não existem alterações a src/main, frontend, pom.xml ou migrations contra a base integrada.

O HEAD/tree finais, a lista exacta dos sete commits, estatística, ficheiros e patch SHA-256 constam do pacote externo final e da entrega, para evitar auto-referência no próprio commit. Delta de publicação proposto: **f2fa467..HEAD-final**; delta documental desta retoma: **d80ad6a..HEAD-final**. Destino proposto `feature/visual-redesign`.

O fecho posterior a d80ad6a **não foi revisto pelo Claude**; limita-se a registar o parecer recebido, acrescentar remissões históricas e preparar este dossier. Confirmar blobs idênticos para aplicação, teste, runner e oráculo, sem atribuir o ensaio ao novo commit.

## Pareceres originais e âmbito exacto

| Parecer | HEAD revisto / resultado | SHA-256 integral, igual ao sidecar |
| --- | --- | --- |
| M4-04 inicial |12c252a; REQUEST CHANGES 0/1/2 |55e89d39dedc2deedb001c9054321f552411c0f35d811cff7f899645b8725d87 |
| R1 |1426a9d; APPROVE WITH CONDITIONS 0/0/1; MAJOR-001 documental e MINOR-002 encerrados |cab9a4d25d25839e5664ea71b50506304a6501bedfa6c73fb7c3d7aadd9e570d |
| **R2** |**d80ad6a; APPROVE 0/0/0, exclusivamente MINOR-001; RESOLVIDO** |**14c26bd42f26cabeb6fc29f20851c18c782fb874ea07c1e3e1f8e21f4de749d6** |

Não restam achados documentais das três revisões. A revisão R2 confirma integridade, completude interna, coerência e separação dos actos; **não autentica autoria, fidelidade independente à mensagem ou anterioridade do acto face ao ensaio**. O acto não tem data original comprovada. A data 06-11-2026 é de recepção/transcrição, não foi convertida em data do acto.

## Três actos separados; remissões sem reescrita

1. [Acto inicial integral](../../governance/sources/2026-11-06_M4-04_autorizacao-administrativa-inicial.md), hash **27cc289e1feffcb6b88200f594a8fbf88ee0349d486db72011f090c35e087540**: mensagem da Administração neste chat, sem identificador externo; inclui o parágrafo após a decisão. [Proveniência](../../governance/sources/2026-11-06_M4-04_autorizacao-administrativa-inicial.provenance.json).
2. [Ordem executiva](../../governance/sources/2026-11-06_M4-04_ordem-execucao.md), hash **f28ef9e133fcc89539359b4fcca471b3385a08a4a690122d1cba11e5a70ba5cb**.
3. [Deliberação posterior R1](../../governance/sources/2026-11-06_M4-04-R1_deliberacao-administrativa.md), hash **e24ed036b9a37c28dd1a8f4e56060811ccd247ad8fae6560a157e06975e50d93**.

As frases antigas de indisponibilidade foram preservadas e receberam remissões datadas para o acto entretanto arquivado e para o fecho R2. REVIEW_PRESERVATION.json também permanece intacto: «initial administrative act not available» é descrição histórica do âmbito daquela preservação, não estado actual. Não se altera um manifesto original para actualizar uma conclusão posterior. O eventual registo do acto na issue #46 requer decisão/publicação próprias; não foi feito aqui.

## Prova técnica confirmada e questão fiscal pendente

**Prova parcial confirmada:** um cenário M404-01, uma classe/um teste aprovado, zero falhas/erros/ignorados, no HEAD **b9ce8ea2b4d3abf91595dd679ac2a53286e1942e**, tree **32eed3ca509fded788c024bc64ced1bd3a25ce9e**, limpo antes/depois; run 20261106-182822-392888. MockMvc/PostgreSQL16.3 descartável, dados e assinatura exclusivamente sintéticos; FT/1, 2/1/1,false/EUR. Interno GFT distinto do fiscal FT; G/número GFT M404A/1 e H/ATCUD SYNM404A-1 concordantes entre configuração, snapshots, nova leitura e QR/PDF. PDF de uma página, QR descodificado da imagem embebida e da página renderizada. [Relatório técnico](VALIDATION.md).

**Questão fiscal ainda aberta:** admissibilidade do espaço após ATCUD: na menção impressa. A [análise documental](R1_ATCUD_ANALYSIS.md) foi aprovada, mas não houve homologação da representação ou autorização para alterar a aplicação/testes. O valor ATCUD/H confirmado é distinto desta questão. Não se presume conformidade nem não conformidade do espaço; não se submete uma alteração fiscal como decidida.

Limites preservados: MockMvc/fonte sintética, sem JAR positivo ou assinatura real, sem comunicação AT/SAF-T ou impressão física; não-EUR, isenções, não sujeição, retenção, liquidação imediata, recibos e FR fora da prova. Descrição literal do cabeçalho não homologada. Id e sequência ambos 1; contains não exclui número adicional; recalculoConfirmado=true com indicador false não prova confirmação de aviso efectivo. M4-02/09/10 não reabertas. Bruto/IVA/total no diálogo continua melhoria pré-produtiva pendente.

## Arquivos e verificações desta entrega

Arquivo original fora da worktree: `/home/arosario/tuuli-backups/m4-04/2026-11-06/20261106-182822-392888/`. Reconfirmação por tamanho/hash: 52 ficheiros do manifesto externo, 35 R1 e 13 R2 íntegros. Estes grupos têm âmbitos diferentes e não são contagem de testes ou ficheiros únicos acumulados.

- Manifesto original da prova: 3d05cac662416fb48119aa046dc2bb609731c31fe3b7996cbbc9ce0fa96821f8.
- ARCHIVE_MANIFEST.json inicial: 12dd223ed5772556ece09f39946acdfcef46dbf0d22f052c0293e733a3a4be10.
- PRESERVATION_MANIFEST.json: edd3557141928ca789396ca0767b74ad1d1e1df529ccf723da38748b8ce73098.
- R1_MANIFEST_SHA256.json: 2945d1e2bd047a965c371cd5ec505cb1d3fb429645bfc78e817647ab59e3e9fa.
- R2_MANIFEST_SHA256.json: b0fea600d37ed96f3c7df6c8acea9d42079b8196448227616e0a1c2054a7e5ae.

O fecho actual terá pacote externo próprio de13-11-2026, sem alterar manifestos anteriores: pareceres/sidecars, lista de commits, HEAD/tree, patch cumulativo e patch documental, digests e relatório. Destino `/home/arosario/tuuli-backups/m4-04/2026-11-13/fecho-documental/`; relatório em Relatorios-AR2/TUULI com sidecar.

Verificações apenas documentais: Git/status/ancestralidade/diff, integridade e referências. **Zero testes, SQL, Docker ou reexecução nesta retoma.** Nenhuma alteração de gates, activação, deploy ou base. Documento preparado para **decisão de publicação pela Administração sobre o HEAD final exacto**, não para merge automático. Manter bases conservadas e FinancialCore inactivo.
