-- Catálogo de referência AT: Tabela dos Códigos de Motivo de Isenção,
-- versão V4.0, de 18-06-2026, consultada em 07-10-2026.
-- A presença no catálogo não autoriza a aplicação automática de um código:
-- o motor valida separadamente o enquadramento e as combinações suportadas.
-- Preservar os nomes e classificações já existentes, incluindo M07.
INSERT INTO public.ivasaft (id, nome) VALUES ('ISE', 'Isento')
ON CONFLICT (id) DO NOTHING;

WITH motivos (id, descricao, fundamento) AS (
    VALUES
    ('M01', 'Artigo 16.º n.º 6 do CIVA', 'Artigo 16.º n.º 6 alíneas a) a d) do CIVA'),
    ('M02', 'Artigo 6.º do Decreto-Lei n.º 198/90, de 19 de junho', 'Artigo 6.º do Decreto-Lei n.º 198/90, de 19 de junho'),
    ('M04', 'Isento artigo 13.º do CIVA', 'Artigo 13.º do CIVA'),
    ('M05', 'Isento artigo 14.º do CIVA', 'Artigo 14.º do CIVA'),
    ('M06', 'Isento artigo 15.º do CIVA', 'Artigo 15.º do CIVA'),
    ('M07', 'Isento artigo 9.º do CIVA', 'Artigo 9.º do CIVA'),
    ('M09', 'IVA – não confere direito a dedução', 'Artigo 62.º alínea b) do CIVA'),
    ('M10', 'IVA – regime de isenção', 'Artigo 53.º n.º 1 do CIVA'),
    ('M11', 'Regime particular do tabaco', 'Decreto-Lei n.º 346/85, de 23 de agosto'),
    ('M12', 'Regime da margem de lucro – Agências de viagens', 'Decreto-Lei n.º 221/85, de 3 de julho'),
    ('M13', 'Regime da margem de lucro – Bens em segunda mão', 'Decreto-Lei n.º 199/96, de 18 de outubro'),
    ('M14', 'Regime da margem de lucro – Objetos de arte', 'Decreto-Lei n.º 199/96, de 18 de outubro'),
    ('M15', 'Regime da margem de lucro – Objetos de coleção e antiguidades', 'Decreto-Lei n.º 199/96, de 18 de outubro'),
    ('M16', 'Isento artigo 14.º do RITI', 'Artigo 14.º do RITI'),
    ('M19', 'Outras isenções', 'Isenções temporárias determinadas em diploma próprio'),
    ('M20', 'IVA – regime forfetário', 'Artigo 59.º-D n.º 2 do CIVA'),
    ('M21', 'IVA – não confere direito à dedução (ou expressão similar)', 'Artigo 72.º n.º 4 do CIVA'),
    ('M25', 'Mercadorias à consignação', 'Artigo 38.º n.º 1 alínea a) do CIVA'),
    ('M26', 'Isenção de IVA com direito à dedução no cabaz alimentar', 'Lei n.º 17/2023, de 14 de abril'),
    ('M30', 'IVA - autoliquidação', 'Artigo 2.º n.º 1 alínea i) do CIVA'),
    ('M31', 'IVA - autoliquidação', 'Artigo 2.º n.º 1 alínea j) do CIVA; exclui as operações abrangidas pelo código M35'),
    ('M32', 'IVA - autoliquidação', 'Artigo 2.º n.º 1 alínea l) do CIVA'),
    ('M33', 'IVA - autoliquidação', 'Artigo 2.º n.º 1 alínea m) do CIVA'),
    ('M34', 'IVA - autoliquidação', 'Artigo 2.º n.º 1 alínea n) do CIVA'),
    ('M35', 'IVA - autoliquidação', 'Artigo 2.º n.º 1 alínea j) do CIVA – Verba 2.42 da Lista I; condições do Decreto-Lei n.º 97/2026, de 20 de maio; faturas emitidas após 1 de julho de 2026'),
    ('M40', 'IVA - autoliquidação', 'Artigo 6.º n.º 6 alínea a) do CIVA, a contrário'),
    ('M41', 'IVA - autoliquidação', 'Artigo 8.º n.º 3 do RITI'),
    ('M42', 'IVA - autoliquidação', 'Decreto-Lei n.º 21/2007, de 29 de janeiro'),
    ('M43', 'IVA - autoliquidação', 'Decreto-Lei n.º 362/99, de 16 de setembro'),
    ('M44', 'IVA – Regras específicas - artigo 6.º', 'Artigo 6.º do CIVA – Regras específicas; operações não localizadas em Portugal pelas regras de exceção dos números 7 e seguintes'),
    ('M45', 'IVA – regime transfronteiriço de isenção', 'Artigo 58.º-A do CIVA; operações localizadas noutro Estado Membro abrangidas pela adesão ao Regime Transfronteiriço de Isenção; Ofício-Circulado n.º 25065, de 08.04.2025'),
    ('M46', 'IVA – e-TaxFree', 'Decreto-Lei n.º 19/2017, de 14 de fevereiro; bens transportados na bagagem pessoal de viajantes sem domicílio ou estabelecimento na União Europeia'),
    ('M99', 'Não sujeito ou não tributado', 'Outras situações de não liquidação do imposto; exemplos: artigo 2.º n.º 2; artigo 3.º n.ºs 4, 6 e 7; artigo 4.º n.º 5, todos do CIVA')
)
INSERT INTO public.misencao
    (id, nome, id_ivasaft, descricao_oficial, fundamento_oficial, fonte_oficial, versao_oficial)
SELECT id, left(descricao, 60), 'ISE', descricao, fundamento,
    'https://info.portaldasfinancas.gov.pt/pt/apoio_ao_contribuinte/Negocios/Faturacao/Regras_mecanismos_comunicacao/e_Fatura/e_Fatura_Comunicacao_elementos_docs_faturacao_2022_seguintes/Documents/Tabela_Codigos_Motivo_Isencao.pdf',
    'V4.0 / 18-06-2026'
FROM motivos
ON CONFLICT (id) DO UPDATE SET
    descricao_oficial = EXCLUDED.descricao_oficial,
    fundamento_oficial = EXCLUDED.fundamento_oficial,
    fonte_oficial = EXCLUDED.fonte_oficial,
    versao_oficial = EXCLUDED.versao_oficial;
