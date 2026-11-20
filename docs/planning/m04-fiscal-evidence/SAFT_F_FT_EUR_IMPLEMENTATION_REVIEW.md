# SAF-T F / FT-EUR — implementação de laboratório e revisão Claude Code

Base PR #62 integrada em `753539a4d82a79fb8dd48ed0c9df85448a2f6f2b`. Branch `codex/saft-f-ft-eur-export`. Código e testes ensaiados `ec4b843c6259f5aecf42106f5428eb835465e62f`, tree `bf1ca1b0071758719b3579c5e086f8434e7dc9f3`. O delta de implementação vai da base ao HEAD ensaiado. O commit deste registo é documental.

## Implementação e fronteira

O serviço interno existe sob o perfil `saft-ft-eur-lab`. Usa uma única transacção PostgreSQL `REPEATABLE READ` read-only. Inventaria os documentos comerciais e financeiros antes de qualquer selecção FT, confronta todos os tipos configurados com a declaração de habilitação e recusa tipos habilitados ou documentos sem cobertura. Um tipo marcado como desabilitado continua sujeito à verificação de documentos já emitidos. A declaração de habilitação é um input controlado no laboratório e não comprova a configuração efectiva da primeira instalação comercial.

Extrai Header, Customer, Product, TaxTable e SalesInvoices para a fatia FT diferida/EUR de IVA normal no continente, emissão própria com proveniência declarada. Consolida masters por referência e recusa conflitos. Conserva anulados e os seus montantes, excluindo-os do TotalCredit de controlo. Usa preço líquido unitário exacto ou escala 12 HALF_UP para divisão periódica. Recusa desconto de cabeçalho não repartido. A validação XSD é condição de devolução do resultado pelo adaptador de laboratório.

Os valores de TaxPointDate, a prova de regime e proveniência, a assinatura integral e a sua versão são exigidos como evidência explícita. A fixture captura uma assinatura sintética auxiliar identificada por documento e número, e comprova-a com uma chave de teste. Esta assinatura verifica o vínculo de ensaio ao número. O ensaio não testa encadeamento, arquivo de assinatura de emissão ou certificação produtiva. O XSD é executado por adaptador de teste com xmlschema 4.3.0 e elementpath 5.1.1, fora das dependências do JAR. O ficheiro oficial é lido da cópia arquivada, SHA-256 `292c0ab4611e3f5a0cdf2abb4e62d9bd41254dc2e76a1fae4d35a8b132d79350`, com acesso externo bloqueado.

Não há endpoint nem ligação a consumidores operacionais. O serviço é uma fatia interna em memória, sem demonstração de capacidade para volumes comerciais. Supplier exigível e famílias sem cobertura são recusados. P12-03 e D-SIGN, a proveniência real de TaxPointDate/regimes, a validação dos campos condicionais para o perímetro comercial e a avaliação fiscal mantêm os gates próprios. Não se infere aptidão comercial da validação estrutural.

## Provas

4 testes aprovados, zero falhas, erros ou ignorados em PostgreSQL 16.3, Java 21 e Maven offline. Execução `20261119-191946-f89f2f`.

1. Duas FT emitidas, uma depois anulada. CustomerID, ProductCode e TaxCode resolvidos no mesmo XML. Estados N e A, número/ATCUD, assinatura sintética, TaxPointDate distinta de InvoiceDate e totais por documento reconciliados. NumberOfEntries 2, TotalCredit 10 EUR. XSD aceitou os XML positivos e recusou InvoiceType inválido. Um agregado incorreto passou no XSD e falhou no oráculo de conteúdo independente.
2. O escritor confirmou GFT SYNTH/3 entre inventário e extracção. O primeiro snapshot manteve 2 documentos e TotalCredit 10 EUR. Uma nova transacção devolveu 3 e 20 EUR.
3. Desconto discriminante com quantidade 3, bruto 3 EUR e desconto 2 EUR. UnitPrice 0,333333333333, líquido 1 EUR, IVA 0,23 EUR e total 1,23 EUR.
4. Recusas por faltar TaxPointDate ou Hash/HashControl, Supplier exigível sem fonte, tipo FR habilitado sem cobertura e FR emitida antes do filtro. O PostgreSQL recusou a tentativa explícita de escrita na transacção de leitura. As leituras persistidas antes/depois do caso positivo confirmaram a ausência de alterações nos documentos, séries, ledger e auditoria.

Arquivo reservado TUULI `SAFT-F-FT-EUR/2026-11-19`. Execução final 17/17 ficheiros verificados, manifesto e sidecar SHA-256 `b2eebb93512edbc596598eaf6b69f5a7da3fc43545276ea003bde79cfdb91d8e`. Inclui XML, logs, Surefire, comandos, snapshot concorrente e prova de remoção de recursos. Quatro tentativas anteriores foram arquivadas separadamente. Os impedimentos corrigidos incluíram acesso do validador à cópia local, import estático no teste, formato do CompanyID sintético, morada incompleta da fixture e pasta de evidência em falta. Não contam como execução aprovada.

## Pedido ao Claude Code

Rever o delta desde `753539a` até ao commit final da branch. Conferir o código exacto ensaiado e os resultados pela proveniência no manifesto. Confirmar a posição do inventário antes da selecção, o limite das declarações da fixture, a mesma transacção de leitura no ensaio concorrente, a distinção XSD/conteúdo, o tratamento de anulados, valores e recusas. Rever também a fronteira experimental, a insuficiência de fontes produtivas e as condições ainda por fechar antes de autorizar utilização comercial.

Revisão independente pendente. Não há canal Claude Code nesta sessão. A entrega aguarda encaminhamento, e a integração depende de decisão expressa da Administração.
