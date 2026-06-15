# 07 - Extratos, Listagens e Totalizadores

## 1. Identificação do documento

**Projeto:** FAC
**Documento:** Extratos, Listagens e Totalizadores
**Versão:** 1.0
**Estado:** Basilar
**Âmbito:** Backend e frontend
**Objetivo:** Definir a estrutura funcional e técnica das listagens do FAC, tomando o Extrato de Cliente como primeira matriz de implementação.

---

## 2. Objetivo

O FAC deve possuir uma estrutura coerente e reutilizável para a apresentação de listagens financeiras, comerciais e de conta corrente.

A listagem **Extrato de Cliente** será a primeira implementação completa e deve servir de matriz funcional e visual para futuras listagens, nomeadamente:

* extrato de fornecedor;
* conta corrente de cliente;
* conta corrente de fornecedor;
* listagem de documentos;
* listagem de faturas;
* listagem de recebimentos;
* listagem de pagamentos;
* mapas de vendas;
* mapas de valores em aberto;
* mapas de vencimentos;
* outras listagens financeiras ou comerciais.

O objetivo não é criar, nesta fase, um motor genérico de reporting comparável a soluções como Crystal Reports ou JasperReports.

Pretende-se criar uma base simples, robusta e evolutiva, sem introduzir complexidade prematura.

---

## 3. Princípios gerais

As listagens do FAC devem respeitar os seguintes princípios:

1. Os resultados apresentados devem corresponder rigorosamente aos filtros selecionados.
2. Os valores anteriores ao período devem ser considerados quando influenciam o saldo.
3. Os movimentos devem ser apresentados de forma cronológica e auditável.
4. Os totais devem distinguir claramente:

    * valores anteriores;
    * valores do período;
    * valores acumulados finais.
5. A apresentação em React pode conter linhas de resumo, subtotais e totalizadores.
6. Não devem ser criadas entidades JPA apenas para suportar listagens.
7. O modelo de domínio não deve ser contaminado por necessidades exclusivamente visuais.
8. Os dados utilizados no ecrã e nas exportações devem ter a mesma origem.
9. Os cálculos contabilísticos devem ser coerentes entre backend, frontend, PDF e Excel.
10. A estrutura deve poder evoluir sem obrigar à reescrita completa de cada nova listagem.

---

# 4. Extrato de Cliente

## 4.1. Função

O Extrato de Cliente deve permitir consultar os movimentos de conta corrente de um cliente dentro de um determinado intervalo de datas.

A listagem deve mostrar:

* identificação do cliente;
* intervalo de datas selecionado;
* filtros aplicados;
* valores anteriores ao período;
* movimentos do período;
* saldo acumulado;
* totais do período;
* valores finais acumulados.

---

## 4.2. Filtros mínimos

O Extrato de Cliente deve suportar, pelo menos:

* cliente;
* data inicial;
* data final.

Poderão ser acrescentados posteriormente:

* tipo de documento;
* série;
* estado;
* estabelecimento;
* moeda;
* centro de custo;
* outros critérios relevantes.

Os filtros devem ser aplicados de forma consistente entre:

* listagem no ecrã;
* impressão;
* exportação para PDF;
* exportação para Excel.

---

# 5. Estrutura da listagem

## 5.1. Cabeçalho

O cabeçalho deve incluir:

* título da listagem;
* código do cliente;
* nome ou denominação do cliente;
* número de identificação fiscal, quando adequado;
* data inicial;
* data final;
* filtros adicionais aplicados;
* data e hora de geração, quando relevante.

---

## 5.2. Linha “Anterior”

Antes dos movimentos do período deve ser apresentada uma linha com a descrição:

**Anterior**

Esta linha representa os movimentos cuja data seja anterior à data inicial do filtro.

Devem ser apresentados:

* total a débito anterior;
* total a crédito anterior;
* saldo anterior.

Os movimentos individuais anteriores à data inicial não devem ser mostrados.

A linha “Anterior” funciona como saldo transportado para o início do período.

Exemplo:

| Data | Documento | Descrição |     Débito |  Crédito |    Saldo |
| ---- | --------- | --------- | ---------: | -------: | -------: |
|      |           | Anterior  | 1 500,00 € | 900,00 € | 600,00 € |

O saldo anterior deve respeitar a convenção de débito e crédito adotada no FAC.

---

## 5.3. Movimentos do período

Depois da linha “Anterior” devem ser apresentados todos os movimentos compreendidos entre:

* data inicial, inclusive;
* data final, inclusive.

Cada movimento deve apresentar, conforme aplicável:

* data;
* tipo de documento;
* série;
* número do documento;
* descrição;
* vencimento;
* débito;
* crédito;
* saldo acumulado.

O saldo acumulado do primeiro movimento deve partir do saldo anterior.

Os movimentos devem ser apresentados segundo uma ordenação estável.

A ordenação base deve ser:

1. data do movimento;
2. data de criação ou lançamento, quando aplicável;
3. identificador interno estável.

Esta regra evita alterações imprevisíveis na ordem quando existem vários movimentos na mesma data.

---

## 5.4. Total do período

Depois dos movimentos deve ser apresentada uma linha com a descrição:

**Total do período**

Esta linha deve incluir apenas os movimentos compreendidos no intervalo selecionado.

Deve apresentar:

* total a débito do período;
* total a crédito do período;
* saldo do período.

O saldo do período corresponde à diferença produzida exclusivamente pelos movimentos do intervalo.

Não deve incluir o saldo anterior.

Exemplo:

| Data | Documento | Descrição        |     Débito |    Crédito |    Saldo |
| ---- | --------- | ---------------- | ---------: | ---------: | -------: |
|      |           | Total do período | 2 000,00 € | 1 200,00 € | 800,00 € |

---

## 5.5. Total final

Depois do total do período deve ser apresentada uma linha com a descrição:

**Total final**

Esta linha representa o acumulado até à data final do filtro.

Deve apresentar:

* débito anterior mais débito do período;
* crédito anterior mais crédito do período;
* saldo final.

O saldo final deve corresponder a:

```text
Saldo final = Saldo anterior + Saldo do período
```

Exemplo:

| Data | Documento | Descrição   |     Débito |    Crédito |      Saldo |
| ---- | --------- | ----------- | ---------: | ---------: | ---------: |
|      |           | Total final | 3 500,00 € | 2 100,00 € | 1 400,00 € |

---

# 6. Convenções de cálculo

Devem existir conceptualmente os seguintes valores:

* débito anterior;
* crédito anterior;
* saldo anterior;
* débito do período;
* crédito do período;
* saldo do período;
* débito final;
* crédito final;
* saldo final.

As relações esperadas são:

```text
Débito final = Débito anterior + Débito do período
```

```text
Crédito final = Crédito anterior + Crédito do período
```

```text
Saldo final = Saldo anterior + Saldo do período
```

A fórmula exata do saldo deve respeitar a convenção contabilística definida no FAC.

Não deve ser assumido automaticamente, em todos os contextos, que:

```text
Saldo = Débito - Crédito
```

A convenção deve ser confirmada de acordo com o tipo de conta corrente e com a forma como os movimentos são atualmente registados.

---

# 7. Responsabilidades do backend e do frontend

## 7.1. Backend

O backend deve fornecer os dados necessários para construir o extrato.

Deve ser responsável, pelo menos, por:

* aplicar os filtros;
* obter os totais anteriores;
* obter os movimentos do período;
* garantir uma ordenação estável;
* respeitar a precisão monetária;
* assegurar a consistência dos dados;
* evitar duplicação ou omissão de movimentos;
* garantir que os movimentos nas datas-limite são corretamente incluídos.

O backend pode também fornecer os totais do período e os totais finais, sobretudo quando esses valores devam ser considerados oficiais.

---

## 7.2. Frontend React

O React pode apresentar e organizar:

* linha “Anterior”;
* movimentos;
* saldo acumulado;
* subtotais;
* total do período;
* total final;
* grupos e secções;
* linhas condicionais de resumo.

O React pode efetuar cálculos auxiliares simples para:

* apresentação imediata;
* validação visual;
* atualização da interface;
* conferência de consistência.

Contudo, o frontend não deve ser a única fonte de verdade para cálculos contabilísticos relevantes.

Sempre que os totais sejam utilizados em documentos oficiais, exportações ou auditoria, devem ser obtidos ou validados pelo backend.

---

# 8. Entidades, DTOs e projeções

## 8.1. Regra fundamental

Não devem ser criadas entidades JPA específicas apenas para suportar:

* listagens;
* totalizadores;
* cabeçalhos de relatório;
* rodapés;
* linhas de resumo;
* exportações;
* apresentação no frontend.

Uma entidade JPA deve representar um conceito persistente do domínio.

Uma listagem é uma representação ou consulta sobre o domínio, não uma nova entidade de negócio.

---

## 8.2. Soluções admitidas

Para obter e transportar dados de listagens podem ser utilizados:

* DTOs;
* records Java;
* projections do Spring Data;
* consultas JPQL;
* consultas nativas;
* agregações SQL;
* modelos TypeScript no frontend.

Exemplo de projeção para valores anteriores:

```java
public interface ExtratoAnteriorProjection {

    BigDecimal getDebitoAnterior();

    BigDecimal getCreditoAnterior();
}
```

Exemplo de DTO simples:

```java
public record ExtratoAnteriorDto(
    BigDecimal debito,
    BigDecimal credito,
    BigDecimal saldo
) {
}
```

Exemplo de linha de extrato:

```java
public record ExtratoClienteLinhaDto(
    Long id,
    LocalDate data,
    String tipoDocumento,
    String numeroDocumento,
    String descricao,
    BigDecimal debito,
    BigDecimal credito
) {
}
```

Estes objetos:

* não são entidades;
* não criam tabelas;
* não são geridos pelo ciclo de vida do Hibernate;
* servem apenas para consulta, transporte ou apresentação.

---

# 9. Estrutura recomendada da resposta

A resposta da API pode ser simples e adaptada à implementação atual.

Exemplo:

```java
public record ExtratoClienteResponse(
    ExtratoClienteAnteriorDto anterior,
    List<ExtratoClienteLinhaDto> movimentos,
    ExtratoClienteTotaisDto totaisPeriodo,
    ExtratoClienteTotaisDto totaisFinais
) {
}
```

Exemplo de totais:

```java
public record ExtratoClienteTotaisDto(
    BigDecimal debito,
    BigDecimal credito,
    BigDecimal saldo
) {
}
```

Esta estrutura é apenas uma possibilidade.

Antes de criar novos DTOs, deve ser analisado o código atual, procurando:

* reutilizar DTOs existentes;
* evitar duplicação;
* manter nomes coerentes;
* não introduzir abstrações desnecessárias.

---

# 10. Totalizadores no React

Os totalizadores podem ser apresentados diretamente dentro da tabela React.

Exemplo conceptual:

```tsx
<tbody>
  <PreviousRow totals={statement.previous} />

  {statement.movements.map((movement) => (
    <StatementMovementRow
      key={movement.id}
      movement={movement}
    />
  ))}

  <PeriodTotalRow totals={statement.periodTotals} />

  <FinalTotalRow totals={statement.finalTotals} />
</tbody>
```

Os totalizadores devem ser visualmente distinguíveis dos movimentos normais.

A diferenciação pode recorrer a:

* tipografia;
* peso da fonte;
* fundo ligeiramente diferente;
* separadores;
* espaçamento;
* alinhamento;
* bordas discretas.

A solução deve respeitar a linguagem visual do FAC:

* suave;
* profissional;
* pouco agressiva;
* com contraste suficiente para leitura;
* sem excesso de cor.

---

# 11. Agrupamentos e subtotais

A arquitetura deve permitir futuramente totalizadores por grupo.

Exemplos:

* mês;
* tipo de documento;
* série;
* estabelecimento;
* vendedor;
* cliente;
* fornecedor;
* estado do documento;
* centro de custo.

Exemplo conceptual:

```text
Janeiro de 2026
  Movimento 1
  Movimento 2
  Total de janeiro

Fevereiro de 2026
  Movimento 3
  Movimento 4
  Total de fevereiro

Total do período
Total final
```

Não deve, contudo, ser criado nesta fase um sistema genérico complexo de grupos e fórmulas.

O primeiro objetivo é implementar corretamente:

* anterior;
* movimentos;
* total do período;
* total final.

A generalização deve ocorrer apenas depois de existir uma segunda ou terceira listagem que demonstre necessidades comuns.

---

# 12. Precisão monetária

No backend Java, os valores monetários devem utilizar:

```java
BigDecimal
```

Não devem ser utilizados:

```java
double
```

ou:

```java
float
```

Os cálculos devem definir explicitamente:

* escala;
* modo de arredondamento;
* moeda;
* número de casas decimais.

No frontend, deve existir uma função central de formatação monetária.

Exemplo:

```ts
const formatCurrency = (value: number) =>
  new Intl.NumberFormat("pt-PT", {
    style: "currency",
    currency: "EUR",
  }).format(value);
```

A utilização de números JavaScript deve ser limitada à apresentação e a cálculos auxiliares simples.

---

# 13. Casos especiais

A implementação deve tratar corretamente:

## 13.1. Sem valores anteriores

A linha “Anterior” pode ser apresentada com zeros ou omitida, conforme decisão visual global.

Por coerência e auditabilidade, recomenda-se a sua apresentação, mesmo quando os valores são zero.

---

## 13.2. Sem movimentos no período

Se existirem valores anteriores, mas não existirem movimentos no período:

* a linha “Anterior” deve ser apresentada;
* deve ser apresentada uma indicação de ausência de movimentos;
* o total do período deve ser zero;
* o total final deve coincidir com o anterior.

---

## 13.3. Cliente sem qualquer movimento

A listagem deve indicar claramente:

```text
Não existem movimentos para os critérios selecionados.
```

Os totais podem ser apresentados a zero.

---

## 13.4. Estornos e notas de crédito

Os movimentos de correção devem respeitar a natureza contabilística definida no sistema.

Não deve ser corrigido artificialmente o sinal apenas para facilitar a apresentação.

---

## 13.5. Movimentos nas datas-limite

Devem ser incluídos:

* movimentos na data inicial;
* movimentos na data final.

A condição temporal deve ser equivalente a:

```text
data >= dataInicial
e
data <= dataFinal
```

Os movimentos anteriores devem respeitar:

```text
data < dataInicial
```

---

## 13.6. Datas e horas

Quando o movimento possuir data e hora, deve ser evitada a exclusão acidental dos movimentos da data final.

A filtragem deve tratar corretamente o intervalo completo do dia.

---

# 14. Desempenho

A listagem deve evitar:

* carregar todos os movimentos históricos para o frontend;
* calcular o anterior percorrendo todos os registos em memória;
* efetuar uma consulta por movimento;
* gerar problemas de N+1;
* carregar entidades completas quando apenas algumas colunas são necessárias.

Os valores anteriores devem ser preferencialmente obtidos através de uma consulta agregada.

Os movimentos do período devem ser obtidos através de uma consulta específica e ordenada.

---

# 15. Paginação

A paginação em ecrã deve ser analisada cuidadosamente.

Em extratos, o saldo acumulado depende dos movimentos anteriores da própria listagem.

Quando existir paginação, o saldo inicial de cada página deve considerar:

* o saldo anterior ao período;
* os movimentos das páginas anteriores.

A paginação não pode fazer com que o saldo acumulado reinicie incorretamente.

Possíveis estratégias:

1. o backend devolve o saldo acumulado de cada linha;
2. o backend devolve o saldo inicial da página;
3. o frontend mantém o acumulado sobre todos os dados já carregados;
4. a paginação é usada apenas visualmente quando o volume é moderado.

A estratégia deve ser escolhida após análise do volume real esperado.

---

# 16. Testes

Devem existir testes para:

1. cliente sem movimentos;
2. cliente com movimentos apenas anteriores;
3. cliente com movimentos apenas no período;
4. cliente com movimentos anteriores e no período;
5. movimentos na data inicial;
6. movimentos na data final;
7. vários movimentos na mesma data;
8. notas de crédito;
9. estornos;
10. valores com casas decimais;
11. total de débito anterior;
12. total de crédito anterior;
13. saldo anterior;
14. total a débito do período;
15. total a crédito do período;
16. saldo do período;
17. débito final;
18. crédito final;
19. saldo final;
20. ordenação estável;
21. coerência entre totais e movimentos;
22. ausência de duplicação de movimentos.

---

# 17. Aplicação às restantes listagens

Depois da consolidação do Extrato de Cliente, devem ser identificados os componentes reutilizáveis.

Podem vir a ser reutilizados:

* barra de filtros;
* indicação do período;
* cabeçalho da listagem;
* tabela;
* alinhamento monetário;
* formatação de datas;
* linhas de resumo;
* botões de exportação;
* estado de carregamento;
* estado sem resultados;
* tratamento de erros;
* metadados dos filtros.

A reutilização deve resultar de necessidades reais observadas.

Não deve ser construída antecipadamente uma framework universal de listagens.

---

# 18. Processo de implementação

Antes de alterar o código, deve ser feita uma análise da implementação atual.

O processo recomendado é:

1. identificar o controller atual;
2. identificar o service atual;
3. identificar os repositories envolvidos;
4. identificar os DTOs existentes;
5. identificar o componente React atual;
6. verificar como são calculados débito, crédito e saldo;
7. confirmar a convenção do sinal;
8. identificar a forma atual de filtragem por datas;
9. propor as alterações por ficheiro;
10. implementar por etapas;
11. executar testes;
12. validar visualmente a listagem.

---

# 19. Fases de desenvolvimento

## Fase 1

Implementar ou corrigir:

* filtros por data;
* linha “Anterior”;
* movimentos do período;
* saldo acumulado;
* total do período;
* total final.

## Fase 2

Consolidar:

* DTOs ou projections;
* consultas agregadas;
* precisão monetária;
* ordenação estável;
* testes.

## Fase 3

Melhorar a apresentação React:

* hierarquia visual;
* alinhamentos;
* estados vazios;
* carregamento;
* tratamento de erros;
* responsividade.

## Fase 4

Aplicar os padrões estabilizados a novas listagens.

---

# 20. Critérios de aceitação

A implementação é considerada concluída quando:

* a linha “Anterior” é corretamente calculada;
* os movimentos respeitam integralmente o filtro;
* os movimentos nas datas-limite são incluídos;
* o saldo acumulado parte do anterior;
* os totais do período não incluem o anterior;
* os totais finais incluem o anterior;
* não são criadas entidades JPA artificiais;
* os valores monetários utilizam precisão adequada;
* os testes principais passam;
* a interface distingue claramente movimentos e totais;
* a estrutura pode ser reutilizada noutras listagens sem duplicação excessiva.
