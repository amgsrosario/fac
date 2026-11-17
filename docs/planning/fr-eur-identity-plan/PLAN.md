# FR/EUR — preparação da identidade e projecção fiscal

17/11/2026. **Plano técnico proposto, sem implementação ou homologação.** Base integrada `e6973910e1652dea85acdc9e0848c7683c34e6d5` (PR #58), tree `e83c31aa9858f9dff51df798591a4aa94528850e`. Branch documental `codex/fr-eur-identity-plan`. Ordem: preparação autónoma recebida neste chat; implementação requer mandato específico. Revisão independente pendente.

## 1. Estado e fundamentos

GitHub confirmou PR #58 merged em 17/11/2026, 19:16:47 UTC, head publicado `b8b0c0f0e44e0c6049b079dbd28b1c2d03878bf2`; ancestralidade local confirmada. O merge inclui V30 e a FR económica inactiva; não activa consumidores, perfil ou configuração fiscal. O commit `4ad1edf87ce3e3f27663baf9127f58f032fd774e` da branch anterior é apenas registo documental local da publicação, **não está integrado** e permanece preservado naquela worktree. Não foi confundido com o HEAD integrado nem incorporado por arrasto.

**Aprovado:** D-CAT-1/ADR-003 (híbrido governado), D-CAT-2A e H1 (FT/FR partilham tipologia interna FT, com liquidação posterior/imediata), D-CAT-3 (revisões e vínculo imutáveis, sem reclassificação histórica), e acto FR único sem RC artificial. Não voltar a pedir aprovação do modelo FR.

**Normas consultadas, sem homologação do produto:** N01 identifica FR em InvoiceType e distingue este campo de InvoiceNo, que admite o código interno. N02 liga D à tipologia externa, G à identificação documental e H ao ATCUD. N03 associa a comunicação de séries ao tipo documental e define o ATCUD por código de validação e sequencial. Fontes oficiais/versionamento/hash em [SOURCES.md](SOURCES.md). Não se conclui que a instalação esteja certificada ou que as projecções estejam aprovadas para utilização.

O contrato SAF-T integrado é proposta estrutural; o desenvolvimento local M4-12/R1 em `0337ff1` contém mapeamentos e decisões P12 ainda reservadas, **não está integrado**. É evidência documental adicional, não autorização. Não implementar exportador ou reabrir P12 neste incremento.

## 2. Confronto dirigido: actual → necessário

F=leitura de código/fonte; P=proposta técnica; R=reserva. Referências exactas e hashes em SOURCE_REGISTER.json, todos fixados por commit.

| Área | F — comportamento actual na base | P — alteração necessária, sob mandato futuro |
| --- | --- | --- |
| Identidade interna | TipoDocumento.codigoFiscal=FT e liquidacaoImediata=true; isso sozinho não cria quitação. FinancialFrIssuer exige acto explícito e vínculo V30 | Preservar FT como tipologia interna; identificar especialização FR numa definição governada e validar o acto, não inferir da descrição/ID/booleano isolado |
| Catálogo | catalog.json vazio. FiscalCatalog valida FT por FtDeferredSemantics; V28 só aceita revisões/configurações FT não imediatas | Evolução aditiva do contrato Java/SQL para uma revisão FT com especialização FR e projecção externa explícita, sem alterar definições antigas nem publicar uma revisão nesta preparação |
| Configuração | TipoDocumentoService vincula novas FT não imediatas; FT imediata de ensaio fica sem vínculo. V25 congela classificação após emissão; V27/V28 congelam vínculo/compatibilidade | Novas configurações do futuro percurso FR projectado exigem vínculo aprovado desde INSERT, coerência em UPDATE/UPSERT/conversões e protecção SQL. Preservar tipos/leituras antigos; nenhuma conversão por arrasto |
| Documento/snapshot | DocumentoComercial.consolidarSnapshotFiscal copia codigoFiscal do tipo. Mapper lê snapshot se disponível; não há campo distinto de tipo fiscal externo FR | Acrescentar identidade externa e referência à revisão/projecção como snapshot imutável, obtidos da revisão vinculada antes de numeração/assinatura. Manter o campo interno FT e a compatibilidade de snapshots antigos |
| Número | DocumentoComercialService:645 usa ID interno + espaço + série + / + sequencial; é compatível com a estrutura de N01, não prova identidade externa | Conservar a estrutura e um ID interno distinto e estável; não impor prefixo literal FR. G, documento, assinatura e futuro InvoiceNo usam o mesmo número persistido |
| Série | SerieId=(tipoDocumento,serie); lock/contador por essa chave. Não representa por si a identidade externa governada do tipo perante aliases | Associar novas séries FR à identidade fiscal explícita/revisão, impedir duas definições/aliases para uma mesma série fiscal. Não reutilizar série/código AT FT ou demo; dados reais/comunicação ficam fora |
| ATCUD | AtcudService concatena código de validação e sequencial. SerieService exige código antes de consumir número | Reutilizar a fórmula; provar a coerência da origem série/tipo FR e rollback, não apenas presença de texto. Nenhuma chamada à AT em testes |
| QR | QrFiscalPayloadBuilder:44/47/48 recebe D do codigoFiscal e G/H persistidos; IFR sintético actual sai D=FT. Há fallback ao ID quando o fiscal falta | No novo percurso projectado, D vem obrigatoriamente da projecção externa FR consolidada; G/H dos mesmos snapshots. Recusar ausência/incoerência; não generalizar a mudança aos ramos FT/RC nem reinterpretar QR antigo |
| PDF | Título usa descrição do tipo consolidada; identificador é montado por tipo/serie/numero no template. Lê QR persistido; ATCUD em todas as páginas | Título fiscal FR e quitação sustentados por snapshot/acto; apresentar número persistido sem divergência para G. Condições/recibo integral explícitos, sem saldo pendente contraditório ou RC artificial. Preservar geometria/ATCUD; o espaço após ATCUD: não é alterado por arrasto |
| Futuro SAF-T | Não há exportador neste contrato. Interface Signature não fornece keyVersion/HashControl; mpagamento tem ID/nome sem mapa externo demonstrado | Mapear SalesInvoices/InvoiceType FR, InvoiceNo/ATCUD e dados de pagamento do mesmo documento/acto; não criar artificialmente um RC/Payments. Hash integral e HashControl exigem fontes próprias; Q/R não os substituem. Não presumir CASH_IN = numerário ou ID interno = PaymentMechanism |

## 3. Menor incremento técnico recomendado (ainda não autorizado a implementar)

**Objectivo:** identidade/projecção FR coerente, apenas no serviço interno inactivo e com fixtures sintéticas; nenhuma habilitação comercial.

1. **Contrato governado aditivo.** Definir versão de esquema que represente tipologia FT, especialização FR e projecção externa FR explicitamente. Recomendação técnica: extensão versionada do híbrido existente, com validação compatível de versões antigas; não transformar codigoFiscal em FR, nem usar configuração editável separada. A forma/nome dos campos é escolha técnica sujeita à revisão, não decisão de produto adicional. Manifesto produtivo continua vazio. A revisão sintética dos testes não é publicação/homologação.
2. **Vínculos e snapshots.** Nova migration após a versão efectiva da base (candidata V31; reconfirmar colisões no mandato). Acrescentar apenas o necessário para origem/revisão da projecção e identidade externa persistidas. Não alterar V1–V30, actualizar revisões existentes ou preencher históricos. Validar Java e PostgreSQL, incluindo escritas directas e clientes antigos; referências imutáveis durante todo o ciclo. Tipos vinculados FT diferidos não podem ser convertidos em FR para contornar V25/V28.
3. **Porta interna do acto FR.** Resolver e validar a projecção **antes** de numerar/assinar. Reutilizar o acto económico V30, a autorização, o numerador e a mesma transacção. O percurso interno de FR projectada exige configuração governada e não aceita fallback sem revisão. A emissão ordinária não pode projectar uma nova configuração FR e terminar sem quitação: deve recusar antes de escritas. Um método/contexto interno de emissão é escolha técnica; não introduzir um flag público confiado ao cliente. O SQL deve exigir, no commit, que um documento novo projectado como FR tenha o acto completo V30; a validação prévia do booleano não basta.
4. **Série e unicidade.** Recomendação: vínculo de série nova à identidade fiscal, com detecção/recusa de duplicação ambígua por emitente/tipo externo/série, inclusive entre aliases internos. Não criar alocadores independentes para a mesma identidade fiscal. A partilha de séries não é necessária ao primeiro corte. O protocolo de locks deve ser único, ordenar as identidades e cobrir replay/concorrência/cronologia, preservando FT/RC. Séries plurianuais e cadeia futura seguem H2-06; não inventar reset anual. Nenhum dado legado recebe nova identidade inferida; colisões desconhecidas impedem configurar **a nova série** até esclarecimento, sem mudar leituras antigas.
5. **Projecções.** QR e impressão recebem a mesma projecção e número consolidados. Em PDF, o título fiscal não depende só de uma descrição editável; a evidência de quitação vem do acto. Manter QR congelado, reimpressão sem nova emissão, Q/R pela fonte de teste já existente. Emitidos antigos, incluindo FR de laboratório com D=FT, mantêm o payload e a identidade efectivamente gravados: não os promover a FR fiscal retroactivamente.
6. **SAF-T como contrato, não exportador.** Documentar as origens necessárias; nenhum XML/end-point. Reutilizar as correcções documentais M4-12 de UnitPrice derivado e Settlement futuro, sem aprovar o perfil. Para FR, a informação de pagamento será do acto único, sem duplicação em RC; campos/tabelas exactos e completude do futuro ficheiro dependem de P12 aplicáveis.

O legado continua a autoridade operacional vigente; a alteração do serviço interno não faz cutover nem introduz outro ledger. Não implementar o circuito de anulação FR aqui: permanece fail-closed, com acto/reversão documental e data de efeito a especificar em incremento próprio. Não decidir nesta preparação a temporalidade de anulação FT.

## 4. Expectativas independentes para os futuros testes

Oráculo proposto para o percurso FR projectado, a executar **apenas após mandato próprio e validação da projecção**:

| Dimensão | Expectativa | Fundamento / estado |
| --- | --- | --- |
| Configuração | Tipologia interna FT, especialização FR/imediata, revisão governada imutável; projecção externa FR separada | D-CAT-2A/H1/3; P técnica para materialização |
| Exemplo de identidade | ID interno **FRE**, série **TESTFR**, sequencial 1 → **FRE TESTFR/1**; tipo fiscal externo **FR** | N01; alias não é código fiscal. Valores escolhidos só como fixture, não catálogo comercial |
| Exemplo ATCUD | Código sintético **TESTFR01** + sequencial 1 → **TESTFR01-1** | N03; não é código comunicado à AT ou série comercial |
| QR | D=FR; G=FRE TESTFR/1; H=TESTFR01-1; data da fixture; montantes iguais aos persistidos, sem recalcular | N01/N02 + invariantes M4-04/09; execução condicionada ao mandato fiscal |
| PDF | Identificação fiscal Fatura-recibo; mesmo número/ATCUD/QR em todas as representações, quitação integral sustentada pelo acto | H1 e contrato de documento único; desenho gráfico é proposta técnica, não homologação |
| Finanças | 1 unidade × 10,00 + IVA sintético 23% = 12,30; um CASH_IN 12,30, posição original 12,30, residual 0, sem RC/Pendente | Cálculo aritmético independente e acto FR aprovado; taxa só da fixture, não aprovação geral |
| Datas | Efeito de dívida/quitação na emissão; instante técnico distinto; nenhuma Data Valor FR autónoma editável | Circuito V30 aprovado/revisto; preservar consulta histórica |
| Preservação | FT D=FT e RC inalterados; sem transformação de históricos FT/imediatos em FR; sem consumidores operacionais novos | Mandato e limites do PR #58 |

Estes resultados não são inferidos do código actual, que ainda projecta o caso de laboratório como FT. Não mudar expectativas para fazer a aplicação passar; qualquer divergência normativa/material deve ser apresentada ao Executivo.

## 5. Decisões, dados em falta e gates — sem reabrir o produto

| Matéria | Natureza e próximo passo |
| --- | --- |
| FR no produto, factura e quita no mesmo documento | **Já aprovada e implementada internamente.** Nenhuma nova decisão de modelo requerida |
| Aplicar identidade/projecção fiscal FR às novas configurações/documentos governados | **Mandato de implementação e validação fiscal expressamente reservados.** Recomendação: autorizar apenas o corte interno sintético acima, após revisão; não pedir ao PO para escolher códigos legais arbitrariamente |
| Revisão/combinações concretas FR e vínculo de série | **Materialização fiscal ainda não homologada.** A correspondência funcional FT 2/1/1,false não homologa automaticamente o caso FR true. Recuperar validação competente; não apresentar IDs como escolha de negócio |
| Meios de recebimento comerciais efectivamente permitidos e respectiva classificação externa | Não demonstrados nas fontes consultadas para FR produtiva. Se a primeira entrega comercial for restringida, a Administração decide o perímetro; o mapa normativo é validação, não inferência de CASH_IN/nome. Não bloqueia a preparação sintética de identidade |
| Emitente, série/código AT reais, certificado/assinatura e regime | **Dados/enquadramentos não demonstrados**, não valores a inventar nem escolhas livres. Necessários à habilitação futura, fora desta ordem |
| Perfil/completude SAF-T e certificação (P12-01/02/03) | Reservas documentais preexistentes, não decididas aqui. Não impedem mapear a FR, mas impedem declarar exportador/ficheiro comercial pronto |
| Anulação FR | **Incremento próprio**; sem semântica nova, reversão ou desbloqueio nesta preparação |
| Activação/consumidores/cutover | Gates próprios; não abrangidos pela revisão/integração do corte inactivo |

Escolhas técnicas que **não exigem decisão de produto**: nome de classe/coluna, DTO interno, forma exacta da migração, representação do snapshot, índice/lock e fixture numérica, desde que respeitem as invariantes e sejam revistas. A matriz de testes detalha os critérios verificáveis. A implementação só começa após ordem específica; a revisão positiva do plano não a autoriza.
