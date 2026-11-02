# H2-08 — Dossier de decisão D-SIGN-02..05

02-11-2026. Mandato: [issue #46](https://github.com/amgsrosario/fac/issues/46).
Base fixa `f7e8f8ad405a0e5714af0e9fc7264a9e581ff0de`, PR #45 integrado.
**F** facto verificado; **N** conteúdo do exemplar normativo consultado;
**H** hipótese a demonstrar; **P** proposta, sem aprovação administrativa.
[Fontes e limites](SOURCES.md) · [Integração e riscos](INTEGRATION_TESTS.md).

## Estado e fronteira da missão

**F:** Git remoto, API e ancestralidade confirmam a integração de `e7eb142`.
A [confirmação Claude H2-07-R1](https://github.com/amgsrosario/fac/issues/44#issuecomment-6580103638)
regista APROVADO 0/0/0, por leitura/evidências, sem testes executados pelo revisor.
A [integração autorizada](https://github.com/amgsrosario/fac/issues/44#issuecomment-6580136463)
encerrou H2-07. Não se repetem testes nem se auditam novamente esses achados.

**F:** `src/main` coincide com a base anterior à H2-07, `ac07247`. Biblioteca/SQL
experimentais estão em `src/test`; não há fonte produtiva implementada por essa
entrega. A emissão sem fonte permanece recusada. O principal continua em
`11d5635`, com alterações preexistentes; `origin/feature/visual-redesign` local
estava desactualizada, pelo que se usou o HEAD remoto obtido para FETCH_HEAD.
Worktree desconhecida `7ce573f` preservada, sem reutilização.

**P:** preparar FT posterior/EUR com nova série autorizada como próximo perímetro,
sem estender a FR, RC/RG/RL, NC/ND, multimoeda ou históricos. Nenhum certificado,
chave real, contratação, acesso operacional AT, migration ou activação nesta missão.
A autorização H2-07 de ensaio não decide D-SIGN-02..05 nem autoriza produção.

## D-SIGN-02 — Custódia e mecanismo A/B

As alternativas partilham bytes canónicos e prova persistida. Não mudam a lei,
o certificado do programa, a propriedade da assinatura ou a cadeia fiscal.
«Fora da aplicação» distingue armazenamento de chave fora do JAR/Git/config/log
versus utilização em memória: A não promete que a chave nunca entra no processo.

| Dimensão | A — assinador local; segredo protegido fora do pacote | B1 — módulo/HSM local não exportável | B2 — KMS/HSM/serviço remoto controlado |
| --- | --- | --- | --- |
| Benefício | Menor dependência de rede/serviço; integração simples | Fronteira de custódia separada, possível operação sem WAN | Gestão central e separação de funções, sujeitas a prova do serviço |
| Exposição | Processo autorizado necessita usar a chave; host/memória comprometidos são risco residual | Não exportabilidade exige evidência do dispositivo/política; aplicação comprometida pode abusar do direito de assinar | Serviço remoto não é necessariamente não exportável; exigir esse contrato e limitar o direito de assinar |
| Empresa/âmbito | Autorizações explícitas, não chave fornecida pelo cliente | Mesma autorização contextual, sessão/token do módulo limitado | Mesmo contexto ligado ao pedido e às credenciais do serviço; impedir confused deputy |
| Latência | H: sem chamada externa, mas há custo criptográfico/locks; medir | H: dependência de driver, sessão e capacidade do dispositivo | H: rede, quotas, retries e espera por resposta; medir sob concorrência |
| Falha | Permissão, ficheiro indisponível, processo/host | Token/dispositivo, driver, capacidade, substituição | Rede, quota, serviço, credenciais e timeout com resultado incerto |
| Offline | Pode funcionar sem WAN se registo confiável/políticas o permitirem | Pode funcionar localmente, se módulo/políticas disponíveis | Não assumir operação offline; sem serviço, emissão fecha, sem fallback A/demo |
| Auditoria | AR2 opera trilhos e separação de acessos; não logar material privado | Correlacionar trilho do módulo com tentativa/documento | Correlacionar trilho do fornecedor e transacção local, sem confundir operação remota com emissão confirmada |
| Rotação/revogação | Procedimento e backups sob responsabilidade do produtor | Procedimento compatível com geração/importação/exportação permitidas pelo módulo | Procedimento e retenção de versões/identificadores no contrato do serviço |
| Recuperação | Backup cifrado/controlo de acesso e ensaio; não copiar chave para evidências | Redundância/clonagem segura ou procedimento do fabricante; sem presumir portabilidade | Dependência de região/fornecedor e recuperação de autorizações; não apagar versões antigas por rollback |
| Custos potenciais | Hardening, administração, backup, resposta a incidente | Aquisição/licenciamento, operação, redundância, suporte | Pedido/assinatura, chaves/versões, rede, auditoria, redundância, suporte e saída |
| Compatibilidade fiscal | Provider tem de demonstrar perfil aplicável; não substituir algoritmo por disponibilidade | Dispositivo pode recusar perfil exigido; não assumir suporte | KMS pode recusar tamanho/hash/padding; não adoptar automaticamente outro perfil |
| Complexidade relativa | Menor integração inicial, maior responsabilidade directa pela protecção do segredo | Integração/recuperação média a alta; dependência do dispositivo | Integração/resiliência alta e contrato de estado incerto obrigatório |
| Reversibilidade | Adaptador substituível para emissões futuras, preservando provas/chaves públicas | Migração futura depende de capacidade/autorização, não re-assina documentos | Portabilidade depende do fornecedor; preservar prova e registo público fora do serviço |

Não foram pedidos preços, criadas contas, feitas chamadas pagas, benchmarks ou
provas de dispositivos. Custos não estão orçamentados. Proposta de TCO comparável:
instalação + operação + volume × custo unitário + continuidade + conformidade +
saída. Medir p50/p95/p99, disponibilidade e tempo sob lock com carga representativa;
volume, tolerância a indisponibilidade, RTO/RPO e orçamento carecem de aprovação.
Não apresentar números inventados como factos ou SLA aprovados.

**Recomendação P:** qualificar B não exportável como preferência de custódia
produtiva **condicionada** a perfil fiscal demonstrado, operação/recuperação e
custo aceitáveis. Comparar B1 se operação offline for requisito; B2 se gestão
central com rede for aceitável. A permanece alternativa viável se o responsável
aceitar formalmente o risco de host e o controlo de acesso/backup for demonstrado.
Não há escolha final, contratação ou fallback automático. É diferente da
recomendação H2-06 de A para ensaio inactivo, que não aprovou custódia produtiva.

A redução de exportação da chave não elimina abuso do direito de assinar nem
comprometimento da aplicação. O modelo de ameaça deve incluir integridade do
software/deployment, controlo de autorização e abuso de operador. Não se promete
resistência a host/raiz totalmente comprometidos apenas por usar HSM ou manifesto.

## D-SIGN-03 — Proveniência e registo de confiança independente

### Responsabilidades propostas, a nomear pela Administração

| Responsabilidade | Evidência a exigir | Não equivale a |
| --- | --- | --- |
| Produtor do programa | Identidade, responsabilidade pela chave e processo de certificação/comunicação aplicável | Empresa cliente, utilizador emissor ou operador de infraestrutura |
| Emitente fiscal | NIF/identidade e snapshot empresarial; série/tipo/contexto autorizados | Titular automático da chave do produtor |
| Responsável fiscal | Validação escrita de normas/projecção/perímetro e evidências AT | Decisão técnica de escolher algoritmo |
| Custódia/segurança | Inventário de chaves, acessos, aprovações, backup/recovery e incidente | Administrador genérico com poder de alterar provas e âncora sem controlo |
| Operação | Disponibilidade e observabilidade; execução de runbook autorizado | Poder para homologar tipos, certificado ou trocar versão em documentos antigos |

O certificado do **programa** (R) não é certificado por empresa/série, chave
pública, código AT da série ou certificado TLS do serviço. A prova organizacional
real não foi fornecida nesta missão: não se conclui que exista ou não certificação
externa; exige-se evidência legítima antes de uso. Não pedir segredos ao PO.

**P:** registo versionado, separado das provas, com identidade do produtor/programa,
certificado aplicável e referência à evidência, fingerprint/chave pública,
keyVersion, perfil normativo/canonicalização, referência de custódia, estados e
actos de autorização/revogação. Associações a empresas/instalações/séries/tipos
são explícitas e autorizadas, não derivadas do ID de login ou de nome do ficheiro.
Não impõe uma chave diferente por cliente, nem partilha universal: esse âmbito
resulta da proveniência validada e política aprovada.

A chave guardada na própria prova é só um dado de comparação. O verificador
obtém a chave autorizada de uma âncora independente e confronta chave/versão/
âmbito antes de verificar RSA e bytes. Uma tabela separada sob o mesmo escritor
comprometido **não** cria independência. Proteger publicação e consumo do registo:

| Alternativa de âncora P | Benefício / custo / risco | Aceitação |
| --- | --- | --- |
| Manifesto de confiança assinado, raiz pública aprovada por canal separado e fixada no deployment | Verificação offline e histórico reprodutível; exige distribuição, anti-regressão e gestão da própria raiz. Assinatura auto-declarada não resolve bootstrap | Evidência humana da fingerprint inicial; app só lê; publicação segregada; versões assinadas, auditadas e com protecção contra rollback |
| Registo autoritativo externo autenticado, com identidades/ACL separadas | Revogação central e trilho; depende de serviço/rede, cache e recuperação. TLS sozinho não prova legitimidade fiscal | Identidade do serviço e autoridade de publicação aprovadas por canal independente; alterações não disponíveis ao escritor das provas; cache só sob política válida |

**Recomendação P:** manifesto verificado para primeiro contrato inactivo de
confiança; registo externo se a custódia/operação aprovadas o justificarem.
Aprovar bootstrap, responsáveis e distribuição antes de implementação. Não se
cria aqui raiz, manifesto produtivo, assinatura de registo ou credencial.
Hash de arquivo sem canal confiável não autentica a origem por si só.

A prova futura conserva a referência da versão do registo, fingerprint,
keyVersion, certificado e bytes exactos que se aplicavam à emissão. KeyVersion
não é versão do catálogo, QR ou snapshot. Leitura histórica usa confiança e
estado aplicáveis, não a chave activa de hoje. Registo deve preservar eventos de
revogação e razão sem apagar a chave pública necessária à verificação histórica.

## D-SIGN-04 — Conformidade actual e evidência humana

[Registo de fontes](SOURCES.md) distingue o texto do exemplar da confirmação da
sua vigência/aplicabilidade actual. O perfil reproduzido em H2-07 não habilita
produção. Não cabe ao PO votar uma dispensa de norma ou ao executor escolher um
perfil por conveniência de um KMS. Cabe nomear responsável e aceitar o dossier
com validação fiscal/jurídica competente e, quando necessário, esclarecimento AT.

Saídas exigidas P: perfil aplicável escrito; oráculos independentes; elegibilidade
FT posterior e projecção fiscal; proveniência do certificado/programa/chave;
comunicação de séries; mensagem/menção impressa/exportação e retenção aplicáveis.
FAQ 4443 permanece reserva: ausência de resposta integral não autoriza alterar
QR de anulação/reimpressão. Nenhuma homologação 2/1/1 ou QR/PDF nesta análise.

## D-SIGN-05 — Plano de operação condicionado, sem activação

| Fase P | Critério de entrada/saída verificável | Acção vedada sem mandato posterior |
| --- | --- | --- |
| Qualificação | D-SIGN-02/03/04 aprovadas, responsável e evidência fiscal, perímetro exacto, riscos/custos aceites | Obter chave real ou configurar fonte apenas por existir adaptador |
| Pré-produção isolada | Ensaios de confiança/rotação/tempo/falha/backup com material não fiscal; zero fallback; revisão independente e artefacto identificado | Reutilizar chaves demo ou séries DEMO26 |
| Aprovisionamento | Operação autónoma autorizada, responsáveis segregados, chave/certificado legítimos e comunicação aplicável comprovada | Gerar/importar/cadastrar chave nesta H2-08 |
| Prontidão | Evidência de transacção, séries novas autorizadas, perfil, PDF/exportação, instalação/base e observabilidade; decisão expressa | Assinatura disponível não homologa configuração nem activa FinancialCore |
| Activação | Mandato de deployment/activação específico e critérios de paragem/retorno aceites | Abrir flag, emitir ou fazer cutover nesta missão |
| Continuidade | Recuperação ensaiada, chaves públicas/provas/registo preservados, clock/cadeia reconciliados | Restaurar backup antigo e voltar a emitir sem verificar sucessores já entregues |

**Rotação P:** preparar nova versão, validar provenance/compatibilidade e obrigações
AT, testar, autorizar e mudar selecção de novas emissões na fronteira definida.
Preservar prova e assinatura anterior mesmo quando a cadeia atravessa mudança
de chave/exercício; não resetar série/cadeia nem resignar o passado. Se o mecanismo
não permitir continuidade segura, emissão fecha até decisão/validação, sem
substituição de chave por conveniência operacional.

**Revogação/incidente P:** bloquear novas assinaturas no âmbito afectado, preservar
provas/logs públicos e delimitar janela, suspeita e documentos afectados. Revogação
não implica invalidar ou recalcular automaticamente todo o histórico; tratamento
fiscal dos documentos afectados exige validação competente. Não apagar, recriar
ou reemitir documentos como resposta automática a comprometimento.

**Rollback P:** antes de emissão confirmada, rollback de dados/número/prova;
após commit/entrega, fechar novas emissões e restaurar software compatível com a
leitura das provas. Não apagar prova, reduzir numerador, voltar à versão de chave
por rollback automático nem reactivar escrita legada paralela. Alterações aditivas
futuras requerem compatibilidade de leitores. Restauração tem de detectar perda
ou duplicação de sucessores com testemunho externo ao backup restaurado.

**Observabilidade P:** correlação de tentativa/documento/serie/versão (sem dados
privados desnecessários), latência/fila, recusa por gate/chave/clock, resultados
incertos e falhas de verificação. Alarmes para gaps de cadeia, registo alterado,
clock recuado e falha de auditoria; limitar acesso aos detalhes financeiros.
Não logar chave privada, payload de credenciais ou corpos inteiros por defeito.
Definir responsáveis, limiares e RTO/RPO por decisão operacional, não inventá-los.

## Decisões específicas a submeter, sem as tomar

| Decisão | Pedido material | Recomendação P / alternativa | Evidência necessária antes de implementar/operar |
| --- | --- | --- | --- |
| D-SIGN-02 | Custódia A, B1 ou B2, responsável, requisito offline e envelope de orçamento | Preferência B não exportável condicionada; A com risco aceite e hardening provado | Perfil suportado, modelo de ameaça, latência/disponibilidade, recuperação e TCO comparável; nenhuma contratação implícita |
| D-SIGN-03 | Produtor/certificado legítimos, âmbito de uso e autoridade da âncora | Bootstrap segregado e registo versionado independente; comparar manifesto e serviço | Proveniência pública/administrativa, ACL/raiz aprovada, keyVersion/âmbito e trilho de publicação |
| D-SIGN-04 | Responsável pela validação fiscal/jurídica e aceitação do dossier de conformidade | Confirmar perfil/aplicabilidade antes de escolher provider; não aprovar algoritmo por votação | Textos actuais/oráculos, projecção fiscal, certificação/séries e reservas respondidas |
| D-SIGN-05 | Política de rotação/incidente/continuidade e condições de futura activação | Aprovar runbook/gates, manter emissão fechada até provas; alternativa adiar operação | Ensaios de restore, estado incerto, segregação, responsabilidades, RTO/RPO e autorização posterior expressa |

Não se pedem decisões novas sobre M1/F2/T1, D-CAT-1/2A/3 ou reabertura H2-07.
Aprovar um dossier ou preferir A/B não autoriza automaticamente implementação,
provisionamento, emissão produtiva ou activação. A próxima missão deve ser
expressa e delimitada conforme [handoff](REVIEW_HANDOFF.md).
