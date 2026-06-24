# Bloco 5 — Consolidação funcional e roteiro da demonstração comercial

## 1. Resultado

O FAC Demo Partner Edition dispõe de um cenário comercial coerente, repetível e verificável, exclusivamente no perfil `demo` e na base `fac_demo`. O trabalho reutiliza o cenário do MD 16 e os serviços reais de emissão, numeração, ATCUD, QR, recebimentos, anulação, extrato, permissões e auditoria.

Não foram criadas migrações Flyway para a demonstração, regras fiscais intracomunitárias, dados estáticos de fachada ou dependências do computador Ubuntu.

## 2. Componentes reutilizados do MD 16

- `application-demo.yaml` e `.env.demo.example`;
- `DemoScenarioService`, `DemoScenarioRunner` e `DemoResetSafety`;
- `scripts/demo/reset-demo.ps1`;
- dados mestres e narrativa existentes;
- serviços de domínio e respetivas validações;
- testes de segurança, emissão, anulação, financeiro, extrato, PDF e isolamento.

O verificador foi separado do seed apenas para garantir leitura estrita: a mesma validação é chamada depois do reset e pelo comando independente.

## 3. Consolidações efetuadas

- cenário exato: 3 utilizadores, 5 clientes, 8 artigos/serviços, 2 séries, 6 documentos comerciais e 2 recebimentos;
- numeradores `FT DEMO26/6` e `RC DEMO26/2`, sem duplicações;
- seis snapshots fiscais com número completo, ATCUD, QR e PDF gerável;
- um documento anulado, um parcialmente pago, um totalmente pago e um documento com 35 linhas;
- extrato com três movimentos;
- auditoria de login, emissão, recebimento, anulação e tentativa negada;
- PDF gerado em modo de validação sem criar eventos de auditoria;
- permissões funcionais consolidadas para mestres, tesouraria e configuração;
- ações não autorizadas ocultadas no frontend e recusadas no backend;
- mensagens 401, 403 e validações funcionais sem detalhes técnicos;
- identificação profissional da edição demo na entrada e página inicial.

## 4. Verificação segura

`scripts/demo/check-demo.ps1` exige perfil `demo`, recusa qualquer base diferente de `fac_demo`, desativa Flyway, seed e autorização de reset, e termina com código não zero perante qualquer divergência. Não apaga, recria ou altera dados e não requer passwords dos utilizadores demo.

O resumo confirma base/perfil, utilizadores/papéis, dados mestres, séries, documentos, recebimentos, estados financeiros, 35 linhas, seis PDFs, extrato e auditoria sem referências quebradas.

## 5. Perfis demonstrativos

| Perfil | Capacidades demonstráveis |
|---|---|
| ADMINISTRADOR | operação completa, configuração, anulações e auditoria |
| OPERADOR | clientes, artigos, documentos e recebimentos, sem configuração nem anulação |
| CONSULTA | leitura e PDFs, sem ações de alteração |

A proteção do backend é independente da visibilidade dos botões no frontend.

## 6. Inspeção visual do PDF longo

Em 24/06/2026, `FT DEMO26/6` foi obtido pela API e as duas páginas A4 foram renderizadas para imagem. Confirmaram-se 35 linhas completas, cliente e identificação documental, cabeçalho de colunas repetido, totais e resumo de IVA juntos, ATCUD, QR, rodapé, paginação, ausência de sobreposições, linhas cortadas, página vazia ou conteúdo fora da área imprimível.

Pendência MD 15, não bloqueante: a página de continuação repete o cabeçalho das colunas, mas não o cabeçalho integral com emitente e referência. Deve ser reavaliada se cada página tiver de ser compreensível quando impressa isoladamente.

## 7. Entregáveis operacionais

- `docs/demo/roteiro-demonstracao-parceiro.md` — percurso de 19 minutos em seis blocos;
- `docs/demo/checklist-pre-demonstracao.md` — preparação técnica, funcional e visual;
- `docs/demo/narrativa-dados-demo.md` — correspondência entre clientes, documentos e perfis;
- `scripts/demo/check-demo.ps1` — inspeção automática só de leitura.

## 8. Limites comunicados

Esta edição serve uma demonstração funcional controlada. Não afirma certificação fiscal, cobertura legal completa, proteção operacional de produção, comunicação real à AT ou tratamento integral de cenários intracomunitários. O MD 17, publicação externa, HTTPS, domínio, reverse proxy, acesso remoto e computador Ubuntu ficaram fora do trabalho.

## 9. Validação final — 24/06/2026

- backend: compilação, testes e empacotamento concluídos;
- frontend: TypeScript e build Vite em modo demo concluídos; o bundle contém a edição e empresa esperadas;
- suíte completa: 125 testes, 0 falhas, 0 erros, 0 ignorados;
- reset 1: `FAC_DEMO_READY`, com 3/5/8/2/6/2 e todas as evidências completas;
- reset 2: o mesmo resultado, confirmando repetibilidade;
- `check-demo`: `FAC_DEMO_CHECK OK`, executado depois dos resets;
- auditoria antes/depois do check: 17/17 eventos;
- base `fac`: nenhuma configuração ou processo desta execução lhe apontou; logs de aplicação mostram `fac_demo` e testes mostram `fac_test`; os contadores de inserts, updates e deletes de `fac` permaneceram a zero e não existiam sessões ativas na inspeção;
- PDF: duas páginas A4 inspecionadas visualmente.

As passwords permanecem exclusivamente em variáveis de ambiente e nunca são guardadas em Git, scripts, documentação, frontend, screenshots ou dados de seed.
