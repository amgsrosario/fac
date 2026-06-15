# 11 - Implementação do ATCUD e Código QR nos documentos PDF

**Projeto:** FAC
**Documento:** Implementação do ATCUD e Código QR nos documentos PDF
**Versão:** 1.0
**Estado:** Aprovado para implementação
**Data:** 15 de junho de 2026
**Autor:** António Rosário

---

## 1. Objetivo

O presente documento define a implementação controlada do:

* ATCUD;
* conteúdo fiscal do Código QR;
* imagem do Código QR;
* apresentação do ATCUD e do Código QR nos documentos PDF emitidos pelo FAC.

A implementação deve aproveitar:

* a entidade `Serie` já existente;
* o campo `codigoAt`;
* o campo `dataCodigoAt`;
* o campo `numerador`;
* a relação entre `Serie` e `TipoDocumento`;
* o processo atual de emissão documental;
* o mecanismo atual de geração de PDF.

Não deve ser criada uma nova entidade de séries documentais.

---

## 2. Relação com os documentos anteriores

Este documento concretiza uma parte delimitada do documento:

```text
09 - Implementação de Séries Documentais, ATCUD e Código QR
```

e pressupõe a conclusão do documento:

```text
10 - Revisão controlada do CRUD de séries documentais
```

O MD 09 continua a constituir o enquadramento geral e a arquitetura futura.

O presente MD 11 limita-se a:

1. gerar o ATCUD;
2. guardar o ATCUD no documento;
3. construir o conteúdo do Código QR;
4. guardar o conteúdo do Código QR;
5. gerar a imagem QR;
6. apresentar ATCUD e QR nos PDFs;
7. garantir reimpressão coerente;
8. criar os testes necessários.

---

## 3. Fora do âmbito

Não implementar nesta tarefa:

* comunicação automática de séries à AT;
* webservice de comunicação de séries;
* certificados digitais da AT;
* consulta automática de séries;
* finalização automática de séries;
* reformulação geral do CRUD de séries;
* criação de uma nova entidade `SerieDocumental`;
* certificação do programa de faturação;
* comunicação de documentos ao e-Fatura;
* implementação integral do SAF-T;
* alteração global da interface React;
* reconstrução geral do mecanismo de emissão;
* sistema completo de estados das séries.

Qualquer necessidade adicional deve ser identificada no relatório final, mas não implementada sem autorização.

---

## 4. Princípio basilar

O ATCUD e o conteúdo do Código QR devem ser gerados no backend durante o processo de emissão definitiva.

Não devem ser gerados:

* no React;
* no browser;
* diretamente no template PDF;
* em cada reimpressão;
* através de cálculos independentes no módulo de reporting.

O PDF deve receber um documento fiscal já consolidado.

Fluxo pretendido:

```text
Documento em rascunho
→ seleção e validação da série
→ atribuição do número sequencial
→ geração do ATCUD
→ consolidação dos totais fiscais
→ construção do payload QR
→ validação
→ persistência
→ emissão definitiva
→ geração do PDF
```

---

## 5. Inspeção inicial obrigatória

Antes de alterar código, analisar:

* `Serie`;
* `SerieId`;
* `TipoDocumento`;
* `SerieRepository`;
* `SerieService`;
* processo atual de atribuição de números;
* locais onde `proximoNumero()` é chamado;
* entidades dos documentos comerciais;
* entidades dos documentos financeiros;
* estados documentais existentes;
* serviços de emissão;
* DTOs de documentos;
* mappers;
* repositories;
* mecanismo atual de geração de PDF;
* templates ou componentes de reporting;
* bibliotecas já existentes para PDF;
* dependências Maven;
* testes de emissão;
* testes de documentos;
* testes de PDF;
* tratamento atual de anulações e reimpressões.

Apresentar um plano curto antes de iniciar alterações.

Não criar componentes duplicados quando já existirem equivalentes no projeto.

---

## 6. Série documental existente

A entidade `Serie` já contém:

```java
private TipoDocumento tipoDocumento;
private String serie;
private String codigoAt;
private LocalDate dataCodigoAt;
private Long numerador;
```

e o método:

```java
public Long proximoNumero() {
    numerador++;
    return numerador;
}
```

Esta entidade deve continuar a ser utilizada.

Não adicionar campos redundantes como:

```text
ultimoNumeroEmitido
proximoNumero
numeroAtual
```

O campo `numerador` já representa a progressão da série.

---

## 7. Validação da série na emissão

Antes da emissão definitiva, verificar:

* existência da série;
* correspondência entre série e tipo documental;
* existência de `codigoAt`;
* `codigoAt` não vazio;
* validade do número sequencial;
* integridade do processo transacional.

A emissão deve ser rejeitada quando:

```java
serie.getCodigoAt() == null
```

ou:

```java
serie.getCodigoAt().isBlank()
```

Mensagem sugerida:

```text
A série selecionada não possui código de validação atribuído pela AT.
```

Não permitir que documentos fiscais definitivos sejam emitidos sem código AT, salvo se existir no projeto um modo técnico de testes claramente separado.

---

## 8. Segurança da numeração

Confirmar que a obtenção do número através de:

```java
serie.proximoNumero()
```

ocorre:

* dentro de uma transação;
* em conjunto com a gravação definitiva do documento;
* com proteção contra duas emissões simultâneas.

Verificar se o projeto já utiliza:

* bloqueio pessimista;
* bloqueio otimista;
* `@Version`;
* restrição única;
* outra proteção equivalente.

Se não existir proteção suficiente, implementar a solução mínima compatível com a arquitetura atual.

A implementação deve garantir que duas emissões simultâneas na mesma série recebem números diferentes.

Exemplo esperado:

```text
Documento A → 101
Documento B → 102
```

Nunca:

```text
Documento A → 101
Documento B → 101
```

---

## 9. Construção do ATCUD

O ATCUD deve resultar da concatenação de:

```text
código de validação da série
+
hífen
+
número sequencial do documento
```

Exemplo:

```text
ABCD1234-27
```

O valor persistido não deve incluir o prefixo visual:

```text
ATCUD:
```

O prefixo deve ser adicionado apenas na apresentação.

No PDF:

```text
ATCUD:ABCD1234-27
```

No modelo persistido:

```text
ABCD1234-27
```

---

## 10. Serviço de ATCUD

Criar um componente isolado, salvo se já existir equivalente.

Nome sugerido:

```text
AtcudService
```

Contrato possível:

```java
public interface AtcudService {

    String gerar(
            String codigoValidacaoAt,
            Long numeroSequencial
    );
}
```

Validações mínimas:

* código não nulo;
* código não vazio;
* remoção de espaços exteriores;
* número não nulo;
* número superior a zero;
* separação por um único hífen;
* ausência do prefixo `ATCUD:`.

O serviço deve ser determinístico.

Para os mesmos dados de entrada deve produzir sempre o mesmo valor.

---

## 11. Persistência no documento

O ATCUD deve ser persistido no documento fiscal no momento da emissão definitiva.

Adicionar ao modelo documental adequado:

```java
@Column(name = "atcud", length = 150)
private String atcud;
```

O comprimento definitivo deve ser validado face ao modelo atual e às regras aplicáveis.

A coluna deve ser inicialmente compatível com documentos antigos.

Não adicionar imediatamente:

```java
nullable = false
```

caso existam documentos previamente gravados sem ATCUD.

A obrigatoriedade deve ser garantida no fluxo de novas emissões.

---

## 12. Snapshot histórico

O documento emitido deve conservar os dados necessários para uma reimpressão fiel.

Confirmar se já ficam persistidos:

* código da série;
* número sequencial;
* identificação completa;
* tipo documental;
* data de emissão;
* código AT ou informação suficiente para auditoria;
* ATCUD;
* valores fiscais;
* estado documental.

Não depender exclusivamente da configuração atual da entidade `Serie` para reconstruir documentos antigos.

O ATCUD persistido deve ser a referência oficial do documento emitido.

---

## 13. Imutabilidade do ATCUD

Depois da emissão:

* o ATCUD não pode ser editado;
* o ATCUD não pode ser regenerado com outro código;
* o ATCUD não pode mudar numa reimpressão;
* a alteração do nome da série não pode afetá-lo;
* a alteração de templates não pode afetá-lo.

Não criar setter público do ATCUD se a arquitetura do domínio permitir evitá-lo.

A atribuição deve ocorrer apenas no processo de emissão.

---

## 14. Conteúdo fiscal do Código QR

O Código QR deve representar uma mensagem fiscal construída segundo as especificações aplicáveis.

A mensagem deve ser construída no backend a partir dos dados definitivos do documento.

Criar, salvo equivalente já existente:

```text
QrFiscalPayloadBuilder
```

Responsabilidade:

```text
Documento fiscal consolidado
→ texto fiscal do QR
```

O builder não deve gerar a imagem.

Deve gerar apenas o texto que será codificado.

---

## 15. Campos do payload QR

O payload deve considerar os campos aplicáveis ao documento, incluindo, nomeadamente:

* NIF do emitente;
* NIF ou identificação do adquirente;
* país do adquirente;
* tipo documental;
* estado do documento;
* data do documento;
* identificação única do documento;
* ATCUD;
* bases tributáveis;
* taxas de IVA;
* valores de IVA;
* montantes isentos;
* total de impostos;
* total do documento;
* retenções, quando aplicáveis;
* elementos do hash, quando aplicáveis;
* outros campos exigidos pela especificação técnica.

Não inventar códigos ou campos.

A composição deve seguir rigorosamente a especificação oficial utilizada pelo projeto.

---

## 16. Ordem e separação dos campos

Os campos devem:

* respeitar a ordem definida na especificação;
* utilizar os identificadores oficiais;
* usar o separador previsto;
* omitir ou preencher campos segundo as regras aplicáveis;
* manter formatação estável;
* evitar espaços ou caracteres adicionais não previstos.

Não utilizar JSON como conteúdo do QR.

Não utilizar uma estrutura proprietária do FAC.

---

## 17. Formatação monetária

Os valores monetários devem ser construídos com:

* `BigDecimal`;
* arredondamento explícito;
* escala coerente;
* ponto como separador decimal;
* ausência de separador de milhares;
* ausência de símbolo monetário.

Exemplo:

```text
1234.50
```

Não utilizar:

```text
1.234,50 €
```

Evitar qualquer conversão monetária com `double` ou `float`.

---

## 18. Datas

As datas incluídas no payload devem respeitar exatamente o formato exigido pela especificação.

A formatação deve ser feita centralmente.

Não utilizar o formato regional apresentado no frontend.

A data visual:

```text
15/06/2026
```

pode ser diferente do formato técnico utilizado no payload.

---

## 19. Cliente não identificado e cliente estrangeiro

O builder deve tratar corretamente:

* consumidor final;
* cliente português identificado;
* cliente estrangeiro;
* país diferente de Portugal;
* identificador fiscal estrangeiro;
* ausência legítima de NIF;
* regras específicas já existentes no FAC.

Não aplicar automaticamente o mesmo valor de NIF a todos os cenários.

Os casos devem ser cobertos por testes.

---

## 20. Taxas de IVA

O payload deve tratar corretamente documentos com:

* uma única taxa;
* várias taxas;
* taxa normal;
* taxa intermédia;
* taxa reduzida;
* operações isentas;
* diferentes regiões fiscais, quando aplicável;
* ausência de IVA em casos legalmente configurados.

Os valores devem resultar dos totais fiscais consolidados do documento.

Não recalcular os impostos no módulo de PDF.

---

## 21. Isenções

Quando existirem operações isentas:

* utilizar os campos adequados;
* respeitar o motivo de isenção já existente no documento;
* garantir coerência entre documento, totais e QR;
* rejeitar payloads incompletos quando a informação obrigatória não existir.

Não implementar nesta tarefa novos regimes de isenção que o FAC ainda não suporte.

---

## 22. Moeda estrangeira

Se o FAC já suportar documentos em moeda estrangeira, analisar as regras necessárias ao Código QR.

A implementação deve utilizar:

* a moeda do documento;
* a taxa de câmbio persistida;
* os valores fiscais relevantes;
* regras de conversão coerentes.

Não obter taxas de câmbio externas durante a impressão ou reimpressão.

Se o suporte a moeda estrangeira ainda não estiver suficientemente consolidado, documentar a limitação e bloquear a geração inadequada do QR.

---

## 23. Validador do payload

Criar, salvo equivalente existente:

```text
QrFiscalValidator
```

Responsabilidades mínimas:

* validar presença dos campos obrigatórios;
* validar ATCUD;
* validar identificação do documento;
* validar data;
* validar totais;
* validar coerência entre bases e impostos;
* validar formatação decimal;
* validar ordem estrutural;
* validar tipo documental;
* validar estado;
* validar tamanho do conteúdo, quando aplicável.

Um payload inválido deve impedir a emissão definitiva.

Mensagem funcional sugerida:

```text
Não foi possível emitir o documento porque os dados fiscais do Código QR são inválidos.
```

Os detalhes técnicos devem ficar no log.

---

## 24. Persistência do payload

O texto integral do QR deve ser persistido.

Adicionar ao documento:

```java
@Column(name = "qr_payload", columnDefinition = "TEXT")
private String qrPayload;
```

Pode também ser adicionada uma versão:

```java
@Column(name = "qr_payload_version", length = 20)
private String qrPayloadVersion;
```

A versão deve identificar a especificação ou formato utilizado.

Exemplo:

```text
AT-QR-1
```

Não utilizar uma versão inventada sem documentar claramente o seu significado.

---

## 25. Razão para persistir o payload

A persistência permite:

* reimpressão fiel;
* auditoria;
* testes;
* investigação de divergências;
* regeneração da imagem;
* independência da biblioteca gráfica;
* preservação histórica.

A reimpressão deve utilizar o payload originalmente guardado.

Não deve reconstruí-lo a partir dos dados atuais, salvo em documentos antigos explicitamente migrados e validados.

---

## 26. Imagem QR

Criar um componente gráfico, salvo equivalente existente:

```text
QrCodeImageGenerator
```

Responsabilidade:

```text
payload QR validado
→ imagem QR
```

Este componente não deve conhecer regras fiscais.

Deve apenas converter texto em imagem.

---

## 27. Biblioteca de QR

Verificar se o projeto já possui uma biblioteca adequada.

Se não possuir, escolher uma biblioteca Java:

* estável;
* bem mantida;
* compatível com a licença do FAC;
* adequada à geração de QR;
* compatível com o mecanismo de PDF utilizado.

Não adicionar várias bibliotecas para a mesma função.

Registar no relatório:

* biblioteca escolhida;
* versão;
* licença;
* motivo da escolha.

---

## 28. Parâmetros gráficos

A geração deve permitir controlar:

* correção de erro;
* dimensões;
* margem;
* resolução;
* formato da imagem;
* contraste;
* escalabilidade.

Utilizar os parâmetros exigidos pela especificação aplicável.

A imagem deve ser adequada tanto para:

* visualização em PDF;
* impressão física.

---

## 29. Integração no PDF

O PDF deve utilizar:

* o ATCUD persistido;
* o payload QR persistido;
* uma imagem gerada a partir desse payload.

O template não deve chamar builders fiscais nem repositories.

Fluxo correto:

```text
Documento emitido
→ leitura de atcud
→ leitura de qrPayload
→ geração da imagem
→ composição do PDF
```

---

## 30. Posicionamento do ATCUD

O ATCUD deve ser apresentado de forma legível.

Formato:

```text
ATCUD:ABCD1234-27
```

Na página onde estiver o Código QR, o ATCUD deve aparecer imediatamente acima do QR.

O ATCUD deve constar nas páginas exigidas pelas regras aplicáveis.

Analisar os templates atuais e implementar uma solução consistente para documentos com:

* uma página;
* várias páginas;
* cabeçalho;
* rodapé;
* última página com totais.

---

## 31. Posicionamento do Código QR

O Código QR deve ser colocado numa área estável do documento.

A posição deve:

* evitar sobreposição com linhas;
* evitar sobreposição com totais;
* manter margens de impressão;
* ser legível;
* permanecer estável em documentos longos;
* não criar páginas adicionais sem necessidade.

O Código QR deve ser colocado na primeira ou na última página, conforme a solução adotada e as regras aplicáveis.

A decisão deve ser documentada.

---

## 32. Tamanho físico

O tamanho deve ser controlado em medidas físicas adequadas ao PDF.

Não depender apenas de píxeis.

Garantir que alterações de resolução não reduzem o QR abaixo da dimensão tecnicamente adequada.

Testar:

* visualização a 100%;
* impressão A4;
* impressão com ajuste à página;
* PDF com várias páginas.

---

## 33. Documentos abrangidos

Aplicar ATCUD e Código QR apenas aos tipos documentais abrangidos e já suportados pelo FAC.

Analisar, entre outros:

* faturas;
* faturas simplificadas;
* faturas-recibo;
* notas de crédito;
* notas de débito;
* recibos;
* outros documentos fiscalmente relevantes existentes.

Não assumir que todos os documentos utilizam exatamente os mesmos campos fiscais.

Criar estratégias específicas por família documental quando necessário, evitando duplicação.

---

## 34. Documentos comerciais e financeiros

Se o FAC possuir modelos separados para documentos comerciais e financeiros, determinar:

* onde persistir o ATCUD;
* onde persistir o payload;
* quais campos fiscais estão disponíveis;
* como reutilizar componentes comuns;
* que diferenças existem entre faturas, notas e recibos.

Não forçar uma herança complexa apenas para esta tarefa.

Preferir componentes partilhados e interfaces pequenas.

---

## 35. Emissão definitiva

O processo de emissão deve ocorrer numa única transação lógica.

Sequência esperada:

```text
1. carregar documento em rascunho;
2. validar dados comerciais;
3. validar totais;
4. carregar e proteger a série;
5. obter o próximo número;
6. construir identificação documental;
7. gerar ATCUD;
8. consolidar dados fiscais;
9. construir payload QR;
10. validar payload;
11. persistir ATCUD e payload;
12. atualizar numerador;
13. mudar o documento para emitido;
14. confirmar transação.
```

Se qualquer passo falhar, a emissão deve ser revertida.

Não deixar documentos parcialmente emitidos.

---

## 36. Geração do PDF após emissão

A geração do PDF deve ocorrer apenas quando o documento estiver validamente emitido.

Se a geração gráfica do PDF falhar depois da emissão fiscal:

* o documento deve continuar emitido;
* o número não deve ser reutilizado;
* deve ser possível tentar gerar novamente o PDF;
* o ATCUD e o payload não devem ser alterados.

Distinguir:

```text
falha na emissão
```

de:

```text
falha na representação PDF
```

---

## 37. Rascunhos

Os rascunhos:

* não devem possuir ATCUD definitivo;
* não devem possuir QR fiscal definitivo;
* não devem consumir o numerador;
* não devem ser apresentados como documentos fiscais emitidos.

Se existir impressão de rascunho, deve conter uma indicação inequívoca:

```text
RASCUNHO
```

e não deve ser confundida com o documento definitivo.

---

## 38. Reimpressão

A reimpressão deve utilizar:

* o mesmo número;
* a mesma série;
* o mesmo ATCUD;
* o mesmo payload;
* os mesmos totais fiscais;
* a mesma data de emissão.

Não gerar um novo ATCUD.

Não incrementar a série.

Não reconstruir o payload segundo regras posteriores.

A imagem QR pode ser regenerada a partir do payload persistido.

---

## 39. Anulação

Confirmar o tratamento atual de anulações.

Um documento anulado deve conservar:

* número;
* série;
* ATCUD;
* payload original;
* histórico.

A representação PDF deve refletir o estado de anulação segundo as regras atuais do projeto.

Não reutilizar o número do documento anulado.

Não eliminar fisicamente o documento.

---

## 40. Documentos antigos

Podem existir documentos emitidos antes desta implementação.

Esses documentos poderão não possuir:

```text
atcud
qrPayload
```

A migração deve ser aditiva e compatível.

Não preencher automaticamente ATCUDs históricos sem uma base factual segura.

Para documentos antigos:

* permitir consulta;
* permitir comportamento controlado;
* não inventar códigos;
* assinalar ausência quando necessário;
* documentar a limitação.

Não tornar imediatamente as novas colunas `NOT NULL` na base de dados.

---

## 41. Migração da base de dados

As alterações devem ser aditivas.

Possíveis colunas:

```text
atcud
qr_payload
qr_payload_version
```

Antes de criar migrações:

* confirmar as tabelas corretas;
* confirmar se existem vários tipos de documentos;
* confirmar a estratégia atual de migração;
* verificar dados existentes;
* preservar a base.

Não:

* apagar tabelas;
* recriar a base;
* alterar a chave de `Serie`;
* limpar dados;
* alterar IDs;
* remover documentos existentes.

Não limpar a base sem autorização expressa.

---

## 42. API e DTOs

Os DTOs de resposta dos documentos podem passar a expor:

```text
atcud
temQrFiscal
```

O payload integral não deve ser exposto desnecessariamente ao frontend, salvo necessidade funcional concreta.

O frontend não precisa de conhecer a composição fiscal do QR.

O backend deve disponibilizar o PDF ou a imagem através dos mecanismos atuais.

Evitar alterações desnecessárias aos contratos REST.

---

## 43. Frontend

O frontend deve apenas:

* apresentar o ATCUD quando necessário;
* permitir visualizar ou descarregar o PDF;
* apresentar erros de emissão compreensíveis;
* distinguir rascunho de documento emitido.

Não deve:

* construir ATCUD;
* construir payload;
* gerar QR fiscal;
* alterar payload;
* recalcular totais.

Não reformular nesta tarefa a interface geral.

---

## 44. Logs e auditoria

Registar, quando compatível com a arquitetura atual:

* emissão do documento;
* série utilizada;
* número atribuído;
* ATCUD gerado;
* versão do payload;
* resultado da validação;
* erro de geração do PDF;
* reimpressão;
* anulação.

Não colocar no log informação pessoal desnecessária.

Não registar segredos ou credenciais.

---

## 45. Testes unitários do ATCUD

Criar testes para:

* código AT válido;
* código AT nulo;
* código AT vazio;
* código AT com espaços exteriores;
* número nulo;
* número zero;
* número negativo;
* número válido;
* resultado com hífen;
* ausência do prefixo visual.

Exemplo:

```text
codigoAt = ABCD1234
numero = 27
resultado = ABCD1234-27
```

---

## 46. Testes unitários do payload

Criar testes para os documentos já suportados.

Casos mínimos:

* fatura nacional;
* fatura simplificada;
* nota de crédito;
* recibo;
* consumidor final;
* cliente identificado;
* cliente estrangeiro;
* uma taxa de IVA;
* várias taxas;
* operação isenta;
* documento com retenção, se suportado;
* documento anulado;
* arredondamentos;
* valores com duas casas decimais.

Comparar o payload completo esperado quando for tecnicamente adequado.

---

## 47. Testes do validador

Testar:

* campo obrigatório em falta;
* ATCUD inválido;
* data inválida;
* total incoerente;
* base e imposto incoerentes;
* tipo documental desconhecido;
* ordem incorreta;
* formato decimal inválido;
* payload válido.

---

## 48. Testes da emissão

Criar ou atualizar testes de integração para:

* emissão completa;
* persistência do ATCUD;
* persistência do payload;
* incremento do numerador;
* rollback quando o payload falha;
* rollback quando a série não tem código AT;
* duas emissões simultâneas;
* emissão em séries diferentes;
* impossibilidade de emitir rascunho incompleto.

---

## 49. Testes de reimpressão

Confirmar que:

* o ATCUD não muda;
* o payload não muda;
* o numerador não muda;
* a imagem QR representa o mesmo conteúdo;
* o PDF continua disponível;
* alterações posteriores à série não alteram o documento.

---

## 50. Testes do PDF

Criar testes possíveis para confirmar:

* presença do texto `ATCUD:`;
* presença da imagem QR;
* geração sem erro;
* documentos de uma página;
* documentos de várias páginas;
* nota de crédito;
* recibo;
* documento anulado;
* reimpressão.

Quando não for possível validar visualmente por teste automático, criar testes estruturais e complementar com testes manuais.

---

## 51. Testes físicos

Realizar e documentar testes com:

* impressão A4;
* impressão a preto e branco;
* PDF no ecrã;
* diferentes leitores de QR;
* telemóvel Android;
* iPhone, quando disponível;
* documento de uma página;
* documento de várias páginas;
* ajuste automático à página;
* impressora de qualidade média.

O QR deve ser lido corretamente após:

```text
payload
→ imagem
→ PDF
→ impressão
→ leitura
```

---

## 52. Critérios de aceitação

A implementação considera-se concluída quando:

1. o ATCUD é gerado no backend;
2. o ATCUD utiliza o código AT da série;
3. o ATCUD utiliza o número sequencial correto;
4. o ATCUD é persistido no documento;
5. o payload QR é construído no backend;
6. o payload é validado antes da emissão;
7. o payload é persistido;
8. a imagem QR é gerada a partir do payload;
9. o PDF apresenta o ATCUD;
10. o PDF apresenta o QR;
11. o ATCUD fica imediatamente acima do QR na página respetiva;
12. documentos antigos continuam acessíveis;
13. reimpressões conservam ATCUD e payload;
14. anulações não reutilizam números;
15. a numeração suporta concorrência;
16. o React não contém lógica fiscal;
17. o PDF não recalcula impostos;
18. os testes unitários passam;
19. os testes de integração passam;
20. os testes físicos confirmam legibilidade;
21. a base de dados não é limpa;
22. não foi implementada comunicação automática à AT.

---

## 53. Ordem de implementação

Executar pela seguinte ordem:

### Fase 1

Inspeção do processo atual de emissão, modelos e PDF.

### Fase 2

Segurança da numeração e validação do código AT.

### Fase 3

Serviço de geração e persistência do ATCUD.

### Fase 4

Construção e validação do payload QR.

### Fase 5

Persistência do payload.

### Fase 6

Geração da imagem QR.

### Fase 7

Integração no PDF.

### Fase 8

Reimpressão e anulação.

### Fase 9

Testes automáticos.

### Fase 10

Testes físicos e relatório final.

Não iniciar uma fase posterior quando existirem falhas relevantes na fase anterior.

---

## 54. Resultado final solicitado ao Codex

No final, apresentar um relatório com:

1. ficheiros analisados;
2. ficheiros alterados;
3. ficheiros criados;
4. dependências adicionadas;
5. migrações criadas;
6. documentos abrangidos;
7. formato do ATCUD implementado;
8. versão da especificação QR utilizada;
9. estratégia de persistência;
10. mecanismo de segurança da numeração;
11. localização do ATCUD no PDF;
12. localização do QR no PDF;
13. tratamento de reimpressão;
14. tratamento de anulação;
15. testes criados;
16. resultado de todos os testes;
17. testes físicos ainda necessários;
18. limitações encontradas;
19. confirmação de preservação da base;
20. confirmação de que não foi implementada comunicação automática à AT.

---

## 55. Instrução final

Executa esta tarefa de forma incremental e compatível com o código existente.

Não reconstruas o módulo de documentos sem necessidade.

Não cries uma segunda entidade de séries.

Não alteres contratos REST ou o frontend salvo quando indispensável.

Não limpes a base de dados.

Não avances para comunicação automática à AT.

O objetivo desta tarefa é exclusivamente:

```text
série existente
→ número seguro
→ ATCUD persistido
→ payload QR persistido
→ QR legível no PDF
```
