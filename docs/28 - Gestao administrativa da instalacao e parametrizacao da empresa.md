# MD 28 — Gestao administrativa da instalacao e parametrizacao da empresa

## Estado

Implementado no modulo FAC, mantendo a arquitetura existente:

- administracao de utilizadores via `/utilizadores`;
- parametrizacao da empresa emitente via `/empresa`;
- sem criacao de uma area paralela `/api/admin`;
- sem remocao fisica de utilizadores.

## Utilizadores

O campo `codigo` continua a ser o identificador funcional e tecnico do utilizador. Por existir historico e referencias por chave, o codigo e imutavel depois da criacao.

A gestao administrativa permite:

- criar utilizadores com perfil funcional;
- pesquisar por codigo, nome ou email;
- filtrar por perfil e estado;
- alterar nome e email;
- alterar perfil funcional;
- ativar/desativar utilizadores;
- redefinir password.

O FAC protege o ultimo `ADMINISTRADOR` ativo. Nao e permitido desativar o ultimo administrador nem retirar-lhe o perfil de administrador.

A redefinicao de password altera a credencial persistida e e auditada, mas nao revoga automaticamente tokens JWT ja emitidos. A autenticacao continua stateless nesta fase.

## Permissoes

Todas as operacoes administrativas em:

- `/utilizadores/**`;
- `/empresa/**`;

exigem a permissao funcional `CONFIGURACAO_GERIR`.

Tentativas negadas sobre areas administrativas sao registadas em auditoria como `TENTATIVA_ADMINISTRATIVA_NEGADA`.

## Empresa emitente

A ficha unica da empresa passou a suportar, alem dos dados legais ja existentes:

- nome comercial;
- telefone;
- IBAN;
- BIC/SWIFT;
- observacoes legais;
- texto de rodape;
- observacoes comerciais por defeito;
- logotipo.

Validacoes implementadas:

- NIF portugues validado quando o pais e `PT`;
- IBAN validado por MOD-97;
- BIC/SWIFT validado por formato;
- website limitado a `http` ou `https`;
- logotipo apenas PNG/JPEG;
- logotipo ate 1 MiB;
- imagem tecnicamente legivel e com dimensoes maximas controladas.

O logotipo e armazenado na base de dados em `bytea`, com metadados de media type e nome interno.

## Snapshot fiscal documental

Os documentos comerciais emitidos passam a consolidar snapshot fiscal v2 com os novos campos da empresa:

- nome comercial;
- telefone;
- IBAN;
- BIC/SWIFT;
- observacoes legais;
- texto de rodape;
- logotipo.

Documentos historicos com snapshot v1 continuam validos e imprimiveis. Alteracoes posteriores na empresa nao alteram documentos ja emitidos.

## Interface

A area de Configuracao passou a ter:

- Empresa — editor real da ficha da entidade emissora e logotipo;
- Utilizadores — gestao administrativa dedicada;
- Parametros — valores base existentes;
- Tabelas — catalogos de apoio.

A gestao de utilizadores foi retirada das tabelas tecnicas para evitar duplicacao funcional e para manter as regras especificas de seguranca, auditoria e protecao do ultimo administrador.

## Demo

O seed demo foi atualizado para usar NIF portugues valido e preencher campos comerciais da empresa demo:

- NIF `599000007`;
- telefone;
- IBAN;
- BIC/SWIFT;
- texto de rodape;
- observacoes legais;
- observacoes comerciais por defeito.

## Testes cobertos

- CRUD administrativo de utilizadores sem exposicao de password;
- password fraca e email duplicado;
- filtros por perfil, estado e pesquisa;
- protecao do ultimo administrador ativo;
- reset de password;
- criacao, consulta e atualizacao da empresa;
- validacao de NIF e IBAN;
- upload, consulta e remocao de logotipo;
- rejeicao de logotipo invalido;
- restricao de `/utilizadores/**` e `/empresa/**` por `CONFIGURACAO_GERIR`;
- auditoria de tentativas administrativas negadas;
- preservacao historica do snapshot fiscal v1/v2 em documentos e PDFs.
