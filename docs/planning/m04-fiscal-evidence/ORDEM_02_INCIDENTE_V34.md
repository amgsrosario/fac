# Ordem 02 — migração aplicada sem autorização específica

Em 25/11/2026, o arranque experimental `ar2-saft-20261125-app` aplicou a
migração Flyway V34 à base sintética `fac_demo` do cluster PostgreSQL
`7712147275187769382`, contentor `ar2-saft-20261125-db`, volume
`ar2-saft-20261125-data`. A base tinha sido criada de raiz e já continha o
cenário sintético inicial. A identidade demo desse cluster estava autorizada;
a migração de uma base existente não tinha autorização específica.

A causa foi o arranque de um JAR que incluía V34 com Flyway activo por
omissão, depois do carregamento inicial dos dados. A identidade do cluster fora
verificada, mas o efeito de Flyway sobre uma base já inicializada não foi
identificado como migração sujeita à reserva antes do arranque. A V34 foi
aplicada com sucesso; V35 não foi aplicada. Foi posteriormente criada a
factura sintética FT DEMO26/7 nessa instância. O backend experimental foi
parado. Não foi efectuado restauro nem substituição da base.

A base fica preservada no estado actual. Antes de qualquer novo arranque,
confirmar o cluster, a base, contentor, volume, versão Flyway instalada e
configuração efectiva de migrações. Em bases descartáveis novas, registar que
estão vazias antes do primeiro arranque. Uma migração posterior da base acima
exige autorização específica para o destino e versões em causa. A instalação
aceite na porta 18089 usa outro cluster e não foi migrada por esta operação.
