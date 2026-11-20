# TAXPOINT-01 — ligação entre pedido, implementação e provas

Base `464e5b052997f1790b365188097d54552e00048d`, confirmada por HTTPS. Branch `codex/taxpoint-date`. Código ensaiado no HEAD `0d8ad4591c7e7628693bb452bb9fc2c0343e539d`, tree `b209086b631d4ce3d6fe32b266263c87f5e6cb4c`. Este registo documental é posterior aos ensaios.

A decisão D1, opção A, de 20/11/2026 na #46 estabelece a data de prestação do documento com fallback explícito para data de emissão. A confirmação da Administração recebida nesta conversa aceita datas anteriores, iguais ou posteriores, com o fundamento indicado no artigo 8.º, n.º 1, alínea c), do CIVA. Aplicação e V33 aceitam datas válidas e mantêm o campo opcional em rascunho. XML e PDF usam a fonte conforme o respectivo contrato.

| Ponto do mandato | Prova e âmbito |
| --- | --- |
| 1 — V33 | Migration aditiva, date anulável e guarda própria. `DocumentTypeImmutabilityTests` verifica upgrade V24→V33 em PostgreSQL descartável, data nova NULL num documento histórico e preservação de todos os campos anteriores. O oráculo de migrations passou de 5 para 9 para corresponder ao esquema integrado, e a comparação histórica abrange os campos anteriores às adições. `CommercialServiceDateTests` verifica recusa por SQL de preencher, substituir ou apagar a data de um documento emitido, e recusa de editar via API. |
| 2 — Resolução única | `SaftFtExport.resolveTaxPointDate(invoice, row)` lê o snapshot existente. A futura coluna na linha, sob mandato próprio, entra na precedência já definida, sem mudar a assinatura do resolvedor ou da interface de evidência e sem mudar o XML. A guarda `TaxPointDate missing` fica como defesa para o caso de ausência de todas as fontes, que a data de emissão obrigatória impede no esquema actual. |
| 3 — Emissão e PDF | `CommercialServiceDateTests` confirma 400 para data inválida, PUT 204 seguido de GET com data posterior, emissão bem-sucedida e menção no PDF. A comparação do texto usa dois PDFs emitidos no HEAD ensaiado, um sem data e outro com data igual à emissão. Após normalizar número, ATCUD e instante de emissão, o texto coincide. A regressão sobre os contratos anteriores de emissão, totais, ATCUD/QR e PDF usa as duas classes existentes intactas. |
| 4 — SAF-T | `ProjectedFrSaftFtExportTests` mantém os dez métodos existentes e acrescenta um cenário que verifica duas linhas por documento no mesmo XML, incluindo prestação anterior, fallback, posterior e original preservada após anulação. Valida XSD e associa a data a cada número documental. A prova REPEATABLE READ, somente leitura, permanece na classe. Ver [contrato SAF-T](SAFT_F_FT_EUR.md). |
| 5 — Ensaio | As cinco classes completas foram seleccionadas para emissão e integridade fiscal, renderização PDF, upgrade e guardas históricas, provas do novo campo e exportação SAF-T. Frontend tsc/Vite e suíte Playwright existente, em cópias limpas do commit `969b0e66208f407d2328334228605e794deaf8b7`. O diff desse SHA até ao HEAD ensaiado contém só as duas classes de teste corrigidas, com árvore frontend e código de produção iguais. |

## Única alteração de expectativa nas dez provas SAF-T anteriores

A rejeição por falta de uma data na antiga fonte `Evidence.line()` foi substituída pelo fallback autorizado na confirmação da D1 de 20/11 (#46). O mesmo método continua a verificar todas as restantes recusas. O teste positivo anterior preserva a expectativa de 02/11 através da data gravada no rascunho.


```diff
- assertThatThrownBy(() -> exporter.export(INSTALLATION,withoutDate,this::validate)).hasMessageContaining("TaxPointDate");
+ long fallback = db.queryForObject("""
+         select min(id) from documento_comercial
+         where estado='EMITIDO' and data_prestacao is null and data_emissao=?
+         """, Long.class, DAY);
+ assertThat(taxPoints(exporter.export(INSTALLATION,withoutDate,this::validate).xml(), fallback))
+         .isNotEmpty().containsOnly(DAY.toString());
```

## Execuções arquivadas

Em PostgreSQL descartável, 73 testes aprovados por modo. Em ambos, as classes contabilizam 2, 17, 26, 17 e 11 testes, respectivamente na ordem listada abaixo, com zero falhas, erros e omissões nas provas aprovadas.

1. `CommercialServiceDateTests` — campo, API, PDF comparativo e guarda SQL.
2. `DocumentTypeImmutabilityTests` — upgrade, preservação do histórico e guardas anteriores.
3. `DocumentoComercialEmissaoIntegridadeTests` — emissão e integridade da FT.
4. `DocumentoComercialPdfServiceTests` — texto e apresentação fiscal do PDF.
5. `ProjectedFrSaftFtExportTests` — XML, fontes de data e regressões do exportador.

| Modo | Manifesto e interpretação |
| --- | --- |
| Sem optimização | `af9fe1b348c38dd396fbe3dd546de809383b2066527e28b5bc1121eb8afc38ee`: quatro classes aprovadas (62 testes), resultado global FAIL devido à cache temporária de Python em falta na classe SAF-T. `19c9cbeb440c651feab876389e307a0ecf2925bef12d3317bdba2067d66f8d76`: repetição dirigida, 11/11 SAF-T no mesmo HEAD, com bibliotecas repostas e verificadas. |
| `PYTHONOPTIMIZE=1` | `92f78e43386251f0a1b6a28f4b741f49960f1a1e33b9ce560e5ab719d740eab1`: manifesto PASS, 73/73 no mesmo HEAD. |
| Frontend | TypeScript/Vite passou em WSL. A primeira execução Playwright em WSL encontrou o Edge em falta. A execução em Windows com Edge nativo terminou com 42/42 e saída 0. Logs, HEAD e versões arquivados. |

Os dois primeiros manifestos FAIL, de HEADs anteriores, foram preservados. Os ajustes trataram a contagem e a comparação do oráculo de upgrade, os campos permitidos no PUT, a normalização do instante impresso e a expectativa HTTP 204 seguida de leitura por GET. O código de produção manteve-se idêntico desde o primeiro ensaio. As classes existentes de emissão e PDF mantiveram o mesmo conteúdo.

O arquivo `Arquivo-Reservado/TAXPOINT-01/2026-11-20/` contém os logs, comandos, XML, fontes de teste, manifestos, sidecars, proveniência das dependências XSD e registos de remoção dos recursos. As 226 entradas foram conferidas. SHA-256 do índice `58219b8c37741657fa5bf99249a3f0cc950401f82a960c2b81d409499ff6af8d`.

## Ficheiros alterados

- `docs/planning/m04-fiscal-evidence/SAFT_F_FT_EUR.md`
- `frontend/src/DocumentosView.tsx`
- `frontend/src/ui/commercial/documents/DraftDocumentEditor.tsx`
- `src/main/java/com/ar2lda/fac/controller/dto/DocumentoComercialCreateDto.java`
- `src/main/java/com/ar2lda/fac/controller/dto/DocumentoComercialDto.java`
- `src/main/java/com/ar2lda/fac/controller/dto/DocumentoComercialUpdateDto.java`
- `src/main/java/com/ar2lda/fac/fiscal/saft/SaftFtExport.java`
- `src/main/java/com/ar2lda/fac/mapper/DocumentoComercialMapper.java`
- `src/main/java/com/ar2lda/fac/model/DocumentoComercial.java`
- `src/main/java/com/ar2lda/fac/service/DocumentoComercialPdfService.java`
- `src/main/java/com/ar2lda/fac/service/DocumentoComercialService.java`
- `src/main/resources/db/migration/V33__commercial_service_date.sql`
- `src/test/java/com/ar2lda/fac/DocumentTypeImmutabilityTests.java`
- `src/test/java/com/ar2lda/fac/fiscal/catalog/CommercialServiceDateTests.java`
- `src/test/java/com/ar2lda/fac/fiscal/catalog/ProjectedFrSaftFtExportTests.java`
- `docs/planning/m04-fiscal-evidence/TAXPOINT_01_EVIDENCE.md`
