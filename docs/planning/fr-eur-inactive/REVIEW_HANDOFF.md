# Pedido de revisão independente — FR/EUR inicialmente inactivo

Executor: Codex Desktop. Revisor solicitado: Claude Code. Sem parecer emitido nesta sessão.

## Identificação e perímetro

Base `f15388d0ac926cc39082869e898d5ef9998059f7` (PR #57). Branch `codex/fr-eur-inactive`, worktree `/home/arosario/Projetos/fac/.worktrees/fr-eur-inactive`. Código/testes ensaiados: `678e3f3bf4fb85835ec2c17437375f56d7bdef3c`, tree `8047edf7d9cde975d2e741920655a4d9604caa30`. A entrega documental posterior não altera implementação/testes; conferir o HEAD de entrega e o diff integral desde a base, incluindo todos os commits locais.

Ler directamente AUTHORIZATION.md (ordem original recebida neste chat), D-CAT-2A/H1 nas fontes canónicas, ADR-002, DESIGN.md e VALIDATION.md. Não inferir homologação fiscal de uma prova sintética.

## Conferência técnica

1. Um documento comercial constitui e quita a dívida na mesma transacção, com recebimento integral único, posição residual zero e nenhum RC/Pendente artificial.
2. Perfil fr-inactive-lab ausente por omissão; nenhum endpoint/consumer operacional novo. Permissão e utilizador autenticado/activo, mesmo perante segurança legada desactivada.
3. Replay do mesmo actor/contexto, chaves concorrentes e bloqueios. Confirmar payload, limites, numeração e ausência de duplicação.
4. Rollback de documento/numerador/auditoria/ledger nas falhas antes e depois da emissão. Inspeccionar as fixtures e as leituras persistidas.
5. V30: novo vínculo e sealing, SQL directo, completude diferida, datas e imutabilidade. Rever especialmente a extensão de financial_circuit_identity_lock: o ramo FR deve exigir conclusão atómica sem enfraquecer nenhum ramo V22 FT/RC. As V1–V29 não mudaram.
6. Consultas internas: o SETTLE FR identifica a origem comercial sem fabricar DocumentoFinanceiro; efeito na data de emissão e instante técnico separado. Preservar recusas históricas existentes. Anulação FR não autorizada permanece fail-closed.
7. Conferir as cinco execuções e os manifestos originais, incluindo tentativas falhadas; o resultado final de 61 testes e as provas reutilizadas pertencem a HEADs explicitamente distintos. Conferir package, versões, isolamento, remoção e ausência de dados conservados.
8. A adaptação da limpeza de DocumentoComercialEmissaoIntegridadeTests só pode actuar na base descartável validada e vazia de FR; não altera guardas produtivos.

## Fidelidade ao modelo e reservas

Confirmar que a quitação é um acto explícito, não uma inferência de liquidacaoImediata; o novo vínculo utiliza o FinancialCore existente, sem segunda autoridade. A projecção externa do documento conserva o comportamento existente: esta entrega NÃO homologa a identificação fiscal FR ou QR/PDF, não publica revisão/configuração, não cria assinatura real nem habilita emissão comercial. Avaliar se esta fronteira está claramente preservada.

Arquivo reservado: `Relatorios-AR2/TUULI/Arquivo-Reservado/FR-EUR-Inactivo/2026-11-17`; manifesto global `7bb09a00ae72457b889c213e6692a68bd17a91a9a0302f8b985c94bb2d0447a8`. Conferir os originais/sidecars e consultar o código directamente, não apenas o relatório.

## Entrega do revisor

Parecer APROVADO / APROVADO COM RESERVAS / DEVOLVER PARA CORRECÇÃO, achados BLOCKER/MAJOR/MINOR com ficheiro/linha, identificação do HEAD/tree efectivamente revistos e SHA-256 do original com sidecar. Separar fidelidade ao mandato, integridade técnica e gates produtivos ainda reservados.

Revisão só de leitura. Não repetir testes válidos sem risco concreto; não alterar código, bases, worktrees, activar, publicar ou integrar. O pedido deve ser efectivamente transmitido ao Claude pelo titular; a existência deste ficheiro não activa um revisor.
