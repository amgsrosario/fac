# Pedido ao Claude Code — delta documental SAF-T

Base integrada e0b15a7a1a7c2c46604463d97c55380884c0dac2. A R1 anteriormente revista em 7efda5ca40ceb0958d6015e28e41f68e45f57653 está reposta byte a byte nos sete documentos no commit 61032379bd3732a5406aea866fbe587766208103. O parecer de confirmação de 13/11/2026 (APPROVE 0/0/0) confere com o sidecar SHA-256 a98576433fd7f0936015729fb37a079e5610ddda885fa8a7cc16ac2c85cbd07d. Original em Relatorios-AR2/TUULI, ficheiro 2026-11-13_TUULI_M4-12_R1_confirmacao-incremental_Claude.md.

Rever apenas `6103237..HEAD` desta entrega e confirmar as alterações de M4-12 a M4-16, condições XSD/N05, mapeamento de Payments e exclusão da quitação interna FR, estado documental V32 e separação entre escolhas P12 e comprovativos em falta. Consultar as fontes arquivadas em M4_12_SOURCES.json e os modelos DocumentoComercial, DocumentoFinanceiro e LinhaDocumentoFinanceiro, FinancialFrIssuer, FinancialFrCorrection e migrations V31/V32 do merge. Evidência FR e M05 reutilizada; nenhum XML produzido ou validado nesta missão. Ainda é preciso mapear exaustivamente as famílias adicionais que a decisão P12-02 tornar aplicáveis. Revisão pendente de encaminhamento e integração sujeita ao gate humano.

## Confirmação incremental dos dois MINOR

Rever apenas o delta a partir de `90ba6607f97811799b85f3ec596274c2454fb7c3` até ao commit que acrescenta esta secção. Parecer anterior de 19/11/2026 conferido com sidecar em Relatorios-AR2/TUULI, `2026-11-19_TUULI_M04-SAFT_consolidacao-PR60_revisao-incremental_Claude.md`, SHA-256 `5faa7f123aac1b4338fe7d123a9ac3c3f6d6a2d92e33fe8019f46a3d9355a8fe`.

1. MINOR-001. Confirmar o cabeçalho pós-PR #60 e as linhas M4-04/M4-17, incluindo a integração #57 e a reserva fiscal sobre o espaço após ATCUD.
2. MINOR-002. Conferir a cópia N05 pelo hash acima registado no contrato e consultar visualmente pp. 44, 45 e 54, §§4.3.1–3 e 4.4.1–3. Confirmar a inclusão de A na contagem, a exclusão de A de ambas as somas e a contribuição de F em WorkingDocuments. Conferir os oráculos discriminantes em SAFT_ACCEPTANCE.md.

Sem resultados de execução fiscal nesta correcção documental. A confirmação aguarda encaminhamento ao Claude Code e mantém-se o gate humano de integração.
