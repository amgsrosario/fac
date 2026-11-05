# Proposta técnica delimitada — podeEmitir

A regra aprovada em #46/6637438647 exige true quando todas as pré-condições obrigatórias verificáveis na consulta estejam satisfeitas. A falta conhecida produz false com motivo compreensível. A emissão conserva a revalidação transaccional. Esta especificação aguarda mandato próprio de implementação.

## Contrato actual e proposta aditiva

GET /documentos-comerciais/{id}/diagnostico devolve DocumentoComercialDiagnosticoDto. Actualmente getDiagnostico avalia RASCUNHO, não anulado, existência de linha COMERCIAL e totais coerentes. As listas alertas/bloqueios incluem também observações de recebimentos/anulação. M05 observou true seguido de recusa400 por código AT ausente.

Proposta: manter endpoint e campos existentes, acrescentando avaliacaoEmissao com avaliadoEm, impedimentos (código/mensagem), avisos (código/mensagem), confirmacaoRecalculoNecessaria e totais projectados para confirmação. podeEmitir será a ausência de impedimentos documentais/configuracionais detectados. Avisos não o tornam false. Preservar podeAnular e a semântica dos bloqueios antigos, sem converter toda a lista antiga em impedimentos de emissão. Os consumidores apresentam os novos motivos específicos e continuam a tratar as mensagens antigas no contexto apropriado.

O GET exige DOCUMENTO_CONSULTAR, o POST DOCUMENTO_EMITIR. **podeEmitir não avalia a autorização do actor.** A UI combina o valor com canEmit e o estado de carregamento. Um cliente com consulta pode obter true e receber403 ao tentar emitir. Autenticação/autorização e resolução do emissor são controladas no POST; não criar uma nova autorização no diagnóstico.

## Recálculo e avaliação sem efeitos persistentes

RECALCULO_POR_CONFIRMAR é **aviso**, acompanhado de confirmacaoRecalculoNecessaria=true quando o indicador persistido `fiscalRecalculoAviso` está activo, ou quando um documento legado (`fiscalMotorVersion == null`) teria esse indicador activado pelo recálculo projectado segundo as mesmas comparações do POST. Com os restantes requisitos satisfeitos, podeEmitir continua true e permite abrir a confirmação. O operador revê os valores actuais antes de enviar recalculoConfirmado=true. Se a gravação ou um novo cálculo alterar os montantes depois da primeira confirmação, a confirmação anterior perde validade e a UI deve pedir nova confirmação sobre os valores actualizados.

Limite actual: LinhaDocumentoComercialService.recalcularTotais chama enquadrar/aplicar, altera entidades e executa saveAll/save. buildDiagnosticoTotais compara valores persistidos; não simula todo o motor fiscal actual. Uma anotação readOnly por si só não converte o recálculo actual em função pura.

Proposta de implementação no mandato futuro: carregar os dados necessários num serviço de avaliação separado, com transacção Spring explicitamente readOnly e isolamento REPEATABLE_READ, sem transacção de escrita exterior. O serviço actual usa jakarta.transaction.Transactional e getDiagnostico não tem transacção, pelo que esse limite deve ser alterado deliberadamente e testado. Construir DTOs de cálculo a partir do snapshot, reutilizar MotorFiscalService.calcular e MotorMonetario e acumular os mesmos totais/arredondamentos em valores imutáveis. Não chamar enquadrar/aplicar/recalcularTotais, setters de entidades geridas, saves ou fornecedores de assinatura. Extrair a acumulação comum apenas se necessário para evitar divergência do cálculo, sem refactorizações laterais.

O cálculo em DTO deve projectar também os dados de enquadramento necessários, usando os valores de RIva e artigos lidos na mesma transacção, sem alterar o documento. Comparar a projecção com os totais persistidos para avaliar a coerência, mantendo o critério do aviso separado. O aviso resulta do indicador persistido já activo OU da condição de activação no legado: mudança projectada de bruto, desconto ou taxa de uma linha (incluindo valor anterior nulo), ou mudança do total documental quando o total anterior não é nulo. Um legado já recalculado com indicador activo continua a exigir confirmação mesmo com totais iguais. Uma diferença num documento não legado, com indicador inactivo, não cria por si a exigência de confirmação. O cálculo projectado não limpa nem grava o indicador. Uma incoerência que permanece depois da projecção, ou uma recusa do motor, gera impedimento. Antes de implementar, provar paridade dos totais projectados com o cálculo do POST, incluindo histórico recalculável. Se essa paridade não puder ser demonstrada dentro do mandato, escalar o limite e não declarar que o diagnóstico cobre essa parte.

## Inventário das recusas e motivos propostos

O inventário refere-se ao percurso comercial actual de DocumentoComercialService.emitir e dependências. Nenhuma regra fiscal adicional é criada pelo diagnóstico. Os códigos abaixo são proposta de contrato, a fixar no mandato.

| Recusa actual / fonte | Motivo ou tratamento no diagnóstico | Alcance / exclusão justificada |
| --- | --- | --- |
| Documento inexistente, findDocumentoForUpdate | 404 | Preservar erro de recurso, sem fabricar documento ou true |
| validateRascunho / validateNaoAnulado | ESTADO_INCOMPATIVEL / DOCUMENTO_ANULADO | Predicados de estado existentes |
| validateTemLinhas | SEM_LINHAS_COMERCIAIS | Conferir pelo menos uma linha COMERCIAL |
| validateDataEmissao | DATA_EMISSAO_ANTERIOR_SERIE | Ler última data emitida da mesma série/tipo; POST revalida |
| Moeda diferente de EUR | MOEDA_NAO_HABILITADA | Preservar perímetro actual |
| recalcularTotais, retenção documental não zero | RETENCAO_NAO_HABILITADA | Regra já existente, sem decidir distribuição fiscal |
| Totais fixos, categoria desconhecida | CATEGORIA_IVA_NAO_SUPORTADA | Apenas categorias actualmente aceites |
| MotorFiscalService.calcular, jurisdição/categoria/regime/taxa | JURISDICAO_NAO_SUPORTADA, CATEGORIA_IVA_NAO_SUPORTADA, TAXA_TERRITORIAL_AUSENTE_OU_AMBIGUA, REGIME_SEM_TAXA, TAXA_INCOMPATIVEL | Reutilizar causas existentes, sem alterar taxas ou territorialidade |
| MotorFiscalService.calcular, não liquidação/isencão | COMBINACAO_FISCAL_NAO_HABILITADA, ISENCAO_SEM_M07_VALIDADO, PROJECCAO_NAO_LIQUIDACAO_NAO_HABILITADA | Mesmas combinações existentes e mensagens compreensíveis |
| MotorFiscalService.calcular, motivos/fundamentos/M16 | MOTIVO_FISCAL_INCOMPATIVEL, M16_SERVICO_NAO_HABILITADO, FUNDAMENTO_FISCAL_AUSENTE | Sem inferir elegibilidade fiscal |
| MotorMonetario.calcular/validar | VALORES_OU_PRECISAO_INVALIDOS, DESCONTO_INVALIDO | Quantidade, preço, taxa, desconto e limites já impostos |
| fiscalRecalculoAviso activo após recálculo e dto.recalculoConfirmado ausente | RECALCULO_POR_CONFIRMAR, na lista de avisos | Usar o indicador persistido ou projectar a sua activação em legado pelas comparações existentes. Manter true se apenas este aviso existir; permitir confirmação explícita no POST |
| validateTotaisCoerentes depois do cálculo | TOTAIS_INCOERENTES | Comparar projecção e invariantes do cálculo, com a distinção de aviso indicada acima |
| Empresa.EMPRESA_ID ausente | EMPRESA_AUSENTE | Presença exigida para o snapshot |
| snapshotCliente / buildImpressao | DADOS_CLIENTE_INSUFICIENTES | Conferir relações/campos efectivamente necessários, preservando tratamento existente de dado inconsistente |
| SerieService.findForUpdate / temCodigoAt | SERIE_AUSENTE / CODIGO_AT_AUSENTE | Ler associação e presença, sem reservar número. Não criar exigência adicional de data: o POST testa temCodigoAt; código/data são também validados no CRUD de séries |
| AtcudService.gerar, código com prefixo ATCUD: | CODIGO_AT_COM_PREFIXO_INVALIDO | Verificar trim e prefixo sem distinguir maiúsculas/minúsculas como no serviço. O número sequencial positivo continua a ser validado no POST, sem reserva pelo GET |
| FiscalQrService flag disabled | QR_DESACTIVADO | Mesma recusa do emissor |
| FiscalQrService.source zero ou múltiplas fontes | FONTE_ASSINATURA_AUSENTE / FONTES_ASSINATURA_AMBIGUAS | Descoberta de fontes, sem chamar commercial() |
| source.commercial retorna vazio | Estado indisponível conhecido pode ser motivo; resultado dinâmico fica no POST | O contrato actual não oferece readiness. Não chamar a fonte nem gerar documento numerado fictício no GET |
| identity/fullSignature/certificado | Validação do resultado fica no POST | Documento/numero/ATCUD e assinatura ainda não existem integralmente na consulta; não é uma pré-condição verificável antecipadamente |
| QrFiscalPayloadBuilder, NIF do emitente obrigatório/dígitos | NIF_EMITENTE_AUSENTE_OU_INVALIDO | Mesmo normalizador/validação actual, sem introduzir algoritmo de validação fiscal novo |
| País do adquirente e tipo fiscal obrigatório | PAIS_ADQUIRENTE_AUSENTE / TIPO_FISCAL_AUSENTE | O tipo usa o fallback actual do builder; não passar a exigir código fiscal universal |
| Número completo / ATCUD / data do QR | Validar entradas disponíveis; número/ATCUD finais ficam no POST | Data existente é verificável; números são gerados na emissão, sem os reservar no GET |
| QrFiscalValidator, formato/tamanho/ordem/campos | Reutilizar verificações aplicáveis às entradas projectadas; erro de campo disponível como DADOS_QR_INVALIDOS | Q/R, número final e mensagem completa são verificados no POST, sem assinatura prévia |
| PendenteService.novoPendente, total <=0 | TOTAL_NAO_POSITIVO_PARA_PENDENTE | Aplicar quando o percurso actual cria pendente (não imediato), incluindo a FT diferida; usar total projectado |
| Catálogo/V28 | VINCULO_FISCAL_INCOERENTE retirado como bloqueio autónomo novo | O POST não chama esse validador; triggers preservam configurações vinculadas. Inventário global FiscalConfigurationReadiness continua separado; não bloquear tipos legados pelo catálogo vazio |
| FKs, constraints diferidas, exclusão financeira, locks e concorrência | Revalidar no POST e preservar rollback/erro | Factos concorrentes/integridade final não se garantem pelo GET. Falha conhecida ao ler dados deve ser reportada, sem sucesso fictício |
| Actor/emissor, autorização e validação do corpo HTTP | 401/403/erros do POST mantidos | O diagnóstico é documental, não autorização ou validação de um corpo que ainda não recebeu |

Na implementação, cruzar os throw/validadores acima com os testes existentes e completar qualquer causa adicional alcançável no motor. Uma falha inesperada de infraestrutura/programação conserva o tratamento de erro da API; não converter silenciosamente essa falha em elegibilidade. Não acrescentar um requisito universal de identidade fiscal, taxa, catálogo ou assinatura real onde os percursos de teste/legados existentes têm contratos próprios.

Uma única fonte presente não comprova o sucesso da assinatura futura. Uma eventual interface interna de disponibilidade só pode fornecer factos de leitura já definidos, sem resolver D-SIGN ou contactar a AT por efeito do GET. A implementação deve documentar os limites de verificabilidade, em particular respostas dinâmicas da fonte e validações do resultado.

## Três consumidores UI e HTML

A regra comum é distinguir preparar/conferir de emitir. A ausência de diagnóstico actualizado nunca autoriza um POST de emissão. Após guardar cabeçalho ou linhas, recarregar documento/totais, obter novo diagnóstico e invalidar o anterior. Cancelar ou ignorar respostas antigas por identidade/versão da edição. Se houver novos valores/aviso, apresentar a confirmação sobre esses valores e exigir confirmação renovada. O emissor real pode ainda recusar por mudança concorrente.

| Consumidor actual | Comportamento proposto |
| --- | --- |
| frontend/src/DocumentosView.tsx, openEmission/emitDocument | Manter acção de conferir e botão final desactivado se carregamento/false. Recarregar diagnóstico ao abrir e após qualquer gravação, mostrar motivos específicos e avisos. Confirmar valores/recálculo explicitamente; não enviar true apenas porque o flag existe |
| frontend/src/ui/commercial/documents/DocumentsView.tsx, DocumentDetail/emitir | Retirar fallback de linhas.length>0 para autorização. Para actor canEmit e rascunho, manter acção visível mas desactivada enquanto sem diagnóstico ou false, com motivos/avisos visíveis. Obter diagnóstico actualizado antes da confirmação e após guardar; propagar recalculoConfirmado só após aceitação dos valores actuais |
| frontend/src/ui/commercial/documents/DraftDocumentEditor.tsx, canEmitCurrent/emitDocument | Para novo/dirty, permitir a acção de guardar e conferir sob permissões existentes, sem a tratar como emissão elegível. Guardar pelo percurso actual, reler documento e linhas, obter diagnóstico e exibir impedimentos/avisos. Só então confirmar e enviar POST se elegível. Para rascunho já guardado, exigir diagnóstico actualizado; remover o atalho de elegibilidade por novo/dirty |
| Diagnóstico HTML | Apresentar os mesmos impedimentos de emissão, avisos e totais do DTO; preservar as mensagens de consulta/anulação no contexto respectivo |

Para os três consumidores, canEmit continua a controlar permissão de interface. Erros ao guardar ou ao obter diagnóstico impedem prosseguir, com mensagem existente e possibilidade de repetir a consulta. Uma recusa do POST conserva a resposta e actualiza os dados/diagnóstico quando apropriado. O aviso de recálculo isolado mantém o botão de confirmação utilizável. Novo ou dirty pode mostrar “Guardar e conferir”, sem antecipar afirmação “pode emitir”.

## Concorrência, efeitos e regressões do mandato futuro

O serviço de avaliação deve produzir um snapshot coerente por consulta, com read-only/REPEATABLE_READ verificado na fronteira Spring escolhida. Os locks, reservas de número e escrita financeira ficam no POST. Após o GET, qualquer alteração relevante pode tornar a emissão inelegível e o POST deve recusar com rollback.

Regressões dirigidas propostas:
- Cada linha verificável da tabela deve ter caso de false/motivo, reutilizando dados sintéticos e constraints activas. As exclusões devem ter prova de revalidação no POST.
- Sem código AT ou fonte: false e motivo, preservando400/409 e snapshots após recusa.
- Recálculo por confirmar, sem outro impeditivo: true com aviso; guardar, reler, confirmar valores novos e enviar recalculoConfirmado. Recálculo superveniente no POST exige nova revisão dos valores segundo contrato actual.
- Paridade do aviso com o POST: legado já recalculado, indicador persistido activo e totais iguais mantém aviso/confirmacão; documento não legado com diferença e indicador inactivo não passa a exigir confirmação só pela diferença. Incluir legado ainda não recalculado cuja projecção activaria o indicador por linha ou total. Em todos os casos, o aviso isolado permite confirmar e prosseguir; o GET não altera o indicador.
- Código AT com prefixo ATCUD: gera false com CODIGO_AT_COM_PREFIXO_INVALIDO; POST mantém a recusa existente.
- Paridade do cálculo puro com o cálculo real, incluindo legado recalculável, retenção, categorias e regimes. Comparar tabelas/sequências antes/depois do GET e provar ausência de setters/saves/chamada à fonte.
- Tipos legados conservam o comportamento actual; tipo governado usa regras existentes sem gate global de criação.
- Troca de série/data/taxa depois do GET: POST revalida. Uma transacção de leitura não congela o estado para a emissão posterior.
- Em cada um dos três consumidores, API simulada para novo/dirty, guardar com aviso novo, gravação falhada, diagnóstico ausente/false/true, resposta atrasada, confirmação/cancelamento e403. Verificar corpo do POST e reavaliação após guardar.
- Fixture FT/1 com FiscalSignatureTestConfiguration permanece apenas MockMvc. Reutilizar a prova financeira M4-02 sem repetir suites não afectadas.

Apenas documentação nesta fase. Os mandatos futuros definem testes proporcionais e revisão independente, preservando manifesto produtivo, históricos, dados reais, RC/RL, FinancialCore, Fatia 1e e fronteiras de publicação/integração. Não há nova arquitectura nem reabertura de H2-07/H2-08. Os campos técnicos já aprovados não voltam como escolha de negócio.
