# Mandato FR/EUR — clarificação administrativa

Proveniência: mensagem do utilizador/Administração neste chat, recebida em 17/11/2026, sem identificador externo disponibilizado. Registo posterior à ordem preservada em AUTHORIZATION.md, que permanece intacta. O texto abaixo confirma o mandato e delimita expressamente o levantamento da suspensão.

> **Mandato FR/EUR autorizado, com levantamento da suspensão limitado a este incremento.** A FR será tratada como implementação em falta do produto aprovado.
>
> **Ordem ao executor — Codex Desktop**
>
> Executar em branch isolada o circuito **FR/EUR, inicialmente inactivo**, com:
>
> - Facturação e quitação integral no mesmo documento, sem criar um RC fiscal artificial.
> - Recebimento registado uma única vez e saldo final zero.
> - Vínculos rastreáveis e integração coerente com a autoridade financeira, incluindo a data dos efeitos nas consultas históricas.
> - Idempotência perante repetição e concorrência.
> - Atomicidade e rollback, incluindo falhas durante a emissão.
> - Preservação dos circuitos FT/RC e dos controlos normais de emissão e segurança.
>
> Utilizar novas migrations quando necessárias, preservando as anteriores. Ensaiar exclusivamente com dados sintéticos, PostgreSQL descartável e assinatura de teste. **Inicialmente inactivo** significa sem acesso pelos consumidores operacionais nem configuração fiscal publicada.
>
> Fixar HEAD e delta, arquivar expectativas e resultados, comprovar isolamento e remoção dos recursos e submeter a implementação à **revisão independente Claude Code**, nas dimensões técnica e de fidelidade ao modelo aprovado.
>
> Actualizar memória institucional, estado operacional e o **ponto 2 — Circuito comercial**, distinguindo implementação, prova, revisão e integração.
>
> **Sem push, PR, merge, activação comercial ou levantamento dos restantes bloqueios.** Escalar apenas impedimentos materiais ou decisões que ultrapassem este mandato.
