# FR/EUR — circuito interno inicialmente inactivo

## Mandato e perímetro

Fonte: ordem do utilizador/Administração neste chat, recebida em 16/11/2026, sem identificador externo disponibilizado. Implementar FR/EUR: facturação e quitação integral no mesmo documento; recebimento único; saldo zero; vínculos/datas históricos; repetição/concorrência/rollback; preservar FT/RC e segurança. Não publicar configuração nem activar consumidores. A ordem integral encontra-se em `AUTHORIZATION.md`.

Base integrada confirmada pela API GitHub: `f15388d0ac926cc39082869e898d5ef9998059f7` (PR #57). A referência tracking local antiga não foi usada. Preservar os commits documentais da outra branch, incluindo `0337ff1`, sem os incorporar indiscriminadamente.

Fundamentos: D-CAT-2A e homologação semântica H1, em `docs/governance/INSTITUTIONAL_MEMORY.md` e `docs/governance/sources/2026-10-23_D-CAT-2-H1.1_ordem-recebida.md`; ADR-002; V18–V29. FR pertence ao produto; a sua tipologia interna FT/imediata não é, isoladamente, prova de um recebimento. O acto explícito deste serviço declara montante, meio e referência de recebimento e liga-os à factura e ao ledger existente.

## Contrato

`FinancialFrIssuer.issue(key, Request)` é interno, sem endpoint, scheduler ou consumidor operacional; o bean só existe sob o perfil experimental `fr-inactive-lab`, ausente por omissão. A autorização funcional DOCUMENTO_EMITIR e um utilizador autenticado/activo são obrigatórios mesmo numa chamada directa. Não existe actor fornecido no pedido.

O acto usa o emissor comercial existente, mantendo numeração, snapshots, recálculo confirmado, assinatura, EUR e todos os controlos normais. Exige documento RASCUNHO, tipo fiscal interno FT/imediato, recebimento positivo em cêntimos exactos, total integral, meio válido e referência explícita. Dados incongruentes provocam rollback integral. Não cria `DocumentoFinanceiro`, `financial_receipt`, nem Pendente legado.

Na mesma transacção cria uma posição DEBT e a ligação V19, consome-a integralmente por SETTLE/CASH_IN no FinancialCore V18 e persiste o vínculo documental V30. `liquidado=true` só é escrito conjuntamente com esse acto. O valor recebido não é uma segunda posição editável: é a declaração imutável verificada contra documento, posição, afectação e componente de caixa.

A data financeira é a emissão do documento único; o instante da operação de recebimento é `momento_emissao`, diferente da data de efeito. A consulta interna apresenta a origem comercial também na operação de liquidação; o campo de documento financeiro permanece vazio porque não existe RC. As recusas históricas para operações genéricas sem datas aprovadas e FT anuladas mantêm-se.

Idempotência: chave serializada por advisory lock antes da emissão; replay exige documento, montante, meio, referência, confirmação e actor iguais. Chaves diferentes para o mesmo rascunho são serializadas pelo bloqueio documental, e a segunda emissão é recusada. Locks, numeração, auditoria e ledger pertencem à mesma transacção.

V30 valida SQL directo, montante integral, âmbito, actor, transacção criadora, ausência de RC/Pendente, uma afectação e um CASH_IN. Depois do vínculo impede alterações documentais (excepto marca `impresso`), alterações da posição, extensões, segundo recibo e reversões genéricas. **Não implementa anulação de FR**: uma futura anulação exige acto documental/financeiro próprio; a recusa preserva a quitação, em vez de permitir uma reversão avulsa.

## Fronteira fiscal e operacional

Não publica revisão fiscal FR, não altera V28 nem torna FT/imediata admissível no percurso governado de FT diferida. Os ensaios usam configuração sintética FT/imediata fora desse primeiro percurso. Não introduz uma projecção FR no QR/PDF: o emissor existente conserva a projecção actual. Esta entrega demonstra o acto financeiro FR e os seus vínculos, **não homologa identificação externa FR, QR/PDF, SAF-T, assinatura real ou certificação**. Não declarar o documento sintético fiscalmente comercializável. Não altera os circuitos operacionais existentes FT/RC.

## Expectativas fixadas antes dos ensaios

| Cenário | Resultado independente esperado |
| --- | --- |
| Serviço sem perfil experimental | Bean ausente; nenhum consumidor operacional novo |
| 1 unidade × EUR 10,00 + IVA 23% | Documento 12,30; dívida original 12,30; CASH_IN 12,30; uma afectação 12,30; residual 0; nenhum RC/Pendente |
| Data emissão 03/11/2026; operação técnica posterior | CREATE e SETTLE com efeito 03/11; instante técnico conhecido distinto; saldo histórico antes/depois 0 |
| Replay exacto | Mesmos três identificadores; nenhuma segunda caixa/dívida/numeração |
| Chave reutilizada com alteração | Recusa sem alterações |
| Duas chamadas mesma chave | Ambas devolvem mesmo acto persistido |
| Duas chaves, mesmo rascunho | Uma emissão e uma recusa; um único recebimento |
| Permissão/assinatura/código AT ausentes | Recusa; preservar estado e numeração |
| Recebimento 12,29 para total 12,30 | Recusa integral, sem dívida/caixa/documento emitido |
| FT diferida | Recusa neste adaptador; circuito FT anterior preservado |
| Falha na afectação ou no vínculo final | Rollback de documento, numerador, auditoria, origem e caixa |
| SQL altera vínculo/data/liquidado/posição ou acrescenta efeitos | Recusa; reimpressão pode marcar impresso |
| Migração V29→V30 e segundo arranque | 1 migration; mestres preservados; sem backfill/configuração publicada; segundo arranque 0 migrations |

Não generalizar a EUR além deste perímetro, nem a isenções, retenções, multimoeda, RC/RL ou anulações de FT.
