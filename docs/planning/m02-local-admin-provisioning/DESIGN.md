# M02 — Primeiro administrador local, sem canal remoto

Mandato [#46/6611272547](https://github.com/amgsrosario/fac/issues/46#issuecomment-6611272547), base `ca1e80245d95362eb8c7a78c90350f1a930eb0bf` (PR #49). Não reabre M01/Security Gate e não autoriza operar instalação real.

## Reconhecimento e escolha técnica

Pesquisa dirigida em scripts, documentação de segurança, serviços/configuração e runners: não localizado mecanismo local de bootstrap aprovado e reutilizável. Existem CRUD Utilizador autenticado, matriz de permissões e runners de **demo**, que não são bootstrap comercial autorizado. Não se consultaram sessões privadas ou plataformas externas; a pesquisa não prova inexistência de ferramenta fora do repositório.

Alternativas: (A) runner Spring condicionado por perfil; (B) CLI explícita sem Spring/servidor. Escolhida **B**, para evitar arranque acidental por configuração, listeners HTTP, runners demo ou canal remoto mesmo autenticado. Não há endpoint, bean, seed universal ou password versionada. A CLI é executada por operador local numa consola e recusa stdin sem consola. A aplicação deve estar parada no procedimento futuro, embora a transacção serialize também com SQL/CRUD concorrentes.

## Identidade, autorização e limites de confiança

A V29 aditiva cria um UUID de instalação e um comprovativo singleton, sem criar contas, roles ou grants. Marca elegibilidade apenas se não existem utilizadores ao aplicar V29; instalações já utilizadas não se tornam elegíveis por remover as suas contas depois. Qualquer utilizador existente (inclusive administrador inactivo ou perfil Consulta) recusa a operação.

Operador prepara ficheiro **absoluto/0600**, dono actual, sem symlink, pais não modificáveis por grupo/outros e de dono actual/root. Campos exactamente: `jdbcUrl`, `operator`, `database`, `cluster`, `installation`, `reference`, `actor`, `expiresAt`; duplicados recusados. URL apenas PostgreSQL loopback com porta/base explícitas e sem parâmetros. Não contém passwords. Identidade inclui nome da base, cluster PostgreSQL, UUID persistido e role técnico; comparados novamente no servidor antes da escrita. Cópia da base noutro cluster/nome não satisfaz a autorização.

O ficheiro é uma **atestação administrativa local**, não uma assinatura criptográfica de aprovação. A aprovação real/referência, a custódia do ficheiro, a identidade humana e os grants são responsabilidade do operador autorizado/DBA. Não se inventa autorização comercial a partir do arquivo ou de um UUID. OS/DBA/superuser malicioso pode modificar código/schema/dados: não se promete resistência a essa autoridade. Compromisso dessas credenciais pertence à operação, não é resolvido por um endpoint novo.

Só executar com role técnico temporário, sem memberships, ownership, superuser, CREATEDB, CREATEROLE, REPLICATION ou BYPASSRLS. A função recusa tais roles. O DBA concede **apenas CONNECT/USAGE e EXECUTE nas duas funções**; não são necessários SELECT/DML nas tabelas, gestão de roles ou acesso aos dados financeiros. Nada disto é concedido por default/PUBLIC ou criado pela migration. HBA/infraestrutura futura deve restringir a role ao acesso local, sem listener público; a CLI não elimina um túnel ou uma configuração de rede privilegiada criada fora do produto. Nenhuma nova superfície HTTP é introduzida.

## Operação atómica e utilização única

`local_admin_identity()` fornece identidade estritamente ao operador com EXECUTE. `provision_first_administrator(...)` é SECURITY DEFINER, search_path fixo e todas as relações qualificadas. EXECUTE PUBLIC revogado. Não recebe SQL dinâmico nem password em claro, só hash BCrypt com formato/custo actual; Java reutiliza o DTO e a política existente. A pequena extracção de `UtilizadorPasswordPolicy` preserva a semântica do CRUD; BCrypt mantém o limite 72 bytes UTF-8.

A função verifica autorização/expiração pelo relógio SQL e a role, bloqueia o singleton e a tabela Utilizador (EXCLUSIVE), verifica novamente a expiração após os locks (timeout de aquisição 5s) e ausência de contas, cria ADMINISTRADOR activo, grava `UTILIZADOR_CRIADO` com origem `M02_LOCAL` e referência/responsável, e consome o comprovativo. Tudo numa única chamada/transacção SQL. Auditoria não contém password/hash/email. Falha de auditoria/consumo reverte a criação; chamadas concorrentes e repetidas não geram segundo administrador. Bloqueio de tabela cobre também a ausência de linhas e INSERT normal concorrente; aplicar em instalação parada limita contenção. Ordem de locks: comprovativo → Utilizador.

Comprovativo consumido é imutável, protegido contra UPDATE/DELETE/TRUNCATE/recriação por triggers e referências ao utilizador/evento. Não há reset ou modo recuperação automático. Uma resposta de rede perdida pode corresponder a commit já efectuado: consultar o comprovativo pelo DBA antes de repetir; não criar outro administrador por alternativa. O ficheiro pode continuar presente, mas o consumo persistente impede reutilização. Revogar grants/inutilizar a role técnica e retirar o ficheiro após operação, segundo o runbook; não há reutilização comercial automática.

## Segredos e empacotamento

Passwords técnicas e do administrador lidas exclusivamente por `Console.readPassword`, nunca por argumentos, ficheiro, ambiente ou HTTP da CLI. Confirmação explícita do UUID e repetição da password. Arrays apagados no finally; bibliotecas JDBC/BCrypt/validação usam Strings transitórias na JVM, pelo que não se promete eliminação física universal da memória. Erros da CLI são genéricos, sem stacktrace/valores JDBC. O retorno expõe apenas o ID de auditoria.

A classe CLI fica no JAR para execução local explícita por PropertiesLauncher; não é a main habitual, não inicia Spring, migrations ou servidor. Artefacto smoke test com `--help` deve demonstrar esse ponto. A função SQL não está acessível por PUBLIC e os grants locais não são transportados por seeds.

## Fora do âmbito

Sem contas reais, operação das bases conservadas, deploy, configuração fiscal, catálogo/manifesto, assinatura, séries AT, cutover, FinancialCore, M03–M05 ou alteração de perfis/JWT. Não altera emitentes, saldos, extractos ou V25/V26. V29 no perímetro produtivo é nova: o SQL V29 experimental da worktree desconhecida `7ce573f` não é incorporado ou executado; não faz parte de Flyway integrado. Qualquer futura integração desse trabalho teria de reconciliar a numeração sob mandato próprio.

## Fontes concretas do reconhecimento

| Fonte | Resultado delimitado |
| --- | --- |
| `controller/UtilizadorController`, `service/UtilizadorService`, `SecurityConfig` | CRUD existente com autorização; não é bootstrap da instalação vazia |
| `demo/DemoScenarioService` e runners demo | Criam contas da demonstração, não reutilizadas nem autorizadas como credenciais comerciais |
| `scripts/backup/{linux,windows}`, `scripts/demo/linux` | Backup/restauro/inventário da tabela Utilizador, sem mecanismo aprovado de primeiro administrador |
| `docs/security/SECURITY_GATE.md`, `AMBIENTES_E_ACESSOS.md` | Contratos JWT/permissões; sugestão antiga de seed local/dev não é aprovação de bootstrap comercial |

Caminhos Java relativos a `src/main/java/com/ar2lda/fac/`. Os grants do DBA **por instalação/base**, a role local e o seu segredo constituem a fronteira de autorização do servidor; conhecer/copiar o UUID não concede EXECUTE noutra base. Referência/responsável no ficheiro preservam a decisão humana, sem a autenticar criptograficamente. O dono controlado das funções necessita das permissões sobre as tabelas e da leitura `pg_control_system()`; isto não é concedido ao operador. Em instalação futura a separação de roles e HBA é pré-condição a validar, não executada nesta missão.

O ficheiro segue o formato Java Properties (`ISO-8859-1`/escapes Unicode); consola e nomes/passwords usam o contrato Java existente. O teste sintético e o smoke do JAR não substituem um ensaio futuro humano da consola/roles/HBA/TLS numa instalação autorizada; nenhuma conta real foi criada.
