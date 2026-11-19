# Pedido de revisão independente Claude Code — SAF-T F/FT-EUR

Rever o delta documental desde `a68c4d5f911cadc68acba926fa4ffcc89f3d1247` até ao commit que introduz este pedido. Referência administrativa: ordem de preparação F/FT-EUR registada no contrato. Reutilizar a aprovação da consolidação PR #61 e os oráculos R1.

Confirmar a cadeia campo → fonte ou lacuna → regra → aceitação, com atenção a TaxPointDate, regimes e Hash/HashControl. Conferir o inventário P12-03, o limite entre primeiro incremento e perímetro efectivamente habilitado, e a recusa de entrega fiscal perante documento sem cobertura. Rever os critérios de validação read-only, reconciliação, anulados e dados obrigatórios em falta. O [contrato e minuta](SAFT_F_FT_EUR.md) e o [inventário](SAFT_F_FT_EUR_P12_03.md) são os artefactos principais.

A confirmação aguarda encaminhamento para Claude Code. Integração por gate humano. A implementação do exportador depende de autorização própria.

## Confirmação incremental R1

Claude Code: rever apenas `a2733359042dc0c2d6948a94fa771e49fb63aa82..HEAD` desta entrega. Confirmar MINOR-001 (texto integral, proveniência, sidecar e referências nos dois documentos), MINOR-002 (N05 pp. 5–6, matriz F e recusa por Supplier exigível sem fonte), MINOR-003 (listas de pré-condições distintas) e O-1 (REPEATABLE READ read-only e commit concorrente entre inventário e extracção). A recolha P12-03 continua. A confirmação aguarda encaminhamento.
