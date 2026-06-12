# FAC - Reporting

## Estado do Documento

Versão: 1.0
Data: 12-06-2026
Estado: Ativo
Autor: António Rosário

---

# 1. Objetivo

O reporting constitui um dos pilares fundamentais do FAC.

A qualidade dos relatórios deverá receber o mesmo nível de atenção que a modelação de dados, a experiência de utilização e os requisitos de certificação fiscal.

O objetivo do módulo de reporting é disponibilizar informação clara, consistente e facilmente utilizável para:

* Operação diária;
* Gestão do negócio;
* Cumprimento fiscal;
* Comunicação com clientes;
* Arquivo documental.

---

# 2. Princípios Fundamentais

## 2.1 Clareza acima de quantidade

Um relatório não é melhor por apresentar mais informação.

Um relatório é melhor quando permite ao utilizador encontrar rapidamente a informação relevante.

A inclusão de informação adicional deverá ser cuidadosamente avaliada face ao impacto na legibilidade.

---

## 2.2 Papel continua a ser importante

Todos os relatórios devem ser concebidos considerando simultaneamente:

* Visualização em ecrã;
* Exportação PDF;
* Impressão em papel.

A impressão não deverá ser considerada uma preocupação secundária.

---

## 2.3 Relatórios orientados ao objetivo

Cada relatório deve existir para responder a uma necessidade concreta.

Devem evitar-se relatórios genéricos que tentem servir todos os cenários.

Sempre que necessário deverão existir versões distintas:

* Resumida;
* Detalhada;
* Analítica.

---

## 2.4 Economia de papel

Antes de adicionar uma coluna ou secção a um relatório deverá ser avaliado o impacto na paginação.

Uma única coluna adicional não deverá transformar um relatório de duas páginas num relatório de dezenas de páginas.

Informação raramente utilizada deverá ser disponibilizada através de:

* Versões detalhadas;
* Exportações;
* Consultas específicas.

---

## 2.5 Consistência

Todos os relatórios deverão apresentar:

* Estrutura uniforme;
* Cabeçalhos consistentes;
* Rodapés consistentes;
* Formatação homogénea;
* Identidade visual comum.

---

# 3. Conceitos Fundamentais

O FAC distingue três conceitos diferentes.

## Consulta

Ferramenta destinada à exploração da informação.

Características:

* Pesquisa;
* Filtros;
* Ordenação;
* Paginação.

Objetivo:

Consultar informação.

Não substitui relatórios.

---

## Exportação

Ferramenta destinada ao tratamento de dados.

Formatos:

* Excel;
* CSV.

Objetivo:

Permitir análise externa e manipulação de informação.

Não substitui relatórios.

---

## Relatório

Documento destinado à apresentação formal da informação.

Formatos:

* PDF;
* Impressão.

Objetivo:

Leitura, arquivo, partilha e impressão.

---

# 4. Categorias de Relatórios

## 4.1 Documentos Comerciais

Incluem:

* Faturas;
* Faturas-recibo;
* Recibos;
* Notas de crédito;
* Orçamentos;
* Proformas.

Estes documentos devem possuir apresentação profissional e cumprir todos os requisitos legais aplicáveis.

---

## 4.2 Relatórios Operacionais

Incluem, entre outros:

* Conta corrente;
* Extrato de cliente;
* Pendentes;
* Recebimentos;
* Vendas por período;
* Vendas por cliente;
* Vendas por artigo.

---

## 4.3 Relatórios Fiscais

Incluem, entre outros:

* Mapas de IVA;
* Resumos fiscais;
* Informação de suporte ao SAF-T;
* Outros mapas exigidos por obrigações legais.

---

# 5. Filosofia de Construção

## Versão Resumida

Sempre que aplicável deverá existir uma versão resumida.

Objetivos:

* Impressão eficiente;
* Consulta rápida;
* Menor consumo de papel.

---

## Versão Detalhada

Sempre que necessário poderá existir uma versão detalhada.

Objetivos:

* Auditoria;
* Investigação;
* Análise aprofundada.

---

# 6. Exportação Universal

Sempre que tecnicamente viável, as consultas do sistema deverão disponibilizar exportação direta para:

* Excel;
* CSV.

A exportação deverá refletir os filtros aplicados pelo utilizador.

---

# 7. Arquitetura

O módulo de reporting deverá ser independente da lógica de negócio.

Princípios:

* Separação entre dados e apresentação;
* Utilização de DTOs específicos para reporting;
* Independência relativamente ao motor de geração documental;
* Possibilidade de substituição futura da tecnologia de reporting.

---

# 8. Tecnologias

A escolha inicial do motor de reporting deverá privilegiar:

* Robustez;
* Maturidade;
* Integração com Java e Spring Boot;
* Independência tecnológica.

A tecnologia escolhida não deverá condicionar a evolução futura do sistema.

---

# 9. Critério de Qualidade

Um relatório apenas será considerado concluído quando cumprir simultaneamente:

* Informação correta;
* Apresentação clara;
* Paginação adequada;
* Boa legibilidade em A4;
* Boa legibilidade em PDF;
* Boa legibilidade em ecrã.

---

# 10. Regra de Ouro

Quando existir conflito entre:

* acrescentar mais informação;

ou

* preservar a legibilidade;

a decisão padrão deverá ser:

**preservar a legibilidade.**

O utilizador deve encontrar rapidamente a informação necessária sem desperdiçar tempo, papel ou recursos.
