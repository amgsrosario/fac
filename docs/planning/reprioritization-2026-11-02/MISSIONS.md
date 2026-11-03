# Sequência proposta — missões pequenas, sem as iniciar

[Mandato e mapa](PENDING_MAP.md). A repriorização autoriza este inventário/PR,
não a execução automática de todas as missões abaixo. Não alterar ordens já
aprovadas nem abrir gates por este documento. Prioridade não é autorização.

## M01 — Delta de prontidão da base limpa, sem refazer #38 (P0)

**Objectivo:** obter prova actual da identidade/schema/runtime da preparação,
e demonstrar o delta para o código integrado, exclusivamente em isolamento.

- Entrada: evidências #38/E03/E04 e snapshot exacto, sem ler/publicar segredos;
  base de código identificada; origem/destino e recursos permitidos delimitados.
- Primeiro conferir em leitura a preparação. Se já existir prova válida no HEAD
  actual, aproveitá-la, sem novo ensaio. Não presumir que precisa de upgrade.
- Se faltar a prova, usar clone descartável para delta V27→V28 e arranque do
  código actual, comparando mestres/exclusões e requisitos; não migrar o contentor
  preparado conservado nem a origem. Não repetir importação completa ou R1/R2.
- Aceitação: identidade inequívoca, migrations/validate/segundo arranque, contagens/
  relações exigidas medidas, exclusões preservadas, startup fechado para catálogo
  vazio e emissão 409, inventário factual emitente/contas/tipos/séries/parâmetros,
  sem dados pessoais no relatório; logs/HEAD/manifesto e revisão independente.
- Saída: delta resolvido/demonstrado ou impedimento exacto; não chamar «comercial
  pronta» à instalação. Inputs de negócio ainda ausentes ficam identificados.
- Sem série AT, conta comercial, configuração fiscal publicada, cutover ou demo.

**É a menor intervenção recomendada seguinte.** Não é reabertura #38: o restauro
já aprovado é entrada; só falta demonstrar actualidade/compatibilidade da instalação.

## M02 — Primeiro provisionamento administrativo seguro (P1, incremento funcional)

**Problema concreto:** base preparada sem contas; criar utilizador por CRUD
exige autoridade já autenticada. A ferramenta comercial inicial não foi localizada
no âmbito E06. Confirmar antes de implementar se há mecanismo externo aprovado.

- Dependências: M01 e mandato que delimite o mecanismo autorizado e responsáveis.
  Identidades/perfis comerciais reais precisam de aprovação, não são fixtures.
- Proposta técnica: capacidade administrativa local controlada para instalação
  vazia, com identidade da base/instalação explícita, uso único e trilho de auditoria,
  sem qualquer canal remoto (incluindo endpoint anónimo ou autenticado), seed universal,
  password versionada ou bypass de segurança. Aplicar menor privilégio, uso único
  verificável e trilho de auditoria como critérios explícitos da futura revisão.
  A forma exacta é especificada/revista no mandato de execução, não escolhida aqui
  como nova decisão de segurança. Reutilizar capacidade equivalente se já aprovada.
- Aceitação/testes: base não autorizada recusada; não substituir administrador
  existente; corridas/repetição controladas; criação/auditoria atómicas; segredo
  não exposto; perfis e invalidação existentes preservados. Contas somente sintéticas
  em PostgreSQL descartável. Login/permissões pós-bootstrap e auditoria verificados.
- Saída: ferramenta/procedimento testado e revisão Claude, sem o executar sobre
  instalação comercial nem aprovar identidades em nome do PO.

**Primeiro desenvolvimento funcional recomendado**, após satisfazer entrada.
Não depende de escolher assinatura, homologar 2/1/1 ou criar séries fiscais.

## M03 — Jornada comercial não fiscal sobre instalação isolada (P1/P2)

- Reutilizar código entregue, não reimplementar CRUD de mestres/rascunhos/linhas.
- Entrada M01/M02; dados sintéticos, permissões e fixtures explicitamente de teste,
  sem publicar revisão fiscal real para ultrapassar catálogo vazio.
- Cobrir empresa/mestres, rascunho+primeira linha/alteração/lista, parâmetros e
  erros de dependências, auditoria/leitura/exportações não fiscais, isolamento de
  permissões. Confirmar recusa de emissão quando faltar fonte ou gate.
- Não emitir documento produtivo nem mudar o emissor. Testes positivos de mecanismos
  existentes podem usar duplos de teste já isolados, sem source no runtime.
- Aceitação: jornada documentada com testes proporcionais e casos negativos; só
  corrigir defeito demonstrado dentro do mandato futuro. Nenhuma nova regra fiscal
  ou regressão inferida por ausência de ensaio. Saída identifica precisamente o que
  bloqueia operação real, sem chamar homologado ao percurso de teste.

## M04 — Fecho das dependências fiscais fora da assinatura (P3)

- Reutilizar H1.1/H2-03/H2-04/H2-05 e gates existentes, sem nova auditoria geral.
- Administração/fiscal valida a tradução das combinações aprovadas e projecções;
  não pedir escolha arbitrária de números nem presumir 2/1/1 homologado.
- Preparar só evidências/oráculos em falta: regras de representação/estado/recibos
  e fonte FAQ ou equivalente comprovada, classificação de outras tipologias quando
  expressamente abrangidas. Não inferir RC/RG ou RL fiscalmente por analogia.
- Delimitar contrato de exportação fiscal se não houver implementação/provedor:
  distinguir mestres/estruturas passíveis de ensaio sem assinatura de componentes
  Hash/HashControl produtivos. Não criar placeholders fiscais nem declarar SAF-T
  certificado por exportar CSV/Excel. Implementação só em missão autorizada com
  schema/oráculos/perímetro definidos; esta proposta não a inicia.
- Aceitação: cada gate tem fonte/decisão/evidência exactas ou reserva explícita;
  nenhuma publicação de manifesto, configuração real ou homologação pelo executor.
- D-SIGN-02..05 não são retomadas para «desbloquear» esta missão. Se uma prova
  depender realmente de assinatura, separá-la e reservá-la para a fase final.

## M05 — Ensaio de piloto não produtivo e runbook de instalação (P4)

- Entrada M01/M02/M03 e separação das reservas M04. Ambiente temporário novo,
  sem apontar à origem, ao destino conservado ou à Partner Demo Ubuntu.
- Validar composição/configuração segura, isolamento, primeira administração,
  leitura/jornada permitida, backup/restauro e observabilidade. Segurança runtime
  não é reabertura JWT/Permissões; usar testes existentes e requisitos do contrato.
- Aceitação: inventário de IDs/recursos/versões, cenário reprodutível, evidência de
  recuperação em descartável, dados não fiscais e aprovação do perímetro de piloto.
  Gate de emissão permanece 409 se fonte/gates ausentes; nenhum dado real ou série
  AT fabricados. Sem promover novo GOLDEN ou actualizar Ubuntu.
- Saída: prontidão **do piloto não produtivo** e lista do que falta para comercial,
  não data de lançamento ou autorização de cutover/certificação.

## Dependências reservadas, não incluídas nas missões acima

- **F01 — transição financeira:** contrato público/idempotência, leitores e
  autoridade única, semântica FT anulada futura e recuperação. Manter 1e suspensa.
  Base limpa dispensa importar a demo; não faz desaparecer esses requisitos.
  Não iniciar consumidor/endpoint/corte pela simples existência de mecanismos.
- **Assinatura D-SIGN-02..05:** reservar para fase final com contributos da
  Administração. H2-08 integrado é material já disponível, não missão a repetir.
- **Operações humanas:** identidade/regime do emitente, identidades/perfis,
  homologações e comunicação/uso de novas séries AT, custos e autorização de
  instalação/activação pertencem aos respectivos gates. Registar inputs ausentes,
  sem interromper trabalho técnico independente que esteja autorizado.

## Pedido de revisão independente Claude Code

Rever este inventário documental contra a base `051b1fc`, issue #46/6584848804,
[fontes E01–E15](EVIDENCE.md), código e testes indicados. Não repetir #38 ou suites.
Confirmar: preparação versus prontidão; R2/errata e V28 não continuam indevidamente
pendentes; ausência de inferências sobre SQL actual; Segurança/H2-07/H2-08 não
reabertas; ausência de reintrodução de migração de histórico; dependências reais
para M01/M02; sequência sem gates abertos ou decisões implícitas; assinatura só
fase final. Não consultar históricos privados ou credenciais.

Emitir APROVADO / APROVADO COM RESERVAS / DEVOLVER PARA CORRECÇÃO e findings
BLOCKER/MAJOR/MINOR com ficheiro/linha, cobertura e limitações. Não implementar,
activar, aceder a bases ou fazer merge. Revisão pendente até existir parecer real;
Codex não substitui Claude. Após revisão, Administração escolhe a próxima ordem.

## PR #48-R1 — confirmação incremental solicitada

[Ordem/revisão de 03-11-2026](https://github.com/amgsrosario/fac/pull/48#issuecomment-6600046074): APROVADO COM RESERVAS 0/0/1 sobre `fdacf90`. Rever exclusivamente `fdacf90..HEAD` R1 (SHA exacto no PR/relatório), confirmando RES01–RES04 no mapa e no estado operacional, todos **adiados/reservados, não cancelados**, com evidências E16/E17; certificação depois das fases preparatórias e dependências de assinatura. Confirmar O-1 pela atestação Claude, sem repetir testes; O-2 apenas qualifica os critérios futuros de M02.

Emitir parecer incremental com severidades e referências, sem iniciar M01–M05 ou alterar gates. A correcção do executor não é aprovação independente. Ordem de execução mantida; Segurança/H2-07/H2-08 não reabertas; PR draft/sem merge.
