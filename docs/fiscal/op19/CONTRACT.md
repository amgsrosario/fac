# OP19 — Contrato funcional e arquitetura financeira

Versão 1.1 · 09-10-2026. Produto aprovado; desenho técnico proposto.
Requisitos legais e gates: [dossier](AT_DOSSIER.md). Testes futuros:
[aceitação](ACCEPTANCE.md). Nenhum contrato API existente é alterado nesta OP.

## 1. Contrato aprovado

| ID | Documento/operação | Efeito funcional |
| --- | --- | --- |
| F01 | FT / ND | Podem criar posição devedora, identificada por origem e moeda |
| F02 | NC | Cria crédito autónomo; a fatura de origem aberta ou paga não muda este efeito |
| F03 | FR | Faturação e pagamento imediato; efeitos identificáveis sem duplicar recebimento |
| F04 | RC | Liquidação de dívida e recebimento; dívida extinta separada de dinheiro recebido |
| F05 | NL | Liquidação de créditos, compensação/reembolso; classificação documental por validar |
| F06 | Compensação | Afectação expressa entre sinais opostos; pode ser parcial ou sem dinheiro |
| F07 | Desconto no RC | Componente não monetária explícita; não gera NC automaticamente |
| F08 | Adiantamento | Crédito distinto de NC; ativação depende de tratamento fiscal/documental |
| F09 | Anulação | Estorno preserva original e respeita dependências efetivas |
| F10 | Conta corrente | Dívida, crédito, saldo líquido, afectações e caixa separados, por moeda |

Montantes das posições são magnitudes positivas; natureza DEVEDORA/CREDORA e
efeito congelado dão o sinal. Uma designação editável de tipo não determina o
histórico. Dívida aberta D, crédito disponível C e saldo S=D−C são três medidas.
D=C=1.000 implica S=0 e duas posições abertas. Só uma operação expressa as
liquida. Não compensar automaticamente por cliente, sinal, montante ou ligação
entre NC e FT. Emitir NC nunca é, por si só, afectar crédito a essa FT.

O RC pode integrar posições devedoras e credoras, incluindo FT e NC, numa única
operação de liquidação. RC e NL utilizam o mesmo motor financeiro de posições,
afectações e movimentos monetários, embora possam ter requisitos documentais
e fiscais distintos. Mantêm-se os gates de classificação documental e das
liquidações sem movimento monetário.

Uma operação pode seleccionar várias dívidas e créditos do mesmo cliente/moeda,
consumir parte de cada crédito e combinar dinheiro, desconto e compensação.
Uma NC não pode ser encerrada enquanto conservar crédito disponível.
Deve apresentar antes/depois, origens, montante de cada afectação e caixa real.
Compensação sem caixa está aprovada funcionalmente; não emitir presumidamente
um RC fiscal de zero enquanto a classificação/documentação não for validada.

Desconto20 + dinheiro980 extingue dívida1000. Registar os três valores e a
natureza do desconto. Não presumir ausência de efeito fiscal; um desconto que
exija regularização segue o circuito validado, sem NC automática universal.
Recebimento1000 para dívida800 pode criar adiantamento200; esse fluxo permanece
inativo até ao gate legal. Reembolso consome crédito e regista saída de caixa;
compensação consome crédito e dívida sem caixa.

## 2. Rectificações e emissão

NC/ND têm modo assistido por uma ou várias faturas e modo livre fundamentado,
incluindo rappels/descontos globais. A referência de origem e a afectação
financeira são relações distintas. Não exigir seleção manual de cada linha
como regra universal. Propor conjuntos de origem pesquisáveis/importáveis,
com critérios, membros congelados, valores e suporte documental conservados.
Não substituir referências legais por um filtro que muda depois da emissão.

O modo livre exige os elementos legais aplicáveis; se a origem histórica ou
fundamento necessário não for demonstrável, bloquear a emissão e encaminhar
para validação, sem inventar taxas. Rectificações sucessivas conservam cadeia,
quantidades/bases/imposto já rectificados e limites para evitar excesso.
Exceções para rectificação global exigem regra validada, não inferência técnica.

Reutilizar o motor monetário (HALF_UP por linha, duas casas nos valores fechados,
quantidade/preço até seis) e motor fiscal, mas fornecer contexto histórico
congelado. Não aplicar taxa/regime atual a emitido antigo. IVA calculado e
liquidado permanecem distintos. Rectificação tem seu próprio snapshot e
referências, sem modificar snapshot V1/V2/V3 do original.

Emissão futura: validar tipo/circuito autorizado, origem/fundamento, moeda,
cronologia e versão de rascunho; reservar número sob bloqueio da série e
revalidar cronologia após esse bloqueio. Uma transação grava documento,
snapshots, posições/movimentos e auditoria; falha não deixa efeito parcial.
Preservar numeração, ATCUD e QR emitidos. Projeção fiscal e efeito financeiro
são separados. Não atribuir código AT a NL nesta especificação.

Estados conceptuais propostos: documento RASCUNHO/EMITIDO/ANULADO; operação
CONFIRMADA/ESTORNADA; posição ABERTA/PARCIAL/ESGOTADA derivada do residual.
ANULADO não apaga documento. Estornar finanças não altera automaticamente o
estado fiscal da fatura; anulação fiscal segue requisitos próprios.

## 3. Alternativas de arquitetura

| Alternativa | Vantagem | Limite | Decisão proposta |
| --- | --- | --- | --- |
| Apenas pendente com valor negativo | Mudança curta | Confunde origem, caixa e afectação; histórico dependente de saldos | Rejeitar |
| Saldo derivado apenas de documentos | Reutiliza extrato | Saldo zero não prova liquidação; não representa afectações | Só leitura de compatibilidade |
| Event sourcing integral | Replay completo | Nova infraestrutura conceptual e migração ampla | Não necessário nesta fase |
| Posições + movimentos preservados + afectações relacionais | Rastreabilidade, transações SQL, evolução aditiva | Exige reconciliação e disciplina de escrita | Recomendada, sujeita à aprovação da OP futura |

Sem Redis ou novo serviço. PostgreSQL/JPA e auditoria existentes são reutilizados.
Não tratar esta recomendação como aprovação da implementação ou schema concreto.

### Entidades conceptuais

- Posição: ID, empresa/cliente/moeda, natureza, origem única, valor original,
  residual projetado, versão e classificação (dívida, NC, adiantamento).
- Operação: ID, chave de idempotência, hash canónico do pedido, autor/data,
  estado, motivo, documento associado e versão de política monetária.
- Movimento: criação/consumo/restauração da posição; valor positivo, efeito
  explícito, operação e referência do movimento estornado. Append-only.
- Afectação: liga fonte de dinheiro/crédito/desconto a destino dívida/reembolso;
  valor e moeda, origem, versões observadas, componentes e suporte documental.
- Caixa: entradas e saídas distintas, método/data/referência; zero é ausência
  de movimento monetário, não um pagamento fictício.
- Dependência: consumo de recurso criado por outra operação, referência de
  rectificação ou outra relação impeditiva validada; não mera ordem cronológica.

Saldo original menos consumos mais estornos deve igualar residual projetado.
Uma atualização de projeção é permitida; movimentos confirmados não são
reescritos. Unicidade origem/posição e operação/chave evita duplicação.
Afectações repetidas da mesma posição no pedido são normalizadas ou rejeitadas
antes de gravar, nunca processadas como consumo independente não controlado.

### Conservação e precisão

Por operação e moeda, sem conversões implícitas:

`dívida extinta + dinheiro devolvido + novo crédito de adiantamento = dinheiro recebido + crédito consumido + desconto não monetário`.

Afectações provam cada termo; uma igualdade agregada não substitui a validação
individual dos limites. Compensação800 gera consumo800 em ambos os lados,
sem caixa; reembolso200 consome crédito200 e gera saída200. Criação de NC é
originação de posição e não uma operação de liquidação nesta equação.
FR deve ligar sua dívida e pagamento uma única vez e resultar em residual0.

Os seis decimais do financeiro legado não devem ser silenciosamente reduzidos.
Proposta para operações EUR novas: valores monetários fechados a cêntimos;
política de distribuição de resíduos explícita, determinística e versionada,
sem criar dinheiro. Migração preserva precisão original. Escalas de outras
moedas e conversão fiscal exigem decisão normativa antes de ativação.

### Concorrência e idempotência

Uma transação inclui idempotência, bloqueios, validação, movimentos/projeções,
numeração e auditoria. Definir ordem global de locks para todos os escritores,
incluindo emissão, liquidação e estorno: séries por ID quando necessárias,
documentos por ID, posições por ID; confirmar desenho concreto na OP técnica
para evitar inversão com os locks comerciais existentes.
Após bloquear, verificar residual/estado/versão, cliente e moeda. Consumo nunca
excede disponível; conflito falha integralmente. Proteção por versão é adicional,
não substitui validação transacional dos consumos.

Chave única por empresa e tipo de operação: pedido idêntico retorna resultado
original, mesmo após timeout; mesma chave com outro conteúdo falha em conflito.
Não reservar novo número no retry. Comportamento de erro/rollback e chave ainda
em curso tem de ser testado. Auditoria de recusa conserva mecanismo isolado
existente; sucesso acompanha commit de negócio.

### Estornos e história

Sem DELETE, restauração cega de saldo antigo ou igualdade de saldo como prova
de ausência de dependência. Estorno cria movimentos inversos, referências ao
original e motivo/autor/data. Estornar operações dependentes em ordem topológica
inversa; bloquear enquanto existir dependência ativa que torne o estorno
inválido. Operações independentes posteriores não bloqueiam por data.
Pagamentos independentes à mesma dívida podem coexistir: desfazer um reabre sua
parcela, conservando o outro. Consumo de adiantamento depende da criação desse
crédito; desfazer a criação exige desfazer primeiro o consumo. Ciclos são erro.
Estorno concorrente ou repetido não pode restaurar duas vezes.

Snapshots futuros incluem emitente/cliente, tipo e efeito financeiro, moeda,
política, termos monetários e referências. PDF/extrato não usam mestre atual
para reconstruir significado histórico. Reutilizar auditoria sem a tornar o
único ledger; auditoria e movimentos financeiros têm finalidades distintas.

## 4. Migração proposta, sem execução

1. Inventariar documentos, pendentes, linhas financeiras e sinais configurados;
   produzir reconciliação por cliente/moeda/origem numa cópia protegida.
2. Introduzir estruturas aditivas por OP específica; manter leitura antiga e
   identificar versão/proveniência de cada registo importado.
3. Importar dívidas/recebimentos demonstráveis, descontos e caixa separados,
   preservando seis casas e IDs/referências; FR sem dupla contagem.
4. NC legada, tipo alterado, origem incerta ou saldo incoerente: marcar exceção,
   preservar original e impedir importação automática como verdade fiscal.
   Não reinterpretar pendente positivo como crédito sem evidência.
5. Validar totais e afectações individualmente; gerar relatório de diferenças,
   resolução aprovada e critério zero-diferença para casos demonstráveis.
6. Ensaiar idempotência do import, backup/restauro e rollback operacional em
   cópia. Só depois gate humano para deployment/cutover; nunca dual-write sem
   transação comum. Não recalcular emitidos nem alterar V16/V17.

## 5. Contratos futuros de leitura e interface

Comandos conceptuais (não endpoints implementados): preparar rectificação,
confirmar liquidação com afectações e chave, consultar resultado, estornar com
motivo e dependências. Valores como decimais exatos; incluir cliente/moeda,
IDs/versões, entradas/saídas, desconto e origem. Backend calcula e valida.

Leitura: dívida/crédito/saldo separados por moeda; residual e história de cada
posição; operação com caixa e componentes; diagnóstico de dependências.
Paginação com desempate por ID e filtros preservados. Estado liquidado decorre
do residual, não de booleano marcado num pagamento parcial.

UI futura: seleção de ambas as naturezas; preview explícito antes/depois;
crédito não consumido permanece disponível; mostrar desconto separado e caixa
zero sem simular dinheiro; pedir confirmação; retry seguro. Não somar moedas
num total etiquetado EUR. Preservar autorizações vigentes; novos gates ou fluxos
exigem avaliação própria, sem inventar permissões nesta OP.
