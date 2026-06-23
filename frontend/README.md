# FAC Workspace UI

Frontend React do FAC.

Esta pasta contem a interface principal, profissional e focada do FAC.

## Principios visuais

- branco quente e superficies suaves;
- baixo contraste;
- sem preto puro;
- sem azul dominante;
- sem sombras pesadas;
- menus discretos;
- pastels quase invisiveis para diferenciar contextos.

## Executar

```bash
cd frontend
npm install
npm run dev
```

O Vite arranca por defeito em:

```text
http://localhost:5173
```

O backend Spring Boot deve estar em:

```text
http://localhost:8080
```

Chamadas para `/api/...` sao encaminhadas para o backend via proxy do Vite.

## Estado da interface

A interface comunica com o backend e disponibiliza os fluxos funcionais implementados no FAC.
