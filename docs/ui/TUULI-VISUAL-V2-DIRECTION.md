# TUULI Visual v2

## Direção

- Arquitetura e design editorial como referência.
- Menos bold; maior uso de pesos 400/500.
- Peso 600 apenas quando necessário; 700 raro ou inexistente.
- Hierarquia por escala, alinhamento e espaço.
- Menos caixas e menos botões pesados.
- Mais branco e tipografia mais silenciosa.
- Grelha rigorosa.
- Números fortes, mas não agressivos.
- Identidade TUULI prolongada para a interface.
- Não procurar modernizar; procurar disciplinar.

**Mais ar. Menos mobiliário.**

A Visual v2 foi validada primeiro em `Documentos` e depois em `Clientes`. A aplicação a novos módulos continua a exigir validação visual e funcional própria.

As decisões funcionais consolidadas na Visual v1 permanecem válidas e não fazem parte desta exploração.

## Tabelas

- As células de dados usam uma escala tipográfica única por defeito.
- `14px / 400` é a referência atual no ecrã `Documentos`.
- A hierarquia resulta de cor, alinhamento e semântica, não de micro-hierarquias tipográficas por coluna.
- Os cabeçalhos usam uma escala própria de sinalização: `12px / 400`, tracking `0.045em` e cor secundária.

**Uma linha é um sistema tipográfico único.**

## Sistema visual

### Tokens

Os tokens centrais vivem em `frontend/src/ui/fac/theme/fac-tokens.css`. A camada Visual v2 consolida uma escala pequena para tipografia, texto, superfícies, linhas, geometria e espaço. Os nomes técnicos `fac-*` podem coexistir com tokens `tuuli-*` quando a sua alteração não acrescenta valor funcional.

### Tipografia

- `400`: estado normal.
- `500`: âncora secundária, apenas quando necessária.
- `600`: grandes âncoras, como o título de página.
- `700`: evitar.

**Hierarquia por cor, escala, posição e espaço antes de font-weight.**

### Primitivas

A infraestrutura reutilizável vive em `frontend/src/ui/tuuli/tuuli-v2.css` e inclui página, métricas, pesquisa, toolbar, contexto, tabela, células semânticas, estado e paginação. `EntityDetailOverlay` continua a ser a primitiva partilhada para slide-over e bottom sheet.

As primitivas são classes semânticas pequenas. Não incluem componentes específicos como linhas de Documentos.

### Regras de tabela

- Uma linha usa uma escala tipográfica única por defeito.
- Conteúdo principal e secundário distinguem-se por cor.
- Números distinguem-se por alinhamento, `tabular-nums` e `nowrap` quando apropriado.
- Estados distinguem-se semanticamente, sem depender de bold.
- Cabeçalhos mantêm escala própria de sinalização.

### Regras numéricas

Os helpers em `frontend/src/ui/tuuli/format.ts` usam locale `pt-PT`. Valores monetários preservam separadores de milhares e vírgula decimal; células numéricas alinham à direita e usam `tabular-nums`.

### Ações e pesquisa

Ações secundárias usam peso `400`, baixa ornamentação e hover subtil. A ação principal usa azul TUULI sem depender de bold. A pesquisa usa borda subtil, fundo leve, ícone discreto e focus TUULI.

### Espaço e contexto

O espaço organiza a hierarquia antes de caixas adicionais. Barras de contexto apresentam seleção e ações imediatas sem competir com a tabela; ações menos frequentes ou destrutivas podem viver num menu explícito.

### Gramáticas de ecrã

- **Listagem simples:** título, indicadores opcionais, toolbar e tabela.
- **Entidade + listagem:** título, contexto da entidade, indicadores/contexto, toolbar, tabela e detalhe temporário.
- **Coleção documental:** título, indicadores da coleção, toolbar, contexto da seleção, tabela e overlay.
- **Analítico:** título, filtros, indicadores e painéis analíticos.

As gramáticas são regras de composição, não componentes rígidos.

**Documentos é a primeira implementação oficial e referência, não um template literal.**

**Clientes é a segunda implementação oficial e valida a gramática Entidade + listagem.**

A Conta Corrente de Clientes é uma subestrutura tabular da mesma gramática: preserva escala tipográfica única por linha, hierarquia semântica por cor e alinhamento, totais discretos e paginação comum.

**Recebimentos é uma implementação oficial de composição híbrida transacional.**

Duas coleções relacionadas coexistem sem se tornarem dois cards; filtros e ferramentas podem ocupar duas linhas funcionais deliberadas. O detalhe documental reutiliza a linguagem existente, enquanto a distribuição usa um workspace transacional específico e validado. Este workspace não constitui, por enquanto, uma nova gramática global.

**Não esconder complexidade funcional. Organizar complexidade funcional.**

**Dashboard é a implementação oficial da gramática Analítica.**

Métricas e áreas analíticas podem ser definidas por espaço, títulos e divisores, sem cards pesados. Pouca densidade de dados não deve ser compensada com decoração; no mobile, preservar ar não significa acrescentar altura. Atalhos operacionais também podem existir como uma estrutura leve, sem cards.

**Listagens e análise é uma implementação oficial da composição Seletor de análise + listagem operacional.**

A seleção de perspetiva funciona como índice editorial e não deve dominar os dados selecionados. A configuração de visibilidade e ordem das colunas é uma ferramenta leve, baseada em linhas e divisores, não um painel de mini-cards. No mobile, todas as perspetivas permanecem acessíveis sem impor um dropdown. Esta composição continua contextual e não constitui uma gramática global rígida.

**Login é a implementação oficial da entrada editorial TUULI Visual v2.**

A autenticação não precisa de um card para ter estrutura: marca e formulário são ancorados por alinhamento, espaço e disciplina tipográfica. O único CTA mantém azul forte sem tornar toda a superfície pesada, e o autofill do browser não justifica CSS frágil. Com este baseline, a migração Visual v2 dos principais ecrãs operacionais fica completa.

**Consistência visual não significa comportamento funcional idêntico.**

### Classificação da implementação de referência

- **Global TUULI:** tokens, tipografia, cores, espaço, pesquisa, ações, tabela, numeração e overlay.
- **Gramática:** métricas da coleção, toolbar, contexto da seleção, tabela e detalhe temporário.
- **Específica de Documentos:** colunas, larguras mínimas, estados documentais, permissões e ações por estado.
- **Legado:** o bloco CSS histórico `.fac-documents-v2`, mantido inativo até uma remoção isolada e verificável.
