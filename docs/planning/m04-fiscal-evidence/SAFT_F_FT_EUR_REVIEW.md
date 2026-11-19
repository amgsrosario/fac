# Pedido de revisão independente Claude Code — SAF-T F/FT-EUR

Rever o delta documental desde `a68c4d5f911cadc68acba926fa4ffcc89f3d1247` até ao commit que introduz este pedido. Referência administrativa: ordem de preparação F/FT-EUR registada no contrato. Reutilizar a aprovação da consolidação PR #61 e os oráculos R1.

Confirmar a cadeia campo → fonte ou lacuna → regra → aceitação, com atenção a TaxPointDate, regimes e Hash/HashControl. Conferir o inventário P12-03, o limite entre primeiro incremento e perímetro efectivamente habilitado, e a recusa de entrega fiscal perante documento sem cobertura. Rever os critérios de validação read-only, reconciliação, anulados e dados obrigatórios em falta. O [contrato e minuta](SAFT_F_FT_EUR.md) e o [inventário](SAFT_F_FT_EUR_P12_03.md) são os artefactos principais.

A confirmação aguarda encaminhamento para Claude Code. Integração por gate humano. A implementação do exportador depende de autorização própria.
