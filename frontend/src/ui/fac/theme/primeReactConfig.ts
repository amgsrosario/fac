import { addLocale, PrimeReactContext } from "primereact/api";

type FacPrimeReactConfig = {
  appendTo: "self";
  cssTransition: boolean;
  hideOverlaysOnDocumentScrolling: boolean;
  inputStyle: "outlined" | "filled";
  locale: string;
  ripple: boolean;
  unstyled: boolean;
};

addLocale("pt-PT", {
  accept: "Sim",
  reject: "Nao",
  choose: "Escolher",
  upload: "Enviar",
  cancel: "Cancelar",
  dayNames: ["domingo", "segunda-feira", "terca-feira", "quarta-feira", "quinta-feira", "sexta-feira", "sabado"],
  dayNamesShort: ["dom", "seg", "ter", "qua", "qui", "sex", "sab"],
  dayNamesMin: ["D", "S", "T", "Q", "Q", "S", "S"],
  monthNames: ["janeiro", "fevereiro", "marco", "abril", "maio", "junho", "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"],
  monthNamesShort: ["jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez"],
  today: "Hoje",
  clear: "Limpar"
});

export const facPrimeReactConfig: FacPrimeReactConfig = {
  appendTo: "self",
  cssTransition: true,
  hideOverlaysOnDocumentScrolling: false,
  inputStyle: "outlined",
  locale: "pt-PT",
  ripple: false,
  unstyled: true
};

export { PrimeReactContext };
