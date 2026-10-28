# H2-05 / issue #41 — PDF, QR e origem dos campos Q/R

28-10-2026. Base `91da67bc5e799d30abfd1c569ec7ef22fbd5f703` (PR #40,
H2-03). Branch `codex/h2-05-fiscal-pdf-qr`. Implementação local para revisão;
não constitui homologação fiscal, certificação AT ou autorização de activação.

## Fontes e perímetro

- Ordem: [issue #41](https://github.com/amgsrosario/fac/issues/41).
- H2-04/R1 consultada integralmente na worktree `h2-04-ft-fiscal-dossier`,
  commit `64c6a2d1a33cd5926bcd67b5c50c2fc995c126c2`. Essa documentação ainda
  não pertence à base deste incremento; não se presume a sua integração.
- Especificações técnicas QR AT v1.1, Outubro de 2020, original com SHA-256
  `e0f1fdd24623916ef573dcda9a99de4d3cb09e6396b2c2b01807af460bc552f2`.
- [Portaria 195/2020, artigo 4.º](https://info.portaldasfinancas.gov.pt/pt/informacao_fiscal/legislacao/diplomas_legislativos/Documents/Portaria_195_2020.pdf):
  ATCUD em todas as páginas; na página do QR, imediatamente acima deste.
- [Portaria 363/2010, artigo 6.º](https://info.portaldasfinancas.gov.pt/pt/informacao_fiscal/legislacao/diplomas_legislativos/Documents/Portaria_363_2010.pdf)
  e [Despacho 8632/2014](https://info.portaldasfinancas.gov.pt/pt/informacao_fiscal/legislacao/diplomas_legislativos/Documents/Despacho_n%C2%BA_8632_2014_03_07.pdf):
  caracteres nas posições 1, 11, 21 e 31 da assinatura completa; certificado
  aplicável ao programa. A extracção não substitui a assinatura fiscal.

## Implementado

1. `DocumentoComercialPdfService`: ATCUD na margem da primeira página e no
   cabeçalho de continuação. Preserva o bloco ATCUD imediatamente acima do QR.
   Não altera o payload persistido nem reescreve documentos históricos.
2. `QrCodeImageService`: QR mínimo versão 9, ECC M, segmento Byte; cresce até
   versão 40 quando necessário. Impressão existente de 40 × 40 mm mantida.
   Margem branca arredondada para módulos inteiros, calculada para garantir
   pelo menos 2,5 mm em cada lado nessa dimensão. Texto Latin-1 quando possível;
   UTF-8 com ECI quando necessário, sem substituição silenciosa de caracteres.
3. `FiscalQrIdentity`: Q extraído da assinatura completa; R recebido da origem
   autorizada, sem valores predefinidos fictícios. Verifica representação Base64
   canónica e formato do certificado; não verifica criptografia ou certificação.
4. `FiscalDocumentSignatureSource`: fronteira interna de assinatura persistida
   por documento. O consumidor exige exactamente uma fonte e confere identidade,
   número e ATCUD. Não aceita Q/R introduzidos directamente pelo cliente público.

## Dependência material não resolvida: fonte real de assinatura

Não foi encontrado produtor/persistência de assinatura fiscal no código actual.
Esta missão não inventa chaves, certificado, encadeamento ou algoritmo fiscal.
**Não existe implementação produtiva de `FiscalDocumentSignatureSource`.**
Sem fonte, a nova emissão comercial recusa 409 e a transacção reverte, incluindo
a numeração; não gera QR fictício. O arranque não depende dessa fonte.
Certificado e assinatura efectivos devem provir do mecanismo fiscal autorizado,
vinculados ao snapshot numerado e duravelmente registados na mesma transacção.
O produtor deverá também garantir validade criptográfica, integridade do snapshot
e encadeamento aplicável; a comparação ID/número/ATCUD não prova esses requisitos.

A geração financeira conserva o contrato opcional: sem fonte não produz novo
payload. Não se implementa assinatura de RC nem se activa qualquer circuito.
Reimpressão comercial usa o payload já persistido; não exige fonte nova. Payloads
históricos não são corrigidos nem se atesta a sua conformidade passada.

Consequência operacional explícita: esta branch não está pronta para emissão
comercial real enquanto faltar a fonte. A Administração deve identificar o
mecanismo autorizado e o certificado efectivo, sem divulgar chaves privadas.
Não se apresenta o item Q/R como integração fiscal completa.

## Escolha técnica para Byte / versão mínima

O `QRCodeWriter` do ZXing 3.5.3 escolhe modo e versão automaticamente; não
disponibiliza opção para impor Byte. A opção compacta pode misturar segmentos.
Compararam-se uma dependência adicional e uma ponte pequena para as primitivas
existentes. Adoptou-se `FiscalByteQrEncoder`, sem copiar/substituir classes da
biblioteca ou instalar dependências. Reutiliza Reed-Solomon, máscara e matriz.
O custo é depender de API interna e usar o mesmo package Java: a versão 3.5.3
deve permanecer fixada e cada actualização exige testes de descodificação do
fluxo real. A revisão independente deve conferir este compromisso técnico.

## Salvaguardas e reservas

Sem migrations, backfill, alterações a séries, seeds, manifesto, saldos,
extractos, emissores FinancialCore ou frontend. Catálogo fiscal e FinancialCore
permanecem inactivos; Fatia 1e suspensa. Nenhuma operação em bases persistentes
ou demo. Sem push, PR ou merge nesta entrega.

FAQ 4443 continua sem evidência acessível; não se fabricou conteúdo ou hash.
Gates 2/1/1, projecção QR/PDF, AT e activação continuam sujeitos a validação e
decisão próprias. Geometria testada não equivale a homologação integral.

[Validação e evidências](VALIDATION.md) · [Pedido de revisão Claude](REVIEW_REQUEST.md).
