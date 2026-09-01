const PT_LOCALE = "pt-PT";

export function money(value: number) {
  const [whole, fraction] = Number(value || 0)
    .toLocaleString(PT_LOCALE, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    .split(",");

  return `${whole.replace(/\B(?=(\d{3})+(?!\d))/g, " ")},${fraction}`;
}

export function integer(value: number) {
  return Number(value || 0).toLocaleString(PT_LOCALE, { maximumFractionDigits: 0 });
}

export function decimal(value: number) {
  return Number(value || 0).toLocaleString(PT_LOCALE, { maximumFractionDigits: 6 });
}
