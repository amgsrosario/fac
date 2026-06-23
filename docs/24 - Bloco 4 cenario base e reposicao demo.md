# Bloco 4 — cenário-base e reposição demo

## Arquitetura

O cenário usa uma base PostgreSQL exclusiva, `fac_demo`. Flyway mantém apenas a estrutura; `demo/demo-base.sql` prepara catálogos e dados mestres e `DemoScenarioService` cria documentos, recebimentos, anulação e auditoria através dos serviços do domínio. Assim, numeração, ATCUD, QR, snapshots, pendentes e regras funcionais são exercitados como na aplicação.

## Reposição no Windows

Configurar localmente as variáveis do ficheiro `.env.demo.example` e executar:

```powershell
$env:SPRING_PROFILES_ACTIVE='demo'
$env:FAC_DEMO_RESET_AUTHORIZED='true'
$env:FAC_DEMO_PASSWORD_ADMIN='<segredo local>'
$env:FAC_DEMO_PASSWORD_OPERADOR='<segredo local>'
$env:FAC_DEMO_PASSWORD_CONSULTA='<segredo local>'
.\scripts\demo\reset-demo.ps1
```

O script recusa qualquer base diferente de `fac_demo`, exige perfil `demo`, autorização explícita e três passwords com pelo menos oito caracteres. Depois recria somente `fac_demo`, executa Flyway, semeia através do backend, gera PDFs e valida utilizadores, perfis, documentos, anulação, pagamento parcial, documento multipágina, extrato e auditoria.

## Utilizadores

- `admin.demo` — `ADMINISTRADOR`
- `operador.demo` — `OPERADOR`
- `consulta.demo` — `CONSULTA`

As passwords nunca são persistidas em texto simples: são lidas do ambiente e gravadas com BCrypt. `.env.demo.example` contém apenas marcadores.

## Dados

Emitente fictício `Alentejo Sabores, Lda.`, cinco clientes, oito artigos/serviços, séries `FT/DEMO26` e `RC/DEMO26`, seis faturas emitidas, recebimentos parcial e total, documento anulado, cliente espanhol e documento com 35 linhas para o MD 15.

## Arranque da demonstração

Após a reposição, iniciar com perfil `demo`, `DATASOURCE_URL=jdbc:postgresql://localhost:25432/fac_demo`, as mesmas passwords e um `FAC_JWT_SECRET` local. No frontend, definir `VITE_FAC_DEMO_MODE=true` para mostrar a identificação “FAC Demo Partner Edition”.

## Limitações e MD 15

Os dados espanhóis usam apenas as regras fiscais atualmente suportadas; não simulam uma regra intracomunitária ainda inexistente. O documento de 35 linhas e o documento anulado ficam preparados para inspeção visual multipágina no MD 15. A reposição pressupõe o contentor PostgreSQL local `facdb` em execução.
