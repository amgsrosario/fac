# Ordem 02 — incompatibilidade da assinatura demo com o XSD

Na instância descartável de teste `fac_demo`, cluster `7712156503559278630`,
a factura sintética FT DEMO26/7 foi emitida com o fornecedor demo RSA-2048.
No pedido de exportação de 25/11/2026, o exportador recuperou a assinatura
integral ligada à emissão e o validador XSD 1.1 recusou o XML antes da
descarga: `/AuditFile/SourceDocuments/SalesInvoices/Invoice/Hash` excede 172
caracteres. A assinatura RSA-2048 em Base64 tem 344 caracteres. O XSD fixado
por SHA-256 `292c0ab4611e3f5a0cdf2abb4e62d9bd41254dc2e76a1fae4d35a8b132d79350`
define `Hash` como `SAFPTtextTypeMandatoryMax172Car`.

O fornecedor demo exige RSA com pelo menos 2048 bits; o contrato SAF-T exige a
assinatura histórica integral e a versão histórica da chave. Truncar, substituir
por resumo ou voltar a assinar a factura na exportação não satisfaz esse
contrato. A revisão independente confirmou o conflito. Um perfil de assinatura
sintético compatível, caso seja autorizado, exige uma decisão fiscal e de
segurança específica, nova identidade e novas emissões; não repara as facturas
já emitidas com o perfil actual. A identidade não pode representar um
certificado real. Nenhuma alteração à salvaguarda ou ao XSD está autorizada
por esta evidência.

O ensaio pela interface para 23–24/06/2026 recusou a descarga com HTTP 409 e
identificou um documento financeiro RC emitido fora da cobertura. O navegador
não observou nenhum evento de descarga. A instância aceite em 18089 não foi
modificada. A candidatura e os testes continuam incompletos e não foram
integrados.

## Continuação autorizada: perfil exclusivamente sintético

A Administração autorizou em 25/11/2026 um fornecedor separado
RSA-1024/SHA-256, sem validade fiscal, apenas no cluster descartável
`7712163055710986279`, base `fac_demo`, contentor `ar2-saft-run3-db`, volume
`ar2-saft-run3-data`. Esta decisão não altera o perfil RSA-2048 nem autoriza
assinatura ou activação produtiva. O novo fornecedor usa uma versão demo
distinta e exige cluster, base, perfil e configuração sintética explícitos.
Foram emitidas novas facturas de teste; não se voltou a assinar FT DEMO26/7
da instância RSA-2048.

Na nova instância, para 25/11/2026, a aplicação descarregou XML com FT
DEMO26/7 emitida e FT DEMO26/8 anulada. O XSD 1.1 aceitou os bytes
descarregados; cada factura tem base 12,50 EUR, IVA 1,63 EUR e total 14,13
EUR. `NumberOfEntries=2` e `TotalCredit=12,50 EUR`, excluindo a anulada.
O período 23–24/06/2026 foi recusado por conter um RC fora da cobertura;
23/06/2026 foi recusado por falta de prova original de assinatura. Não houve
descarga em nenhum dos casos de recusa. Estes são ensaios sintéticos e não
substituem a revisão técnica nem autorizam integração ou utilização fiscal.
