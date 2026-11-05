# M04 — Levantamento dos17 requisitos em05-11-2026

A matriz original mantém as referências N01..N08/E01..E06/C211. Este levantamento actualiza o estado e a prioridade. As provas citadas são reutilizadas, sem execução adicional ou nova conclusão jurídica.

| Requisito | Prova encerrada no perímetro | Pendência concreta | Assinatura e decisão reservada |
| --- | --- | --- | --- |
| M4-01 Semântica FT/FR/RC | Decisões D-CAT-2A/H1 existentes | Projecções externas e circuitos próprios | Conceito aprovado; emissão depende dos gates de cada tipo |
| M4-02 FT diferida | Correspondência2/1/1,false aprovada; seis critérios MockMvc confirmados com fonte sintética | Emissão positiva peloJAR, assinatura real e validações fiscais exteriores à correspondência | Sim para emissão produtiva, novos mandatos |
| M4-03 Vínculos/imutabilidade | V25–V28 e provasH2-03 | Publicação de definições/revisões legítimas | Estrutura independente; sem materialização automática por aprovação funcional |
| M4-04 Projecção FT/FR | Fontes/oráculos identificados emM04 | Concordância tipo interno, InvoiceType externo, número,QR,PDF eSAF-T | Estrutura analisável separadamente; validação fiscal das projecções reservada |
| M4-05 Séries/ATCUD | Locks,concatenação e rollback testados; casos negativosM03/M05/M4-02 | Séries/comunicação reais e identidade doemitente | Preparação documental independente; emissão final exige assinatura |
| M4-06 ATCUD multipágina | H2-05 prova1/2/4páginas noPDF sintético | Aceitação sobre instalação/configuração final | Layout independente, não repetir testes existentes |
| M4-07 QR geometria | H2-05 T12/ZXing/PDF300dpi | Impressão física e configuração final | Independente na geometria; dados fiscais reais têm outros gates |
| M4-08 Q/R | Derivação sintética/identidade e recusas409 demonstradas | Fonte real,custódia/persistência/certificação | Dependente D-SIGN/fase final |
| M4-09 Montantes/regimesQR | Testes estruturais H2-05 existentes | Oráculos para cada regime/território/isenção e tipos fora do perímetro | Mapeamento semântico pode avançar; cobertura fiscal exige validação própria |
| M4-10 Estados/reimpressão | FAQ4443 obtida, payload persistido e testesPDF existentes | Oráculo dirigido da invariância do QR persistido entre representações por estado | Próxima fatia recomendada, independente de nova assinatura usando fixture de payload persistido |
| M4-11 RC/RG/DataValor | DataValor e contratos entregues | Regime doemitente, projecçãoRC/RG e eventual enquadramento fiscal | Sem decidir regime por inferência; fonteprodutiva paraemissão |
| M4-12 SAF-T estrutura | XSD oficial1.04_01/contrato proposto emM04 | Aprovar perímetro/perfil e futuro executor/exportador | Estrutura pode avançar sem assinatura; exportação final tem gates |
| M4-13 SAF-T mestres/totais | Fontes identificadas; CSV/XLSX nãofiscal validado | Implementar mapeamento/exportador e oráculos exactos mediante mandato | Estrutura independente, exige perímetro aprovado; maior queM4-10 |
| M4-14 Hash/HashControl | Origem/versionamento rastreados documentalmente | Origem real persistida deHash eversão,chave e cadeia | Dependente D-SIGN, semdefaults fictícios |
| M4-15 Payments | Fonte/XSD e distinçãoRC/RG conhecidos | PerfilPayments, vínculos/datas/totais e exportador | Semdependência genérica deHash emPayments; enquadramento reservado |
| M4-16 EstadosSAF-T/anulação | Contratos documentais distinguidos de temporalidadefinanceira | Oráculos por tabela/estado/totais e integração futura | Estrutura independente, temporalidadeFT/F01 reservada |
| M4-17 Instalação/certificação | M01/M02/M03 integradas; M05 tecnicamente encerrada e local; M4-02MockMvcconfirmada | Integração governada,emitente/contas/séries reais,processoAT,auditoriafinal | Fecho comercial/certificação mantidos, assinatura real necessária |

## Próximo requisito recomendado — M4-10

Preparar um mandato pequeno de teste dirigido de reimpressão. Fixar um documento sintético com payload QR persistido e representar os estados documentalmente suportados, incluindo anulado, através dos componentes reais de consulta/impressão. Comparar exactamente os bytes de payload usados na reimpressão com o valor persistido e verificar a apresentação do estado actual noPDF. Conferir o comportamento explícito dos históricos sempayload sem inventar umvalor. Reutilizar geometria/multipágina H2-05.

A camada a validar é leitura/impressão. Uma fixture sintética pode representar o estado e o payload, evitando depender de novo cálculo/fornecedor de assinatura. O teste deve comprovar ausência de escritas e de chamadas ao fornecedor durante a reimpressão. A anulação financeira, a sequência de uma assinatura real e a emissão positiva peloJAR não são critérios desta fatia. Se a documentação fiscal exigir um oráculo ainda não fixado, escalá-lo antes de escolher um comportamento novo.

Critérios propostos: payload persistido igual no documento e QRdescodificado, estado actual visível, repetição estável, ausência de escrita financeira e recusa/indicação documentada quando a fonte histórica for insuficiente. RevisãoClaude dirigida às fontes, fixture e limites. Implementação e execução dependem de ordem própria. A prioridade decorre de uma lacuna delimitada de preservação do documento, menor que iniciar oexportadorSAF-T, que ainda carece de aprovação deperfil/perímetro.
