# FAC - Princípios Basilares e Âmbito Funcional

## Estado do Documento

Versão: 1.1
Data: 06-06-2026
Estado: Ativo
Autor: António Rosário

---

# 1. Objetivo do Projeto

O FAC é uma aplicação de faturação certificável pela Autoridade Tributária Portuguesa, destinada a micro e pequenas empresas, profissionais independentes e organizações que necessitem de emitir documentos comerciais e fiscais de forma simples, eficiente e conforme a legislação portuguesa.

O projeto nasce com uma preocupação central:

**resolver um problema concreto sem se transformar num ERP generalista.**

O FAC não pretende competir com grandes plataformas ERP nem substituir sistemas de gestão empresarial complexos.

O seu foco é a emissão de documentos, controlo de recebimentos e disponibilização da informação necessária para a gestão operacional e cumprimento fiscal.

---

# 2. Princípios Fundamentais

## 2.1 Simplicidade

Qualquer funcionalidade deve justificar claramente o seu valor.

Se uma funcionalidade aumentar significativamente a complexidade sem gerar benefício proporcional para a maioria dos utilizadores, deverá ser rejeitada.

---

## 2.2 Certificação Fiscal

Todas as decisões funcionais e técnicas devem respeitar os requisitos legais necessários à certificação pela Autoridade Tributária.

O sistema deve suportar:

* Assinatura de documentos fiscalmente relevantes;
* Séries documentais;
* Comunicação de séries à AT;
* ATCUD;
* QR Code;
* Integridade documental;
* Exportação SAF-T (PT);
* Requisitos legais em vigor.

---

## 2.3 Independência Tecnológica

Nenhuma tecnologia específica deverá ser considerada insubstituível.

Sempre que possível deverão existir camadas de abstração que permitam a substituição futura de componentes sem necessidade de reescrever o sistema.

---

## 2.4 Crescimento Controlado

O FAC deverá crescer apenas quando existir necessidade comprovada.

O projeto rejeita:

* Funcionalidades especulativas;
* Desenvolvimento baseado em modas tecnológicas;
* Complexidade preventiva;
* Funcionalidades que não respondam a necessidades reais dos utilizadores.

---

## 2.5 Produtividade

As operações mais frequentes devem exigir o menor número possível de ações.

O sistema deve privilegiar:

* Rapidez;
* Clareza;
* Eficiência operacional.

---

# 3. Âmbito Funcional Inicial

## Incluído

### Clientes

* Cadastro de clientes;
* Moradas;
* NIF;
* Contactos;
* Condições de pagamento.

### Artigos e Serviços

* Artigos;
* Serviços;
* Categorias;
* Taxas de IVA;
* Unidades;
* Código interno de identificação.

### Documentos Comerciais

* Orçamentos;
* Faturas;
* Faturas-recibo;
* Recibos;
* Notas de crédito;
* Proformas.

### Recebimentos

* Registo de recebimentos;
* Controlo de pendentes;
* Conta corrente simples.

### Fiscalidade

* IVA;
* Motivos de isenção;
* SAF-T;
* Séries documentais;
* ATCUD;
* QR Code.

### Reporting Operacional

* Extrato de cliente;
* Pendentes;
* Vendas por período;
* Vendas por cliente;
* Vendas por artigo.

---

## Excluído

O FAC não deverá incluir nesta fase:

* Gestão de produção;
* MRP;
* Planeamento industrial;
* CRM avançado;
* Recursos Humanos;
* Contabilidade;
* Gestão documental complexa;
* Business Intelligence;
* Workflow empresarial;
* Gestão avançada de stocks multi-armazém.

---

# 4. Modelo de Dados

## Princípios

### Identificadores

Os identificadores funcionais são imutáveis.

Após criação não podem ser alterados.

---

### Catálogos

Catálogos estáveis poderão utilizar identificadores definidos pelo sistema.

Exemplos:

* Países;
* Taxas de IVA;
* Motivos de isenção;
* Moedas;
* Modos de pagamento.

---

### Integridade

A integridade dos dados prevalece sobre a conveniência operacional.

Não serão permitidas operações que comprometam a rastreabilidade documental.

---

# 5. Arquitetura Técnica

## Backend

Tecnologia principal:

* Java
* Spring Boot
* Spring Data JPA
* PostgreSQL

Arquitetura:

Controller → Service → Repository

---

## Frontend

Tecnologia principal:

* React
* TypeScript
* PrimeReact

Princípios:

* Interfaces simples;
* Componentes reutilizáveis;
* Navegação intuitiva;
* Foco na produtividade.

---

## Persistência

Base de dados:

PostgreSQL

Princípios:

* Entidades focadas no domínio;
* DTOs para comunicação externa;
* Separação clara entre domínio e API.

---

# 6. Reporting

## Objetivo

Disponibilizar documentos e mapas operacionais sem criar dependência excessiva de ferramentas externas.

---

## Princípios

O reporting é um módulo independente da lógica de negócio.

Os relatórios não deverão aceder diretamente às entidades da aplicação.

Toda a informação destinada a relatórios deverá ser preparada através de DTOs específicos.

---

## Documentos Oficiais

Devem existir modelos próprios para:

* Faturas;
* Recibos;
* Faturas-recibo;
* Notas de crédito;
* Orçamentos;
* Proformas.

---

## Tecnologia Inicial

A geração documental poderá utilizar JasperReports como motor de renderização.

Contudo:

* O sistema não deverá ficar dependente do Jasper;
* Os templates deverão ser substituíveis;
* A lógica de negócio não deverá conhecer o motor de reporting.

---

## Exportações

O sistema deverá suportar:

* PDF;
* Excel;
* CSV.

---

## Relatórios de Gestão

Inicialmente os relatórios de gestão deverão privilegiar:

* Grelhas de consulta;
* Pesquisa;
* Ordenação;
* Exportação.

Relatórios complexos apenas serão desenvolvidos quando existir necessidade comprovada.

---

# 7. Experiência de Utilização

O utilizador principal do FAC é um empresário ou colaborador administrativo.

Consequentemente:

* Menos cliques;
* Menos configuração;
* Menos complexidade.

O sistema deve ser compreendido rapidamente por alguém que nunca recebeu formação formal.

---

# 8. Critérios para Aceitação de Novas Funcionalidades

Uma funcionalidade apenas deverá ser aprovada se cumprir simultaneamente:

1. Resolve um problema real;
2. É utilizada por uma parte significativa dos utilizadores;
3. Não compromete a simplicidade do sistema;
4. Não aumenta significativamente os custos de manutenção;
5. Não transforma o FAC num ERP generalista.

---

# 9. Regra de Ouro do Projeto

Sempre que existir dúvida entre:

* adicionar mais uma funcionalidade;

ou

* manter o sistema simples;

a decisão padrão deverá ser:

**manter o sistema simples.**

O FAC pretende ser um excelente software de faturação.

Não pretende ser tudo para todos.
