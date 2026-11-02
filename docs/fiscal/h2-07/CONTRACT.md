# H2-07 — corte experimental inactivo de assinatura

Mandato: [issue #44](https://github.com/amgsrosario/fac/issues/44), D-SIGN-01
parcialmente aprovada. Base integrada observada: `ac07247` (PR #42).
Dossier H2-06 aprovado: `10483f8`, incorporado exclusivamente como documentação,
com rastreabilidade nos commits desta branch. D-SIGN-02..05 continuam pendentes.

## Isolamento e proveniência

Worktree nova `h2-07-signature-isolated`, branch `codex/h2-07-signature-isolated`.
A worktree `h2-07-signature-inactive` e commit `7ce573f` foram preservados;
nenhum código, SQL ou teste dessa implementação foi lido como fonte, copiado,
cherry-picked ou incorporado. Os metadados de caminhos e hashes foram usados
somente na investigação de proveniência autorizada e publicada na #44.

A base integrada contém V1–V28. Existe V29 apenas na implementação preservada,
não na base integrada. Este corte **não cria V29**: `db/signature-trial` é uma
localização Flyway separada e explícita, com history `h207_trial_history` e
schema `signature_trial`. Não é carregada por `db/migration` nem pelo arranque
normal. A migration de ensaio recusa bases sem nome `h207_<32 hex>_test`.
Não é uma migration comercial alternativa nem uma solução para históricos.

## Isolamento R1 do artefacto produtivo

02-11-2026, MINOR-003: as três classes de ensaio foram movidas de
`src/main/java` para `src/test/java`, mantendo o pacote e os testes.
O SQL foi movido de `src/main/resources/db/signature-trial` para
`src/test/resources/db/signature-trial`. Nenhum deles integra o JAR produtivo.
Não se optou por manter a biblioteca no artefacto com um guard de utilização.
A compilação produtiva não depende das classes de teste. O runner R1 executa
build limpo e inspecciona entradas/referências do JAR empacotado, falhando se
nele encontrar classes, SQL ou referências à biblioteca experimental.

## Componentes implementados

- `TrialCanonical`: snapshot experimental apenas FT, EUR, sem liquidação
  imediata. Datas sem fracções de segundo; recusa de data documental posterior
  ao instante do snapshot. Recusa precisão monetária que exigiria arredondamento.
  Mensagem UTF-8 com cinco campos separados por ponto e vírgula. Não usa DTO,
  locale, Data Valor ou data de vencimento como fonte implícita.
- `EphemeralTrialSigner`: par RSA-1024 efémero gerado apenas pelo ensaio,
  SHA1withRSA/PKCS#1 v1.5, Base64 de 172 caracteres; sem importação, exportação
  privada, keystore, certificado ou número R fictício. É o perfil do exemplar
  normativo, **não validação de vigência/aplicabilidade produtiva**.
- `TrialProofEngine`: API Java explícita, não bean, não controller e não
  implementação de `FiscalDocumentSignatureSource`. Recusa datasource fora
  de localhost ou sem sufixo `_test`, e ligação JDBC em autocommit durante
  append. Exige scope e série de ensaio previamente criados, prefixo H207T.
- Prova experimental imutável com identidade do documento sintético, série/tipo
  fiscal FT, sequência, mensagem, montante, datas, antecessor, assinatura e chave
  pública. Nunca cria documento comercial, pendente ou movimento financeiro.
- Leitura verifica conteúdo e criptografia antes de devolver a prova e a
  projecção marcada `NONFISCAL:`. Essa projecção não é payload AT, PDF fiscal
  ou fonte produtiva; R não é fabricado. Releitura não reassina.

O snapshot e a série são sintéticos e não podem provar a ligação a uma FT
operacional real. A futura integração de emissão, certificado, projecção
QR/PDF, SystemEntryDate real e exportação exige outro mandato e os gates.
Não se apresenta a API experimental como emissor fiscal.

## Transacção e concorrência

Lock primeiro no scope de ensaio (serializa também relógio global), depois na
identidade série/tipo fiscal. O predecessor é lido/verificado sob lock;
replay do mesmo documento/payload devolve a prova, payload ou série diferente
é conflito. É contrato de ensaio, não alteração ao HTTP comercial existente.

A cadeia é por scope/série/tipo fiscal FT e não por um ID configurável de
TipoDocumento. Não se mapeiam FR/RC ou outras famílias por analogia. A passagem
de exercício mantém a cadeia plurianual sem reset. Data documental regressiva
na série e SystemEntryDate regressivo no scope são recusados. O ensaio recusa
qualquer data documental futura, sem implementar a faculdade produtiva de
alerta/emissão futura do §3.4.2. O fuso do instante operacional não é escolhido:
o teste recebe LocalDateTime sintético, à precisão fixada no oráculo.

Rollback testado com entidade JPA de testemunho e provas JDBC no mesmo
JpaTransactionManager/HibernateJpaDialect/DataSource. Não demonstra sozinho
integração com a emissão operacional/FinancialFtOrigin, que não foi alterada.
Não existe chamada externa nem promessa de rollback de HSM/KMS.

## Integridade e limites

PostgreSQL protege sequência, predecessor, cronologia, canonicalização,
identidade e imutabilidade contra UPDATE/DELETE/TRUNCATE, incluindo constraints
antecipadas/diferidas. O scope serve de lock; a cronologia considera também
as provas persistidas. Chave diferente na cadeia é recusada: rotação não foi
aprovada para esta fatia. Históricos e séries comerciais não são importados.

SQL estrutural **não verifica RSA**. Um INSERT directo estruturalmente válido
com assinatura forjada pode ser armazenado; leitura e novo append verificam
criptografia e recusam esse head. Há teste explícito desta limitação, não se
promete prevenção criptográfica SQL. Não há consumidor produtivo desse schema.
Superutilizador, desactivação de triggers e host comprometido não são ameaças
resolvidas por esta biblioteca. Custódia produtiva e verificação criptográfica
no perímetro comercial continuam matéria futura.

### Limite de confiança da chave pública — MINOR-002

A chave pública está guardada na própria prova experimental. Uma verificação
RSA bem-sucedida demonstra coerência entre mensagem, assinatura e essa chave;
**não demonstra a proveniência ou autorização dessa chave**. Não há uma âncora
independente de confiança. Um atacante capaz de inserir uma prova com a sua
própria chave e assinatura coerentes pode passar essa verificação. A detecção
de assinatura inválida ou de chave diferente numa cadeia iniciada não resolve
a autenticação do primeiro par de chave/prova nem de uma cadeia substituída.

No desenho produtivo, a chave pública/keyVersion e o âmbito autorizado devem
estar vinculados a um registo confiável separado das provas, protegido contra
substituição pelo escritor das provas e com proveniência/rotação verificáveis.
O leitor deve verificar a identidade autorizada nesse registo e depois a
assinatura; não confiar automaticamente na chave recebida do próprio registo
assinado. D-SIGN-02..05 decidirão custódia/proveniência/gestão segundo os gates.
Esta R1 não implementa custódia real, registo de confiança ou certificação.

## Fronteira mantida

Sem fonte real registada, HTTP 409 permanece. Sem alterações a emissão,
anulação, numeração, DTO públicos, QR/PDF operacionais, manifesto, V25–V28,
demo, bases persistentes ou autoridade financeira legada. Sem activação de
catálogo, RC/RL, FinancialCore, Fatia 1e, deploy ou cutover. Gates 2/1/1,
QR/PDF, AT, RC/RG, FAQ 4443 e D-SIGN-02..05 não foram encerrados.
