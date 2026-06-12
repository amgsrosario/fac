# 05 - Implementação da Camada de Segurança

## Estado do Documento

| Campo | Valor |
|---------|---------|
| Documento | 05 - Implementação da Camada de Segurança |
| Projeto | FAC |
| Versão | 1.0 |
| Estado | Draft |
| Data | 12-06-2026 |
| Autor | António Rosário |

---

# 1. Objetivo

Este documento define a primeira implementação da camada de segurança do projeto FAC.

O objetivo é disponibilizar uma solução simples, robusta, moderna e evolutiva, garantindo:

- Autenticação de utilizadores;
- Proteção dos endpoints da API;
- Controlo de acessos baseado em perfis;
- Armazenamento seguro de credenciais;
- Preparação para futuras evoluções funcionais.

Esta implementação destina-se à primeira versão operacional do sistema e privilegia simplicidade e facilidade de manutenção.

---

# 2. Princípios Orientadores

A implementação da segurança deverá respeitar os seguintes princípios:

## Simplicidade

A solução deve resolver os requisitos atuais sem introduzir complexidade desnecessária.

## Evolução Progressiva

A arquitetura deverá permitir evoluir futuramente para:

- Refresh Tokens;
- Permissões por módulo;
- Multiempresa;
- Single Sign-On (SSO);
- Integração com Identity Providers externos.

## Segurança por Defeito

Todos os endpoints da aplicação deverão ser considerados protegidos por defeito.

Apenas os endpoints explicitamente definidos como públicos poderão ser acedidos sem autenticação.

## Separação de Responsabilidades

A lógica de autenticação e autorização não deverá estar misturada com a lógica funcional da aplicação.

---

# 3. Tecnologia Adotada

## Backend

- Spring Boot 3.x
- Spring Security 6
- JWT (JSON Web Token)

## Base de Dados

- PostgreSQL

## Frontend

- React

## Modelo de Sessão

A aplicação deverá funcionar em modo:

```text
STATELESS
```

Não deverão existir sessões HTTP armazenadas no servidor.

Toda a autenticação será efetuada através de tokens JWT.

---

# 4. Modelo de Autenticação

## Processo de Login

O utilizador envia:

```json
{
  "username": "utilizador",
  "password": "password"
}
```

para:

```http
POST /api/auth/login
```

Após validação das credenciais, o sistema devolve:

```json
{
  "token": "jwt-token",
  "type": "Bearer",
  "expiresIn": 3600
}
```

O frontend deverá armazenar o token e enviá-lo em todos os pedidos subsequentes.

---

## Header de Autenticação

Todos os pedidos autenticados deverão utilizar:

```http
Authorization: Bearer <token>
```

---

# 5. JWT

## Objetivo

O JWT será utilizado para:

- Identificar o utilizador autenticado;
- Transportar informação mínima necessária;
- Evitar utilização de sessões no servidor.

---

## Informação Contida no Token

O token deverá incluir:

- Identificador do utilizador;
- Username ou email;
- Perfil (Role);
- Data de emissão;
- Data de expiração.

---

## Configuração

As configurações deverão ser externas ao código.

Exemplo:

```properties
fac.security.jwt.secret=
fac.security.jwt.expiration-minutes=60
```

---

## Regras

A chave secreta:

- Nunca deverá estar hardcoded;
- Nunca deverá ser publicada em repositórios Git.

---

# 6. Utilizadores

## Entidade

Deverá existir uma entidade própria para autenticação.

Sugestão:

```java
AppUser
```

---

## Campos Mínimos

```java
id
name
username
email
password
active
role
```

---

## Palavra-passe

As passwords deverão ser armazenadas exclusivamente sob forma encriptada.

Algoritmo:

```text
BCrypt
```

Não é permitido:

- Texto simples;
- Encriptação reversível;
- Hashes obsoletos.

---

# 7. Perfis de Utilizador

## Role ADMIN

Permissões:

- Acesso total ao sistema;
- Configuração;
- Administração;
- Gestão de utilizadores.

---

## Role USER

Permissões:

- Utilização normal da aplicação;
- Operações de negócio.

---

## Evolução Futura

A estrutura deverá permitir evoluir para permissões por módulo:

- Clientes;
- Fornecedores;
- Artigos;
- Documentos;
- Tesouraria;
- Configurações;
- SAF-T;
- Séries;
- Utilizadores.

---

# 8. Endpoints Públicos

Os seguintes endpoints deverão permanecer públicos:

```http
POST /api/auth/login
```

Opcionalmente:

```http
POST /api/auth/register
```

apenas em ambiente de desenvolvimento.

---

## Actuator

Caso exista monitorização:

```http
/actuator/health
```

poderá permanecer público.

---

# 9. Endpoints Protegidos

Todos os restantes endpoints deverão exigir autenticação válida.

Exemplos:

```http
/api/clientes/**
/api/fornecedores/**
/api/artigos/**
/api/documentos/**
/api/configuracoes/**
```

---

# 10. Filtro JWT

Deverá existir um filtro dedicado.

Responsabilidades:

- Ler o header Authorization;
- Validar o token;
- Extrair o utilizador;
- Carregar os dados do utilizador;
- Criar contexto de autenticação.

---

## Nome Sugerido

```java
JwtAuthenticationFilter
```

---

# 11. Serviço JWT

Deverá existir um serviço próprio responsável por:

- Gerar tokens;
- Validar tokens;
- Extrair claims;
- Extrair username;
- Validar expiração.

---

## Nome Sugerido

```java
JwtService
```

---

# 12. Serviço de Autenticação

Responsável por:

- Login;
- Validação de credenciais;
- Criação de tokens;
- Registo de utilizadores (se aplicável).

---

## Nome Sugerido

```java
AuthService
```

---

# 13. Configuração Spring Security

Deverá existir uma configuração dedicada.

## Requisitos

### CSRF

Desativado.

Motivo:

```text
API REST Stateless
```

---

### Sessões

```java
SessionCreationPolicy.STATELESS
```

---

### Password Encoder

```java
BCryptPasswordEncoder
```

---

### Authentication Manager

Configuração explícita.

---

### JWT Filter

Inserção na cadeia de filtros do Spring Security.

---

# 14. DTOs

Devem existir DTOs específicos para autenticação.

## LoginRequest

```java
username
password
```

---

## LoginResponse

```java
token
type
expiresIn
```

---

## RegisterRequest

Opcional.

Utilização exclusiva em desenvolvimento.

---

## Regras

Nunca devolver:

```java
password
```

em qualquer DTO.

---

# 15. Utilizador Inicial

Para facilitar o desenvolvimento deverá existir um utilizador inicial.

Exemplo:

```text
Username: admin
Email: admin@fac.local
Password: admin123
Role: ADMIN
```

---

## Regras

A criação automática deverá ser utilizada apenas em ambiente de desenvolvimento.

Nunca deverá ser utilizada em produção.

---

# 16. Estrutura de Packages

Sugestão de organização:

```text
pt.finpart.fac.auth

pt.finpart.fac.security

pt.finpart.fac.security.jwt

pt.finpart.fac.user

pt.finpart.fac.config
```

---

# 17. Critérios de Aceitação

A implementação será considerada concluída quando:

- O login funcionar corretamente;
- O JWT for gerado com sucesso;
- O JWT permitir autenticação nos endpoints protegidos;
- Os endpoints protegidos recusarem acessos sem token;
- As passwords forem armazenadas em BCrypt;
- O sistema devolver HTTP 401 quando não autenticado;
- O sistema devolver HTTP 403 quando sem permissões suficientes;
- A aplicação compilar sem erros;
- Não existirem alterações disruptivas na arquitetura atual.

---

# 18. Evoluções Previstas

A presente implementação constitui apenas a primeira camada de segurança.

Evoluções futuras previstas:

- Refresh Tokens;
- Revogação de tokens;
- Auditoria de acessos;
- Histórico de logins;
- Permissões por módulo;
- Permissões por ação;
- Multiempresa;
- Single Sign-On;
- MFA (Multi-Factor Authentication).

---

# 19. Decisão Arquitetural

Para a primeira versão do FAC fica adotada a seguinte estratégia:

```text
Spring Security
+
JWT
+
BCrypt
+
Roles (ADMIN / USER)
+
Arquitetura Stateless
```

Esta solução oferece um equilíbrio adequado entre simplicidade, segurança, facilidade de implementação e capacidade de evolução futura.