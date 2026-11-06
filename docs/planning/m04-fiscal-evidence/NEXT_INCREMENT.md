# Proposta seguinte — M4-04, identificação fiscal da FT diferida/EUR

**Recomendação para decisão da Administração, não ordem de execução.** Preparada após PR #56/M4-09 integrado em f2fa46765fbd1a2c0ab65d3978f321ef050c3424. Sem novo teste ou alteração funcional nesta missão.

## Lacuna concreta

M4-09 demonstrou os montantes I7/I8/N/O; M4-10 demonstrou a preservação do QR na reimpressão comercial. Nenhuma destas conclusões homologa a relação entre identificador interno do tipo, tipo fiscal externo, número completo, série/ATCUD e identificação visível no PDF.

Factos existentes: QrFiscalPayloadBuilder usa codigoFiscal no campo D e numeroDocumentoCompleto no G; DocumentoComercialPdfService apresenta descrição e identificadores internos. A captura já arquivada M409-06-qr-fields.json contém D=FT, G=GFT SYNTH/1 e H=SYNM402-1. É um dado sintético observado, **não uma conclusão de invalidade ou validade fiscal**. Falta o oráculo fundamentado e uma prova cruzada específica da identidade. Não presumir que um ID interno deva ser igual ao código fiscal nem converter o prefixo por iniciativa técnica.

## Cobertura reutilizável

- Decisões D-CAT-2A/H1: semântica FT diferida e distinção FT/FR aprovadas, sem reabertura.
- Fontes N04 (D/G), N05 (InvoiceType) e matriz M4-04: entradas para localizar o oráculo aprovado, sem nova interpretação fiscal implícita.
- QrFiscalPayloadBuilderTests.usaCodigoFiscalDoTipoDocumentoNoCampoD: verifica separação de ID interno e código fiscal em unidade; não demonstra a cadeia persistida/PDF nem homologa FR.
- Fixture FT/1, 2/1/1,false e FiscalSignatureTestConfiguration já existentes; CommercialReprintJourneyTests e ferramentas PdfQrInspection/ZXing, sem repetir anulação, geometria ou montantes.
- Capturas M4-09: número, código, ATCUD e payload persistidos; reutilizar antes de pedir novo ensaio. Sem reexecutar os seis cenários por rotina.

## Âmbito mínimo proposto

FT diferida/EUR apenas, emitida através de MockMvc em PostgreSQL descartável com fonte sintética existente, controlos normais e catálogo exclusivamente de teste. Uma configuração com ID interno distinto de FT é útil para impedir equivalência acidental de classificadores. Comparar configuração governada, snapshots consolidados, resposta HTTP, número/série/ATCUD persistidos, campos QR D/G/H e identificação do PDF emitido.

Não incluir FR/FT imediata, RC/RG/RL/NC/ND, outros regimes/territórios/moedas, exportador SAF-T, chaves reais, JAR positivo, bases conservadas, migração histórica, publicação de catálogo ou activação. Não reconstruir payload nem alterar o número fiscal para satisfazer o teste.

## Prova necessária e aceitação

1. Matriz campo a campo com fonte/regra/oráculo aprovado para tipo externo, número e ATCUD. Não derivar a expectativa da implementação. Se a fonte não determinar o campo, parar essa conclusão e apresentar a lacuna exacta.
2. Inventário da cobertura reutilizada e do novo caso estritamente necessário. Capturas existentes primeiro; teste adicional só após mandato e oráculo validado.
3. No caso novo autorizado, leituras persistidas após HTTP e comparação com o PDF/QR extraído, preservando snapshots, numeração e controlos. Sem chamadas directas de emissão ou dados reais.
4. Identidade Git limpa, comandos/versões, logs/XML e hashes; isolamento e remoção de recursos próprios. Prova limitada não equivale a homologação global.
5. Rever independentemente com Claude. Se surgir divergência, documentar sem corrigir a aplicação e devolver à Administração, salvo mandato explícito posterior de correcção.

## Dependências e decisão necessária

A Administração decide a abertura do mandato e o perímetro parcial FT. Não é necessário voltar a aprovar a semântica ou 2/1/1,false. É necessário localizar o oráculo fiscal já aprovado da projecção; se ele não existir, submeter especificamente a correspondência de D/G/número/PDF para validação antes de implementar ou declarar conformidade. A assinatura real não é pré-requisito deste ensaio sintético; permanece gate produtivo.

SAF-T exige decisão adicional de perfil/perímetro/TaxAccountingBasis e é uma intervenção maior; não é a próxima fatia recomendada. D-SIGN e F01 permanecem reservados. A melhoria bruto/IVA/total no diálogo final é um seguimento UI separado, pendente antes de uso produtivo, sem execução automática.

## Minuta de mandato proposto

«Autorizar uma prova dirigida M4-04 da identidade da FT diferida/EUR entre configuração, snapshots, QR e PDF. Reutilizar provas existentes e fixar o oráculo fundamentado antes de executar novo caso MockMvc em PostgreSQL descartável com dados e assinatura sintéticos. Se a regra não estiver demonstrada ou houver divergência, apresentar o campo/efeito exacto para decisão, sem alterar aplicação ou regras. Entregar evidência e revisão independente. Sem activação, assinatura real, emissão pelo JAR, bases conservadas, push, PR ou merge.»

Minuta não emitida como ordem nem executada nesta tarefa.
