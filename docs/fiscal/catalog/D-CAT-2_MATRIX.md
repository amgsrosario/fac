# D-CAT-2 — Matriz concreta de evidências para decisão

22-10-2026. Base 3bc2f70c473b0cd6bb5011ba46eb4b0dc382bc7f.
**Proposta de matriz, não matriz fiscal homologada.** Sem acesso a bases/demo.
[Arquitectura aprovada](../../governance/decisions/ADR-003-fiscal-catalog.md).

## 1. Combinações efectivamente demonstradas

| Origem / tipo | Código Fiscal demonstrado | Área / Entidade / sinal | Liquidação imediata | Efeito observado e limites |
| --- | --- | --- | --- | --- |
| Seed FT | O INSERT omite codigo_fiscal; não demonstra FT fiscal | 2 / 1 / 1 | false | Configura tipo interno FT; não prova dados de qualquer ambiente |
| Seed RC | O INSERT omite codigo_fiscal; não demonstra RC fiscal | 3 / 1 / 2 | true | Configura tipo interno RC; booleano não é especialização aprovada pela D-CAT-2A |
| Originação FT do núcleo | Exige codigoFiscal=FT | Sem matriz desses três atributos neste predicado | Exige false | Nova emissão EMITIDO/EUR cria DEBT e vínculo; não cobre FR ou outras moedas |
| Emissor RC interno | Exige codigoFiscal=RC; origens FT | A combinação exacta dos três não é demonstrada apenas por este predicado | Não é fundamento de especialização RC | Liquida FT no núcleo, inactivo operacionalmente |
| Documento financeiro legado | Data Valor validada/persistida apenas se codigoFiscal=RC | DTO admite área 1…99, Entidade 1…9, sinal 1…2, independentemente | Campo genérico existente | Intervalos de DTO não são matriz de combinações autorizadas |

Fontes: [seed](../../../src/main/resources/demo/demo-base.sql), linhas 151–175;
[FinancialFtOrigin](../../../src/main/java/com/ar2lda/fac/financial/FinancialFtOrigin.java),
originateNewEmission; [FinancialReceiptIssuer](../../../src/main/java/com/ar2lda/fac/financial/FinancialReceiptIssuer.java),
validação de tipo/origens; [DocumentoFinanceiroService](../../../src/main/java/com/ar2lda/fac/service/DocumentoFinanceiroService.java),
create; [DTO](../../../src/main/java/com/ar2lda/fac/controller/dto/TipoDocumentoCreateDto.java).
O upsert do seed também não atribui codigo_fiscal: numa base existente poderá
preservar valor anterior; não se afirma que todas as instalações tenham NULL.

[UI](../../../frontend/src/TabelasEspecificasView.tsx), opções: área 1 comercial,
2 comercial/facturação, 3 financeiro/tesouraria; Entidade 1 Cliente; sinal 1
Débito/2 Crédito. Oferece FT, FR, NC, ND e outros códigos, sem provar emissão
ou habilitação de cada um. FR aparece como Código Fiscal distinto, em tensão
com D-CAT-2A; não deve ser copiado automaticamente para o catálogo aprovado.
A UI só listar Cliente não prova exclusão funcional de outras Entidades.

[PendenteService](../../../src/main/java/com/ar2lda/fac/service/PendenteService.java),
criarDeDocumento, não cria Pendente se liquidação imediata; restantes emitidos
com número/valor positivo seguem caminho genérico. Isso não é aprovação da
natureza fiscal de todos os tipos. [Repositório financeiro](../../../src/main/java/com/ar2lda/fac/repository/DocumentoFinanceiroRepository.java)
agrupa sinal 1 como débito e 2 como crédito; não derivar daqui sinal fiscal RL.
[DemoScenarioService](../../../src/main/java/com/ar2lda/fac/demo/DemoScenarioService.java),
linhas 95/104, usa IDs FT/RC; não demonstra correspondência fiscal nem RL.

## 2. Matriz-alvo parcialmente aprovada e lacunas

| Tipo TUULI | Tipologia/Código Fiscal aprovado | Especialização | Efeito aprovado | Valores a decidir/confirmar |
| --- | --- | --- | --- | --- |
| FT | FT | Liquidação imediata não | Constitui dívida do cliente | Área/Entidade/sinal definitivos: 2/1/1 é candidato demonstrado no seed, não homologação |
| FR | FT | Liquidação imediata sim | Liquidada na emissão | Não encontrada configuração FR completa nas fontes delimitadas; não copiar código FR da UI |
| RC | Tipologia RC; código RC exigido no emissor interno | Sem especialização adicional | Recebimento/liquidação de dívida existente | 3/1/2 é candidato demonstrado; decidir representação técnica sem especialização, sem alterar RC históricos true |
| RL | Correspondência fiscal NÃO CONFIRMADA | Sem especialização adicional | Pagamento/devolução ao cliente, incluindo crédito NC | Código, área, Entidade e sinal não preenchidos por inferência; validação documental necessária |

Regras desta tabela são D-CAT-2A onde indicado; candidatos numéricos são factos
de configuração, não novos efeitos aprovados. Não equiparar RC/RL. Não atribuir
false automaticamente aos RC históricos por confundir ausência de especialização
com valor do campo legado. NC/ND e restantes opções UI não recebem combinações
inventadas; OP19 aprova conceitos mas não substitui esta matriz completa.

## 3. D-CAT-3 — recomendação sujeita ao PO

Recomendar revisões semânticas imutáveis, com vínculo estável de novos tipos à
revisão aprovada. Alterações futuras de efeito criam nova revisão; tipo já utilizado
não troca revisão/atributos protegidos. V25/V26 não bastam para congelar uma
tabela de semântica futura: a nova implementação terá de impedir deriva indirecta.
Alternativa relevante: revisões por vigência com resolução temporal na leitura;
mais flexível, mas exige regras de datas/retroactividade e pode reinterpretar
originais. Preferir vínculo explícito, sem escolher já esquema físico.

Transição recomendada: nenhum backfill inferido nem normalização silenciosa;
primeiro novas configurações dentro da matriz aprovada, leitores antigos
preservados até equivalência demonstrada e política autorizada. Alternativa:
associar tipos existentes por equivalência actual demonstrada; maior cobertura,
mas não prova classificação original e exige aprovação de política histórica.
Não alterar V25, V26 ou emitidos para acomodar a nova semântica.

## 4. Decisões efectivas e aceitação proposta

Administração: homologar combinações numéricas por tipologia, resolver a
representação da ausência de especialização RC/RL, confirmar correspondência RL
antes de a implementar e decidir D-CAT-3/transição dos tipos existentes. Não é
necessária nova escolha entre híbrido/código/persistido: D-CAT-1 já resolveu.

Primeiro incremento proposto, após aprovação: catálogo governado e validação
de novas configurações, sem cutover. Critérios propostos: materialização/Java
coerentes; recusa de combinações não aprovadas por serviço e SQL; emissão e
configuração concorrentes seguras; revisão nova não reclassifica emitidos;
upgrade aditivo/segundo arranque; regressão RC/não-RC e Data Valor/DTO/PDF/saldos
sem alterações; testes PostgreSQL descartável e revisão Claude. Não são testes
executados nem autorização de implementação. Fiscalidade completa, multimoeda,
RC operacional e 1e continuam fora do âmbito.
