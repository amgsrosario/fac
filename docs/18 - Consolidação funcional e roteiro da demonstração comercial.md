# MD 18 — Consolidação funcional e roteiro da demonstração comercial

## 1. Objetivo

Preparar a aplicação FAC Demo Partner Edition para uma demonstração comercial presencial ou remota, utilizando o ambiente local atualmente disponível.

Este módulo deve validar e consolidar os percursos funcionais criados nos módulos anteriores, garantindo que a demonstração:

* começa sempre num estado conhecido;
* utiliza dados coerentes;
* apresenta diferentes perfis de acesso;
* percorre os principais fluxos funcionais;
* evita páginas vazias, erros técnicos ou comportamentos inesperados;
* possui um roteiro de apresentação repetível;
* permite regressar rapidamente ao estado inicial.

Este módulo não tem como objetivo implementar novas funcionalidades estruturais nem publicar a aplicação num servidor externo.

---

## 2. Princípios

A demonstração deve refletir o funcionamento real da aplicação.

Não devem ser criadas simulações visuais desligadas do domínio, valores estáticos apresentados apenas no frontend ou atalhos que contornem:

* serviços do domínio;
* regras de emissão;
* numeração documental;
* ATCUD;
* QR Code;
* pagamentos;
* anulações;
* permissões;
* auditoria;
* extratos;
* geração de PDF.

O ambiente demonstrativo deve continuar a utilizar exclusivamente a base:

```text
fac_demo
```

O perfil obrigatório mantém-se:

```text
demo
```

---

## 3. Âmbito funcional da demonstração

A demonstração consolidada deve permitir apresentar, de forma sequencial, os seguintes blocos.

### 3.1. Identificação do ambiente

Confirmar visualmente:

* designação “FAC Demo Partner Edition”;
* empresa “Alentejo Sabores, Lda.”;
* indicação clara do perfil autenticado;
* inexistência de referências visuais ao ambiente de desenvolvimento;
* inexistência de passwords, tokens ou informação técnica sensível no ecrã.

### 3.2. Demonstração dos perfis

Validar os três utilizadores:

* `admin.demo`, perfil ADMINISTRADOR;
* `operador.demo`, perfil OPERADOR;
* `consulta.demo`, perfil CONSULTA.

Para cada perfil, confirmar:

* autenticação válida;
* identificação do perfil ativo;
* menus e operações visíveis;
* operações ocultadas;
* operações bloqueadas pelo backend;
* resposta funcional adequada quando uma operação não é permitida;
* registo de auditoria das ações relevantes.

O frontend não substitui a segurança do backend.

Mesmo que uma operação esteja escondida na interface, o backend deve continuar a recusá-la quando o perfil não tem autorização.

### 3.3. Clientes

Apresentar a listagem de clientes e validar os casos demonstrativos:

* cliente nacional;
* cliente espanhol;
* consumidor final;
* cliente com movimentos em extrato;
* cliente associado a documento anulado.

Confirmar que os dados são coerentes e suficientemente realistas para uma apresentação comercial.

Não implementar neste módulo novas regras fiscais intracomunitárias.

### 3.4. Artigos e serviços

Apresentar os oito artigos e serviços existentes.

Confirmar:

* códigos;
* designações;
* tipos;
* preços;
* taxas de IVA;
* unidades;
* estado ativo;
* coerência entre artigos e documentos emitidos.

### 3.5. Séries documentais

Apresentar e validar:

```text
FT/DEMO26
RC/DEMO26
```

Confirmar:

* associação ao tipo documental correto;
* estado;
* numeração;
* código de validação da série, quando aplicável;
* ATCUD produzido nos documentos;
* inexistência de saltos ou duplicações indevidas dentro do cenário reposto.

### 3.6. Documentos comerciais

A demonstração deve identificar claramente os seis documentos existentes e a finalidade de cada um:

1. documento parcialmente pago;
2. documento totalmente pago;
3. documento anulado;
4. documento emitido ao cliente espanhol;
5. documento multipágina com 35 linhas;
6. documento utilizado nos movimentos de extrato.

Para cada documento, validar:

* número;
* série;
* data;
* cliente;
* linhas;
* incidências;
* IVA;
* total;
* estado;
* ATCUD;
* QR Code;
* PDF;
* pagamentos associados;
* impacto no extrato;
* impacto da anulação.

### 3.7. Pagamentos

Apresentar os dois movimentos financeiros existentes.

Demonstrar:

* documento parcialmente pago;
* saldo ainda em dívida;
* documento totalmente pago;
* inexistência de saldo pendente;
* relação entre recebimento e documento;
* atualização correta do extrato;
* separação entre documentos comerciais e movimentos financeiros.

### 3.8. Anulação

Demonstrar a anulação utilizando o documento já anulado no cenário.

Confirmar:

* estado do documento;
* motivo da anulação;
* utilizador responsável;
* data e hora;
* impossibilidade de editar ou pagar um documento anulado;
* exclusão do documento anulado dos totalizadores financeiros, quando aplicável;
* manutenção do documento no histórico;
* registo completo na auditoria.

Não é necessário anular novamente o documento durante todas as apresentações.

A apresentação pode utilizar o estado previamente preparado pelo reset.

### 3.9. Extrato de cliente

Selecionar o cliente preparado para a demonstração do extrato.

Confirmar a apresentação de:

* saldo anterior, quando aplicável;
* três movimentos;
* débitos;
* créditos;
* saldo acumulado;
* total do período;
* saldo final;
* exclusão do documento anulado;
* ordenação cronológica estável.

### 3.10. PDF e documento multipágina

Abrir o PDF do documento com 35 linhas.

Executar uma inspeção visual mínima, sem substituir o trabalho aprofundado previsto no MD 15.

Confirmar:

* abertura correta;
* existência de todas as páginas;
* cabeçalho coerente;
* identificação do documento;
* cliente;
* linhas;
* transporte entre páginas, quando aplicável;
* totais finais;
* ATCUD;
* QR Code;
* inexistência de sobreposição grave;
* inexistência de linhas cortadas;
* inexistência de página final vazia;
* inexistência de colunas fora da área imprimível.

Os problemas encontrados devem ser registados para tratamento no MD 15.

Este módulo apenas bloqueia a demonstração quando o PDF for manifestamente inadequado para apresentação.

### 3.11. Auditoria

Autenticar como administrador e apresentar a área de auditoria.

Confirmar a existência de registos relativos a:

* login;
* emissão;
* pagamento;
* anulação;
* consulta ou operação administrativa relevante;
* tentativa negada por falta de permissões.

Demonstrar a pesquisa por referência documental.

---

## 4. Roteiro oficial da demonstração

Criar o ficheiro:

```text
docs/demo/roteiro-demonstracao-parceiro.md
```

O roteiro deve conter uma apresentação orientada para aproximadamente 15 a 20 minutos.

### Sequência recomendada

#### Bloco 1 — Enquadramento

Duração aproximada: 2 minutos.

Apresentar:

* objetivo do FAC;
* público-alvo;
* simplicidade operacional;
* separação entre faturação, recebimentos, extratos e controlo;
* estado atual do produto.

#### Bloco 2 — Empresa, clientes e artigos

Duração aproximada: 3 minutos.

Apresentar:

* empresa demo;
* clientes;
* consumidor final;
* cliente espanhol;
* artigos e serviços;
* séries documentais.

#### Bloco 3 — Documentos e PDF

Duração aproximada: 5 minutos.

Apresentar:

* listagem de documentos;
* uma fatura normal;
* fatura multipágina;
* ATCUD;
* QR Code;
* PDF;
* documento anulado.

#### Bloco 4 — Pagamentos e extrato

Duração aproximada: 4 minutos.

Apresentar:

* documento parcialmente pago;
* documento totalmente pago;
* recebimentos;
* extrato;
* saldo acumulado;
* total do período.

#### Bloco 5 — Segurança e auditoria

Duração aproximada: 3 minutos.

Apresentar:

* diferença entre administrador, operador e consulta;
* bloqueios;
* auditoria;
* rastreabilidade.

#### Bloco 6 — Encerramento

Duração aproximada: 2 minutos.

Apresentar:

* funcionalidades já consolidadas;
* aspetos ainda em evolução;
* próximos passos;
* possibilidade de adaptação ao mercado e aos parceiros.

---

## 5. Lista de controlo antes da demonstração

Criar o ficheiro:

```text
docs/demo/checklist-pre-demonstracao.md
```

A lista deve permitir confirmar, no próprio dia:

* repositório atualizado;
* working tree sem alterações inesperadas;
* Docker ativo, quando necessário;
* PostgreSQL disponível;
* base `fac_demo` acessível;
* perfil `demo` ativo;
* passwords demo configuradas;
* reset concluído;
* validação automática concluída;
* backend ativo;
* frontend ativo;
* login dos três utilizadores confirmado;
* seis documentos existentes;
* dois movimentos financeiros existentes;
* um documento anulado;
* um documento parcialmente pago;
* um documento totalmente pago;
* PDF multipágina disponível;
* extrato disponível;
* auditoria disponível;
* browser sem separadores ou dados inadequados;
* zoom do browser adequado;
* downloads antigos removidos ou organizados;
* ausência de informação sensível visível;
* plano alternativo preparado caso uma operação em direto falhe.

---

## 6. Verificação automática do cenário

Reutilizar a validação já implementada no MD 16 e, quando necessário, ampliá-la para produzir um resumo legível antes da demonstração.

Criar, caso ainda não exista uma solução equivalente, um comando ou script de verificação que não altere os dados.

Sugestão:

```text
scripts/demo/check-demo.ps1
```

O script deve:

* confirmar que o perfil ativo é `demo`;
* confirmar que a base é exatamente `fac_demo`;
* não executar reset;
* não apagar dados;
* não recriar documentos;
* executar apenas verificações;
* devolver código de saída diferente de zero quando existir uma falha;
* apresentar um resumo final legível.

### Verificações mínimas

* três utilizadores;
* perfis corretos;
* cinco clientes;
* oito artigos e serviços;
* duas séries;
* seis documentos;
* um documento anulado;
* um documento parcialmente pago;
* um documento totalmente pago;
* um documento com 35 linhas;
* dois movimentos financeiros;
* seis PDFs válidos;
* extrato com três movimentos;
* auditoria coerente;
* base correta;
* perfil correto.

---

## 7. Página inicial da demonstração

Avaliar a página inicial atual da FAC Demo Partner Edition.

Sem redesenhar toda a aplicação, melhorar apenas o necessário para que o ponto de entrada seja profissional.

A página inicial deve, preferencialmente, apresentar:

* nome da aplicação;
* identificação “Demo Partner Edition”;
* empresa ativa;
* utilizador e perfil;
* acessos rápidos às principais áreas;
* indicação discreta de que se trata de um ambiente de demonstração.

Evitar:

* dashboards com números inventados;
* gráficos sem utilidade;
* excesso de cartões;
* cores agressivas;
* mensagens técnicas;
* informação de configuração;
* exposição do nome da base de dados;
* exposição do perfil Spring;
* exposição de passwords.

---

## 8. Tratamento de erros na demonstração

Rever os principais fluxos demonstrativos para garantir que erros previsíveis produzem mensagens compreensíveis.

Validar especialmente:

* login inválido;
* operação sem permissões;
* tentativa de alterar documento anulado;
* tentativa de pagar documento anulado;
* documento ou cliente inexistente;
* falha na abertura de PDF;
* backend indisponível;
* sessão expirada.

As mensagens apresentadas ao utilizador não devem conter:

* stack traces;
* nomes de classes Java;
* SQL;
* nomes internos de tabelas;
* detalhes de infraestrutura;
* segredos;
* caminhos absolutos do computador.

Os detalhes técnicos devem permanecer apenas nos logs adequados.

---

## 9. Dados e narrativa comercial

Criar o ficheiro:

```text
docs/demo/narrativa-dados-demo.md
```

Este documento deve explicar a história funcional dos dados existentes.

Exemplo:

* quem é cada cliente;
* que documento lhe foi emitido;
* qual foi pago;
* qual ficou parcialmente pago;
* qual foi anulado;
* qual serve para demonstrar o extrato;
* qual contém 35 linhas;
* que utilizador realizou as principais operações.

A narrativa deve corresponder aos dados realmente criados pelo seed.

Não duplicar valores manualmente quando estes possam ser obtidos através da aplicação ou dos serviços.

---

## 10. Credenciais

As passwords não devem ser guardadas:

* no Git;
* nos documentos;
* no frontend;
* nos scripts;
* nos logs;
* em screenshots;
* em dados de seed;
* em ficheiros `.env` versionados.

Pode ser criado um documento de instruções que indique apenas os nomes das variáveis:

```text
FAC_DEMO_PASSWORD_ADMIN
FAC_DEMO_PASSWORD_OPERADOR
FAC_DEMO_PASSWORD_CONSULTA
```

O ficheiro `.env.demo.example` deve continuar sem segredos reais.

---

## 11. Testes

Adicionar testes apenas quando necessários para cobrir alterações introduzidas por este módulo.

Devem manter-se válidos:

* testes de segurança;
* testes de permissões;
* testes do reset;
* testes de emissão;
* testes de anulação;
* testes financeiros;
* testes de extrato;
* testes de auditoria;
* testes de geração de PDF;
* testes de isolamento da base demo.

O resultado final deve preservar:

```text
0 falhas
0 erros
```

O frontend deve compilar sem erros.

O backend deve compilar e executar os testes no diretório real do projeto.

---

## 12. Documentação final

Criar:

```text
docs/25 - Consolidacao funcional e roteiro da demonstracao comercial.md
docs/demo/roteiro-demonstracao-parceiro.md
docs/demo/checklist-pre-demonstracao.md
docs/demo/narrativa-dados-demo.md
```

Criar também, quando tecnicamente adequado:

```text
scripts/demo/check-demo.ps1
```

Atualizar o README apenas se existir uma secção específica para execução da demonstração.

---

## 13. Fora do âmbito

Ficam expressamente fora deste módulo:

* instalação no computador Ubuntu;
* publicação na Internet;
* configuração de domínio;
* HTTPS;
* reverse proxy;
* abertura de portas no router;
* VPN;
* acesso remoto;
* Dockerização para produção;
* alta disponibilidade;
* backups de produção;
* tratamento fiscal intracomunitário completo;
* submissão do SAF-T;
* comunicação real de séries à AT;
* certificação da aplicação pela AT;
* redesenho integral do frontend;
* revisão aprofundada do motor multipágina, reservada ao MD 15.

---

## 14. Critérios de aceitação

O módulo considera-se concluído quando:

1. o ambiente demo pode ser reposto através do procedimento do MD 16;
2. a integridade do cenário pode ser verificada sem novo reset;
3. existe um roteiro de demonstração entre 15 e 20 minutos;
4. existe uma checklist prévia;
5. existe uma narrativa coerente dos dados;
6. os três perfis podem ser demonstrados;
7. os seis documentos podem ser identificados pela sua finalidade;
8. pagamentos, extrato, anulação e auditoria podem ser apresentados;
9. o PDF multipágina foi inspecionado visualmente;
10. não são expostos segredos ou detalhes técnicos;
11. backend e frontend compilam;
12. todos os testes passam;
13. não existe qualquer dependência do segundo computador ou do Ubuntu.

---

## 15. Relatório final da implementação

No final, apresentar:

* resumo do que foi implementado;
* ficheiros criados;
* ficheiros alterados;
* testes adicionados ou alterados;
* resultado da suite;
* resultado da compilação do backend;
* resultado da compilação do frontend;
* resultado da verificação do cenário demo;
* problemas encontrados;
* pendências transferidas para o MD 15;
* confirmação de que não foi utilizada nem alterada a base `fac`;
* confirmação de que não foi necessária qualquer intervenção no computador Ubuntu.
