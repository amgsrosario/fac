# M4-12 — Perfil e perímetro para decisão administrativa

13-11-2026. **Recomendação, não homologação.** Reutiliza N05 Anexo I, notas gerais e §1.4, e N07/XSD. [Fontes e datas](M4_12_SOURCES.json). Opções são legais/funcionais: não uma escolha livre de letras por conveniência de implementação.

## Decisões já vinculativas e limites desta preparação

D-CAT-1/2A/3, H2-02 e a correspondência funcional FT diferida 2/1/1,false mantêm-se aprovadas; não voltar a pedi-las. M4-02/04/09/10 são provas técnicas parciais, não homologação de exportação ou assinatura. O FinancialCore não é, só por existir, contabilidade estatutária SAF-T. O contrato não altera emissão, numeração ou estados financeiros.

## P12-01 — Natureza do software/instalação

| Opção de TaxAccountingBasis | Consequência para o contrato | Situação e recomendação |
| --- | --- | --- |
| **F — facturação** | Exportação fiscal do sistema de facturação e restantes documentos aplicáveis; não implica GeneralLedgerEntries | **Recomendação condicionada:** alvo inicial se confirmado que TUULI fornece facturação própria e não contabilidade estatutária integrada. Sem autorizar omitir recibos/guias/conferência realmente emitidos |
| **I — contabilidade integrada com facturação** | Acrescenta plano/movimentos contabilísticos e referências obrigatórias de integração | Não demonstrado pelo ledger FinancialCore; só se for produto/instalação contabilística efectiva. Aumenta significativamente perímetro |
| C — contabilidade | Exige as estruturas contabilísticas próprias | Não corresponde ao alvo documental FT apenas; não usar para contornar lacunas de facturação |
| P — facturação parcial | N05 nota i): extracção parcial por períodos mensais completos (e último inferior, se aplicável), incluindo as tabelas exigidas para aplicação integrada; não extracção arbitrária por tipo | Não é licença para um exportador que só suporta uma parte dos tipos emitidos; não escolher por a primeira implementação ser pequena |
| E / S — terceiros / autofacturação | Identidade, indicadores e tabelas próprias desses contextos | Não aprovados por esta missão nem inferidos de utilizadores/tenants |
| R / T — apenas recibos / transporte | Condição de programa emitir exclusivamente esse tipo, nos termos da fonte | Não descreve uma instalação que também emite FT; RC/RG continuam reservados |

**Decisão material a obter:** confirmar a natureza real do programa/instalação que fornecerá os dados. Propor F sob as condições acima; se houver contabilidade estatutária, identificar o fornecedor/origem e reavaliar I. Não pedir ao PO que decida arbitrariamente a validade jurídica de uma letra. Validação fiscal deve confirmar enquadramento, não o executor.

## P12-02 — Perímetro comercial e completude

| Alternativa | Benefício / custo / risco | Recomendação |
| --- | --- | --- |
| **Primeira instalação comercial efectivamente limitada a FT diferida/EUR e operações explicitamente abrangidas** | Menor implementação; requer limitação real e governada do que pode ser emitido, todos os campos obrigatórios e declaração correcta do produto. Esta documentação não instala essa limitação | Candidata a primeiro incremento **apenas se a Administração aprovar e demonstrar o perímetro operacional**, incluindo anuladas e linhas fiscais. Não presumir que FT implica um só tipo de imposto ou data de prestação igual à emissão |
| Instalação que já emite FR/NC/ND/RC/WorkingDocuments/MovementOfGoods ou outras famílias | Exige cobertura das tabelas/documentos correspondentes e enquadramentos próprios | Não emitir um SAF-T incompleto como completo. Omitir famílias por estarem fora do desenvolvimento é inaceitável; a alternativa é alargar contrato/implementação ou recusar a entrega fiscal |
| Fatia interna de desenvolvimento FT/EUR sintética | Permite preparar gerador/validador e fontes em ambiente descartável quando houver ordem | Não é produto comercial nem perfil P automático. Certificados/assinaturas de teste nunca legitimam entrega real |

**Decisão material a obter:** quais as operações/documentos da primeira instalação e quais os que terão de ser cobertos antes de disponibilizar o exportador. A proposta FT/EUR não homologa regimes, TaxCode, TaxPointDate, FR/RC ou outros códigos. Anulações documentais devem ser representadas; temporalidade financeira FT permanece reservada.

## P12-03 — Instalação, exercício e metadados legítimos

A Administração/emitente valida identidade da empresa/produtor, exercício/período fiscal, centralização/estabelecimentos, regime aplicável e release/certificado. Não enviar credenciais. A equipa técnica transforma esses elementos em configuração validada, com códigos/moradas/versões rastreáveis; não escolhe dados fiscais por seed. FiscalYear/StartDate/EndDate/TaxEntity não são filtros de pesquisa livres. A política normativa de extracção anual/fracções deve ser aplicada ao perfil aprovado, não substituída por datas arbitrárias.

Recomendação: uma instalação de facturação identificada, extracção read-only consistente e perímetro completo acordado; implementação de extracção parcial só após fundamentar as condições aplicáveis. Não confundir esta decisão com custódia de chaves D-SIGN: a estratégia criptográfica continua reservada à fase final.

## Questões técnicas que não devem virar decisões de produto artificiais

- Ordenação estável, lotes, controlo de consistência, namespaces/codificação e validação são responsabilidades técnicas do futuro mandato, não motivos para pedir decisões campo a campo ao PO.
- Falta de data de prestação, indicador de regime, conta ou versão de chave não autoriza escolher um substituto incorrecto. Identificar o evento/dado legítimo necessário; eventual alteração de recolha/persistência segue mandato próprio.
- Excepções normativas fundamentadas (p.ex. AccountID quando não há conta, ProductNumberCode quando não há EAN) são diferentes de defaults inventados para certificado/assinatura. Não pedir escolha arbitrária sobre uma regra já determinada.
- A política de mestres deve evitar conflito de identidade e referências. Mudanças legítimas de nome/morada não provam reclassificação; diagnosticar conflitos e fixar data de referência sem reescrever documentos.

## Gate de fecho

Contrato apenas fechado após: revisão independente positiva; P12-01/P12-02/P12-03 decididas no alcance aplicável; correspondências condicionais e lacunas obrigatórias tratadas por fonte/limite explícito. Aprovar o documento não autoriza implementar ou publicar o exportador. Não existe decisão nova nesta proposta.

**ATCUD: espaço na menção continua aberto. Bruto/IVA/total no diálogo permanece melhoria pré-produtiva pendente.** Sem alterar aplicação ou estados de activação.

## R1 — Decisões em linguagem de negócio e dados a confirmar

| Decisão | Questão de negócio | Recomendação | Fundamento | Consequência |
| --- | --- | --- | --- | --- |
| P12-01 | O TUULI fornece facturação ou também contabilidade estatutária integrada? | F se confirmado o primeiro caso; I só com contabilidade estatutária efectiva | N05 §1.4 e notas f)/i); FinancialCore não prova contabilidade estatutária | Determina tabelas e regras; letra não é escolha arbitrária para reduzir implementação |
| P12-02 | Que operações serão realmente disponibilizadas na primeira instalação comercial? | Perímetro inicial restrito e efectivamente controlado, cobrindo todos os documentos que nele possam existir | N05 notas d)/f); mapeamento actual não cobre todas as famílias | Decidir necessidade de Supplier, GeneralLedgerAccounts/Entries, MovementOfGoods, WorkingDocuments e Payments; completar os mappings aplicáveis antes de entrega, ou recusar saída incompleta |
| P12-03 | Qual a instalação/release legítima e qual o seu enquadramento e estado de certificação para disponibilização comercial? | Alvo certificado com fontes autênticas; confirmar obrigação legal, certificação efectiva, certificado e release antes de autorizar disponibilização | N05 §§1.13, 4.1.4.4/.5; condições diferentes para Hash e HashControl; ver R1 | Identidade, período, TaxEntity e assinatura verificáveis; não presumir 0 aplicável ao TUULI nem tratar ausência de prova como isenção |

**Dados desconhecidos a obter, não opções livres da Administração:** identidade real de empresa/produtor, exercício, estabelecimentos/centralização, release/certificado, estado efectivo e obrigação legal de certificação. Necessitam evidência e validação competente. **Escolhas reservadas:** natureza e âmbito do produto disponibilizado (P12-01/02), instalação/release e condições de disponibilização aprovadas (P12-03), dentro da lei. A Administração não escolhe dispensar certificação exigível. Não foram consultadas bases reais nem presumido o seu conteúdo.

P12-03 deve registar expressamente a conclusão comprovada sobre certificação e as consequências para Hash e HashControl, separadamente. Global (facturação sem estabelecimento específico) e Sede (contabilidade/integrada) são regras condicionais da fonte, não valores intercambiáveis. [Correcções e exemplos R1](M4_12_R1_CORRECTIONS.md). Contrato continua aberto.

## Consolidação de 19/11/2026 — factos e escolhas remanescentes

| ID | Evidência recuperada | Decisão reservada e consequência |
| --- | --- | --- |
| P12-01 | O TUULI integrado emite e gere documentos de facturação no código. O FinancialCore demonstra posições e quitações, sem prova de contabilidade estatutária integrada. O ensaio M05 refere instalação sintética de facturação, não a declaração da instalação real. | Confirmar **facturação própria** como natureza da primeira instalação e verificar se há contabilidade estatutária integrada ou facturação por terceiros. F decorre da primeira hipótese confirmada. I implicaria as fontes e tabelas contabilísticas. |
| P12-02 | D-CAT-2A e a homologação semântica FT/FR/RC mantêm-se. As provas FT/EUR são parciais. FR tem identidade e correcção integradas (#59/#60), mas permanece inactiva. RC/RG, NC/ND e restantes famílias não adquirem autorização por existir código, opção ou enum. | Aprovar a lista efectiva de operações/documentos e respectivas condições de disponibilização. Recomenda-se fechar primeiro o menor perímetro realmente habilitável. Se incluir recibos, transporte ou conferência, fechar cada família aplicável. O perímetro escolhido determina o contrato e a bateria de aceitação do exportador. |
| P12-03 | M05 fixa build e recuperação apenas sintéticos. A instalação conservada da issue #38 é evidência de preparação isolada. H2-08 documenta a falta de comprovativo organizacional de certificação e a origem exigida das chaves. Não se encontrou no dossier consultado uma identidade de instalação comercial confirmada, artefacto de release aprovado para esse destino nem prova de obrigação/estado de certificação desse release. | Aprovar a instalação e o release candidatos quando identificados, vinculando-os à prova de enquadramento e admissibilidade. A obrigação e a certificação efectiva são factos a comprovar, não opções de produto. O resultado fixa Header, evidências de Hash/HashControl e condições de recusa. |

**Dossier mínimo por obter em P12-03.** Identificação autorizada do emitente, estabelecimento/centralização e período, identificação do produtor/programa, SHA Git e SHA-256 do binário de release, enquadramento e obrigação de certificação validados, prova do estado e do número de certificado quando aplicável e correspondência desse acto com o release. Seguir depois a fonte de assinatura integral e versão histórica de chave do dossier D-SIGN. Os materiais de teste e os valores QR Q/R não preenchem estas lacunas.

Fontes consultadas: N05 Anexo I notas gerais e §§1.4/1.13, N07, [M05 VALIDATION](../m05-isolated-pilot/VALIDATION.md), [D-SIGN/H2-08](../../fiscal/h2-08/DECISIONS.md), [correcção FR R2](../fr-eur-inactive/CORRECTION_R2_REVIEW_HANDOFF.md) e registos do estado operacional. Nenhuma escolha P12 foi ratificada neste delta.

## Deliberação F / FT-EUR

A Administração confirmou a facturação própria, sem contabilidade estatutária integrada nem facturação por terceiros. F fica como perfil de referência sujeito a confirmação de conformidade legal. FT/EUR delimita o primeiro incremento. A lista de operações efectivamente habilitadas na instalação continua a ser necessária para provar a completude. [Contrato e minuta](SAFT_F_FT_EUR.md) e [inventário P12-03](SAFT_F_FT_EUR_P12_03.md).
