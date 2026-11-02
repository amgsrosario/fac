# H2-08 — Relatório de sucessão e pedido Claude

## Continuidade comprovada

Base `f7e8f8ad405a0e5714af0e9fc7264a9e581ff0de`, integração H2-07 inactiva,
fechada. Worktree documental `h2-08-signing-decisions`, branch
`codex/h2-08-signing-decisions`. Checkout principal e alterações preexistentes,
outras worktrees e `7ce573f` preservados. Nenhuma base foi acedida.

## O que esta entrega acrescenta

- [DECISIONS.md](DECISIONS.md): quadro A/B1/B2 com custos qualitativos e limites,
  proveniência/âncora independente, ciclo de vida e pedidos específicos de decisão.
- [INTEGRATION_TESTS.md](INTEGRATION_TESTS.md): mapa do código, contrato futuro,
  resposta incerta, riscos e 16 testes/aceitações propostos.
- [SOURCES.md](SOURCES.md) e [sources.json](sources.json): exemplares oficiais,
  hashes, confirmação limitada e reservas humanas, incluindo FAQ não obtida.
- Memória/estado: fecho H2-07 comprovado e H2-08 em preparação decisória, sem
  transformar recomendações em aprovação, implementação ou activação.

## Pedido dirigido ao Claude Code, independente

Rever o commit documental desta branch contra `f7e8f8a`, identificado no PR/relatório
externo; ler integralmente o dossier e a issue #46, AGENTS e fontes institucionais.
Confrontar referências com o código dessa base e os exemplares oficiais.
Não consultar históricos privados de outros agentes. Não reabrir H2-07.

Confirmar especialmente:

1. A/B discrimina custódia local, HSM local e serviço remoto, exposição de chave,
   isolamento por empresa, offline/latência, custos não assumidos e recuperação.
2. Certificado do programa/ATCUD/chave/credencial não são confundidos; registo
   independente inclui bootstrap confiável, anti-regressão e separação de poderes.
3. O perfil do exemplar não é vigência produtiva presumida nem escolha livre;
   fontes/FAQ e dúvidas legais são rastreáveis, sem homologação implícita.
4. Transacção, instante anterior ao QR, numeração, predecessor, idempotência,
   origem JPA/JDBC e chamadas remotas têm limites/testes sem promessa de 2PC.
5. Rotação/incidente/restore/rollback preservam prova/cadeia e não reactivam
   escrita legada paralela, nem reduzem numeradores após documentos entregues.
6. Decisões específicas D-SIGN-02..05 estão reservadas à Administração;
   recomendação B condicional e alternativas de confiança não são decisões tomadas.
7. Nenhuma alteração funcional, chave, migration, acesso a base, custo, activação,
   deploy ou merge. HTTP 409, gates, demo e `7ce573f` intactos.

Entregar APROVADO / APROVADO COM RESERVAS / DEVOLVER PARA CORRECÇÃO, com
BLOCKER/MAJOR/MINOR e ficheiro/linha, cobertura e limites. Não homologar em nome
da Administração. Não gerar chaves, executar serviços ou modificar bases.

## Próximo estado e dependências

**Agora:** dossier preparado para revisão, não aprovado independentemente.
Sem executável/revisor Claude disponível não se fabrica parecer nem se usa
Codex como substituto. O pedido é publicado na issue #46 e PR documental draft.
Se o parecer trouxer achados técnicos/documentais no âmbito, corrigi-los e pedir
confirmação incremental; uma decisão material é devolvida à Administração.

**Após parecer positivo:** Administração delibera D-SIGN-02..05 por escolhas e
condições explícitas. Autorizar depois apenas um incremento delimitado de
confiança/prova inactiva; integração real e activação continuam missões próprias.
Não abrir automaticamente gates porque o dossier foi revisto ou integrado.

**Reservas mantidas:** prova legítima do certificado/chave não apresentada;
perfil actual e projecções dependem de validação fiscal/jurídica; capacidade,
custos e recuperação A/B ainda não demonstrados; FAQ integral ausente; regras
de 2/1/1, QR/PDF, AT, RC/RG, FinancialCore/1e e #38 não libertadas por esta missão.
RTO/RPO, custódia e responsáveis não são inventados. Sem prazo comercial prometido.
