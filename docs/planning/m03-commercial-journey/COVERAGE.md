# M03 — Levantamento dirigido anterior aos ensaios

Base integrada `e6bb33f76523a268b39978733e099c96e98d7999` (PR #50), confirmada por API/Git/FETCH_HEAD, ancestralidade do HEAD M02 e tree igual à versão aprovada `e09433b`. Worktree isolada `codex/m03-commercial-journey`; principal/alterações preexistentes e restantes worktrees preservados. Mandato [#46/6613324580](https://github.com/amgsrosario/fac/issues/46#issuecomment-6613324580), contrato M03 em MISSIONS.md. Isto não é auditoria geral de segurança nem reabertura de M01/M02/#38.

## Matriz de cobertura e lacunas à entrada

| Percurso | Cobertura existente identificada (não execução nova) | Prova válida reutilizada / lacuna | Selecção M03 |
| --- | --- | --- | --- |
| Empresa | EmpresaControllerTests criaConsultaEAtualizaEmpresa, negativos NIF/IBAN/capital/logótipo | Código/testes presentes; falta percurso encadeado autenticado sem fonte fiscal na base integrada | Jornada nova CRUD; dois negativos existentes. Não repetir uploads/logótipo fora da lacuna |
| Clientes | ClienteControllerTests: criação/defaults, PT/estrangeiro, referências/pesquisa | CRUD entregue; cruzar criação/leitura/edição com rascunho e permissões | Jornada + três casos existentes de validade/dependências/estrangeiro |
| Artigos/serviços | ArtigoControllerTests: CRUD, código/identificação/tipo/paginação | CRUD entregue; cruzar alteração com linha/totais | Jornada + quatro negativos existentes |
| Parâmetros | ParametrosClienteControllerTests (3), ParametrosDocumentoComercialControllerTests (2) | Configuração entregue; dependências e singleton merecem prova V29 actual | Reutilizar cinco casos e percurso autenticado; sem nova regra fiscal |
| Rascunho + primeira linha | DocumentoComercialCriacaoService transaccional; DocumentoComercialControllerTests criaDocumentoComPrimeiraLinhaAtomicamente e criaAtualizaEApagaRascunhoDocumentoComercial | Testes antigos da classe importam fonte fiscal e fixtures governadas sintéticas. Não é prova actual de jornada sem esses duplos | Novo cenário real de commits HTTP sem transacção exterior de teste, sem qualquer fonte de assinatura ou revisão fiscal |
| Linhas/reordenação/consultas | LinhaDocumentoComercialController/Service e casos existentes da classe comercial | Lacuna de encadeamento com perfis e falha de primeira linha sem header/auditoria órfãos | Novo teste, incluindo total da fixture, leitura, ordenação, exclusão e relação pai/filho |
| Exportação não fiscal | DadosMestresImportExportTests exportaClientesEArtigosEmMaisDeUmaPagina e exportaModelosEExportacoesComProtecaoCsv | CSV/XLSX não são SAF-T; importa separar permissões e múltiplas páginas | Só dois casos de exportação + percurso autenticado CSV/XLSX; importação não executada |
| Permissões/JWT | SecurityIntegrationTests/PermissionMatrixIntegrationTests; prova M02 99 casos aprovada e R1 33, pareceres independentes | src/main na integração coincide com e09433b; prova geral válida reutilizada, não repetir suites. Falta evidência dos mesmos percursos sem fonte fiscal | JWT/login reais no novo cenário para ADMINISTRADOR/OPERADOR/CONSULTA, positivos/negativos limitados aos percursos M03 |
| Auditoria | AuditoriaControllerTests paginaVolumesSemLimiteDeCem e combinaFiltrosEOrdenaDeFormaEstavel | Leitura/filters presentes; falta trilho durável da sequência comercial sem emissão | Dois testes existentes e verificações novas de eventos/rollback em transacções HTTP reais |
| Gates | Manifesto revisions=[]; FiscalQrService exige fonte; TipoDocumentoService recusa FT diferida sem binding | M01/M02 não são autorização de emitir nem homologação 2/1/1 | Negativos independentes: catálogo/FT 409, assinatura 409/rollback número, série sem código 400 |

Caminhos Java relativos a `src/main/java/com/ar2lda/fac/`, testes a `src/test/java/com/ar2lda/fac/`. Nenhuma presença de teste é apresentada como aprovação de execução M03.

## Fronteira das fixtures e da prova

Novo cenário usa apenas dados/contas sintéticos em PostgreSQL descartável. Fixtures de referências/tipo e identidades são pré-condições de teste, não provisionamento real nem reexecução M02. Login usa JWT/validação de utilizador/permissões existentes, sem bypass. Transacções de cada pedido são efectivamente concluídas; o teste não envolve a jornada numa transacção global que esconda defeitos de rollback.

Para testar rascunhos sem publicar uma revisão fiscal, existe uma fixture **não classificada**, de forma legada, com código fiscal NULL e área/classificação técnica já suportadas. Não é nova tipologia, FT homologada, escolha 2/1/1 ou configuração comercial aprovada. Série sem código e outra com marcador sintético apenas para atingir a recusa de assinatura, sem comunicação AT ou emissão bem-sucedida. O código sintético não é código AT legítimo. Nenhum fornecedor de assinatura ou revisão fiscal (mesmo sintética) é criado pelo novo cenário. Os cinco testes de parâmetros antigos também só preparam fixtures legadas sem homologação; não usar resultados como configuração fiscal real.

Sem rascunho FT produtivo criado a partir de catálogo vazio: esse percurso está deliberadamente bloqueado por H2-03/gates. Não fabricar catálogo para pintar a jornada de verde. Esse bloqueio esperado será separado de um defeito real.

MockMvc com serviços/persistência reais não é browser, deployment TLS/HBA, isolamento runtime comercial, SAF-T ou emissão/certificação. M04/M05 e assinatura continuam por autorizar; observações M02 O2–O9 conservadas e não resolvidas implicitamente. Os ensaios existentes de segurança aqui reutilizados não são repetidos nem reabertos.
