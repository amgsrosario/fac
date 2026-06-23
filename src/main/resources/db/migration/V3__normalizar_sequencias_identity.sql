-- Bases antigas foram criadas e alimentadas manualmente; algumas sequências podem
-- estar atrás do maior identificador persistido. A normalização não altera linhas.
SELECT setval(pg_get_serial_sequence('armazem', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM armazem;
SELECT setval(pg_get_serial_sequence('cliente', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM cliente;
SELECT setval(pg_get_serial_sequence('documento_comercial', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM documento_comercial;
SELECT setval(pg_get_serial_sequence('documento_financeiro', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM documento_financeiro;
SELECT setval(pg_get_serial_sequence('familia', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM familia;
SELECT setval(pg_get_serial_sequence('linha_documento_comercial', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM linha_documento_comercial;
SELECT setval(pg_get_serial_sequence('linha_documento_financeiro', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM linha_documento_financeiro;
SELECT setval(pg_get_serial_sequence('morada', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM morada;
SELECT setval(pg_get_serial_sequence('mpagamento', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM mpagamento;
SELECT setval(pg_get_serial_sequence('pendente', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM pendente;
SELECT setval(pg_get_serial_sequence('riva_taxa', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM riva_taxa;
SELECT setval(pg_get_serial_sequence('transporte', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM transporte;
