# H2-05 / issue #41 — Pedido de revisão independente Claude Code

Rever a branch `codex/h2-05-fiscal-pdf-qr`, worktree
`/home/arosario/Projetos/fac/.worktrees/h2-05-fiscal-pdf-qr`, contra a base
`91da67bc5e799d30abfd1c569ec7ef22fbd5f703`. Obter o HEAD com `git rev-parse HEAD`;
o hash final consta também do relatório externo de entrega.

Ler AGENTS.md, memória/estado institucional, a ordem issue #41, H2-04/R1 no
commit `64c6a2d1a33cd5926bcd67b5c50c2fc995c126c2`, e este dossier integral.
Confrontar o diff e as evidências originais; não aceitar auto-aprovação do Codex.

Verificar prioritariamente:

1. ATCUD em todas as páginas; acima do QR; PDFs de uma/várias páginas/anulados.
2. Byte real, ECC M, versão mínima 9, crescimento, dimensões/margens no PDF final
   e legibilidade após renderização, incluindo texto fora de Latin-1.
3. Ponte para primitivas internas ZXing: máscara, bits, contagem, Reed-Solomon,
   ECI, capacidade e compatibilidade com 3.5.3, sem substituição da biblioteca.
4. Q nas posições 1/11/21/31, R do certificado aplicável, vínculo documental,
   recusa sem fonte/ambiguidade e rollback antes de consumir numeração.
5. **Não existe produtor produtivo da assinatura.** Determinar se o bloqueio
   explícito de nova emissão é correctamente delimitado. A aprovação técnica
   não deve converter a fixture sintética em evidência de assinatura fiscal real.
6. Reimpressões/payloads históricos preservados; geração financeira opcional;
   nenhuma alteração a histórico, séries, migrations, manifesto ou activações.
7. Conferir 231 testes finais e package, separando execuções locais de CI.

Emitir APPROVE / APPROVE WITH CONDITIONS / REQUEST CHANGES, com contagem
BLOCKER/MAJOR/MINOR e referências exactas. Separar defeitos da implementação
da dependência fiscal explícita da assinatura/certificado e dos gates já abertos.

Revisão exclusivamente de leitura. Sem bases persistentes, merge, activação ou
homologação. Não há execução Claude disponível neste ambiente; esta entrega
prepara o pedido e não declara revisão independente realizada.


## Pedido incremental H2-05-R1

A ordem executiva escolheu a alternativa B do MAJOR-001. Rever exclusivamente
o diff após `32623904e2427e712b77a1002475e341821f4db0` e [R1.md](R1.md):
fornecedor demo separado, ausência de fallback, guard datasource anterior ao
Flyway/JPA, origem persistida/aviso no PDF, chave externa, representação Q/R
deliberadamente não fiscal e produção sem fonte 409. Conferir ATCUD ausente,
registo do gate RC/RG, pin ZXing/smoke JAR, todas as classes emissoras (incluindo
PermissionMatrixIntegrationTests) e ensaios de seed/repetição/reset descartável.
Não homologar configuração fiscal ou considerar RSA demo como assinatura AT.
As falhas intermédias e as respectivas correcções estão preservadas no arquivo
R1, separado da evidência original. A emissão comercial real continua bloqueada.
Solicita-se veredicto e matriz de resolução do MAJOR e três MINOR; não se
declara confirmação Claude antes da revisão efectiva.


## Pedido incremental H2-05-R2

Rever exclusivamente o complemento após `203276f706773b4ae56764d54c88d6ccca0c7479`
e [R2.md](R2.md), incluindo os registos finais pós-commit. Confirmar MINOR-A/B/C
e R-1/R-2, fixação dos testes ao HEAD/tree/worktree limpos e ausência de alterações
posteriores; distinguir os 254 testes do commit original da nova execução final.
Conferir procedimento demo/GOLDEN sem deploy, registo da regressão preexistente
de permissões, diagnóstico de marcadores demo sem alteração de dados e rótulos QR.
Verificar preservação do 409, ausência de fallback, manifesto/gates fechados.
Não declarar um novo GOLDEN aprovado ou revisão positiva antes de parecer efectivo.
