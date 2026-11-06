# M04 — Quadro reconciliado dos 17 requisitos, 06-11-2026

Referência remota confirmada: `f2fa46765fbd1a2c0ab65d3978f321ef050c3424`, merge PR #56 (M4-09), conforme [#46/6690486932](https://github.com/amgsrosario/fac/issues/46#issuecomment-6690486932). Integrações anteriores preservadas: PR #54/M4-10 (`41dbe85`) e PR #55/podeEmitir (`5342255`). «Integrado» é estado Git, não homologação ou autorização operacional. Sem novos ensaios; reutilizam-se as provas nos HEADs indicados nas respectivas validações.

[Fontes e matriz original](../m04-fiscal-evidence/MATRIX.md), N01–N08/E01–E06/C211. Não se infere que uma reserva significa ausência de decisão funcional; as decisões já aprovadas mantêm-se vinculativas.

| Requisito | Estado comprovado no perímetro | Lacuna / reserva efectiva | Seguimento e dependência |
| --- | --- | --- | --- |
| M4-01 Semântica FT/FR/RC | Decisões D-CAT-2A/H1 aprovadas | Projecções fiscais e circuitos próprios não integralmente homologados | Não reabrir conceitos; prova e mandato por tipo |
| M4-02 FT diferida | Correspondência funcional 2/1/1,false aprovada; seis critérios MockMvc com fonte sintética confirmados; entrega consolidada PR #53 | Emissão positiva pelo JAR, assinatura real e validações fiscais exteriores à correspondência | Mandato produtivo próprio; não extrapolar a prova sintética |
| M4-03 Vínculos/imutabilidade | V25–V28/H2-03 integradas; coerência Java/SQL e histórico preservados | Definição material legítima e publicação governada das revisões | Não publicar catálogo ou reclassificar por efeito das provas |
| M4-04 Projecção FT/FR | Fontes N04 D/G e N05; código e testes unitários identificados; captures sintéticas mostram D=FT e G com número interno | Oráculo aprovado de identidade/número/representação e correspondência entre configuração, persistência, QR, PDF e futura exportação | **Próximo incremento recomendado: prova parcial FT diferida/EUR**, sem FR/FT imediata nem SAF-T; ver proposta |
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

## Próximo incremento e sequência

Recomenda-se **M4-04, identificação e projecção da FT diferida/EUR**, com lacuna, fontes, cobertura reutilizável, prova necessária, dependências e mandato proposto em [NEXT_INCREMENT.md](../m04-fiscal-evidence/NEXT_INCREMENT.md). O objectivo não é repetir M4-09/M4-10 ou reabrir D-CAT: é demonstrar a identidade fiscal e documental nos vários suportes, com um oráculo previamente fundamentado.

Dependência material: o executor deve primeiro recuperar a regra/oráculo já aprovado para o número/projecção; se não estiver comprovado, apresentar o campo e alternativa exactos à validação administrativa/fiscal antes de mudar comportamento ou afirmar conformidade. Aprovação da semântica FT e da correspondência 2/1/1,false não é homologação implícita de D/G, QR/PDF ou SAF-T. Novo mandato necessário para executar a proposta; nenhum incremento iniciado aqui.

## Melhoria pré-produtiva separada

**Mostrar bruto, IVA e total no diálogo final**, inclusive aviso de recálculo já conhecido e operador no cabeçalho. Continua pendente antes de utilização produtiva, fora desta tarefa documental e da prova fiscal proposta. Não condicionou o parecer técnico recebido, não foi implementada e não se presume autorização para a implementar.

## Provas integradas e rastreabilidade

- M4-09: PR #56, merge f2fa46765fbd1a2c0ab65d3978f321ef050c3424, HEAD autorizado 5c5b34403cb00e0190f810209cbc29091b68653f, CI 42244362450 success (safety/package). Parecer incremental SHA-256 921303c688390d528885423dd8c7f37afada2511c2e6271af74ff0835d4bd881; [validação](../m4-09-qr-amounts/VALIDATION.md). Seis cenários em duas execuções, não suite conjunta.
- M4-10: PR #54, merge 41dbe8539df9626db8d941ffd688d8c2e754a420; parecer ba7b8bd5acd76ecc0ca28d35a48af8c9108094d18420dcebc462ae52d40ce36f; [validação](../m4-10-commercial/VALIDATION.md).
- podeEmitir: PR #55, merge 5342255e329f3a2ebe6d7b5ad97e9b6f8c09cf07; parecer ec8905c589068024c375285ad9b509f28ca7c815b778bb3a479cabf1eb4ad4a0; [validação](../emission-readiness/VALIDATION.md). Informação de elegibilidade, não garantia de fonte dinâmica ou autorização fiscal.

O registo local df71e68 mantém-se intacto na worktree anterior. Esta reconciliação actualiza o quadro corrente sem apresentar aquele commit como integrado. Sem testes, novas regras, código funcional, bases conservadas, push, PR, merge ou activação.
