# Decisões históricas do portátil HP

Estado comparado com a base integrada `b6b5f0f`. Uma decisão está
**REFLECTIDA** quando subsiste como requisito, arquitectura ou
comportamento demonstrado, incluindo uma reserva explícita. Esse estado
não implica que a funcionalidade esteja habilitada. **NÃO REFLECTIDA**
identifica uma escolha ou capacidade histórica ainda sem correspondência
integral demonstrada. **CONTRADITA** exige uma decisão posterior que
substitua a orientação no mesmo âmbito.

As fontes são os caminhos originais do [índice](INDEX.md). As que já
existem no Git são indicadas nesse índice pelo caminho e, quando
aplicável, pelo blob histórico. O quadro cobre as decisões distintas,
consolidando duplicações de bloco e distinguindo os resultados de
ensaios de UI, que por si só não aprovam comportamento. O material
retido conserva apenas os metadados de retenção no índice.

Referências vigentes: [memória institucional](../../governance/INSTITUTIONAL_MEMORY.md),
[estado operacional](../../governance/OPERATIONAL_STATE.md),
[missões de 02/11](../../planning/reprioritization-2026-11-02/MISSIONS.md),
[mapa de pendências](../../planning/reprioritization-2026-11-02/PENDING_MAP.md)
e [registo executivo #46](https://github.com/amgsrosario/fac/issues/46).

## Cobertura dos marcos procurados

| Marco | Resultado | Partes cobertas pelos documentos encontrados |
| --- | --- | --- |
| Plano de Agosto/Setembro de 2026 | **Cobertura temática parcial; plano original ausente.** | `demo/golden-demo.md` documenta o ciclo FRESH/GOLDEN, verificações e reposição da demo e contém registo de 09/09/2026. Os documentos 09–12 fixam o desenho de séries, ATCUD/QR/PDF, o snapshot e a migração. O documento 11 inclui uma referência a Agosto, que por si só não é uma data de aprovação nem identifica um plano. Os documentos 16, 18 e 24–26 dão o contexto do cenário, da demonstração e da instalação Ubuntu. Estes ficheiros cobrem blocos de trabalho, sem evidência da sequência, responsáveis ou critérios globais do plano de Agosto/Setembro. |
| Diagnóstico de Outubro de 2026 | **Diagnóstico Fatia 1d ausente; antecedentes parciais.** | `32 - Auditoria tecnica final da versao V58.md` inclui arquitectura, segurança, dados e recuperação. Os documentos 13, 30 e 31 dão antecedentes dos testes seguros e da prontidão da demo. `ui/FAC_UI_DIAGNOSTICO_TRANSICAO_COMERCIAL.md`, de 27/06, analisa a transição da interface. Nenhum destes contém a Fatia 1d, a matriz dos seis riscos ou a aceitação de Outubro. A [aceitação de 21/10](../../governance/records/2026-10-21-slice1d-po-acceptance.md) existe na base actual e está fora do ZIP. |

## Produto e interface

| ID | Fonte, secção e data indicada | Decisão | Estado actual e referência |
| --- | --- | --- | --- |
| H01 | 01 — Princípios, §§1–2, 06/06/2026 | Faturação para pequenos operadores, com simplicidade e crescimento controlado. | REFLECTIDA. O mapa de 02/11 concentra a entrega na base, segurança e jornada comercial. |
| H02 | 01, §3, 06/06 | Clientes, artigos/serviços, IVA, documentos, pendentes e conta-corrente formam o núcleo. | REFLECTIDA. M03 e M04 mantêm os circuitos e as suas fronteiras fiscais. |
| H03 | 01, §3 e 04, §5, 06 e 11/06 | Estender o perímetro a orçamentos, proformas e restantes famílias conforme aplicáveis. | NÃO REFLECTIDA. P12-02 exige prova das operações efectivamente habilitadas. A primeira exportação de laboratório cobre FT/EUR. |
| H04 | 01, §2.2 e 04, §§25–27, 06 e 11/06 | Arquitectura preparada para AT e certificação, conservando o controlo explícito de habilitação. | REFLECTIDA. [Contrato SAF-T F/FT-EUR](../../planning/m04-fiscal-evidence/SAFT_F_FT_EUR.md) e gates M04/P12. |
| H05 | 02 — Frontend, «Sidebar» e «Experiência por objeto» | Navegação lateral, vistas de domínio e informação contextual. | REFLECTIDA. `frontend/src/ui/commercial/` e documentos actuais de arquitectura UI. |
| H06 | 02, «Workspace com separadores» | Workspace com separadores persistentes para objectos em trabalho. | NÃO REFLECTIDA. A base tem navegação e vistas de objectos. A cobertura equivalente de um workspace transversal persistente não está demonstrada. |
| H07 | 02, «Command Palette» | Comandos de acção acessíveis por teclado, para além da pesquisa. | NÃO REFLECTIDA. `GlobalSearch.tsx` dá pesquisa e ligação profunda. A cobertura da paleta geral de acções permanece por demonstrar. |
| H08 | 02, «Stack pretendida» e 03 — Contrato Workspace, §§1–4, 08/06 | Separar SPA e backend, adoptar React/TypeScript e padrão lista/detalhe/editor. | REFLECTIDA. `frontend/package.json`, `frontend/src/ui/commercial/` e APIs Java. |
| H09 | 03, §§5–8, 08/06 | Distinguir documentos comerciais, pendentes e documentos financeiros na experiência de trabalho. | REFLECTIDA. M03 e o modelo financeiro mantêm fases e vínculos explícitos. |
| H10 | UI — Arquitectura e Diagnóstico Comercial, 27–29/06 | Reorganizar a experiência comercial preservando a autoridade do backend. | REFLECTIDA. Projectos UI integrados e [evidência M03](../../planning/reprioritization-2026-11-02/EVIDENCE.md). |
| H11 | UI — POC V60, Foundation e Serviços, 27–29/06 | Validar a transição com protótipos e testes visuais antes de a generalizar. | REFLECTIDA. `docs/ui/`, testes browser e registos de integração. Capturas representam estados de ensaio. |
| H12 | UI — direcção V2 e auditoria da migração | Manter consistência de densidade, comportamento responsivo, paginação e navegação nas vistas. | REFLECTIDA. Base visual integrada e testes Playwright do frontend. |
| H13 | UI — documentos laboratoriais e finais, 02–03/07 | Fazer coincidir a ordem das linhas no editor, persistência e impressão e provar as linhas de texto. | REFLECTIDA. `LinhaDocumentoComercialService`, `DocumentoComercialPdfService` e provas da jornada. |

## Emissão, fiscalidade, tesouraria e reporting

| ID | Fonte, secção e data indicada | Decisão | Estado actual e referência |
| --- | --- | --- | --- |
| H14 | 04 — Integração AT, §§4.1, 15, 11/06 | Gerar SAF-T de faturação com XSD, reconciliação e prova por famílias. | NÃO REFLECTIDA no âmbito completo. PR #63 e #65 integram um laboratório F / FT-EUR, com as restantes famílias dependentes de P12. |
| H15 | 04, §4.2, 11/06 | Preparar SAF-T simplificado para comunicação periódica. | NÃO REFLECTIDA. O contrato F / FT-EUR limita-se a faturação e mantém a entrega comercial reservada. |
| H16 | 04, §4.3, 11/06 | Manter possibilidade de SAF-T contabilístico futuro. | NÃO REFLECTIDA. A instalação de referência é faturação própria, com perfil F. `GeneralLedgerAccounts` e `GeneralLedgerEntries` ficam excluídos desse perfil, sem decisão sobre um futuro módulo contabilístico. |
| H17 | 04, §§4.4, 16–18, 11/06 | Prever webservices AT, estados de comunicação, erros e reprocessamento. | NÃO REFLECTIDA. A comunicação real e as credenciais AT permanecem reservadas no mapa M04. |
| H18 | 04, §§4.5, 14.3, e 09 — Séries, §§4–7 | Identificar série por tipo, manter numerador e derivar ATCUD do código validado e número. | REFLECTIDA. `SerieService`, `DocumentoComercialService` e prova parcial M4-04, PR #57. |
| H19 | 10 — CRUD de séries, §§2–9 e validações | Identidade da série estável, normalização e coerência entre código AT e data. | REFLECTIDA. Guardas e imutabilidade das configurações/séries no estado integrado. |
| H20 | 09, §§7–8, e 11 — ATCUD/QR/PDF, §3 | Fasear a emissão. Reservar número e persistir identidade fiscal numa transacção; recusar série sem código. | REFLECTIDA. `DocumentoComercialService`, `SerieService` e regressões de integridade. |
| H21 | 11, §§2–3 e contrato de impressão | Backend calcula e congela, PDF renderiza e não recalcula; reimpressão conserva o documento fiscal. | REFLECTIDA. `DocumentoComercialPdfService` e prova parcial M4-10, PR #54. |
| H22 | 11 e 15 — PDF multipágina, «Solução aplicada» | Identificação documental legível nas páginas de continuação e QR no PDF conforme a sua geometria. | REFLECTIDA. H2-05 e PR #57, incluindo reservas de homologação e espaço após `ATCUD:`. |
| H23 | 12 — Modelo fiscal histórico, «Dados históricos» e «Estratégia de migração» | Preservar dados históricos e snapshots sem inventar dados fiscais em falta. | REFLECTIDA. Migrations aditivas e auditoria histórica. O mapa M01 mantém cópias preservadas e exclui cutover. |
| H24 | 04, §§5–6, 11/06 | Distinguir fatura, fatura-recibo, recibo e outras famílias com identidade fiscal própria. | REFLECTIDA. H1 e [H2-02](../../governance/sources/2026-10-23_H2-02_deliberacao-administracao-AR2.md). FR/EUR tem núcleo, identidade e correcção integrados pelos PR #58–60 e continua inactiva. |
| H25 | 14, 17 e 23 — Bloco 3, «Estado» e «Payload QR» | Anulação lógica, auditável; conservar número, série, ATCUD, QR e snapshot. | REFLECTIDA. M4-10 prova comercial parcial, e PR #60 mantém actos FR separados. A homologação QR/PDF continua reservada. |
| H26 | 14/17/23, «Papéis» e «Auditoria» | Papéis, JWT e auditoria de actos; rever atribuições históricas antes de produção. | REFLECTIDA. M02, matriz de permissões e gates da instalação piloto. |
| H27 | 06 — Reporting, §§2–4, 12/06 | Distinguir consulta, exportação e relatório, conservando clareza e suporte em papel. | REFLECTIDA. Consultas e exportações do domínio, nos documentos actuais de reporting. |
| H28 | 07 — Extratos, §§4–5 e totalizadores | Saldo anterior, movimentos e total do período e saldo final com filtros equivalentes. | REFLECTIDA. `FinancialQueries`, jornadas e exigência de separar os efeitos no extracto FR. |
| H29 | 08 — Exportação PDF/Excel, §§4–6 | Fonte de dados comum no backend, com apresentação própria por formato. | REFLECTIDA. Servidores de reporting e contrato de filtros/listagens; não equivale a prova SAF-T. |
| H30 | 04, §§24–25, 11/06 | Sequência inicial das seis fases de execução, começando pelo modelo fiscal interno. | CONTRADITA. A reprioritização de 02/11 em [#46](https://github.com/amgsrosario/fac/issues/46#issuecomment-6584848804) coloca M01 base limpa, M02 segurança administrativa e M03 jornada antes do fecho fiscal e do piloto. A assinatura produtiva fica para a fase final. Foi substituída a sequência de entrega, preservando os requisitos fiscais. |
| H31 | 01, §2.2 e 04, §§19, 24, 26, 06 e 11/06 | Assinatura produtiva, cadeia verificável e comprovativos de certificação como capacidades necessárias. | NÃO REFLECTIDA em produção. H2-07 prova material de ensaio. D-SIGN-02..05 e P12-03 permanecem reservados. |

## Instalação, operação e dados

| ID | Fonte, secção e data indicada | Decisão | Estado actual e referência |
| --- | --- | --- | --- |
| H32 | 13 — Testes e backup, «Bases e perfis», «Fixtures» | Separar testes de desenvolvimento e produção e usar dados de teste controlados. | REFLECTIDA. Guardas da base e RUNNER-01 integrado pelo PR #64; runners históricos preservados. |
| H33 | 16 — Cenário-base, §§3–4, e 24 — Bloco 4 | Demo repetível, isolada e sintética, com coerência entre utilizadores, séries e documentos. | REFLECTIDA. `docs/demo/golden-demo.md` actual e manual Partner Demo. |
| H34 | 16, reposição, e 24, «Reposição no Windows» | Reset a partir do cenário semeado como mecanismo histórico de reposição. | CONTRADITA no procedimento normal da demo já usada. O guia actual [Golden Demo](../../demo/golden-demo.md) dá precedência à reposição GOLDEN validada e conserva FRESH para preparação, distinguindo criação e recuperação. |
| H35 | 18, §§3–4, e 25 — Consolidação comercial, §§1–9, 24/06 | Demonstrar perfis, clientes, artigos, documentos, pagamentos, impressão e recuperação coerentes. | REFLECTIDA. Guias demo e jornadas M03/M05; prontidão comercial tem gates próprios. |
| H36 | 26 — Demo Partner Edition, «Resultado», «Salvaguardas», «Recuperação» | Instalação Ubuntu isolada para demo, com recuperação controlada e sem partilhar o circuito comercial. | REFLECTIDA. Demo Ubuntu conservada e excluída das intervenções de base limpa. |
| H37 | 28 — Administração, «Utilizadores», «Empresa emitente», «Snapshot» | Administrar perfis e emitente separadamente da emissão; congelar elementos aplicáveis ao documento. | REFLECTIDA. M02 primeiro provisionamento local e provas de snapshot. P12-03 recolhe emitente e instalação. |
| H38 | 29 — Dados mestres, «Política transacional», «Duplicados», «Auditoria» | Importar e exportar clientes e artigos por circuito auditado, com validação e controlo de duplicados. | REFLECTIDA. `DadosMestresTransferService`. OE-LD-01 separa a selecção de dados mestres da origem histórica. |
| H39 | 30 — Backup e restauro, «Salvaguardas», «Retenção», «Cifragem» | Backup verificável e restauro separado, com preservação e tratamento de retenção/cifra. | REFLECTIDA. M01/M05 e OE-LD-01 demonstram restauro e isolamento no seu âmbito; os dados operacionais são preservados. |
| H40 | 31 — Fecho da demo, «Critério de prontidão», e 32 — Auditoria V58, levantamento | Avaliar prontidão por ensaios e achados, incluindo runtime, segurança e restauro. | REFLECTIDA. Fatia 1d e M01–M05 mantêm esta abordagem. A auditoria V58 tem base e alcance próprios. |
| H41 | CRUDS — Chaves e códigos, «Decisões finais» por entidade | Usar códigos naturais onde têm significado operacional e preservar referências históricas. | REFLECTIDA. Normalizações de armazém/modo/prazo, configurações fiscais e identidade de série. |
| H42 | Demo — Golden, ciclo FRESH/GOLDEN, 09/09/2026 no registo | Selar o cenário revisto, verificar proveniência e hash e restaurar apenas no destino demo identificado. | REFLECTIDA. [Guia GOLDEN actual](../../demo/golden-demo.md). A versão histórica é preservada como referência. |
| H43 | Demo — contingência, checklists, manuais e roteiros | Confirmar dados e saúde antes da demo; travar perante incidente, usar backup e um percurso reproduzível. | REFLECTIDA. Guias demo vigentes e M05 isolada. |

## Contagem e prioridade de decisão

**33 REFLECTIDAS, 8 NÃO REFLECTIDAS e 2 CONTRADITAS.** Os cinco pontos de
maior impacto para alinhamento são H30 (sequência substituída), H34
(reposição GOLDEN), H31 (assinatura e certificação), H14 (SAF-T para
além de FT/EUR) e H17 (comunicação real à AT). Os dois primeiros têm
decisão posterior identificada. Os restantes são lacunas de produto ou
gates, com o alcance já delimitado na documentação vigente.
