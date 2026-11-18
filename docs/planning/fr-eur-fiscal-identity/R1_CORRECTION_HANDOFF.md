# FR/EUR — confirmação incremental Claude Code após correcção

Rever o delta cumulativo desde `3ce7039148388dbd6f9a67ff364f301cde8e7e88` até ao commit que introduz este documento. Branch `codex/fr-eur-fiscal-identity`. Código e testes executados em `312511b94486826ff0c16ef23081faf53c392bc8`, tree `62aa8aad187635705dd644dbae332dcf7b2045f2`. Parecer original e sidecar conferidos na R1. SHA-256 `5e1b2edd094e50b35389972b20721bcd9b52d21982b5605ef46a0dccd823529c`.

## MINOR-001

- NOAT exige `BadRequestException` com a mensagem «A série selecionada não possui código de validação atribuído pela AT.» e verifica ausência de efeitos.
- O UPDATE que tenta vincular à FR um tipo com histórico FT é recusado primeiro pela V27. Uma segunda tentativa, numa transacção descartável revertida, isola a guarda V27 e exige a recusa de identidade de `fiscal_claim_identity` da V31. A configuração e o estado permanecem iguais. I04(b) continua a provar a preservação do percurso legado. **C6 aberto.**
- `TEST_MATRIX.md` atribui à comparação Git com a base integrada a igualdade do manifesto produtivo.

## MINOR-002

A V31 local permite que um UPDATE que altera apenas o numerador saia da guarda de séries antes dos locks consultivos. Compara os restantes campos com semântica segura para NULL. A numeração continua a usar o bloqueio normal da linha. INSERT e alterações de identidade continuam no percurso original com locks de série e código AT, mutex e verificações de unicidade.

O mesmo ensaio de escritas FT em PostgreSQL descartável comparou V30 e V31. Mostra-se o tempo total do segundo escritor. Para tornar a espera observável, quando o monitor detectou um bloqueio manteve a primeira transacção aberta por mais 200 ms. As medições distinguem existência e origem do bloqueio, sem estimar latências de produção.

| Chaves dos escritores | V30 original | V31 anterior | V30 na repetição | V31 corrigida |
| --- | ---: | ---: | ---: | ---: |
| Mesmo nome de série, tipos distintos | 8 ms | 224 ms, advisory de série | 6 ms | 8 ms, sem espera |
| Mesmo tipo, séries distintas | 219 ms | 222 ms, transactionid | 220 ms | 221 ms, transactionid |
| Tipo, série e código AT distintos | 6 ms | 11 ms | 6 ms | 10 ms, sem espera |

As 12 transacções na repetição confirmaram. Em cada par o teste verificou o próximo numerador, o valor persistido e exactamente um documento emitido por número e série. Nos casos de tipos distintos o segundo escritor confirmou antes de libertar o primeiro. Os checkpoints de `pg_locks` ficaram vazios após a numeração. A emissão continua a adquirir `FISCAL_CODE` depois da escrita V25 na linha do tipo. O ensaio SQL mede as escritas e os locks. As regressões de emissão FR/FT/RC são provas separadas.

A suite FR manteve a prova concorrente de identidade com snapshot REPEATABLE READ. O caso de colisão cobre ainda tentativas de mudar simultaneamente o numerador e a série ou o código AT. Ambas são recusadas e o numerador permanece zero.

A execução anterior está em `target/fr-projection-evidence/20261118-125206-88e97d` com manifesto SHA-256 `e6d01e772ad9dc49029ba15ff923960db72cab7a072bd03418257ee09ebb819c`. A repetição está em `target/fr-projection-evidence/20261118-142658-dec609`.

Claude Code deve confirmar os três oráculos do MINOR-001, a fronteira da exclusão e a preservação das guardas de identidade, os resultados comparativos e as regressões do MINOR-002. O circuito continua inactivo. Confirmação independente pendente.

Resultados da repetição: 158 aprovados em 7 classes, zero falhas, erros ou ignorados. Comparação concorrente 1, projecção FR 19, upgrade V30→V31 e segundo arranque 1, circuito financeiro FR 12, recebimentos 80, catálogo FT 19 e integridade comercial 26. Manifesto da repetição SHA-256 `92363c5fec79d735c4a5c5993912e14666612ba50b65e259d2c7a6d0ed1343ef`. Remoção confirmada pelo disposal.json. Os valores e a proposta do R1.md são o registo anterior à correcção.
