# D-CAT-2 — Matriz concreta de evidências para decisão

22-10-2026. Base 3bc2f70c473b0cd6bb5011ba46eb4b0dc382bc7f.
**Matriz técnica ainda não integralmente homologada; semântica FT/FR/RC homologada em H1.1.** Sem acesso a bases/demo.
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

## 3. D-CAT-3 — princípios aprovados, sem reabrir decisão

A ordem de continuidade de 22-10-2026 confirma revisões fiscais imutáveis,
vínculo estável e transição sem reclassificação histórica automática. Esta
aprovação posterior supera o estado «recomendação» do relatório anterior.
V25/V26 não bastam para congelar uma tabela semântica futura: o vínculo não pode
passar a resolver a revisão mais recente nas leituras de emitidos. Não atribuir
retrospectivamente versões sem prova. Nenhuma alteração histórica executada.

## 4. Matriz funcional — quatro dimensões distintas

| Tipo | Aprovado pela Administração (D-CAT-2A) | Existente demonstrado no código/configuração | Correspondência fiscal demonstrada | Reserva material efectiva |
| --- | --- | --- | --- | --- |
| FT | Tipologia FT; constitui dívida; liquidação posterior | Seed 2/1/1,false; adaptador origina FT EUR não imediata | Relação FT→FT aprovada; código FT usado no adaptador, não é parecer normativo | Homologar semântica/combinação de área/Entidade/sinal, não escolher algarismos sem contexto |
| FR | Tipologia FT; liquidação imediata na emissão | UI lista código FR separado; PendenteService omite pendente imediato; não prova circuito FR completo | FR→FT é decisão funcional aprovada; lista UI não demonstra correspondência alternativa | Aprovar combinação concreta e limites de implementação; não alterar FR histórico por inferência |
| RC | Recebimento/liquidação de dívida existente; sem especialização adicional | Seed 3/1/2,true; emissor interno exige RC e FT, inactivo | Código RC é predicado implementado; não valida fiscalmente toda combinação | Homologar efeitos-base/área/Entidade/sinal e perímetro elegível; representação técnica do campo não é nova decisão de negócio |
| RL | Pagamento/devolução ao cliente, incluindo crédito NC; sem especialização adicional | Nenhuma configuração completa demonstrada no âmbito delimitado | Correspondência exacta NÃO demonstrada; nenhum código atribuído | Confirmação documental/fiscal e matriz específica antes de implementar RL |

Os valores 2/1/1 e 3/1/2 são representações técnicas demonstradas. A Administração
decide a semântica e combinações permitidas; a tradução em IDs deve manter a
compatibilidade existente. Não pedir ao PO que escolha números como negócio.
Ausência de especialização RC/RL já foi decidida: cabe à implementação escolher
representação sem activar funcionalidade ou normalizar o true legado. Só escalar
se não for possível preservar simultaneamente o contrato e os históricos.

## 5. Decisões materiais remanescentes

1. Homologar por tipologia natureza/efeito financeiro-base, áreas e Entidades
   permitidas, sinal e especializações; não generalizar a UI Cliente a todas
   as Entidades nem usar intervalos DTO como aprovação. O modelo FT/FR e RC/RL
   já aprovado não volta a votação.
2. Confirmar a correspondência fiscal RL e os efeitos específicos que ainda
   carecem de prova. Enquanto não existir confirmação, RL não entra em regras
   executáveis, não recebe código fiscal e não é equiparado a RC.
3. Delimitar os tipos/circuitos abrangidos pela primeira validação operacional
   futura. Presença no catálogo não habilita emissão nem muda limites actuais.

D-CAT-1 e D-CAT-3 não estão pendentes. Nenhum obstáculo relativo a multimoeda,
temporalidade FT ou corte é inventado como pré-condição de uma fundação inactiva.
[Primeiro incremento técnico](FIRST_SLICE.md): especificação pronta; execução
ainda não iniciada. Nenhum teste funcional executado nesta preparação documental.


## 6. Homologação semântica H1.1 — estado posterior

23-10-2026: FT constitui dívida com liquidação posterior; FR é liquidada na
emissão; RC reduz/extingue dívida existente. FT/FR têm tipologia interna FT,
diferenciada pela liquidação imediata; RC sem especialização adicional.
As referências anteriores a homologação semântica pendente foram superadas
pela ordem H1.1; candidatos técnicos e correspondências fiscais não o foram.
Área/Entidade/sinal concretos continuam a exigir prova/homologação suficiente.
Não usar 2/1/1 como decisão nova nem resolver RC/RG sem enquadramento demonstrado.
RL permanece sem correspondência homologada. [Primeiro perímetro FT](H1_1_FT_SPEC.md).
