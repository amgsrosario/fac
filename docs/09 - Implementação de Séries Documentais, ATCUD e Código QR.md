# 09 - Implementação de Séries Documentais, ATCUD e Código QR

**Projeto:** FAC
**Documento:** Implementação de Séries Documentais, ATCUD e Código QR
**Versão:** 1.0
**Estado:** Planeado — implementação adiada
**Prioridade:** Média
**Momento de implementação:** Antes da certificação e entrada em produção
**Data:** 15 de junho de 2026
**Autor:** António Rosário

---

## 1. Objetivo

> Este documento define a arquitetura futura das séries documentais, do ATCUD e do Código QR.
> A implementação não integra o ciclo atual de desenvolvimento do FAC.
> O tema deverá ser retomado após a estabilização da emissão documental, dos cálculos fiscais, do reporting e do SAF-T.

* séries documentais;
* numeração sequencial dos documentos;
* comunicação das séries à Autoridade Tributária e Aduaneira;
* código de validação atribuído pela AT;
* ATCUD;
* Código QR;
* regras de emissão definitiva;
* anulação e reimpressão de documentos;
* integração futura com SAF-T;
* integração futura com os serviços eletrónicos da AT.

O objetivo é garantir que os documentos fiscalmente relevantes são emitidos de forma:

* legalmente conforme;
* sequencial;
* auditável;
* tecnicamente segura;
* resistente a concorrência;
* coerente com o SAF-T;
* independente do mecanismo utilizado para gerar o PDF ou imprimir o documento.

O ATCUD e o Código QR não devem ser tratados como simples elementos gráficos acrescentados ao documento no momento da impressão.

Devem fazer parte do processo fiscal de emissão e ficar associados de forma permanente ao documento.

---

## 2. Princípio basilar

Um documento fiscal só pode ser considerado emitido quando tiver sido concluído, com sucesso, o processo de emissão definitiva.

A emissão definitiva deve compreender, numa única operação lógica:

1. validação do documento;
2. validação da série documental;
3. atribuição do número sequencial;
4. construção da identificação completa do documento;
5. geração do ATCUD;
6. consolidação dos valores fiscais;
7. construção do conteúdo do Código QR;
8. validação do conteúdo fiscal;
9. gravação definitiva do documento;
10. bloqueio dos elementos fiscalmente imutáveis.

O PDF, a impressão e a exportação são representações posteriores de um documento fiscal já emitido.

Não devem ser responsáveis por gerar:

* o número do documento;
* o ATCUD;
* os valores fiscais;
* o conteúdo do Código QR.

---

## 3. Âmbito

Esta implementação deve abranger os documentos fiscalmente relevantes suportados pelo FAC, nomeadamente:

* faturas;
* faturas simplificadas;
* faturas-recibo;
* notas de crédito;
* notas de débito;
* recibos;
* outros documentos que venham a ser incluídos no âmbito funcional da aplicação.

Cada tipo documental deve estar associado ao respetivo código normalizado utilizado no SAF-T.

Exemplos:

| Documento           | Código |
| ------------------- | -----: |
| Fatura              |     FT |
| Fatura simplificada |     FS |
| Fatura-recibo       |     FR |
| Nota de crédito     |     NC |
| Nota de débito      |     ND |
| Recibo              |     RC |

A lista definitiva de tipos documentais deve ser centralizada e não deve depender de valores textuais dispersos pelo código.

---

## 4. Conceito de série documental

Uma série documental representa uma sequência autónoma de numeração associada a:

* uma entidade emitente;
* um tipo documental;
* um código de série;
* um ambiente;
* um código de validação atribuído pela AT.

A mesma designação de série pode ser utilizada em diferentes tipos documentais, mas cada combinação deve ser tratada de forma independente.

Exemplo:

| Série | Tipo documental | Código AT |
| ----- | --------------- | --------- |
| 2026  | FT              | ABCD1234  |
| 2026  | NC              | EFGH5678  |
| 2026  | RC              | IJKL9012  |

O código de validação da AT está associado à combinação entre a série e o tipo documental.

Por esse motivo, a série não deve ser modelada apenas como um texto existente no documento.

Deve existir como entidade funcional própria.

---

## 5. Modelo de dados da série

Deve ser criada uma entidade própria, designada, por exemplo:

```text
SerieDocumental
```

### 5.1. Campos propostos

```text
id
empresaId
codigoSerie
descricao
tipoDocumentoSaft
numeroInicial
ultimoNumeroEmitido
proximoNumero
codigoValidacaoAt
estado
origemCodigoValidacao
ambiente
dataPrevistaInicio
dataComunicacaoAt
dataPrimeiraUtilizacao
dataFinalizacao
version
createdAt
updatedAt
```

### 5.2. Descrição dos campos

#### `empresaId`

Identifica a entidade emitente à qual a série pertence.

A série nunca deve ser partilhada entre entidades emitentes distintas.

#### `codigoSerie`

Identificador funcional da série.

Exemplos:

```text
2026
A
ALG26
MILFONTES26
FT2026
```

#### `descricao`

Descrição interna para facilitar a identificação pelo utilizador.

Exemplo:

```text
Faturas gerais de 2026
```

#### `tipoDocumentoSaft`

Código normalizado do tipo documental.

Exemplo:

```text
FT
NC
RC
```

#### `numeroInicial`

Primeiro número previsto para utilização na série.

Na maioria das situações será:

```text
1
```

#### `ultimoNumeroEmitido`

Último número efetivamente atribuído a um documento emitido.

#### `proximoNumero`

Número previsto para a próxima emissão.

Este valor deve ser controlado transacionalmente.

#### `codigoValidacaoAt`

Código atribuído pela AT após a comunicação da série.

Este código será utilizado na construção do ATCUD.

#### `estado`

Estado funcional da série.

Estados propostos:

```text
RASCUNHO
AGUARDA_VALIDACAO_AT
ATIVA
SUSPENSA
FINALIZADA
```

#### `origemCodigoValidacao`

Forma através da qual o código de validação foi obtido.

Valores propostos:

```text
PORTAL_FINANCAS
WEBSERVICE
```

#### `ambiente`

Identifica se a série pertence a um ambiente de teste ou de produção.

Valores:

```text
TESTE
PRODUCAO
```

#### `version`

Campo utilizado para controlo otimista de concorrência.

Pode ser implementado através de:

```java
@Version
```

---

## 6. Restrições de unicidade

Deve existir uma restrição de unicidade para impedir configurações duplicadas.

A combinação mínima deve incluir:

```text
empresa
codigoSerie
tipoDocumentoSaft
ambiente
```

Exemplo conceptual:

```text
UNIQUE (
    empresa_id,
    codigo_serie,
    tipo_documento_saft,
    ambiente
)
```

Esta restrição evita que duas séries distintas tentem controlar a mesma sequência documental.

---

## 7. Estados da série

### 7.1. RASCUNHO

A série foi criada, mas ainda não está pronta para utilização.

Neste estado:

* pode ser editada;
* ainda não possui código de validação da AT;
* não pode emitir documentos;
* não pode atribuir numeração fiscal.

### 7.2. AGUARDA_VALIDACAO_AT

A série está configurada e aguarda a introdução ou obtenção do código atribuído pela AT.

Neste estado:

* não pode emitir documentos;
* o utilizador pode registar o código de validação;
* pode ser comunicada através do Portal das Finanças ou, futuramente, por webservice.

### 7.3. ATIVA

A série está disponível para emissão.

Para ficar ativa deve possuir:

* entidade emitente;
* código da série;
* tipo documental;
* número inicial;
* código de validação da AT;
* ambiente;
* estado válido.

### 7.4. SUSPENSA

A série encontra-se temporariamente bloqueada.

Não pode ser utilizada para novas emissões.

Os documentos já emitidos permanecem válidos e disponíveis para consulta e reimpressão.

### 7.5. FINALIZADA

A série foi encerrada definitivamente.

Neste estado:

* não pode voltar a emitir documentos;
* não pode regressar ao estado ativo;
* permanece disponível para auditoria;
* mantém o histórico de documentos emitidos.

---

## 8. Comunicação das séries à AT

A implementação deve considerar duas modalidades.

### 8.1. Comunicação manual

Na primeira fase, a comunicação será realizada manualmente no Portal das Finanças.

Fluxo proposto:

1. o utilizador cria a série no FAC;
2. o utilizador comunica a série no Portal das Finanças;
3. a AT atribui um código de validação;
4. o utilizador regista esse código no FAC;
5. o FAC valida formalmente os dados;
6. a série passa ao estado `ATIVA`.

Esta modalidade deve ser suficiente para o primeiro ciclo de implementação.

### 8.2. Comunicação automática

Numa fase posterior, o FAC poderá comunicar diretamente com os serviços eletrónicos da AT.

A integração deve ficar isolada atrás de uma interface própria.

Exemplo:

```java
public interface ComunicacaoSeriesProvider {

    ResultadoComunicacaoSerie comunicar(
        SerieComunicacaoRequest request
    );

    ResultadoConsultaSerie consultar(
        SerieConsultaRequest request
    );

    ResultadoFinalizacaoSerie finalizar(
        SerieFinalizacaoRequest request
    );
}
```

A lógica de negócio do FAC não deve depender diretamente:

* do protocolo da AT;
* do formato SOAP;
* do WSDL;
* do certificado digital;
* da disponibilidade temporária do serviço externo.

Deve existir uma camada de adaptação.

Exemplo:

```text
SerieDocumentalService
        |
        v
ComunicacaoSeriesProvider
        |
        v
AtComunicacaoSeriesAdapter
        |
        v
Webservice AT
```

---

## 9. Ambiente de testes e ambiente de produção

O FAC deve distinguir de forma inequívoca:

```text
TESTE
PRODUCAO
```

Uma série criada em ambiente de testes nunca pode ser utilizada para emitir documentos reais.

O sistema deve impedir:

* utilização de códigos de teste em produção;
* mistura de numerações entre ambientes;
* utilização acidental de séries de teste num documento comercial real.

A identificação do ambiente deve estar presente:

* na série;
* no processo de emissão;
* nos registos de auditoria;
* nas configurações de integração.

---

## 10. Dados fiscais a persistir no documento

No momento da emissão definitiva, os principais dados da série devem ser copiados para o documento.

Campos propostos:

```text
serieDocumentalId
codigoSerie
tipoDocumentoSaft
numeroSequencial
numeroDocumentoCompleto
codigoValidacaoSerie
atcud
qrPayload
qrPayloadVersion
dataHoraEmissaoDefinitiva
```

### 10.1. Razão para a duplicação controlada

O documento deve conservar a fotografia fiscal do momento em que foi emitido.

Uma reimpressão futura não deve depender:

* da configuração atual da série;
* da designação atual do tipo documental;
* de alterações posteriores à aplicação;
* de alterações numa biblioteca de geração de QR;
* de consultas dinâmicas a tabelas de configuração.

Os dados fiscais persistidos no documento devem permanecer imutáveis.

---

## 11. Numeração documental

A numeração deve ser:

* sequencial;
* única dentro da série;
* atribuída apenas na emissão definitiva;
* resistente a emissões concorrentes;
* irreversível.

Um rascunho não deve consumir numeração fiscal.

### 11.1. Regra geral

A atribuição do número deve ocorrer apenas quando o utilizador executa uma ação inequívoca, por exemplo:

```text
Emitir documento
```

Antes desse momento, o documento deve permanecer em estado:

```text
RASCUNHO
```

### 11.2. Proibição de `MAX + 1`

Não deve ser utilizado o padrão:

```sql
SELECT MAX(numero) + 1
```

Esta abordagem não é segura quando existem duas emissões simultâneas.

Pode originar:

* números duplicados;
* conflitos de gravação;
* documentos parcialmente emitidos;
* falhas difíceis de reproduzir.

### 11.3. Controlo concorrencial

A atribuição do número deve ser realizada dentro de uma transação.

Opções possíveis:

* bloqueio pessimista da série;
* controlo otimista com `@Version`;
* sequência transacional específica;
* mecanismo equivalente suportado pela base de dados.

Abordagem recomendada:

```text
1. bloquear a série;
2. validar o estado;
3. obter o próximo número;
4. incrementar a sequência;
5. emitir o documento;
6. confirmar a transação.
```

Se qualquer etapa falhar, toda a operação deve ser revertida.

---

## 12. Identificação completa do documento

O documento deve possuir uma identificação funcional completa e estável.

Exemplo:

```text
FT 2026/153
```

Esta identificação deve resultar de:

```text
tipo documental + série + número sequencial
```

O formato exato deve ser definido centralmente e não deve ser construído de forma diferente em cada ecrã ou relatório.

Deve existir um componente único responsável por produzir a identificação documental.

Exemplo:

```java
public interface IdentificacaoDocumentoService {

    String construir(
        String tipoDocumento,
        String codigoSerie,
        long numeroSequencial
    );
}
```

---

## 13. ATCUD

O ATCUD resulta da concatenação entre:

* o código de validação da série atribuído pela AT;
* o número sequencial do documento.

Formato:

```text
CODIGO_VALIDACAO-NUMERO_SEQUENCIAL
```

Exemplo:

```text
ABCD234F-153
```

Na representação visual do documento deve aparecer:

```text
ATCUD:ABCD234F-153
```

O valor persistido deve ser apenas:

```text
ABCD234F-153
```

O prefixo:

```text
ATCUD:
```

é apenas um elemento de apresentação.

---

## 14. Serviço de geração do ATCUD

Deve existir um serviço pequeno, isolado e determinístico.

Exemplo:

```java
public interface AtcudService {

    String gerar(
        String codigoValidacaoSerie,
        long numeroSequencial
    );
}
```

### 14.1. Validações

O serviço deve validar:

* código de validação obrigatório;
* código sem espaços;
* número sequencial superior a zero;
* separação por hífen;
* inexistência do prefixo `ATCUD:` no valor;
* formato compatível com as regras adotadas.

### 14.2. Imutabilidade

Depois da emissão:

* o ATCUD não pode ser alterado;
* o código de validação gravado no documento não pode ser substituído;
* uma reimpressão deve utilizar o mesmo ATCUD.

---

## 15. Código QR

O Código QR deve ser gerado a partir dos dados fiscais definitivos do documento.

Não deve ser gerado:

* no frontend;
* durante a impressão;
* diretamente no template PDF;
* a partir de dados não consolidados.

Deve existir no backend um conjunto de componentes próprios.

Exemplo:

```text
QrFiscalPayloadBuilder
QrFiscalValidator
QrCodeImageGenerator
```

---

## 16. `QrFiscalPayloadBuilder`

Responsável por construir o texto fiscal que será codificado no QR.

Entrada:

* dados do emitente;
* dados do adquirente;
* identificação do documento;
* data do documento;
* ATCUD;
* estado fiscal;
* bases tributáveis;
* taxas de IVA;
* valores de imposto;
* operações isentas;
* total do documento;
* retenções, quando aplicáveis;
* restantes campos legalmente exigidos.

Saída:

```text
String qrPayload
```

O builder deve ser independente da biblioteca utilizada para produzir a imagem QR.

---

## 17. `QrFiscalValidator`

Responsável por validar o payload antes da emissão definitiva.

Deve verificar:

* presença dos campos obrigatórios;
* ordem dos campos;
* formato das datas;
* formato dos valores monetários;
* separadores;
* casas decimais;
* coerência entre bases e impostos;
* coerência entre total líquido, impostos e total final;
* correspondência entre o ATCUD e o documento;
* identificação correta do tipo documental;
* estado fiscal;
* tratamento de documentos em moeda estrangeira;
* campos aplicáveis a isenções;
* campos aplicáveis a retenções.

Um payload inválido deve impedir a emissão definitiva.

---

## 18. `QrCodeImageGenerator`

Responsável por converter o payload validado numa imagem QR.

Este componente não deve conhecer regras fiscais.

A sua única responsabilidade deve ser:

```text
payload fiscal validado
        |
        v
imagem QR
```

A biblioteca utilizada deve permitir controlar, pelo menos:

* correção de erro;
* modo de codificação;
* dimensões;
* margens;
* formato da imagem;
* resolução.

---

## 19. Estrutura conceptual do payload

O payload do Código QR utiliza campos identificados e separados de acordo com a especificação fiscal aplicável.

Exemplo meramente ilustrativo:

```text
A:509999999*
B:999999990*
C:PT*
D:FT*
E:N*
F:20260615*
G:FT 2026/153*
H:ABCD234F-153*
N:23.00*
O:123.00*
Q:AB12
```

O conteúdo real deve ser construído de acordo com:

* o tipo de documento;
* as taxas de IVA presentes;
* as isenções;
* o estado do documento;
* a moeda;
* as retenções;
* os restantes elementos fiscais aplicáveis.

A ordem dos campos deve ser rigorosamente respeitada.

---

## 20. Valores monetários

Os valores monetários incluídos no payload devem seguir um formato uniforme.

Princípios:

* utilização de ponto como separador decimal;
* duas casas decimais, salvo regra específica;
* ausência de separadores de milhares;
* ausência de símbolos monetários;
* arredondamentos controlados;
* utilização de `BigDecimal`.

Exemplo:

```text
1234.50
```

Não utilizar:

```text
1.234,50 €
```

### 20.1. Moeda estrangeira

Quando o documento estiver expresso noutra moeda, os valores relevantes para o QR devem ser calculados segundo as regras fiscais aplicáveis.

O FAC deve conservar:

* moeda original;
* taxa de câmbio;
* valores originais;
* valores convertidos relevantes;
* data ou origem da taxa utilizada.

A conversão não deve ser improvisada durante a impressão.

---

## 21. Persistência do payload

O texto integral do payload deve ser persistido no documento.

Campo proposto:

```text
qrPayload
```

Deve também ser guardada a versão da especificação utilizada:

```text
qrPayloadVersion
```

Exemplo:

```text
1.0
```

### 21.1. Razões para persistir o payload

A persistência permite:

* auditoria;
* reimpressão fiel;
* comparação com os dados do documento;
* testes automáticos;
* investigação de divergências;
* regeneração da imagem;
* independência da biblioteca QR;
* preservação histórica.

A imagem QR pode ser regenerada a partir do payload.

Não é obrigatório conservar a imagem como elemento permanente, desde que o payload original seja preservado.

---

## 22. Snapshot fiscal do documento

O QR deve ser construído a partir de um snapshot fiscal consolidado.

Exemplo conceptual:

```text
DocumentoFiscalSnapshot
```

Campos possíveis:

```text
nifEmitente
nifAdquirente
paisAdquirente
tipoDocumento
estadoDocumento
dataDocumento
numeroDocumento
atcud
totalLiquido
totalIsento
basesPorTaxa
ivaPorTaxa
totalImposto
retencoes
totalDocumento
moeda
taxaCambio
```

Este snapshot deve ser produzido antes da gravação definitiva e validado como uma unidade coerente.

O objetivo é evitar que cada componente volte a calcular os totais de forma independente.

O mesmo snapshot pode alimentar:

* emissão;
* QR;
* PDF;
* SAF-T;
* auditoria.

---

## 23. Fluxo da emissão definitiva

A emissão definitiva deve decorrer numa única transação.

Fluxo recomendado:

```text
1. receber pedido de emissão;
2. carregar o documento em rascunho;
3. validar permissões;
4. validar dados do cliente;
5. validar linhas;
6. validar impostos;
7. carregar e bloquear a série;
8. confirmar que a série está ativa;
9. confirmar o tipo documental;
10. atribuir o próximo número;
11. construir a identificação completa;
12. gerar o ATCUD;
13. consolidar o snapshot fiscal;
14. construir o payload QR;
15. validar o payload;
16. gravar os dados fiscais;
17. atualizar o último número da série;
18. alterar o estado para emitido;
19. registar auditoria;
20. confirmar a transação.
```

Se ocorrer uma falha entre os pontos 1 e 19, a operação deve ser revertida integralmente.

---

## 24. Estados do documento

Devem existir estados documentais claros.

Proposta mínima:

```text
RASCUNHO
EMITIDO
ANULADO
```

Poderão existir outros estados funcionais, desde que não confundam o estado comercial com o estado fiscal.

### 24.1. RASCUNHO

* pode ser editado;
* não possui número fiscal definitivo;
* não possui ATCUD definitivo;
* não deve possuir payload QR definitivo;
* não deve ser exportado como documento emitido.

### 24.2. EMITIDO

* possui número definitivo;
* possui ATCUD;
* possui payload QR;
* possui data e hora de emissão;
* os dados fiscais ficam bloqueados;
* pode ser impresso e exportado.

### 24.3. ANULADO

* mantém o número;
* mantém a série;
* mantém o ATCUD;
* mantém o histórico;
* não volta a disponibilizar o número;
* deve refletir o estado de anulação nas representações futuras.

---

## 25. Anulação

A anulação não pode eliminar o documento.

Deve ser implementada como alteração controlada do estado fiscal.

Deve ficar registado:

```text
dataHoraAnulacao
utilizadorAnulacao
motivoAnulacao
estadoAnterior
estadoAtual
```

O número documental nunca regressa à sequência.

Exemplo:

```text
FT 2026/153 - ANULADO
```

O número `153` não pode voltar a ser utilizado.

---

## 26. Reimpressão

A reimpressão deve utilizar os dados fiscais originalmente persistidos.

Deve conservar:

* número;
* série;
* tipo documental;
* ATCUD;
* payload QR;
* totais;
* data de emissão;
* dados históricos relevantes.

Não deve:

* gerar um novo ATCUD;
* reconstruir o QR com regras atuais;
* substituir o código de validação;
* atribuir um novo número;
* alterar os valores originais.

Quando aplicável, a representação deve identificar que se trata de:

```text
2.ª via
Reimpressão
Cópia
```

sem alterar a identidade fiscal do documento.

---

## 27. Regras visuais

O documento deve apresentar o ATCUD de forma visível e legível.

Representação:

```text
ATCUD:ABCD234F-153
```

Na página em que o Código QR estiver presente, o ATCUD deve ficar imediatamente acima do QR.

Exemplo:

```text
ATCUD:ABCD234F-153

[ CÓDIGO QR ]
```

O ATCUD deve ser apresentado em todas as páginas do documento.

O Código QR poderá ser colocado na primeira ou na última página, de acordo com o modelo documental adotado e com as regras aplicáveis.

---

## 28. Dimensões e legibilidade do QR

A geração do QR deve respeitar as especificações técnicas aplicáveis.

Devem ser considerados:

* dimensão física mínima;
* margem de segurança;
* contraste suficiente;
* correção de erro;
* qualidade de impressão;
* resolução;
* legibilidade em papel;
* legibilidade em PDF;
* leitura por dispositivos móveis.

A dimensão não deve ser controlada apenas em píxeis.

O mecanismo de geração de PDF deve garantir uma dimensão física estável, independentemente da resolução.

---

## 29. Integração com o PDF

O componente de geração de PDF deve receber um documento já emitido.

Entrada conceptual:

```text
DocumentoFiscalEmitido
```

O PDF deve limitar-se a representar:

* dados comerciais;
* dados fiscais;
* ATCUD;
* imagem QR gerada a partir do payload persistido;
* totais;
* estado do documento;
* paginação.

Não deve recalcular a lógica fiscal.

Arquitetura:

```text
Documento emitido
        |
        v
Modelo de impressão
        |
        v
PDF
```

---

## 30. Integração com o frontend

O React não deve gerar:

* ATCUD;
* número fiscal;
* payload QR;
* imagem QR fiscal definitiva;
* totais fiscais oficiais.

O frontend deve:

* permitir selecionar séries válidas;
* apresentar o estado da série;
* impedir ações claramente inválidas;
* solicitar a emissão ao backend;
* apresentar o resultado;
* permitir descarregar ou visualizar o PDF.

A validação do frontend é apenas complementar.

A decisão fiscal final pertence ao backend.

---

## 31. Ecrã de gestão de séries

Deve existir uma área própria para gerir séries documentais.

Informação recomendada:

```text
Código da série
Descrição
Tipo documental
Número inicial
Último número emitido
Próximo número
Código de validação AT
Origem do código
Estado
Ambiente
Data de comunicação
Data de primeira utilização
Data de finalização
```

### 31.1. Ações possíveis

Consoante o estado:

* criar série;
* editar rascunho;
* registar código da AT;
* validar série;
* ativar série;
* suspender série;
* reativar série suspensa;
* finalizar série;
* consultar documentos emitidos;
* consultar histórico;
* comunicar à AT, numa fase posterior.

---

## 32. Bloqueios após primeira utilização

Depois da emissão do primeiro documento, devem ficar bloqueados:

* código da série;
* tipo documental;
* número inicial;
* código de validação da AT;
* ambiente;
* entidade emitente.

Alterações posteriores devem ser realizadas através da criação de uma nova série.

Não deve ser permitido corrigir silenciosamente uma série já utilizada.

---

## 33. Auditoria

As operações relevantes devem ficar registadas.

Eventos mínimos:

```text
SERIE_CRIADA
SERIE_EDITADA
CODIGO_AT_REGISTADO
SERIE_ATIVADA
SERIE_SUSPENSA
SERIE_REATIVADA
SERIE_FINALIZADA
DOCUMENTO_EMITIDO
DOCUMENTO_ANULADO
DOCUMENTO_REIMPRESSO
```

Cada registo deve incluir:

* data e hora;
* utilizador;
* empresa;
* ação;
* entidade afetada;
* valores anteriores;
* valores posteriores;
* resultado;
* origem da operação.

---

## 34. Integração com SAF-T

O SAF-T deve utilizar os mesmos valores que ficaram persistidos no documento.

Em particular:

* número do documento;
* série;
* tipo documental;
* ATCUD;
* datas;
* estado;
* totais;
* impostos;
* moeda;
* dados do adquirente.

Não deve existir uma segunda implementação independente do ATCUD para o SAF-T.

Regra:

```text
ATCUD do documento
=
ATCUD do QR
=
ATCUD do SAF-T
```

Esta igualdade deve ser garantida por arquitetura, não apenas por testes ocasionais.

---

## 35. Tratamento de erros

Os erros devem ser classificados.

### 35.1. Erros de configuração

Exemplos:

* série sem código AT;
* série inativa;
* tipo documental incompatível;
* ambiente incorreto.

### 35.2. Erros funcionais

Exemplos:

* documento sem cliente obrigatório;
* documento sem linhas;
* valores negativos indevidos;
* impostos incoerentes.

### 35.3. Erros técnicos

Exemplos:

* falha de base de dados;
* conflito concorrencial;
* falha na biblioteca QR;
* indisponibilidade do serviço da AT.

### 35.4. Regra de segurança

Nenhum erro pode deixar um documento parcialmente emitido.

O resultado deve ser sempre um de dois:

```text
emissão concluída
```

ou:

```text
emissão não realizada
```

Nunca deve existir um estado intermédio em que:

* o número foi consumido;
* o ATCUD foi atribuído;
* mas o documento não ficou corretamente gravado.

---

## 36. Testes unitários

### 36.1. Testes do ATCUD

Devem existir testes para:

* código de validação válido;
* código vazio;
* código nulo;
* código com espaços;
* número igual a zero;
* número negativo;
* número positivo;
* números com vários algarismos;
* separação correta por hífen;
* ausência do prefixo textual.

Exemplo esperado:

```text
codigo = ABCD234F
numero = 153
resultado = ABCD234F-153
```

### 36.2. Testes das séries

Devem existir testes para:

* criação de série;
* ativação sem código AT;
* ativação com código válido;
* utilização de série suspensa;
* utilização de série finalizada;
* tipo documental incompatível;
* ambiente de teste em produção;
* tentativa de alteração após primeira emissão;
* unicidade da série.

### 36.3. Testes do payload QR

Casos mínimos:

* fatura normal;
* fatura simplificada;
* fatura-recibo;
* nota de crédito;
* nota de débito;
* documento isento;
* documento com uma taxa de IVA;
* documento com várias taxas de IVA;
* consumidor final;
* cliente identificado;
* cliente estrangeiro;
* moeda estrangeira;
* retenção;
* documento anulado;
* arredondamentos.

---

## 37. Testes de integração

Devem ser testados:

* processo completo de emissão;
* rollback em caso de erro;
* duas emissões simultâneas;
* emissão em séries diferentes;
* reimpressão;
* anulação;
* geração de PDF;
* persistência do payload;
* correspondência entre QR e documento;
* correspondência entre ATCUD e SAF-T.

### 37.1. Teste concorrencial

Deve existir um teste em que duas operações tentam emitir simultaneamente na mesma série.

Resultado esperado:

```text
Documento A -> número 153
Documento B -> número 154
```

Nunca:

```text
Documento A -> número 153
Documento B -> número 153
```

---

## 38. Testes físicos

Os testes automáticos não substituem os testes de leitura real.

Devem ser realizados testes com:

* PDF visualizado no ecrã;
* impressão A4;
* impressão a preto e branco;
* impressoras com menor qualidade;
* documentos com várias páginas;
* redução da escala de impressão;
* ampliação;
* diferentes leitores de QR;
* vários telemóveis.

O QR deve continuar legível após o percurso completo:

```text
dados
→ payload
→ imagem
→ PDF
→ impressão
→ leitura
```

---

## 39. Segurança e permissões

As operações sobre séries devem exigir permissões próprias.

Exemplos:

```text
SERIE_CONSULTAR
SERIE_CRIAR
SERIE_EDITAR
SERIE_ATIVAR
SERIE_SUSPENDER
SERIE_FINALIZAR
SERIE_COMUNICAR_AT
DOCUMENTO_EMITIR
DOCUMENTO_ANULAR
```

A introdução manual do código de validação da AT deve ser reservada a utilizadores autorizados.

A finalização de uma série deve exigir confirmação explícita.

---

## 40. API proposta

### 40.1. Séries

Exemplos de endpoints:

```http
GET /api/series-documentais
GET /api/series-documentais/{id}
POST /api/series-documentais
PUT /api/series-documentais/{id}
POST /api/series-documentais/{id}/registar-codigo-at
POST /api/series-documentais/{id}/ativar
POST /api/series-documentais/{id}/suspender
POST /api/series-documentais/{id}/reativar
POST /api/series-documentais/{id}/finalizar
GET /api/series-documentais/{id}/documentos
```

### 40.2. Emissão

```http
POST /api/documentos-comerciais/{id}/emitir
```

Resposta possível:

```json
{
  "documentoId": 987,
  "numeroDocumento": "FT 2026/153",
  "atcud": "ABCD234F-153",
  "estado": "EMITIDO",
  "dataHoraEmissao": "2026-06-15T14:30:00"
}
```

### 40.3. Anulação

```http
POST /api/documentos-comerciais/{id}/anular
```

Pedido:

```json
{
  "motivo": "Documento emitido com identificação incorreta do adquirente"
}
```

---

## 41. Serviços de domínio propostos

Componentes recomendados:

```text
SerieDocumentalService
NumeracaoDocumentoService
EmissaoDocumentoService
IdentificacaoDocumentoService
AtcudService
DocumentoFiscalSnapshotFactory
QrFiscalPayloadBuilder
QrFiscalValidator
QrCodeImageGenerator
DocumentoAnulacaoService
```

Cada componente deve ter uma responsabilidade clara.

O `EmissaoDocumentoService` deve orquestrar o processo, mas não deve conter todas as regras em métodos extensos e monolíticos.

---

## 42. Exemplo de arquitetura

```text
DocumentoController
        |
        v
EmissaoDocumentoService
        |
        +--> SerieDocumentalService
        |
        +--> NumeracaoDocumentoService
        |
        +--> AtcudService
        |
        +--> DocumentoFiscalSnapshotFactory
        |
        +--> QrFiscalPayloadBuilder
        |
        +--> QrFiscalValidator
        |
        +--> AuditoriaService
        |
        v
DocumentoRepository
```

A geração do PDF deve ocorrer depois:

```text
Documento emitido
        |
        v
DocumentoPdfService
        |
        +--> QrCodeImageGenerator
        |
        v
PDF
```

---

## 43. Fases de implementação

### Fase 1 - Séries documentais

Implementar:

* entidade;
* estados;
* tipo documental;
* ambiente;
* numeração;
* configuração manual;
* código de validação AT;
* ecrã de gestão;
* permissões básicas.

### Fase 2 - Emissão definitiva

Implementar:

* estados do documento;
* ação de emissão;
* transação;
* bloqueio da série;
* atribuição do número;
* identificação completa;
* imutabilidade fiscal.

### Fase 3 - ATCUD

Implementar:

* serviço de geração;
* validações;
* persistência;
* integração com documentos;
* representação no PDF;
* testes.

### Fase 4 - Código QR

Implementar:

* snapshot fiscal;
* builder;
* validator;
* persistência do payload;
* geração da imagem;
* integração com PDF;
* testes físicos.

### Fase 5 - SAF-T

Garantir:

* utilização do ATCUD persistido;
* utilização dos mesmos totais fiscais;
* coerência entre documento, QR e SAF-T;
* testes cruzados.

### Fase 6 - Comunicação automática à AT

Implementar:

* cliente técnico;
* autenticação;
* certificados;
* comunicação;
* consulta;
* finalização;
* gestão de erros;
* histórico das comunicações;
* ambiente de testes.

---

## 44. Decisões de implementação

Ficam estabelecidas as seguintes decisões:

1. As séries documentais serão entidades próprias.

2. Cada série será associada a um tipo documental SAF-T.

3. A comunicação manual através do Portal das Finanças será suportada na primeira fase.

4. O webservice da AT será implementado apenas após estabilização do núcleo fiscal.

5. O número do documento será atribuído apenas na emissão definitiva.

6. Rascunhos não consumirão numeração fiscal.

7. A numeração será controlada transacionalmente.

8. O padrão `MAX + 1` não será utilizado.

9. O ATCUD será gerado no backend.

10. O valor persistido não incluirá o prefixo `ATCUD:`.

11. O payload QR será construído e validado no backend.

12. O payload QR será persistido no documento.

13. A imagem QR poderá ser regenerada a partir do payload.

14. O PDF não executará cálculos fiscais.

15. O React não gerará elementos fiscais definitivos.

16. Documentos emitidos serão fiscalmente imutáveis.

17. Documentos anulados manterão número, série e ATCUD.

18. Reimpressões utilizarão os dados originais.

19. O SAF-T utilizará o ATCUD persistido no documento.

20. Os ambientes de teste e produção serão estritamente separados.

---

## 45. Critérios de aceitação

A implementação considera-se concluída quando:

* existe gestão autónoma de séries;
* cada série possui um tipo documental;
* cada série possui um ambiente;
* não é possível emitir com série inativa;
* não é possível emitir sem código de validação AT;
* o número é atribuído de forma sequencial;
* duas emissões concorrentes não recebem o mesmo número;
* o ATCUD é gerado corretamente;
* o ATCUD é persistido;
* o payload QR é construído;
* o payload QR é validado;
* o payload QR é persistido;
* o PDF apresenta o ATCUD;
* o PDF apresenta o Código QR;
* o ATCUD aparece em todas as páginas;
* o QR é legível em testes físicos;
* a anulação mantém número e ATCUD;
* a reimpressão conserva os dados originais;
* o SAF-T utiliza o mesmo ATCUD;
* existem testes unitários;
* existem testes de integração;
* existe auditoria das operações;
* códigos de teste não podem ser usados em produção.

---

## 46. Riscos a evitar

### 46.1. Gerar o ATCUD durante a impressão

Risco:

* valores diferentes em impressões diferentes;
* ausência de persistência;
* divergência com SAF-T.

### 46.2. Gerar o QR no React

Risco:

* lógica fiscal no cliente;
* manipulação indevida;
* diferenças entre browsers;
* ausência de controlo central.

### 46.3. Recalcular o QR em cada reimpressão

Risco:

* alterações históricas;
* utilização de regras novas em documentos antigos;
* divergência com o original.

### 46.4. Usar `MAX + 1`

Risco:

* duplicação de números em concorrência.

### 46.5. Permitir editar séries utilizadas

Risco:

* quebra de rastreabilidade;
* incoerência histórica;
* documentos associados a configurações diferentes.

### 46.6. Misturar ambiente de teste e produção

Risco:

* emissão real com códigos inválidos;
* documentos fiscalmente desconformes.

### 46.7. Colocar toda a lógica no serviço de PDF

Risco:

* acoplamento;
* dificuldade de testes;
* repetição de cálculos;
* inconsistência com SAF-T.

---

## 47. Conclusão

A implementação do ATCUD e do Código QR deve começar na gestão das séries e no processo de emissão definitiva.

O problema não é essencialmente gráfico.

É um problema de:

* identidade documental;
* numeração;
* integridade;
* imutabilidade;
* auditoria;
* conformidade fiscal.

A ordem correta é:

```text
série válida
→ número sequencial
→ documento definitivo
→ ATCUD
→ snapshot fiscal
→ payload QR
→ persistência
→ PDF
→ SAF-T
```

O webservice da AT constitui uma automatização posterior.

O núcleo do sistema deve funcionar corretamente mesmo quando o código de validação é introduzido manualmente.

Esta abordagem permite que o FAC evolua com segurança, evitando que as exigências fiscais fiquem dispersas entre o frontend, o PDF, o SAF-T e os serviços externos.
