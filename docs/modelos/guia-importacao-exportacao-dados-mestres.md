# Guia curto — importacao/exportacao de dados mestres

1. Abrir `Importar/Exportar`.
2. Escolher `Clientes` ou `Artigos`.
3. Descarregar o modelo em CSV ou XLSX.
4. Preencher sem alterar os cabeçalhos.
5. Enviar para validacao.
6. Corrigir erros, se existirem.
7. Confirmar apenas quando o resumo estiver correto.
8. Usar exportacao para obter todos os registos ou apenas ativos.

Notas:

- linhas com erro nao sao importadas;
- duplicados nao sao atualizados automaticamente;
- formulas em XLSX sao rejeitadas;
- valores exportados que comecem por `=`, `+`, `-` ou `@` sao neutralizados.
