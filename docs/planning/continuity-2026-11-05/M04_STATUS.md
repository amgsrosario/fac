# M04 — Quadro reconciliado dos 17 requisitos, 06-11-2026

Referência remota confirmada: `f2fa46765fbd1a2c0ab65d3978f321ef050c3424`, merge PR #56 (M4-09), conforme [#46/6690486932](https://github.com/amgsrosario/fac/issues/46#issuecomment-6690486932). Integrações anteriores preservadas: PR #54/M4-10 (`41dbe85`) e PR #55/podeEmitir (`5342255`). «Integrado» é estado Git, não homologação ou autorização operacional. Actualização M4-04: um novo cenário dirigido MockMvc foi executado no HEAD b9ce8ea, com revisão pendente; as provas anteriores continuam reutilizadas nos seus HEADs originais.

[Fontes e matriz original](../m04-fiscal-evidence/MATRIX.md), N01–N08/E01–E06/C211. Não se infere que uma reserva significa ausência de decisão funcional; as decisões já aprovadas mantêm-se vinculativas.

| Requisito | Estado comprovado no perímetro | Lacuna / reserva efectiva | Seguimento e dependência |
| --- | --- | --- | --- |
| M4-01 Semântica FT/FR/RC | Decisões D-CAT-2A/H1 aprovadas | Projecções fiscais e circuitos próprios não integralmente homologados | Não reabrir conceitos; prova e mandato por tipo |
| M4-02 FT diferida | Correspondência funcional 2/1/1,false aprovada; seis critérios MockMvc com fonte sintética confirmados; entrega consolidada PR #53 | Emissão positiva pelo JAR, assinatura real e validações fiscais exteriores à correspondência | Mandato produtivo próprio; não extrapolar a prova sintética |
| M4-03 Vínculos/imutabilidade | V25–V28/H2-03 integradas; coerência Java/SQL e histórico preservados | Definição material legítima e publicação governada das revisões | Não publicar catálogo ou reclassificar por efeito das provas |
| M4-04 Projecção FT/FR | **Prova parcial FT diferida/EUR executada; R1 documental pendente após parecer inicial 0/1/2.** M404-01, 1 aprovado em b9ce8ea, MockMvc/fonte sintética, configuração/snapshots/número/ATCUD/QR/PDF concordantes | **Menção ATCUD com espaço: validação fiscal pendente, sem presumir não conformidade; valor ATCUD/H favorável preservado.** FR/FT imediata, SAF-T, assinatura real e homologação QR/PDF não demonstrados | [Pedido Claude e evidências](../m4-04-ft-identity/R1_REVIEW_HANDOFF.md), sem reabrir M4-02/09/10 |
| M4-05 Séries/ATCUD | Locks/concatenação/rollback e recusas já testados; podeEmitir integrado explica pré-condições conhecidas | Associação legítima emitente/série/código AT e comunicação real | Gate AT e mandato de instalação; diagnóstico não comunica séries |
| M4-06 ATCUD multipágina | H2-05/E03, PDFs sintéticos de 1/2/4 páginas | Configuração/equipamento final e históricos sem ATCUD | Reutilizar provas; não repetir correcção entregue |
| M4-07 Geometria QR | E03, métricas/descodificação PDF a 300 dpi e smoke ZXing | Impressão física e configuração final | Geometria ensaiada não é homologação fiscal/produtiva |
| M4-08 Q/R | Derivação/identidade sintética e recusas 409 demonstradas; diagnóstico não chama assinatura | Fonte real, custódia, persistência, cadeia e certificação | D-SIGN reservado à fase final; podeEmitir=true não garante disponibilidade dinâmica |
| M4-09 Montantes/regimes QR | **Prova parcial integrada no PR #56**, Claude 0/0/0 sobre a79e86d: 5 casos em 066a984 e 1 em 1528b1f, MockMvc/fonte sintética; valores consolidados/persistidos/QR concordantes | **Reservados: não-EUR, isenções, não sujeição, retenção, liquidação imediata e recibos.** Não generalizar; requisito completo permanece parcialmente demonstrado | Não repetir os seis cenários nem reabrir os MINOR encerrados; restante perímetro só em mandato próprio |
| M4-10 Estados/reimpressão | **Prova parcial comercial integrada no PR #54**: POST de anulação e três PDFs com payload preservado; auditoria delimitada; Claude 0/0/0 | PDF financeiro, assinatura real, impressão física e homologação QR/PDF; sequências não integralmente demonstradas | Não repetir a prova concluída nem chamar completa à linha M4-10 |
| M4-11 RC/RG/Data Valor | Data Valor e contratos implementados; novo circuito inactivo | Regime do emitente e projecção fiscal RC/RG | Não inferir enquadramento nem substituir emissão por Data Valor fiscal; sem activação |
| M4-12 SAF-T estrutura | XSD oficial 1.04_01 e contrato documental proposto | Perfil/TaxAccountingBasis, tabelas, perímetro e contrato aprovados; exportador não demonstrado nestas entregas | Decisão material de perfil antes de exportador; não produzir SAF-T comercial parcial |
| M4-13 SAF-T mestres/totais | Entradas/fontes identificadas; CSV/XLSX não são SAF-T | Mapeamento, obrigatoriedades, referências, snapshots e totalizadores exactos | Depende de M4-12 e mandato de implementação |
| M4-14 Hash/HashControl | Lacuna de origem/versionamento rastreada em M04-R1 | Assinatura integral e versão autêntica da chave persistidas por documento | D-SIGN/fase final; sem defaults ou inferência da chave/certificado actual |
| M4-15 Payments | XSD e distinção RC/RG conhecidos; sem exigência genérica de Hash/HashControl directo em Payments | Perfil/regime, vínculos, datas, totais e exportador | Enquadramento e mandato próprios; sem activar recebimentos |
| M4-16 Estados SAF-T/anulação | QR comercial preservado na prova parcial; estado exportado é contrato distinto da temporalidade financeira | Oráculos por tabela/estado, datas/motivos/totais e exportação | Perfil SAF-T primeiro; temporalidade FT/F01 continua reservada |
| M4-17 Instalação/certificação | M01–M04 integradas; M05/M4-02 consolidadas no PR #53; M4-10/podeEmitir/M4-09 parciais nos PR #54–#56 | Emitente/contas/séries legítimos, assinatura real, release/revisões fiscais, auditoria final, AT e melhoria do diálogo | Integração não é prontidão comercial ou certificação; gates preservados |

## Seguimento M4-04

A proposta [NEXT_INCREMENT.md](../m04-fiscal-evidence/NEXT_INCREMENT.md), fixada em0fcb2d6, foi autorizada por ordem posterior recebida neste chat. Foi executado apenas M404-01, com oráculos normativos/funcionais fixados antes, ID interno GFT distinto do tipo fiscal FT, PostgreSQL descartável, assinatura exclusivamente de teste e extracção QR do PDF real retornado por MockMvc. [Validação](../m4-04-ft-identity/VALIDATION.md):1/0/0/0, HEAD b9ce8ea, recurso removido, revisão independente pendente.

Próximo passo é a revisão independente deste delta, não nova execução ou activação. A prova não homologa projecções ou SAF-T e não reabre decisões semânticas. Mandatos futuros permanecem necessários para os âmbitos reservados. Não se apresenta a existência de fontes como aprovação do enquadramento do emitente.

## Melhoria pré-produtiva separada

**Mostrar bruto, IVA e total no diálogo final**, inclusive aviso de recálculo já conhecido e operador no cabeçalho. Continua pendente antes de utilização produtiva, fora desta tarefa documental e da prova fiscal proposta. Não condicionou o parecer técnico recebido, não foi implementada e não se presume autorização para a implementar.

## Provas integradas e rastreabilidade

- M4-09: PR #56, merge f2fa46765fbd1a2c0ab65d3978f321ef050c3424, HEAD autorizado 5c5b34403cb00e0190f810209cbc29091b68653f, CI 42244362450 success (safety/package). Parecer incremental SHA-256 921303c688390d528885423dd8c7f37afada2511c2e6271af74ff0835d4bd881; [validação](../m4-09-qr-amounts/VALIDATION.md). Seis cenários em duas execuções, não suite conjunta.
- M4-10: PR #54, merge 41dbe8539df9626db8d941ffd688d8c2e754a420; parecer ba7b8bd5acd76ecc0ca28d35a48af8c9108094d18420dcebc462ae52d40ce36f; [validação](../m4-10-commercial/VALIDATION.md).
- podeEmitir: PR #55, merge 5342255e329f3a2ebe6d7b5ad97e9b6f8c09cf07; parecer ec8905c589068024c375285ad9b509f28ca7c815b778bb3a479cabf1eb4ad4a0; [validação](../emission-readiness/VALIDATION.md). Informação de elegibilidade, não garantia de fonte dinâmica ou autorização fiscal.

O registo local df71e68 mantém-se intacto na worktree anterior. Esta reconciliação actualiza o quadro corrente sem apresentar aquele commit como integrado. Sem testes, novas regras, código funcional, bases conservadas, push, PR, merge ou activação.

### M4-04 R1 documental

Parecer inicial Claude REQUEST CHANGES 0/1/2 conferido com sidecar; fonte original preservada. [Análise da menção ATCUD](../m4-04-ft-identity/R1_ATCUD_ANALYSIS.md) separa valor/H confirmados e formato gráfico em validação fiscal. Deliberação R1 arquivada com hash/proveniência; arquivo externo concluído. Sem novo ensaio, alteração de expectativas/testes/aplicação ou homologação. Confirmação incremental pendente.
