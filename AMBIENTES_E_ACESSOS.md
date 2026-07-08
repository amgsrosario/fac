# FAC — Ambientes, servidores e acessos locais

Este documento serve como mapa rápido dos ambientes FAC usados em desenvolvimento local e demonstração Docker. O objectivo é evitar confusão entre portas, bases de dados, containers e credenciais demo.

## Regra Mental Principal

| Porta / nome | Significado |
| --- | --- |
| `5173` | Frontend Vite local de desenvolvimento |
| `8080` | Backend Spring Boot local |
| `8088` | Demo Docker completa |
| `25432` | PostgreSQL local `facdb` |
| `5050` | pgAdmin local |
| `admin.demo` | Utilizador da demo; não pertence necessariamente ao ambiente local ligado à base `fac` |

## Ambiente Local De Desenvolvimento

### Frontend

| Item | Valor |
| --- | --- |
| URL | `http://localhost:5173` |
| Proxy API | `/api` para `http://localhost:8080` |

Comando:

```powershell
cd C:\projeto_faturação\fac\frontend
npm run dev
```

### Backend

| Item | Valor |
| --- | --- |
| URL | `http://localhost:8080` |
| Health | `http://localhost:8080/actuator/health` |

Comando:

```powershell
cd C:\projeto_faturação\fac
java -jar target\fac-0.0.1-SNAPSHOT.jar
```

### Base De Dados

| Item | Valor |
| --- | --- |
| Container | `facdb` |
| Porta host | `25432` |
| Porta interna | `5432` |
| Base usada pelo backend local | `fac` |

Nota: `admin.demo` não existe na base `fac`, salvo se for criado manualmente ou por seed específico.

### pgAdmin

| Item | Valor |
| --- | --- |
| URL | `http://localhost:5050` |
| Container | `fac-pgadmin` |

## Demo Docker Completa

### Frontend Demo

| Item | Valor |
| --- | --- |
| URL | `http://localhost:8088` |
| Container | `fac-demo-frontend-1` |
| Porta | `8088:80` |

### Backend Demo

| Item | Valor |
| --- | --- |
| Container | `fac-demo-backend-1` |
| Porta | `8080` apenas interna no Docker, não publicada no host |
| Profile | `demo` |
| Datasource | `jdbc:postgresql://db:5432/fac_demo` |

### Base Demo Docker

| Item | Valor |
| --- | --- |
| Container | `fac-demo-db-1` |
| Base | `fac_demo` |
| Porta host | Sem porta publicada |

Esta base é separada da base `fac_demo` que possa existir dentro do container local `facdb`.

### Endpoints

Endpoint correcto para validar o backend demo através do frontend Docker:

```text
http://localhost:8088/api/actuator/health
```

Endpoint enganador:

```text
http://localhost:8088/actuator/health
```

Este segundo URL cai no SPA/frontend e não deve ser usado para validar o backend demo.

## Diferença Entre `fac_demo` No `facdb` E `fac-demo-db-1`

`facdb` é o PostgreSQL local usado pelo ambiente de desenvolvimento e testes. Dentro de `facdb` podem existir várias bases, por exemplo:

- `fac`;
- `fac_demo`;
- `fac_test`;
- outras bases temporárias de validação.

A demo Docker oficial usa outra base, dentro do container `fac-demo-db-1`.

Por isso, a existência de uma base chamada `fac_demo` em `facdb` não significa que a demo Docker esteja a usá-la. A demo Docker usa o seu próprio PostgreSQL interno.

## Credenciais Demo

- `admin.demo` existe na demo Docker.
- `admin.demo` também pode existir em `facdb/fac_demo`.
- `admin.demo` não existe na base `fac` usada pelo backend local em `8080`, salvo criação manual ou seed específico.
- As passwords demo não estão hardcoded no código.
- O seed demo espera estas variáveis:
  - `FAC_DEMO_PASSWORD_ADMIN`
  - `FAC_DEMO_PASSWORD_OPERADOR`
  - `FAC_DEMO_PASSWORD_CONSULTA`

Consultar o `.env.demo` local ou documentação operacional privada para os valores reais.

Nunca colocar passwords reais neste ficheiro versionado.

## Comandos Úteis

### Ambiente Local

Arrancar PostgreSQL e pgAdmin locais:

```powershell
cd C:\projeto_faturação\fac
docker-compose up -d
```

Compilar e arrancar backend local:

```powershell
cd C:\projeto_faturação\fac
mvn -q -DskipTests package
java -jar target\fac-0.0.1-SNAPSHOT.jar
```

Arrancar frontend Vite local:

```powershell
cd C:\projeto_faturação\fac\frontend
npm run dev
```

### Demo Docker

Preparar e arrancar a demo Docker:

```powershell
cd C:\projeto_faturação\fac
.\scripts\demo\prepare-commercial-demo.ps1
```

Recriar a demo de forma controlada:

```powershell
cd C:\projeto_faturação\fac
.\scripts\demo\prepare-commercial-demo.ps1 -Reset
```

## Como Confirmar O Que Está A Correr

Ver containers Docker:

```powershell
docker compose ps
```

Validar backend local:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

Validar proxy Vite para o backend local:

```powershell
Invoke-RestMethod http://localhost:5173/api/actuator/health
```

Validar backend demo Docker através do frontend demo:

```powershell
Invoke-RestMethod http://localhost:8088/api/actuator/health
```

Resumo:

- `5173/api/actuator/health` valida o proxy Vite para o backend local.
- `8080/actuator/health` valida o backend local.
- `8088/api/actuator/health` valida o backend demo Docker.

## Erros Comuns

### `Utilizador ou password inválidos` Em `http://localhost:5173` Com `admin.demo`

Causa provável:

O backend local está ligado à base `fac`, onde `admin.demo` não existe.

Solução:

Usar um utilizador da base `fac`, criar seed local apropriado, ou usar a demo Docker em `http://localhost:8088`.

### `Vite http proxy error ECONNREFUSED`

Causa provável:

O frontend Vite está ligado, mas o backend local em `8080` está desligado.

Solução:

Arrancar o backend local e confirmar:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

### `pgAdmin CSRF token missing` / `tokens do not match`

Causa provável:

A sessão ou os cookies do pgAdmin expiraram.

Solução:

Fazer refresh, abrir janela anónima, ou reiniciar o container:

```powershell
docker restart fac-pgadmin
```

## Avisos Importantes

- Não confundir `5173` com `8088`.
- Não assumir que a demo Docker tem o código mais recente da branch local; pode ser necessário reconstruir a imagem.
- Não usar `admin.demo` em `5173`, salvo se o backend local estiver ligado a uma base onde esse utilizador exista.
- Não correr `prepare-commercial-demo.ps1 -Reset` sem intenção clara, porque recria a demo.
- Não colocar passwords reais neste documento.
