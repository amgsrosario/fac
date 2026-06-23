# Documento 16 — Cenário-base e Reposição do Ambiente de Demonstração

**Projeto:** FAC
**Designação funcional:** MD 16 — Cenário-base e reposição do ambiente de demonstração
**Versão:** 1.0
**Estado:** A implementar
**Prioridade:** Crítica
**Objetivo estratégico:** Consolidar a FAC Demo Partner Edition
**Dependências:** Documentos correspondentes aos MD 11, 12, 13 e 14 concluídos
**Documento seguinte:** Polimento orientado do PDF fiscal e robustez multipágina

---

# 1. Enquadramento

O FAC já possui um núcleo funcional avançado, incluindo:

* backend Spring Boot;
* frontend React;
* gestão de clientes;
* gestão de artigos;
* documentos comerciais;
* linhas documentais;
* séries;
* emissão documental;
* cálculo de impostos e totais;
* numeração concorrencialmente segura;
* ATCUD persistido;
* payload QR persistido;
* snapshots históricos;
* geração de PDF baseada em dados persistidos;
* Flyway;
* ambiente de testes dedicado;
* backup e restauro;
* anulação documental;
* perfis e permissões;
* auditoria das operações críticas.

O objetivo atual não é concluir imediatamente todas as funcionalidades necessárias para uma distribuição comercial plena.

A prioridade é criar uma versão demonstrativa sólida, coerente e repetível, designada:

> **FAC Demo Partner Edition**

Esta versão será utilizada para apresentar o produto a um parceiro comercial relevante.

A demonstração deverá transmitir:

* maturidade funcional;
* coerência documental;
* estabilidade;
* clareza visual;
* segurança operacional;
* potencial de evolução;
* domínio do circuito comercial e financeiro.

Para isso, é necessário abandonar demonstrações improvisadas sobre bases de desenvolvimento e criar um ambiente próprio, com dados realistas, relações coerentes e reposição automática.

---

# 2. Objetivo

Implementar um cenário-base demonstrativo que possa ser reposto de forma segura e previsível antes de cada apresentação.

No final deste documento deverá ser possível:

1. preparar uma base de dados limpa para demonstração;
2. carregar automaticamente uma empresa fictícia;
3. criar utilizadores de demonstração;
4. criar clientes, artigos, serviços, séries e configurações necessárias;
5. criar documentos comerciais coerentes;
6. criar movimentos financeiros;
7. apresentar extratos com saldo anterior e movimentos do período;
8. apresentar listagens com totalizadores;
9. demonstrar documentos emitidos, pagos, parcialmente pagos e anulados;
10. demonstrar auditoria;
11. repetir o cenário sem duplicações;
12. restaurar o ambiente antes de cada demonstração;
13. validar automaticamente a coerência dos dados;
14. identificar a versão demonstrativa em execução.

---

# 3. Princípios orientadores

## 3.1. Repetibilidade

O mesmo processo de reposição deverá produzir sempre o mesmo cenário funcional.

A reposição não deverá depender de:

* introdução manual extensa;
* correções posteriores;
* alteração direta da base de dados;
* sequências ocultas conhecidas apenas pelo programador;
* dados acumulados de demonstrações anteriores.

## 3.2. Isolamento

O cenário de demonstração deverá funcionar num ambiente isolado.

Não deverá ser executado automaticamente em:

* produção;
* ambiente de clientes;
* base principal de desenvolvimento;
* qualquer base que contenha informação real.

## 3.3. Realismo controlado

Os dados devem parecer credíveis, mas ser totalmente fictícios.

Não devem ser utilizados:

* NIF reais de clientes;
* moradas pessoais reais;
* contas bancárias reais;
* emails pessoais;
* nomes de pessoas sem autorização;
* dados provenientes de empresas reais.

## 3.4. Coerência integral

Os documentos devem respeitar:

* séries existentes;
* datas coerentes;
* estados documentais válidos;
* totais;
* impostos;
* movimentos financeiros;
* saldos;
* auditoria;
* permissões;
* relações entre documentos.

## 3.5. Simplicidade operacional

A reposição deverá poder ser realizada através de um comando, script ou mecanismo administrativo controlado.

O processo deve ser suficientemente simples para ser executado antes de uma apresentação sem intervenção técnica complexa.

---

# 4. Âmbito

## 4.1. Incluído

Este documento deverá incluir:

* perfil de execução específico de demonstração;
* empresa fictícia;
* utilizadores demo;
* clientes demo;
* artigos e serviços demo;
* séries demo;
* documentos comerciais;
* documentos financeiros;
* pagamentos e recebimentos;
* documento parcialmente pago;
* documento anulado;
* auditoria coerente;
* extratos com movimentos;
* listagens com totalizadores;
* documento multipágina;
* mecanismo de reposição;
* proteção contra execução indevida;
* validação pós-carga;
* documentação de credenciais;
* identificação da versão demo;
* testes;
* instruções operacionais.

## 4.2. Não incluído

Ficam fora deste documento:

* certificação oficial;
* número real de certificado;
* comunicação com a Autoridade Tributária;
* comunicação automática de séries;
* SAF-T;
* hash fiscal certificado;
* faturação eletrónica;
* integração bancária;
* pagamentos online;
* multiempresa completa;
* importação de dados externos;
* migração de dados reais;
* operações cambiais avançadas;
* reposição remota através de interface pública;
* criação de uma infraestrutura cloud definitiva;
* automatização de deploy de produção.

---

# 5. Designação da versão

A aplicação deverá identificar a versão demonstrativa de forma discreta.

Sugestão:

```text
FAC Demo Partner Edition
```

A identificação poderá aparecer:

* no ecrã de autenticação;
* no rodapé da aplicação;
* na área de sessão;
* no título da aplicação;
* numa página “Sobre”.

A identificação não deverá surgir nos documentos fiscais impressos, salvo indicação explícita de que se trata de ambiente demonstrativo.

Nos PDFs de demonstração deverá ser considerada uma indicação discreta:

```text
Documento emitido em ambiente de demonstração
```

Esta indicação não deverá interferir com:

* ATCUD;
* QR Code;
* número documental;
* cabeçalhos;
* totais;
* legibilidade.

---

# 6. Empresa fictícia

## 6.1. Identidade

Criar a seguinte empresa fictícia:

> **Alentejo Sabores, Lda.**

## 6.2. Atividade

A empresa dedica-se a:

* comercialização de azeite;
* comercialização de vinho;
* cabazes de oferta;
* produtos alimentares regionais;
* transporte e entrega;
* pequenos serviços de preparação de encomendas empresariais.

## 6.3. Dados sugeridos

```text
Denominação: Alentejo Sabores, Lda.
Nome comercial: Alentejo Sabores
NIF: utilizar número fictício reservado ao ambiente demo
Morada: Rua da Oliveira, 24
Código postal: 7800-000 Beja
Localidade: Beja
País: Portugal
Telefone: 284 000 000
Email: geral@alentejosabores.demo
Website: www.alentejosabores.demo
Moeda base: EUR
```

O NIF deverá:

* ser claramente fictício;
* cumprir apenas as validações técnicas necessárias ao sistema;
* não corresponder, tanto quanto possível, a uma entidade real;
* ser documentado como dado de demonstração.

## 6.4. Dados bancários

Caso o PDF exiba dados bancários, utilizar:

```text
Banco: Banco Demonstração
IBAN: PT50 0000 0000 0000 0000 0000 0
SWIFT: DEMOPTPL
```

Estes dados devem ser identificados internamente como fictícios.

---

# 7. Utilizadores de demonstração

Criar três utilizadores:

## 7.1. Administrador

```text
Nome: Administrador Demo
Username: admin.demo
Perfil: ADMINISTRADOR
Estado: Ativo
```

## 7.2. Operador

```text
Nome: Operador Demo
Username: operador.demo
Perfil: OPERADOR
Estado: Ativo
```

## 7.3. Consulta

```text
Nome: Consulta Demo
Username: consulta.demo
Perfil: CONSULTA
Estado: Ativo
```

## 7.4. Credenciais

As palavras-passe:

* não devem ficar escritas em texto simples no código;
* devem ser configuráveis;
* devem ser armazenadas com hash;
* podem ser definidas através de variáveis de ambiente;
* devem constar de documentação operacional não pública.

Sugestão de variáveis:

```text
FAC_DEMO_ADMIN_PASSWORD
FAC_DEMO_OPERADOR_PASSWORD
FAC_DEMO_CONSULTA_PASSWORD
```

Caso não estejam definidas, o sistema deverá:

* falhar de forma controlada;
* ou utilizar credenciais exclusivamente locais, claramente assinaladas;
* nunca expor palavras-passe em logs.

---

# 8. Clientes de demonstração

Criar pelo menos quatro clientes.

## 8.1. Cliente nacional empresarial

```text
Nome: Mercearia do Castelo, Lda.
Tipo: Empresa
País: Portugal
Localidade: Évora
NIF: fictício
Email: compras@merceariadocastelo.demo
Condição de pagamento: 30 dias
```

Objetivo:

* demonstrar faturação normal;
* mostrar saldo em conta corrente;
* mostrar documento parcialmente pago;
* mostrar extrato com vários movimentos.

## 8.2. Cliente empresarial espanhol

```text
Nome: Sabores Ibéricos, S.L.
Tipo: Empresa
País: Espanha
Localidade: Badajoz
NIF/VAT: fictício
Email: pedidos@saboresibericos.demo
Condição de pagamento: transferência bancária
```

Objetivo:

* demonstrar cliente estrangeiro;
* mostrar morada e identificação fiscal internacional;
* não utilizar tratamentos fiscais que o sistema ainda não suporte de forma consolidada.

A operação deverá ser configurada apenas com regras já corretamente suportadas.

## 8.3. Consumidor final

```text
Nome: Consumidor Final
Tipo: Particular
País: Portugal
NIF: consumidor final ou valor aceite pelo sistema
Condição de pagamento: pronto pagamento
```

Objetivo:

* demonstrar venda simples;
* demonstrar pagamento integral;
* demonstrar documento de menor valor.

## 8.4. Cliente para anulação

```text
Nome: Hotel Planície Dourada, Lda.
Tipo: Empresa
País: Portugal
Localidade: Beja
NIF: fictício
Condição de pagamento: 15 dias
```

Objetivo:

* possuir um documento emitido sem movimentos financeiros;
* permitir anulação durante a apresentação;
* demonstrar auditoria e PDF anulado.

---

# 9. Artigos e serviços

Criar entre oito e dez registos.

## 9.1. Artigos sugeridos

| Código         | Descrição                    | Tipo   | Unidade |
| -------------- | ---------------------------- | ------ | ------- |
| AZEITE-500     | Azeite Virgem Extra 500 ml   | Artigo | UN      |
| AZEITE-750     | Azeite Virgem Extra 750 ml   | Artigo | UN      |
| AZEITE-5L      | Azeite Virgem Extra 5 L      | Artigo | UN      |
| VINHO-TINTO    | Vinho Tinto Regional 750 ml  | Artigo | UN      |
| VINHO-BRANCO   | Vinho Branco Regional 750 ml | Artigo | UN      |
| CABAZ-CLASSICO | Cabaz Alentejano Clássico    | Artigo | UN      |
| CABAZ-PREMIUM  | Cabaz Alentejano Premium     | Artigo | UN      |
| COMPOTA-FIGO   | Compota Artesanal de Figo    | Artigo | UN      |

## 9.2. Serviços sugeridos

| Código     | Descrição                         | Tipo    | Unidade |
| ---------- | --------------------------------- | ------- | ------- |
| TRANSPORTE | Serviço de transporte e entrega   | Serviço | UN      |
| PREPARACAO | Preparação personalizada de cabaz | Serviço | UN      |

## 9.3. Preços sugeridos

Os preços deverão produzir documentos visualmente interessantes e totais fáceis de interpretar.

Exemplo:

```text
AZEITE-500: 6,50 €
AZEITE-750: 9,80 €
AZEITE-5L: 49,00 €
VINHO-TINTO: 8,20 €
VINHO-BRANCO: 7,60 €
CABAZ-CLASSICO: 32,50 €
CABAZ-PREMIUM: 58,00 €
COMPOTA-FIGO: 4,80 €
TRANSPORTE: 12,50 €
PREPARACAO: 7,50 €
```

## 9.4. IVA

Utilizar apenas taxas de IVA que estejam corretamente configuradas e testadas no sistema.

O cenário deverá incluir mais do que uma taxa apenas se:

* o cálculo estiver consolidado;
* o PDF estiver correto;
* os totalizadores forem coerentes;
* não criar risco para a demonstração.

A estabilidade tem prioridade sobre a variedade fiscal.

---

# 10. Séries documentais

Criar séries próprias do ambiente demo.

Sugestão:

```text
FT D/2026
FR D/2026
RC D/2026
NC D/2026
```

A nomenclatura concreta deverá respeitar o modelo atual do FAC.

Cada série deverá:

* estar ativa;
* ter numeração coerente;
* possuir código de validação adequado ao ambiente demo;
* permitir geração de ATCUD;
* estar claramente separada das séries de desenvolvimento;
* não colidir com dados anteriores.

## 10.1. Numeração

A carga do cenário deverá produzir numeração previsível.

Exemplo:

```text
FT D/2026/1
FT D/2026/2
FT D/2026/3
FT D/2026/4
```

A reposição deve reiniciar o cenário conforme a estratégia definida, sem manipular incorretamente documentos já existentes fora do ambiente demo.

---

# 11. Documentos comerciais

Criar um conjunto equilibrado de documentos.

## 11.1. Fatura integralmente paga

Cliente:

```text
Consumidor Final
```

Conteúdo:

* azeite;
* vinho;
* preparação de cabaz.

Estado:

```text
EMITIDO
```

Situação financeira:

```text
PAGO
```

Objetivo:

* demonstrar venda simples;
* demonstrar total;
* demonstrar recebimento;
* demonstrar PDF.

## 11.2. Fatura parcialmente paga

Cliente:

```text
Mercearia do Castelo, Lda.
```

Conteúdo:

* várias caixas de azeite;
* vinho;
* transporte.

Estado:

```text
EMITIDO
```

Situação financeira:

```text
PARCIALMENTE PAGO
```

Objetivo:

* demonstrar saldo pendente;
* demonstrar extrato;
* demonstrar bloqueio de anulação;
* demonstrar mensagem 409 Conflict.

## 11.3. Fatura por pagar

Cliente:

```text
Mercearia do Castelo, Lda.
```

Conteúdo:

* cabazes;
* compotas;
* serviço de preparação.

Estado:

```text
EMITIDO
```

Situação financeira:

```text
EM ABERTO
```

Objetivo:

* contribuir para saldo do cliente;
* enriquecer extrato;
* mostrar totalizadores.

## 11.4. Documento para cliente espanhol

Cliente:

```text
Sabores Ibéricos, S.L.
```

Conteúdo:

* azeite;
* vinho;
* transporte.

Estado:

```text
EMITIDO
```

Objetivo:

* mostrar cliente internacional;
* demonstrar documento comercial com dados estrangeiros;
* não acionar regras fiscais incompletas.

## 11.5. Documento destinado a anulação

Cliente:

```text
Hotel Planície Dourada, Lda.
```

Conteúdo:

* cabazes premium;
* preparação;
* transporte.

Estado inicial:

```text
EMITIDO
```

Situação financeira:

```text
SEM MOVIMENTOS
```

Objetivo:

* ser anulado durante a demonstração;
* gerar evento de auditoria;
* mostrar PDF anulado;
* desaparecer dos totalizadores ativos;
* permanecer historicamente consultável.

## 11.6. Documento previamente anulado

Criar também um documento já anulado no cenário-base.

Objetivo:

* permitir demonstrar listagem de anulados sem consumir o documento reservado à anulação em direto;
* mostrar PDF com marca de anulação;
* mostrar auditoria histórica;
* validar exclusão de saldos.

---

# 12. Documento multipágina

Criar pelo menos um documento com linhas suficientes para gerar duas ou mais páginas.

O documento deverá conter:

* vários artigos;
* descrições suficientemente extensas;
* quantidades variadas;
* preços;
* impostos;
* totalizadores;
* cabeçalho repetido;
* rodapé;
* ATCUD;
* QR Code.

Objetivos:

* revelar problemas de paginação;
* testar cabeçalhos e rodapés;
* testar quebra de tabela;
* testar totais na última página;
* testar marca de anulação, caso seja utilizada uma cópia anulada;
* orientar o trabalho posterior de polimento visual.

Este documento deverá ser utilizado como referência no documento seguinte, dedicado ao PDF final.

---

# 13. Documentos financeiros

Criar movimentos coerentes com os documentos comerciais.

## 13.1. Recebimento integral

Associar à fatura do consumidor final.

O movimento deverá:

* liquidar integralmente o documento;
* aparecer no extrato;
* ficar auditado, caso o sistema já audite esta operação;
* produzir o estado financeiro correto.

## 13.2. Recebimento parcial

Associar à fatura da Mercearia do Castelo.

Exemplo:

```text
Total da fatura: 1.250,00 €
Recebimento: 500,00 €
Saldo pendente: 750,00 €
```

Os valores concretos deverão ser calculados pelo sistema, não forçados manualmente.

## 13.3. Documento em aberto

A segunda fatura da Mercearia do Castelo deverá permanecer sem recebimento.

Objetivo:

* produzir saldo acumulado;
* permitir leitura clara do extrato;
* demonstrar total do período.

---

# 14. Extrato de cliente

O cliente principal para demonstração do extrato será:

> **Mercearia do Castelo, Lda.**

O extrato deverá apresentar:

* saldo anterior;
* pelo menos duas faturas;
* um recebimento parcial;
* débito;
* crédito;
* saldo acumulado;
* total do período;
* saldo final.

## 14.1. Saldo anterior

O cenário deverá incluir um movimento anterior ao período normalmente utilizado na demonstração.

Exemplo:

```text
Período demonstrado: 01/06/2026 a 30/06/2026
Movimento anterior: maio de 2026
```

O extrato deverá mostrar:

```text
Anterior
```

com os valores acumulados antes do início do período.

## 14.2. Coerência

Os valores do extrato deverão resultar exclusivamente dos movimentos persistidos.

Não deverá ser criado um saldo anterior artificial sem suporte documental, salvo se o modelo atual já contemplar saldos de abertura de forma explícita.

---

# 15. Listagens e totalizadores

O cenário deverá permitir demonstrar:

* documentos por período;
* documentos por cliente;
* documentos por estado;
* documentos ativos;
* documentos anulados;
* total líquido;
* total de imposto;
* total bruto;
* total recebido;
* total pendente.

## 15.1. Documento anulado

O documento anulado:

* deverá aparecer quando o filtro incluir anulados;
* não deverá entrar nos totais ativos;
* deverá estar claramente identificado;
* deverá manter o valor histórico visível na consulta própria.

## 15.2. Quantidade de dados

Devem existir dados suficientes para que as listagens não pareçam vazias, mas não tantos que prejudiquem a clareza.

Referência recomendada:

```text
5 a 8 documentos comerciais
2 a 4 movimentos financeiros
4 clientes
8 a 10 artigos e serviços
3 utilizadores
2 documentos anulados, incluindo um para demonstração em direto
```

---

# 16. Auditoria do cenário

A carga inicial deverá produzir uma auditoria coerente.

Deverão existir eventos demonstráveis de:

* login;
* emissão;
* anulação;
* tentativa de anulação negada;
* operação executada por administrador;
* operação executada por operador.

## 16.1. Documento reservado à demonstração

O documento destinado à anulação em direto não deverá estar previamente anulado.

Após a anulação, a auditoria deverá permitir pesquisar:

* referência documental;
* utilizador;
* tipo de evento;
* data;
* motivo.

## 16.2. Tentativa negada

A demonstração poderá incluir:

1. entrada como operador;
2. confirmação de que a ação está oculta;
3. opcionalmente, tentativa técnica controlada;
4. registo de tentativa negada.

Não deverá ser criada uma demonstração artificial que exija ferramentas técnicas externas perante o parceiro.

---

# 17. Estratégia de implementação

Deverá ser escolhida uma estratégia compatível com a arquitetura atual.

São admitidas as seguintes abordagens.

## 17.1. Migração Flyway específica

Criar uma migração de dados destinada exclusivamente ao ambiente demo.

Vantagens:

* repetibilidade;
* controlo de versão;
* integração com o esquema.

Riscos:

* mistura entre estrutura e dados demo;
* execução indevida noutros ambientes.

Só deverá ser utilizada se existir proteção rigorosa por ambiente.

## 17.2. Seeder Spring Boot

Criar um componente específico de carga.

Exemplo conceptual:

```java
@Profile("demo")
@Component
public class DemoDataSeeder {
}
```

Vantagens:

* controlo por perfil;
* utilização dos serviços da aplicação;
* cálculo coerente;
* criação de snapshots;
* emissão através do fluxo real.

Esta é a abordagem preferencial, desde que:

* seja idempotente;
* esteja protegida;
* não execute em produção;
* utilize os serviços reais sempre que apropriado.

## 17.3. Script SQL controlado

Pode ser utilizado para:

* limpeza;
* reposição da base;
* preparação técnica;
* tarefas impossíveis de executar através da aplicação.

Não deverá ser utilizado para contornar regras do domínio.

## 17.4. Estratégia recomendada

Utilizar combinação de:

1. base de dados dedicada;
2. perfil Spring `demo`;
3. script de limpeza ou recriação;
4. Flyway para esquema;
5. seeder para dados;
6. validação automática no final.

---

# 18. Perfil de demonstração

Criar ou consolidar:

```text
spring.profiles.active=demo
```

O perfil `demo` deverá:

* utilizar base de dados dedicada;
* ativar o seeder;
* identificar a aplicação como Demo Partner Edition;
* não comunicar com serviços externos;
* não enviar emails reais;
* não realizar comunicações fiscais;
* não utilizar credenciais de produção.

## 18.1. Proteções

A carga demo só deverá ocorrer quando forem satisfeitas condições explícitas.

Exemplo:

```text
perfil ativo = demo
FAC_DEMO_SEED_ENABLED=true
nome da base contém fac_demo
```

Se uma destas condições falhar, a reposição deve ser recusada.

---

# 19. Base de dados dedicada

Criar uma base própria, por exemplo:

```text
fac_demo
```

A base não deverá ser confundida com:

```text
fac
fac_test
```

## 19.1. Regras

* `fac_test` continua reservada a testes automatizados;
* `fac_demo` fica reservada a apresentações;
* a base principal de desenvolvimento não deve ser limpa pelo mecanismo demo;
* os scripts devem validar o nome da base antes de apagar dados.

## 19.2. Confirmação de segurança

Qualquer script destrutivo deverá:

* verificar o ambiente;
* verificar a base;
* falhar em caso de dúvida;
* escrever mensagem clara;
* nunca assumir que a ligação está correta.

---

# 20. Mecanismo de reposição

Deverá existir um comando principal.

Exemplo Windows:

```text
scripts\reset-demo.ps1
```

Exemplo Linux:

```text
scripts/reset-demo.sh
```

O processo deverá:

1. validar o perfil;
2. validar o nome da base;
3. parar a aplicação, se necessário;
4. criar backup opcional do estado anterior;
5. limpar ou recriar a base;
6. executar Flyway V1 até à versão atual;
7. iniciar a aplicação em perfil demo;
8. executar o seeder;
9. validar os dados;
10. produzir relatório;
11. terminar com sucesso ou falha inequívoca.

## 20.1. Resultado esperado

Mensagem final:

```text
FAC Demo Partner Edition preparada com sucesso.
```

Em caso de falha:

```text
A reposição do ambiente demo falhou. O ambiente não deve ser utilizado para apresentação.
```

---

# 21. Idempotência

O seeder deverá ser idempotente ou executar apenas sobre uma base confirmadamente limpa.

Não deverá ser possível criar acidentalmente:

* clientes duplicados;
* artigos duplicados;
* séries duplicadas;
* utilizadores duplicados;
* documentos repetidos;
* eventos de auditoria inconsistentes.

São admitidas duas estratégias:

## Estratégia A

Recusar execução quando já existem dados demo.

## Estratégia B

Limpar integralmente o ambiente demo e recriar.

Para apresentações, recomenda-se a Estratégia B, desde que protegida pelo nome da base e perfil.

---

# 22. Utilização dos serviços reais

Sempre que possível, os documentos deverão ser criados através dos serviços reais da aplicação.

Não inserir diretamente na base:

* totais calculados;
* ATCUD;
* payload QR;
* snapshots;
* estados financeiros;
* movimentos;
* auditoria de sucesso.

O objetivo é garantir que o cenário valida o produto real.

É aceitável inserir diretamente:

* tabelas de referência;
* configurações;
* dados mestres simples;
* elementos necessários antes da utilização dos serviços.

---

# 23. Datas do cenário

As datas devem ser previsíveis.

Existem duas abordagens.

## 23.1. Datas fixas

Exemplo:

```text
maio e junho de 2026
```

Vantagens:

* consistência;
* facilidade de ensaio;
* resultados sempre iguais.

## 23.2. Datas relativas

Exemplo:

```text
mês atual e mês anterior
```

Vantagens:

* aparência atualizada.

Riscos:

* alteração de resultados;
* problemas no final do mês;
* diferenças entre ensaios.

## 23.3. Decisão recomendada

Utilizar datas fixas no cenário-base.

A demonstração deverá utilizar filtros preparados para essas datas.

Isto favorece a repetibilidade e reduz surpresas.

---

# 24. Validação automática

Após a carga, executar validações.

## 24.1. Utilizadores

Confirmar:

* três utilizadores;
* perfis corretos;
* utilizadores ativos;
* palavras-passe armazenadas com hash.

## 24.2. Dados mestres

Confirmar:

* uma empresa demo;
* quatro clientes;
* oito a dez artigos e serviços;
* séries ativas.

## 24.3. Documentos

Confirmar:

* documentos emitidos;
* documento pago;
* documento parcialmente pago;
* documento em aberto;
* documento anulado;
* documento disponível para anulação;
* documento multipágina.

## 24.4. Fiscalidade

Confirmar:

* número documental;
* ATCUD;
* payload QR;
* snapshots;
* totais;
* impostos.

## 24.5. Financeiro

Confirmar:

* recebimentos;
* saldos;
* documento parcialmente pago;
* bloqueio de anulação.

## 24.6. Auditoria

Confirmar:

* eventos de emissão;
* evento de anulação;
* utilizadores associados;
* referência documental pesquisável.

## 24.7. Extratos

Confirmar:

* saldo anterior;
* movimentos;
* débitos;
* créditos;
* saldo final;
* exclusão de anulados.

---

# 25. Relatório de preparação

A reposição deverá produzir um resumo.

Exemplo:

```text
FAC Demo Partner Edition

Empresa: Alentejo Sabores, Lda.
Utilizadores: 3
Clientes: 4
Artigos e serviços: 10
Séries: 3
Documentos comerciais: 7
Documentos financeiros: 2
Documentos anulados: 1
Documento reservado à anulação: FT D/2026/6
Documento multipágina: FT D/2026/5
Validações: aprovadas
```

O relatório poderá ser:

* apresentado no terminal;
* guardado em ficheiro;
* disponibilizado numa área administrativa.

---

# 26. Interface de demonstração

## 26.1. Ecrã inicial

Após login, a aplicação deverá apresentar um estado visualmente limpo.

Evitar:

* avisos técnicos;
* dados vazios;
* mensagens de desenvolvimento;
* componentes incompletos;
* links sem função;
* áreas experimentais.

## 26.2. Credenciais

As credenciais não deverão ficar visíveis no ecrã público, salvo se o ambiente estiver totalmente isolado.

Pode existir uma folha operacional separada para o apresentador.

## 26.3. Utilizador ativo

Manter visível:

* nome;
* perfil;
* opção de terminar sessão.

## 26.4. Navegação

As áreas principais da demonstração devem ser fáceis de localizar:

* clientes;
* artigos;
* documentos;
* recebimentos;
* extratos;
* listagens;
* auditoria.

---

# 27. Plano offline

A versão demo deverá poder funcionar sem internet, salvo dependências estritamente necessárias.

Verificar:

* frontend servido localmente;
* backend local;
* PostgreSQL local;
* fontes disponíveis;
* QR gerado localmente;
* ausência de recursos CDN;
* ausência de APIs externas;
* ausência de autenticação externa.

## 27.1. Equipamento

Preparar:

* computador principal;
* carregador;
* browser atualizado;
* base demo;
* scripts;
* backup;
* cópia local do projeto;
* versão compilada.

## 27.2. Alternativa

Manter disponível:

* backup restaurável;
* PDF de exemplos;
* capturas de ecrã;
* apresentação curta;
* versão local previamente iniciada.

O plano alternativo não substitui o sistema, mas reduz o risco de uma apresentação completamente interrompida.

---

# 28. Marcação da versão no Git

Criar uma versão identificável.

Sugestão:

```text
fac-demo-partner-v1
```

ou:

```text
v0.8-demo-partner
```

A marca deverá ser criada apenas após:

* testes completos;
* reposição validada;
* ensaio do guião;
* correção dos bloqueadores.

Registar:

* commit;
* tag;
* data;
* base de dados compatível;
* instruções de arranque;
* credenciais operacionais.

---

# 29. Testes obrigatórios

## 29.1. Seeder

Testar:

* execução apenas no perfil demo;
* recusa noutros perfis;
* recusa sobre base não autorizada;
* criação dos utilizadores;
* criação dos dados mestres;
* criação dos documentos;
* criação dos movimentos;
* validação final.

## 29.2. Reposição

Testar:

* reposição sobre base vazia;
* reposição após demonstração;
* segunda reposição sem duplicações;
* falha controlada;
* recuperação após falha;
* execução em Windows;
* execução em Linux, se suportado.

## 29.3. Dados

Testar:

* totais;
* estados;
* saldos;
* pagamentos;
* documentos anulados;
* bloqueios;
* auditoria;
* extratos;
* listagens.

## 29.4. Segurança

Testar:

* utilizador consulta não altera;
* operador não anula;
* administrador anula;
* credenciais não aparecem em logs;
* reset não executa fora do ambiente demo.

## 29.5. PDF

Testar:

* documentos simples;
* documento multipágina;
* documento anulado;
* ATCUD;
* QR;
* totais;
* cabeçalhos;
* rodapés.

## 29.6. Suite completa

Condição:

```text
0 falhas
0 erros
```

---

# 30. Ensaio manual obrigatório

Depois da implementação, executar pelo menos três ciclos completos:

```text
reset
→ arranque
→ login
→ percurso demonstrativo
→ anulação
→ consulta de auditoria
→ consulta de extrato
→ PDF
→ novo reset
```

O objetivo é confirmar que:

* o cenário regressa ao estado inicial;
* o documento reservado à anulação reaparece emitido;
* a auditoria regressa ao baseline;
* não existem duplicações;
* os números documentais são previsíveis;
* os totais permanecem iguais.

---

# 31. Percurso demonstrativo base

O cenário deverá suportar este guião:

1. login como operador;
2. visão geral do sistema;
3. consulta da Mercearia do Castelo;
4. consulta de artigos;
5. criação ou consulta de documento;
6. emissão;
7. visualização de ATCUD e QR;
8. geração de PDF;
9. consulta do documento parcialmente pago;
10. consulta do extrato;
11. consulta de listagem e totalizadores;
12. tentativa de anulação bloqueada;
13. login como administrador;
14. anulação do documento reservado;
15. geração do PDF anulado;
16. consulta da auditoria;
17. explicação da evolução futura.

---

# 32. Critérios de aceitação

O documento será considerado concluído quando:

* existir base `fac_demo`;
* existir perfil `demo`;
* o reset estiver protegido;
* os três utilizadores forem criados;
* a empresa fictícia estiver criada;
* os clientes estiverem criados;
* os artigos e serviços estiverem criados;
* as séries estiverem configuradas;
* os documentos estiverem emitidos pelo fluxo correto;
* existirem movimentos financeiros coerentes;
* existir documento parcialmente pago;
* existir documento anulado;
* existir documento disponível para anulação;
* existir documento multipágina;
* o extrato apresentar saldo anterior;
* as listagens apresentarem totalizadores;
* os anulados não entrarem nos totais ativos;
* a auditoria estiver coerente;
* o ambiente puder ser reposto através de um comando;
* a reposição for repetível;
* as validações automáticas passarem;
* a suite completa passar;
* três ensaios manuais completos forem realizados;
* não existirem bloqueadores no percurso demonstrativo.

---

# 33. Restrições de implementação

Não introduzir:

* lógica fiscal fictícia;
* documentos inseridos diretamente com totais forçados;
* bypass das regras de emissão;
* bypass das permissões;
* credenciais em texto simples;
* reset disponível publicamente;
* execução automática em produção;
* dependência de internet;
* dados pessoais reais;
* ferramentas de orquestração desnecessárias;
* arquitetura adicional sem benefício direto para a demo.

Sempre que houver dúvida, deverá prevalecer:

1. estabilidade;
2. previsibilidade;
3. facilidade de reposição;
4. coerência funcional;
5. baixo risco visual;
6. possibilidade de evolução futura.

---

# 34. Ordem recomendada de implementação

## Etapa 1 — Infraestrutura

* criar perfil demo;
* criar configuração da base;
* criar proteções;
* criar scripts.

## Etapa 2 — Seeder

* empresa;
* utilizadores;
* clientes;
* artigos;
* séries.

## Etapa 3 — Documentos

* emitir documentos comerciais;
* gerar ATCUD;
* gerar QR;
* criar snapshots;
* criar documento multipágina.

## Etapa 4 — Financeiro

* criar recebimento integral;
* criar recebimento parcial;
* validar saldos.

## Etapa 5 — Anulação e auditoria

* criar documento previamente anulado;
* reservar documento para anulação em direto;
* validar pesquisa por referência.

## Etapa 6 — Extratos e listagens

* validar saldo anterior;
* validar movimentos;
* validar totalizadores;
* validar exclusão de anulados.

## Etapa 7 — Reposição

* automatizar limpeza;
* recriar base;
* executar Flyway;
* executar seeder;
* validar.

## Etapa 8 — Ensaios

* executar três ciclos;
* registar falhas;
* corrigir;
* marcar versão.

---

# 35. Entregáveis

A implementação deverá produzir:

* configuração `demo`;
* base de dados dedicada;
* seeder;
* scripts de reset;
* scripts de arranque;
* validações automáticas;
* utilizadores demo;
* empresa demo;
* clientes demo;
* artigos e serviços demo;
* séries demo;
* documentos demo;
* movimentos financeiros;
* auditoria;
* testes;
* documentação de credenciais;
* manual de reposição;
* relatório final.

---

# 36. Relatório final esperado

No final, apresentar:

## Implementado

Resumo das funcionalidades concluídas.

## Dados criados

Quantidades e referências principais.

## Ficheiros criados

Lista completa.

## Ficheiros alterados

Lista completa.

## Scripts

Descrição dos comandos de reset e arranque.

## Segurança

Proteções implementadas contra execução indevida.

## Testes

* total;
* falhas;
* erros;
* testes novos.

## Ensaios

Resultado dos três ciclos manuais.

## Pendências

Lista transparente.

## Referências da demonstração

* documento multipágina;
* documento parcialmente pago;
* documento anulado;
* documento reservado à anulação;
* cliente principal do extrato.

---

# 37. Instrução final para o Codex

Implementar o presente documento em:

```text
C:\Projeto_faturação\fac
```

Preservar integralmente a arquitetura e os comportamentos consolidados.

Não alterar funcionalidades estáveis sem necessidade.

Não introduzir dados diretamente em tabelas fiscais quando estes devam ser produzidos pelos serviços reais.

Não comprometer:

* numeração;
* ATCUD;
* QR;
* snapshots;
* auditoria;
* permissões;
* saldos;
* extratos;
* listagens;
* rollback;
* Flyway;
* `ddl-auto=validate`.

A implementação deverá ser orientada exclusivamente para a criação de uma:

> **FAC Demo Partner Edition estável, credível, previsível e repetível.**

Não considerar o trabalho concluído enquanto o ambiente não puder ser reposto e demonstrado várias vezes com os mesmos resultados.
