# OP19 — Reconhecimento e relatório documental

Versão 1.1 · 09-10-2026.

## Estado observado

Fetch origin com prune executado. Base remota confirmada:
`c2f7e20703b6627beba670954da9c9e187ba556b`, feature/visual-redesign.
GitHub confirmou PR16 merged para essa linha, merge
`cc6fcc4cbb2d240de68f00976e0e627822871d04`, e PR17 merged, merge `c2f7e207…`.
AGENTS e ambas as fontes canónicas foram consultados antes das alterações.

Checkout principal permanece na branch feature/visual-redesign, HEAD11d5635,
com docker-compose.yml modificado e .worktrees/ não versionada. Preservado.
Nova worktree .worktrees/financial-contract-op19, branch
codex/financial-contract-op19, criada sobre a base remota confirmada.
Outras worktrees preservadas; não foram reutilizadas para edição.

Não foi encontrado relatório OP18 em docs na base remota (pesquisa OP18 e
inventário documental). Contexto anterior foi reutilizado como orientação;
conclusões não disponíveis como artefacto versionado não foram inventadas.

## Componentes diretamente relacionados

Caminhos a partir da raiz do repositório:

| Componente | Localização / limite observado |
| --- | --- |
| Comercial | src/main/java/com/ar2lda/fac/service/DocumentoComercialService.java; emissão/snapshots/anulação existentes |
| Financeiro | src/main/java/com/ar2lda/fac/service/DocumentoFinanceiroService.java; create/anular/criarLinha, dívida descontada versus caixa |
| Pendentes | src/main/java/com/ar2lda/fac/service/PendenteService.java; novoPendente positivo, sem crédito autónomo |
| Conta corrente | src/main/java/com/ar2lda/fac/service/ExtratoClienteService.java; saldo derivado não equivale a afectação |
| Monetário/fiscal | src/main/java/com/ar2lda/fac/service/MotorMonetario.java e MotorFiscalService.java; reutilização sem recalcular emitidos |
| Auditoria | src/main/java/com/ar2lda/fac/service/AuditoriaService.java e AuditoriaIsoladaService.java; sucesso/recusa existentes |
| Persistência | model/Pendente.java, DocumentoFinanceiro.java, LinhaDocumentoFinanceiro.java e repository/PendenteRepository.java sob o mesmo pacote |
| Interface atual | frontend/src; PendentesView.tsx/contratos existentes reconhecidos no contexto anterior; nenhuma edição |

Leitura dirigida confirmou pendente positivo independente de sinal, liquidação
parcial com bruto/desconto/líquido, criação financeira sem chave de retry e
anulação que compara saldo atual com saldo após linha. São motivos para o
desenho futuro; não se declara que os cenários novos funcionam hoje.

## Alterações e verificações

Cinco ficheiros novos em docs/fiscal/op19 e atualizações pontuais das duas
fontes canónicas. Apenas documentos Markdown; nenhum código/teste/configuração.
Contrato, alternativas, invariantes, migração, casos de aceitação, dossier,
gates legais e sequência futura preparados. Revisão documental local efetuada.
OP19-R concluída pelo revisor op19_independent_review: BLOCKER0/MAJOR1/MINOR3,
CORRIGIR ANTES DE APROVAR. OP19-C corrige os quatro pontos: NC parcialmente
consumida/reutilizada/estornada, A14 fiscal explícito, RC misto/motor comum e
ND/modos de criação. Nova verificação independente limitada solicitada ao mesmo
revisor; resultado final APROVAR, BLOCKER0/MAJOR0/MINOR0. Uma quebra editorial
da tabela foi corrigida e confirmada pelo revisor. A aprovação é documental,
sem validação jurídica ou execução dos cenários futuros.

Verificações: git diff --check e verificação de caminhos/links locais do dossier.
Sem testes funcionais/package, porque não houve alterações executáveis; casos
A01–A36 especificados, não executados. Nenhuma DB/migration foi executada.
Sem staging, commit, push ou PR nesta OP. Publicação aguarda nova instrução.
