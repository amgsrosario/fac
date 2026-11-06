# Proposta de mandato — M4-04: identidade da FT diferida/EUR

06-11-2026. **Preparação documental para decisão da Administração; não autoriza ensaios.** Base integrada: PR #56, `f2fa46765fbd1a2c0ab65d3978f321ef050c3424`. Preparação local após `bb5ef0ef27802db10fcddead47dfef62245883c2`, preservando o registo `df71e68`. Sem testes ou alterações funcionais nesta preparação.

## Decisões e oráculos, sem confundir autoridades

As decisões D-CAT-2A/H1 e ADR-003 sustentam a semântica FT diferida e a separação entre tipologia e identificador interno. A correspondência funcional `2/1/1,false` já aprovada não é reaberta. As fontes primárias N03/N04/N05 de [SOURCES.md](SOURCES.md) fundamentam o significado dos campos; a sua disponibilidade não homologa globalmente QR/PDF, o emitente ou uma instalação comercial.

| Correspondência esperada | Fundamento independente da aplicação | Comportamento observado / expectativa técnica proposta |
| --- | --- | --- |
| Configuração FT diferida/EUR, revisão sintética FT/1 | D-CAT-2A, ADR-003 e decisão funcional M4-02, registada na matriz; não equivale a homologação fiscal integral | Manter `2/1/1,false`, sem introduzir liquidação imediata ou recibos. Usar identificador interno distinto de FT para tornar a separação observável |
| Tipo fiscal externo FT → QR D | N04 §4, campo D, remete para InvoiceType; N05 Anexo I, tabela 4.1, define o tipo fiscal. H1_1_FT_SPEC distingue tipologia interna e projecção externa | QrFiscalPayloadBuilder usa codigoFiscal. No perímetro proposto, esperar D=FT; não estender a FR ou outros documentos |
| Série e sequência → número documental | N05 Anexo I §4.1.4.1: identificador com código interno do tipo, série e sequência. N03 arts.2–3 distingue identificação da série, tipo e código de validação | DocumentoComercialService.buildNumeroDocumentoCompleto usa ID interno + espaço + série + barra + número. **Não exigir que o prefixo interno seja FT só porque D=FT** |
| Número documental → QR G | N04 §4, campo G, remete para InvoiceNo no caso da factura | Esperar igualdade exacta com o número persistido, preservando série, separadores e sequência; não recompor G a partir de D |
| Código de validação e sequência → ATCUD → QR H | N03 art.3: código de validação com mínimo de oito caracteres, seguido da sequência; N04 §4, campo H | AtcudService concatena código e sequência; builder usa ATCUD persistido. Fixture futura com código sintético de oito caracteres, sem comunicação à AT ou alegação de atribuição real. H contém o valor, sem rótulo ATCUD |
| Número e ATCUD → identificação PDF | N03 art.4 para representação ATCUD; N05 §4.1.4.1 para identidade documental. Igualdade entre representações é critério técnico proposto, não homologação do layout | PDF apresenta descrição configurada, ID/série/número e rótulo ATCUD. Comparar identificadores com persistência e QR. Não inventar obrigatoriedade de uma descrição literal nem alterar layout |

Fontes institucionais: [ADR-003](../../governance/decisions/ADR-003-fiscal-catalog.md), [H1.1](../../fiscal/catalog/H1_1_FT_SPEC.md), [matriz D-CAT-2](../../fiscal/catalog/D-CAT-2_MATRIX.md) e [MATRIX.md](MATRIX.md). Os estados históricos destes documentos não revogam decisões posteriores registadas na matriz. Não usar o código como único fundamento do resultado esperado.

## Cobertura reutilizável e lacuna concreta

- M4-02: prova positiva MockMvc com fonte sintética e leituras financeiras persistidas. Reutilizar configuração e controlos; não repetir a demonstração da dívida.
- M4-09 integrada: seis cenários em duas execuções, montantes consolidados/persistidos/QR. Captura M409-06 contém D=FT, G=GFT SYNTH/1 e H=SYNM402-1; é evidência sintética, não homologação nem prova de código de validação AT legítimo. Preservar a captura original sem a corrigir.
- M4-10 integrada: reimpressão comercial parcial, inclusive após anulação real. Reutilizar mecanismos PDFBox/ZXing e extracção; não repetir o percurso de anulação nem geometria.
- `QrFiscalPayloadBuilderTests.usaCodigoFiscalDoTipoDocumentoNoCampoD`: separação em unidade, não prova persistência→QR efectivamente extraído do PDF.

A lacuna é uma comparação dirigida, no mesmo documento emitido pelo percurso MockMvc normal, entre configuração governada, snapshots, número/série/ATCUD persistidos, QR persistido e QR/identificação extraídos do PDF. A igualdade isolada de duas saídas da aplicação não substitui os oráculos acima.

## Cenário adicional mínimo, ainda não executado

M404-01: configuração interna `GFT`, fiscal FT, diferida/EUR, revisão FT/1; série sintética nova `M404A`, sequência inicial 1 e código de validação exclusivamente sintético `SYNM404A`. Fixar antes do pedido os resultados: D=FT; número e G=`GFT M404A/1`; ATCUD e H=`SYNM404A-1`; mesmos identificadores no PDF, distinguindo o rótulo visual ATCUD do valor de H. A fixture não constitui série comunicada ou código atribuído pela AT.

Reutilizar um documento simples e assinatura exclusivamente de teste existente. Só acrescentar segundo documento sequencial se o levantamento dirigido não encontrar prova reutilizável da relação número/ATCUD; justificar a necessidade antes de executar. Não introduzir cenários monetários, anulações ou testes gerais já aprovados.

## Critérios de aceitação da futura prova parcial

1. Fixar fontes, fixture e expectativas antes do ensaio; registar separadamente regras aprovadas, normas consultadas e critérios técnicos propostos.
2. Emitir via MockMvc, com controlos normais activos, PostgreSQL descartável e dados sintéticos. Nenhuma escrita nas bases conservadas; assinatura real e emissão positiva pelo JAR excluídas.
3. Ler o documento persistido após o pedido e comparar classificação, série, sequência, número completo e ATCUD. Extrair o QR do PDF final e verificar D/G/H contra o oráculo e a persistência, sem reconstruir o payload para o teste passar.
4. Verificar a identificação visível no PDF, incluindo distinção descrição/identificador e rótulo/valor ATCUD. Reutilizar cobertura válida de todas as páginas; não declarar nova prova multipágina ou geométrica sem a executar.
5. Arquivar HEAD/tree/delta, comandos, versões, resultados, logs/XML/PDF/capturas e hashes; comprovar isolamento e remoção apenas dos recursos próprios. Separar tentativas falhadas.
6. Claude realiza revisão independente do HEAD e evidências exactos. Divergência funcional é reportada, sem corrigir aplicação ou regra fiscal neste mandato.

## Decisão necessária e reservas

A decisão pedida é **autorizar esta prova técnica parcial com estes oráculos e limites**. Não existe fundamento para pedir ao PO que escolha arbitrariamente entre prefixo interno e FT: N05 já distingue o código interno do tipo fiscal externo. Não há homologação global ou autorização produtiva implícita.

Não foi demonstrado nesta preparação um contrato aprovado que imponha uma descrição literal única no cabeçalho PDF. Recomendação de negócio: preservar a descrição configurada e verificar a identidade documental, deixando eventual normalização do título comercial para decisão separada, se pretendida. Não bloquear a prova de identidade por uma escolha editorial excluída do âmbito.

Continuam reservados: homologação QR/PDF integral, assinatura real, certificação/AT e configuração comercial. Excluir não-EUR, isenções, não sujeição, retenção, liquidação imediata, recibos, FR/RL/NC/ND, exportador SAF-T, migrações históricas e activação. SAF-T requer contrato próprio. Manter a melhoria bruto/IVA/total no diálogo final pendente antes de uso produtivo, sem a implementar.

## Minuta para decisão administrativa

> A Administração autoriza a M4-04 exclusivamente como prova técnica parcial de identidade da FT diferida/EUR, nos termos desta proposta. Codex reutilizará a cobertura válida e executará apenas o cenário adicional necessário por MockMvc, em PostgreSQL descartável, com dados sintéticos, revisão FT/1, configuração 2/1/1,false e fonte de assinatura exclusivamente de teste existente. Fixará previamente os resultados para tipo fiscal, série, número, ATCUD e QR/PDF, com as fontes indicadas, distinguindo identificador interno e tipo fiscal externo. Entregará comparações persistidas, PDF/QR extraídos, proveniência, hashes e prova de isolamento/remoção para revisão independente Claude Code. Qualquer divergência ou fundamento insuficiente será submetido à Administração, sem correcção funcional unilateral. Mantêm-se todas as reservas fiscais e produtivas. Sem alterações funcionais/fiscais, assinatura produtiva, emissão positiva pelo JAR, bases conservadas, push, PR, merge ou activação. A aprovação da prova não constitui homologação QR/PDF ou certificação.

**Minuta proposta; não emitida nem executada nesta tarefa.** [Handoff documental](M4_04_HANDOFF.md).
