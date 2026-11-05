# Proposta local de integração M05/M4-02

Ordem #46/6656010004. **Destino proposto: `feature/visual-redesign` do repositório `amgsrosario/fac`.** Base remota consultada em leitura por HTTPS em 05-11-2026, `d17de43f714a18c7f7bb7b1fc719f71d77b4e14f` (PR52). O checkout principal continua em 11d5635 com alterações preexistentes docker-compose.yml e .worktrees/. A sua branch local e o tracking antigo não foram usados para determinar a base actual.

| Entrega | HEAD | Relação |
| --- | --- | --- |
| M05 | 828eefa02bd4fbda6f3ad8d0283c879c11cd911f | Dez commits depois da base |
| M4-02 | 43bea3f5e30fb045c3f927997a39d7798d286d2e | Sete commits depois da M05, contém toda a M05 |

Proposta recomendada: usar uma única entrega derivada da branch M4-02, que já contém a sequência completa. Preservar os 17 commits M05/M4-02 para rastrear ensaios, correcções e pareceres. Uma divisão em dois PRs é tecnicamente possível (base→M05 e M05→M4-02), com gestão adicional da base do segundo PR. A opção de publicação e a estratégia de integração ficam reservadas à Administração. A entrega inclui também o commit documental de preparação `e686eb8377965c98d3cd7280d95fd15e6a82ca68`, perfazendo 18 commits antes desta R1. Esta correcção documental acresce à mesma entrega. O relatório final fornece o seu HEAD e a lista completa obtida por `git log --reverse d17de43..HEAD`, evitando atribuir ao documento o hash do commit que o contém.

## Commits incluídos antes desta R1

c0968cd49fc3f76cd9b3c1232a9f36cb0dc61373 test(pilot): prepare isolated M05 runtime and recovery exercise
a635d62017feee0b73ab7d44d01a75a3d9c6a5de test(pilot): wait for final PostgreSQL TCP readiness and preserve failures
244abddbfd91ee9091becd87a060ab0d81551c33 test(pilot): allow nonroot runtime to read the immutable JAR mount
6318e52fe88e67a01f6cde1f0b51f43bcaeab38f docs(pilot): record M05 runtime recovery evidence and review handoff
4d4d9e47aa273a34e286ec364b1a4453a4c5d861 test(m05): make isolated runbook portable and delimit evidence
80fbed84c811e9886db653622b59a06814adf473 docs(m05): record R1 default-path evidence and review handoff
22fc4c643cdf2068bd106d97ac8bebdc1e65b932 docs(m05): correct provenance of the R1 package and runtime evidence
74dab7b9c6ee8d021114e2ef7bcc1f094c46aff5 docs(m05): restore H2-07 history and record R2 confirmation
1d1c63da79340c9df3b6c07dd81dcc8156d68ff7 docs(m05): align remaining R1 review status in memory
828eefa02bd4fbda6f3ad8d0283c879c11cd911f docs(m05): record verified evidence and administrative closure
323503495ab19f94805f573fa30d87d17b7d5a59 test(m4-02): prove governed deferred FT via committed MockMvc requests
083934a96b1f576abd13d27b88a336e5d9ae4fb4 test(m4-02): place catalog fixture in its supported test package
5acca8677f6dc7f6707b6c81869d75a8ebb54171 test(m4-02): read legacy pending customer using its persisted column
e090a4add3fe084bad2244d2e66b76de6ab63b35 test(m4-02): expect the existing conflict on repeated emission
835c8fc5ea2d17f6dc90daf04444b78cf63c0d90 docs(m4-02): record persisted positive proof and independent review handoff
c24348a8cc8857957e276ed97c679951ed3a6657 docs(m4-02): distinguish synthetic proof from pending production evidence
43bea3f5e30fb045c3f927997a39d7798d286d2e docs(m4-02): archive independent approval and align remaining status
e686eb8377965c98d3cd7280d95fd15e6a82ca68 docs(planning): prepare M05 M4-02 integration and M04 continuity


## Delta e conflitos

Delta base→43bea3f: 11 ficheiros, 1003 inserções/5 remoções. São três documentos de governação/matriz, três ficheiros de runbook/validação/handoff M05, runner M05, três ficheiros de documentação/runner M4-02 e o teste dirigido. A lista exacta e o diff estão no arquivo de entrega. src/main, frontend, pom.xml e migrations têm diff vazio. Os únicos executáveis acrescentados são ferramentas de ensaio e teste.

A base remota é ancestral da M05, que é ancestral da M4-02. Não existem linhas divergentes entre estas referências que produzam conflito de merge. Esta conclusão é de ancestralidade, sem efectuar merge. Se o remoto avançar, repetir apenas a comparação Git necessária antes de publicar.

A worktree M05 tem alterações documentais locais em INSTITUTIONAL_MEMORY.md e MATRIX.md. Foram preservadas. A comparação mostrou conteúdo histórico já incorporado na M4-02 e versões anteriores do estado do requisito. Não voltar a aplicar esses diffs sobre a branch consolidada. Não apagar a worktree ou as alterações por esta constatação.

## Revisões e evidências

M05 encerrada conforme o registo [#46/6638129158](https://github.com/amgsrosario/fac/issues/46#issuecomment-6638129158), após a aprovação condicionada #6638075940. Parecer de fecho da prova integral/MINOR001 e sidecar ec77d8f7e7a3a56a264ed4f748eec278f9dd06ba7a560310c6802f83d4e4f1d4. Prova integral: manifesto d295e03cfe80cc0a8d7411b2973418b8b619aa5f8a95739b2932b38e1761a77c. Prova 409 no JAR limitada a M5N: parecer 962394e0fc752564b705d6c57ce0e4e146f4a2070205168b322e581721dd6b64.

M4-02 positivo confirmado no contexto MockMvc sintético: parecer e212d533b3ef1f75a0fcd5b169c16bdbb733a11117884464c800e18e6d51ab33 e confirmação R1 04b22642b57115efaf5a50a234e1eb20d5bda17fc7a4d1da426dd87b7bd048d9. As evidências são reutilizadas. O presente levantamento não repete testes.

As revisões aprovaram os perímetros entregues e o seguimento documental ficou em 43bea3f. A presente proposta de continuidade ainda requer apreciação. Nenhum conflito documental foi resolvido por merge nesta preparação.


## Salvaguardas da entrega

A futura publicação neste repositório público expõe os caminhos locais, identificadores sintéticos e ferramentas contidos no diff. Os arquivos privados de evidência e logs brutos não integram o delta. A Administração deve apreciar essa exposição na decisão de publicação. Preservar as branches, as alterações locais M05 e os arquivos, sem transportar credenciais ou dados pessoais para o repositório. Revalidar o remoto e os checks/revisões exigidos no momento autorizado de publicação. A aprovação dos ensaios e a preparação desta entrega não activam catálogo, RC/RL, FinancialCore ou Fatia 1e. Mantêm-se a Partner Demo, o GOLDEN e as bases conservadas.


## Delta cumulativo actualizado

Até ao HEAD revisto `988dc1a68d8b8ccbcf02103bba21be48fc1d337d`, o delta desde d17de43 abrange 14 ficheiros, +1228/-6. A entrega desta R2 inclui também o seu commit documental. O delta cumulativo até à R2 é 14 ficheiros, +1250/-6. A identificação integral do HEAD e a lista de todos os commits constam do relatório externo, com o hash do diff cumulativo.
