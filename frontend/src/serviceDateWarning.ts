type ServiceDateWarning = "after-issue" | "late-issue";

export const serviceDateWarningText: Record<ServiceDateWarning, string> = {
  "after-issue": "A data de prestação é posterior à data de emissão. Confirme que a factura é emitida antes da prestação (CIVA, art. 8.º, n.º 1, al. c)).",
  "late-issue": "A data de emissão excede o 5.º dia útil após a data de prestação (CIVA, art. 36.º, n.º 1, al. b))."
    + " Confirme as datas."
};

const DAY = 86_400_000;
const fixedHolidays = new Set(["01-01", "04-25", "05-01", "06-10", "08-15", "10-05", "11-01", "12-01", "12-08", "12-25"]);

function easterSunday(year: number): number {
  // Gregorian computus (Meeus/Jones/Butcher).
  const golden = year % 19;
  const century = Math.floor(year / 100);
  const yearInCentury = year % 100;
  const solarCorrection = Math.floor(century / 4);
  const lunarCorrection = Math.floor((century - Math.floor((century + 8) / 25) + 1) / 3);
  const epact = (19 * golden + century - solarCorrection - lunarCorrection + 15) % 30;
  const weekdayCorrection = (32 + 2 * (century % 4) + 2 * Math.floor(yearInCentury / 4)
    - epact - yearInCentury % 4) % 7;
  const exception = Math.floor((golden + 11 * epact + 22 * weekdayCorrection) / 451);
  const result = epact + weekdayCorrection - 7 * exception + 114;
  const date = new Date(0);
  date.setUTCFullYear(year, Math.floor(result / 31) - 1, result % 31 + 1);
  return date.getTime();
}

function isBusinessDay(instant: number): boolean {
  const date = new Date(instant);
  const weekday = date.getUTCDay();
  if (weekday === 0 || weekday === 6 || fixedHolidays.has(date.toISOString().slice(5, 10))) return false;
  const easter = easterSunday(date.getUTCFullYear());
  return instant !== easter - 2 * DAY && instant !== easter && instant !== easter + 60 * DAY;
}

function civilDate(value: string | null | undefined): number {
  if (!value || !/^\d{4}-\d{2}-\d{2}$/.test(value)) return NaN;
  const date = new Date(value);
  return Number.isFinite(date.getTime()) && date.toISOString().slice(0, 10) === value ? date.getTime() : NaN;
}

export function serviceDateWarning(service: string | null | undefined, issue: string): ServiceDateWarning | null {
  const serviceDate = civilDate(service);
  const issueDate = civilDate(issue);
  if (!Number.isFinite(serviceDate) || !Number.isFinite(issueDate) || serviceDate === issueDate) return null;
  if (serviceDate > issueDate) return "after-issue";
  let businessDays = 0;
  for (let date = serviceDate + DAY; date < issueDate; date += DAY) {
    if (isBusinessDay(date) && ++businessDays === 5) return "late-issue";
  }
  return null;
}
