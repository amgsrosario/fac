# 08 - Exportação de Listagens para PDF e Excel

## 1. Identificação do documento

**Projeto:** FAC
**Documento:** Exportação de Listagens para PDF e Excel
**Versão:** 1.0
**Estado:** Basilar
**Âmbito:** Backend, frontend e reporting
**Objetivo:** Definir a estratégia técnica e funcional para exportar as listagens do FAC para PDF e Excel.

---

## 2. Objetivo

O FAC deve permitir a exportação das principais listagens para:

* PDF;
* Excel, em formato `.xlsx`.

O **Extrato de Cliente** será a primeira listagem a suportar estas exportações.

A implementação deverá servir de referência para futuras listagens, sem criar desde já uma framework excessivamente abstrata.

As exportações devem preservar:

* filtros;
* ordenação;
* movimentos;
* valores anteriores;
* subtotais;
* totais do período;
* totais finais;
* identificação da empresa;
* identificação da entidade consultada.

---

# 3. Princípios gerais

As exportações devem respeitar os seguintes princípios:

1. O PDF e o Excel devem representar os mesmos dados apresentados na listagem.
2. Os cálculos não devem ser reimplementados de forma divergente em cada formato.
3. Os dados exportados devem ter a mesma origem dos dados apresentados no ecrã.
4. O PDF deve ser orientado à leitura, arquivo e impressão.
5. O Excel deve ser orientado à análise, filtragem e reutilização dos dados.
6. A exportação não deve depender de capturas de ecrã.
7. Os ficheiros devem ser gerados com nomes claros e previsíveis.
8. As bibliotecas utilizadas devem ser adequadas a utilização comercial.
9. Devem ser evitadas dependências com licenciamento incompatível com a comercialização do FAC.
10. A arquitetura deve permitir acrescentar novas listagens de forma progressiva.

---

# 4. Fonte comum dos dados

A exportação deve utilizar os mesmos dados e regras aplicados à listagem.

O fluxo conceptual deve ser:

```text
Base de dados
      ↓
Serviço de consulta
      ↓
Dados estruturados da listagem
      ↓
Apresentação React
      ↓
Exportação PDF
      ↓
Exportação Excel
```

O React, o PDF e o Excel não devem possuir versões independentes das regras contabilísticas.

Os seguintes valores devem ser coerentes em todos os formatos:

* débito anterior;
* crédito anterior;
* saldo anterior;
* débito do período;
* crédito do período;
* saldo do período;
* débito final;
* crédito final;
* saldo final.

---

# 5. Responsabilidade da geração

## 5.1. Geração no backend

A abordagem preferencial para o FAC é a geração de PDF e Excel no backend.

Vantagens:

* maior controlo;
* resultados previsíveis;
* possibilidade de testes automatizados;
* menor dependência do browser;
* reutilização das regras existentes;
* melhor capacidade de arquivo;
* maior consistência entre utilizadores;
* proteção das regras de negócio;
* possibilidade futura de envio por email ou armazenamento.

---

## 5.2. Papel do frontend

O frontend deve:

* recolher os filtros atuais;
* solicitar a exportação;
* apresentar estado de processamento;
* receber o ficheiro;
* iniciar a transferência;
* apresentar mensagens de erro;
* impedir cliques repetidos durante o processamento.

O frontend não deve gerar o PDF através de uma simples captura do HTML apresentado no browser.

---

# 6. Exportação para PDF

## 6.1. Finalidade

O PDF deve constituir uma versão:

* imprimível;
* arquivável;
* visualmente estável;
* adequada a envio para clientes;
* independente do browser utilizado.

---

## 6.2. Conteúdo mínimo

O PDF do Extrato de Cliente deve incluir:

* identificação da empresa emitente;
* logótipo, quando disponível;
* título “Extrato de Cliente”;
* identificação do cliente;
* número de identificação fiscal;
* período selecionado;
* filtros aplicados;
* data e hora de emissão;
* cabeçalho das colunas;
* linha “Anterior”;
* movimentos do período;
* total do período;
* total final;
* número da página;
* total de páginas, se tecnicamente adequado.

---

## 6.3. Estrutura sugerida

### Cabeçalho do documento

Deve incluir:

* logótipo;
* nome da empresa;
* NIF;
* morada;
* contactos, quando aplicável;
* título da listagem.

### Identificação do cliente

Deve incluir:

* código;
* nome ou denominação;
* NIF;
* morada, quando adequado.

### Critérios

Deve incluir:

* data inicial;
* data final;
* outros filtros relevantes.

### Corpo

Deve apresentar:

* anterior;
* movimentos;
* total do período;
* total final.

### Rodapé

Pode incluir:

* data de geração;
* nome da aplicação;
* número da página;
* total de páginas;
* indicação de documento informativo, quando aplicável.

---

# 7. Paginação do PDF

A paginação é uma das áreas mais sensíveis do reporting.

O PDF deve:

* repetir o cabeçalho das colunas em cada página;
* evitar cortar linhas entre páginas;
* evitar páginas quase vazias;
* manter totais próximos da secção a que pertencem;
* impedir que o total final fique isolado de forma inadequada;
* controlar margens;
* respeitar a área imprimível;
* utilizar orientação adequada.

Sempre que possível, o PDF deve usar formato:

```text
A4 vertical
```

A orientação horizontal deve ser utilizada apenas quando a largura das colunas o justificar.

---

## 7.1. Largura das colunas

As colunas devem ter larguras definidas de acordo com a natureza dos dados.

Exemplo:

* data: largura curta;
* documento: largura média;
* descrição: largura flexível;
* débito: largura monetária;
* crédito: largura monetária;
* saldo: largura monetária.

As colunas monetárias devem ser alinhadas à direita.

As descrições devem permitir quebra de linha quando necessário.

Não deve ser permitida a criação de páginas adicionais por causa de uma coluna residual ou desnecessária.

---

## 7.2. Repetição do cabeçalho

O cabeçalho da tabela deve repetir-se em todas as páginas.

A repetição deve incluir:

* títulos das colunas;
* identificação suficiente da listagem;
* período ou cliente, quando necessário para leitura independente da página.

---

## 7.3. Totais no PDF

As linhas de totais devem ser visualmente diferenciadas.

Devem ser apresentadas:

* linha “Anterior”;
* linha “Total do período”;
* linha “Total final”.

Os totais devem:

* utilizar tipografia adequada;
* manter alinhamento com as colunas;
* evitar cores excessivas;
* permanecer legíveis em impressão a preto e branco.

---

# 8. Exportação para Excel

## 8.1. Finalidade

O Excel deve ser um ficheiro de trabalho verdadeiro.

Não deve ser uma reprodução visual estática do PDF.

O utilizador deve poder:

* ordenar;
* filtrar;
* copiar;
* analisar;
* acrescentar fórmulas;
* criar tabelas dinâmicas;
* reutilizar os dados.

---

## 8.2. Formato

O formato principal deve ser:

```text
.xlsx
```

Não deve ser utilizado `.xls`, salvo necessidade futura expressamente identificada.

---

## 8.3. Conteúdo mínimo

O ficheiro Excel deve incluir:

* título da listagem;
* identificação do cliente;
* período selecionado;
* filtros aplicados;
* data de emissão;
* cabeçalhos;
* linha “Anterior”;
* movimentos;
* total do período;
* total final.

---

# 9. Tipos de dados no Excel

Os valores devem ser exportados com tipos corretos.

## 9.1. Datas

As datas devem ser gravadas como datas Excel.

Não devem ser exportadas apenas como texto.

Formato visual sugerido:

```text
dd/mm/yyyy
```

---

## 9.2. Valores monetários

Os valores monetários devem ser gravados como números.

Não devem ser exportados como texto contendo:

```text
€
```

O símbolo monetário deve ser aplicado através da formatação da célula.

Exemplo de valor interno:

```text
1250.50
```

Exemplo de apresentação:

```text
1 250,50 €
```

---

## 9.3. Textos

Campos como:

* número de documento;
* série;
* código postal;
* NIF;
* referências;

devem ser analisados antes de serem tratados como números.

Alguns destes campos devem permanecer como texto para evitar:

* perda de zeros à esquerda;
* conversões automáticas;
* notação científica;
* alterações de formato.

---

# 10. Fórmulas no Excel

Existem duas abordagens possíveis para os totalizadores.

## 10.1. Valores calculados pelo FAC

O FAC calcula os totais e grava os valores nas células.

Vantagens:

* consistência com a aplicação;
* maior controlo;
* resultado oficial;
* menor dependência da interpretação do Excel.

---

## 10.2. Fórmulas Excel

O ficheiro pode utilizar fórmulas como:

```excel
=SUM(D8:D35)
```

Vantagens:

* o total é atualizado se o utilizador alterar os valores;
* maior interatividade.

Desvantagens:

* o ficheiro deixa de representar exclusivamente o resultado oficial;
* alterações manuais podem modificar os totais;
* podem existir diferenças de arredondamento.

---

## 10.3. Orientação recomendada

Na primeira versão, os totais devem ser preferencialmente calculados pelo FAC.

As fórmulas Excel poderão ser acrescentadas futuramente como complemento ou validação.

---

# 11. Estrutura da folha Excel

A primeira folha poderá ser designada:

```text
Extrato Cliente
```

A estrutura sugerida é:

```text
Linha 1: Título
Linha 2: Empresa
Linha 3: Cliente
Linha 4: NIF
Linha 5: Período
Linha 6: Filtros
Linha 7: Emissão
Linha 8: Espaço
Linha 9: Cabeçalhos
Linha 10: Anterior
Linhas seguintes: Movimentos
Penúltimas linhas: Total do período
Última linha: Total final
```

A estrutura deve ser suficientemente simples para análise automática.

Devem ser evitadas células fundidas na área dos dados.

As células fundidas podem ser usadas apenas no título ou cabeçalho institucional, quando acrescentem valor.

---

# 12. Formatação do Excel

O ficheiro deve possuir:

* cabeçalho destacado;
* primeira linha de dados claramente identificada;
* alinhamento adequado;
* formatação de datas;
* formatação monetária;
* larguras de coluna razoáveis;
* quebra de texto na descrição;
* linhas de totais diferenciadas;
* congelamento do cabeçalho;
* autofiltro, quando adequado.

Exemplo de congelamento:

```text
Congelar painéis abaixo do cabeçalho da tabela
```

O autofiltro deve ser aplicado apenas às linhas de movimentos, evitando incluir linhas de totais quando isso prejudique a utilização.

---

# 13. Nomes dos ficheiros

Os nomes devem ser claros e não conter caracteres inválidos.

Formato sugerido para PDF:

```text
extrato-cliente-{codigo}-{dataInicial}-{dataFinal}.pdf
```

Formato sugerido para Excel:

```text
extrato-cliente-{codigo}-{dataInicial}-{dataFinal}.xlsx
```

Exemplo:

```text
extrato-cliente-000125-2026-01-01-2026-06-30.pdf
```

Devem ser tratados:

* espaços;
* barras;
* acentos;
* caracteres especiais;
* nomes excessivamente longos.

---

# 14. Endpoints

A API pode disponibilizar endpoints semelhantes a:

```http
GET /api/clientes/{clienteId}/extrato
```

```http
GET /api/clientes/{clienteId}/extrato/exportar/pdf
```

```http
GET /api/clientes/{clienteId}/extrato/exportar/xlsx
```

Todos os endpoints devem aceitar os mesmos filtros.

Exemplo:

```http
GET /api/clientes/125/extrato/exportar/pdf?dataInicial=2026-01-01&dataFinal=2026-06-30
```

---

## 14.1. Resposta PDF

O endpoint deve devolver:

```http
Content-Type: application/pdf
```

e:

```http
Content-Disposition: attachment; filename="extrato-cliente-000125-2026-01-01-2026-06-30.pdf"
```

---

## 14.2. Resposta Excel

O endpoint deve devolver:

```http
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
```

e:

```http
Content-Disposition: attachment; filename="extrato-cliente-000125-2026-01-01-2026-06-30.xlsx"
```

---

# 15. Arquitetura recomendada

A implementação deve evitar classes genéricas prematuras.

Pode começar com uma organização semelhante a:

```text
reporting/
├── extrato/
│   ├── ExtratoClienteDataService
│   ├── ExtratoClientePdfExporter
│   ├── ExtratoClienteExcelExporter
│   └── dto/
├── pdf/
│   └── utilitários comuns
├── excel/
│   └── utilitários comuns
└── formatter/
    ├── DateFormatter
    ├── CurrencyFormatter
    └── FileNameSanitizer
```

Esta estrutura é apenas indicativa.

Deve ser adaptada à organização atual do projeto.

---

## 15.1. Evitar abstração excessiva

Não devem ser criadas antecipadamente interfaces como:

```text
UniversalReportEngine
GenericSectionBuilder
DynamicReportFormula
ReportExpressionLanguage
GenericReportGroup
```

A primeira implementação deve resolver bem o Extrato de Cliente.

Os elementos comuns devem ser extraídos apenas quando forem utilizados por mais do que uma listagem.

---

# 16. Bibliotecas

## 16.1. Excel

Para Excel, deve ser considerada preferencialmente:

```text
Apache POI
```

Razões:

* biblioteca consolidada;
* suporte a `.xlsx`;
* formatação de células;
* fórmulas;
* filtros;
* congelamento de painéis;
* larguras de coluna;
* estilos;
* ampla utilização no ecossistema Java.

Deve ser verificada a versão compatível com a versão atual do Spring Boot e Java utilizada no FAC.

---

## 16.2. PDF

A biblioteca PDF deve ser selecionada após análise de:

* licenciamento;
* utilização comercial;
* tabelas multipágina;
* cabeçalhos;
* rodapés;
* numeração de páginas;
* controlo de margens;
* fontes;
* desempenho;
* manutenção do projeto.

Devem ser avaliadas soluções como:

* OpenPDF;
* Apache PDFBox;
* Flying Saucer com HTML e CSS;
* OpenHTMLtoPDF;
* JasperReports, caso futuramente se justifique;
* outras opções compatíveis com utilização comercial.

Não deve ser introduzida uma biblioteca estrutural sem apresentar previamente:

* licença;
* vantagens;
* limitações;
* impacto no projeto;
* esforço de manutenção;
* adequação ao FAC.

---

# 17. Estratégias possíveis para PDF

## 17.1. Construção programática

O PDF é construído diretamente através de uma biblioteca Java.

Vantagens:

* controlo preciso;
* bom desempenho;
* independência de HTML;
* comportamento previsível.

Desvantagens:

* mais código;
* maior esforço visual;
* manutenção mais trabalhosa.

---

## 17.2. HTML para PDF

O backend gera HTML e converte-o para PDF.

Vantagens:

* reutilização de conhecimentos de HTML e CSS;
* construção visual mais simples;
* facilidade na criação de templates.

Desvantagens:

* suporte CSS limitado;
* paginação mais delicada;
* diferenças face ao browser;
* necessidade de testar o motor de conversão.

---

## 17.3. Motor de reporting

Pode ser utilizado JasperReports ou solução semelhante.

Vantagens:

* grupos;
* subtotais;
* cabeçalhos;
* rodapés;
* paginação;
* templates;
* funcionalidades próximas do Crystal Reports.

Desvantagens:

* maior complexidade;
* curva de aprendizagem;
* manutenção de templates;
* integração adicional;
* eventual peso excessivo para a fase atual do FAC.

---

## 17.4. Orientação inicial

Para a primeira implementação, deve ser escolhida uma solução que:

* produza A4 corretamente;
* suporte tabelas multipágina;
* repita cabeçalhos;
* controle larguras;
* não possua limitações comerciais;
* não introduza complexidade desnecessária.

A decisão deve ser tomada depois de criar uma prova técnica com o Extrato de Cliente.

---

# 18. Interface do utilizador

Na listagem devem existir ações:

* Exportar PDF;
* Exportar Excel.

Os botões devem:

* respeitar os filtros atuais;
* ficar desativados durante o processamento;
* indicar que o ficheiro está a ser gerado;
* apresentar erro compreensível;
* manter o utilizador na listagem;
* não limpar os filtros;
* não recarregar desnecessariamente a página.

Exemplo conceptual:

```tsx
<Button
  label="Exportar PDF"
  icon="pi pi-file-pdf"
  loading={exportingPdf}
  onClick={handleExportPdf}
/>

<Button
  label="Exportar Excel"
  icon="pi pi-file-excel"
  loading={exportingExcel}
  onClick={handleExportExcel}
/>
```

A apresentação deve seguir a linguagem visual suave do FAC.

---

# 19. Segurança

A exportação deve respeitar as mesmas permissões da listagem.

O utilizador não deve conseguir exportar dados a que não tenha acesso.

O backend deve validar:

* utilizador autenticado;
* empresa ou tenant;
* permissões;
* acesso ao cliente;
* filtros;
* parâmetros;
* limites de volume.

Não deve ser confiado ao frontend o controlo de acesso.

---

# 20. Desempenho e limites

A geração de ficheiros pode consumir memória e processamento.

Devem ser considerados:

* número máximo de linhas;
* paginação ou streaming;
* dimensão máxima do ficheiro;
* tempo limite;
* utilização de memória;
* concorrência;
* ficheiros temporários;
* limpeza de recursos.

Para volumes normais, a geração pode ser síncrona.

Se futuramente existirem exportações muito grandes, poderá ser analisada uma solução assíncrona.

Essa complexidade não deve ser introduzida na primeira versão sem necessidade comprovada.

---

# 21. Testes do PDF

Devem existir testes para validar:

1. geração de ficheiro;
2. ficheiro não vazio;
3. tipo MIME;
4. nome do ficheiro;
5. presença do título;
6. presença do cliente;
7. presença do período;
8. presença da linha “Anterior”;
9. presença dos movimentos;
10. presença do total do período;
11. presença do total final;
12. funcionamento com várias páginas;
13. repetição do cabeçalho;
14. ausência de corte de colunas;
15. funcionamento sem movimentos;
16. funcionamento com caracteres portugueses;
17. valores monetários corretos.

---

# 22. Testes do Excel

Devem existir testes para validar:

1. geração de ficheiro `.xlsx`;
2. ficheiro não vazio;
3. tipo MIME;
4. nome do ficheiro;
5. existência da folha;
6. nome válido da folha;
7. datas como datas;
8. valores monetários como números;
9. cabeçalhos corretos;
10. presença da linha “Anterior”;
11. presença dos movimentos;
12. presença do total do período;
13. presença do total final;
14. formatação monetária;
15. larguras de coluna;
16. congelamento do cabeçalho;
17. autofiltro, quando aplicável;
18. compatibilidade com Microsoft Excel;
19. compatibilidade com LibreOffice Calc;
20. ausência de fórmulas inválidas.

---

# 23. Consistência entre formatos

Deve ser criado pelo menos um teste de integração que compare:

* resposta da API;
* dados usados no PDF;
* dados usados no Excel.

Os valores seguintes devem coincidir:

* número de movimentos;
* débito anterior;
* crédito anterior;
* saldo anterior;
* débito do período;
* crédito do período;
* saldo do período;
* débito final;
* crédito final;
* saldo final.

O objetivo não é comparar visualmente os ficheiros, mas garantir que todos recebem os mesmos dados-base.

---

# 24. Processo de implementação

Antes de alterar código, o Codex deve:

1. analisar a estrutura atual do Extrato de Cliente;
2. identificar o endpoint utilizado pelo React;
3. identificar os DTOs existentes;
4. identificar os serviços e repositories envolvidos;
5. verificar se existem bibliotecas de PDF ou Excel instaladas;
6. verificar a versão do Java;
7. verificar a versão do Spring Boot;
8. verificar a política de licenciamento do projeto;
9. apresentar as opções técnicas;
10. recomendar uma solução para Excel;
11. recomendar uma solução para PDF;
12. indicar os ficheiros a criar ou alterar;
13. assinalar riscos;
14. só depois iniciar a implementação.

---

# 25. Fases de implementação

## Fase 1

Consolidar os dados do Extrato de Cliente:

* anterior;
* movimentos;
* total do período;
* total final;
* filtros;
* ordenação.

## Fase 2

Implementar exportação Excel:

* `.xlsx`;
* tipos corretos;
* formatação;
* testes.

## Fase 3

Criar prova técnica de PDF:

* A4;
* multipágina;
* cabeçalhos repetidos;
* totais;
* testes de impressão.

## Fase 4

Implementar PDF definitivo.

## Fase 5

Extrair utilitários comuns quando forem necessários noutras listagens.

---

# 26. Critérios de aceitação

A funcionalidade é considerada concluída quando:

* o PDF respeita os filtros;
* o Excel respeita os filtros;
* os dados coincidem com a listagem;
* os totais coincidem em todos os formatos;
* o PDF é legível e imprimível em A4;
* o Excel contém valores numéricos reais;
* as datas são reconhecidas como datas;
* os cabeçalhos estão corretamente formatados;
* os ficheiros têm nomes adequados;
* os tipos MIME estão corretos;
* os botões funcionam sem recarregar a página;
* os erros são comunicados ao utilizador;
* as permissões são respeitadas;
* as bibliotecas possuem licenciamento adequado;
* os testes principais passam.
