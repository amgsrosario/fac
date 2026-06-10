# FAC Workspace UI

Prova de conceito visual para o frontend React do FAC.

O objetivo desta pasta e validar uma interface calma, profissional e focada para uma aplicacao de faturacao simples. Nao e um ERP, nao e um SaaS generico e nao substitui ainda o pseudo frontend em `src/main/resources/static`.

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

## Estado da POC

Esta versao ainda usa dados visuais estaticos. Serve para fechar linguagem visual, ritmo de ecras e hierarquia de informacao antes de ligar formularios e fluxos reais.
