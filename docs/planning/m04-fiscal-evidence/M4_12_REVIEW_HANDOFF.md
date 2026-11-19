# Claude — Revisão independente M4-12 / Contrato SAF-T

13-11-2026. **Pedido preparado para transmissão pela Coordenação, sem activação automática do revisor.** Contrato NÃO fechado; não homologar decisões em nome da Administração.

## Identidade e delta

Branch/worktree `codex/m04-continuity-post-m404` / `/home/arosario/Projetos/fac/.worktrees/m04-continuity-post-m404`. Base integrada f15388d0ac926cc39082869e898d5ef9998059f7/PR #57; partida local **1e79e878ef459958bc7ec8d5c9b3cf9c94e379c6**. Rever delta documental a partir de1e79e87 até ao HEAD final exacto da entrega. O commit anterior é a continuidade pós-M4-04, não nova prova SAF-T.

## Documentos e fontes

[Contrato](SAFT_CONTRACT.md), [mapeamento](SAFT_FIELD_MAP.md), [decisões/perfil](SAFT_PROFILE_DECISIONS.md), [aceitação futura](SAFT_ACCEPTANCE.md), [proveniência/hashes](M4_12_SOURCES.json); [matriz M04](MATRIX.md) e [quadro 17 requisitos](../continuity-2026-11-05/M04_STATUS.md).

Reutilizados N05 Portaria 302/2016 (exemplar AT, hash 7ef94dfc0d1e8677f5c705abe301148e1cdedfb7a4305634259be095ca37a0d0), N07 XSD 1.04_01 (hash 292c0ab4611e3f5a0cdf2abb4e62d9bd41254dc2e76a1fae4d35a8b132d79350), originais intactos. XSD foi lido estruturalmente, não instalado/executado como validador. Perfil/legalidade fiscal não decorrem de XML bem formado nem da capa vigente do exemplar.

## Conferências prioritárias

1. **Fidelidade de origem:** campos e linhas de Empresa/Cliente/Artigo/DocumentoComercial/LinhaDocumentoComercial e repositórios; verificar o que existe e o que é só proposta. Campo existente não prova conteúdo legítimo em base real. Evitar qualificações de completude que os modelos não sustentam.
2. **Assinatura:** interface S expõe fullSignature/certificado, mas não keyVersion; documento não guarda assinatura integral/HashControl. QR guarda Q/R, que não suprem os campos. Não há fallback, nova arquitectura D-SIGN ou autorização de chaves reais.
3. **Campos discriminantes:** CompanyID vs id1; produtor vs emitente; TaxPointDate vs emissão; SystemEntryDate ligado à gravação/assinatura; ProductNumberCode vs texto/EAN; snapshots vs mestres; regime explícito vs zeros inventados; Debit/Credit na conta de vendas vs sinal da dívida.
4. **Perfil/perímetro:** recomendação F é condicionada; I/C exigem origem contabilística efectiva; P não legitima omitir tipos emitidos; E/S/R/T não aprovados. Uma fatia interna FT/EUR não é exportador comercial completo. Não reabrir correspondência funcional2/1/1 ou semântica aprovada.
5. **Completude/estados:** anulados incluídos e totalizadores próprios; repository de linhas analíticas exclui anulados. Não sugerir N+1, filtragem por cliente arbitrária ou reparação histórica. Tratamento read-only/segurança é proposto para ordem futura, não demonstrado por novo ensaio.
6. **Aceitação e gates:** critérios são futuros; zero testes, SQL, Docker, XML exportado ou implementação. Perfil/perímetro ainda reservados. A questão ATCUD: e melhoria do diálogo continuam abertas. Sem activação FinancialCore, recibos ou 1e.

Emitir APPROVE / APPROVE WITH CONDITIONS / REQUEST CHANGES, achados BLOCKER/MAJOR/MINOR com referências e o alcance efectivamente revisto. Confirmar se o material permite decisão de perfil e apontar apenas lacunas materiais. Parecer positivo não substitui as decisões administrativas aplicáveis; **contrato só fechado após revisão e decisões reservadas**. Preservar parecer original e SHA-256. Sem implementar, alterar bases, emitir, activar, push, PR ou merge.

## Continuidade R1 — pedido incremental

O pedido acima é histórico. Recebido REQUEST CHANGES sobre dd44be9; original/sidecar conferidos com digest `32dd54ce0fe3adcc80350aac99bcd695db857a2b65ef472045429eba3501b047`. Rever apenas o delta desde dd44be99cf30f7e7746efd5bc8ad79f630cc85cd ao novo HEAD documental da entrega: [resolução e fundamentos R1](M4_12_R1_CORRECTIONS.md), map/contrato/aceitação e P12, coerência da matriz/memória/estado. Confirmar MAJOR-001, MINOR-001 e observações O-1 a O-5. Nenhum novo teste ou XML; aprovação ainda não recebida. Pedido preparado para transmissão pela Coordenação, sem presumir activação do Claude por este ficheiro.
