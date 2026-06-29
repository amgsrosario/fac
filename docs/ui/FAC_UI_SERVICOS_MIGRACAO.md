# FAC UI - Migracao Comercial de Servicos

## 1. Objetivo

Criar a primeira vista comercial para a edicao `SERVICES_SIMPLE`, apresentando a entidade tecnica `Artigo` como `Servicos` sem alterar backend, base de dados, DTOs, endpoints, seed, regras fiscais, PDFs ou a vista legacy.

## 2. Comportamento Legacy

O ficheiro `frontend/src/ArtigosView.tsx` foi inspecionado e nao foi alterado. Continua a ser usado quando `VITE_FAC_UI_MODE` nao e `commercial`, preservando o modo `legacy` como predefinido.

Comportamentos identificados no ecran legacy:

- dados: `Artigo` com codigo, abreviatura, codigo de identificacao, descricao, unidade, familia, peso, IVA compra, IVA venda, PVP, inativo, retencao e observacoes;
- endpoints: `GET /api/artigos`, `POST /api/artigos`, `PUT /api/artigos/{codigo}`, `GET /api/familias`, `GET /api/tipos-taxa-iva`;
- permissao: `MESTRES_GERIR` para criar e editar;
- pesquisa: codigo, descricao, abreviatura e codigo de identificacao;
- selecao: codigo selecionado em estado local;
- colunas: configuraveis por `ColumnSelector` e `localStorage`;
- validacoes: codigo, descricao, unidade, familia, IVA compra/venda, PVP e peso;
- estados: loading, mensagens de erro, lista vazia e aviso de sucesso;
- catalogos: familias e tipos de taxa de IVA.

## 3. Comportamento Commercial

Foi criada a rota `/services` no modo `commercial`, dentro de `frontend/src/ui/commercial/services/`. A rota usa `DesktopShell`, `MobileShell`, `ResponsiveSlot`, `useDeviceClass`, componentes FAC e `FacToastProvider`.

O modo commercial passa a manter login e sessao, em vez de abrir apenas o laboratorio. `/ui-lab` continua disponivel no modo commercial.

## 4. Campos Visiveis

- Codigo
- Descricao
- Unidade
- Preco sem IVA
- Taxa de IVA
- Ativo

## 5. Campos Ocultos

- Familia
- Peso
- IVA de compra
- Abreviatura
- Codigo de identificacao
- Retencao
- Observacoes
- Campos logisticos, stock, armazem e descontos nao aparecem porque nao existem no contrato atual de `ArtigoCreateDto`/`ArtigoUpdateDto` inspecionado.

## 6. Valores Predefinidos Usados

Sem IDs fixos.

- Familia: procura uma familia existente cuja descricao contenha `servic`, normalizada sem acentos. Se nao existir, a criacao fica bloqueada com mensagem clara.
- Peso: `0`, mantendo o valor neutro ja usado no legacy.
- IVA de compra: primeiro tipo de IVA ativo existente; em edicao preserva o IVA de compra do artigo selecionado.
- IVA de venda: campo visivel e obrigatorio.
- Retencao: `false` em criacao; em edicao preserva o valor existente.
- Abreviatura, codigo de identificacao e observacoes: vazios em criacao; preservados em edicao.

## 7. Limitacoes

O modelo atual nao tem entidade propria de unidades. A unidade continua a ser uma string com maximo de 3 caracteres, por isso foram oferecidas apenas opcoes compativeis com esse limite: `UN`, `H`, `DIA`, `MES`, `SES`, `SER`, `AVN`.

Diagnostico posterior:

- `mvn -DskipTests compile` passou;
- `target/classes/com/ar2lda/fac/FacApplication.class` existe;
- a classe principal real e `com.ar2lda.fac.FacApplication`;
- o modulo Maven correcto e a raiz do projecto;
- `mvn spring-boot:run '-Dspring-boot.run.main-class=com.ar2lda.fac.FacApplication'` continuou a falhar neste ambiente Windows com `Could not find or load main class com.ar2lda.fac.FacApplication`;
- `mvn -DskipTests package` gerou o JAR Spring Boot correctamente;
- o backend arrancou pelo artefacto Maven empacotado: `java -jar target/fac-0.0.1-SNAPSHOT.jar`.

Conclusao: nao foi demonstrado defeito no package, classe principal ou `pom.xml`. O erro ficou circunscrito ao modo como `spring-boot:run` monta o classpath neste ambiente local. Nao foi feita alteracao Java.

## 8. Desktop

Desktop usa composicao de produtividade:

- cabecalho com contexto, titulo `Servicos`, descricao e acao `Novo servico`;
- pesquisa por codigo ou descricao;
- filtro por estado;
- lista tabular propria e controlada;
- detalhe lateral do servico selecionado;
- dialogo amplo para criacao e edicao.

## 9. Tablet

Tablet usa a mesma shell comercial, mas a grelha adapta para uma coluna quando a largura nao suporta lista e detalhe confortavelmente. A rota foi preparada para 768 px e 1024 px sem tabela comprimida em mobile.

## 10. Mobile

Mobile nao usa tabela comprimida. A experiencia usa:

- cabecalho compacto;
- pesquisa e filtro em coluna;
- lista em cartoes;
- detalhe em pagina integral;
- formulario em pagina integral;
- acao `Novo`;
- navegacao explicita de regresso a lista.

## 11. Permissoes

A permissao `MESTRES_GERIR` e respeitada no frontend:

- utilizador com permissao ve `Novo servico` e `Editar servico`;
- utilizador sem permissao consulta e recebe mensagem de consulta no detalhe;
- backend continua a ser a autoridade final.

## 12. Endpoints Reutilizados

- `GET /api/artigos?size=200&sort=codigo,asc`
- `POST /api/artigos`
- `PUT /api/artigos/{codigo}`
- `GET /api/familias?size=200&sort=descricao,asc`
- `GET /api/tipos-taxa-iva?size=100&sort=descricao,asc`

Nao foram criados endpoints.

## 13. Validacoes

- codigo obrigatorio em criacao, apenas letras maiusculas e numeros;
- descricao obrigatoria;
- unidade obrigatoria;
- preco igual ou superior a zero;
- taxa de IVA obrigatoria;
- familia oculta obrigatoria com origem segura em catalogo existente;
- IVA de compra oculto obrigatorio com origem em catalogo existente;
- prevencao de duplo clique por estado `saving`;
- erro de API apresentado como mensagem de validacao sem stack trace.

## 14. Componentes FAC Usados

- `FacButton`
- `FacInputText`
- `FacSelect`
- `FacDialog`
- `FacStatusBadge`
- `FacLoadingState`
- `FacErrorState`
- `FacEmptyState`
- `FacMessage`
- `FacToastProvider`
- `DesktopShell`
- `MobileShell`
- `ResponsiveSlot`
- `useDeviceClass`

Nao foi introduzida uma abstracao universal de tabela. A tabela local e pequena, controlada e especifica para esta migracao.

## 15. Riscos

- A criacao depende de existir uma familia de catalogo identificavel como servicos; sem isso, a UI bloqueia a gravacao para evitar IDs rigidos.
- Como unidade e string curta, nomes como `Servico`, `Sessao` e `Avenca` precisam de codigos abreviados.
- O primeiro IVA ativo para IVA de compra e uma escolha tecnica herdada da necessidade do contrato atual; deve ser confirmada quando houver parametrizacao explicita.
- Sem backend local operacional, criacao/edicao reais nao foram exercitadas nesta sessao.

## 16. Testes

Executado:

- `git status --short`
- `git branch --show-current`
- `git log -1 --oneline`
- `git show V60-ui-poc --no-patch --oneline`
- `tsc -b`
- `vite build`

Resultado:

- branch: `feature/ui-commercial`;
- working tree inicial limpo;
- tag `V60-ui-poc` presente;
- TypeScript passou;
- Vite build passou.

`npm run build` nao executou diretamente porque o `npm` global local falhou antes de entrar no projeto: `Cannot find module ... npm-cli.js`. A validacao equivalente foi executada com os binarios locais `node_modules/.bin/tsc.cmd` e `node_modules/.bin/vite.cmd`.

## 17. Capturas

Diretorio: `docs/ui/services-screen/`.

Capturas geradas:

- `services-auth-required-desktop-1440.png`
- `services-auth-required-tablet-768.png`
- `services-auth-required-mobile-375.png`

Estas capturas validam a rota commercial no estado acessivel sem backend autenticado. As capturas de lista, detalhe, edicao, vazio, erro e loading com dados reais continuam pendentes ate o backend local arrancar e existir login funcional. Nao foi declarada aprovacao visual definitiva.

## 18. Futuro Upgrade

A vista mantem a entidade `Artigo`, os nomes de contrato e os endpoints atuais. O perfil `SERVICES_SIMPLE` oculta campos avancados sem remover dados. Num upgrade para `FULL`, os campos avancados podem voltar a ser expostos em outra composicao sem migracao de base de dados.

## 20. Validacao Funcional Com Backend

Backend:

- artefacto: `target/fac-0.0.1-SNAPSHOT.jar`;
- perfil activo: `demo`;
- base usada: `fac_demo`;
- seed/check/reset demo: desligados;
- healthcheck: `GET /actuator/health` devolveu `200`.

Endpoints testados:

- `GET /artigos?size=200&sort=codigo,asc`;
- `GET /artigos/{codigo}`;
- `POST /artigos`;
- `PUT /artigos/{codigo}`;
- `GET /familias?size=200&sort=descricao,asc`;
- `GET /tipos-taxa-iva?size=100&sort=descricao,asc`.

Servicos de validacao:

- `SVCTEST0629`: criado/editado via API, depois mantido inactivo;
- `SVCTESTUI0629`: criado via UI, depois mantido inactivo;
- `SVCTESTUI0630`: criado via UI durante repeticao de capturas, depois mantido inactivo.

Resultados:

- lista carregou dados reais;
- pesquisa e seleccao foram exercitadas na UI;
- detalhe mostrou codigo, descricao, unidade, preco, IVA e estado;
- criacao via API funcionou;
- criacao via UI funcionou;
- edicao via API funcionou;
- formulario de edicao abriu na UI;
- persistencia apos nova leitura foi confirmada pela API;
- perfil `CONSULTA` leu lista e recebeu `403` ao tentar criar;
- botoes de criacao/edicao dependem de `MESTRES_GERIR` no frontend;
- backend continuou a ser a autoridade final.

Payload de criacao relevante:

```json
{
  "codigo": "SVCTEST0629",
  "abreviatura": null,
  "codigoIdentificacao": null,
  "descricao": "Servico validacao comercial",
  "unidade": "UN",
  "familiaId": 1001,
  "peso": 0,
  "ivaCompraId": "INTERMEDIA",
  "ivaVendaId": "INTERMEDIA",
  "pvp": 123.45,
  "inativo": false,
  "retencao": false,
  "observacoes": null
}
```

Campos ocultos confirmados:

- `familiaId`: `1001`, familia existente `Servicos`;
- `peso`: `0`;
- `ivaCompraId`: `INTERMEDIA`, primeiro IVA activo obtido do catalogo;
- `abreviatura`, `codigoIdentificacao`, `observacoes`: `null`;
- `retencao`: `false`.

Durante a validacao foi corrigida uma fragilidade: a seleccao da familia oculta passou a privilegiar correspondencia exacta normalizada `Servicos`, antes de aceitar correspondencia parcial. Isto evita escolher `Cabazes e servicos` quando a ordenacao do catalogo a coloca antes.

## 21. Capturas Funcionais Reais

Capturas reais com backend:

- `desktop-lista-real-1440.png`;
- `desktop-lista-real-1024.png`;
- `desktop-detalhe-real-1440.png`;
- `desktop-criacao-real-1440.png`;
- `desktop-pos-criacao-real-1440.png`;
- `desktop-edicao-real-1440.png`;
- `desktop-validacao-real-1440.png`;
- `tablet-lista-real-768.png`;
- `mobile-lista-real-430.png`;
- `mobile-lista-real-375.png`;
- `mobile-detalhe-real-375.png`;
- `mobile-criacao-real-375.png`.

As capturas antigas `services-auth-required-*` foram mantidas apenas como evidencia inicial do estado antes do backend estar operacional.

## 22. Builds e Testes Posteriores

- `mvn -DskipTests compile`: passou;
- `mvn -DskipTests package`: passou;
- `npm run build`: falhou antes de entrar no projecto porque o `npm` global local procura `C:\Users\amgsr\AppData\Roaming\npm\node_modules\npm\bin\npm-cli.js`, inexistente;
- `tsc -b` via `node_modules/.bin`: passou;
- `vite build` via `node_modules/.bin`: passou;
- `mvn -q test`: arrancou mas excedeu 120 segundos neste ambiente; foram parados apenas os processos Java de teste pendurados. Relatorios Surefire foram produzidos parcialmente ate `18:01`.

## 19. Decisoes Pendentes

- Confirmar a familia oficial para novos servicos no catalogo real.
- Confirmar regra explicita para IVA de compra em servicos.
- Confirmar se devem existir codigos de unidade oficiais para hora, dia, mes, sessao, servico e avenca.
- Corrigir ou confirmar o arranque local do backend por Maven para permitir validacao funcional completa.
- Produzir capturas funcionais completas com dados reais e permissoes admin/consulta.
