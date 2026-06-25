# Roteiro completo da demonstracao comercial

Duração alvo: 15 a 20 minutos. Ambiente exclusivo: perfil `demo`, base `fac_demo`.

| Tempo | Ecra | Acao | Mensagem verbal | Resultado esperado | Risco | Contingencia |
|---|---|---|---|---|---|---|
| 0-2 min | Entrada/Dashboard | Abrir a aplicacao e autenticar | "A FAC mostra um circuito comercial completo para uma PME, com simplicidade operacional e controlo." | Ver `FAC Demo Partner Edition` e Alentejo Sabores | Login demora | Usar browser ja autenticado ou explicar com roteiro curto |
| 2-4 min | Navegacao | Mostrar perfis admin, operador e consulta | "Administrador configura e audita; operador trabalha; consulta acompanha sem alterar." | Menus mudam por permissao | Troca de utilizador lenta | Ficar em admin e explicar perfis pela auditoria |
| 4-5 min | Empresa | Abrir Configuracao > Empresa | "A entidade emitente tem dados fiscais, contacto, IBAN e logotipo refletidos no PDF." | Empresa completa e logotipo presente | Edicao acidental | Abrir apenas leitura, nao gravar |
| 5-7 min | Clientes/Artigos/Importar | Mostrar clientes, artigos e importacao/exportacao | "Os dados mestres podem ser carregados e exportados em CSV/XLSX, com validacao antes de gravar." | Listas coerentes e modelos disponiveis | Ficheiro local em falta | Mostrar exportacao e modelos incluidos |
| 7-9 min | Documentos | Abrir documentos emitidos | "A serie DEMO26 mostra numeracao, ATCUD, QR e snapshots fiscais." | FT DEMO26/1-6 visiveis | Pesquisa filtrada | Limpar pesquisa |
| 9-11 min | PDF | Abrir PDF multipagina | "O documento longo prova paginação, linhas e totais." | PDF com 35 linhas, QR e ATCUD | PDF abre lento | Usar PDF previamente aberto |
| 11-12 min | Documentos | Abrir documento anulado | "A anulação preserva motivo, autor e rastreabilidade." | Estado ANULADO e marca no PDF | Documento errado | Usar FT DEMO26/4 |
| 12-14 min | Tesouraria/Listagens | Mostrar pagamento e extrato | "A conta corrente liga fatura, recebimento e saldo." | 2 recebimentos e extrato com 3 movimentos | Filtro errado | Usar cliente Casa dos Sabores do Sul e ano 2026 |
| 14-16 min | Auditoria | Filtrar eventos | "Operações críticas e tentativas recusadas ficam auditadas." | Emissao, anulacao, recebimento, login, tentativa negada | Sem eventos recentes | Usar eventos do seed |
| 16-18 min | Continuidade | Mostrar comandos de backup/check | "A demo pode ser reposta, verificada e restaurada." | `FAC_DEMO_CHECK OK`, backup recente | Evitar terminal partilhado | Mostrar guia operacional sem passwords |
| 18-20 min | Encerramento | Resumir valor e limites | "Está pronta para demonstrar fluxo comercial; SAF-T e produção ficam como evolução." | Parceiro percebe escopo e proximos passos | Pergunta fiscal profunda | Distinguir demo funcional de certificacao |
