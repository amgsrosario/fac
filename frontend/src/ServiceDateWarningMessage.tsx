import { serviceDateWarning, serviceDateWarningText } from "./serviceDateWarning";

type DocumentDates = { dataPrestacao?: string | null; dataEmissao: string };

export function ServiceDateWarningMessage({ dataPrestacao, dataEmissao }: DocumentDates) {
  const warning = serviceDateWarning(dataPrestacao, dataEmissao);
  return warning && <p role="status">{serviceDateWarningText[warning]}</p>;
}

export function serviceDateConfirmation(fresh: DocumentDates, displayed: DocumentDates): string {
  const warning = serviceDateWarning(fresh.dataPrestacao, fresh.dataEmissao);
  if (!warning) return "";
  const differs = fresh.dataEmissao !== displayed.dataEmissao
    || (fresh.dataPrestacao ?? "") !== (displayed.dataPrestacao ?? "");
  const dates = differs ? `Emissão: ${fresh.dataEmissao}. Prestação: ${fresh.dataPrestacao}.\n` : "";
  return dates + serviceDateWarningText[warning] + "\nA data de prestação não pode ser alterada depois de emitida.\n";
}
