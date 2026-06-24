# Checklist pré-demonstração

## Na véspera

- [ ] Repositório atualizado e branch correta.
- [ ] Working tree revista, sem alterações inesperadas.
- [ ] Docker Desktop e PostgreSQL disponíveis.
- [ ] Código compilado e testes completos sem falhas.
- [ ] Base isolada `fac_demo` reposta com `scripts/demo/reset-demo.ps1`.
- [ ] Verificação só de leitura concluída com `scripts/demo/check-demo.ps1`.
- [ ] Frontend compilado com `VITE_FAC_DEMO_MODE=true`.
- [ ] Login confirmado nos três perfis, sem guardar passwords no repositório.
- [ ] PDF de 35 linhas e PDF anulado abertos e legíveis.
- [ ] Resolução do ecrã e zoom do navegador ajustados.
- [ ] Downloads antigos removidos ou organizados.
- [ ] Plano alternativo preparado: PDF e roteiro disponíveis caso uma operação em direto falhe.

## Quinze minutos antes

- [ ] Confirmar no ecrã **FAC Demo Partner Edition** e **Alentejo Sabores, Lda.**
- [ ] Confirmar backend e frontend ativos, sem avisos técnicos no ecrã.
- [ ] Confirmar perfil Spring `demo` e base `fac_demo` apenas no terminal de preparação, nunca no ecrã partilhado.
- [ ] Confirmar seis documentos comerciais e dois recebimentos.
- [ ] Confirmar um documento anulado, um parcial e um liquidado.
- [ ] Confirmar extrato com três movimentos.
- [ ] Confirmar eventos de emissão, anulação, recebimento, login e tentativa negada.
- [ ] Fechar separadores, notificações e ferramentas técnicas que não façam parte do roteiro.
- [ ] Confirmar que passwords, tokens, terminais e ficheiros locais não estão visíveis.

## Comando de verificação

```powershell
$env:SPRING_PROFILES_ACTIVE = "demo"
.\scripts\demo\check-demo.ps1
```

O resultado válido começa por `FAC_DEMO_CHECK OK`. Qualquer desvio termina o comando com código diferente de zero. O comando não cria, altera nem elimina dados.
