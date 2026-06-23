# 11 - Cenário-base e reposição do ambiente de demonstração

**Projeto:** FAC
**Documento:** Cenário-base e reposição do ambiente de demonstração
**Versão:** 1.0
**Estado:** Planeado para implementação
**Data:** 15 de junho de 2026
**Autor:** António Rosário

---

## 1. Objetivo

O presente documento define a criação de um cenário-base de demonstração para o FAC, destinado a:

* apresentações a potenciais parceiros;
* demonstrações comerciais;
* validação funcional;
* testes manuais;
* formação;
* gravação de vídeos;
* demonstrações remotas;
* avaliação da interface;
* validação de regressões.

O cenário deve permitir repor o FAC, de forma rápida e previsível, num estado conhecido, coerente e visualmente credível.

O objetivo não é criar uma grande massa de dados.

O objetivo é criar uma pequena empresa fictícia, com informação suficiente para demonstrar os principais fluxos funcionais da aplicação.

---

## 2. Princípio basilar

O ambiente de demonstração deve ser:

* separado do ambiente de desenvolvimento;
* separado dos testes automáticos;
* separado de qualquer futura produção;
* facilmente recriável;
* composto exclusivamente por dados fictícios;
* funcionalmente coerente;
* seguro;
* previsível;
* reutilizável.

A preparação de uma demonstração não deve depender da introdução manual repetida de clientes, artigos e documentos.

Deve existir um mecanismo automático de reposição do cenário-base.

---

## 3. Âmbito

A primeira versão do cenário deverá incluir, pelo menos:

* uma empresa emitente;
* duas ou três séries documentais;
* três clientes;
* três ou quatro artigos ou serviços;
* três ou quatro documentos de cada tipo suportado;
* documentos comerciais;
* documentos financeiros;
* movimentos suficientes para demonstrar extratos;
* diferentes estados documentais;
* diferentes taxas ou enquadramentos de IVA;
* dados suficientes para demonstrar filtros e totalizadores.

O cenário deve permanecer pequeno.

Não deve transformar-se numa base de dados pesada ou difícil de compreender.

---

## 4. Nome do ambiente

O ambiente deve ser identificado como:

```text
FAC - Ambiente de Demonstração
```

A base de dados poderá ser designada:

```text
fac_demo
```

O perfil Spring deverá ser:

```text
demo
```

O ficheiro de configuração deverá ser:

```text
application-demo.yml
```

ou equivalente, de acordo com a estrutura atual do projeto.

---

## 5. Separação de ambientes

Devem existir bases de dados distintas para cada finalidade.

Estrutura recomendada:

```text
fac_dev
fac_test
fac_demo
fac_prod
```

Significado:

| Base       | Finalidade                    |
| ---------- | ----------------------------- |
| `fac_dev`  | Desenvolvimento corrente      |
| `fac_test` | Testes automáticos            |
| `fac_demo` | Demonstrações e apresentações |
| `fac_prod` | Futura produção               |

O ambiente de demonstração nunca deve utilizar a base de desenvolvimento.

A reposição da demonstração nunca deve apagar ou alterar dados de outros ambientes.

---

## 6. Empresa fictícia

Deve ser criada uma empresa fictícia, com dados suficientemente credíveis para apresentação.

Exemplo:

```text
Demo Norte Consulting, Lda.
```

Dados exemplificativos:

```text
NIF: utilizar um valor fictício ou reservado para testes
Morada: Avenida Empresarial, n.º 25
Código postal: 1000-100 Lisboa
País: Portugal
Telefone: 210 000 000
Email: geral@demonorte.example
```

Os dados devem ser claramente fictícios.

Não devem ser utilizados:

* NIF reais;
* moradas pessoais;
* emails reais;
* dados de clientes da Finpart;
* informação retirada de empresas verdadeiras sem autorização.

---

## 7. Identificação visual do ambiente

A aplicação deve apresentar uma indicação visível de que está num ambiente de demonstração.

Exemplo:

```text
AMBIENTE DE DEMONSTRAÇÃO
```

Pode ser utilizada uma faixa discreta no cabeçalho:

```text
Dados fictícios. Não utilizar para emissão fiscal real.
```

Esta indicação deve estar presente em todos os ecrãs principais.

O objetivo é evitar qualquer confusão entre:

* demonstração;
* desenvolvimento;
* produção real.

---

## 8. Clientes do cenário

Devem ser criados três clientes com perfis distintos.

### 8.1. Cliente empresarial nacional

Exemplo:

```text
Alfa Gestão Empresarial, Lda.
```

Características:

* pessoa coletiva;
* NIF português fictício;
* morada nacional;
* email empresarial;
* movimentos comerciais;
* faturas por liquidar;
* recebimentos parciais.

Este cliente deve ser utilizado para demonstrar:

* emissão de fatura;
* conta corrente;
* recebimento parcial;
* saldo pendente;
* extrato com saldo anterior.

### 8.2. Cliente particular ou consumidor final

Exemplo:

```text
Consumidor Final
```

Características:

* sem identificação fiscal obrigatória, quando permitido;
* documentos de pequeno valor;
* fatura simplificada;
* pagamento imediato.

Este cliente deve ser utilizado para demonstrar:

* emissão rápida;
* documento simplificado;
* pagamento imediato;
* movimentos sem saldo pendente.

### 8.3. Cliente estrangeiro

Exemplo:

```text
Iberia Digital Services, S.L.
```

Características:

* país Espanha;
* identificação fiscal estrangeira fictícia;
* morada fora de Portugal;
* moeda euro;
* situação fiscal diferenciada, quando aplicável.

Este cliente deve ser utilizado para demonstrar:

* cliente estrangeiro;
* país diferente;
* validações de morada;
* eventual enquadramento fiscal distinto.

---

## 9. Artigos e serviços

Devem ser criados três ou quatro artigos ou serviços.

### 9.1. Serviço com taxa normal de IVA

Exemplo:

```text
Consultoria de gestão
```

Características:

* tipo: serviço;
* unidade: hora;
* preço unitário: 75,00 €;
* IVA normal.

### 9.2. Serviço de avença

Exemplo:

```text
Acompanhamento mensal
```

Características:

* tipo: serviço;
* unidade: mês;
* preço unitário: 250,00 €;
* IVA normal.

### 9.3. Artigo com taxa reduzida ou diferenciada

Exemplo:

```text
Manual técnico impresso
```

Características:

* tipo: artigo;
* unidade: unidade;
* preço unitário: 25,00 €;
* taxa de IVA configurada de acordo com o cenário de teste.

### 9.4. Serviço isento

Exemplo:

```text
Formação profissional isenta
```

Características:

* tipo: serviço;
* preço unitário: 300,00 €;
* motivo de isenção configurado.

Este artigo deve ser criado apenas se o FAC já suportar corretamente:

* isenções;
* motivos de isenção;
* validação fiscal associada.

Se esta funcionalidade ainda não estiver estabilizada, o artigo deve ficar fora da primeira versão do cenário.

---

## 10. Séries documentais

Devem ser criadas séries coerentes com os tipos documentais disponíveis.

Exemplo:

| Tipo de documento   | Série  | Nome                               |
| ------------------- | ------ | ---------------------------------- |
| Fatura              | DEMO26 | Faturas demonstração               |
| Nota de crédito     | DEMO26 | Notas de crédito demonstração      |
| Recibo              | DEMO26 | Recibos demonstração               |
| Fatura simplificada | DEMO26 | Faturas simplificadas demonstração |

Cada série deve utilizar:

* um numerador coerente;
* código AT fictício ou reservado para demonstração;
* data do código AT;
* associação correta ao tipo documental.

Os códigos utilizados não devem ser apresentados como códigos reais ou validados pela AT.

---

## 11. Documentos comerciais

O cenário deve incluir três ou quatro documentos de cada tipo já suportado de forma estável.

Não devem ser criados documentos de tipos ainda incompletos.

### 11.1. Faturas

Devem existir pelo menos quatro faturas:

#### Fatura 1

Cliente:

```text
Alfa Gestão Empresarial, Lda.
```

Conteúdo:

* consultoria de gestão;
* acompanhamento mensal;
* total significativo;
* parcialmente recebida.

Objetivo:

* demonstrar múltiplas linhas;
* demonstrar IVA;
* demonstrar saldo pendente;
* demonstrar recibo parcial.

#### Fatura 2

Cliente:

```text
Alfa Gestão Empresarial, Lda.
```

Conteúdo:

* um único serviço;
* totalmente por liquidar.

Objetivo:

* demonstrar dívida em aberto;
* alimentar extrato de cliente;
* demonstrar saldo acumulado.

#### Fatura 3

Cliente:

```text
Iberia Digital Services, S.L.
```

Conteúdo:

* serviço empresarial;
* cliente estrangeiro;
* enquadramento fiscal adequado ao estado atual da aplicação.

Objetivo:

* demonstrar internacionalização;
* demonstrar país e dados fiscais estrangeiros.

#### Fatura 4

Cliente:

```text
Consumidor Final
```

Conteúdo:

* valor reduzido;
* pagamento imediato, se o tipo documental o permitir.

Objetivo:

* demonstrar operação simples.

---

## 12. Faturas simplificadas

Se o FAC já suportar faturas simplificadas, criar três ou quatro exemplos.

Características:

* valores reduzidos;
* consumidor final;
* um ou dois artigos;
* pagamentos imediatos;
* datas recentes.

Estas faturas devem permitir mostrar:

* emissão rápida;
* listagem;
* filtragem por tipo documental;
* totalizadores.

---

## 13. Notas de crédito

Devem existir duas ou três notas de crédito relacionadas com faturas já existentes.

### Exemplo 1

Nota de crédito parcial sobre uma fatura da Alfa Gestão Empresarial.

Motivo:

```text
Correção parcial do serviço faturado.
```

Objetivo:

* demonstrar ligação ao documento original;
* demonstrar correção parcial;
* demonstrar impacto no saldo do cliente.

### Exemplo 2

Nota de crédito total sobre um documento de pequeno valor.

Motivo:

```text
Anulação integral do serviço.
```

Objetivo:

* demonstrar reversão integral;
* demonstrar impacto em listagens;
* demonstrar estado e rastreabilidade.

Não devem ser criadas notas de crédito sem documento de origem, caso o FAC exija essa relação.

---

## 14. Recibos

Devem existir três ou quatro recibos.

### Recibo 1

Recebimento parcial da Fatura 1.

Objetivo:

* manter saldo remanescente;
* demonstrar conta corrente;
* demonstrar extrato.

### Recibo 2

Liquidação total de uma fatura.

Objetivo:

* demonstrar encerramento do saldo;
* demonstrar correspondência entre documento comercial e financeiro.

### Recibo 3

Recebimento relativo a mais do que um documento, caso o FAC já suporte essa possibilidade.

Se essa funcionalidade ainda não estiver implementada, limitar o recibo a um único documento.

### Recibo 4

Recibo com data posterior e movimento recente.

Objetivo:

* tornar a listagem mais rica;
* demonstrar filtros por período.

---

## 15. Relações entre documentos

Os documentos devem contar uma pequena história empresarial.

Exemplo:

```text
Fatura A
→ recebimento parcial
→ saldo remanescente
```

```text
Fatura B
→ nota de crédito parcial
→ saldo corrigido
```

```text
Fatura C
→ recibo integral
→ saldo zero
```

```text
Fatura D
→ sem pagamento
→ documento em aberto
```

O cenário não deve limitar-se a documentos independentes.

As relações são essenciais para demonstrar:

* extratos;
* saldos;
* totais;
* pagamentos;
* correções;
* rastreabilidade.

---

## 16. Datas dos documentos

As datas devem ser relativas à data de execução do cenário.

Exemplo:

| Documento          | Data               |
| ------------------ | ------------------ |
| Documento antigo   | Hoje menos 60 dias |
| Movimento anterior | Hoje menos 45 dias |
| Fatura intermédia  | Hoje menos 30 dias |
| Recibo parcial     | Hoje menos 20 dias |
| Fatura recente     | Hoje menos 10 dias |
| Movimento atual    | Hoje menos 2 dias  |

O objetivo é garantir que o cenário permanece atual, independentemente da data em que é executado.

Evitar datas fixas como:

```text
2025-01-01
```

que rapidamente fazem a demonstração parecer abandonada numa arrecadação digital.

---

## 17. Cenários para o extrato de cliente

O cliente empresarial nacional deve permitir demonstrar:

* saldo anterior ao período;
* movimentos a débito;
* movimentos a crédito;
* saldo acumulado;
* total do período;
* saldo final.

Exemplo de período de demonstração:

```text
últimos 30 dias
```

Deve existir pelo menos um movimento anterior ao início do filtro, para que a linha:

```text
Anterior
```

tenha conteúdo.

O cenário deve permitir demonstrar:

* extrato com movimentos;
* extrato sem movimentos num período;
* cliente com saldo zero;
* cliente com saldo pendente.

---

## 18. Cenários para listagens

Os dados devem permitir demonstrar filtros por:

* cliente;
* período;
* tipo documental;
* estado;
* série;
* moeda, se aplicável.

As listagens devem produzir resultados suficientemente variados para mostrar:

* cabeçalhos;
* linhas;
* ordenação;
* totalizadores;
* estados vazios;
* paginação, se existente;
* seleção de colunas.

---

## 19. Utilizador de demonstração

Deve existir um utilizador específico para demonstrações.

Exemplo:

```text
demo@fac.local
```

ou:

```text
parceiro.demo
```

A palavra-passe deve ser configurada por variável de ambiente ou mecanismo seguro.

O utilizador deve ter permissões suficientes para:

* consultar;
* criar clientes;
* criar documentos;
* emitir documentos de demonstração;
* consultar listagens;
* consultar extratos;
* gerar PDF.

Não deve ter permissões para:

* alterar configurações críticas;
* apagar estruturas base;
* aceder a dados de outros ambientes;
* executar reposições destrutivas;
* alterar credenciais técnicas.

---

## 20. Estratégia de criação dos dados

A solução principal recomendada é um seeder Java específico para o perfil `demo`.

Nome sugerido:

```text
DemoDataSeeder
```

O seeder deve ser executado apenas quando o perfil ativo for:

```text
demo
```

Exemplo conceptual:

```java
@Profile("demo")
@Component
public class DemoDataSeeder {
}
```

O seeder deve:

1. verificar se o cenário já existe;
2. evitar duplicação acidental;
3. criar dados de referência;
4. criar empresa;
5. criar clientes;
6. criar artigos;
7. criar séries;
8. criar documentos;
9. criar recebimentos;
10. validar o resultado final.

---

## 21. Utilização de serviços de domínio

Sempre que possível, o seeder deve utilizar os serviços reais da aplicação.

Exemplo:

```text
ClienteService
ArtigoService
DocumentoComercialService
DocumentoFinanceiroService
SerieService
```

Vantagens:

* respeita regras de negócio;
* deteta regressões;
* acompanha a evolução do domínio;
* evita SQL desligado da aplicação;
* aproxima a criação de dados da utilização real.

Contudo, deve evitar:

* chamadas HTTP internas desnecessárias;
* utilização do frontend;
* duplicação de regras;
* dependências circulares.

Se algum serviço for inadequado para seeders, pode ser criada uma camada de apoio específica, desde que a lógica fiscal não seja duplicada.

---

## 22. Idempotência

O seeder deve ser idempotente.

Isto significa que executá-lo mais do que uma vez não deve criar duplicados.

Opções:

### Opção A

Verificar se existe uma marca do cenário:

```text
DEMO_BASE_V1
```

### Opção B

Verificar se existe a empresa fictícia.

### Opção C

Usar uma tabela técnica de controlo de seeders.

A abordagem deve ser consistente com o projeto.

Exemplo conceptual:

```text
Se cenário DEMO_BASE_V1 já existe
→ não voltar a criar
```

---

## 23. Reposição do cenário

Deve existir um mecanismo simples para repor o ambiente.

Comando recomendado:

```bash
./reset-demo.sh
```

No Windows:

```powershell
.\reset-demo.ps1
```

O script deve executar, de forma controlada:

1. confirmar que o destino é `fac_demo`;
2. parar os serviços necessários;
3. eliminar apenas a base de demonstração;
4. recriar a base;
5. aplicar o esquema;
6. executar o seeder;
7. arrancar backend e frontend;
8. verificar a disponibilidade;
9. apresentar resultado final.

---

## 24. Proteção contra execução no ambiente errado

O script de reposição deve abortar se a base de dados não for:

```text
fac_demo
```

Deve existir uma confirmação técnica explícita.

Exemplo:

```text
Operação recusada: a base configurada não corresponde ao ambiente de demonstração.
```

Nunca deve executar comandos destrutivos com:

```text
fac_dev
fac_test
fac_prod
```

A proteção deve existir no script e, quando possível, também na aplicação.

---

## 25. Dump PostgreSQL

Depois de o cenário-base estar estabilizado, pode ser criado um dump:

```text
fac_demo_base.dump
```

O dump permite:

* reposição rápida;
* demonstrações sem executar todo o seeder;
* recuperação após falhas;
* utilização offline;
* criação de ambientes temporários.

O dump não substitui o seeder.

O seeder deve permanecer a fonte lógica e documentada do cenário.

O dump funciona como fotografia técnica do ambiente.

---

## 26. Estratégia combinada

A estratégia recomendada é:

```text
DemoDataSeeder
→ fonte lógica do cenário
```

```text
fac_demo_base.dump
→ reposição rápida
```

```text
reset-demo.sh / reset-demo.ps1
→ automatização da operação
```

Esta combinação oferece:

* robustez;
* rapidez;
* repetibilidade;
* recuperação;
* independência de preparação manual.

---

## 27. Docker

Se o FAC estiver a utilizar Docker Compose, deve ser considerado um perfil ou ficheiro específico para demonstração.

Exemplo:

```text
docker-compose.demo.yml
```

Serviços possíveis:

```text
fac-demo-db
fac-demo-backend
fac-demo-frontend
```

A base de dados deve utilizar:

* volume próprio;
* credenciais próprias;
* porta configurada;
* nome inequívoco.

Exemplo de volume:

```text
fac_demo_postgres_data
```

Não reutilizar o volume da base de desenvolvimento.

---

## 28. Configuração do perfil demo

O perfil `demo` deve conter apenas as diferenças necessárias.

Exemplo conceptual:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/fac_demo
  jpa:
    hibernate:
      ddl-auto: validate
```

A configuração exata deve respeitar a estratégia atual do projeto.

Não introduzir `create-drop` sem decisão consciente.

O ambiente deve preservar os dados durante uma demonstração e ser reposto apenas por ação explícita.

---

## 29. Geração do esquema

A criação do esquema deve seguir a estratégia oficial do projeto.

Possibilidades:

* Flyway;
* scripts SQL;
* Hibernate;
* restauração de dump.

Não deve ser criada uma estratégia paralela apenas para demonstração.

Se o projeto ainda não utilizar migrações formais, documentar essa limitação e evitar alterações destrutivas automáticas.

---

## 30. Roteiro de demonstração

O cenário deve suportar um roteiro de 15 a 20 minutos.

### Passo 1 - Entrada

Mostrar:

* login;
* identificação do ambiente;
* dashboard ou ecrã inicial.

### Passo 2 - Clientes

Mostrar:

* listagem;
* pesquisa;
* ficha de cliente;
* criação rápida de um novo cliente.

### Passo 3 - Artigos

Mostrar:

* artigos e serviços;
* preços;
* taxas de IVA;
* pesquisa.

### Passo 4 - Documento comercial

Criar uma fatura para um cliente existente.

Demonstrar:

* seleção de cliente;
* seleção de artigo;
* quantidades;
* preço;
* IVA;
* totais;
* gravação.

### Passo 5 - Emissão

Demonstrar:

* atribuição da série;
* numeração;
* emissão;
* estado definitivo.

Não apresentar o sistema como fiscalmente certificado enquanto tal não for verdade.

### Passo 6 - PDF

Mostrar:

* visualização;
* paginação;
* totais;
* aspeto profissional;
* impressão em A4.

### Passo 7 - Conta corrente

Abrir o extrato do cliente.

Mostrar:

* saldo anterior;
* débitos;
* créditos;
* saldo acumulado;
* total do período;
* saldo final.

### Passo 8 - Listagens

Mostrar:

* filtros;
* ordenação;
* seleção de colunas;
* totalizadores;
* estados sem movimentos.

### Passo 9 - Roadmap

Explicar, quando relevante:

* SAF-T;
* ATCUD;
* Código QR;
* segurança;
* evolução da interface;
* reporting;
* exportações.

---

## 31. Reinício após cada demonstração

Depois de cada demonstração, o ambiente pode ficar alterado devido a:

* novos clientes;
* novas faturas;
* recibos;
* alterações de filtros;
* testes feitos pelo parceiro.

Antes da demonstração seguinte, deve ser possível executar:

```text
repor cenário-base
```

O resultado deve ser sempre o mesmo.

O tempo objetivo para a reposição deve ser inferior a cinco minutos.

Idealmente:

```text
1 a 2 minutos
```

---

## 32. Variações do cenário

A primeira versão deve conter apenas um cenário-base.

No futuro poderão existir:

```text
DEMO_BASE
DEMO_COMERCIAL
DEMO_CONTABILIDADE
DEMO_PARCEIRO
DEMO_FORMACAO
```

Não implementar já múltiplos cenários sem necessidade.

Começar por:

```text
DEMO_BASE_V1
```

Depois de estabilizado, poderão ser acrescentadas variantes.

---

## 33. Versionamento do cenário

O cenário deve possuir uma versão.

Exemplo:

```text
DEMO_BASE_V1
```

Quando houver alterações incompatíveis:

```text
DEMO_BASE_V2
```

A versão deve permitir identificar:

* estrutura esperada;
* dados criados;
* funcionalidades demonstradas;
* compatibilidade com a versão da aplicação.

---

## 34. Dados realistas, mas fictícios

Os dados devem parecer plausíveis.

Evitar:

```text
Cliente 1
Cliente 2
Teste
Artigo A
Documento Teste
```

Preferir:

```text
Alfa Gestão Empresarial, Lda.
Iberia Digital Services, S.L.
Consultoria de gestão
Acompanhamento mensal
```

A credibilidade visual é importante numa demonstração.

Contudo, todos os dados devem continuar claramente fictícios.

---

## 35. Volumes recomendados

A primeira versão deve manter aproximadamente:

| Entidade              |         Quantidade |
| --------------------- | -----------------: |
| Empresa               |                  1 |
| Utilizadores          |             1 ou 2 |
| Clientes              |                  3 |
| Artigos/serviços      |                  4 |
| Séries                |             3 ou 4 |
| Faturas               |                  4 |
| Faturas simplificadas |                  3 |
| Notas de crédito      |                  2 |
| Recibos               |             3 ou 4 |
| Outros documentos     | Apenas se estáveis |

O volume pode ser ajustado ao que estiver efetivamente implementado.

Não criar documentos artificiais apenas para atingir números.

---

## 36. Critérios de qualidade dos dados

Os dados devem respeitar:

* integridade referencial;
* datas coerentes;
* numeração coerente;
* relações entre documentos;
* totais corretos;
* IVA correto;
* saldos corretos;
* estados documentais válidos;
* ausência de duplicados;
* ausência de entidades órfãs.

O cenário deve falhar durante a criação se alguma destas regras não for cumprida.

---

## 37. Validação automática do cenário

Após a criação, o sistema deve validar, pelo menos:

```text
Existe uma empresa
Existem três clientes
Existem artigos
Existem séries
Existem documentos comerciais
Existem documentos financeiros
Existe cliente com saldo
Existe cliente sem saldo
Existe movimento anterior ao período de demonstração
```

Pode ser criado um componente:

```text
DemoScenarioValidator
```

Este componente deve produzir um relatório simples.

Exemplo:

```text
Cenário DEMO_BASE_V1 criado com sucesso.

Clientes: 3
Artigos: 4
Faturas: 4
Notas de crédito: 2
Recibos: 3
```

---

## 38. Testes

Devem existir testes para:

### Seeder

* execução em base vazia;
* segunda execução sem duplicação;
* execução apenas no perfil `demo`;
* falha controlada quando faltam dados de referência;
* criação das relações esperadas.

### Reposição

* proteção contra base incorreta;
* recriação de `fac_demo`;
* execução do seeder;
* validação final;
* resultado repetível.

### Cenário funcional

* extrato do cliente principal;
* saldo anterior;
* saldo final;
* listagem de documentos;
* totalizadores;
* emissão de novo documento;
* criação de novo cliente.

---

## 39. Segurança

A reposição do cenário é uma operação destrutiva.

Deve exigir:

* ambiente `demo`;
* nome de base confirmado;
* utilizador técnico autorizado;
* comando explícito;
* registo da operação.

Nunca deve estar disponível a utilizadores comuns através da interface principal.

Uma futura ação administrativa deverá exigir dupla confirmação.

---

## 40. Credenciais

As credenciais do ambiente não devem ficar escritas diretamente no código.

Devem ser fornecidas através de:

* variáveis de ambiente;
* ficheiro `.env` não versionado;
* mecanismo de segredos;
* configuração externa.

Pode existir um ficheiro de exemplo:

```text
.env.demo.example
```

Sem palavras-passe reais.

---

## 41. Documentação de utilização

Deve existir uma secção no README ou um documento específico com os comandos:

```text
Criar ambiente demo
Iniciar ambiente demo
Repor cenário
Parar ambiente
Criar dump
Restaurar dump
```

Exemplo:

```bash
docker compose -f docker-compose.demo.yml up -d
./reset-demo.sh
```

Os comandos reais devem ser adaptados à infraestrutura existente.

---

## 42. Limites da primeira implementação

A primeira implementação não deve incluir:

* dezenas de clientes;
* grandes volumes de documentos;
* anonimização de dados reais;
* importação de bases de clientes;
* demonstrações multiempresa complexas;
* comunicação real com a AT;
* códigos AT reais;
* automações excessivas;
* painel público de reposição;
* múltiplos cenários especializados.

O foco deve permanecer num cenário pequeno, estável e reutilizável.

---

## 43. Fases de implementação

### Fase 1 - Definição do cenário

Definir:

* empresa;
* clientes;
* artigos;
* séries;
* documentos;
* relações;
* datas;
* saldos esperados.

### Fase 2 - Perfil demo

Criar:

* `application-demo.yml`;
* ligação à base `fac_demo`;
* identificação visual;
* utilizador de demonstração.

### Fase 3 - Seeder

Criar:

* `DemoDataSeeder`;
* controlo de versão;
* idempotência;
* validação final.

### Fase 4 - Reposição

Criar:

* `reset-demo.sh`;
* `reset-demo.ps1`, se necessário;
* proteções;
* mensagens;
* validação.

### Fase 5 - Dump

Criar:

* dump inicial;
* comandos de restauração;
* documentação.

### Fase 6 - Roteiro

Preparar:

* sequência da apresentação;
* dados a utilizar;
* documentos a abrir;
* filtros a aplicar;
* pontos do roadmap.

---

## 44. Critérios de aceitação

A implementação considera-se concluída quando:

* existe uma base `fac_demo`;
* existe um perfil `demo`;
* os dados são fictícios;
* existe uma empresa de demonstração;
* existem três clientes;
* existem três ou quatro artigos;
* existem séries coerentes;
* existem documentos comerciais;
* existem documentos financeiros;
* existem relações entre faturas, créditos e recibos;
* existe cliente com saldo anterior;
* existe cliente com saldo pendente;
* existe cliente com saldo zero;
* as datas são relativas;
* o cenário pode ser recriado automaticamente;
* a reposição não afeta outras bases;
* o seeder não cria duplicados;
* existe indicação visual de demonstração;
* existe utilizador de demonstração;
* existe roteiro de apresentação;
* os testes relevantes passam;
* a reposição demora menos de cinco minutos.

---

## 45. Resultado esperado

Após a implementação, deverá ser possível executar um único comando e obter:

```text
FAC iniciado em ambiente de demonstração
Base fac_demo recriada
Dados DEMO_BASE_V1 carregados
Aplicação disponível
```

O utilizador deverá poder iniciar imediatamente uma apresentação, sem preparação manual adicional.

---

## 46. Decisão final

O cenário-base será tratado como um ativo permanente do projeto FAC.

Não será apenas uma massa de dados descartável.

Será utilizado como:

* instrumento comercial;
* ambiente de validação;
* base de formação;
* suporte a testes manuais;
* referência para o desenvolvimento da interface;
* base para apresentações frequentes.

A implementação deve privilegiar simplicidade, repetibilidade e segurança.

O princípio orientador é:

```text
uma demonstração
→ um cenário conhecido
→ uma reposição simples
→ um resultado previsível
```
