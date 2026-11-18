# FR/EUR — implementação inactiva da identidade fiscal

Mandato técnico recebido em 18/11/2026 neste chat: implementar/testar o plano aprovado `36c6aadf45ec4bd54a6c937b9cb8e997f6e92197`, em branch isolada, circuito inactivo e C6 aberto; entregar código/commit/evidências ao Claude. Ordem posterior inclui as validações fiscais já autorizadas independentes da FR, sem decidir P12. Publicação/integração/activação permanecem reservadas. Base integrada reconfirmada: `e697391` (PR #58); a branch nova parte do plano aprovado, preservado por ancestralidade.

## Desenho e expectativas anteriores aos ensaios

- Manifesto produtivo vazio; schema v1 FT diferida continua válido. Schema v2 descreve tipologia FT, especialização FR, externalType FR, título Fatura-recibo e combinações explícitas. Sem revisões/pointers reais publicados. Só fixtures sintéticas.
- GovernedFrConfiguration e FinancialFrIssuer sob `fr-inactive-lab`, sem endpoint/consumidor operacional. A criação governada é explícita; CRUD ordinário de FT imediata continua sem auto-vínculo (C6). `issueProjected` exige percurso projectado; `issue` conserva o ensaio económico anterior, sem converter por fallback.
- Documento persiste projecção/revisão antes de numerar/assinar. DTO mantém construtores anteriores e separa tipologia interna de FiscalProjectionDto. QR D e título vêm desta identidade, G/H dos mesmos valores gravados. PDF mostra quitação e número persistido. Commit diferido exige o acto V30 completo; emissão ordinária de nova FR governada é recusada.
- V31 aditiva, sem modificar V1–V30, dados ou históricos. Código interno tem identidade externa durável, também entre tabelas. Guarda Java de pré-validação e SQL independente; origens financeiras antigas sem prova não são classificadas por inferência. Registos de identidade sobrevivem à remoção de configurações.
- Séries FR novas têm identidade externa própria e recusam colisões/reciclagem de série/código AT. Chaves de serialização versionadas evitam validar snapshots RR obsoletos; não há outro ledger financeiro.
- Oráculo sintético: código FRE, série TESTFR, tipo interno FT, projecção FR; número FRE TESTFR/1, ATCUD TESTFR01-1; 1×10,00 +23%=12,30, um CASH_IN12,30, residual0, nenhum RC. O código AT é apenas de teste, sem comunicação AT. Replay devolve o mesmo acto; faltas/falhas fazem rollback integral. Histórico e FT/RC não são reclassificados.

## Fronteiras

C6 continua aberto: novas FT imediatas sem vínculo podem seguir o `/emitir` legado com D=FT e sem quitação. I04(b) será prova dessa preservação, não resolução/prontidão do produto. Anulação FR continua recusada; não se muda a temporalidade da anulação FT. SAF-T avança apenas no contrato das origens expostas (FiscalProjectionDto, snapshots e acto de pagamento); não há XML/exportador, HashControl inventado, escolha de perfil P12 ou assinatura real. ATCUD/QR e anulações comerciais já existentes entram nas regressões dirigidas, sem reabrir homologação. Nunca activar o perfil em instalação operacional.
