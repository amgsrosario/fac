# ADR-001 — Fonte única financeira por substituição progressiva

Versão1.0 · 12-10-2026 · Estado: **APROVADA PELO PRODUCT OWNER**.
Fundamento: decisão expressa OP22-GOV, após comparação OP22-P2.
A aprovação refere-se à orientação arquitectónica; não autoriza implementação
funcional nesta missão documental nem decide as matérias reservadas abaixo.

## Contexto e pressupostos confirmados

TUULI AIR está exclusivamente em desenvolvimento e demonstração. O Product
Owner confirmou que não existem documentos ou saldos de produção cujo histórico
seja necessário preservar. Os dados atuais são descartáveis e uma nova demo
poderá ser reconstruída posteriormente, mediante autorização própria.
Não é necessário desenvolver migração ou compatibilidade histórica apenas para
proteger essa demo. Isto não autoriza apagar bases/dados nem alterar as regras
de integridade, auditoria e preservação dos documentos que o produto emita.

OP19 definiu o contrato funcional; OP20 implementou FinancialCore; OP21 ligou
originação/anulação de novas FT EUR. RC continua no legado e Pendente ainda
é fonte operacional. Originação e liquidação não estão unificadas atualmente.
Os relatórios OP22-P/OP22-P2 foram apresentados ao PO na conversa da missão;
esta ADR regista a decisão, sem os apresentar como relatórios já versionados.

## Alternativas e comparação

| Estratégia | Benefícios | Custos, riscos e consequências |
| --- | --- | --- |
| A — Integração incremental de RC elegíveis, mantendo Pendente operacional | Menor primeira alteração; conserva os consumidores atuais | Sincronização/duas escritas, divergências, fallback e posterior substituição; trabalho transitório sem necessidade de proteger históricos de produção |
| B — Substituição progressiva do legado por FinancialCore | Reutiliza o núcleo, delimita validação por etapas e converge para uma fonte financeira | Exige completar contratos de leitura e adaptar consumidores; risco de ativar fronteira incompleta ou omitir dependências de Pendente |
| C — Reformulação conjunta FT/FR/RC/NC/ND/NL, compensações e conta corrente | Desenho unificado desde uma entrega ampla | Esforço inicial e regressões mais extensos; várias decisões fiscais pendentes podem bloquear a entrega conjunta |

A estimativa é relativa, não calendarizada: A tem menor custo inicial e maior
custo transitório; B custo inicial intermédio e retirada progressiva de duplicação;
C maior custo inicial e incerteza de execução. Pressupõe reutilização do backend,
PostgreSQL, motores fiscal/monetário e apresentação existentes, sem infraestrutura
nova. Resultados futuros de esforço/manutenção são estimativas, não factos medidos.
A comparação segue a [GOV-ALT-001](../INSTITUTIONAL_MEMORY.md#gov-alt-001--transparência-e-avaliação-de-alternativas).

## Decisão expressa e fundamentos

**Escolhida B — Substituição progressiva do legado.**

Os dados demonstrativos não justificam perpetuar compatibilidade histórica.
FinancialCore fornece base reutilizável e pretende-se evitar dois motores
financeiros permanentes. A prioriza uma coexistência cujo principal fundamento
foi retirado pelo PO; C concentra demasiados circuitos e riscos numa intervenção.
B combina o destino unificado com intervenções delimitadas e verificáveis.
Esta é decisão do PO, não conversão automática da recomendação do Executor.

## Destino e consequências arquitectónicas

FinancialCore deverá tornar-se a única fonte de verdade financeira de posições
devedoras/credoras, afectações, movimentos, compensações e estornos. Pendente
perderá o papel de fonte independente de saldos; poderá subsistir temporariamente
como interface ou projeção de leitura derivada do núcleo, sem saldo autónomo.

A primeira fronteira a preparar é FT/RC, incluindo escrita, leitura, anulação
e consistência. A substituição ocorre por etapas validadas; não ativar uma
fronteira operacional incompleta como circuito estabilizado. Não apresentar
esse destino como já implementado: hoje RC e saldo operacional permanecem legados.
Preservar reutilização dos motores RIVA/monetário e dos contratos fiscais aprovados.

## Riscos conhecidos

- Coexistência atual: RC altera Pendente sem afectar a posição OP21; não presumir equivalência.
- Precisão do RC legado e núcleo difere; política de recebimentos continua reservada.
- Idempotência documental RC, vínculos, consultas completas e retirada dos consumidores de Pendente ainda exigem intervenção.
- Regras de anulação RC por comparação de saldos não equivalem às dependências efetivas do núcleo.
- Faltam integrações documentais de créditos/NC/ND/NL e validação de cenários fiscais pendentes.
- CI verde da OP21 cobre safety validator e package, não integração PostgreSQL integral.

## Decisões reservadas

B não aprova alterações fiscais, política monetária/precisão dos recebimentos,
evolução de estornos, tratamento documental NC/ND/NL, descontos financeiros,
adiantamentos, compensações/RC sem caixa, migração ou eliminação efetiva de dados,
SAF-T ou declarações de conformidade/certificação AT. Exigem análise e autorização
próprias. Preservar as decisões funcionais OP19, distinguindo-as da autorização
de implementação e da admissibilidade jurídica de cada cenário.

## Sequência preliminar — sem autorização de execução nesta OP

1. Definir a fronteira FT/RC e os contratos em falta, enumerando consumidores,
   idempotência, precisão e estornos; obter decisões reservadas necessárias.
2. Implementar e validar a fatia FT/RC com escrita/leitura/anulação coerentes,
   por OP própria; manter demonstração estabilizada até completar a fronteira.
3. Transferir consultas/reporting/consumidores para a fonte única; Pendente
   pode funcionar como projeção derivada durante adaptação, sem segundo saldo.
4. Integrar outros circuitos por OPs próprias e gates fiscais aplicáveis.
5. Retirar a autoridade legada e preparar nova demo controlada, sem migrar
   documentos atuais por mera conveniência e sem eliminar dados sem autorização.

Detalhes e ordem fina dependem das próximas OPs; não constituem decisões fiscais
ou plano funcional já autorizado. Evidências/estado corrente ficam em
[OPERATIONAL_STATE.md](../OPERATIONAL_STATE.md). Fundamentos duradouros ficam na
[Memória Institucional](../INSTITUTIONAL_MEMORY.md).
