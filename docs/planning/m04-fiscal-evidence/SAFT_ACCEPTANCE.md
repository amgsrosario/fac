# M4-12 — Aceitação de futura implementação, não executada

13-11-2026. Critérios propostos complementam ST01–ST08 do [contrato](SAFT_CONTRACT.md). Não são resultados de testes. Sem motor XML instalado, XML gerado, teste, SQL ou Docker nesta missão.

| ID | Critério verificável para uma ordem futura | Dependência / prova necessária |
| --- | --- | --- |
| A12-01 | Fixar perfil, tabelas, tipos, período/estabelecimento e release autorizados; recusar pedidos que excedam esse âmbito | Decisões P12-01/02/03 e contrato revisto; nenhuma homologação por enum do XSD |
| A12-02 | XSD 1.04_01/namespace/hash fixados; motor compatível com vc:minVersion 1.1 e regras de conteúdo, não só XML bem formado | Instalação/validação só em futura ordem; exemplos positivos/negativos sintéticos. O parse estrutural actual não conta como validação |
| A12-03 | Header legítimo, coerente com documentos/instalação; produtor≠emitente quando aplicável; tamanhos/formato validados sem truncagem silenciosa | Dados/release/certificado autorizados; não obter assinatura produtiva de demo ou R do QR |
| A12-04 | Selecção completa e snapshot consistente; rascunhos excluídos e anulados incluídos; ordenação estável e lotes sem N+1/omissões/duplicações | Ensaios de paginação/concorrência apenas em PostgreSQL descartável; não reutilizar findAnaliticas que exclui anulados |
| A12-05 | CustomerID/ProductCode/TaxTable e referências únicas/coerentes; diagnosticar identidades ausentes ou incompatíveis; não corrigir mestres/históricos | Política explícita de fonte/data de referência; alterações posteriores do mestre não reinterpretam campos consolidados |
| A12-06 | FT/EUR: InvoiceNo/ATCUD/InvoiceType e campos monetários concordam com snapshots e oráculos independentes; UnitPrice derivado líquido de descontos de linha/cabeçalho e sem impostos, precisão/resíduo e excepção TaxBase conforme R1 | Reutilizar provas M4-02/04/09/10, acrescentando só o delta de exportação autorizado. Não atribuir provas antigas a novo HEAD |
| A12-07 | InvoiceDate/SystemEntryDate/TaxPointDate/vencimento/geração distintos e sustentados em eventos; testar prestação anterior à emissão | Falta de data/evento obrigatório provoca diagnóstico, não fallback de emissão ou meia-noite inventada; eventual recolha nova é outro mandato |
| A12-08 | InvoiceStatus/Date/Reason/autor reflectem o estado documental legítimo; anulação não reescreve QR/número/ATCUD | Cenário documental sintético autorizado; não decide temporalidade financeira da anulação FT |
| A12-09 | NumberOfEntries e TotalDebit/TotalCredit obedecem ao conjunto/estados da tabela; NetTotal/TaxPayable/GrossTotal conciliados | Diferenciar conta de vendas, dívida do cliente e tesouraria. Testes exactos de valores e descontos sem dedução duplicada; desconto aplicado na linha distinto do acordo futuro documental, que não altera GrossTotal; falta de acordo não autoriza inventá-lo |
| A12-10 | No perfil certificado pretendido, Hash=assinatura integral autêntica ligada ao documento; HashControl=versão da chave usada; divergência/falta recusa entrega fiscal. P12-03 deve comprovar obrigação e estado de certificação; condições normativas para 0 não presumidas | Fonte durável e contrato D-SIGN aprovado. Não bastam Q/R, certificado, versão activa ou chave actual; não gerar placeholders. Não executar assinatura real nesta missão |
| A12-11 | Regimes especiais e impostos têm enquadramento explícito; o opcional do XSD não elimina notas fiscais nem dados existentes | Gate de regimes/territórios/projecções. Valores0 apenas quando condição verdadeira comprovada ou excepção normativa aplicável |
| A12-12 | Condições de pagamento não geram recebimento: nenhuma PaymentAmount/Date inventada por existir mPagamento/vencimento | RC/RG/Payments em contrato próprio; FT diferida não cria caixa na emissão |
| A12-13 | XML parcial/sem campos obrigatórios não é disponibilizado como fiscal completo; diagnóstico localiza campo/documento e causa | Produto não oferece falsa completude. Permitir apenas evidência estrutural sintética rotulada quando expressamente autorizada |
| A12-14 | Operação sem escritas em documentos, numeração, pendentes, ledger ou mestres; sem activação de consumidores | Auditoria de extracção, se pretendida, exige contrato explícito; não presumir que auditoria financeira possa ser alterada |
| A12-15 | Autorização/empresa/período e protecção do ficheiro financeiro; sem endpoint público não autorizado; cancelamento não altera dados | Testes de permissões/isolation e gestão dos artefactos próprios no futuro mandato; nenhuma conta/dado real nos ensaios |
| A12-16 | Relatório acompanha versão do esquema, release, origens, selecção, contagens, erros e hashes; revisão independente do HEAD exacto | Sem declarar novo resultado em commit apenas documental; CI mínimo não prova o contrato fiscal completo |

## Menor sequência proposta, sem a executar

1. Decidir/rever perfil e contrato, resolver inputs obrigatórios ou delimitar casos recusados de forma legítima. Não escolher data/regime por conveniência nem alterar mestres existentes.
2. Em ordem própria, implementar extracção consistente e diagnóstico/validação estrutural nos casos autorizados. Fixtures só para provas internas, nunca valores fiscais fictícios em produto. O desenho deve prever todos os dados obrigatórios; não prometer que a aplicação actual basta sem mudanças de contrato de assinatura/datas/regimes.
3. Entrega/uso fiscal só depois de origem autêntica de assinatura/certificação, restantes gates e cobertura completa do perímetro comercial. Reversibilidade do exportador read-only: desligar a funcionalidade/recusar entrega sem alterar documentos; não apaga arquivos ou dados existentes.

A questão do espaço após ATCUD: e a melhoria bruto/IVA/total no diálogo continuam fora desta implementação e abertas. Aprovação dos critérios não é autorização de código ou novos ensaios.
