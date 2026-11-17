# Pedido de revisão independente — preparação do próximo incremento FR/EUR

Executor: Codex Desktop. Revisor: Claude Code. **Revisão pendente**, sem aprovação presumida. O pedido anterior da FR económica foi atendido e não deve ser repetido.

## Base e perímetro

Repositório amgsrosario/fac; branch codex/fr-eur-identity-plan; worktree /home/arosario/Projetos/fac/.worktrees/fr-eur-identity-plan. Base e6973910e1652dea85acdc9e0848c7683c34e6d5, merge PR #58, tree e83c31aa9858f9dff51df798591a4aa94528850e. O commit documental final desta preparação será identificado na entrega. Rever o delta inteiro desde a base; não atribuir o parecer anterior a este plano.

## Fontes directamente consultáveis

Ler a ordem original em AUTHORIZATION.md, AGENTS.md e memória/estado, PLAN.md, TEST_PLAN.md, SOURCES.md, SOURCE_REGISTER.json e OFFICIAL_SOURCES.json. Conferir as decisões originais H1/D-CAT, o parecer FR anterior (SHA-256 46c8904275d9bb17b908a1ee3dfd80f3f1ad4054a8c209a031910a59eff8a9a1) e o código dos componentes citados. Documentos locais M4-12 em 0337ff1 são propostas não integradas, não decisões canónicas novas.

Arquivo previsto para conferência: Relatorios-AR2/TUULI/Arquivo-Reservado/FR-EUR-Identidade/Preparacao-2026-11-17. O relatório de entrega identifica o manifesto, o HEAD final e as fontes oficiais reutilizadas/evidências de merge. Não houve ensaios de aplicação, SQL, Docker, emissão ou alteração funcional nesta missão.

## Verificações prioritárias

1. Integração efectiva de b8b0c0f em e697391; distinção do documental local 4ad1edf; memória e estado reflectem merge sem activação. Preservação da cronologia implementação/regularização, do parecer original e dos gates.
2. Fidelidade ao produto aprovado: FR factura e quita integralmente num documento, sem RC artificial; FT/FR partilham tipologia interna FT. Não pedir nova aprovação do modelo.
3. Separação entre interno, tipo externo, número, série e projecção. Validar N01 InvoiceType/InvoiceNo, N02 D/G/H e N03. O número interno diferente de FR é permitido pelo oráculo; não trocar o prefixo por inferência.
4. Catálogo Java/V28 hoje só diferido: evolução proposta aditiva/imutável, sem alterar V1–V30 ou publicar manifesto. Não enfraquecer FT, RC ou a protecção histórica.
5. Fecho transaccional: projecção antes da assinatura, mas FR só pode consolidar com quitação completa. Não permitir emissão ordinária de nova configuração FR sem acto, nem aceitar flag público como autoridade. Rever compatibilidade do SQL diferido com snapshot e sealing V30.
6. Série/aliases/unicidade, locks e reimpressão: suficiência do plano e ausência de pressupostos fiscais inventados. Não concluir que a chave interna prova identidade fiscal, nem inventar códigos AT comerciais.
7. Testes: oráculos independentes, assinatura de teste, MockMvc/controlador apenas de teste, PDF efectivamente descodificado, leituras persistidas, rollback com causa esperada e matriz de regressão proporcional. Não generalizar provas sintéticas ou presumir homologação.
8. Futuro SAF-T: mapeamento sem XML/exportador, nenhuma fonte fictícia de HashControl/PaymentMechanism, sem duplicar pagamentos em RC; P12 permanecem nos seus gates. Distinguir dados desconhecidos, validação fiscal e escolhas reais de produto.
9. Anulação FR em incremento próprio; FinancialCore e consumidores operacionais inactivos. A revisão favorável do plano NÃO autoriza implementar a projecção ou activar o circuito.

## Saída

Parecer APROVADO / APROVADO COM RESERVAS / DEVOLVER PARA CORRECÇÃO, BLOCKER/MAJOR/MINOR por ficheiro/localização. Identificar HEAD/tree efectivamente revistos, fontes conferidas, decisões materiais que realmente faltem e SHA-256 do parecer original com sidecar. Revisão só de leitura, sem repetir testes ou alterar ambiente. Na ausência de runtime/canal Claude, o titular transmite este pedido; a sua existência não equivale a revisão realizada.
