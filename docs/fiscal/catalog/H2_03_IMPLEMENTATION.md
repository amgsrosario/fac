# H2-03 — Contrato de configurações FT governadas (issue #39)

## Autoridade e fronteira

Implementação local de H2-03/R1, sobre `c17a9d8af9cf2d197c8c8c21f1f780a38efee0d9`
(PR #37). Ordem/handoff: AR2-Executive-Reports, commit
`4032d5742ac4a59cd3075f9d2dad85949a5a70ef`, ficheiro
`TUULI/2026/10/2026-10-27_TUULI_Issue38_R2_fecho-e_handoff_H2-03.md`.
A fonte H2-02 permanece em
[arquivo administrativo](../../governance/sources/2026-10-23_H2-02_deliberacao-administracao-AR2.md).

Foi lida integralmente a especificação externa
`2026-10-23_TUULI_H2-03-R1_especificacao-tecnica_Codex.md`, SHA-256
`46ceb0975e83f595a7e720f20687008a3fe492984ae7dd7e773db794624b0292`,
e o parecer incremental Claude que a aprovou. Essa aprovação documental não
é uma revisão independente desta implementação.

O perímetro é **código fiscal explícito FT e liquidação não imediata**.
Não se infere identidade fiscal por ID, nome ou área. RC, RL, guias sem código
e restantes configurações fora deste perímetro conservam os contratos existentes.
Não há identidade fiscal universal obrigatória nem homologação implícita de RL.

## Contrato de release e de persistência

`FiscalCatalog` lê, apenas quando utilizado, o manifesto governado. A propriedade
opcional `creationBindings.ftDeferred` contém a chave exacta
`{fiscalCode: "FT", revision: inteiro positivo}` para novas configurações.
A definição tem de constar do manifesto e da materialização V27; a verificação
compara o conjunto integral e valores JSON sem conversão para `double`.
Não se selecciona a revisão maior, mais recente ou fornecida pelo cliente.

A definição FT usa os quatro campos `schemaVersion` (1), `fiscalCode` (FT),
`revision` (inteiro positivo de 64 bits) e `semantics`. Este último contém apenas
`allowedConfigurations`: lista não vazia de tuplos completos e únicos, cada um
com `codigoFiscal` (FT), `areaGestao` (inteiro 1–99), `entidade` (inteiro 1–9),
`sinalContabilistico` (inteiro 1–2) e `liquidacaoImediata` (false).
Os intervalos são limites técnicos de representação, não combinações homologadas.
Não se constrói o produto cartesiano de atributos de tuplos diferentes.
Campos desconhecidos, duplicados, coerções e estruturas inválidas são recusados.

`TipoDocumentoService.create` exige modalidade explícita para FT; resolve a chave,
valida o tuplo e persiste a nova entidade com `persist`/`flush` na transacção.
Usar INSERT em vez de merge evita que uma criação concorrente actualize outra
configuração com o mesmo ID. O DTO de criação não aceita uma revisão do cliente.
O DTO de leitura devolve `fiscalCatalogCode` e `fiscalCatalogRevision` realmente
persistidos. A associação JPA é inserível e não actualizável.

Na alteração, o serviço bloqueia a configuração e valida todos os atributos
classificativos contra a **mesma revisão**, mesmo antes da primeira emissão.
V25 continua a impedir alterações classificativas após emissão. Uma configuração
sem vínculo não o recebe por UPDATE: a conversão para FT não imediata exige nova
configuração governada. Uma alteração apenas descritiva/técnica não reclassifica.

## V28 e caminhos alternativos

A V28 é aditiva: funções e triggers de validação, sem dados fiscais, backfill,
remoção ou actualização de documentos/tipos. V25, V26 e V27 não são modificadas.

- A definição FT inserida na tabela V27 tem de respeitar o contrato estrutural.
- INSERT e UPDATE de configurações vinculadas validam o tuplo na revisão exacta.
- Novas FT não imediatas sem vínculo e reclassificações incompatíveis são recusadas.
- UPSERT, COPY, INSERT SELECT, operações multi-row e aplicações antigas ficam
  sujeitos aos mesmos triggers. A protecção não depende do mapper ou do Java.
- V27 continua a proteger o par, a FK e a imutabilidade de vínculo/revisão;
  V25/V26 preservam configuração emitida e associação documental histórica.
- Registos antigos permanecem como estão, sem atribuição retrospectiva de revisão.
  Isto não os declara aptos para a base comercial nem institui excepções por ID,
  data de migração ou marcador de grandfathering.

Um binário antigo pode ler os campos anteriores mas não recebe autorização para
escrever configurações inválidas. Não se remove V28 nem se relaxam triggers como
rollback operacional. A recuperação de uma instalação/restauro incompatível é
uma operação governada, sem correcção automática ou reclassificação inferida.

## Prontidão interna e restauros

`FiscalConfigurationReadiness.inspect()` é um serviço interno de leitura, sem
endpoint nem consumidor operacional novo. Inventaria configurações, vínculo e
contagens de documentos, em consulta agregada ordenada, e verifica o catálogo
em consultas de número fixo (sem N+1).

Sinaliza FT não imediatas sem vínculo, caminhos de emissão sem identidade
explícita, histórico emitido sem identidade, catálogo indisponível e vínculo
incompatível. As áreas servem apenas para identificar caminhos implementados,
nunca para atribuir um código fiscal. A correcção/exclusão da futura base comercial
exige classificação comprovada e operação própria; o serviço não altera dados.
`configurationReady()` avalia este contrato de configuração;
`operationalActivationPermitted()` permanece sempre false.

Dump completo pode transportar configurações já existentes; após restauro/upgrade
a prontidão tem de ser inspeccionada antes do uso comercial. Um data-only que
introduza novas FT incompatíveis é recusado. O antigo script P16 que atribui FT
por ID não constitui uma fonte fiscal: o ensaio controlado demonstra a sua
recusa e rollback no caso abrangido. Os fluxos de instalação, seeds e restauros
GOLDEN/demo/dev precisam de validação no seu ambiente autorizado antes de uso;
nenhum seed real ou ambiente existente foi alterado nesta missão.

## Gates e estado de entrega

O manifesto operacional continua **vazio**, sem `creationBindings` nem definição
FT publicada. Sem chave/materialização homologadas, a criação FT governada
falha em fechado. As revisões e tuplos dos testes são exclusivamente sintéticos.
Não publicar 2/1/1, códigos SAF-T, projecções QR/PDF ou outras combinações sem
homologação própria. FR/imediata, RC, RL, NC/ND e demais modelos não são
implementados por esta fatia.

Nenhuma alteração a emissão, numeração, ATCUD, QR/PDF, SAF-T, saldos, recebimentos,
consumidores financeiros, base histórica, demo ou preparação isolada #38.
FinancialCore permanece inactivo, legado mantém a autoridade operacional,
sem cutover e com Fatia 1e suspensa.

Implementação preparada para **revisão Claude independente ainda pendente**.
Ver [validação e limitações da evidência](H2_03_VALIDATION.md).
