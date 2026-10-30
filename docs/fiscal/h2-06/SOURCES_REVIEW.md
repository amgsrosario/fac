# H2-06 — Fontes, limitações e revisão independente

## Base verificável

- Issue [#43](https://github.com/amgsrosario/fac/issues/43), mandato de análise, sem implementação.
- Base remota observada e usada: `ac07247da7f4dce973b471e5334e2b86bb4e97e4`, merge [PR #42](https://github.com/amgsrosario/fac/pull/42). Ancestralidade de `e1e9e63c478d84f52bc508076d438a76b0eca897` confirmada.
- A referência local `origin/feature/visual-redesign` estava desactualizada (`089b181`); o HEAD efectivo foi consultado por HTTPS e obtido para FETCH_HEAD, sem alterar checkout principal ou a referência tracking. Não usar o tracking antigo como base desta análise.
- CI H2-05: workflow «Piloto TUULI - segurança dos testes», run `41083075726`, concluído com sucesso no HEAD `e1e9e63`. O ficheiro do workflow executa safety tests e package, não a regressão PostgreSQL integral.
- Parecer H2-05-R2 original: `2026-10-28_TUULI_H2-05-R2_confirmacao-final_Claude.md`, arquivo de relatórios TUULI. SHA-256 conferido: `d377b2f572fb9f3c95e1b19dc2bc06e3cd7f0eb663842290086b325c21d6d5af`. APPROVE, 0/0/0. Não é parecer sobre H2-06.
- Governação central consultada exclusivamente em leitura: `AI_DEVELOPMENT_SYSTEM.md`, AR2-Governance `2f6cd4a83aa9e552d1daa32c51a99a7bedd0b4e5`. Codex executor / Claude revisor; sem auto-aprovação independente.

## Fontes primárias consultadas em 30-10-2026

Estes textos sustentam requisitos a validar; não constituem certificação do TUULI nem homologação de tipologias.

| Fonte oficial | Elementos consultados | Bytes / SHA-256 do exemplar |
| --- | --- | --- |
| [Portaria 363/2010, exemplar consolidado no Portal das Finanças](https://info.portaldasfinancas.gov.pt/pt/informacao_fiscal/legislacao/diplomas_legislativos/Documents/Portaria_363_2010.pdf) | Arts. 3–6, assinatura, conservação e impressão; considerar alterações posteriores assinaladas no próprio exemplar | 47074 / `6bbfc8e1e4405542c0fa1533964d3ee0d235d363ba3adf0e83414edb2db2a5b3` |
| [Despacho 8632/2014](https://info.portaldasfinancas.gov.pt/pt/informacao_fiscal/legislacao/diplomas_legislativos/Documents/Despacho_n%C2%BA_8632_2014_03_07.pdf) | §§1.1, 2.1.5, 2.2, 2.6.5, 3.4.2–3.4.3, 4.1, 4.4, 6.2 e 7.1: âmbito, cadeia/plurianualidade, datas, RSA e requisitos criptográficos, impressão e SAF-T | 253457 / `890b9b4dae906035600aa6038e17022ff7f82391473546a36794d9727bfa5bd7` |
| [Especificações QR v1.1, Outubro 2020](https://info.portaldasfinancas.gov.pt/pt/apoio_ao_contribuinte/Negocios/Faturacao/Regras_mecanismos_comunicacao/Comunicacao_de_series_ATCUD/Codigo_barras_bidimensional/Documents/Especificacoes_Tecnicas_Codigo_QR.pdf) | Q/R e geometria, incluindo Byte, ECC M, versão mínima 9, 30 mm e margem 2,5 mm | 957688 / `e0f1fdd24623916ef573dcda9a99de4d3cb09e6396b2c2b01807af460bc552f2` |
| [Portal oficial de certificação](https://info.portaldasfinancas.gov.pt/pt/apoio_ao_contribuinte/Negocios/Faturacao/Regras_mecanismos_comunicacao/Certificacao_programas/Certificacao_software_faturacao/Paginas/default.aspx) | Índice normativo, listagem e validador; sem obter credenciais nem comprovar certificação do TUULI | Consulta web; não apresentado como exemplar binário arquivado |

Exemplares e metadados HTTP/data UTC: arquivo técnico H2-06, `2026-10-30/sources.json`. Não se descarregaram chaves privadas nem se criaram credenciais. Os exemplos normativos não são credenciais utilizáveis.

**FAQ 4443:** o endereço anteriormente usado no dossier devolveu HTTP 404 nesta consulta. O índice de pesquisa não substitui a resposta integral. Não se arquivou uma resposta inexistente nem se atribuiu hash fictício. A matéria de estado do documento anulado/QR reimpresso mantém reserva. Não está demonstrado um exemplar integral arquivado na H2-04/R1; retira-se essa alegação da versão inicial. É necessária a resposta integral verificável antes de concluir esta matéria.

## Limites de confirmação

- Factos de código referem-se à base indicada e aos componentes inspeccionados em CURRENT_CONTRACT. Ausência de produtor/exportador identificável nesse âmbito não prova ausência de sistemas externos.
- Não houve acesso a chaves, certificado legítimo de produção, contratos de HSM/KMS, plataforma de certificação autenticada ou instâncias de clientes.
- O exemplar do Despacho exige RSA (§4.1), chave de 1024 bits, SHA-1, PKCS#1 v1.5 (§6.2) e assinatura Base64 de 172 caracteres sem separadores (§7.1). Não são apenas exemplos nem escolha do PO. A confirmação da vigência/aplicabilidade actual é reserva separada: a indicação editorial «Vigente» não substitui a conferência de alterações/instruções posteriores e enquadramento concreto. Exigir suporte exacto do perfil aplicável em A/B e oráculos T02; não transportar o codec demo nem substituir o perfil por conveniência técnica.
- Nenhum teste da aplicação foi repetido nesta missão documental. Resultados H2-05 são evidência anterior, com revisão e CI de âmbitos diferentes.

## Pedido de revisão Claude Code — preparado, ainda não realizado

Rever o diff documental desta branch relativamente a `ac07247`, lendo integralmente CURRENT_CONTRACT, PROPOSAL e RISKS_TESTS, memória/estado e as fontes acima. Confrontar o contrato observado com o código referido e o mandato da issue #43.

Verificar especialmente: proveniência de assinatura/certificado; distinção ATCUD/Q/R/chave; instante fixado antes do QR; cadeia e relógio global; atomicidade JPA/JDBC e efeitos externos; reimpressão/anulação sem nova assinatura; guards SQL versus prova criptográfica; separação demo/produção; excepção/enquadramento RC/RG; ausência de decisões implícitas de perfil, custódia ou activação.

Entregar APROVADO / APROVADO COM RESERVAS / DEVOLVER PARA CORRECÇÃO, com findings BLOCKER/MAJOR/MINOR e referências exactas. Não implementar correcções nem homologar em nome da Administração. A aptidão documental não autoriza implementação ou activação. Não foi invocado outro Codex como substituto do Claude.

## H2-06-R1 — confirmação incremental solicitada

Fonte dos achados: [decisão executiva na issue #43](https://github.com/amgsrosario/fac/issues/43#issuecomment-6530199084), de 30-10-2026: APROVADO COM RESERVAS, 0 BLOCKER / 1 MAJOR / 2 MINOR. Esta referência contém o resumo executivo; não é o ficheiro original do parecer Claude. O original e respectivo hash ainda não foram disponibilizados nesta correcção; não se declara o seu arquivo byte a byte.

Rever apenas o diff complementar sobre `30dc74c`, confirmando MAJOR-001 (requisitos do exemplar versus vigência actual, compatibilidade A/B e T02), MINOR-001 (alegação FAQ retirada, reserva mantida) e MINOR-002 (série/tipo fiscal, mapeamento interno, plurianualidade, datas e T03/T04). Confrontar §§2.1.5, 3.4.2, 4.1, 6.2 e 7.1 com a fonte oficial. Verificar que não se homologa FR ou outras projecções, não se implementa e não se alteram gates. Emitir parecer independente com severidades; D-SIGN-01 a D-SIGN-05 permanecem suspensas até confirmação. Correcção pelo executor não equivale a fecho independente.
