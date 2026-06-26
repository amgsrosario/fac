# FAC - MD32: Auditoria tecnica final da versao V58

## 1. Sumario executivo

Auditoria realizada sobre a versao V58 do FAC antes da demonstracao comercial e do piloto controlado.

- Commit auditado: `4cb11b3`.
- Estado inicial do repositorio: limpo, sem ficheiros modificados.
- Resultado global: a versao esta pronta para demonstracao comercial em ambiente Windows/demo.
- Resultado para piloto: pronta para piloto controlado, com condicoes de mitigacao indicadas neste documento.
- Criticos: nenhum identificado.
- Altos: nenhum identificado.
- Medios: riscos funcionais/controlados em concorrencia de recibos, semantica de liquidacao parcial e armazenamento de token no browser.
- Correcao aplicada durante a auditoria: `spring.jpa.show-sql=false` explicitamente em `demo` e `prod`.

Nao foram adicionadas funcionalidades novas. A base real `fac` nao foi usada nos ensaios; os testes e a demonstracao foram executados contra `fac_demo` e, temporariamente, contra `fac_restore_test`.

## 2. Ambito

A auditoria cobriu:

- configuracao Spring Boot, perfis `dev`, `demo`, `prod` e `test`;
- seguranca, autenticacao JWT e autorizacoes funcionais;
- migracoes Flyway e integridade relacional;
- importacao/exportacao de dados mestres;
- documentos comerciais, documentos financeiros, pendentes e anulacoes;
- frontend React/Vite;
- Docker Compose de demonstracao;
- scripts de backup, verificacao e restauro;
- documentacao existente MD25 a MD31;
- ensaio integral de build, testes, demo, backup e restauro.

Ficaram fora do ambito: validacao fisica em Ubuntu/servidor final, auditoria externa de vulnerabilidades com acesso a internet e testes de carga multiutilizador prolongados.

## 3. Arquitetura observada

Backend:

- Spring Boot `3.3.4`, Java `21`, PostgreSQL e Flyway.
- API REST com seguranca por Bearer JWT.
- Camadas separadas em controller, service, repository, model e DTO.
- MapStruct para mapeamento.
- Apache POI para Excel, OpenHTMLToPDF/PDFBox para PDF e ZXing para QR Code.

Frontend:

- React `18`, TypeScript e Vite.
- Build estatico servido por Nginx no ambiente demo.
- API base configurada por `VITE_API_BASE_URL`, com fallback local.

Demo:

- `compose.demo.yaml` com PostgreSQL, backend e frontend.
- Apenas o frontend expoe porta de host: `8088 -> 80`.
- Backend e base de dados ficam apenas na rede Docker.

## 4. Achados

### 4.1 Criticos

Nenhum achado critico identificado.

### 4.2 Altos

Nenhum achado alto identificado.

### 4.3 Medios

#### M1 - Concorrencia em documentos financeiros e pendentes

O fluxo de criacao de documentos financeiros atualiza pendentes a partir de leitura normal por `pendenteRepository.findById(...)`. Nao foi observado bloqueio pessimista equivalente ao usado noutros pontos sensiveis, como series/documentos.

Impacto: dois recibos concorrentes sobre o mesmo pendente podem, em teoria, ler o mesmo saldo e provocar liquidacao duplicada ou saldo incorreto.

Estado: nao corrigido nesta auditoria por exigir teste concorrente e confirmacao de regra de negocio.

Recomendacao: introduzir leitura bloqueante do pendente com `PESSIMISTIC_WRITE`, validar saldo dentro da mesma transacao e acrescentar teste concorrente.

#### M2 - Semantica de `liquidado` em liquidacoes parciais

Foi observado comportamento em que um documento comercial pode ficar com `liquidado=true` mesmo quando permanece `valorPendente` positivo apos pagamento parcial. Existe teste automatizado que espera esse comportamento, pelo que a auditoria nao alterou a regra.

Impacto: risco de interpretacao errada em UI, listagens, relatórios ou futuras regras de negocio que assumam que `liquidado=true` significa saldo totalmente liquidado.

Estado: nao corrigido por ser decisao funcional.

Recomendacao: decidir se `liquidado` significa "tem liquidacao associada" ou "saldo totalmente liquidado". Se a segunda interpretacao for a correta, ajustar regra, testes e vistas.

#### M3 - Token JWT guardado em `localStorage`

O frontend guarda o token JWT em `localStorage`.

Impacto: aceitavel para demo/piloto controlado, mas aumenta exposicao em caso de XSS.

Estado: nao corrigido nesta fase.

Recomendacao: antes de exposicao ampla ou internet, avaliar cookie `HttpOnly`/`SameSite`, expiracao curta e hardening de CSP.

### 4.4 Baixos

#### B1 - SQL logging herdado para demo/prod

`application.yaml` tinha `spring.jpa.show-sql=true`. Os perfis `demo` e `prod` nao desligavam explicitamente esta opcao, embora o Docker demo a sobrescrevesse por variavel de ambiente.

Impacto: execucao direta com perfil `demo` ou `prod` podia emitir SQL nos logs.

Estado: corrigido durante esta auditoria.

Correcao:

- `src/main/resources/application-demo.yaml`: `spring.jpa.show-sql=false`.
- `src/main/resources/application-prod.yaml`: `spring.jpa.show-sql=false`.

#### B2 - Nginx sem Content-Security-Policy

O Nginx do frontend tem cabecalhos de seguranca relevantes, incluindo `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy` e `server_tokens off`, mas nao foi observada CSP.

Impacto: risco baixo no demo local/controlado; importante antes de exposicao externa.

Recomendacao: adicionar CSP restritiva apos validar dependencias de runtime do frontend.

#### B3 - Sessoes de importacao nao associadas explicitamente ao utilizador criador

As sessoes de importacao usam identificador UUID e exigem permissao funcional para confirmar/cancelar, mas nao foi observada uma ligacao explicita ao utilizador que iniciou a sessao.

Impacto: baixo a medio; requer permissao e conhecimento/descoberta do UUID.

Recomendacao: associar sessao ao utilizador criador e validar propriedade, com excecao administrativa se necessaria.

#### B4 - Ficheiros legados com credenciais de exemplo

Ficheiros de referencia como `comandos-sql.txt` e `comandos-demo.sql` contêm exemplos de credenciais/hashes.

Impacto: baixo se forem apenas referencia local, mas fraco para entrega externa.

Recomendacao: arquivar, remover ou sanitizar antes de handoff externo.

#### B5 - Auditoria externa de vulnerabilidades nao executada

Builds Maven e frontend passaram, mas nao foi executada auditoria online de dependencias devido ao ambiente sem rede livre.

Impacto: risco residual normal.

Recomendacao: executar `npm audit`, OWASP Dependency Check ou ferramenta equivalente em ambiente com rede controlada antes de producao.

### 4.5 Informativos

- Avisos PDFBox/fontes nos testes sao esperados no ambiente local.
- Aviso de serializacao `PageImpl` indica oportunidade futura para DTO/paged model estavel.
- Aviso `spring.jpa.open-in-view` deve ser decidido conscientemente numa futura fase de hardening.
- A validacao fisica em Ubuntu/servidor final continua necessaria antes de uso fora de Windows/demo local.

## 5. Seguranca

Pontos positivos observados:

- `/auth/login` e `/actuator/health` expostos sem autenticacao; endpoints funcionais exigem autenticacao/autorizacao.
- Autorizacoes funcionais aplicadas para configuracao, utilizadores, importacao/exportacao e anulacao.
- Servicos tambem usam verificacoes funcionais com `@PreAuthorize`/`FunctionalAuthorization`.
- Erros comuns sao tratados por `GlobalExceptionHandler`, sem exposicao evidente de stack traces em respostas normais.
- Configuracao `securityEnabled=false` esta limitada por perfil/configuracao e coberta por testes.

Riscos controlados:

- Token em `localStorage`.
- Sem CSP no Nginx.
- Credenciais de exemplo em ficheiros legados de referencia.

## 6. Dados, migracoes e integridade

Pontos positivos observados:

- Migracoes Flyway de `V1__baseline_esquema_fac.sql` a `V6__importacao_exportacao_dados_mestres.sql`.
- Backend em demo/prod usa `ddl-auto=validate`.
- Testes cobrem integridade referencial, eliminacoes bloqueadas e cenarios funcionais principais.
- Series/documentos usam bloqueios transacionais onde a numeracao e anulacao exigem controlo.

Ponto a tratar:

- Pendentes usados por documentos financeiros devem receber controlo concorrente explicito antes de piloto com varios utilizadores em simultaneo.

## 7. Importacao e exportacao

Pontos positivos observados:

- Limite de ficheiro: `10 MB`.
- Limite de linhas: `10000`.
- Limite de colunas: `100`.
- Rejeicao de celulas Excel com formulas.
- Protecao POI contra zip bombs via `ZipSecureFile.setMinInflateRatio(0.01d)`.
- Exportacao mitiga CSV/XLSX formula injection prefixando valores iniciados por `=`, `+`, `-` ou `@`.
- Confirmacao/cancelamento de importacao usa bloqueio pessimista sobre a sessao.

Risco residual:

- Sessao de importacao sem propriedade por utilizador.

## 8. Backup, verificacao e restauro

Pontos positivos observados:

- Scripts recusam bases perigosas como `fac`, `postgres`, `template0` e `template1`.
- Restauro permitido apenas para destinos controlados como `fac_restore_test`, `fac_demo` e `fac_test`.
- Backups incluem SHA256 e metadata.
- Verificacao valida checksum.
- Classe `BackupSafety` replica regras de seguranca no backend.

Ensaio executado:

- Backup criado: `backups/demo/fac_demo_fac_demo_2026-06-25_193046_4cb11b3-dirty.backup`.
- Tamanho: `104337` bytes.
- SHA256: `39879574fe40a8905183e6abd4326a7a2bed2e5d513cf75733cfe78bdb9e231a`.
- Verificacao: OK.
- Restauro para `fac_restore_test`: OK.
- Checks funcionais sobre `fac_restore_test`: OK.
- Base temporaria `fac_restore_test`: removida no fim.
- Bases finais observadas no Postgres demo para nomes sensiveis: apenas `fac_demo`.

## 9. Demo comercial

Resultado do ensaio:

- `scripts/demo/prepare-commercial-demo.ps1 -Reset -SkipBackup`: OK.
- `FAC_DEMO_CHECK OK | base=fac_demo | perfil=demo`.
- `FAC_COMMERCIAL_DEMO_CHECK OK | base=fac_demo | perfil=demo`.
- `FAC_COMMERCIAL_DEMO_READY url=http://127.0.0.1:8088 base=fac_demo perfil=demo`.
- Containers finais:
  - `fac-demo-db-1`: healthy.
  - `fac-demo-backend-1`: healthy.
  - `fac-demo-frontend-1`: healthy.
- HTTP frontend: `http://127.0.0.1:8088` respondeu `200`.

## 10. Validacoes executadas

Todas as validacoes seguintes foram executadas com sucesso:

- `mvn -q test` com `SPRING_JPA_SHOW_SQL=false`.
- TypeScript: `tsc -b`.
- Frontend build: `vite build`.
- Backend package: `mvn -q -DskipTests package`.
- Docker build demo: backend e frontend.
- Preparacao demo comercial com reset.
- Backup demo.
- Verificacao do backup.
- Restauro para base controlada.
- Checks funcionais e comerciais sobre a base restaurada.
- Limpeza da base temporaria de restauro.
- Confirmacao HTTP do frontend demo.

Avisos observados e considerados nao bloqueantes:

- mensagens esperadas de integridade referencial em testes negativos;
- avisos PDFBox/fontes;
- aviso Java agent/dynamic loading;
- aviso Spring sobre serializacao direta de `PageImpl`.

## 11. Alteracoes feitas pela auditoria

Foram alterados apenas ficheiros de configuracao e este relatorio:

- `src/main/resources/application-demo.yaml`: adiciona `spring.jpa.show-sql=false`.
- `src/main/resources/application-prod.yaml`: adiciona `spring.jpa.show-sql=false`.
- `docs/32 - Auditoria tecnica final da versao V58.md`: adiciona o presente relatorio.

Nao foram alteradas regras de negocio, contratos de API, frontend funcional, migracoes ou scripts.

## 12. Decisao de prontidao

### Demonstracao comercial

Estado: aprovado.

Condicoes:

- usar o ambiente demo preparado;
- manter exposicao local/controlada;
- nao usar a base real `fac`;
- manter backup verificado antes de qualquer ensaio destrutivo.

### Piloto controlado

Estado: aprovado com condicoes.

Condicoes antes ou durante o arranque controlado:

- tratar concorrencia em documentos financeiros/pendentes;
- decidir e estabilizar semantica de `liquidado` em pagamentos parciais;
- sanitizar ficheiros legados com credenciais de exemplo antes de entrega externa;
- adicionar CSP/hardening se houver exposicao fora de rede controlada;
- executar validacao no servidor/Ubuntu final;
- executar auditoria de dependencias com rede controlada antes de producao.

Conclusao: a V58 esta tecnicamente consistente para demonstracao comercial e suficientemente robusta para piloto controlado, desde que os riscos medios identificados sejam geridos de forma explicita.
