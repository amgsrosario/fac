# Ordem persistida das linhas documentais

Data: 2026-07-03

Ambito: backend real dos documentos comerciais. Nao houve integracao da nova UI em `/documentos`, nem alteracoes funcionais no laboratorio.

## Endpoint

`PUT /documentos-comerciais/{documentoId}/linhas/ordem`

Corpo:

```json
{
  "linhaIds": [15, 12, 18, 21]
}
```

Resposta: `200 OK` com as linhas reordenadas no formato `LinhaDocumentoComercialDto`, seguindo o padrao de leitura existente.

## DTO

Foi criado `ReordenarLinhasDocumentoDto`, com `linhaIds: List<Long>`. Bean Validation garante lista nao nula e elementos nao nulos; o service continua a validar todo o conjunto recebido.

## Validacoes

O service rejeita a operacao quando:

- o documento nao existe;
- o documento nao esta em `RASCUNHO`;
- a lista e nula;
- a lista esta vazia quando o documento tem linhas;
- ha ids duplicados;
- a lista nao representa exatamente todas as linhas atuais do documento;
- ha ids em falta, ids adicionais, ids inexistentes ou ids de outro documento.

A permissao usada e a ja existente: `DOCUMENTO_EDITAR_RASCUNHO`.

## Estrategia Contra Colisao

Como `numero_linha` e unico por documento, a reordenacao nao atualiza diretamente `1 -> 2` e `2 -> 1`.

A rotina transacional aplica duas fases:

1. atribui numeros temporarios negativos fora do intervalo normal;
2. executa `flush`;
3. atribui `1..N` conforme a nova ordem;
4. executa `flush` final.

Isto evita depender da ordem acidental de updates do Hibernate e preserva a constraint unica.

## Atomicidade

A validacao do conjunto acontece antes de qualquer alteracao. A escrita decorre dentro da mesma transacao do service. Em erro de validacao, nenhuma linha e renumerada.

## DELETE

Depois de remover uma linha em documento `RASCUNHO`, o service faz `flush` da remocao e compacta a ordem restante para `1..N`, preservando a ordem relativa. A rotina e a mesma estrategia segura de numeros temporarios.

Linhas `COMERCIAL` e `TEXTO` sao tratadas da mesma forma para ordenacao e compactacao.

## Compatibilidade

Nao foi criada nova migracao para esta etapa. A Etapa 2 reutiliza `numero_linha` e a constraint unica existentes.

Nao foram alterados:

- tipo ou constraint de `numero_linha`;
- calculos fiscais;
- PDF;
- diagnostico;
- emissao;
- descricao `varchar(80)`;
- modulo visual `/documentos`.

## Testes

Foram adicionados testes de controller para:

- reordenacao simples de tres linhas comerciais;
- mistura de linhas `TEXTO` e `COMERCIAL`;
- rejeicao de duplicados, lista incompleta, id inexistente, id de outro documento e lista vazia indevida;
- protecao em `EMITIDO` e `ANULADO`;
- compactacao apos `DELETE`, incluindo linha `TEXTO`.

## Limitacoes

A execucao dos testes de controller depende da base PostgreSQL de teste em `localhost:25432`. Nesta ronda, a compilacao passou, mas a execucao dos testes com contexto Spring ficou bloqueada porque a base recusou ligacao.

## Proxima Etapa

Quando a BD de teste estiver disponivel:

1. executar os testes especificos de reordenacao;
2. executar `mvn -q test`;
3. so depois integrar chamadas de API na futura UI documental.
