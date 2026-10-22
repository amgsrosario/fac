# Primeira fatia técnica — fundação inactiva do catálogo

Estado: infraestrutura implementada localmente e testada, pendente de revisão Claude. D-CAT-1/D-CAT-2A/D-CAT-3
aprovadas; D-CAT-2 não homologada. Base documental PR #34 + d2ebd0b.

## Objectivo e perímetro

Preparar o mecanismo de definição única versionada, materialização PostgreSQL,
consumo Java e verificação de coerência. Revisões imutáveis e vínculos estáveis.
Não incorporar matriz não homologada em seeds, enums ou validadores executáveis.
FT/FR são duas especializações da tipologia FT, não códigos independentes no
contrato-alvo. RC/RL não ganham opção funcional de liquidação imediata.

Superfície prevista: schema/repositório de revisões governadas, carregamento
Java a partir da definição única e validação de coerência; não criar editor
fiscal, endpoint, adaptação de leitores históricos ou alterações de emissores.
Concretização local: V27__inactive_fiscal_catalog.sql, fiscal_catalog_revision,
vínculo opcional em tipodocumento e serviço interno FiscalCatalog.
[Evidência, execução e limites](VALIDATION.md).

Sem matriz homologada, o incremento implementável é a infraestrutura inactiva:
nenhuma regra fiscal concreta não aprovada, nenhuma atribuição de revisão a
tipos já existentes e nenhum catálogo operacional apresentado como completo.
Fixtures sintéticos de testes só provam mecanismo de versões/coerência; não
se tornam definições fiscais de produção nem inventam Código Fiscal RL.
A ligação a novos tipos e validação material por tipologia só entram após
homologação D-CAT-2; até lá não há mudança no CRUD ou na emissão existentes. A estrutura
SQL já suporta vínculo fornecido no INSERT de uma configuração nova, sem
atribuição retroactiva; não é validação material da matriz ainda não homologada.

## Invariantes e aceitação técnica proposta

- Definição semântica única materializada sem deriva; incoerência detectada antes
  de usar o catálogo, sem fallback que produza classificação silenciosa. A política
  concreta de falha deve respeitar a fase inactiva e não bloquear o legado por
  ausência de catálogo ainda não habilitado.
- Revisões governadas imutáveis; alteração cria nova revisão sem modificar
  referências existentes. Sem resolver a revisão «actual» para reinterpretar
  emitidos. Semântica fiscal não editável pelo utilizador.
- V25/V26 mantidas; não reatribuir tipos financeiros, não alterar cinco atributos
  bloqueados, não forçar reclassificação antiga. Nenhum backfill inferido.
- PostgreSQL descartável: upgrade aditivo preserva dados e segundo arranque; SQL
  directo não modifica revisões publicadas ou vínculos protegidos; concorrência
  entre publicação/vínculo não cria revisão mutável ou identidade ambígua.
- Java e SQL usam a mesma definição, com provas de igualdade; fixtures não
  demonstram aprovação de matriz fiscal. Regressão V25/V26, RC/não-RC, Data Valor
  e leitores legados deve passar no âmbito afectado.
- Não activar RC/RL/FinancialCore operacional/1e nem alterar demo. Sem API nova,
  efeitos monetários ou fiscalidade presumida.

Estes são critérios técnicos propostos para execução posterior, não testes
executados nem homologação de D-CAT-2. Não criar fundação vazia e declará-la
catálogo funcional completo: o ganho desta fatia é integridade de publicação e
identidade governada; validação de configurações depende da matriz.

## Pedido de revisão cruzada Claude

Rever o diff de implementação complementar a 79766bc nesta branch, exclusivamente
por leitura, com contexto dos contratos d2ebd0b/79766bc.
Confirmar que D-CAT-3 está aprovada apenas nos termos comunicados, D-CAT-2 não
foi homologada por inferência e códigos/Entidades/sinais novos não foram criados.
Confrontar a matriz com as fontes já ligadas; verificar fronteira desta fundação
inactiva, V25/V26 e ausência de reinterpretação histórica.
Emitir APPROVE / APPROVE WITH CONDITIONS / REQUEST CHANGES com BLOCKER, MAJOR
e MINOR. Não corrigir ficheiros nem executar migrations em bases persistentes.
Claude não disponível no executor desta preparação: comando ausente do PATH e
nenhuma ferramenta Claude exposta. Pedido pronto para envio/execução humana;
não foi efectivamente entregue a um revisor externo nesta sessão.
