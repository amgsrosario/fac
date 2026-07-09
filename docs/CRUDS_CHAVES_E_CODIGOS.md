# FAC — CRUDs, chaves e códigos funcionais

Este documento regista a decisão de produto sobre chaves técnicas, chaves naturais e códigos funcionais nas CRUDs auxiliares e comerciais do FAC.

O objectivo é evitar alterações estruturais desnecessárias e concentrar a próxima evolução apenas onde a mudança melhora claramente a experiência do utilizador e a coerência funcional.

## Princípio De Produto

- Evitar expor identificadores técnicos ao utilizador quando existir um código funcional claro.
- Manter a estrutura actual quando a entidade já está coerente, quando o identificador técnico é aceitável, ou quando a alteração não traz benefício funcional suficiente nesta fase.
- Não alterar entidades fiscais, documentais ou historicamente sensíveis sem necessidade comprovada.
- Separar alterações estruturais de ajustes funcionais futuros.

## Decisões Finais

| Entidade | Estado / decisão | Observações |
| --- | --- | --- |
| Armazém | Alteração aceite | Chave natural `String(3)`, exemplo `001`. A decisão final concorda com a alteração já feita. |
| País | Fica como está | O código ISO já é `String(3)`. Não propor alteração adicional se o estado actual estiver coerente. |
| Moeda | Fica como está | Manter estrutura actual. |
| Código Postal | Fica como está | Deve prever códigos postais estrangeiros no desenho funcional/futuro. |
| Modo de Pagamento | Candidato aprovado para alteração estrutural | Deve passar para `String(3)`. |
| Prazo de Pagamento | Fica como está | Manter estrutura actual. |
| Regime IVA | Fica como está | Manter estrutura actual. |
| Motivo de Isenção | Fica como está | Manter estrutura actual. |
| IVA SAF-T | Fica como está | Manter estrutura actual. |
| Transporte | Candidato aprovado para alteração estrutural | Deve passar para `String(3)`. |
| Clientes | Fica como está | Manter estrutura actual. |
| Artigos | Fica como está | Manter estrutura actual. |
| Documentos | Fica como está | Manter estrutura actual. |
| Séries | Fica como está | Manter estrutura actual. |

## Notas Por Entidade

### Armazém

Decisão final: concorda-se com a alteração já realizada.

O armazém deve usar uma chave natural curta, com `String(3)`, por exemplo:

- `001`;
- `LIS`;
- `ALG`;
- `BJA`.

O código do armazém é simultaneamente o identificador funcional visível e a chave da entidade.

### País

O país já usa código ISO em `String(3)`.

Decisão final:

- manter como está;
- não propor alteração adicional se a implementação actual continuar coerente com `String(3)`.

### Moeda

Decisão final: fica como está.

A moeda já tem uma identidade funcional própria no domínio e não fica aprovada para alteração estrutural nesta fase.

### Código Postal

Decisão final: fica como está.

Nota funcional futura: o desenho deve prever códigos postais estrangeiros, evitando assumir que todos os códigos postais seguem formato português.

### Modo De Pagamento

Decisão final: candidato aprovado para alteração estrutural.

O modo de pagamento deve passar para código funcional `String(3)`, mantendo uma identificação curta, legível e estável para o utilizador.

Esta alteração fica aprovada, mas deve ser feita numa tarefa própria, com inventário de relações, DTOs, frontend, seed/demo e testes.

### Prazo De Pagamento

Decisão final: fica como está.

Não há aprovação para alteração estrutural nesta fase.

### Regime IVA

Decisão final: fica como está.

Por impacto fiscal e por não haver benefício estrutural imediato, não deve ser alterado nesta fase.

### Motivo De Isenção

Decisão final: fica como está.

Por impacto fiscal e SAF-T, não deve ser alterado nesta fase.

### IVA SAF-T

Decisão final: fica como está.

Não alterar a estrutura nesta fase.

### Transporte

Decisão final: candidato aprovado para alteração estrutural.

O transporte deve passar para código funcional `String(3)`, por ser uma tabela auxiliar simples onde o utilizador beneficia de um código curto visível.

Esta alteração fica aprovada, mas deve ser feita numa tarefa própria, com inventário de relações, DTOs, frontend, seed/demo e testes.

### Clientes

Decisão final: fica como está.

Não alterar a chave ou estrutura de identificação nesta fase.

### Artigos

Decisão final: fica como está.

O artigo já trabalha com código funcional próprio e não fica aprovado para alteração estrutural nesta fase.

### Documentos

Decisão final: fica como está.

Não alterar chaves documentais, numeração, séries, ATCUD, QR, emissão, anulação ou regras fiscais no âmbito desta análise.

### Séries

Decisão final: fica como está.

A estrutura actual deve ser preservada, incluindo numeradores e código AT.

## Conclusão

Nesta fase, além do Armazém já tratado, apenas as seguintes entidades ficam aprovadas como candidatas a alteração estrutural para `String(3)`:

- Modo de Pagamento;
- Transporte.

Todas as restantes entidades devem manter a estrutura actual, salvo ajustes funcionais futuros que não impliquem alteração estrutural de chave/código.

Qualquer alteração estrutural futura deve ser feita em tarefa isolada, com:

- inventário de entidades, DTOs, mappers, repositories, services e controllers;
- inventário de relações e foreign keys;
- revisão do frontend e tipos TypeScript;
- plano de migration Flyway;
- revisão de seed/demo;
- testes backend e frontend adequados;
- confirmação explícita de que não afecta regras fiscais, emissão, anulação, PDF, SAF-T, QR, ATCUD ou cálculos.
