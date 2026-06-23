# 11 — Implementação de ATCUD, Código QR e Emissão Fiscal em PDF

**Projeto:** FAC
**Documento:** 11
**Versão:** 1.0
**Estado:** Proposto para implementação
**Âmbito:** Backend, frontend, base de dados, PDF, testes e validação fiscal
**Prioridade:** Crítica

---

## 1. Finalidade

O presente documento define a implementação do circuito técnico e funcional necessário para:

* associar documentos fiscalmente relevantes a séries documentais;
* controlar a numeração sequencial dos documentos;
* gerar e persistir o ATCUD;
* construir a mensagem fiscal destinada ao Código QR;
* gerar o Código QR;
* apresentar ATCUD e Código QR nos documentos em PDF;
* garantir a imutabilidade dos documentos emitidos;
* distinguir documentos em rascunho, emitidos e anulados;
* criar mecanismos de validação e auditoria;
* preparar o FAC para futura certificação como programa de faturação.

A implementação deverá respeitar a legislação e as especificações técnicas publicadas pela Autoridade Tributária e Aduaneira.

Este documento não declara nem pressupõe que o FAC já seja um programa certificado.

---

## 2. Objetivo funcional

No final da implementação, o FAC deverá permitir executar o seguinte circuito:

1. Criar ou editar um documento em estado de rascunho.
2. Selecionar uma série documental válida.
3. Validar os dados obrigatórios do documento.
4. Emitir definitivamente o documento.
5. Atribuir, de forma transacional:

    * número sequencial;
    * identificação completa do documento;
    * ATCUD;
    * data e hora de emissão;
    * estado fiscal;
    * dados necessários ao Código QR.
6. Gerar o Código QR.
7. Gerar o PDF fiscal.
8. Consultar, descarregar e imprimir o documento.
9. Impedir alterações ao conteúdo fiscal depois da emissão.
10. Permitir a anulação apenas através de operação própria, auditada e devidamente refletida no documento.

O circuito central será:

> Rascunho → validação → emissão definitiva → numeração → ATCUD → QR Code → PDF → consulta ou anulação.

---

## 3. Princípios obrigatórios

### 3.1. O backend é a autoridade fiscal

Toda a lógica relacionada com:

* numeração;
* ATCUD;
* estado fiscal;
* totais;
* impostos;
* mensagem do QR Code;
* emissão;
* anulação;
* imutabilidade;

deverá residir no backend.

O frontend não poderá calcular, atribuir ou alterar valores fiscais definitivos.

---

### 3.2. A emissão é uma operação indivisível

A emissão deverá decorrer numa única transação de base de dados.

Ou são concluídas todas as operações necessárias à emissão, ou nenhuma alteração é persistida.

Não poderá existir um documento que:

* tenha número mas não tenha ATCUD;
* tenha ATCUD mas não tenha sido emitido;
* tenha sido emitido sem totais consolidados;
* tenha mensagem QR incompleta;
* tenha consumido um número por falha parcial não controlada.

---

### 3.3. O PDF não contém lógica de negócio

O gerador de PDF não poderá recalcular:

* bases tributáveis;
* IVA;
* totais;
* descontos;
* retenções;
* ATCUD;
* número documental;
* campos do QR Code.

O PDF deverá limitar-se a representar um modelo de impressão fiscal, integralmente preenchido com dados previamente consolidados pelo backend.

---

### 3.4. Imutabilidade após emissão

Após a emissão, não poderão ser alterados, entre outros:

* cliente;
* identificação fiscal;
* moradas;
* linhas;
* quantidades;
* preços;
* descontos;
* taxas e códigos de IVA;
* motivos de isenção;
* moeda;
* câmbio;
* série;
* número;
* data fiscal;
* totais;
* ATCUD;
* mensagem QR;
* estado fiscal original.

As correções fiscalmente relevantes deverão ser efetuadas através dos documentos adequados, como notas de crédito, notas de débito ou outros documentos previstos no modelo funcional do FAC.

---

### 3.5. Dados históricos desacoplados dos mestres

O documento emitido deverá manter uma fotografia dos dados relevantes à data da emissão.

Uma alteração posterior à ficha do cliente, artigo, empresa, taxa de IVA ou outro mestre não poderá alterar o conteúdo de um documento já emitido.

---

## 4. Referências técnicas e legais

A implementação deverá considerar, pelo menos:

* Decreto-Lei n.º 28/2019, de 15 de fevereiro;
* Portaria n.º 195/2020, de 13 de agosto;
* especificações técnicas do Código QR publicadas pela AT;
* estrutura e nomenclatura SAF-T (PT) aplicável aos documentos comerciais;
* regras de certificação de programas de faturação;
* documentação técnica da AT relativa à comunicação de séries e ATCUD.

Sempre que existir divergência entre este documento e uma norma ou especificação oficial atualizada, prevalece a fonte oficial.

---

## 5. Âmbito desta implementação

### 5.1. Incluído

Esta implementação abrange:

* utilização de séries documentais já existentes no FAC;
* armazenamento do código de validação atribuído pela AT;
* validação da aptidão fiscal da série;
* numeração sequencial e concorrente;
* geração do ATCUD;
* construção da mensagem QR;
* geração da imagem QR;
* persistência dos dados fiscais consolidados;
* emissão definitiva;
* anulação;
* PDF fiscal;
* testes unitários, de integração e concorrência;
* validações no frontend;
* registo de auditoria;
* documentação técnica.

---

### 5.2. Não incluído

Ficam fora deste documento:

* comunicação automática de séries através de webservice da AT;
* obtenção automática do código de validação;
* envio do SAF-T;
* comunicação de faturas à AT;
* comunicação de documentos de transporte;
* pedido formal de certificação do FAC;
* assinatura digital do PDF;
* envio automático do documento por correio eletrónico;
* arquivo legal certificado;
* implementação completa de todos os tipos documentais;
* substituição do documento comercial por um simples PDF editável.

A arquitetura deverá, contudo, permitir a implementação futura destas funcionalidades sem reconstrução do núcleo documental.

---

## 6. Estados do documento

Deverá existir um estado documental explícito.

Sugestão:

```java
public enum EstadoDocumento {
    RASCUNHO,
    EMITIDO,
    ANULADO
}
```

Poderão existir estados operacionais adicionais, desde que não confundam o estado fiscal.

Por exemplo, o pagamento não deve ser representado através do estado fiscal do documento:

```java
public enum EstadoPagamento {
    NAO_APLICAVEL,
    NAO_PAGO,
    PARCIALMENTE_PAGO,
    PAGO
}
```

### 6.1. RASCUNHO

Um documento em rascunho:

* ainda não possui número fiscal definitivo;
* ainda não possui ATCUD definitivo;
* não produz um PDF fiscal definitivo;
* pode ser alterado;
* pode ser eliminado segundo as regras funcionais da aplicação;
* não consome numeração da série.

Uma impressão de rascunho, caso seja permitida, deverá apresentar uma marca visual inequívoca:

> RASCUNHO — DOCUMENTO SEM VALOR FISCAL

---

### 6.2. EMITIDO

Um documento emitido:

* possui número definitivo;
* possui série;
* possui identificação documental completa;
* possui ATCUD;
* possui data e hora de emissão;
* possui totais consolidados;
* possui mensagem QR persistida;
* pode gerar PDF fiscal;
* não pode ser editado;
* não pode ser eliminado.

---

### 6.3. ANULADO

Um documento anulado:

* mantém o número original;
* mantém o ATCUD original;
* mantém os dados históricos;
* mantém a mensagem QR correspondente ao seu estado fiscal;
* não é eliminado;
* apresenta o estado de anulação de forma visível;
* regista o motivo, utilizador, data e hora da anulação;
* permanece disponível para auditoria, listagens e SAF-T.

A anulação não deverá libertar nem reutilizar o número atribuído.

---

## 7. Modelo da série documental

A entidade de série documental deverá ser revista e complementada, sem duplicar campos funcionalmente equivalentes já existentes.

Deverá comportar, pelo menos:

```text
id
empresaId
tipoDocumentoId
codigoSerie
descricao
anoFiscal, se aplicável
numeroInicial
proximoNumero
ultimoNumeroEmitido
codigoValidacaoAT
dataComunicacaoAT
estadoSerie
ativa
dataInicioUtilizacao
dataFimUtilizacao
versao
createdAt
updatedAt
```

Sugestão de estado:

```java
public enum EstadoSerieDocumental {
    RASCUNHO,
    AGUARDA_COMUNICACAO,
    ATIVA,
    ENCERRADA,
    BLOQUEADA
}
```

### 7.1. Série apta para emissão

Uma série só poderá ser utilizada para emissão quando:

* pertence à empresa emissora;
* corresponde ao tipo de documento;
* está ativa;
* está dentro do respetivo período de utilização;
* possui código de validação da AT válido, quando legalmente exigível;
* não está encerrada ou bloqueada;
* possui configuração de numeração válida;
* não existe incompatibilidade com o ano ou exercício, quando aplicável.

---

### 7.2. Código de validação da série

Na primeira fase, o código de validação da AT poderá ser introduzido manualmente por um utilizador autorizado.

O sistema deverá:

* remover espaços indevidos;
* rejeitar valores vazios;
* validar o comprimento mínimo legal;
* validar o conjunto de caracteres permitido;
* impedir alteração depois de existirem documentos emitidos;
* solicitar confirmação explícita antes de ativar a série;
* registar a operação na auditoria;
* apresentar parcialmente mascarado em listagens gerais, quando adequado;
* apresentar integralmente apenas em áreas autorizadas.

A aplicação deverá alertar que a introdução manual não comprova que o código tenha sido efetivamente atribuído pela AT.

---

## 8. Numeração documental

### 8.1. Regra geral

Cada combinação fiscalmente autónoma deverá possuir a sua própria sequência.

A sequência deverá considerar, conforme o modelo existente:

* empresa;
* tipo de documento;
* série.

A numeração deverá ser:

* sequencial;
* crescente;
* única;
* não reutilizável;
* resistente a emissões concorrentes.

---

### 8.2. Operação concorrente

A atribuição do número deverá utilizar uma estratégia segura de concorrência.

São admissíveis, mediante adequação à arquitetura existente:

* bloqueio pessimista da série;
* atualização atómica com controlo de versão;
* sequência específica da base de dados;
* tabela própria de contadores com bloqueio transacional.

Não é aceitável:

```java
long numero = serie.getUltimoNumero() + 1;
```

sem bloqueio ou controlo concorrencial.

Duas emissões simultâneas nunca poderão receber o mesmo número.

---

### 8.3. Formato do número

A sequência numérica deverá ser armazenada como valor numérico.

A identificação fiscal completa deverá ser construída de forma determinística, segundo o tipo de documento, série e número.

Exemplo conceptual:

```text
FT FAC2026/123
```

A implementação deverá respeitar a convenção adotada pelo projeto e a correspondência futura com os campos do SAF-T.

Não deverão ser aplicados zeros à esquerda ao número fiscal sem decisão funcional expressa e validação da compatibilidade com as regras aplicáveis.

---

### 8.4. Persistência dos componentes

O documento deverá guardar separadamente:

```text
tipoDocumentoFiscal
codigoSerie
numeroSequencial
numeroDocumentoCompleto
codigoValidacaoSerie
atcud
```

Não se deverá depender da interpretação posterior de uma única string para recuperar estes elementos.

---

## 9. Geração do ATCUD

O ATCUD deverá ser calculado exclusivamente no backend.

Formato:

```text
codigoValidacaoSerie-numeroSequencial
```

Exemplo:

```text
ABCD1234-125
```

A menção apresentada no documento será:

```text
ATCUD: ABCD1234-125
```

### 9.1. Componente próprio

Criar um componente dedicado, por exemplo:

```java
@Component
public class AtcudGenerator {

    public String generate(
            String codigoValidacaoSerie,
            long numeroSequencial
    ) {
        // validação e construção
    }
}
```

O componente deverá:

* rejeitar código de validação inexistente;
* rejeitar número inferior ao mínimo permitido;
* normalizar o código;
* gerar sempre o mesmo resultado para os mesmos argumentos;
* não consultar diretamente repositórios;
* não conhecer regras de PDF;
* ser coberto por testes unitários.

---

### 9.2. Persistência

O ATCUD deverá ser persistido no momento da emissão.

Não deverá ser reconstruído dinamicamente em cada consulta ou impressão.

Esta opção assegura:

* estabilidade histórica;
* auditoria;
* coerência entre PDF, QR Code e SAF-T;
* proteção contra alterações posteriores na série.

---

## 10. Modelo fiscal consolidado do documento

O documento emitido deverá possuir os dados necessários à sua reprodução histórica.

Sem impor uma duplicação desordenada do domínio, deverá ser garantida a persistência dos seguintes grupos:

### 10.1. Emitente

* nome ou denominação;
* NIF;
* morada;
* código postal;
* localidade;
* país;
* capital social, quando aplicável;
* matrícula e conservatória, quando aplicável;
* contactos apresentados no documento.

### 10.2. Adquirente

* nome ou denominação;
* NIF;
* país fiscal;
* morada;
* código postal;
* localidade.

### 10.3. Documento

* tipo fiscal;
* série;
* número;
* identificação completa;
* ATCUD;
* data do documento;
* data e hora de emissão;
* moeda;
* taxa de câmbio, quando aplicável;
* estado;
* motivo de anulação, quando aplicável.

### 10.4. Linhas

* código do artigo ou serviço;
* descrição;
* quantidade;
* unidade;
* preço unitário;
* desconto;
* base tributável;
* taxa de IVA;
* código fiscal;
* motivo de isenção;
* valor de imposto;
* total da linha.

### 10.5. Totais

* total bruto;
* total de descontos;
* bases por espaço fiscal e taxa;
* total de IVA por taxa;
* total de impostos;
* retenções, quando aplicável;
* total líquido;
* total a pagar.

---

## 11. Construção da mensagem do Código QR

### 11.1. Componente fiscal próprio

Deverá ser criado um componente exclusivamente responsável pela construção da mensagem fiscal do QR Code.

Exemplo:

```java
public interface FiscalQrPayloadBuilder {

    FiscalQrPayload build(DocumentoFiscalSnapshot documento);
}
```

O resultado deverá distinguir:

```java
public record FiscalQrPayload(
        String canonicalPayload,
        Map<String, String> fields
) {}
```

A mensagem canónica será a única utilizada para gerar o QR Code.

---

### 11.2. Responsabilidades

O construtor deverá:

* selecionar os campos aplicáveis ao tipo de documento;
* ordenar os campos segundo a especificação da AT;
* usar os identificadores oficiais;
* aplicar os separadores exigidos;
* formatar datas;
* formatar valores monetários;
* agregar bases e impostos por taxa e espaço fiscal;
* preencher o estado documental;
* incluir a identificação completa do documento;
* incluir o ATCUD;
* incluir os caracteres aplicáveis do hash;
* incluir o número do certificado, quando aplicável;
* rejeitar documentos inconsistentes;
* produzir mensagens determinísticas.

---

### 11.3. Separadores e formato

A mensagem deverá utilizar a estrutura exigida pela AT, com campos identificados e separados por `*`.

Exemplo meramente ilustrativo:

```text
A:123456789*B:987654321*C:PT*D:FT*E:N*F:20260622*G:FT FAC2026/125*H:ABCD1234-125*...
```

O exemplo não deverá ser copiado como implementação fixa.

Os campos aplicáveis deverão resultar das especificações técnicas oficiais e dos dados concretos do documento.

---

### 11.4. Datas

As datas incluídas na mensagem QR deverão utilizar o formato previsto pela especificação da AT.

A formatação deverá ser centralizada e testada.

Não deverá depender do locale do servidor ou do navegador.

---

### 11.5. Valores monetários

Os valores deverão:

* utilizar `BigDecimal`;
* ser arredondados segundo as regras definidas no domínio;
* apresentar o número de casas decimais exigido;
* utilizar ponto como separador decimal na mensagem QR;
* não utilizar separador de milhares;
* nunca ser formatados através de `double` ou `float`.

Exemplo:

```text
1250.00
```

---

### 11.6. Campos condicionais

Os campos relativos a bases tributáveis e impostos deverão ser incluídos conforme:

* espaço fiscal;
* taxa reduzida;
* taxa intermédia;
* taxa normal;
* operações isentas;
* outras taxas;
* imposto do selo;
* regimes especiais;
* tipologia do documento.

Não se deverá produzir um conjunto fixo e indiferenciado de campos.

---

### 11.7. Hash e número do certificado

A especificação do QR Code prevê informação relacionada com:

* caracteres do hash do documento;
* número do certificado do programa.

Estes valores deverão provir do mecanismo fiscal e de certificação do FAC.

Enquanto o projeto não possuir esse mecanismo integralmente implementado e validado:

* não inventar um hash;
* não inventar um número de certificado;
* não apresentar o FAC como certificado;
* não permitir ativar produção fiscal através de valores fictícios;
* utilizar valores de teste apenas em perfis de desenvolvimento ou demonstração claramente identificados;
* manter uma barreira explícita entre modo de desenvolvimento e modo de produção fiscal.

A implementação deverá criar uma abstração, por exemplo:

```java
public interface FiscalCertificationProvider {

    FiscalCertificationData obtainFor(DocumentoFiscalSnapshot documento);
}
```

Esta abstração permitirá integrar posteriormente o algoritmo e os dados definitivos exigidos para certificação.

---

### 11.8. Persistência da mensagem

O documento deverá guardar:

```text
qrPayload
qrSpecificationVersion
qrGeneratedAt
```

A imagem binária do QR Code poderá ser gerada no momento do PDF a partir da mensagem persistida.

Como alternativa, poderá existir cache da imagem, desde que a mensagem canónica permaneça a fonte oficial.

---

## 12. Geração da imagem QR

Deverá ser utilizada uma biblioteca Java madura e mantida, preferencialmente ZXing ou equivalente tecnicamente justificado.

Criar um serviço isolado:

```java
public interface QrCodeImageGenerator {

    byte[] generatePng(String payload, QrCodeOptions options);
}
```

O serviço deverá permitir configurar:

* largura;
* altura;
* margem;
* correção de erro;
* formato de saída;
* codificação de caracteres.

A configuração concreta deverá respeitar as especificações técnicas da AT e assegurar perfeita legibilidade após:

* geração do PDF;
* visualização;
* impressão;
* digitalização;
* eventual redimensionamento permitido.

Não deverá ser aplicado:

* logótipo sobre o QR Code;
* fundo transparente problemático;
* decoração;
* moldura invasiva;
* compressão com perda;
* alteração estética dos módulos;
* cores de baixo contraste.

---

## 13. Emissão definitiva

### 13.1. Endpoint próprio

A emissão não deverá resultar de um simples `PUT` de atualização.

Criar uma operação explícita, por exemplo:

```http
POST /documentos-comerciais/{id}/emitir
```

ou uma designação equivalente coerente com a API existente.

---

### 13.2. Serviço de emissão

Criar um serviço transacional dedicado:

```java
@Service
public class EmissaoDocumentoService {

    @Transactional
    public DocumentoEmitidoDto emitir(Long documentoId) {
        // circuito completo
    }
}
```

Responsabilidades:

1. carregar o documento;
2. bloquear ou controlar concorrência;
3. confirmar que está em rascunho;
4. validar a empresa;
5. validar o cliente;
6. validar as linhas;
7. validar a série;
8. recalcular ou confirmar totais através do serviço oficial;
9. obter o próximo número;
10. construir a identificação documental;
11. gerar o ATCUD;
12. criar o snapshot fiscal;
13. obter os dados de certificação aplicáveis;
14. construir a mensagem QR;
15. persistir todos os dados;
16. alterar o estado para emitido;
17. registar auditoria;
18. confirmar a transação;
19. devolver o DTO do documento emitido.

---

### 13.3. Idempotência

Uma segunda chamada de emissão sobre um documento já emitido:

* não poderá atribuir novo número;
* não poderá consumir numeração;
* não poderá gerar um ATCUD diferente;
* não poderá alterar dados fiscais.

A API deverá devolver:

* o documento já emitido, quando a operação puder ser tratada idempotentemente; ou
* conflito de estado `409 Conflict`.

A opção deverá ser consistente e testada.

---

## 14. Validações prévias à emissão

A emissão deverá falhar quando, entre outras situações:

* o documento não existe;
* o documento não está em rascunho;
* a empresa não está configurada;
* a empresa não possui NIF válido;
* o cliente obrigatório não está identificado;
* o NIF do cliente é inválido, quando exigível;
* não existem linhas;
* existem quantidades inválidas;
* existem preços inválidos;
* falta uma taxa ou código fiscal;
* existe isenção sem motivo válido;
* existe incompatibilidade entre imposto e motivo de isenção;
* os totais são inconsistentes;
* a série não pertence à empresa;
* a série não corresponde ao tipo documental;
* a série não está ativa;
* falta o código de validação da AT;
* a sequência está inconsistente;
* existe duplicação de número;
* falta informação necessária ao QR Code;
* o modo de produção exige dados de certificação ainda indisponíveis.

Os erros deverão ser devolvidos através do mecanismo uniforme de tratamento de exceções da aplicação.

---

## 15. Anulação

### 15.1. Endpoint próprio

Criar uma operação explícita:

```http
POST /documentos-comerciais/{id}/anular
```

Pedido sugerido:

```json
{
  "motivo": "Descrição obrigatória do motivo da anulação"
}
```

---

### 15.2. Regras

A anulação deverá:

* estar limitada a utilizadores autorizados;
* exigir motivo não vazio;
* registar data e hora;
* registar o utilizador;
* manter número e ATCUD;
* atualizar o estado fiscal;
* atualizar os dados necessários à representação e exportação fiscal;
* impedir nova anulação;
* impedir eliminação;
* impedir edição posterior;
* gerar nova versão visual do PDF com indicação inequívoca de anulado.

Deverá ser confirmada a estratégia relativa à mensagem QR de documentos anulados segundo as especificações oficiais e a futura integração SAF-T.

---

### 15.3. Auditoria

Guardar, pelo menos:

```text
documentoId
estadoAnterior
estadoNovo
motivo
utilizadorId
dataHora
origem
```

---

## 16. PDF fiscal

### 16.1. Endpoint

Disponibilizar um endpoint semelhante a:

```http
GET /documentos-comerciais/{id}/pdf
```

O endpoint deverá:

* aceitar apenas documentos emitidos ou anulados;
* rejeitar rascunhos como documentos fiscais;
* devolver `application/pdf`;
* sugerir nome de ficheiro coerente;
* respeitar permissões e empresa do utilizador.

Poderá existir endpoint distinto para pré-visualização de rascunho, desde que o resultado esteja claramente marcado como não fiscal.

---

### 16.2. Conteúdo mínimo

O PDF deverá apresentar, conforme aplicável:

* logótipo;
* dados completos do emitente;
* dados do adquirente;
* tipo e designação do documento;
* número completo;
* data;
* vencimento;
* linhas;
* quantidades;
* preços;
* descontos;
* taxas;
* bases;
* impostos;
* totais;
* moeda;
* condições de pagamento;
* observações;
* motivos de isenção;
* informação legal aplicável;
* ATCUD;
* QR Code;
* estado de anulação;
* número de página;
* indicação de página e total de páginas.

---

### 16.3. ATCUD no PDF

O ATCUD deverá:

* ser perfeitamente legível;
* ser apresentado com a menção `ATCUD:`;
* constar em todas as páginas;
* ficar imediatamente acima do QR Code na página onde este for apresentado;
* não ser truncado;
* não ser escondido em rodapé ilegível;
* permanecer visível em impressão A4.

---

### 16.4. QR Code no PDF

O QR Code deverá:

* constar na primeira ou na última página;
* estar dentro do corpo do documento;
* manter dimensões e contraste adequados;
* não ser dividido entre páginas;
* não ficar sobreposto por texto;
* ser testado com leitores comuns;
* corresponder exatamente à mensagem persistida.

Por uma questão de estabilidade de layout, recomenda-se a sua colocação na última página, numa área fiscal reservada, salvo melhor decisão resultante do desenho final do relatório.

---

### 16.5. Documentos com várias páginas

O motor de PDF deverá assegurar:

* cabeçalho repetido de forma controlada;
* identificação do documento em todas as páginas;
* ATCUD em todas as páginas;
* totais apenas na secção final;
* linhas não cortadas de forma ilegível;
* rodapé previsível;
* QR Code numa única página;
* numeração de páginas;
* ausência de páginas quase vazias por erro de layout;
* impressão A4 sem necessidade de ajustes manuais.

---

### 16.6. Documento anulado

O PDF de um documento anulado deverá apresentar:

* estado `ANULADO`;
* marca visual clara;
* motivo da anulação, quando apropriado;
* dados originais;
* número original;
* ATCUD original;
* data de anulação;
* ausência de qualquer aparência que permita confundi-lo com documento ativo.

---

## 17. Arquitetura sugerida

A nomenclatura deverá ser ajustada ao projeto existente.

Estrutura conceptual:

```text
documentos/
├── controller/
│   ├── DocumentoComercialController
│   └── DocumentoPdfController
├── service/
│   ├── DocumentoComercialService
│   ├── EmissaoDocumentoService
│   ├── AnulacaoDocumentoService
│   ├── NumeracaoDocumentoService
│   └── DocumentoPdfService
├── fiscal/
│   ├── AtcudGenerator
│   ├── FiscalQrPayloadBuilder
│   ├── QrCodeImageGenerator
│   ├── FiscalCertificationProvider
│   ├── DocumentoFiscalSnapshotFactory
│   └── FiscalDocumentValidator
├── dto/
│   ├── EmitirDocumentoResponse
│   ├── AnularDocumentoRequest
│   ├── DocumentoEmitidoDto
│   └── DocumentoFiscalPrintDto
├── repository/
├── domain/
└── exception/
```

Evitar:

* lógica fiscal no controller;
* acesso direto a repositórios pelo gerador de PDF;
* geração do ATCUD no mapper;
* construção da mensagem QR no template;
* duplicação de cálculos entre frontend e backend;
* entidades artificiais criadas apenas para facilitar a impressão.

DTOs de impressão e projeções de leitura são aceitáveis. Novas entidades persistentes só deverão ser criadas quando representem conceitos reais do domínio ou requisitos claros de auditoria e histórico.

---

## 18. Alterações de base de dados

A implementação deverá analisar o modelo existente antes de criar migrações.

Campos conceptualmente necessários no documento:

```text
estadoDocumento
numeroSequencial
numeroDocumentoCompleto
codigoSerieSnapshot
codigoValidacaoSerieSnapshot
atcud
dataHoraEmissao
dataHoraAnulacao
motivoAnulacao
anuladoPor
qrPayload
qrSpecificationVersion
qrGeneratedAt
fiscalSnapshotVersion
version
```

Na série:

```text
proximoNumero
ultimoNumeroEmitido
codigoValidacaoAT
dataComunicacaoAT
estadoSerie
version
```

Criar índices e restrições, nomeadamente:

```text
UNIQUE (empresa_id, tipo_documento_id, serie_id, numero_sequencial)
UNIQUE (empresa_id, numero_documento_completo)
```

A segunda restrição deverá ser adaptada caso o formato permita legitimamente identificações repetidas entre universos documentais distintos.

Deverão existir restrições de nulidade condicionadas ao estado, asseguradas através da aplicação e, quando tecnicamente adequado, da base de dados.

---

## 19. Frontend

### 19.1. Ecrã do documento

Em rascunho:

* permitir edição;
* permitir seleção de série;
* apresentar validações;
* permitir guardar;
* disponibilizar botão `Emitir documento`;
* apresentar confirmação antes da emissão.

Após emissão:

* colocar o formulário em modo de consulta;
* ocultar ações de edição;
* mostrar número;
* mostrar ATCUD;
* mostrar estado;
* disponibilizar PDF;
* disponibilizar impressão;
* disponibilizar anulação apenas a utilizadores autorizados.

---

### 19.2. Confirmação de emissão

Antes da emissão, apresentar mensagem clara:

> A emissão atribui um número fiscal definitivo e torna o documento inalterável. Confirme que os dados estão corretos.

A confirmação não substitui as validações no backend.

---

### 19.3. Erros

O frontend deverá distinguir:

* erro de validação;
* série inválida;
* conflito concorrente;
* documento já emitido;
* ausência de configuração fiscal;
* falha de geração do PDF;
* falha técnica inesperada.

Não apresentar ao utilizador mensagens internas do Java, SQL ou stack traces.

---

## 20. Segurança e permissões

Mesmo que a camada completa de segurança seja desenvolvida noutro documento, esta implementação deverá preparar permissões distintas:

```text
DOCUMENTO_CRIAR
DOCUMENTO_EDITAR_RASCUNHO
DOCUMENTO_EMITIR
DOCUMENTO_ANULAR
DOCUMENTO_CONSULTAR
DOCUMENTO_OBTER_PDF
SERIE_GERIR
SERIE_CONFIGURAR_AT
```

O backend deverá validar sempre:

* utilizador;
* empresa;
* permissão;
* pertença do documento à empresa;
* pertença da série à empresa.

Não confiar em identificadores enviados pelo frontend.

---

## 21. Auditoria

Registar, pelo menos:

* criação do rascunho;
* alterações ao rascunho;
* emissão;
* número atribuído;
* ATCUD gerado;
* geração do PDF;
* anulação;
* alteração do código de validação da série;
* ativação ou encerramento da série;
* erros críticos de emissão.

O registo deverá incluir:

```text
tipoEvento
entidade
entidadeId
empresaId
utilizadorId
dataHora
dadosEssenciais
resultado
```

Não guardar informação sensível ou documentos completos indiscriminadamente nos logs técnicos.

---

## 22. Modo de desenvolvimento e modo fiscal

A aplicação deverá distinguir explicitamente:

```text
DEVELOPMENT
DEMO
PRODUCTION
```

Em `DEVELOPMENT` e `DEMO` poderão ser utilizados dados de teste, desde que:

* sejam claramente identificados;
* não sejam confundíveis com certificação;
* não sejam ativados silenciosamente em produção;
* não permitam emitir documentos apresentados como fiscalmente válidos.

Em `PRODUCTION`, a emissão deverá ser bloqueada quando não estiverem satisfeitos os requisitos fiscais configurados.

Sugestão:

```yaml
fac:
  fiscal:
    mode: DEVELOPMENT
    production-enabled: false
```

A configuração de produção não poderá depender apenas de uma opção manipulável no frontend.

---

## 23. Testes obrigatórios

### 23.1. ATCUD

Testar:

* geração normal;
* normalização;
* ausência de código;
* número inválido;
* estabilidade do resultado;
* não alteração posterior.

---

### 23.2. Numeração

Testar:

* primeiro documento;
* sequência normal;
* séries diferentes;
* tipos diferentes;
* empresas diferentes;
* série encerrada;
* série sem código AT;
* emissão concorrente;
* rollback;
* tentativa de segunda emissão;
* não reutilização após anulação.

Deverá existir teste de concorrência real com múltiplas transações.

---

### 23.3. QR Code

Testar:

* ordem dos campos;
* separadores;
* datas;
* valores monetários;
* campos obrigatórios;
* campos condicionais;
* operações isentas;
* várias taxas de IVA;
* arredondamentos;
* cliente sem NIF, quando admissível;
* documento anulado;
* payload determinístico;
* geração e leitura da imagem produzida.

Sempre que possível, o teste deverá gerar o QR Code e voltar a descodificá-lo, confirmando que o conteúdo é exatamente igual ao payload original.

---

### 23.4. Emissão

Testar:

* emissão válida;
* documento inexistente;
* documento sem linhas;
* totais inválidos;
* série incompatível;
* código AT inexistente;
* emissão repetida;
* falha a meio da transação;
* documento imutável após emissão;
* auditoria.

---

### 23.5. Anulação

Testar:

* anulação válida;
* motivo vazio;
* documento em rascunho;
* documento já anulado;
* utilizador sem permissão;
* manutenção do número;
* manutenção do ATCUD;
* indisponibilidade de edição.

---

### 23.6. PDF

Testar:

* documento de uma página;
* documento de várias páginas;
* muitas linhas;
* descrições longas;
* valores elevados;
* várias taxas;
* isenções;
* ATCUD em todas as páginas;
* QR Code apenas na posição definida;
* PDF de documento anulado;
* leitura do QR Code extraído ou renderizado do PDF.

---

## 24. Critérios de aceitação

A implementação será considerada concluída quando:

1. Um rascunho não consumir numeração.
2. A emissão atribuir número numa transação segura.
3. Duas emissões concorrentes nunca gerarem duplicados.
4. O ATCUD ser gerado corretamente e persistido.
5. O documento emitido ficar funcionalmente imutável.
6. A mensagem QR ser construída segundo a especificação oficial.
7. O QR Code gerado poder ser lido e reproduzir exatamente a mensagem.
8. O ATCUD constar em todas as páginas do PDF.
9. O QR Code constar na página definida e ser legível.
10. O PDF não recalcular dados fiscais.
11. A anulação não eliminar nem renumerar o documento.
12. Todas as operações críticas ficarem auditadas.
13. Os testes automatizados cobrirem os principais cenários.
14. O modo de demonstração não ser confundido com produção fiscal.
15. O projeto continuar a compilar e os testes existentes continuarem a passar.
16. Não serem introduzidas entidades persistentes sem necessidade de domínio.
17. Não ser apresentada qualquer alegação de certificação do FAC.

---

## 25. Estratégia de implementação

O Codex deverá:

1. analisar primeiro o modelo atual de documentos e séries;
2. identificar funcionalidades já implementadas;
3. reutilizar serviços, DTOs, enums e regras existentes;
4. evitar duplicações;
5. apresentar um resumo das alterações necessárias;
6. implementar por blocos coerentes;
7. criar ou atualizar migrações;
8. executar testes;
9. corrigir regressões;
10. documentar as decisões tomadas.

A implementação deverá privilegiar alterações incrementais e compatíveis com a arquitetura existente.

Não deverá reconstruir integralmente o módulo documental sem necessidade demonstrada.

---

## 26. Resultado esperado

Após este documento, o FAC deverá possuir um circuito documental tecnicamente coerente e demonstrável:

```text
Rascunho
   ↓
Validação fiscal
   ↓
Bloqueio transacional da série
   ↓
Atribuição do número
   ↓
Geração do ATCUD
   ↓
Consolidação fiscal
   ↓
Construção da mensagem QR
   ↓
Persistência
   ↓
Emissão definitiva
   ↓
PDF fiscal
   ↓
Consulta, impressão ou anulação
```

Este circuito constituirá a base para:

* certificação futura;
* SAF-T;
* comunicação à AT;
* documentos de transporte;
* reporting fiscal;
* cenário de demonstração;
* operação comercial real.

---

## 27. Nota de prudência

A presença visual de ATCUD e Código QR num PDF não transforma, por si só, o FAC num programa certificado nem torna automaticamente o documento fiscalmente conforme.

A conformidade depende do conjunto integral das regras aplicáveis, incluindo:

* certificação do programa, quando exigível;
* assinatura ou encadeamento dos documentos;
* regras de integridade;
* exportação SAF-T;
* comunicação de séries;
* correta configuração do emitente;
* regras dos tipos documentais;
* manutenção de registos;
* controlo de acesso;
* auditoria;
* demais obrigações legais e técnicas.

O FAC deverá assumir esta distinção em toda a sua arquitetura e comunicação.
