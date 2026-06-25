# MD 29 — Importacao e exportacao de dados mestres

## Estado

Implementado para dados mestres de clientes e artigos.

## Formatos

Suportados:

- CSV UTF-8 com separador `;`, BOM tolerado, aspas e acentos;
- XLSX sem macros, processando apenas a primeira folha.

Nao suportado:

- XLS antigo;
- documentos comerciais/financeiros;
- atualizacao ou fusao automatica de registos existentes.

## Limites

- tamanho maximo: 10 MB;
- maximo de 10 000 linhas;
- maximo de 100 colunas;
- sessoes de importacao validas por 30 minutos;
- uma folha XLSX processada.

## Estrategia de importacao

A importacao e feita em duas fases:

1. validacao do ficheiro;
2. confirmacao da importacao.

Na validacao nao e gravado qualquer cliente ou artigo. O FAC cria apenas uma sessao temporaria e auditavel em `importacao_dados_mestres`, com dados normalizados e validade curta.

Na confirmacao, o backend revalida duplicados e regras criticas antes de gravar. Uma sessao confirmada, cancelada ou expirada nao pode ser reutilizada.

## Politica transacional

A confirmacao grava apenas linhas validas. Linhas com erro nunca sao gravadas. Cada confirmacao e executada de forma controlada e impede dupla confirmacao com bloqueio pessimista sobre a sessao.

## Duplicados

Clientes:

- NIF duplicado no ficheiro: erro;
- NIF existente na base: erro;
- email alternativo igual ao principal: aviso.

Artigos:

- codigo duplicado no ficheiro: erro;
- codigo existente na base: erro;
- codigo de identificacao duplicado no ficheiro ou na base: erro;
- abreviatura igual a descricao: aviso.

Nao ha atualizacao massiva nem merge automatico neste MD.

## Seguranca

- ficheiros vazios sao rejeitados;
- extensoes fora de `.csv` e `.xlsx` sao rejeitadas;
- nomes com path traversal sao rejeitados;
- XLSX com formulas e rejeitado;
- exportacoes CSV/XLSX protegem contra formula injection prefixando valores perigosos;
- conteudo integral dos ficheiros nao e guardado em auditoria;
- ficheiros nao sao guardados em diretorios publicos.

## Permissoes

Novas permissoes:

- `DADOS_MESTRES_IMPORTAR`;
- `DADOS_MESTRES_EXPORTAR`.

Perfis:

- ADMINISTRADOR: importa e exporta;
- OPERADOR: exporta;
- CONSULTA: exporta;
- OPERADOR e CONSULTA nao importam.

## Endpoints

Importacao:

- `POST /importacoes/clientes/validar`
- `POST /importacoes/clientes/{id}/confirmar`
- `DELETE /importacoes/clientes/{id}`
- `GET /importacoes/clientes/modelo?formato=csv|xlsx`
- `POST /importacoes/artigos/validar`
- `POST /importacoes/artigos/{id}/confirmar`
- `DELETE /importacoes/artigos/{id}`
- `GET /importacoes/artigos/modelo?formato=csv|xlsx`

Exportacao:

- `GET /exportacoes/clientes?formato=csv|xlsx`
- `GET /exportacoes/clientes?formato=csv|xlsx&ativos=true`
- `GET /exportacoes/artigos?formato=csv|xlsx`
- `GET /exportacoes/artigos?formato=csv|xlsx&ativos=true`

No frontend Vite os mesmos endpoints sao chamados com prefixo `/api`.

## Campos de clientes

Modelo:

`nome;morada;morada1;localidade;nif;tel;tm;email;email1;tspiva;iban;retencao;inativo;observacoes;codPostalId;paisId;moedaId;mPagamentoId;pPagamentoId;rivaId;transporteId`

Obrigatorios:

- `nome`;
- `morada`;
- `nif`;
- `email`;
- `codPostalId`;
- `paisId`;
- `moedaId`;
- `transporteId`.

## Campos de artigos

Modelo:

`codigo;abreviatura;codigoIdentificacao;descricao;unidade;familiaId;peso;ivaCompraId;ivaVendaId;pvp;inativo;retencao;observacoes`

Obrigatorios:

- `codigo`;
- `descricao`;
- `unidade`;
- `familiaId`;
- `ivaCompraId`;
- `ivaVendaId`;
- `pvp`.

## Auditoria

Eventos adicionados:

- `IMPORTACAO_VALIDACAO_RECEBIDA`;
- `IMPORTACAO_VALIDACAO_CONCLUIDA`;
- `IMPORTACAO_CONFIRMADA`;
- `IMPORTACAO_CANCELADA`;
- `IMPORTACAO_CONCLUIDA`;
- `IMPORTACAO_PARCIAL`;
- `IMPORTACAO_FALHADA`;
- `EXPORTACAO_DADOS_MESTRES`;
- `TENTATIVA_IMPORTACAO_NEGADA`.

## Interface

Foi adicionada a area principal `Importar/Exportar`, visivel para utilizadores com `DADOS_MESTRES_IMPORTAR` ou `DADOS_MESTRES_EXPORTAR`.

Permite:

- escolher clientes ou artigos;
- escolher CSV ou XLSX;
- descarregar modelo;
- validar ficheiro;
- ver resumo, erros, avisos e amostra;
- confirmar ou cancelar a importacao;
- exportar todos ou apenas ativos.

## Compatibilidade demo

A implementacao nao altera o seed demo nem importa dados automaticamente. Os modelos sao gerados dinamicamente e tambem existem exemplos estaticos em `docs/modelos`.

O Nginx da demo (`deploy/demo/nginx.conf`) declara `client_max_body_size 10m`, alinhado com o limite do backend.

## Limitacoes

- nao ha importacao assíncrona;
- nao ha atualizacao massiva;
- nao ha historico de detalhe fora da auditoria e da sessao temporaria;
- XLSX usa POI com limite de 10 MB para manter consumo de memoria controlado.
