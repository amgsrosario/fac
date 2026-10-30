# H2-06 — Proposta de integração produtiva, não autorizada para implementação

Legenda F/N/H/P em [CURRENT_CONTRACT.md](CURRENT_CONTRACT.md). Tudo nesta
proposta marcado P é desenho a submeter à Administração e revisão Claude.
Não altera M1/F2/T1, D-CAT-1/2A/3, H2-02, V25–V28 ou os gates existentes.

## 1. Perímetro recomendado e fronteira de confiança

**P:** preparar primeiro FT de liquidação posterior, EUR, em série nova e
instalação futura autorizada. A fase inicial seria inactiva e testada apenas
com material de ensaio identificado, sem certificado fictício produtivo.
Não incluir FR/imediata, RC/RG/RL, NC/ND, documentos importados/manuais,
multimoeda ou base antiga por analogia. A confirmação normativa de uma família
não homologa automaticamente as configurações técnicas TUULI.

Separar quatro responsabilidades futuras:

1. **Orquestrador de emissão:** autorização, perímetro libertado, série,
   snapshot, instante fiscal, transacção, persistência, pendente/FinancialCore
   já existente e evento de emissão. Não delega a autoridade fiscal ao QR.
2. **Assinador controlado:** recebe bytes canónicos definidos pela aplicação,
   chave/versão seleccionadas de configuração governada e devolve assinatura.
   Não escolhe montantes, antecessor, certificado ou tipos com base em dados
   públicos do pedido. Não recebe uma cadeia de SQL ou um DTO arbitrário.
3. **Repositório de prova:** persiste e verifica vínculo ao documento; guarda
   assinatura e metadados suficientes para validação independente e histórico.
4. **Fonte para QR/PDF:** consulta a prova já produzida na emissão; não assina,
   numera, altera documentos ou selecciona uma chave corrente numa reimpressão.

`FiscalDocumentSignatureSource` é hoje uma fronteira de leitura de cinco
campos, não o orquestrador. Poderá ser adaptada sobre o repositório proposto;
a sua extensão necessita de especificação/implementação futura. Não fornecer
um bean aparentemente real que devolva sucesso sem prova persistida.

**P:** uma fonte real disponível não basta para habilitar emissão: exigir
libertação explícita do perímetro e conjunção das provas fiscais/operacionais
aplicáveis. Não usar o diagnóstico `configurationReady()` como se fosse um
guard já integrado. Manter 409 para ausência/ambiguidade, configuração
incoerente, âmbito não libertado ou prova não verificável; sem fallback demo.

## 2. Conteúdo da prova e autoridade de cada campo

Nomes abaixo são **P**, não classes/colunas/migrations existentes.

| Grupo proposto | Campos/invariante |
| --- | --- |
| Identidade | ID do documento, snapshot de emitente/NIF, tipo **interno**, série e número; âmbito da instalação/programa; unicidade documental |
| Conteúdo canónico | Data documental, SystemEntryDate fiscal definido uma vez, número completo, total EUR conforme contrato homologado, assinatura anterior completa; bytes exactos e versão de canonicalização |
| Encadeamento | Âmbito emitente/série/tipo fiscal aplicável, com mapeamento explícito do tipo interno; referência ao antecessor e sua assinatura; primeiro documento apenas quando vazio comprovado; número monotónico |
| Material fiscal | Assinatura completa, perfil/algoritmo aprovado, versão da chave, referência/fingerprint público, certificado do programa aplicável à emissão e proveniência documental dessa configuração |
| Auditoria | Utilizador/contexto autorizado, correlação da tentativa, instante técnico UTC, versão da aplicação e referência da prova; sem segredo privado |
| Integração | Referência inequívoca ao snapshot fiscal, QR/versão e resultado da verificação; nenhuma segunda autoridade de montantes ou classificação |

Não confundir `keyVersion`, `fiscalSnapshotVersion` (actualmente 3),
`qrPayloadVersion` e revisão do catálogo. Não guardar só Q: quatro caracteres
não permitem reconstruir/verificar a assinatura. Guardar certificado aplicável
por prova evita alterar retroactivamente R com configuração nova.

**N resumida:** a mensagem assinada da FT segue a ordem
`InvoiceDate;SystemEntryDate;InvoiceNo;GrossTotal;HashAnterior`. O termo Hash
anterior designa a assinatura anterior completa, não um SHA escolhido pelo
programador. Primeira emissão e continuidade/rotação seguem o perfil fiscal.
**N — conteúdo do exemplar consultado:** o Despacho 8632/2014 exige RSA
(§4.1) e especifica chave de 1024 bits, SHA-1 e PKCS#1 v1.5 (§6.2);
§7.1 exige assinatura de 172 caracteres Base64 sem separadores de linha.
São requisitos desse texto, não meros exemplos nem alternativas de produto.
A designação do capítulo 7 como exemplo prático não torna facultativa essa
exigência. §6.2 contém ainda parâmetros de formato/codificação a conferir na
fonte e nos oráculos, sem traduzir literalmente rótulos ambíguos para APIs.

**Reserva distinta — vigência/aplicabilidade actual:** o exemplar oficial
apresenta o estado «Vigente»; isso é evidência editorial consultada, não uma
verificação exaustiva de alterações, instruções posteriores ou do enquadramento
concreto do TUULI. Confirmar esses pontos com responsável competente antes da
implementação produtiva. Não tratar a reserva como liberdade para escolher
algoritmo, tamanho ou padding. Não transpor SHA256/RSA-2048 da demo, nem trocar
por RSA-PSS/OAEP por conveniência de um fornecedor. Exigir oráculos compatíveis
com o perfil aplicável demonstrado; se a oferta A/B não o suportar, recusá-la.
Não se implementa criptografia nem se geram chaves nesta missão.

**P:** canonicalização independente de `DTO.toString()`, JSON ou locale.
Definir datas/segundos/fuso, representação de valores, separadores e bytes com
oráculos aprovados. Não introduzir política de arredondamento, câmbio ou
regras monetárias novas: se não houver correspondência comprovada entre total
assinado, snapshot, QR e futura exportação, recusar o caso e escalar a lacuna.
O total não-EUR continua fora do perímetro já bloqueado no código.

### Identidade da cadeia e mapeamento interno/fiscal

**N:** §2.1.5 usa o último documento da mesma série/tipo; admite continuidade
plurianual com o último Hash do exercício anterior. Não pressupor reset anual.
§1.8 exige unicidade da série no contribuinte/tipo. **P:** representar e validar
o tipo fiscal aplicável e a série, além do ID interno, antes de definir a chave
de serialização. Um lock por `tipoDocumentoId` não demonstra, por si, ausência
de duas cadeias para uma mesma identidade fiscal.

| Tipo interno / decisão existente | Mapeamento a respeitar / limite desta proposta |
| --- | --- |
| FT não imediata | Tipologia interna FT aprovada; mapeamento fiscal explícito da configuração/revisão e identidade série/tipo a provar no perímetro homologado. Não homologar 2/1/1 ou QR/PDF por esta tabela |
| FR imediata | Partilha tipologia interna FT pela D-CAT-2A; isto não autoriza projectar FR como FT em SAF-T/QR nem fundir cadeias por analogia. FR continua excluída até projecção validada |
| RC/RG/RL e outras famílias | Fora do perímetro; não inferir tipo fiscal, assinatura ou partilha de cadeia a partir de nomes, sinal ou ID interno |

**N:** §3.4.2 prevê alerta para data documental futura e impede regressar à
data actual ou anterior na mesma série depois dessa emissão. Distinguir esta
regra de datas documentais da verificação global de SystemEntryDate (§3.4.3).
**P:** validar cronologia documental sob o lock da identidade série/tipo fiscal,
incluindo a não regressão de datas; testar fronteiras e séries plurianuais.
Não alterar agora a política de datas do TUULI nem autorizar emissões futuras.

## 3. Transacção e ciclo de emissão propostos

**P**, sequência conceptual a validar contra o grafo de locks real:

1. Autenticar/autorizar; validar fornecedor real, configuração aprovada,
   certificado/proveniência, perímetro e gates **antes da numeração**, sempre
   que essas verificações não dependam de estado ainda não consolidado.
2. Bloquear documento/configuração/série segundo ordem global auditada. Ler
   antecessor sob o lock da identidade série/tipo fiscal mapeada, não antes. Revalidar cronologia e
   condições dependentes de estado concorrente depois da aquisição do lock.
3. Fixar uma vez o instante fiscal de registo e os campos definitivos. Não
   usar o instante de commit nem permitir que o assinador consulte outro relógio.
   SystemEntryDate não é Data Valor, data de vencimento ou `dataHoraOperacao`.
   A política de fuso/DST e eventual relógio por emitente exigem validação;
   um lock por série não resolve sozinho a comparação global entre séries.
4. Atribuir número/ATCUD, consolidar snapshot e mensagem normativa; resolver
   material/versionamento autorizados; assinar e verificar criptograficamente
   com a chave pública correspondente, antes de disponibilizar documento/QR.
5. Persistir prova, versão de chave, vínculo ao antecessor e configuração
   aplicada; formar Q/R da prova e persistir QR; concluir documento, pendente,
   originação já existente e auditoria na **mesma transacção de dados**.
6. Commit único. Falha em assinatura, prova, QR, SQL, originação ou auditoria
   de sucesso reverte todas as escritas, inclusive número/cabeça da cadeia.
   PDF/envio ao cliente só depois de emissão confirmada e prova verificável.

Não se afirma que um futuro serviço externo participe no rollback PostgreSQL.
A implementação deve demonstrar que JPA/JDBC partilham transacção/datasource;
não presumir atomicidade por existirem anotações. Não alterar nesta missão o
fluxo FinancialCore ou regras temporais de anulação de FT/RC.

### Concorrência, repetição e falhas

- **F:** o POST comercial actual recusa repetir emissão de documento já
  emitido; não tem a chave pública de idempotência do emissor RC interno.
- **P:** unicidade de prova por documento e sequência/âmbito; nenhum segundo
  número ou sucessor para a mesma emissão. Preservar a resposta 409 existente
  para repetição e permitir recuperação autorizada por consulta do documento
  confirmado; não aprovar silenciosamente uma nova semântica HTTP de replay.
- **P:** timeout depois de commit exige leitura/verificação antes de repetir;
  depois de rollback, uma nova tentativa não usa a prova não confirmada como
  antecessor. Em assinador externo, correlação/idempotência e estado incerto
  têm de ser contratados: não repetir cegamente nem fabricar sucesso local.
- **P:** cadeia inclui os documentos assinados anteriores mesmo se anulados;
  anulação não apaga, recalcula ou muda assinatura antiga. Não resolver aqui a
  data financeira da anulação ou a alteração de estado no QR de reimpressão.
- **P:** auditar recusas/rotação com correlação e metadados mínimos. Eventos
  novos são proposta, não elementos actuais do enum. Se persistidos após
  rollback, definir canal isolado seguro, evitando FKs para registos abortados,
  deadlocks e falsos eventos de emissão. Nunca registar chaves/credenciais.

## 4. Persistência e protecção sem promessas artificiais

**P:** relação própria de prova, aditiva, com vínculo único ao documento e
à identidade de âmbito/série/número. SQL deve proteger referências, campos
obrigatórios, compatibilidade do conteúdo, imutabilidade, sequência e ausência
de dois sucessores concorrentes. Desenhar o tratamento de início/continuidade
de exercício antes de escolher constraints: preservar a possibilidade de continuidade
plurianual do §2.1.5, sem inventar reset anual ou apagar o predecessor.

Não fazer backfill de assinaturas nem actualizar migrations integradas.
Históricos sem prova mantêm a condição conhecida; documento importado não
recebe assinatura TUULI inferida. Base limpa #38 e novas séries são operações
autónomas: vazio comprovado permite iniciar cadeia, ausência de hash num
histórico existente **não** prova que se está no primeiro documento.

Guards SQL estruturais não verificam RSA por si só. **P:** separar permissões
de escrita, validar criptografia antes de uso fiscal e definir detecção de
adulteração por SQL/restore. Se a arquitectura aprovada exigir verificação
criptográfica dentro da base, isso requer desenho e dependência próprios;
não declarar essa garantia sem implementação/teste. DBA/superutilizador/host
comprometido continuam parte do modelo de ameaça e recuperação, não se promete
invulnerabilidade por triggers.

## 5. Alternativas de custódia/assinatura — GOV-ALT-001

| Alternativa P | Benefícios | Custos/limitações | Riscos e pressupostos | Consequências futuras |
| --- | --- | --- | --- | --- |
| A — Assinador Java local com segredo externo protegido | Menor superfície inicial; sem serviço remoto na transacção; ensaio offline reprodutível | Hardening do processo/host, armazenamento/backup seguro e administração da chave; não elimina custo de certificação | Acesso à memória/host pode expor chave; não copiar o ficheiro simples da demo como política produtiva; requer responsável de custódia e suporte comprovado do perfil aplicável (RSA-1024/SHA-1/PKCS#1 v1.5/172 Base64 no exemplar), sem substituição automática pelo provider Java | Adaptador substituível; rotação/recovery e exportação de prova desenhadas desde início |
| B — Módulo/HSM/KMS não exportável ou serviço de assinatura controlado | Melhor fronteira de custódia, potencial separação de funções e trilho de uso | Infraestrutura/contrato, custos não autorizados, disponibilidade/latência, compatibilidade exacta do perfil e integração | Serviço externo não reverte com a base; exige prova de suporte ao perfil aplicável (incluindo RSA-1024/SHA-1/PKCS#1 v1.5/172 Base64 no exemplar), resposta verificável, correlação, recuperação de timeout e autorização do produtor; uma política que recuse esse perfil não autoriza substituí-lo | Pode reduzir exposição da chave, mas aumenta dependência operacional e complexidade de continuidade |

**Recomendação P:** desenhar a fronteira independente do mecanismo e propor A
para a primeira fatia **inactiva/de ensaio**, se a avaliação de segurança e o
perfil normativo a admitirem. Para custódia real, comparar A/B com responsáveis
e custo antes da decisão; não contratar serviço, gerar chave real ou aprovar A
como produção por esta recomendação. Não substituir implementação própria por
importação de facturas de outro sistema sem uma decisão de autoridade documental.

### Ciclo de vida comum exigido na proposta

Custódia do produtor identificado; geração/provisionamento só em operação
autorizada; referência versionada e fingerprint público; controlo de acesso;
selecção explícita de versão activa; verificação de proveniência do certificado.
Rotação de chave fiscal segue o procedimento normativo aplicável e preserva
provas/chaves públicas antigas; não é rotação de password/TLS. Recuperação,
incidente, revogação e indisponibilidade devem fechar emissão até validação;
não apagar históricos ou re-assinar documentos antigos para recuperar serviço.
Retenção e backup segundo requisitos aplicáveis a confirmar, sem prazo inventado.

## 6. Decisões materiais efectivamente novas

| ID proposto, não aprovado | Decisão a solicitar | Recomendação / alternativa |
| --- | --- | --- |
| D-SIGN-01 | Autorizar eventual primeira fase implementativa e seu perímetro | FT posterior/EUR, inactiva, provas/codec/testes primeiro; alternativa adiar até recolha fiscal completa. Não alargar automaticamente a FR/RC/outras famílias |
| D-SIGN-02 | Responsável/produtor, custódia A/B e meios operacionais | Fronteira substituível; avaliar A de ensaio versus B protegido com custos/segurança; não há despesa autorizada |
| D-SIGN-03 | Disponibilizar e validar prova pública do certificado/programa e versão aplicável, ou plano de certificação se ainda não existir | Não basta o formato numérico; não pedir chave privada, token ou password. Não se afirma se o TUULI já é certificado |
| D-SIGN-04 | Nomear validação fiscal do perfil, menção impressa/exportação e gates | Normas não são opções de negócio; não votar algoritmos ou dispensas. Confirmar interpretação/oráculos com responsável competente antes de implementar/activar |
| D-SIGN-05 | Autorizar futuramente política de rotação/incidente/recuperação e arranque comercial | Exigir ensaio/aceitação por evidência; alternativa manter emissão fechada até prontidão. Base #38/deploy/cutover continuam missões separadas |

Canonicalização, interfaces e índices são escolhas técnicas dentro de um futuro
mandato aprovado; não devem voltar ao PO como decisões de arredondamento,
homologação fiscal ou excepções de produto disfarçadas. D-CAT e Data Valor
já aprovados não são reabertos.

## 7. Dependências e menor sequência recomendada

1. **Agora:** revisão independente da análise, fontes e reservas; Administração
   decide o mandato, custódia e responsáveis. Fonte integral FAQ 4443 continua
   sem acesso directo (404); índice de pesquisa não fecha o gate.
2. **Só após autorização:** primeira fatia inactiva de prova/canonicalização,
   repositório, clocks/locks e adaptador de ensaio, com oráculos validados,
   sem chave/certificado real e sem desbloquear o serviço. Rever e testar.
3. **Operações posteriores próprias:** integração do mecanismo real e provas
   de certificado/rotação, menção PDF/exportação, homologação do perímetro e
   configuração catálogo. Instalação/base limpa/deploy/activação só após os
   gates fiscais e operacionais e decisão expressa. Assinador não é cutover.

| Gate existente | Relação com a proposta |
| --- | --- |
| 2/1/1 e D-CAT-2 | Identidade/efeitos da configuração e vínculo governado continuam por homologar; assinatura não os aprova |
| QR/PDF | Geometria H2-05 é evidência limitada; faltam prova real, menção de certificação e projecção completa |
| AT | Proveniência do certificado/chave e validação de conformidade continuam abertas |
| FAQ 4443 | Separar conteúdo assinado imutável, estado documental e QR reimpresso; sem resposta integral não decidir actualização/preservação da mensagem |
| RC/RG | Recibos têm enquadramento próprio; nenhuma assinatura ou sentinela Q/R produtiva inferida |
| Catálogo | Manifesto vazio preservado; disponibilidade de fornecedor não publica revisões |
| FinancialCore / 1e / #38 | Não activa nem migra; a originação FT existente é apenas uma dependência transaccional a preservar |

[Matriz de risco, testes e aceitação](RISKS_TESTS.md).
