# Pedido Claude — revisão independente M4-04

**Estado: preparado para transmissão pela Coordenação; não transmitido automaticamente.** Rever a prova parcial, não homologar fiscalidade ou reabrir M4-02/09/10.

## Identidade

- Base integrada f2fa46765fbd1a2c0ab65d3978f321ef050c3424 (PR #56).
- Proposta autorizada 0fcb2d6d4c185eaee9c04a990d9a42f285869669; cherry-picks documentais 3ed0f19 e d051281 preservam a preparação na nova base.
- Branch codex/m4-04-ft-identity; worktree /home/arosario/Projetos/fac/.worktrees/m4-04-ft-identity.
- **HEAD ensaiado b9ce8ea2b4d3abf91595dd679ac2a53286e1942e; tree 32eed3ca509fded788c024bc64ced1bd3a25ce9e; limpo antes/depois**.
- HEAD final documental exacto será identificado na entrega/pedido externo; confirmar blobs idênticos para teste, runner e oráculo relativamente ao HEAD ensaiado. Não atribuir a execução ao commit posterior.

## Fontes, alcance e evidência

[Mandato e oráculo](MATRIX.md), [relatório técnico](VALIDATION.md), [runbook](RUNBOOK.md), [proposta e fundamentos](../m04-fiscal-evidence/NEXT_INCREMENT.md), [fontes N03/N04/N05 e hashes](../m04-fiscal-evidence/M4_04_HANDOFF.md). Ordem executiva integral recebida e proveniência em docs/governance/sources/2026-11-06_M4-04_ordem-execucao.*. Não foi disponibilizado exemplar separado da deliberação administrativa original: não afirmar que a minuta é essa fonte.

Arquivo original/preservado: target/m404-qr-evidence/20261106-182822-392888 e backups/test/m4-04/20261106-182822-392888, nesta worktree. Manifesto original SHA-256 **3d05cac662416fb48119aa046dc2bb609731c31fe3b7996cbbc9ce0fa96821f8**; ARCHIVE_MANIFEST.json **12dd223ed5772556ece09f39946acdfcef46dbf0d22f052c0293e733a3a4be10**. 38 ficheiros originais; cópia de 45 ficheiros incluindo manifesto original/suplemento, mais manifesto da cópia. Conferir sem modificar fontes ou ensaios.

Um caso MockMvc aprovado, zero falhas/erros/ignorados, Maven0: interno GFT, fiscal FT, revisão FT/1, 2/1/1,false/EUR, série M404A, número GFT M404A/1, ATCUD SYNM404A-1. Catálogo/código de validação/assinatura exclusivamente sintéticos. PDF de uma página extraído/descodificado da imagem e da página renderizada a 300 dpi, com D/G/H iguais ao oráculo/persistência. Não confundir tipologia FT com prefixo interno GFT ou rótulo visual ATCUD com valor H.

## Conferências prioritárias

1. Confirmar oráculos anteriores à execução, significado N05 do prefixo interno, remissões N04 e composição N03; distinguir regra, fixture e homologação.
2. Rever teste e logs/XML/pedidos/persistência/PDF. Confirmar uma classe/um caso e asserções efectivas; não apresentar a suite anterior como reexecutada.
3. Verificar que o PDF/QR inspeccionado é o retornado pelo endpoint normal, com snapshots persistidos após emitir e sem invocar directamente serviços de emissão.
4. Conferir HEAD/tree/status, hashes e cópia de preservação; isolamento de cluster/rede/tmpfs e remoção exacta. Não operar sobre bases conservadas nem repetir ensaio sem risco concreto.
5. Verificar ausência de src/main/frontend/pom/migrations alterados, reservas e coerência de matriz/memória/estado. Exemplar administrativo separado não recebido é limite de proveniência expresso, não reconstruído.

Emitir APPROVE / APPROVE WITH CONDITIONS / REQUEST CHANGES, BLOCKER/MAJOR/MINOR com localização concreta, limites e SHA-256 do parecer original. Sem alterações de ficheiros, bases, Git, push, PR, merge ou activação. Revisor não substitui autorização administrativa de publicação nem homologação fiscal.
