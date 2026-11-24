# SEC-PW-01

Base confirmada por HTTPS: `f3308cfca06d9c33c9164d7a92232c35b8e143fb`.

- `docs/05 - Implementação da Camada de Segurança.md`, §15: remoção do exemplo e remissão para o provisionamento local seguro M02.
- `docs/13 - Infraestrutura segura de testes e backup PostgreSQL.md`: marcador para a palavra-passe do PostgreSQL de teste.
- `docs/fiscal/catalog/VALIDATION.md`: marcador nas duas invocações que exemplificavam a palavra-passe do PostgreSQL de teste.
- Política: inclusão do exemplo retirado na lista de recusa. Mantêm-se a normalização e a regra do código do utilizador.
- Testes dirigidos: casos exacto, insensível a maiúsculas, variantes semelhantes e entradas anteriores da lista. `UtilizadorControllerTests` cobre os pontos de entrada de criação e alteração. A família `FirstAdministrator*Tests` cobre o provisionamento local, autorização, fronteira, terminal e login.

A procura do valor retirado em `src/test` e `src/main/resources` na base não identificou ocorrências. `docs/historico/` fica preservado.
