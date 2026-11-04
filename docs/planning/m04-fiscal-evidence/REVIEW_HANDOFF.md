# M04 — Pedido de revisão independente Claude Code

Ordem [#46/6628280906](https://github.com/amgsrosario/fac/issues/46#issuecomment-6628280906). Branch local `codex/m04-fiscal-evidence`, worktree `/home/arosario/Projetos/fac/.worktrees/m04-fiscal-evidence`. Base `ff9d2ec92e2419cbe6fec9be22d65afa5520e84b`/PR #51; HEAD exacto no relatório externo M04. Rever exclusivamente o diff documental, fontes e correspondência às decisões/código existente. Não declarar revisão realizada por existir este pedido.

Ler AGENTS, memória/estado, MISSIONS/PENDING_MAP, MATRIX.md, SOURCES.md, sources.json e SAFT_CONTRACT.md. Documentos H2-04 anteriores estão no objecto local `64c6a2d` e arquivo externo, não afirmar integração na base. E01/E03/E05 e pareceres anteriores são entradas, não autorização de homologação. A M04 não executou novos testes de aplicação, SQL ou Docker.

Prioridades:

1. Matriz por requisito: fonte, prova/estado, lacuna/meio de obter, dependência da assinatura, reserva e próximo passo, sem classificações fiscais presumidas ou decisões adicionais. Preservar2/1/1 pendente de validação fiscal expressa, D-CAT-1/2A/3/H2-02 e exclusões.
2. Nova FAQ4443: conferir HTML integral oficial `faq-qr-00084.html`, resposta e hash 01dccd91844986d3e86c86b68475c870b8038c400649b4e1d4cd514f913ddd47; antigo 00879 continua404. Fechar apenas a lacuna de obtenção documental, não homologação QR/PDF. Conferir distinção mensagem original/estado actual e limite de históricos financeiros sem payload.
3. Reutilização E01/E03:50 e 124 artefactos conferidos, 318 casos H2-03 anteriores e286 H2-05 anteriores, sem nova execução nem soma de fases. Alterações posteriores não afectaram os componentes fiscais/PDF/QR examinados; não declarar suite actual integral, impressão real ou certificação.
4. SAF-T: XSD oficial1.04_01/minVersion1.1 obtido, contrato ainda proposto, sem exportador. FT/FR externos distintos da tipologia interna comum, RC/RG sujeito a enquadramento, TransactionDate≠Data Valor, estado exportado actual≠QR congelado. Hash integral≠Q e HashControl≠R; não impor Hash/HashControl a Payments quando o XSD não os contém. Sem defaults fictícios/SAF-T parcial comercial.
5. Reservas F01, D-SIGN-02..05, NC/ND, RC/NL/RL e certificação não reabertas/canceladas. Nenhuma mudança funcional, de manifesto, base, demo/GOLDEN ou worktree7ce573f; sem M05, push/PR/merge ou activação.

Arquivo M04 `/home/arosario/tuuli-backups/m04/2026-11-04/`: 6 ficheiros + manifesto SHA-256 `1536e33418d9d9bd40d43ef7bb6cb2953663776074f946918536746d93af3468`. Originais antigos mantidos nos locais E01–E05. Conferir fontes e hashes antes do parecer, sem operar bases conservadas. Só repetir teste mediante risco concreto e autorização; aqui basta leitura.

Emitir APROVADO / APROVADO COM RESERVAS / DEVOLVER PARA CORRECÇÃO com BLOCKER/MAJOR/MINOR, HEAD revisto, referências exactas, distinção entre lacuna fiscal reservada e defeito documental, limitações e matérias efectivamente reservadas à Administração. Parecer não equivale a homologação/activação ou autorização SAF-T. Sem runtime Claude nesta sessão: pedido preparado para transmissão efectiva pelo PO/Coordenação, não activação por comentário GitHub.

## R1 — Confirmação incremental dos dois MINOR

Comparar `ddd563b3ddb7da4db215e581957941ce5387cab3..HEAD` final indicado no relatório R1, branch/worktree M04 existentes. Ordem #46/6628777871; parecer original SHA-256 `e09a436ba2818678a8fd2a82d94b5b42bd83f5020eb18357ad7201d7f6b888f0`, original/sidecar conferidos.

MINOR-001: verificar N08/Portaria363 original e hash, relação normativa N05→N08, E06 com referências exactas a H2-07/CONTRACT e H2-08/DECISIONS, e falta de versão de chave em Signature. Não confundir keyVersion de um desenho/ensaio com implementação produtiva ou certificado; M4-14 deixa explícita a inexistência actual da origem de HashControl no contrato.

MINOR-002: conferir por componente C211/M4-02: seed com tuplo completo exclusivamente demonstrativo, FinancialFtOrigin sem leitura do tuplo, extracto só sinal, UI só filtro de área; todos com ficheiro/linha. 2/1/1 permanece pendente de validação fiscal expressa.

Arquivo R1 `/home/arosario/tuuli-backups/m04-r1/2026-11-04/`,4 ficheiros + manifesto SHA-256 `d5063556d61cc7948388492c145fd00730f147a4c91718c5c8ff8a3aa7b4e805`. Original M04/FAQ/XSD/provas anteriores intactos; R1 só documental, zero testes/SQL/Docker. O-1–O-4 não condicionantes mantidas, sem alegado fecho. Emitir veredicto e estado dos dois MINOR sobre o HEAD exacto, sem repetir a revisão global ou tomar decisões fiscais/D-SIGN. Sem push/PR/merge, bases, activação ou M05. Pedido a transmitir efectivamente pelo PO, não activação por ficheiro/comentário.
