-- Preserva o significado historico do codigo legado antes de alinhar o catalogo
-- com a fonte oficial da Autoridade Tributaria.
insert into public.freguesia (
    codigo,
    codigo_distrito,
    codigo_concelho,
    codigo_freguesia,
    concelho,
    nome,
    extinta
)
values ('010121', '01', '01', '21', 'AGUEDA', 'AGUEDA E BORRALHA', false)
on conflict (codigo) do update set
    codigo_distrito = excluded.codigo_distrito,
    codigo_concelho = excluded.codigo_concelho,
    codigo_freguesia = excluded.codigo_freguesia,
    concelho = excluded.concelho,
    nome = excluded.nome,
    extinta = excluded.extinta;

update public.armazem
set id_freguesia = '010121'
where id_freguesia = '010103';

update public.empresa
set id_freguesia = '010121'
where id_freguesia = '010103';

update public.freguesia
set codigo_distrito = '01',
    codigo_concelho = '01',
    codigo_freguesia = '03',
    concelho = 'AGUEDA',
    nome = 'AGUADA DE CIMA',
    extinta = false
where codigo = '010103';
