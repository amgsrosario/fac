# Security Gate — contrato de operação

TUULI AIR suporta exposição pública, com autenticação Bearer JWT e autorização
por capacidades existentes. Este contrato não substitui a configuração segura
da infraestrutura nem declara o fecho executivo do capítulo Segurança.

## Configuração e arranque

- Segurança activa por defeito. `FAC_SECURITY_ENABLED=false` só é admitido com
  perfil `test` exclusivo, marcador de contexto vindo dos recursos de teste e
  presença da infraestrutura de testes no classpath. Esses recursos/classes não
  são incluídos no JAR operacional. `dev`, `demo`, `prod` ou perfis mistos não
  podem desactivar a segurança.
- `prod`, `demo` e perfis não reconhecidos como desenvolvimento/teste exigem
  secret JWT estável. O valor deve ser gerado com CSPRNG com pelo menos 256 bits
  de entropia, por exemplo `openssl rand -base64 32`, e fornecido por mecanismo
  operacional de secrets. Nunca versionar o resultado nem imprimi-lo em logs.
- A validação rejeita vazio, menos de 32 bytes UTF-8, menos de 12 caracteres
  distintos, whitespace e placeholders comuns. Essas verificações são mínimos;
  não demonstram a entropia de um valor escolhido por uma pessoa.
- HS256 e derivação SHA-256 existentes preservados. Todas as instâncias devem
  partilhar o secret configurado. Rotação invalida tokens assinados pela chave
  anterior; coordenar reinícios/configuração. Não existe key-ring nesta versão.
- Chave aleatória por arranque só permanece disponível em `dev` explicitamente activado ou no contexto
  de teste autorizado. Não usar o perfil de desenvolvimento em produção.
- `FAC_JWT_EXPIRATION_MINUTES`: inteiro de 1 a 1440; default 60. Mantém-se a
  tolerância temporal padrão de 60 segundos do Spring Security.

## Login

Protecção própria da aplicação, por instância, com janelas fixas:

| Propriedade `fac.security.login.*` | Default | Intervalo admitido |
| --- | --- | --- |
| `identifier-limit` | 8 | 1–100 |
| `address-limit` | 60 | 1–1000 |
| `global-limit` | 600 | 1–10000 |
| `window-seconds` | 60 | 1–3600 |
| `capacity` | 4096 | 16–16384 |

Os limites contam tentativas, incluindo sucessos, antes da consulta de identidade
ou BCrypt. O identificador apresentado é normalizado por trim/lowercase. A origem
é `HttpServletRequest.getRemoteAddr()`, sem leitura de `X-Forwarded-For` pelo limiter.

Ao atingir qualquer limite ou a capacidade, a aplicação devolve 429 e
`Retry-After`; a janela seguinte recupera automaticamente. Não existe lockout
permanente. Não há eviction de chaves activas que permita contornar um bloqueio.
O estado total é limitado; códigos e emails são aliases distintos, sujeitos
adicionalmente ao limite de origem e global.

Atrás de proxy, vários clientes podem partilhar o mesmo endereço observado.
Dimensionar o limite de origem para esse deployment. Não activar processamento
indiscriminado de forwarded headers; o edge deve remover valores do cliente e a
infraestrutura deve garantir a confiança nos proxies. A aplicação não configura
proxies confiáveis nem transforma headers arbitrários em identidade de origem.

Não há coordenação entre instâncias: reinícios apagam as janelas e várias
instâncias multiplicam o orçamento total. Aceitar/documentar esse limite e
adicionar protecção no edge quando necessário, sem retirar o controlo da aplicação.

A auditoria normal de login é preservada. Pedidos limitados não fazem novas
escritas de auditoria por tentativa; logs agregados assinalam a activação e, no
próximo pedido após a janela, a quantidade recusada. Não incluem identificadores,
IP, password ou JWT. Limites/configuração não dependem da existência do utilizador.
Utilizador inexistente/inactivo/password errada recebem a mesma mensagem, com
trabalho BCrypt aproximadamente equivalente. Não se promete tempo constante.

Passwords continuam sujeitas à política existente, com rejeição explícita acima
de 72 bytes UTF-8 para evitar truncagem ambígua do BCrypt. Login com valor acima
desse limite falha como credenciais inválidas. ASCII mantém os limites anteriores.

## Tokens e sessão

Tokens aceites exigem assinatura HS256 válida, `sub` textual não vazio, `exp`,
issuer `fac`, `token_version` inteiro não negativo e `authorities` como colecção de
nomes de capacidades existentes. Claims inválidas são recusadas antes da consulta
de utilizador. Identidade activa e versão persistida continuam obrigatórias.
Não se introduz audience nem novas permissões. Claims de apresentação não são
usadas como gates de autorização.

Reset, desactivação e mudança efectiva de perfil continuam a invalidar sessões.
Reactivação não recupera tokens antigos. `row_version` conserva a protecção contra
stale writes. LocalStorage, logout local, ausência de refresh tokens e revogação
individual são riscos residuais deliberadamente aceites nesta versão.

## Ficheiros

- Importações: até 10 MiB, 10.000 linhas de dados e 100 colunas; CSV rejeita durante
  o parsing. Transporte multipart admite 11 MiB para acomodar overhead.
- XLSX: antes do modelo POI, ZIP limitado a 256 entries, 32 MiB por entry e 64 MiB
  de conteúdo expandido total. XML de worksheet é identificado pelo conteúdo,
  independentemente do caminho/extensão; limites de linhas/colunas são precoces.
  DTD/referências externas recusadas e protecção POI de inflate ratio preservada.
- Logos: 1 MiB, PNG/JPEG reais, até 2000×2000. Header/formato/dimensões são verificados
  antes da descodificação de pixels. Nomes internos fixos e armazenamento existente
  preservados. Não há escrita em paths fornecidos pelo cliente.
- Traversal, rejeição de fórmulas XLSX e neutralização de formula injection nos
  Dados Mestres são preservados. PDFs conservam o motor/layout e escaping existente.

Estes limites reduzem o custo antes de materializar estruturas; não representam
uma sandbox, quotas distribuídas ou garantia de memória constante. O PDF pode
continuar a materializar o HTML e resultado final, conforme decisão anterior.

## Browser e deployment

Nginx versionado aplica nos blocos servidor os headers de segurança e cache,
sem `add_header` nos locations que interrompa a herança. CSP permite scripts da
mesma origem, bloqueia objectos e limita framing; estilos inline permanecem
permitidos pela utilização real de React/PrimeReact. Não há `unsafe-inline` em
scripts. Imagens locais/data/blob e fontes locais/data são permitidas.

- Produção pública exige HTTPS. TLS termina no edge/proxy/load balancer.
- Redireccionamento HTTP→HTTPS, certificados, renovação, HSTS no edge, firewall,
  exposição de serviços e gestão/rotação de secrets pertencem ao deployment.
- HTTP interno só deve circular dentro da arquitectura protegida do deployment.
- Nginx interno HTTP não injecta HSTS e não afirma garantir segurança pública.
- Same-origin é preservado; CORS não é aberto globalmente.
- CSRF permanece desactivado para Bearer explícito, sem cookie de autenticação.
  Uma futura mudança para cookies exige rever CSRF/origens.
- BD/pgAdmin de desenvolvimento têm bind a loopback. Credenciais exemplificativas
  versionadas não devem ser reutilizadas noutros ambientes.

Não foram realizadas pesquisa externa de CVEs, migrações, alterações de IAM,
certificação AT ou Fecho Fiscal. A auditoria selectiva de recusas permanece um
resíduo aceite do AIR. Fecho formal de Segurança pertence ao Executivo/António.
