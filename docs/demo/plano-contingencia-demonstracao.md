# Plano de contingencia da demonstracao

| Problema | Diagnostico rapido | Comando | Solucao | Alternativa de apresentacao |
|---|---|---|---|---|
| Aplicacao nao abre | Verificar containers e porta | `docker-compose --project-directory . --env-file .env.demo -f compose.demo.yaml ps` | `docker-compose --project-directory . --env-file .env.demo -f compose.demo.yaml up -d` | Usar roteiro curto e PDFs ja abertos |
| Porta 8088 ocupada | Confirmar processo na porta | `Get-NetTCPConnection -State Listen -LocalPort 8088` | Alterar `FAC_DEMO_HTTP_PORT` e reiniciar frontend | Mostrar em localhost alternativo |
| Backend indisponivel | Healthcheck do backend | `docker-compose --project-directory . --env-file .env.demo -f compose.demo.yaml ps backend` | Recriar backend com Compose | Mostrar dados ja carregados e explicar fluxo |
| PostgreSQL indisponivel | Estado do servico `db` | `docker-compose --project-directory . --env-file .env.demo -f compose.demo.yaml ps db` | `docker-compose --project-directory . --env-file .env.demo -f compose.demo.yaml up -d db` | Mostrar PDF e roteiro |
| Login falha | Confirmar perfil/base e passwords locais | `.\scripts\demo\prepare-commercial-demo.ps1 -SkipBackup` | Reset com passwords corretas | Ficar em sessao ja autenticada |
| Password incorreta | Variaveis locais incoerentes | Rever `.env.demo` sem partilhar ecra | Corrigir e executar `-Reset` | Usar outro perfil valido |
| PDF nao abre | Testar outro documento | Abrir FT DEMO26/6 | Recarregar pagina ou usar PDF previamente exportado | Explicar evidencias do check |
| Importacao falha | Ver mensagens de validacao | Usar modelo oficial | Repetir com ficheiro modelo | Mostrar exportacao e validacao sem confirmar |
| Demo incoerente | Check falha | `.\scripts\demo\prepare-commercial-demo.ps1 -Reset` | Reset e novo check | Usar backup restaurado |
| Rede local falha | Testar localhost | `http://127.0.0.1:8088` | Apresentar no proprio computador | Evitar acesso remoto |
| Browser mantem cache | Testar janela anonima | Ctrl+F5 | Limpar cache ou trocar browser | Usar separador preparado |
| Container unhealthy | Ver logs | `docker-compose --project-directory . --env-file .env.demo -f compose.demo.yaml logs --tail=80` | Recriar servico afetado | Passar para roteiro curto |
| Reset incompleto | Check demo falha | `.\scripts\demo\prepare-commercial-demo.ps1 -Reset` | Reexecutar reset controlado | Restaurar backup recente |
| Check falha | Ler primeira mensagem funcional | `FAC_DEMO_CHECK` ou `FAC_COMMERCIAL_DEMO_CHECK` | Corrigir dado local ou reset | Nao improvisar dados em direto |
