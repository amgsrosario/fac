# ADR-003 — Catálogo canónico de Código Fiscal

Data: 22-10-2026. Estado: **D-CAT-1 APROVADA pela Administração AR2 / PO**.
Fonte: ordem expressa D-CAT-1 e retoma sobre o PR #34. Não é implementação.

## Contexto e decisão

OA §3.A/OB §4 estabelecem autoridade canónica do Código Fiscal; D-CAT-2A define
tipologias e especializações. V25/V26 preservam o mestre utilizado e o vínculo
financeiro, sem reconstruir classificações antigas.

Aprovação: arquitectura híbrida governada. Definição semântica única, versionada
e governada; materialização PostgreSQL por migrations controladas; consumo Java
com verificações de coerência. Proibidas edição livre por utilizadores e
autoridades classificativas independentes. A forma exacta dos ficheiros/tabelas
não foi decidida nesta ordem e não é imposta por este ADR.

## Alternativas consideradas e fundamento

| Alternativa | Benefício | Custo/risco relevante |
| --- | --- | --- |
| Rígido em código | Simplicidade de release e revisão | SQL precisa de representação coerente; risco de duplicar semântica |
| Persistido/versionado | Referência acessível a SQL e Java | Exige governação de publicação e protecção de alterações; edição livre não aprovada |
| Híbrido governado — escolhido | Integra revisão em release, referência persistente e garantias SQL | Exige verificação de coerência para evitar deriva entre representações |

Fundamento técnico da recomendação submetida: Java/PostgreSQL/Flyway e protecções
SQL já existentes. A decisão expressa do PO escolheu o híbrido; não se atribui
ao PO uma escolha de esquema concreto ou política D-CAT-3 ainda pendente.

## Relação com D-CAT-2A e limites

[Decisão funcional canónica](../INSTITUTIONAL_MEMORY.md#d-cat-2a--tipologias-fiscais-e-especializações):
FT/FR partilham FT com liquidação posterior/imediata; RC/RL seguem modelos
próprios, sem especialização adicional. RL representa pagamento/devolução ao
cliente, incluindo crédito NC; correspondência fiscal exacta não confirmada.

[Matriz de evidências](../../fiscal/catalog/D-CAT-2_MATRIX.md) distingue contrato
aprovado e implementação actual. Não homologa valores encontrados no seed/UI.
D-CAT-3 foi aprovada posteriormente: revisões fiscais imutáveis, vínculo estável
e transição sem reclassificação histórica automática, conforme ordem de
continuidade da Administração de 22-10-2026. A matriz D-CAT-2 permanece pendente. Não reclassificar históricos, enfraquecer
V25/V26, implementar catálogo ou activar circuitos nesta missão. O catálogo não
equivale a certificação nem habilitação operacional. Legado conserva autoridade
operacional; RC novo inactivo e Fatia 1e suspensa.
