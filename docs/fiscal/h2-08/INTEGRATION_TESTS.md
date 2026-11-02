# H2-08 — Integração futura, riscos e testes necessários

Base de evidência `f7e8f8a`. **F** = código observado; **P/H** = desenho/teste
futuro. Os caminhos Java abaixo têm prefixo `src/main/java/com/ar2lda/fac/`.
Não se implementou nem se executaram estes testes nesta missão documental.

## Pontos exactos de integração

| Ponto na base | F: situação actual | P: contrato futuro / condição |
| --- | --- | --- |
| `service/DocumentoComercialService.java:350–398` | Emissão transaccional: documento/tipo, validações, série/número, snapshot, QR, estado/instante, pendente, origem e auditoria | Inserir preparação/validação de prova na mesma fronteira, antes de QR e confirmação; não expor documento final incompleto |
| Mesmo, `:357,371–388` | Data validada antes do lock de série; QR antes de atribuir momentoEmissao | Revalidar cronologia dependente de concorrência sob locks; fixar instante final antes da canonicalização, sem mover Data Valor ou mudar datas financeiras |
| `repository/SerieRepository.java:15–25`; `service/SerieService.java:107–121` | Lock por tipo interno/série; código AT não vazio e próximo número | Identidade fiscal série/tipo e correspondência do tipo interno comprovadas; presença de código não prova comunicação válida à AT |
| `model/Serie.java`; `model/DocumentoComercial.java:62–75` | Série/QR/snapshot persistidos; sem contrato produtivo de assinatura/antecessor/chave | Persistência aditiva futura e imutável da prova, ligação única ao documento e registo confiável; sem backfill de assinaturas |
| `service/FiscalDocumentSignatureSource.java:16–23` | Leitura de cinco campos, sem keyVersion/registro/bytes | Fonte produtiva só lê prova verificada e autorizada. Estender contrato futuro em mandato próprio; não ligar assinador ao método de leitura/reimpressão |
| `service/FiscalQrService.java:25–39,62–74` | Exactamente uma fonte, vínculo ID/número/ATCUD, 409 se ausente/ambígua/incoerente | Verificar proveniência e conteúdo antes de Q/R. Gate de habilitação separado da existência de bean; sem fallback |
| `service/FiscalQrIdentity.java:16–27` | Valida Base64/formato e extrai Q/R; não prova RSA ou certificado | Derivação só após prova confiável; R da evidência legítima do programa aplicável, Q da assinatura persistida |
| `service/QrFiscalPayloadBuilder.java:167` | Formatação decimal a duas casas HALF_UP | Oráculos de correspondência entre total assinado/snapshot/QR/exportação; não inventar nova regra de arredondamento ou alterar dívida |
| `service/DocumentoComercialPdfService.java:50–68,313–324` | PDF de emitido/anulado com payload consolidado; não valida assinatura real | Reimpressão sem assinar; verificar prova/trust e preservar versão aplicada. Menção fiscal e política de estado/QR têm validação própria |
| `financial/FinancialFtOrigin.java:26–40` | MANDATORY, flush JPA, criação de posição FT elegível e vínculo JDBC | Demonstrar mesma transacção/datasource com futura prova e número; não tratar o testemunho H2-07 como teste desta integração real |
| `service/DocumentoComercialService.java:401–439` | Anulação bloqueia pendente antes de documento, valida movimentos e reverte origem | Preservar prova e inclusão no encadeamento, sem reabrir a temporalidade financeira de anulação FT; analisar grafo completo de locks |
| `service/DocumentoFinanceiroPdfService.java:193–199`; `FiscalQrService.java:42–58` | RC possui QR opcional/fallback de leitura e enquadramento separado | Não permitir que futura fonte assine durante impressão financeira; RC/RG fora deste perímetro |
| `demo/DemoSignatureSource.java:53–87` | Perfil demo exclusivo, base/cluster autorizados e séries demonstrativas; bloqueios próprios | Separação também de registo/chaves/credenciais; não afrouxar guards existentes nem interpretar demo como autorização produtiva |
| `src/test/.../signingtrial`; `src/test/resources/db/signature-trial` | Ensaio integrado H2-07, fora de JAR | Evidência de mecanismo, não fornecedor/registo produtivo. Não copiar registo auto-confiável como solução final |

**F:** contexto empresarial actual usa `Empresa.EMPRESA_ID`; não se demonstra
multiempresa produtiva por adicionar um campo scope. A autorização futura deve
ligar contexto autenticado, empresa do documento e âmbito do registo confiável;
`emissorId` identifica utilizador, não constitui essa autoridade fiscal.

## Contrato transaccional proposto (P)

1. Pré-validar autorização, perímetro homologado, fonte, perfil e registo confiável
   antes de consumir número, sempre que os dados necessários existam. Nenhum
   segredo, certificado ou versão é aceite como autoridade a partir do cliente.
2. Adquirir locks de documento/tipo/identidade fiscal de série e fronteira de
   cronologia segundo grafo global auditado, incluindo pendentes/posições e
   anulação existentes. H2-07 serializou scope sintético; isso não demonstra
   ausência de deadlock em produção. Ordenar conjuntos de identidades de modo
   determinístico e testar todas as operações concorrentes que as partilham.
3. Resolver antecessor e selecção de versão autorizada sob fronteira estável,
   revalidar datas/cronologia e fixar SystemEntryDate documental uma vez. Não
   confundir instante de operação financeira, commit, emissão/Data Valor e
   relógio de um KMS. Definir fuso/precisão pelo perfil aplicável, não por locale.
4. Numerar e consolidar snapshot/mensagem; conferir série/tipo fiscal/ATCUD e
   total EUR conforme representação normativa comprovada. FT/FR partilham
   tipologia interna, mas FR continua excluída; não fundir cadeias/projecções.
5. Assinar e verificar com chave do registo confiável, vinculando bytes, digest,
   documento, série/tipo, antecessor e versão. Registo pode ser alterado durante
   tentativa: provar selecção consistente e política de revogação na fronteira
   de confirmação; se perder autorização, abortar em vez de trocar silenciosamente.
6. Persistir documento/prova imutável/versão/registo, QR e origem financeira
   existente na mesma transacção PostgreSQL; evento de sucesso só para emissão
   confirmada. JPA/JDBC e falha de auditoria/origem têm rollback demonstrável.
7. Só entregar PDF/documento final depois de confirmação. Em falha de resposta
   após commit, consultar documento/prova antes de nova tentativa. Preservar
   contrato comercial actual de recusa a segunda emissão; não transpor chave
   idempotente RC de ensaio como nova API pública.

A prova deve incluir identidade, bytes exactos e versão de canonicalização,
assinatura integral, referência ao antecessor, keyVersion/fingerprint, versão do
registo confiável e certificado aplicáveis. SQL protege vínculos e imutabilidade;
verificador independente protege a criptografia/proveniência. Escritor das provas
não pode publicar/alterar a âncora. Eventual verificação criptográfica PostgreSQL
é desenho futuro específico; não prometer garantia não implementada.

### B remoto: resultado incerto não é rollback externo

**P:** correlacionar pedido com tentativa e digest dos bytes finais. Retry de um
pedido de assinatura só reutiliza a identidade quando os bytes são idênticos e
isso é suportado pelo contrato do serviço; nunca reutilizar resposta com outro
número, timestamp, montante ou predecessor. Signature obtida mas não confirmada
na base não constitui documento emitido nem predecessor da cadeia fiscal.
Preservar correlação de estado incerto sem criar falso evento de emissão.

**P recomendado para primeiro contrato:** emissão síncrona fechada em falha,
com limite de espera a aprovar/medir. Não alegar 2PC com KMS. Comparar alternativa
assíncrona apenas se a latência o exigir: ela introduz estados de reserva/pendência,
recuperação e eventual mudança do contrato público que exigem mandato adicional.
Não libertar locks e depois confirmar snapshot antigo sem nova validação; não
propor implementação assíncrona implícita nesta análise.

## Matriz de riscos residuais e aceitação futura

Gravidade é impacto da futura produção sem mitigação; não findings contra H2-07.

| Risco | Impacto / dependência | Teste/evidência necessária e critério de aceitação P |
| --- | --- | --- |
| R01 Crítico — âncora substituível | Assinatura coerente com chave atacante pode passar | T01 substituir prova+chave e registo no domínio do escritor: recusa; raiz/autoridade publicada por canal independente; ACL/anti-rollback demonstrados |
| R02 Crítico — perfil errado | Custódia A/B disponível não prova conformidade | T02 oráculos oficiais e verificador independente para bytes, algoritmo/tamanho/padding/codificação; provider incompatível recusado; validação fiscal actual escrita |
| R03 Crítico — âmbito cruzado | Uso da chave por empresa/série/tipo não autorizado | T03 contextos trocados, login forjado, referência de chave de outro âmbito: recusa sem expor dados; testar empresa única e futura segregação, sem alegar multiempresa existente |
| R04 Alto — data/relógio/cadeia | Bifurcação, ordem errada, novo exercício e keyVersion | T04 primeira emissão comprovada, predecessor anulado, plurianualidade, rotação e relógio global; datas regressivas/futuras segundo regra; uma cadeia série/tipo sem reset inferido |
| R05 Crítico — transacção parcial | Número, documento, prova, pendente e origem divergentes | T05 falhas em cada passo JPA/JDBC/auditoria e antes/depois de flush/commit; zero confirmação parcial; teste no emissor real, não só testemunho |
| R06 Alto — concorrência/deadlock | Locks novos versus emissão/anulação existentes | T06 múltiplas séries/docs em ordens opostas, latência do provider, níveis de isolamento e 40001; commit único ou aborto controlado, sem aceitar falha arbitrária como sucesso |
| R07 Crítico — resposta incerta/replay | Assinatura remota órfã, dupla emissão ou resposta trocada | T07 timeout antes/depois de assinatura e commit, duplicação e digest diferente; reconsulta/retry correlacionados; nunca segundo número/documento por replay |
| R08 Crítico — adulteração SQL | Estrutura válida e criptografia falsa; escritor tenta mudar âncora | T08 INSERT/UPDATE/DELETE/TRUNCATE, reassociação e constraints antecipadas/diferidas; imutabilidade SQL e recusa criptográfica/de confiança antes de uso fiscal |
| R09 Alto — PDF/QR histórico | Reimpressão usa versão activa/reassina/anulado errado | T09 sem chamada ao assinador ao imprimir após rotação/revogação; Q/R da prova/registo históricos, ATCUD e geometria; reserva FAQ integral resolvida antes de política de estado |
| R10 Crítico — fuga demo | Chave sintética ou série demonstrativa em produção | T10 perfis/base/cluster misturados, múltiplas fontes, credencial cruzada e payload demo; recusa/409, sem fallback; não alterar demo existente |
| R11 Crítico — perda/compromisso | Chave/certificado/registo indisponível ou revogado | T11 procedimento de pausa, rotação/recovery/restore em isolamento; preservação da verificação histórica e política fiscal de incidente validada |
| R12 Crítico — restore regressivo | Backup anterior a documento já entregue reinicia sequência | T12 restauração comparada com testemunho independente de backup (registo/trilho/entregas autorizadas); nenhum sucessor duplicado; fechar emissão se não reconciliável |
| R13 Alto — arredondamento/projecção | GrossTotal assinado difere do snapshot/QR/exportação | T13 casos de fronteira de precisão e EUR; usar regra vigente comprovada, recusar incompatibilidade; sem mudar dívida/saldo por conveniência de assinatura |
| R14 Alto — custo/indisponibilidade | B remoto quotas/WAN; A/B1 host/dispositivo | T14 ensaio não pago/autorizado ou especificação comprovada de capacidade/falha/offline; SLO/RTO/RPO e custo aprovados antes de operação |
| R15 Crítico — activação implícita | Bean torna emissor disponível sem homologação | T15 fonte presente com gate ausente/manifesto vazio: recusa; 2/1/1, QR/PDF, AT, RC/RG, FinancialCore/1e/#38 não abrem por disponibilidade técnica |
| R16 Alto — vazamento/auditoria falsa | Segredos/dados financeiros em logs ou tentativa abortada como sucesso | T16 inspecção de logs/erro/traces, permissões, falha de canal de auditoria; sem segredos, eventos de emissão só após commit; detalhes financeiros minimizados |

## Menor sequência futura proposta, sem executar

1. Rever este dossier e obter decisões D-SIGN-02..05 suficientemente delimitadas;
   nomear validação fiscal e bootstrap da confiança. Incompatibilidade normativa
   impede escolha técnica do provider, não dispensa de requisitos.
2. Após mandato próprio, implementar em isolamento o contrato de registo confiável
   e prova produtiva **ainda inactiva**, com adaptador de teste/duplo de fornecedor,
   sem reintroduzir o ensaio H2-07 no JAR. T01/T02/T03/T08 primeiro.
3. Após novo perímetro autorizado, integração transaccional no emissor FT com
   T04–T16 e regressão dirigida; material de ensaio e emissão produtiva fechada.
   Provisionamento real, homologação, base comercial, deploy e activação são
   operações autónomas posteriores. Não estimar prazo sem decisões/capacidade.
