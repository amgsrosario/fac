# M4-12 R1 — Correcções documentais e fundamentos

13-11-2026. Partida dd44be99cf30f7e7746efd5bc8ad79f630cc85cd. **Proposta corrigida, confirmação independente pendente; contrato aberto.** Sem código, XML ou ensaios.

## Parecer original preservado

`2026-11-13_TUULI_M4-12_revisao-independente_Claude.md`, arquivo `Relatorios-AR2/TUULI`, SHA-256 calculado igual ao sidecar: `32dd54ce0fe3adcc80350aac99bcd695db857a2b65ef472045429eba3501b047`. REQUEST CHANGES: 0 BLOCKER, 1 MAJOR, 1 MINOR. O original não é substituído por esta resposta; cópia e sidecar preservados no dossier R1 externo.

## MAJOR-001 — UnitPrice

**Fonte normativa:** [N05, Portaria 302/2016, Anexo I](https://info.portaldasfinancas.gov.pt/pt/informacao_fiscal/legislacao/diplomas_legislativos/Documents/Portaria_302_2016.pdf), §4.1.4.19.7 (página 29/105 do exemplar AT): preço unitário líquido de descontos de linha e cabeçalho, excluindo impostos; preencher 0.00 quando obrigatório TaxBase. §4.1.4.19.8 trata separadamente a base tributável específica. Fontes e hashes arquivados em M4_12_SOURCES.json; N07 define UnitPrice como SAFmonetaryType, decimal não negativo, sem fractionDigits. A ausência dessa restrição não fixa por si uma precisão fiscal aprovada.

**Facto no código:** MotorMonetario.calcular arredonda quantidade × preço a 2 casas HALF_UP, calcula o desconto sobre esse bruto, arredonda o desconto e obtém a base líquida. Portanto, L.precoUnitario é entrada bruta. LinhaDocumentoComercialService agrega os descontos das linhas em D.valorDesconto; esse agregado não demonstra um desconto adicional de cabeçalho nem pode ser subtraído novamente. Não se encontrou nesse percurso uma origem separada de distribuição de desconto de cabeçalho.

**Derivação técnica proposta, não fórmula imposta pela Portaria:** para quantidade positiva q e montante líquido persistido N da linha, já depois de todos os descontos efectivos e antes de impostos, obter U = N/q. Verificar primeiro a legitimidade/coerência de N (baseTributavel/valorLinha) no caso concreto. Nunca usar total com IVA, mestre actual ou deduzir duas vezes descontos. Se existir desconto de cabeçalho efectivo, a origem da sua afectação à linha tem de estar demonstrada: esta missão não escolhe método proporcional nem inventa uma distribuição. Quantidade inválida ou origem líquida ambígua exige diagnóstico, não divisão por zero ou preenchimento fictício.

**Precisão e arredondamento propostos para revisão:** conservar os decimais persistidos e usar aritmética decimal, sem double. Divisão exacta quando finita; quando periódica, serializar com escala 12 e HALF_UP, sem arredondamento intermédio adicional. Trata-se de escolha técnica proposta, não de exigência legal de 12 casas, nem alteração da escala dos dados persistidos. No caso periódico, a diferença absoluta entre q × U serializado e N está limitada a q × 0.5 × 10^-12. Registar essa diferença; não forçar igualdade exacta nem alterar CreditAmount, NetTotal ou GrossTotal para a eliminar. A aceitação futura deve conferir o limite matemático e a interpretação fiscal, sem converter esse limite em tolerância monetária geral. Excepção normativa TaxBase: U=0.00 por essa condição, não porque falta preço ou assinatura.

### Oráculos documentais discriminantes — não executados

| Caso futuro | Dados e cálculo independente | Resultado esperado / discriminação |
| --- | --- | --- |
| Desconto efectivo de linha e divisão periódica | q=3; preço bruto=1.00; bruto=3.00; desconto de linha em valor=2.00; sem desconto adicional de cabeçalho; N=1.00 | UnitPrice exacto=1/3; serializado proposto=0.333333333333; q × U=0.999999999999. Não exportar o preço bruto 1.00; valor líquido continua 1.00; desconto aplicado da linha=2.00 |
| Linha e cabeçalho efectivos, com afectação explicitamente dada | q=3; preço=10.00; bruto=30.00; desconto de linha=3.00; parcela de cabeçalho já comprovada para esta linha=2.00; N=25.00 | U=25/3; proposta=8.333333333333. Não 10.00 (bruto), 9.00 (só linha) nem novo desconto sobre 25.00. A parcela 2.00 é premissa sintética futura, não método aprovado nem capacidade actual demonstrada |
| TaxBase obrigatório | Enquadramento fiscal que obriga esse campo, sustentado em fonte própria | UnitPrice=0.00; TaxBase válido separado; não extrapolar o caso para FT normal nem inventar o enquadramento |

Os futuros testes devem conferir preço de origem, descontos aplicados, líquido persistido, UnitPrice exportado, resíduos de divisão, impostos e totais independentemente; incluir desconto percentual com bruto arredondado. Estes cálculos documentais não são execução de testes nem novos resultados M4-09.

## MINOR-001 — Desconto aplicado e acordo futuro

N05 §4.1.4.19.18 (Line/SettlementAmount): descontos que já afectam GrossTotal. §§4.1.4.20.5.1/.2: SettlementDiscount/SettlementAmount do documento descrevem desconto acordado para o futuro sem modificar o GrossTotal presente; .5.3 é a data acordada para pagamento com desconto; .5.4 são termos/prazo de pagamento. Os acordos futuros não têm origem dedicada demonstrada; condições de pagamento existentes podem sustentar PaymentTerms, nunca provar por si um desconto futuro.

Exemplo documental futuro: líquido 27.00, IVA 23%=6.21, GrossTotal=33.21 após desconto aplicado de 3.00 sobre bruto 30.00. Um acordo futuro legítimo de desconto 1.00 é declarado separadamente e mantém GrossTotal=33.21; não 32.21. Não deduzir novamente o desconto aplicado nem transformá-lo em acordo futuro. Taxa usada só como premissa aritmética, sem homologar regimes ou efectuar ensaio.

## Outras observações do parecer

- O-1: TaxEntity reflecte as regras condicionais N05 §1.13 (Global/Sede/estabelecimento), não um default inventado; P12-03 determina os factos da instalação.
- O-2: N05 §4.1.4.4 prevê Hash=0 quando não há obrigação de certificação; §4.1.4.5 prevê HashControl=0 para programa não certificado. São condições distintas. Não se demonstra aqui a sua aplicabilidade ao TUULI, nem se autoriza emissão/entrega sem assinatura no perfil certificado pretendido. P12-03 exige apurar obrigação, estado efectivo e certificado/release legítimos, com validação fiscal; uma decisão do PO não dispensa obrigação legal.
- O-3: perfil P refere extracção parcial por períodos nas condições da nota geral i), incluindo aplicação integrada; não subconjunto arbitrário de tipos nem sinónimo de primeira fatia técnica.
- O-4: P12-02 inclui decisão explícita sobre Supplier, GeneralLedgerAccounts/Entries, MovementOfGoods, WorkingDocuments e Payments, ainda sem mapping completo. Não os omitir por conveniência.
- O-5: espaços editoriais corrigidos apenas no material M4-12 afectado. Registos históricos não são reescritos.

## Confirmação incremental solicitada

Rever o delta documental a partir de dd44be9: MAJOR-001, MINOR-001 e O-1 a O-5, incluindo a proposta de precisão, descontos de cabeçalho sem distribuição inventada, distinção das condições de Hash/HashControl e decisões P12. Confirmar ou apontar reservas; não declarar aprovação antes do parecer. Sem novos ensaios. Permanecem abertos ATCUD: e bruto/IVA/total no diálogo; assinatura produtiva e implementação SAF-T exigem mandatos próprios.
