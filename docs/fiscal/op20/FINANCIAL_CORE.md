# OP20 — Fundação do motor financeiro

Versão 1.0 · 12-10-2026. Base observada: `194ffd458b7405822d4934bac5cfef5feb87486b`,
merge do PR18 na linha feature/visual-redesign. Implementação local, não publicada.

## Modelo efetivamente implementado

`src/main/java/com/ar2lda/fac/financial/FinancialCore.java` é um serviço Spring
interno transacional. Usa JdbcTemplate na mesma transação Spring/JPA, por
necessitar de SQL explícito para locks e constraints; não introduz outra base,
outro transaction manager ou outro motor monetário. Os contratos internos são
records Scope, Position, Part e Settlement. Não há controllers, novos endpoints
ou chamadas nos serviços comerciais/financeiros antigos.

V18 cria quatro tabelas independentes: financial_position (natureza, origem,
referência, empresa/cliente/moeda, original, residual, estado derivado e data),
financial_operation (CREATE/SETTLE/REVERSE, chave/payload canónico, autor/motivo,
data, original de estorno), financial_allocation (CONSUME/RESTORE, posição,
montante e vínculo inverso) e financial_component (CASH_IN/CASH_OUT/DISCOUNT,
montante, efeito inverso). Cancelamento da criação fica no histórico da operação
e marca a posição cancelada; não apaga a origem. Não migra pendentes históricos.

## Invariantes

- Natureza, original, origem e âmbito da posição imutáveis; residual entre0 e
  original. Estado OPEN/PARTIAL/EXHAUSTED deriva do residual, ou CANCELLED.
- Consumo explícito, sem compensação automática; crédito residual reutilizável.
- Por liquidação: dívida extinta + saída de caixa = crédito consumido + entrada
  de caixa + desconto. Criação de adiantamentos não está implementada nesta OP.
- Desconto não é caixa; zero dinheiro não cria componente monetário fictício.
- Usa MotorMonetario.dinheiro/HALF_UP; negativos rejeitados antes de arredondar,
  entradas até seis casas e limites de numeric19,2; consumo arredondado zero
  recusado. Não existe política monetária paralela nem alteração aos legacy6.
- Referências externas são metadados congelados; não desencadeiam emissão FT/NC.
- FKs compostas impedem misturar âmbitos; original da posição é único por empresa.
- Triggers impedem UPDATE/DELETE da história e mudança de identidade da posição.
  Constraints diferidas validam residual, conservação e bijeção dos estornos no
  commit. Registos subordinados só são inseríveis na transação criadora da operação.

## Transações, concorrência e idempotência

Criação/liquidação/estorno são atómicos. pg_advisory_xact_lock serializa empresa
+ chave, incluindo chaves ainda não existentes. A unicidade no banco reforça o
controlo. Colisões do hash apenas serializam pedidos distintos, não os confundem.
Payload canónico inclui âmbito, tipo e conteúdo financeiro normalizado; mesma
chave e conteúdo repetem ID, conteúdo diferente é recusado. Chaves têm escopo
comum aos três comandos por empresa. Autor original é preservado no retry.

Liquidação bloqueia posições por ID crescente e valida âmbito/disponível após
lock. Estorno bloqueia operação original, depois posições por ID crescente;
consulta movimentos e não repõe cegamente um saldo anterior. Não usa locks de
séries, pois não emite documentos. A integração futura terá de harmonizar a
ordem com os circuitos documentais, sem pressupor que já o faz.

Estorno SETTLE cria afectações inversas e caixa inverso, conserva desconto como
componente inverso identificado. Duas afectações independentes à mesma origem
não são dependentes entre si. Estorno CREATE exige reversão de todos os consumos
ativos da posição; bloquear enquanto houver consumo. Não permite estorno de
estorno, double restore nem cascade automático. Novo grafo documental ou recurso
derivado exigirá OP de integração; o grafo suportado aqui é criação→consumo.

## Isolamento e limites

Empresa existente é singleton (CHECK id=1); esta OP não cria multiempresa nem
associação nova cliente/empresa. Scope obrigatório, FKs compostas e rejeição de
empresa inexistente impedem atravessar âmbitos. Moedas e clientes são verificados;
a política operacional do núcleo é EUR. Leituras fora do âmbito são recusadas.
A futura integração deve resolver Scope/autor a partir da identidade autorizada,
não diretamente de um payload público. O serviço não constitui API pública.

Originais e operações guardam metadados financeiros históricos; não substituem
snapshots fiscais V3 ou os PDFs existentes. Sem emissão, numeração, RC/NL,
adiantamentos, rectificações fiscais, alterações de conta corrente ou SAF-T.
NL/RCzero/descontos/adiantamentos/rappels permanecem juridicamente pendentes.
Não certificado pela AT. Segurança institucional permanece formalmente encerrada.

## Testes e evidência

FinancialCoreMoneyTests: política existente e rejeição de entradas inválidas.
FinancialCoreIntegrationTests: A–H da OP20; corresponde ao núcleo dos cenários
OP19 A01–A05, A07–A10, A15, A20–A23, A25–A28, sem emissão documental.
Inclui crédito parcial, caixa200, zero caixa, desconto20/caixa980, refund,
retry concorrente, sobreconsumo concorrente, estornos concorrentes, rollback,
scopes e rejeição SQL de manipulação. Barreiras coordenam duas transações reais.
FinancialCoreMigrationTests: V17→V18, catálogo antigo preservado, zero posições
importadas e segunda inicialização sem migration. TestDatabaseSafetyValidatorTests
protegem a execução. Todas as bases são PostgreSQL16.3 descartáveis `_test`.

Regressão dirigida: documentos comerciais/emissão, PDF financeiro, extrato,
schema e integridade referencial. As44 verificações do controller comercial
incluem a prova adicional de que FT/RC não acrescentam operações ao núcleo.
Fixtures de núcleo e regressão usam bases distintas para não contaminar lookups.
Os resultados efetivos finais e revisão constam de [VALIDATION.md](VALIDATION.md).
