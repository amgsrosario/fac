# H2-06 — Riscos, testes propostos e aceitação

30-10-2026. Análise na base `ac07247`. Esta matriz não é um resultado de testes nem homologação. Gravidade indica impacto de uma futura integração produtiva sem a garantia indicada; não constitui finding contra a implementação H2-05 aprovada.

| Risco / impacto | Garantia proposta / teste futuro | Evidência de aceitação |
| --- | --- | --- |
| R01 — Crítico: assinatura sem proveniência legítima | T01: certificado do programa e chave pública confrontados com documentação autorizada; assinatura inválida, chave incorrecta ou certificado ausente recusados | Dossier de proveniência aprovado; nenhum número de exemplo aceite como credencial |
| R02 — Crítico: conteúdo diferente do assinado | T02: oráculos oficiais para os cinco campos, ordem, separadores, datas, decimais, codificação e assinatura anterior; mutação de cada campo invalida a prova | Bytes canónicos e verificação criptográfica reprodutíveis; perfil normativo validado, sem escolher algoritmo por analogia com demo |
| R03 — Alto: hora técnica ausente ou posterior à assinatura | T03: fixar instante definitivo antes da assinatura/QR; precisão, fuso, mudança de dia, relógio recuado entre séries/tipos | Persistência, bytes assinados e SystemEntryDate documental coerentes; Data Valor não substitui instante técnico |
| R04 — Crítico: bifurcação da cadeia | T04: concorrência na mesma série e séries distintas, primeira emissão, predecessor anulado, retries e rollback | Uma cadeia linear por âmbito autorizado; nenhum predecessor escolhido por leitura sem serialização |
| R05 — Crítico: numeração/documento/prova/origem financeira parciais | T05: falhas injectadas antes/depois de assinatura, QR, JPA flush, JDBC e auditoria | Rollback integral no mesmo recurso transaccional; nenhum documento emitido sem prova; política de resposta incerta documentada |
| R06 — Alto: deadlock ou relógio global incoerente | T06: validar grafo de bloqueios documento/tipo/série/posição e regra global de cronologia, incluindo operações simultâneas | Ordem determinística, timeout controlado, sem alterar semântica monetária ou anulação FT |
| R07 — Crítico: repetição produz segunda emissão | T07: repetição após sucesso, falha e perda de resposta; duas chamadas concorrentes | Uma prova por documento, sem segunda numeração; preservar contrato actual de conflito/reconsulta, sem inventar API idempotente |
| R08 — Crítico: contorno SQL | T08: INSERT/UPDATE/DELETE e reassociação da prova, chave/versão/predecessor/âmbito incompatíveis | Constraints/guards e privilégios limitam corrupção estrutural; teste de adulteração criptográfica separado. SQL estrutural não prova assinatura RSA |
| R09 — Alto: reimpressão assina novamente ou usa chave actual | T09: PDF/QR repetidos após rotação e anulação | Leitura da prova original, sem chamada ao assinador; conteúdo assinado preservado. Estado/QR de documento anulado sujeito à confirmação FAQ 4443 |
| R10 — Crítico: fuga demo/produção | T10: perfis, cluster, séries reais, marcador demo, múltiplas fontes, fonte ausente | Arranque/uso recusado conforme contrato; HTTP 409 produtivo sem fonte real; zero fallback demo |
| R11 — Alto: rotação perde verificabilidade | T11: versão antiga/nova, chave revogada, histórico, backup/restauro e incidente | Vínculo imutável a versão, chaves públicas históricas disponíveis e procedimento AT comprovado; segredos fora de Git/logs/evidências |
| R12 — Alto: fornecedor remoto responde após timeout | T12: indisponibilidade, resposta tardia e duplicada, pedido com conteúdo diferente | Correlação estável por documento e digest; nenhum efeito externo tratado como rollback SQL. Aplicável se alternativa remota for escolhida |
| R13 — Alto: projecção fiscal incompleta | T13: Q derivado da assinatura persistida e R do certificado legítimo; PDF/ATCUD todas as páginas, geometria QR e menção legal | Oráculos oficiais e PDF final; catálogo vazio não torna tipologia homologada; gates 2/1/1 e QR/PDF explícitos |
| R14 — Alto: reclassificação histórica | T14: documento anterior sem prova, chave desconhecida, importação e série antiga | Não inferir assinatura/chave nem iniciar cadeia como se fosse primeira emissão; tratamento aprovado antes de uso fiscal |
| R15 — Alto: contaminação dos recibos | T15: RC/RG fora do perímetro, PDF financeiro e fallback de consulta | Nenhuma assinatura produtiva fabricada na impressão; enquadramento dos recibos confirmado separadamente |
| R16 — Alto: dados de outro emitente/série | T16: identidade documento/empresa/série/certificado inconsistente | Recusa sem exposição de segredos ou dados alheios; o contexto actual de empresa única não prova suporte multiempresa |
| R17 — Alto: regressão funcional | T17: classes emissoras comerciais, permissões, tipos, V25–V28, PDF, core transaccional existente e package | PostgreSQL descartável; contagens efectivas e XML, sem apresentar CI mínimo como regressão integral |
| R18 — Alto: assinatura disponível tomada como autorização | T18: catálogo inactivo, manifesto vazio, homologação ausente e gates financeiros | Disponibilidade técnica da fonte não habilita emissão/consumidores/cutover; autorização de activação permanece distinta |

## Etapas de aceitação propostas

1. **Documento:** Claude confronta fontes e código; Administração decide perímetro, custódia e responsabilidades. Este commit apenas prepara essa revisão.
2. **Infraestrutura futura inactiva:** perfil validado e prova aditiva com testes em PostgreSQL descartável; material exclusivamente de teste, inequivocamente não fiscal. Nenhuma alteração nesta missão.
3. **Conformidade produtiva:** origem legítima do certificado, procedimentos de chave, oráculos AT, projecção documental e segurança aceites. Não se obtém por sucesso dos testes locais.
4. **Activação autónoma:** homologações e decisão expressa de instalação/activação; #38, FinancialCore e cutover continuam operações separadas.

## Verificações efectivamente realizadas na H2-06

Leitura do Git, issue #43, integração PR #42, workflow e fontes públicas oficiais; confronto estático com código e documentação; validação de referências e whitespace do diff documental. Não foram executados testes de aplicação, emissão, migrations, acessos a bases, geração de chaves ou chamadas operacionais AT. Os testes T01–T18 são propostas futuras.
