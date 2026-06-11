# FAC - Integração com a Autoridade Tributária e SAF-T (PT)

## 1. Estado do Documento

**Versão:** 1.0
**Data:** 11-06-2026
**Estado:** Ativo
**Projeto:** FAC
**Autor:** António Rosário
**Finalidade:** Documento basilar para orientar o desenvolvimento da integração fiscal do FAC com a Autoridade Tributária e Aduaneira.

---

## 2. Objetivo

Este documento define os princípios técnicos, funcionais e fiscais que devem orientar o desenvolvimento da componente de integração do FAC com a Autoridade Tributária.

O objetivo é garantir que o FAC seja desenvolvido desde o início com uma estrutura compatível com:

* geração de ficheiro SAF-T (PT);
* comunicação de documentos de faturação;
* comunicação de séries documentais;
* geração e utilização de ATCUD;
* preparação para certificação de software de faturação;
* testes em ambiente de validação/homologação;
* futura evolução para comunicação automática via webservice.

Este documento não substitui a legislação, as portarias, os manuais técnicos da AT, os esquemas XSD oficiais nem o processo formal de certificação. Serve como referência interna de arquitetura e decisão.

---

## 3. Princípio Orientador

A integração com a AT não deve ser tratada como uma funcionalidade acessória.

No FAC, a componente fiscal deve ser considerada uma parte estrutural do sistema, tal como a base de dados, a faturação, os clientes, os artigos e os impostos.

Todas as decisões funcionais devem respeitar três princípios:

1. **Rastreabilidade**

    * Cada documento deve poder ser reconstruído, auditado e exportado.

2. **Imutabilidade fiscal**

    * Depois de emitido, um documento fiscalmente relevante não deve ser alterado nos seus elementos essenciais.

3. **Preparação para certificação**

    * Mesmo que o FAC comece como projeto interno ou MVP, a arquitetura deve estar preparada para evoluir para software certificável.

---

## 4. Âmbito da Integração AT

A integração com a AT no FAC deverá abranger, no mínimo, as seguintes áreas:

### 4.1 SAF-T (PT) de Faturação

O FAC deve gerar ficheiros SAF-T (PT) com os dados fiscalmente relevantes da faturação.

Deve ser prevista a exportação de:

* dados da empresa;
* clientes;
* produtos e serviços;
* impostos;
* faturas;
* faturas simplificadas;
* notas de crédito;
* notas de débito;
* recibos, quando aplicável;
* documentos de transporte, quando aplicável;
* documentos anulados;
* documentos retificativos;
* totais fiscais.

### 4.2 SAF-T Simplificado

O FAC deve estar preparado para gerar ficheiros com a estrutura adequada à comunicação mensal da faturação, quando aplicável.

Este ficheiro deve conter apenas os elementos necessários para comunicação dos documentos fiscalmente relevantes à AT.

### 4.3 SAF-T Total

O FAC deve estar preparado para gerar ficheiros completos, quando aplicável, contendo informação mais abrangente para efeitos de auditoria, inspeção ou exportação integral.

Mesmo que numa primeira fase o foco seja apenas faturação, o modelo de dados não deve impedir futura evolução para SAF-T contabilístico.

### 4.4 Comunicação por Webservice

O FAC deve prever uma arquitetura que permita, numa fase posterior, comunicar diretamente com a AT através de webservices.

Esta comunicação poderá incluir:

* comunicação de documentos de faturação;
* comunicação de documentos de transporte;
* comunicação de séries;
* finalização de séries;
* validação de respostas da AT;
* armazenamento de códigos de resposta;
* gestão de erros e reprocessamentos.

### 4.5 Séries Documentais e ATCUD

O FAC deve incluir gestão própria de séries documentais.

Cada tipo de documento deve ter séries próprias e configuráveis.

O sistema deve prever:

* identificação da série;
* tipo de documento associado;
* data de início;
* data de fim, quando aplicável;
* número inicial;
* último número emitido;
* código de validação da série atribuído pela AT;
* estado da série;
* data de comunicação à AT;
* data de finalização da série.

O ATCUD deve ser gerado com base no código de validação da série e no número sequencial do documento.

---

## 5. Tipos de Documento Fiscalmente Relevantes

O FAC deve tratar como documentos fiscalmente relevantes todos os documentos que possam ter relevância fiscal, comercial ou probatória.

Devem ser considerados, pelo menos:

* FT - Fatura;
* FS - Fatura Simplificada;
* FR - Fatura-Recibo;
* NC - Nota de Crédito;
* ND - Nota de Débito;
* RC - Recibo;
* GT - Guia de Transporte;
* GR - Guia de Remessa;
* GC - Guia de Consignação;
* OR - Orçamento, quando venha a ser considerado relevante no âmbito da aplicação;
* EC - Encomenda, quando emitida ao cliente e suscetível de conferência futura.

A decisão final sobre os tipos documentais ativos deve ser alinhada com o modelo SAF-T, legislação aplicável e processo de certificação.

---

## 6. Regras Estruturais dos Documentos

Cada documento emitido pelo FAC deve conter, no mínimo, informação suficiente para:

* impressão;
* consulta;
* exportação SAF-T;
* comunicação à AT;
* auditoria interna;
* certificação futura.

### 6.1 Campos mínimos do cabeçalho

Cada documento deve conter:

* identificador interno;
* tipo de documento;
* série;
* número sequencial;
* número completo do documento;
* ATCUD;
* data de emissão;
* data de operação;
* cliente;
* NIF do cliente;
* país do cliente;
* morada fiscal, quando aplicável;
* estado do documento;
* utilizador emissor;
* data e hora de criação;
* data e hora de emissão;
* hash do documento;
* hash do documento anterior, quando aplicável;
* motivo de anulação, quando aplicável;
* documento de origem, quando aplicável;
* documento retificado, quando aplicável.

### 6.2 Campos mínimos das linhas

Cada linha deve conter:

* artigo ou serviço;
* descrição;
* quantidade;
* unidade;
* preço unitário;
* desconto;
* taxa de IVA;
* motivo de isenção, quando aplicável;
* valor líquido;
* valor de IVA;
* valor total;
* referência ao regime de IVA;
* classificação fiscal necessária ao SAF-T.

---

## 7. Imutabilidade Fiscal

Depois de emitido, um documento fiscalmente relevante não pode ser livremente editado.

O FAC deve distinguir claramente:

### 7.1 Documento em rascunho

Pode ser alterado livremente antes da emissão.

Não tem:

* número fiscal definitivo;
* ATCUD definitivo;
* hash fiscal definitivo;
* comunicação à AT.

### 7.2 Documento emitido

Depois da emissão:

* recebe número definitivo;
* recebe ATCUD;
* recebe hash;
* fica bloqueado para edição fiscal;
* passa a poder ser exportado para SAF-T;
* passa a poder ser comunicado à AT.

### 7.3 Correção de documento emitido

A correção deve ser feita através de:

* nota de crédito;
* nota de débito;
* documento retificativo;
* anulação, apenas quando fiscalmente admissível.

Nunca através de alteração direta dos valores originais.

---

## 8. Numeração e Séries

A numeração dos documentos deve ser:

* sequencial;
* cronológica;
* independente por tipo de documento e série;
* sem saltos injustificados;
* sem reutilização de números;
* persistida em base de dados de forma segura.

O FAC deve garantir que dois documentos nunca recebem o mesmo número dentro da mesma série e tipo documental.

A atribuição do número deve ocorrer no momento da emissão, não no momento da criação do rascunho.

---

## 9. ATCUD

O ATCUD deve ser tratado como campo obrigatório nos documentos fiscalmente relevantes, sempre que aplicável.

O FAC deve prever a seguinte estrutura lógica:

```text
ATCUD = Código de Validação da Série + "-" + Número Sequencial do Documento
```

O código de validação da série deve ser obtido através de comunicação prévia da série à AT.

Sem código de validação válido, a série não deve permitir emissão de documentos fiscalmente relevantes.

---

## 10. QR Code

O FAC deve prever geração de QR Code nos documentos impressos ou disponibilizados em PDF.

O QR Code deve ser gerado de acordo com as regras técnicas aplicáveis aos programas de faturação certificados.

A geração do QR Code deve ser feita a partir dos dados fiscais do documento, incluindo, quando aplicável:

* NIF do emitente;
* NIF do adquirente;
* país;
* tipo de documento;
* estado do documento;
* data;
* número do documento;
* ATCUD;
* base tributável;
* IVA;
* total;
* hash ou parte relevante do hash, quando aplicável.

---

## 11. Assinatura e Hash dos Documentos

O FAC deve prever um mecanismo de assinatura dos documentos fiscalmente relevantes.

O hash deve garantir a integridade da cadeia documental.

A arquitetura deve prever:

* geração de hash no momento da emissão;
* ligação ao documento anterior relevante;
* armazenamento do hash;
* impossibilidade de recalcular silenciosamente documentos emitidos;
* exportação do hash no SAF-T;
* validação interna da cadeia documental.

A implementação concreta deve respeitar a legislação e especificação técnica aplicável aos programas certificados.

---

## 12. Modelo de Dados Recomendado

### 12.1 Entidades principais

O FAC deve prever, no mínimo, as seguintes entidades:

```text
Company
Customer
Product
Tax
TaxExemptionReason
DocumentType
DocumentSeries
FiscalDocument
FiscalDocumentLine
FiscalDocumentTaxSummary
SaftExport
AtCommunication
AtCommunicationLog
AtCredential
```

### 12.2 DocumentSeries

Campos recomendados:

```text
id
code
description
documentType
startDate
endDate
initialNumber
lastIssuedNumber
validationCode
atCommunicationDate
atFinalizationDate
status
createdAt
updatedAt
```

Estados possíveis:

```text
DRAFT
READY_TO_COMMUNICATE
COMMUNICATED
ACTIVE
CLOSED
ERROR
```

### 12.3 FiscalDocument

Campos recomendados:

```text
id
documentType
series
number
fullNumber
atcud
issueDate
operationDate
customer
status
currency
exchangeRate
netTotal
taxTotal
grossTotal
hash
previousHash
sourceDocument
correctionReason
cancellationReason
createdBy
issuedBy
createdAt
issuedAt
updatedAt
```

Estados possíveis:

```text
DRAFT
ISSUED
CANCELLED
CORRECTED
COMMUNICATED
COMMUNICATION_ERROR
```

### 12.4 SaftExport

Campos recomendados:

```text
id
periodStart
periodEnd
exportType
fileName
filePath
generatedAt
generatedBy
status
validationStatus
validationErrors
hash
```

Tipos possíveis:

```text
SIMPLIFIED
FULL
AUDIT
MONTHLY_COMMUNICATION
```

### 12.5 AtCommunication

Campos recomendados:

```text
id
communicationType
relatedDocument
relatedSeries
requestPayload
responsePayload
responseCode
responseMessage
status
sentAt
receivedAt
retryCount
```

Tipos possíveis:

```text
INVOICE_COMMUNICATION
TRANSPORT_COMMUNICATION
SERIES_COMMUNICATION
SERIES_FINALIZATION
SAFT_SUBMISSION
VALIDATION
```

---

## 13. Módulo Técnico Recomendado

A integração AT deve ficar isolada num módulo próprio.

Estrutura recomendada:

```text
fac-at
 ├── saft
 │    ├── exporter
 │    ├── mapper
 │    ├── validator
 │    ├── xsd
 │    └── model
 │
 ├── series
 │    ├── communicator
 │    ├── validator
 │    └── service
 │
 ├── documents
 │    ├── hash
 │    ├── qrcode
 │    ├── signature
 │    └── fiscal-rules
 │
 ├── webservice
 │    ├── client
 │    ├── auth
 │    ├── request
 │    ├── response
 │    └── retry
 │
 └── certification
      ├── fixtures
      ├── scenarios
      └── tests
```

---

## 14. Interfaces Técnicas

A aplicação principal não deve depender diretamente dos detalhes técnicos da AT.

Deve ser criada uma camada de abstração.

### 14.1 Interface principal

```java
public interface TaxAuthorityGateway {

    SaftExportResult generateSaft(SaftExportRequest request);

    AtCommunicationResult communicateInvoice(String documentId);

    AtCommunicationResult communicateTransportDocument(String documentId);

    AtCommunicationResult communicateSeries(String seriesId);

    AtCommunicationResult finalizeSeries(String seriesId);

    ValidationResult validateSaft(String saftExportId);
}
```

### 14.2 Serviço de emissão fiscal

```java
public interface FiscalDocumentIssuer {

    FiscalDocument issueDraft(String draftDocumentId);

    FiscalDocument cancelDocument(String documentId, String reason);

    FiscalDocument createCreditNote(String sourceDocumentId, CreditNoteRequest request);
}
```

### 14.3 Serviço de séries

```java
public interface DocumentSeriesService {

    DocumentSeries createSeries(CreateSeriesRequest request);

    DocumentSeries communicateSeries(String seriesId);

    DocumentSeries activateSeries(String seriesId);

    DocumentSeries closeSeries(String seriesId);

    String generateAtcud(DocumentSeries series, Long documentNumber);
}
```

---

## 15. Geração do SAF-T

A geração do SAF-T deve seguir um processo controlado.

### 15.1 Etapas

1. Selecionar período.
2. Validar dados da empresa.
3. Validar clientes.
4. Validar artigos e serviços.
5. Validar impostos.
6. Validar documentos emitidos.
7. Construir estrutura SAF-T.
8. Gerar XML.
9. Validar contra XSD.
10. Guardar ficheiro.
11. Registar exportação.
12. Disponibilizar download.
13. Registar utilizador e data da geração.

### 15.2 Validações prévias

Antes de gerar o ficheiro, o FAC deve validar:

* NIF da empresa;
* morada da empresa;
* país da empresa;
* NIF dos clientes;
* códigos de país;
* taxas de IVA;
* motivos de isenção;
* documentos sem linhas;
* documentos sem ATCUD;
* documentos sem hash;
* documentos anulados sem motivo;
* notas de crédito sem referência ao documento original;
* séries sem código de validação, quando aplicável.

---

## 16. Comunicação à AT

A comunicação à AT deve ser implementada como processo assíncrono e registado.

Nenhuma comunicação deve ocorrer sem registo de:

* pedido enviado;
* data e hora;
* utilizador ou processo;
* resposta recebida;
* código de resposta;
* mensagem de erro, quando exista;
* estado final.

### 16.1 Estados de comunicação

```text
PENDING
SENT
ACCEPTED
REJECTED
ERROR
RETRY_PENDING
CANCELLED
```

### 16.2 Reprocessamento

O sistema deve permitir reprocessar comunicações falhadas, mas nunca duplicar documentos fiscalmente emitidos.

O reprocessamento deve atuar apenas sobre a comunicação, não sobre a emissão fiscal.

---

## 17. Ambientes

O FAC deve distinguir claramente:

### 17.1 Ambiente de desenvolvimento

Usado para desenvolvimento interno.

Pode usar dados fictícios.

Não comunica com a AT real.

### 17.2 Ambiente de testes/homologação

Usado para validação técnica.

Deve permitir testar:

* geração SAF-T;
* validação XSD;
* comunicação de documentos;
* comunicação de séries;
* respostas de erro;
* credenciais de teste;
* cenários de certificação.

### 17.3 Ambiente de produção

Usado por empresas reais.

Deve exigir:

* credenciais válidas;
* séries comunicadas;
* validações rigorosas;
* logs completos;
* controlo de acessos;
* mecanismos de auditoria.

---

## 18. Credenciais AT

As credenciais de acesso à AT nunca devem ser guardadas em texto simples.

O FAC deve prever:

* encriptação das credenciais;
* separação por empresa;
* separação por ambiente;
* controlo de permissões;
* registo de alterações;
* ocultação visual de passwords;
* impossibilidade de exportação indevida.

Devem existir perfis próprios para:

* comunicação de faturas;
* comunicação de documentos de transporte;
* comunicação de séries;
* outros serviços que venham a ser necessários.

---

## 19. Testes Obrigatórios

A componente AT deve ter testes próprios.

### 19.1 Testes unitários

Devem validar:

* geração de ATCUD;
* numeração sequencial;
* bloqueio de documentos emitidos;
* cálculo de totais;
* cálculo de IVA;
* aplicação de isenções;
* geração de hash;
* geração de QR Code;
* mapeamento para SAF-T.

### 19.2 Testes de integração

Devem validar:

* geração de XML SAF-T;
* validação contra XSD;
* exportação de ficheiros;
* simulação de comunicação com AT;
* tratamento de respostas;
* reprocessamento de erros.

### 19.3 Testes de certificação interna

Devem existir cenários com:

* fatura normal;
* fatura simplificada;
* nota de crédito total;
* nota de crédito parcial;
* documento anulado;
* cliente nacional;
* cliente comunitário;
* cliente extracomunitário;
* IVA normal;
* IVA reduzido;
* IVA intermédio;
* isenção de IVA;
* arredondamentos;
* descontos;
* várias linhas;
* várias taxas de IVA no mesmo documento;
* documentos em séries diferentes;
* encerramento de série;
* tentativa de emissão sem série válida;
* tentativa de alteração de documento emitido.

---

## 20. Regras de Desenvolvimento

Nenhum programador deve implementar faturação fiscal diretamente no controlador ou no frontend.

As regras fiscais devem ficar concentradas em serviços próprios.

### 20.1 Proibido

É proibido:

* alterar documentos emitidos diretamente na base de dados;
* gerar números no frontend;
* gerar ATCUD no frontend;
* calcular totais apenas no frontend;
* permitir edição de valores fiscais após emissão;
* apagar documentos fiscalmente relevantes;
* reutilizar números;
* emitir documentos sem série ativa;
* emitir documentos sem validação fiscal prévia.

### 20.2 Obrigatório

É obrigatório:

* validar no backend;
* persistir logs;
* manter histórico;
* usar transações;
* bloquear concorrência na emissão;
* testar cenários fiscais;
* manter o modelo alinhado com o SAF-T;
* documentar alterações relevantes.

---

## 21. Segurança e Auditoria

O FAC deve manter auditoria sobre operações fiscalmente relevantes.

Devem ser registados:

* criação de documentos;
* emissão de documentos;
* anulação;
* geração de notas de crédito;
* comunicação à AT;
* geração de SAF-T;
* alteração de séries;
* comunicação de séries;
* alteração de credenciais;
* falhas de comunicação;
* reprocessamentos.

Cada registo deve incluir:

* utilizador;
* data e hora;
* operação;
* entidade afetada;
* valor anterior, quando aplicável;
* valor novo, quando aplicável;
* origem da operação.

---

## 22. Frontend

O frontend deve respeitar a lógica fiscal definida no backend.

### 22.1 Documentos em rascunho

Pode permitir:

* editar linhas;
* alterar cliente;
* alterar datas permitidas;
* alterar descontos;
* apagar rascunho;
* pré-visualizar documento.

### 22.2 Documentos emitidos

Deve permitir apenas:

* consultar;
* imprimir;
* exportar PDF;
* emitir nota de crédito;
* consultar comunicação AT;
* consultar hash;
* consultar ATCUD;
* consultar histórico.

Não deve permitir edição direta.

---

## 23. Alertas no Sistema

O FAC deve alertar o utilizador quando:

* não existir série ativa;
* a série não tiver código de validação;
* o cliente não tiver NIF válido;
* existir documento sem comunicação;
* existir falha de comunicação à AT;
* o SAF-T tiver erros de validação;
* existirem documentos emitidos fora do período esperado;
* existirem séries próximas de encerramento;
* existirem credenciais AT inválidas.

---

## 24. Ordem Recomendada de Implementação

A implementação deve seguir uma sequência prudente.

### Fase 1 - Modelo fiscal interno

* tipos de documento;
* séries;
* numeração;
* documentos;
* linhas;
* totais;
* estados;
* bloqueio após emissão.

### Fase 2 - ATCUD e QR Code

* comunicação manual de séries;
* registo do código de validação;
* geração de ATCUD;
* geração de QR Code;
* impressão/PDF.

### Fase 3 - SAF-T

* modelo SAF-T;
* mapeamento;
* geração XML;
* validação XSD;
* exportação;
* logs.

### Fase 4 - Testes fiscais

* cenários fiscais;
* documentos de teste;
* validação de totais;
* validação de isenções;
* validação de notas de crédito;
* validação de documentos anulados.

### Fase 5 - Comunicação AT

* credenciais;
* webservice;
* comunicação de documentos;
* comunicação de séries;
* logs;
* reprocessamento;
* ambiente de testes.

### Fase 6 - Preparação para certificação

* documentação técnica;
* evidência de testes;
* regras de imutabilidade;
* controlo de acessos;
* validação da cadeia de documentos;
* revisão legal e fiscal.

---

## 25. Decisão Arquitetural

A integração AT deve ser tratada como domínio próprio dentro do FAC.

Não deve ser uma coleção dispersa de métodos utilitários.

A estrutura recomendada é:

```text
domain
 ├── fiscal
 ├── documents
 ├── series
 ├── taxes
 └── saft

infrastructure
 ├── at
 ├── xml
 ├── qrcode
 └── security

application
 ├── issue-document
 ├── export-saft
 ├── communicate-at
 └── manage-series
```

---

## 26. Checklist de Conformidade Interna

Antes de considerar a componente AT como pronta, deve ser possível responder afirmativamente às seguintes perguntas:

* O sistema impede emissão sem série ativa?
* O sistema impede emissão sem código de validação da série, quando aplicável?
* O ATCUD é gerado corretamente?
* O número do documento é sequencial?
* O documento emitido fica bloqueado?
* O documento tem hash?
* O documento aparece no SAF-T?
* O SAF-T valida contra o XSD?
* As notas de crédito referenciam o documento original?
* Os documentos anulados mantêm registo?
* O sistema guarda logs de comunicação?
* As credenciais estão protegidas?
* Há testes para os principais cenários fiscais?
* O frontend respeita o estado fiscal do documento?
* O sistema diferencia rascunho de documento emitido?
* Há separação clara entre desenvolvimento, testes e produção?

---

## 27. Posição Oficial do Projeto FAC

O FAC deve ser desenvolvido como uma aplicação de faturação moderna, simples e robusta, mas com consciência fiscal desde a origem.

A ambição não é criar imediatamente um ERP completo.

A ambição é criar uma solução de faturação tecnicamente limpa, fiscalmente coerente e preparada para evoluir.

A integração SAF-T e AT não deve ser acrescentada no fim.

Deve estar no ADN do projeto.

---

## 28. Nota Final

Este documento passa a servir como referência basilar para todas as decisões relacionadas com:

* faturação;
* documentos fiscalmente relevantes;
* SAF-T;
* ATCUD;
* QR Code;
* comunicação com a AT;
* certificação futura.

Qualquer alteração funcional ou técnica que afete estas áreas deve ser confrontada com este documento antes de ser implementada.
