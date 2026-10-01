-- EntityLookup uses Intl.Collator("pt-PT", { numeric: true }).
-- A dedicated collation preserves this order across remote pages, without changing main list endpoints.
CREATE COLLATION lookup_pt_natural (provider = icu, locale = 'pt-u-kn-true');
