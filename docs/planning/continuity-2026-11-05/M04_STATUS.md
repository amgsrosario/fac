# M04 — Levantamento dos 17 requisitos em 05-11-2026

A matriz original mantém as referências N01..N08/E01..E06/C211. Este levantamento actualiza o estado e a prioridade. As provas citadas são reutilizadas, sem execução adicional ou nova conclusão jurídica.

| Requisito | Prova encerrada no perímetro | Pendência concreta | Assinatura e decisão reservada |
| --- | --- | --- | --- |
| M4-01 Semântica FT/FR/RC | Decisões D-CAT-2A/H1 existentes | Projecções externas e circuitos próprios | Conceito aprovado; emissão depende dos gates de cada tipo |
| M4-02 FT diferida | Correspondência 2/1/1,false aprovada; seis critérios MockMvc confirmados com fonte sintética | Emissão positiva peloJAR, assinatura real e validações fiscais exteriores à correspondência | Sim para emissão produtiva, novos mandatos |
| M4-03 Vínculos/imutabilidade | V25–V28 e provas H2-03 | Publicação de definições/revisões legítimas | Estrutura independente; sem materialização automática por aprovação funcional |
| M4-04 Projecção FT/FR | Fontes/oráculos identificados em M04 | Concordância tipo interno, InvoiceType externo, número,QR,PDF eSAF-T | Estrutura analisável separadamente; validação fiscal das projecções reservada |
| M4-05 Séries/ATCUD | Locks,concatenação e rollback testados; casos negativosM03/M05/M4-02 | Séries/comunicação reais e identidade do emitente | Preparação documental independente; emissão final exige assinatura |
| M4-06 ATCUD multipágina | H2-05 prova de 1/2/4 páginas no PDF sintético | Aceitação sobre instalação/configuração final e históricos sem ATCUD | Layout independente, não repetir testes existentes |
| M4-07 QR geometria | E03 de SOURCES.md, H2-05 e DocumentoComercialPdfServiceTests.pdfFinalMantemAtcudModoVersaoDimensoesEMargem, com descodificação a 300 dpi | Impressão física e configuração final | Independente na geometria; dados fiscais reais têm outros gates |
| M4-08 Q/R | Derivação sintética/identidade e recusas409 demonstradas | Fonte real,custódia/persistência/certificação | Dependente D-SIGN/fase final |
| M4-09 Montantes/regimes QR | Cinco cenários FT diferida/EUR normal passaram em MockMvc: expectativas racionais independentes, consolidados, persistência e I7/I8/N/O; HEAD ensaiado 066a984 | **Reservados: não-EUR, isenções, não sujeição, retenção, liquidação imediata e recibos.** Não generalizar a conclusão dos casos sintéticos; restante perímetro fiscal/produtivo não demonstrado. | Conferir [VALIDATION](../m4-09-qr-amounts/VALIDATION.md) e pedir revisão dirigida, sem alargar o âmbito |
| M4-10 Estados/reimpressão | FAQ4443 obtida, payload persistido e testesPDF existentes | Prova parcial comercial com transição real de estado e reimpressão; homologação QR/PDF pendente | Próxima fatia recomendada, independente de nova assinatura na reimpressão comercial |
| M4-11 RC/RG/DataValor | DataValor e contratos entregues | Regime do emitente, projecçãoRC/RG e eventual enquadramento fiscal | Sem decidir regime por inferência; fonte produtiva para emissão |
| M4-12 SAF-T estrutura | XSD oficial1.04_01/contrato proposto em M04 | Aprovar perímetro/perfil e futuro executor/exportador | Estrutura pode avançar sem assinatura; exportação final tem gates |
| M4-13 SAF-T mestres/totais | Fontes identificadas; CSV/XLSX nãofiscal validado | Implementar mapeamento/exportador e oráculos exactos mediante mandato | Estrutura independente, exige perímetro aprovado; maior queM4-10 |
| M4-14 Hash/HashControl | Origem/versionamento rastreados documentalmente | Origem real persistida de Hash e versão, chave e cadeia | Dependente D-SIGN, sem defaults fictícios |
| M4-15 Payments | Fonte/XSD e distinçãoRC/RG conhecidos | PerfilPayments, vínculos/datas/totais e exportador | Sem dependência genérica deHash emPayments; enquadramento reservado |
| M4-16 EstadosSAF-T/anulação | Contratos documentais distinguidos de temporalidadefinanceira | Oráculos por tabela/estado/totais e integração futura | Estrutura independente, temporalidadeFT/F01 reservada |
| M4-17 Instalação/certificação | M01/M02/M03 integradas; M05 tecnicamente encerrada e local; M4-02 MockMvc confirmada | Integração governada,emitente/contas/séries reais,processo AT, auditoria final | Fecho comercial/certificação mantidos, assinatura real necessária |

## Próximo requisito recomendado — M4-10 comercial, prova parcial

Propor um mandato dirigido ao **documento comercial**, abrangendo a passagem real de EMITIDO a ANULADO e a reimpressão do payload QR persistido. O alcance é parcial relativamente à linha M4-10 da matriz. A emissão produtiva, o PDF financeiro e a homologação QR/PDF permanecem pendentes. Reutilizar a FAQ 4443 (N06), os testes H2-05 e a fonte sintética apenas de teste já usada na M4-02 para preparar, se necessário, um comercial emitido. O requisito não depende de desenvolver uma fonte real de assinatura. A reimpressão comercial deve reutilizar o payload e não consultar qualquer fonte.

### Percurso proposto e controlos

1. Preparar uma instalação sintética isolada e um documento comercial emitido com payload persistido, documento numerado, cliente e eventuais pendentes/origens coerentes. Reutilizar a preparação M4-02 quando o mandato futuro o autorizar. Fixar o payload original e os controlos documentais/financeiros.
2. Obter o PDF pela rota comercial real `GET /documentos-comerciais/{id}/pdf`. Descodificar o QR e comparar exactamente a mensagem com o campo persistido. Conferir o estado emitido na representação.
3. Enviar `POST /documentos-comerciais/{id}/anular` com actor autorizado e justificação segundo o contrato existente. Reler a persistência e exigir ANULADO. Não escrever ANULADO directamente por SQL ou mock para satisfazer este critério. Se o serviço recusar por um controlo vigente, registar a causa e os estados e parar esse percurso para decisão própria. Não alterar a temporalidade financeira FT.
4. Reimprimir pela mesma rota, comparar o QR com o payload original e conferir a apresentação actual de anulado. Repetir a leitura para estabilidade. Para documento comercial sem payload, verificar a recusa409 existente de legado/inconsistência. Não reconstruir o QR.
5. Conferir writes por fase e submeter a revisão independente, com limites e reservas expressos.

### Escritas esperadas por fase

A preparação grava apenas dados sintéticos. A anulação real pode alterar documento, pendente, operação de reversão/origem e auditoria conforme o serviço actual; essas alterações devem ser inventariadas e conferidas como efeitos da anulação. O teste não atribui a esses movimentos uma semântica temporal nova.

Na rota real de PDF comercial, a auditoria `DOCUMENTO_PDF_GERADO` de `DocumentoComercialPdfService.gerar` é uma escrita permitida. Fixar como critério ausência de alterações ao documento/payload, numeração, pendentes, movimentos e configurações **durante cada reimpressão**, admitindo a auditoria existente e campos legítimos de sessão quando aplicáveis, identificados por tabela/campo. Não usar `gerarParaValidacao`, que omite a auditoria, como substituto da rota real. O PDF financeiro marca impresso e pode procurar a fonte perante payload nulo; fica fora desta fatia.

### Critérios e reservas

Payload persistido igual antes/depois da transição real e igual ao QR descodificado nos PDFs, estado actual visível, repetição estável, writes de anulação conferidos separadamente dos writes de reimpressão e recusas de payload ausente preservadas. Confrontar `DocumentoComercialPdfService.gerar/validateQrConsolidado`, `DocumentoComercialService.anular`, a imutabilidade da entidade e os testes actuais de PDF. A protecção do payload comercial na entidade não deve ser descrita como trigger SQL universal. A homologação QR/PDF, impressão física e validação fiscal das projecções permanecem reservadas.

Esta intervenção reduz uma lacuna pequena de preservação do documento. SAF-T ainda requer aprovação de perfil/perímetro e implementação maior. M4-10 não reabre H2-07/H2-08, decisões funcionais aprovadas, D-SIGN ou F01. Mandato de implementação e execução pendente.

## Actualização dirigida M4-09 — 06-11-2026

Base integrada PR #55, 5342255e329f3a2ebe6d7b5ad97e9b6f8c09cf07. A ordem M4-09 foi executada em branch isolada: cinco cenários aprovados sem divergência funcional, sem alterar a aplicação. A recomendação anterior de executar a prova parcial M4-10 é histórica: integrada no PR #54. O registo local df71e68, que reconciliou o quadro completo, permanece preservado na worktree anterior e não foi indevidamente apresentado como integrado. Aqui só se actualiza M4-09 e a referência corrente; não se reabrem as outras matérias. Revisão independente pendente. A melhoria bruto/IVA/total no diálogo continua reservada a tarefa própria.
