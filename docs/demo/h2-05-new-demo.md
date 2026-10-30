# H2-05 — Instalação demo nova e novo GOLDEN

Procedimento para uma operação futura expressamente autorizada. A R2 apenas
documenta estes passos: **não executa instalação, promoção, restauro ou deploy
na Ubuntu nem altera a demo existente**. Os ensaios anteriores foram em bases
descartáveis; não constituem aprovação de um GOLDEN operacional novo.

## 1. Separar a instalação antiga da nova

Preservar a instância actual, backups, GOLDEN, metadados, symlinks e logs. O
GOLDEN antigo com códigos DEMOFT26/DEMORC26 e payloads anteriores não é compatível
com o fornecedor H2-05 activo; sem fornecedor, nova emissão continua 409.
Não converter séries, editar documentos ou limpar o dump para contornar guards.

Preparar outro directório de aplicação e **outro cluster PostgreSQL 16.3**,
com contentor/volume/rede/portas próprios e identificados. A nova base chama-se
`fac_demo`. Não usar `fac/facdb`, o destino #38 ou recursos da Partner Demo.
Não basta mudar o nome lógico da base: conferir `system_identifier`, host,
contentor, volume e datasource. Guardar a autorização desse destino.

Usar o commit final indicado no relatório R2, após aprovação independente e
autorização de instalação; verificar HEAD/tree, hash do JAR, Java 21 e a
execução offline de `QrJarSmoke` pelo JAR. Não promover só porque o build passou.
Manifesto fiscal continua vazio, gates comerciais fechados e FinancialCore
operacional inactivo.

## 2. Chave sintética e identidade autorizada

Criar uma chave RSA exclusiva deste ambiente, PKCS#8 DER, ≥2048 bits, sem
password embutida e sem usar qualquer chave ou certificado fiscal real.
O ficheiro fica fora do repositório e de `user.dir`, sem symlink, permissões
0600 e dono igual ao utilizador que executa o Java; directório privado 0700.
Exemplo **apenas depois de autorizar e definir o caminho externo**:

```sh
umask 077
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 \
  | openssl pkcs8 -topk8 -nocrypt -outform DER -out "$H2_DEMO_KEY_FILE"
chmod 600 "$H2_DEMO_KEY_FILE"
```

Não apresentar a chave no terminal nem a incluir em Git, relatório, dump,
manifesto de evidências ou ficheiro GOLDEN. Guardar separadamente uma referência
ao segredo e o fingerprint **público**, com controlo de acesso e recuperação.
Uma eventual rotação/recriação é deliberada e registada; não é fallback automático.

Na instância nova já identificada, consultar em leitura:

```sql
select current_database();
select system_identifier::text from pg_control_system();
```

Registar a aprovação do cluster exacto; não copiar o identificador de outra
instância nem remover as exclusões do código. A consulta requer permissões
adequadas na instância isolada, não concessões em produção.

## 3. Configuração explícita e arranque novo

Preparar um ficheiro de ambiente privado, sem o publicar. Configurar:

| Parâmetro | Valor / condição |
| --- | --- |
| SPRING_PROFILES_ACTIVE | exclusivamente `demo` |
| DATASOURCE_URL/USERNAME/PASSWORD | nova instância `fac_demo`; credenciais próprias |
| FAC_JWT_SECRET | segredo exclusivo do ambiente |
| FAC_DEMO_SIGNATURE_ENABLED | `true` por decisão explícita |
| FAC_DEMO_SIGNATURE_AUTHORIZED_CLUSTER | identificador exacto autorizado acima |
| FAC_DEMO_SIGNATURE_KEY_FILE | caminho absoluto acessível ao Java, externo a `user.dir` |
| FAC_DEMO_RESET_AUTHORIZED | `true` apenas durante seed/reset autorizado |
| FAC_DEMO_PASSWORD_ADMIN/OPERADOR/CONSULTA | credenciais demo próprias, nunca comerciais |

Não configurar `spring.flyway.url` separado. Não activar `prod`, perfis mistos,
catálogo, emissão fiscal comercial, FinancialCore ou Fatia 1e. Os guards actuam
antes de Flyway/JPA e recusam destino, séries ou origem documental incompatíveis.

Para o percurso **JAR** isolado, fornecer essas variáveis pelo mecanismo seguro
do ambiente (sem passwords na linha de comandos) e executar:

```sh
java -jar "$H2_DEMO_JAR" --spring.profiles.active=demo \
  --server.address=127.0.0.1 --server.port=0 \
  --fac.demo.seed-on-startup=true --fac.demo.exit-after-seed=true
```

Este comando pressupõe uma base nova vazia; não elimina dados. O seed inclui
as duas referências postais canónicas que utiliza e emite pelo circuito da
aplicação. Após validação, desactivar seed/reset e arrancar para uso local.
Repetir seed na mesma base deve apenas validar, sem novas emissões/numeradores.

**Compose e scripts existentes:** `.env.demo` sozinho não encaminha as três
opções de assinatura nem monta a chave. O Compose usa nomes de projecto/volume
fixos; os scripts `common.sh` fixam esse ficheiro. Não os executar contra a nova
instalação supondo que uma variável mudou o destino. Uma eventual configuração
Compose/overlay terá de ser explicitamente preparada, revista e ensaiada numa
operação autónoma, com chave read-only e UID correcto, antes de uso operacional.
Os scripts Linux/Windows não foram executados na R2; o percurso JAR é a prova
isolada já disponível, não uma declaração de prontidão do deploy.

## 4. Validar o cenário antes de preparar o GOLDEN

Exigir: 5 clientes, 8 artigos, 3 utilizadores demo, 2 séries (FT/DEMO26 e
RC/DEMO26), numeradores 6/2, 6 documentos comerciais e 2 financeiros. Confirmar
Flyway V1–V28 sem erros, snapshots na versão corrente do modelo (actualmente
V3), origem/payload/versão `TUULI-DEMO-1` e códigos sintéticos DEMO-FT/DEMO-RC.
Não atribuir códigos AT reais ou identidade fiscal por inferência.

Executar os checks `DemoScenarioCheckService` e `CommercialDemoCheckService`
pelo JAR/runners existentes na nova instância, um arranque de cada vez:

```sh
java -jar "$H2_DEMO_JAR" --spring.profiles.active=demo --server.port=0 \
  --fac.demo.check-on-startup=true --fac.demo.exit-after-check=true
java -jar "$H2_DEMO_JAR" --spring.profiles.active=demo --server.port=0 \
  --fac.demo.commercial-check-on-startup=true \
  --fac.demo.exit-after-commercial-check=true
```

Manter a datasource isolada e as variáveis autorizadas do passo anterior.
Verificar anulação demo suportada,
liquidações e auditoria, PDF de várias páginas e reimpressão. Cada página tem de
mostrar **DEMONSTRAÇÃO — SEM VALIDADE FISCAL**; QR é formato não AT, com Q
prefixado DEMO e R textual DEMO-SEM-CERTIFICADO, não um certificado fictício.
Confirmar geometria/descodificação e o inventário `DEMO_ARTIFACTS`: essa demo
é deliberadamente **inapta para prontidão comercial**.

## 5. Gerar candidato GOLDEN e testar restauro

Parar a utilização/escritas na nova instalação durante o snapshot; a antiga
continua preservada. Gerar dump PostgreSQL custom exclusivamente da base nova,
num destino privado fora do repositório, com nome histórico novo. Exemplo com
**contentor novo previamente conferido**, nunca o da Partner Demo:

```sh
docker exec "$H2_DEMO_CONTAINER" pg_dump -U "$H2_DEMO_DB_USER" \
  -d fac_demo --format=custom --no-owner > "$H2_GOLDEN_CANDIDATE"
sha256sum "$H2_GOLDEN_CANDIDATE"
docker exec -i "$H2_DEMO_CONTAINER" pg_restore --list < "$H2_GOLDEN_CANDIDATE"
```

Se o comando exigir autenticação, usar o mecanismo secreto existente na instância;
não colocar passwords em argumentos nem imprimir o dump. Catálogo não prova
restauro: testar o dump em **outro cluster descartável**, também com base
`fac_demo`, confirmar identidade/autorização própria, chave acessível em local
externo e arrancar o mesmo JAR. Um novo cluster não herda automaticamente a
autorização do anterior; qualquer ajuste é explícito e registado, sem bypass.
Repetir contagens, relações, checks, emissão/reimpressão e guards aplicáveis.

Criar metadados que incluam pelo menos o contrato dos scripts de backup:
`project=fac`, `environment=demo`, `database=fac_demo`, `backupFile`,
`backupSizeBytes`, `sha256`, `validationResult=OK` **só depois das verificações**.
Acrescentar commit/tree/JAR SHA-256, PostgreSQL/Flyway, origem demo, contagens,
séries/numeradores, ambiente/cluster de criação e ensaio, fingerprint público e
referência protegida da chave, relatório de restauro, data e aprovadores.
O dump contém hashes de passwords demo: acesso privado e retenção governada.

## 6. Aprovar antes de promover

Submeter candidato, metadados, hashes, provas e configuração de execução à
revisão independente e ao PO. A aprovação do código não aprova automaticamente
o candidato, a instalação ou a actualização Ubuntu. Não declarar GOLDEN aprovado
sem decisão explícita, nem alterar o CURRENT antigo nesta missão.

Só numa operação futura autorizada, numa janela sem concorrência, promover o
par dump/metadados validado e actualizar as referências CURRENT sob controlo
operacional. Preservar o par antigo e permitir regressar **à versão antiga da
aplicação e respectiva configuração**; não reutilizar o dump antigo na H2-05.
Não pressupor atomicidade da troca de dois symlinks nem a compatibilidade dos
scripts antigos com o novo modelo sem ensaio específico.

Se a validação falhar, não promover nem actualizar a Ubuntu. Preservar provas e
originais e devolver o impedimento, sem limpeza, reclassificação ou fallback.
