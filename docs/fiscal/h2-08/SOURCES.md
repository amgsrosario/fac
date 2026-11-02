# H2-08 — Rastreabilidade normativa e limites de confirmação

Consulta pública em 02-11-2026. **N** descreve o exemplar; não é uma conclusão
jurídica de vigência/aplicabilidade exaustiva ao TUULI. **F** é evidência Git/código;
**P** é recomendação. Não foram consultados portais AT autenticados, bases de
clientes, chaves, contratos de fornecedor ou serviços pagos.

## Matriz D-SIGN-04

| ID / fonte oficial | Conteúdo a rastrear no exemplar | Confirmação alcançada / reserva humana |
| --- | --- | --- |
| N01 [Portaria 363/2010, exemplar consolidado](https://info.portaldasfinancas.gov.pt/pt/informacao_fiscal/legislacao/diplomas_legislativos/Documents/Portaria_363_2010.pdf) | Arts. 3–6: certificação, identificação, conservação e impressão; alterações indicadas no exemplar | Texto e origem pública confirmados; validar regime consolidado aplicável e responsabilidade do produtor. Não comprova certificado TUULI |
| N02 [Despacho 8632/2014](https://info.portaldasfinancas.gov.pt/pt/informacao_fiscal/legislacao/diplomas_legislativos/Documents/Despacho_n%C2%BA_8632_2014_03_07.pdf) | §§4.1/6.2/7.1 exigem no exemplar RSA-1024, SHA-1, PKCS#1 v1.5, 172 Base64; §§2.1.1/2.1.4/4.2/4.3/3.1.4: chave do produtor, Modelo 24/upload, exclusivo conhecimento e protecção; §§2.1.2/2.6.5/3.1.2–3: persistência, cifragem, Hash/HashControl, backups/reposições; §§2.1.3–5/3.4.2–3: versão/cadeia/datas | Requisitos, não sugestões de algoritmo. Estado editorial «Vigente» e presença no portal não demonstram ausência de instruções posteriores; responsável fiscal/jurídico deve confirmar perfil/aplicabilidade antes de escolha produtiva |
| N03 [Portal de certificação](https://info.portaldasfinancas.gov.pt/pt/apoio_ao_contribuinte/Negocios/Faturacao/Regras_mecanismos_comunicacao/Certificacao_programas/Certificacao_software_faturacao/Paginas/default.aspx) | Índice de normas, lista e ferramenta de validação, evidências de chave pública/Modelo 24 | Página confirmada; não se consultou certificação concreta, executou validador ou descarregou chave privada de exemplo |
| N04 [Portaria 195/2020](https://info.portaldasfinancas.gov.pt/pt/informacao_fiscal/legislacao/diplomas_legislativos/Documents/Portaria_195_2020.pdf) | Arts. 2–5: séries/tipo, ATCUD e projecção QR | Exemplar confirmado; não comprova comunicação de qualquer série TUULI, homologação 2/1/1 ou adequação da representação concreta |
| N05 [QR v1.1, Outubro 2020](https://info.portaldasfinancas.gov.pt/pt/apoio_ao_contribuinte/Negocios/Faturacao/Regras_mecanismos_comunicacao/Comunicacao_de_series_ATCUD/Codigo_barras_bidimensional/Documents/Especificacoes_Tecnicas_Codigo_QR.pdf) | Conteúdo Q/R e geometria; relação com projecção documental | Exemplar confirmado; R numérico de exemplo não é certificado. Confirmar regras/projecção actuais para o perímetro antes de produção |
| N06 [URL histórica da FAQ 4443](https://info.portaldasfinancas.gov.pt/pt/apoio_contribuinte/questoes_frequentes/pages/faqs-00879.aspx) | Resposta integral sobre documento anulado/QR, a obter | HTTP 404 novamente; não há exemplar integral obtido nesta missão, nem hash de resposta fictícia. Não concluir que a FAQ deixou de existir; não decidir política pela ausência da página |

**Conclusão limitada:** o portal consultado ainda referencia o Despacho; a H2-07
reproduziu o seu perfil em ensaio. Não se declara esse perfil aprovado para
produção em 2026 nem se escolhe outro por preferências de segurança/provider.
Uma incompatibilidade com políticas de criptografia actuais deve ser escalada
com evidência para confirmação competente; não se resolve por votação do PO.
A escolha comercial de A/B vem depois de saber o que o mecanismo terá de cumprir.

Reservas a devolver ao responsável fiscal/jurídico nomeado: enquadramento actual
com diplomas posteriores; perfil exacto/representação e oráculos; identificação
legítima do produtor/certificado; procedimentos de chave/versão/séries aplicáveis;
menções/exportação/retenção e política de documentos anulados/reimpressão.
Não há prazo legal de retenção inventado nem reconhecimento de validade fiscal
para demo ou para os 194 testes. RC/RG conserva análise própria, fora desta fatia.

### N02 — complemento R1 de rastreabilidade

Conferidos no mesmo exemplar, sem nova conclusão de vigência: §§2.1.1 e
2.1.4/4.2 vinculam chave do produtor e proveniência Modelo 24/upload; §§4.3 e
3.1.4 condicionam custódia e protecção em uso. D-SIGN-03 deve ligar cada versão
à chave pública comunicada, mantendo âncora independente. D-SIGN-05 exige esse
gate antes da rotação. Validar juridicamente B2 face ao exclusivo conhecimento,
sem transformar o regime de chave em escolha comercial por cliente.

§§2.1.2 e 2.6.5 mapeados para persistência/associação/conservação da assinatura,
regime de cifragem da base e exportação Hash/HashControl históricos; §§3.1.2–3
para política periódica de backups, número de reposições e controlo de manipulação.
Interpretação/aplicabilidade actual, incluindo cifragem de BD/suportes/backups,
requer validação competente. Não se autoriza desactivar segurança ou dispensar
requisitos por esta reserva. Critérios em D-SIGN-05 e T08/T11/T12/T16.

A/B1/B2 **sem preferência** até confirmação D-SIGN-04 e suporte real. Eventual
incompatibilidade de KMS/HSM com o perfil do exemplar é hipótese a confirmar por
fontes técnicas/ensaios; o revisor não verificou fornecedores ou vigência e esta
R1 não transforma essa hipótese numa conclusão sobre serviços concretos.

## Exemplares e integridade

[Metadados de consulta e SHA-256](sources.json) são versionados com este dossier.
Os cinco exemplares públicos obtidos estão preservados em arquivo técnico
`/home/arosario/tuuli-backups/h2-08/2026-11-02/`, sem alterar o arquivo H2-06.
Metadados incluem URL, URL final, data UTC, estado, tamanho e hash ou erro.
O índice HTML é conteúdo de consulta, não norma; os PDFs podem conter material
**ilustrativo** da própria publicação, que não foi importado/utilizado como chave.
Nenhuma chave real ou credencial foi gerada, descarregada de serviços operacionais
ou solicitada. Não se arquiva uma página de erro como se fosse resposta da FAQ.

Hash permite conferir identidade do exemplar, não a sua autenticidade fiscal
ou vigência por si só. As referências administrativas originais e o contexto
humano continuam necessários; uma assinatura auto-declarada não cria confiança.

## Fontes institucionais e de implementação

- [Issue #46](https://github.com/amgsrosario/fac/issues/46): H2-08 autorizada;
  não aprova D-SIGN-02..05.
- [Confirmação H2-07-R1](https://github.com/amgsrosario/fac/issues/44#issuecomment-6580103638)
  e [integração](https://github.com/amgsrosario/fac/issues/44#issuecomment-6580136463):
  registos executivos verificáveis, distintos dos ficheiros originais Claude.
  Não se declara arquivo byte a byte do parecer original sem o obter/conferir.
- [PR #45](https://github.com/amgsrosario/fac/pull/45): API merged=true e
  merge `f7e8f8ad405a0e5714af0e9fc7264a9e581ff0de`, com ancestralidade `e7eb142`.
- [H2-06](../h2-06/PROPOSAL.md), [R1](../h2-06/SOURCES_REVIEW.md),
  [H2-07 contrato](../h2-07/CONTRACT.md) e [validação](../h2-07/VALIDATION.md).
  H2-08 acrescenta preparação de decisão, sem reabrir achados encerrados.
- AGENTS.md e GOV-ALT-001 lidos na base integrada; memória/estado actualizados
  apenas quanto ao fecho H2-07 comprovado e à preparação H2-08.
- Governação central `AI_DEVELOPMENT_SYSTEM.md`, AR2-Governance
  `2f6cd4a83aa9e552d1daa32c51a99a7bedd0b4e5`, consultada em leitura.
  Codex executor/Claude independente; merge reservado à Administração.
- [Mapa de código e testes futuros](INTEGRATION_TESTS.md): referências de linha
  válidas na base fixa. Ficheiros estritamente ligados a assinatura/emissão/
  série/QR/PDF/origem financeira/demo; não foi feita auditoria geral.

## Verificações efectivamente executadas

Git/status/worktrees/HEAD remoto/API/ancestralidade; comparação estática do código;
consulta/arquivo de fontes públicas; hashes, referências Markdown e diff/whitespace.
Nenhum teste de aplicação, DB, emissão, chave, migration, benchmark ou serviço
remoto de assinatura foi executado. Os T01–T16 são critérios futuros, não
resultados desta missão. Não se repetem os testes H2-07 nem se apresenta CI
safety/package como validação de conformidade fiscal.
