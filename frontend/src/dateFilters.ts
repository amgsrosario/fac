export type DateRange = { dataInicial: string; dataFinal: string };

export function currentYearDateRange(now = new Date()): DateRange {
  const year = now.getFullYear();
  return {
    dataInicial: `${year}-01-01`,
    dataFinal: localDateIso(now)
  };
}

function localDateIso(value: Date) {
  const year = value.getFullYear();
  const month = String(value.getMonth() + 1).padStart(2, "0");
  const day = String(value.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}
