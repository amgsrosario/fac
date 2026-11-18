# Validações independentes e decisões indispensáveis

18/11/2026. Trabalho dentro dos mandatos existentes; não é homologação nem autorização de activação. Fontes: plano aprovado `36c6aad`, D-CAT-2A, ADR-003 e fontes fiscais registadas em `../fr-eur-identity-plan/SOURCES.md`. A proposta M4-12/R1 em `0337ff127380207a2e9ecd81b1dfa74ac6bcf8f1` continua documental/local: não se atribui integração ou aprovação às suas escolhas P12.

## SAF-T: origens verificáveis da nova FR

| Campo/efeito futuro | Origem efectiva | Lacuna/limite |
| --- | --- | --- |
| InvoiceType | `documento_comercial.projection_external_type=FR`, exposto por FiscalProjectionDto | Não inferir FR do booleano nem alterar documentos anteriores |
| InvoiceNo | `numero_documento_completo`, incluindo código interno e série | Código interno FRE não é o código fiscal FR; unicidade histórica entre tabelas protegida pela V31 |
| ATCUD | `documento_comercial.atcud` | Código de validação sintético; comunicação AT e habilitação real não demonstradas |
| Estado/data/total/linhas | Documento e snapshots fiscais persistidos pelos serviços existentes | Completude normativa do exportador não demonstrada; nova projecção não corrige fontes antigas |
| Quitação e montante recebido | `financial_immediate_invoice` e operação SETTLE/CASH_IN ligada ao mesmo documento comercial | Não criar RC/Payments artificial nem presumir CASH_IN=dinheiro físico/PaymentMechanism NU |
| PaymentMechanism | Meio de pagamento interno guardado no acto | Correspondência externa não homologada: ID/nome interno não basta |
| Hash | Fonte integral de assinatura, distinta dos quatro caracteres Q do QR | Fonte exclusivamente sintética no ensaio; sem assinatura/certificado produtivo |
| HashControl | Versão legítima da chave e contrato de assinatura produtiva | Interface actual não fornece esta versão; R do QR é certificado, não HashControl; não inventar 0 |
| UnitPrice / Settlement | Contrato documental M4-12/R1: preço derivado líquido dos descontos aplicados, sem imposto; desconto futuro separado | Nenhum XML/algoritmo SAF-T implementado ou ensaiado nesta missão; precisão/arredondamento seguem futura especificação revista |

## Actividades que avançam sem nova decisão

- Regressões ATCUD, QR/PDF e anulação/reimpressão comercial existentes seleccionadas no VALIDATION.md.
- Regresso FT/RC e reversões já suportadas; não implica autorizar anulação FR ou resolver a temporalidade FT.
- Mapeamento acima, isolamento do perfil, integridade SQL e preservação das migrations anteriores.

## Decisões/dados realmente necessários à conclusão comercial

| Referência | Questão indispensável | Recomendação e consequência |
| --- | --- | --- |
| P12-01 | Natureza efectiva da instalação: facturação própria ou contabilidade estatutária integrada? | F se comprovado o primeiro caso. FinancialCore não prova contabilidade estatutária; I exigiria as fontes contabilísticas adicionais. Não escolher perfil para ocultar tabelas em falta |
| P12-02 | Operações/documentos efectivamente habilitados na primeira instalação | Cobrir todos os tipos realmente emitidos, incluindo anulados e famílias aplicáveis; só restringir o produto por decisão expressa. FR aprovada não volta a ser submetida como modelo por decidir |
| P12-03 | Instalação/release legítima, obrigação e estado efectivo de certificação | Obter evidência autêntica de emitente/produtor/certificado/release/versão de chave. Dados desconhecidos não são opções arbitrárias. Ausência de prova não autoriza Hash=0 ou isenção |
| C6 | Restringir futuramente novas FT imediatas sem vínculo? | Continua aberto; I04(b) demonstra preservação do legado, não quitação. Qualquer bloqueio exige decisão separada |
| Anulação FR | Acto integral de neutralização, datas e relações do documento único | Incremento próprio; não implementar reversão avulsa nem herdar automaticamente a regra RC |

A questão do espaço após `ATCUD:` e a melhoria pré-produtiva bruto/IVA/total no diálogo final mantêm-se abertas. Assinatura produtiva, comunicação AT/séries, homologação e activação continuam bloqueadas. As decisões P12 não impedem esta prova interna; impedem declarar pronto o exportador/produto comercial.
