# D-CAT-2/H1.1 — Especificação FT não imediata

23-10-2026. Base reconhecida: 089b18165f793ae3059ee6f7da5d1d92e4de9ec6,
PR #35 integrado. Estado: especificação para revisão, nenhuma implementação.
Autoridade: ordem executiva H1.1; [ADR-003](../../governance/decisions/ADR-003-fiscal-catalog.md)
e [matriz com reservas](D-CAT-2_MATRIX.md). D-CAT-1/2A/3 preservadas.

## 1. Contrato e fronteira

A homologação semântica FT/FR/RC não aprova todos os valores técnicos.
Esta primeira fatia futura admite apenas novas configurações FT de liquidação
posterior: tipologia interna FT e liquidacao_imediata=false. Constituem dívida
do cliente quando emitidas. Não alterar o emissor, a autoridade financeira,
saldos ou extractos. Não activar o FinancialCore RC nem substituir consumidores.

FR e qualquer FT imediata, incluindo aliases, estão excluídas. RC permanece
excluído até comprovação do enquadramento fiscal necessário; RL/NC/ND e todos
os restantes tipos também. Esta exclusão limita o novo caminho governado e não
bloqueia por arrasto caminhos legados fora do mandato.

Não confundir tipologia interna, código SAF-T e representação QR/PDF. A nova
configuração não pode alterar uma projecção fiscal por mera cópia do catálogo.
Qualquer necessidade de modificar QR/PDF exige projecção validada e nova ordem.

## 2. Componentes e percurso técnico proposto

- `fiscal/catalog.json`: definição única governada, publicada em release;
  manter schemaVersion, fiscalCode, revision e semantics da V27. Não inventar
  a definição material nem o número da revisão antes da validação dos valores.
- Migration posterior aditiva (número a determinar pela base então actual):
  materializar exactamente a revisão aprovada; nunca editar V25/V26/V27.
- `FiscalCatalog.revision`, `verifyCoherence`, `forType`: reutilizar verificação
  integral, chave explícita e cópias defensivas; nenhuma procura da última versão.
- `TipoDocumentoService`, repository/model/mapper e DTO de criação: futura
  adaptação mínima para seleccionar internamente a revisão governada e gravar
  vínculo no INSERT. Não aceitar JSON semântico arbitrário nem revisão livre
  pelo utilizador. DTO de actualização não permite reatribuição.
- `TipoDocumentoController`: reutilizar contrato existente quando compatível;
  se precisar de alterar API pública, apresentar o contrato antes de implementar.
  Nenhum editor fiscal novo. Interface sem alterações nesta primeira proposta.
- PostgreSQL: complementar validação material do novo vínculo, além da FK V27;
  SQL directo não pode inserir uma configuração vinculada com especialização,
  área, Entidade ou sinal incompatíveis com a revisão. Não aplicar normalização
  retroactiva a configurações sem vínculo.

Antes de criar: validar perímetro FT não imediata, campos obrigatórios e
combinação homologada, disponibilidade da revisão e coerência completa entre
manifesto e PostgreSQL. Persistir configuração e vínculo atomicamente. Recusar
revisão ausente/divergente sem configuração parcial ou fallback. Sem numeração
ou emissão neste acto; nenhuma chamada aos emissores financeiros.

## 3. Gate de valores — não tomar decisões pelo PO

O seed demonstra 2/1/1 para área/Entidade/sinal, mas não homologa a combinação.
Antes de materializar uma revisão executável, apresentar a correspondência
comercial/cliente/débito e obter confirmação suficiente da combinação permitida.
Não ampliar os intervalos do DTO para uma matriz fiscal presumida. Se a prova
não existir, parar só a definição material; não preencher semantics com candidatos
nem apresentar fixtures sintéticos como revisão de produção.

O contrato técnico está especificado; a definição fiscal executável depende
desta reserva expressa. Homologação semântica não deve ser reapresentada ao PO.
Não há decisão nova sobre moeda, rateio, IVA de caixa, anulação FT ou cutover.

## 4. Compatibilidade e histórico

V27 exige par código/revisão e codigo_fiscal coerentes, apenas no INSERT.
Não associar revisão a tipos existentes, mesmo nunca emitidos, porque V27
proíbe NULL→revisão em UPDATE. Criar nova configuração quando autorizada,
sem converter automaticamente a antiga. V25 continua a congelar os cinco
atributos após primeira emissão, inclusive após anulação; V26 mantém a associação
do documento financeiro ao seu tipo. Revisões e vínculos são imutáveis.

Leituras de tipos vinculados resolvem a chave persistida, nunca a revisão mais
recente. Leitores legados e tipos históricos sem vínculo permanecem inalterados;
não atribuir classificação desconhecida nem recusar leituras antigas nesta fatia.
Não adicionar snapshots redundantes, backfill, migração ou reclassificação.
Ausência de manifesto continua sem impedir inicialização do legado; uso do
catálogo falha em fechado. Nenhum dado demo ou persistente é usado nos testes.

## 5. Critérios verificáveis e testes futuros

Todos abaixo são testes a implementar/executar, não resultados desta missão.

| ID | Aceitação e prova requerida em PostgreSQL descartável |
| --- | --- |
| FT01 | Nova FT não imediata e combinação homologada guarda uma única chave governada; Java/SQL devolvem conteúdo idêntico |
| FT02 | FR, FT imediata por qualquer ID e demais tipologias recusadas no novo percurso; legado fora do perímetro preservado |
| FT03 | Revisão ausente, divergência de manifesto, FK/par inválido e semântica incompatível recusados antes de gravar configuração |
| FT04 | SQL directo não contorna validação material, nem altera/apaga revisão ou reatribui vínculo; rollback integral |
| FT05 | Publicação/vinculação concorrentes em READ COMMITTED e REPEATABLE READ não deixam referência parcial; testar commit, rollback e retry, sem presumir espera da FK |
| FT06 | Nova revisão não muda configuração anterior; edição permitida de campos não protegidos e recusas V25/V26 mantidas após emissão/anulação |
| FT07 | Upgrade desde V27 preserva tipos/documentos, saldos e vínculos NULL; validate e segundo arranque sem migrations adicionais |
| FT08 | Nova FT conserva QR/PDF e código fiscal esperados por projecção validada; nenhuma FR numerada pelo novo caminho; comparar documentos novos e preservar históricos |
| FT09 | Regressão FiscalCatalogTests, DocumentTypeImmutabilityTests, FinancialDocumentClassificationTests e testes de CRUD/emissão comercial afectados; package |
| FT10 | Legado arranca sem catálogo utilizável; novo uso falha em fechado, sem fallback; RC novo/consumidores/1e continuam inactivos |

Verificar número executado/aprovado/falhado/ignorado e arquivar logs/XML; CI
mínimo não substitui integração PostgreSQL. Aplicar git diff --check. Testes
browser só se a implementação futura afectar interface, fora desta proposta.

## 6. Reversibilidade e entrega futura

Separar disponibilidade da revisão da utilização por novas configurações.
Uma retirada do novo percurso deve impedir novas adesões, preservando todos os
vínculos/revisões já gravados e as leituras compatíveis. Não efectuar down migration,
apagar definições, limpar dados ou retirar validações persistentes. O plano de
rollback deve provar que a versão anterior da aplicação consegue ler os dados
novos; caso contrário, rollback de binário fica bloqueado e exige correcção em
frente. Não prometer reversão destrutiva de uma revisão imutável.

Só iniciar execução com ordem funcional expressa, reserva de valores resolvida,
projecção FT confirmada e revisão independente desta especificação. Após futura
implementação: PostgreSQL dirigido/regressão, package, revisão Claude, CI e merge
humano conforme normas. Esta ordem prepara documentação, não publica definições.

## 7. Pedido incremental Claude e validação documental

Rever esta especificação e o diff institucional sobre a base 089b181: fidelidade
à homologação semântica, exclusões, vínculo V27, defesa SQL, V25/V26, históricos,
reserva área/Entidade/sinal, QR/PDF e rollback. Emitir APROVADO / APROVADO COM
RESERVAS / DEVOLVER PARA CORRECÇÃO, com BLOCKER/MAJOR/MINOR e localizações.
Revisão exclusivamente de leitura. Não implementar nem homologar reservas.

O parecer Claude D-CAT-2/02 APROVADO é anterior e não aprova este novo diff.
Claude não disponível no PATH nem nas ferramentas deste executor; pedido
preparado para revisão externa, não efectivamente submetido. Sem parecer
positivo desta alteração não se declara PR pronto nem se publica nesta entrega.
