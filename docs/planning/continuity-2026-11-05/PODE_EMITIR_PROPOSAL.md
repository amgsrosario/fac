# Proposta técnica delimitada — podeEmitir

Regra aprovada em #46/6637438647. Todas as pré-condições obrigatórias verificáveis no momento da consulta devem estar satisfeitas para podeEmitir=true. A falta conhecida de códigoAT, fonte de assinatura ou outro impeditivo deve produzir false com motivo compreensível. A consulta é uma avaliação prévia; o acto de emitir mantém validação transaccional. A presente proposta aguarda mandato de implementação.

## Contrato actual e consumidores

GET /documentos-comerciais/{id}/diagnostico, DocumentoComercialDiagnosticoDto. O método getDiagnostico actualmente calcula podeEmitir com RASCUNHO, nãoanulado, presença de linhaCOMERCIAL e totais coerentes. As listas alertas/bloqueios também incluem observações de recebimentos e anulação. AUI usa podeEmitir para texto e botão. A versãoHTML do diagnóstico reutiliza a informação. M05 observou podeEmitir=true e bloqueios vazios, seguidos de400 por série sem códigoAT.

A assinatura actual FiscalDocumentSignatureSource só oferece commercial/financial com documento numerado. A descoberta de beans e o flagQR estão em FiscalQrService. Existe FiscalConfigurationReadiness para inventário da instalação, cujo operationalActivationPermitted() é semprefalse. Esse relatório global não pode ser convertido directamente no gate de cada rascunho.

## Contrato proposto para um mandato próprio

Preservar endpoint, identificadores, totais, pendente, alertas/bloqueios e podeAnular. Alterar a semântica de podeEmitir para a regra aprovada. Acrescentar, se necessário após conferir todos os consumidores, avaliacaoEmissao com instante de avaliação e motivos específicos de emissão, cada um com código estável e mensagem. AUI e oHTML devem apresentar esses motivos quando false, sem usar como impeditivos de emissão todas as mensagens antigas de bloqueios, que têm outros fins. Uma versão mínima pode conservar oDTO e acrescentar as mensagens de emissão à lista existente; a implementação deve provar que não mistura regras de anulação com emissão.

Proposta preferida: avaliação explícita de emissão, aditiva noDTO, para separar o significado das mensagens. Não introduzir endpoint novo. Códigos propostos incluem ESTADO_INCOMPATIVEL, DOCUMENTO_ANULADO, SEM_LINHAS_COMERCIAIS, DATA_EMISSAO_INVALIDA, MOEDA_NAO_HABILITADA, TOTAIS_INCOERENTES, RECALCULO_POR_CONFIRMAR, EMPRESA_AUSENTE, SERIE_AUSENTE, CODIGO_AT_AUSENTE, QR_DESACTIVADO, FONTE_ASSINATURA_AUSENTE, FONTES_ASSINATURA_AMBIGUAS e VINCULO_FISCAL_INCOERENTE. Confirmar cada código contra as validações efectivas antes de fixar aAPI.

O actor e a autorização devem manter as regras actuais. Falta de permissão de consulta preserva401/403. A avaliação da elegibilidade documental e da configuração deve evitar transformar a ausência de uma autorização de interface em aprovação fiscal. A validação do emissor e a autorização de emissão continuam noPOST.

## Pré-condições e reutilização

| Área | Verificação de leitura proposta | Fonte e cuidado |
| --- | --- | --- |
| Estado/linhas/datas | Regras existentes de rascunho, anulação, linhaCOMERCIAL e data | Extrair apenas predicados puros comuns aos métodos validate existentes; cobrir alterações concorrentes noPOST |
| Montantes | Coerência e sinal admissível; necessidade de confirmação de recálculo | buildDiagnosticoTotais/validações de emissão; simular cálculo sem gravar ou mudar os totais emGET |
| Emitente/cliente | Presença e dados obrigatórios usados pelo snapshot/QR | Reutilizar validações conhecidas do código; sem introduzir novos requisitos fiscais ou chamadasAT |
| Moeda | EUR e gates actuais | Reutilizar a recusa existente da emissão nãoEUR |
| Série | Existência, associação ao tipo e código/data exigidos pelo contrato actual | Leitura de SerieRepository; nunca chamar proximoNumeroParaEmissao, reservar número ou bloquear com escrita |
| Catálogo | Coerência do vínculo para tipos já governados | Revisão exacta eFtDeferredSemantics; catálogo vazio decriação não pode bloquear indiscriminadamente tipos legados que oemissor admite |
| QR | Flag activo e requisitos de payload verificáveis nos dados disponíveis | Reutilizar validação pura, sem emitir QR persistido |
| Assinatura | Detectar zero ou múltiplas fontes, e estado indisponível conhecido | Consulta de metadados/capacidade sem chamar commercial() nem criar assinatura, chave ou documento fictício |

Falta de fonte conhecida é um impeditivo, assim como ambiguidade. Uma única fonte presente não comprova que uma assinatura futura já existe ou será válida. O contrato actual não oferece readiness de fornecedor. O mandato deve começar pela informação verificável disponível, como quantidade de fontes e flags, e declarar os testes dinâmicos feitos apenas naemissão. Caso seja necessário um contrato de disponibilidade adicional, propor uma interface interna read-only específica, sem alterar por inferência o desenho produtivo D-SIGN ou realizar chamadas remotas. O comportamento dos fornecedores de teste deve permitir exercitar estados conhecidos.

Uma falha de leitura da configuração obrigatória deve manter a avaliação fechada com motivo compreensível, segundo o tratamento de erro existente. Erros de programação ou infraestrutura não devem ser engolidos nem convertidos em sucesso. Preservar404, autenticação e autorização daAPI.

## Concorrência e efeitos

GET deve executar somente leituras coerentes do rascunho e da configuração. Não consome numeração, não altera snapshots, não gera assinatura, não muda estado financeiro nem tenta reparar dados. Não promete que o estado fica congelado após a resposta. OPOST relê sob os locks já existentes, recalcula e valida, podendo recusar após uma alteração concorrente. As recusas e rollback existentes conservam códigos/mensagens até mandato explícito de alteração. Não tornar oPOST dependente de ter sido feito umGET ou de um token fictício de aprovação.

## Regressões propostas

| Caso | Critério |
| --- | --- |
| ReproduçãoM05 semcódigoAT | GETfalse e motivo específico; POST400 conserva dados/numeração |
| Ausência/ambiguidade de fontes eQRdesactivo | false, mensagem explícita e controlo doPOSTpreservado |
| FT/1 e fonte sintética apenasMockMvc | Dados elegíveis produzemtrue, emissão pelo percurso de teste mantém os seis critérios M4-02 |
| Rascunho semlinhas, anulado/emitido, data inválida, moeda nãoEUR | false por cada impeditivo conhecido, conforme contratos existentes |
| Totais incoerentes/recálculo | GETsemalterar dados, motivo/necessidade de confirmação visível; POST conserva confirmaçãoactual |
| Vínculo incoerente/tipo legado | Recusa governada específica; comportamento legado não passa a depender do ponteiro decriação |
| Empresa/série ausentes | Motivosconcretos e erros deconsultaconforme contrato, semcriaçãoautomática |
| Mudança apósGET | POST recusa com rollback quando pré-condição deixa de existir |
| Ausência de efeitos | Snapshots das tabelas/sequências iguais antes/depois doGET, nenhuma chamada ao fornecedor deassinatura |
| Consumidores | UI/HTML exibem motivo e mantêm botão coerente, podeAnular e consultas existentes preservados |

Testes proporcionais no mandato futuro: unidade dos predicados, MockMvc+PostgreSQL descartável dos gates/leitura pura e teste deUI comAPI simulada para botão/mensagens. Reutilizar fixtures existentes. O ensaio não deve publicar manifesto, activar circuitos ou repetirM05.
