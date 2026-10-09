# OP19 — Dossier AT inicial, riscos e plano

Versão 1.1 · 09-10-2026. Dossier de preparação, não pedido de certificação.
Complementa o documento basilar 04; a matriz é versionada com o Git.

## Estados de evidência

PRODUTO_APROVADO: decisão OP19. LEGAL_CONFIRMADO: exige fonte/versionamento e
validação do requisito concreto. INTERPRETAÇÃO_PENDENTE: não autoriza emissão.
IMPLEMENTADO: apenas código integrado identificado. TESTE_DISPONÍVEL: evidência
com origem e limites. NÃO_SATISFEITO: requisito ainda sem implementação/prova.
Estes estados são dimensões distintas e podem coexistir na mesma linha.
Nenhuma interpretação legal nova foi confirmada nesta OP.

## Matriz normativa e rastreabilidade

As fontes abaixo são referências candidatas para investigação jurídica futura,
não conteúdo normativo revalidado nesta OP. Guardar então versão, data de
consulta, excerto/requisito aplicável e responsável pela validação.

| ID | Tema / fonte a validar | Estado e funcionalidade afetada | Aceitação |
| --- | --- | --- | --- |
| L01 | NL/reembolso; especificações oficiais AT SAF-T e comunicação documental, documento basilar04 | INTERPRETAÇÃO_PENDENTE; sem código AT inventado; F05 NÃO_SATISFEITO | A07 |
| L02 | Compensação e eventual RCzero; regras AT de documentos de pagamento e séries | Produto F06 aprovado; classificação/formalismos INTERPRETAÇÃO_PENDENTE | A02/A04/A25–A28 |
| L03 | Descontos e regularização IVA; CIVA arts36/78 como fontes candidatas | Produto F07 aprovado; distinguir tipos/necessidade de regularização, não presume dispensa fiscal | A05 |
| L04 | Recebimentos antecipados/IVA; CIVA arts7/8/29/36 como fontes candidatas | F08 aprovado conceptualmente; ativação INTERPRETAÇÃO_PENDENTE | A06 |
| L05 | NC/ND, referências e prova de regularização; CIVA arts36/78 | Modos assistido/livre aprovados; requisitos exatos por cenário e prova de conhecimento por destinatário pendentes | A11/A14/A19/A29–A36 |
| L06 | Taxas históricas/território; CIVA art18, legislação regional e vigência | Motor RIVA IMPLEMENTADO em PR16; reconstrução histórica NÃO_SATISFEITA | A11/A16 |
| L07 | Moedas/conversão, referência fiscal EUR e escalas | Emissão nãoEUR recusada; política futura INTERPRETAÇÃO_PENDENTE | A12 |
| L08 | Numeração/ATCUD/QR/estados; regras AT e docs09/12 | Componentes existentes IMPLEMENTADOS; extensão aos novos circuitos e formalismos NÃO_SATISFEITA | A18/A22/A23 |
| L09 | Certificação, assinatura, integridade e SAF-T; especificações AT vigentes | Objetivo institucional; não certificado por esta OP; requisitos/provas formais NÃO_SATISFEITOS | Dossier futuro |
| T01 | Posições e afectações F01–F10 | PRODUTO_APROVADO; proposta técnica CONTRACT; circuito novo NÃO_SATISFEITO | A01–A10/A17/A25–A36 |
| T02 | Locks/idempotência/estornos | Proposta técnica; testes futuros NÃO_SATISFEITOS | A08–A10/A15/A20/A21/A23/A27/A28 |
| T03 | Snapshots/migração | V3 IMPLEMENTADO comercial PR16; evolução financeira proposta | A16/A22/A24 |

Fontes oficiais de partida: [CIVA](https://info.portaldasfinancas.gov.pt/pt/informacao_fiscal/codigos_tributarios/civa_rep/Pages/codigo-do-iva-indice.aspx)
e [Portal AT](https://info.portaldasfinancas.gov.pt/).
Os documentos locais anteriores não substituem legislação ou documentação AT
vigente; localizar o texto oficial exato antes de marcar LEGAL_CONFIRMADO.

## Estrutura e arquivo de evidências

Contrato/cálculos/movimentos/emissão/estornos: [CONTRACT](CONTRACT.md).
Testes/rastreabilidade: [ACCEPTANCE](ACCEPTANCE.md). Reconhecimento: [REPORT](REPORT.md).
Para implementação futura, cada requisito terá caminho/símbolo de código,
commit, caso de teste, resultado executado e prova fiscal externa quando exigida.
Arquivar artefactos reproduzíveis com versão e limitações; logs locais ignorados
não são por si sós arquivo institucional. Não duplicar memória institucional.

Evidência existente: PR16 integra motor por linha, RIVA/V3/V16/V17; estado
canónico reporta283 testes backend e11 Playwright, executados na missão anterior.
Esta OP não os repetiu nem os usa como prova de compensação/NC/NL/idempotência.
OP18 não encontrado como relatório versionado; contexto conversacional não é
artefacto de evidência fiscal nem prova de testes adicionais.

## Riscos priorizados e gates

| Prioridade | Risco / decisão pendente | Gate e responsável |
| --- | --- | --- |
| P0 | NL, RCzero e adiantamento sem formalismo demonstrado | Validação fiscal por responsável designado pelo PO antes de ativação; sem código presumido |
| P0 | Pendentes positivos não modelam NC autónoma; extrato assinado não prova afectação | OP técnica de posições/movimentos; aceitação A01–A07 |
| P0 | Anulação financeira por comparação de saldo; retry sem chave | Locks/idempotência/dependências, A08–A10/A20/A23 antes de circuito novo |
| P1 | Rectificação livre/múltipla, descontos e taxas históricas | Validar L03/L05/L06; origem/suporte congelados; não aplicar regime atual |
| P1 | Histórico com tipo editável ou origem ambígua | Snapshot e reconciliação; não inventar verdade em migração |
| P1 | Precisão legacy6 versus valores novos2; mistura de moedas | Política versionada, preservação legacy, sem conversão implícita |
| P1 | Conflito de locks com emissão e cronologia fora do lock de série | Ordem comum e revalidação após lock; A20/A23 |
| P2 | PDF financeiro baseado em mestres e booleano liquidado parcial | Leitura por snapshots/residual; A15/A22 |

Riscos são limitações do desenho atual observadas estaticamente, não incidentes
reproduzidos nesta OP. Segurança permanece formalmente encerrada; estas
questões financeiras não reabrem a missão Security Gate.

## Sequência proposta de OPs reduzidas

1. **Validação fiscal documental**: L01–L07, matriz de cenários e formalismos;
   saída: decisões jurídicas rastreáveis. Pode avançar antes de implementação.
2. **Integridade financeira atual**: locks/duplicados/idempotência/estornos,
   sem ativar NC/NL; regressão real e revisão independente.
3. **Posições e ledger aditivo**: contratos internos/invariantes, snapshots,
   ensaio de migração em cópia e reconciliação; aprovação de arquitetura concreta.
4. **NC/ND**: assistido/livre, múltiplas origens, regime histórico, crédito
   autónomo; só cenários legais validados, nenhum autoencontro.
5. **Liquidações mistas**: compensação, descontos, refund/NL e adiantamentos
   apenas nos subfluxos com gates legais satisfeitos; testes concorrentes.
6. **Conta corrente e UI**: selecção sinais opostos, preview e caixa real,
   moedas separadas, PDFs históricos, regressão de permissões.
7. **Reconciliação/cutover e dossier AT**: ensaio de dados reais em cópia,
   backup/restauro, evidências formais, autorização de deployment/certificação.

Cada OP futura precisa de autorização própria, âmbito e critérios de saída.
Esta sequência não autoriza código, migrations, deployment ou novas APIs.
