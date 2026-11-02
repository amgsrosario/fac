# TUULI AIR — Mapa executivo de pendências reais

02-11-2026. [Ordem vinculativa](https://github.com/amgsrosario/fac/issues/46#issuecomment-6584848804).
Base fixa `051b1fc1eb78e5c0c81c51b77d0d2f94bde20435`, PR #47 integrado.
Este inventário **não inicia** as missões propostas nem altera gates.
[F] facto observado; [R] resultado de relatório/teste anterior identificado;
[I] inferência limitada; [P] proposta. [Fontes](EVIDENCE.md) e [missões](MISSIONS.md).

## Conclusões prioritárias

1. **#38: preparação/restauro concluídos, prontidão operacional parcial.** Não
   repetir a preparação. A última prova examinada usa V27; o código actual usa
   V28. O delta da base exacta e o runtime actual precisam de comprovação antes
   de qualquer reutilização. Não se afirma que o contentor ainda esteja em V27.
2. **Bootstrap é dependência concreta:** a prova #38 tem zero utilizadores e o
   CRUD exige autorização. Não foi localizado procedimento comercial de primeiro
   provisionamento nos componentes inspeccionados. Não inventar conta/credencial
   nem desactivar segurança; preparar capacidade segura em mandato posterior.
3. **Contratos não são todos por implementar:** H2-03/V28, V25/V26, Data Valor,
   mecanismos RC inactivos, reconciliação, JWT/permissões e QR/PDF já têm código,
   testes e integrações. Não os recolocar no backlog como tarefas novas.
4. **Independência da assinatura é parcial:** mestres, rascunhos, autorização,
   runtime, oráculos fiscais não assinados e preparação do piloto podem avançar.
   Emissão produtiva/validação fiscal completa continuam bloqueadas. Não substituir
   assinatura por mock operacional para vencer o 409.
5. **D-SIGN-02..05 ficam na fase final**, por decisão expressa. H2-07/H2-08
   encerradas; não se retomam custódia/fornecedores, chaves ou desenho nesta missão.

## 1. Base limpa — prioridade P0

| ID | Estado técnico | Evidência | Pendência real / dependência | Próxima missão proposta |
| --- | --- | --- | --- | --- |
| B01 Preparação alternativa B | CONCLUÍDO no perímetro #38 | E02/E03: importação selectiva/restauro, 4 clientes/10 artigos, 42 tabelas, 15 sequences, 84 FKs sem órfãos; sem transacções/DEMO26; testes 5/5 | Não equivale a base comercial completa. Não há novo pedido de escolha A/B | Não repetir; conservar snapshot/identidade e origem |
| B02 Restauro e errata | CONCLUÍDO | E03/E04: restauro confirmado R2; errata arquivada cc67e3f resolve metadado antigo restore_not_tested | MINOR editorial não deve voltar como bloqueio técnico | Sem nova implementação |
| B03 Delta schema/arranque actual | PARCIAL / estado SQL actual NÃO VERIFICADO | E03 prova V27 em 27-10; E05 código V28; E10 apenas contentor running/sem porta | Demonstrar se base preparada já recebeu V28; se não, ensaiar delta no clone exacto, não na origem/destino conservado | M01, antes de reutilizar a preparação |
| B04 Emitente | BLOQUEADO por dados/validação comercial | E03 presença de campos não valida identidade; EmpresaService e EmpresaControllerTests existem | Identidade/regime/dados aprovados; não copiar a empresa demo por inferência | M01 identifica inputs; preenchimento só com autorização/dados legítimos |
| B05 Primeiro utilizador | PARCIAL: CRUD entregue, bootstrap comercial não demonstrado | E03 zero contas; E06 SecurityConfig:90, UtilizadorService:48–67 e controller:35–39 | Procedimento inicial compatível com segurança; identidades/perfis reais só depois de aprovados | M02, implementação/testes sintéticos após novo mandato |
| B06 Tipos/séries/parâmetros | BLOQUEADO para configuração real | E03 zero tipos/séries/parâmetros; E05 V28 implementada, manifesto vazio | H2-03 já resolvida; faltam homologação técnica/fiscal, dados autorizados da série AT e defaults aprovados | M04 prepara fecho dos gates sem os abrir; não fabricar valores |
| B07 Segurança runtime/credenciais | PARCIAL; não é falha do Security Gate | E03 restauro lógico não cobre roles/HBA/secrets; E06 contrato runtime | Validar least privilege/segregação e configuração da instalação em isolamento; segredo técnico separado não é credencial comercial | M01/M02 e M05, sem ler/publicar segredos |

[R] Os agregados B01/B04/B05/B06 são a prova **de 27-10**, não contagens SQL
actuais reexecutadas. [F] Em 02-11 consultou-se só metadata Docker do contentor
preparado, sem SQL. Running não demonstra integridade, versão ou prontidão.
A origem `fac/facdb` e o Ubuntu Partner Demo não foram acedidos.

## 2. Circuitos comerciais/documentais — prioridade P1

| ID | Estado técnico | Evidência | Pendência real / limite | Missão |
| --- | --- | --- | --- | --- |
| C01 CRUD/mestres/rascunho/linhas | PARCIALMENTE CONCLUÍDO como circuito de produto | E05/E07: serviços de criação/com primeira linha e classes de testes comerciais/mestres | Falta prova de jornada da base limpa exacta com runtime/identidades aprovadas; existência de testes não é prova de execução actual dessa instalação | M03, testes/ajustes pontuais apenas se defeito demonstrado |
| C02 Classificação/tipos governados | CONCLUÍDO como infra/guards, BLOQUEADO para publicar definição | V25/V26 PR #32/#33; V27 #35; V28 #40, E05; manifesto sem revisões | Não implementar novamente snapshots ou bypass da política A; 2/1/1 e projecções continuam gates | M04, dossier já existente como entrada, sem reabrir decisões |
| C03 Numeração/ATCUD/PDF/QR | PARCIAL: componentes e ensaios integrados | E07 H2-05 PR #42; código multipágina/geometria e testes; 409 sem fonte | Certificação/projecção completa e comunicação de séries não provadas. Material de ensaio não é emissão válida | M03 testa recusa/leitura e M04 prepara o fecho independente; assinatura só final |
| C04 RC Data Valor | CONCLUÍDO no perímetro 1a; novo RC inactivo | E08 PR #27, DocumentoFinanceiroService:95/109 e V23 | Não reabrir F2/T1 aprovados; dados/fluxos comerciais reais e RC/RG permanecem gates | Manter; regressão proporcional no futuro incremento |
| C05 RC FinancialCore/reversão/substituição | IMPLEMENTADO INACTIVO, integração operacional BLOQUEADA | E08 PR #23/#24/#28; FinancialReceiptIssuer/Reversal e V20–V24 | Endpoint/permissões/chave idempotente pública/consumidores e corte conjunto não entregues; sem activar como atalho | Reserva financeira F01; nenhuma Fatia 1e iniciada |
| C06 Saldos/leituras/histórico | PARCIAL; autoridade legada mantida | E08 Pendente no legado, consultas internas e diagnóstico; balanceAt recusa casos não suportados | Anulação temporal FT futura depende de decisão comprovada; leitores operacionais não migrados. Base limpa não resolve invariantes futuras | F01 permanece reservado, não executar migração de consumidores |
| C07 FR/RL/NC/ND/outros/não-EUR | COBERTURA LIMITADA / homologação ou implementação não demonstradas para todo o perímetro | E09 matriz D-CAT; semântica FT/FR/RC aprovada, outras correspondências reservadas | Não apresentar todos como implementados porque UI enumera códigos; não inventar equivalência RL/RC nem novos efeitos | Administração delimita documento seguinte quando necessário; não desenvolvimento fiscal por analogia |

## 3. Segurança/permissões/auditoria — prioridade P2

| ID | Estado | Evidência | Pendência real / missão |
| --- | --- | --- | --- |
| S01 JWT/permissões/Security Gate | CONCLUÍDO e integrado no perímetro aprovado | E06 PR #8/#9/#12/#14; segurança formalmente encerrada; classes presentes | Não reabrir auditoria geral nem tratar resíduos aceites como defeitos. Configuração/ensaio da instalação são trabalhos distintos |
| S02 Provisionamento/autorização na base limpa | PARCIAL | B05/B07, matriz de permissões com seis testes; SecuritySettings recusa bypass | M02 testa primeiro provisionamento sem endpoint anónimo/bypass e mantém utilizadores comerciais por autorizar |
| S03 Auditoria operacional/recuperação | PARCIAL | AuditoriaService/Controller e testes; #38 exclui auditoria antiga | Verificar eventos das jornadas e trilho da nova instalação; não importar auditoria demo. Recuperação runtime não provada pelo dump lógico, M05 |
| S04 Actualidade dos testes/CI | COBERTURA LIMITADA, não teste falhado | E11 workflow safety/package; suites PostgreSQL são locais e datadas | Cada incremento define/reexecuta só testes relevantes com logs/XML/HEAD. Não propor nova suite genérica como condição artificial para todo o trabalho |

## 4. Fecho fiscal independente de assinatura — prioridade P3

| ID | Estado | Evidência | Trabalho possível agora / gate preservado |
| --- | --- | --- | --- |
| F02 Homologação 2/1/1 e projecção FT/FR/RC | BLOQUEADO por evidência/decisão material, não por ausência de V28 | E05/E09 H1.1 semântica versus tuplos; H2-04 existente | M04 consolida o dossier já revisto e os oráculos; não escolhe números, publica revisões ou retoma D-SIGN-04 |
| F03 QR/PDF sem assinatura | PARCIALMENTE ENTREGUE | H2-05 geometria/ATCUD; FAQ 4443 reserva, E07/E09 | Fechar evidências de representação/estado/recibo só quando fonte/requisito confirmado; não fabricar Q/R nem assumir regime RC/RG |
| F04 Exportação fiscal SAF-T | NÃO IDENTIFICADA implementação completa no âmbito inspeccionado | E12: IvaSaft é catálogo CRUD; exportações existentes são mestres/listagens/extractos; pesquisa SourceDocuments/HashControl não encontra exportador | Missão futura delimita contrato/oráculos de exportação estrutural; não confundir Excel/CSV com SAF-T nem inventar Hash. Não se prova inexistência de sistema externo |
| F05 Regimes/valores fora do perímetro | RESERVADO/BLOQUEADO | Estado institucional: não-EUR, retenção e outras projecções recusadas ou não demonstradas | Não reabrir aprovações nem criar novas regras; delimitar dependências quando houver missão de documento/regime específico |

[F/P] A emissão produtiva permanece bloqueada pelo 409 e por todos os gates.
O mapa não autoriza homologação/certificação. A preparação fiscal acima não
escolhe mecanismo/chave ou custódia D-SIGN-02..05, reservadas à fase final.

## 5. Piloto não produtivo — prioridade P4

| ID | Estado | Evidência | Missão / restrição |
| --- | --- | --- | --- |
| P01 Instalação/jornada isolada | PARCIAL | #38 prepara base; E06 define runtime; falta ensaio completo da composição actual | M05, após M01/M02/M03; identidade/cluster próprios, sem uso comercial |
| P02 Demo/GOLDEN novo | NÃO DEMONSTRADO como realizado após H2-05 | E13 guia exige nova instalação e aprovação de outro GOLDEN | Não actualizar Ubuntu, restaurar seed antigo ou reutilizar DEMO26 comercial. Qualquer missão demo nova é autónoma; não repetir #38 como demo |
| P03 Entrada piloto comercial | BLOQUEADO | Gates fiscais/financeiros, credenciais/dados/séries, assinatura final e autorização de instalação | Não propor data ou cutover. Aceitar primeiro evidência de piloto não produtivo |

## Não são pendências novas deste levantamento

- H2-07 e H2-08: entregues/integradas, não reabrir achados encerrados.
- Restauro #38 e errata R2: resolvidos; não repetir a Fase 1/OE-LD-01.
- Escolha alternativa B e arquitectura D-CAT: aprovadas, não pedir nova escolha.
- Compatibilidade com histórico financeiro demo: não transportar transacções;
  origem fica preservada, sem inventar Data Valor. Isso não elimina regras dos
  documentos futuros, recuperação ou autoridade única.
- Resíduos de Segurança aceites e manutenção npm futura: não transformados em
  nova missão urgente ou vulnerabilidade comprovada nesta análise.

## Estado das issues versus estado técnico

[F] Busca do repositório devolveu seis issues (#38/#39/#41/#43/#44/#46), estados
confirmados individualmente: #38/#39/#41 fechadas, #43/#44/#46 abertas. Há PRs
integrados #42/#45/#47 apesar das issues correspondentes continuarem abertas.
Não reabrir implementação por esse motivo nem fechar issues automaticamente.
[Fontes E01/E02/E05/E14](EVIDENCE.md). O estado administrativo é só um sinal,
não a prova técnica de entrega ou prontidão.

## Sequência recomendada

**M01 → M02 → M03 → M04 → M05**, com M04 (evidência/homologação pendente)
preparável em paralelo depois de confirmar M01, sem abrir gates.
M01 é a menor intervenção seguinte; M02 é o primeiro incremento funcional
proposto para resolver uma dependência concreta. [Aceitação/limites](MISSIONS.md).
F01/corte financeiro e D-SIGN-02..05 não são iniciados por esta sequência.
