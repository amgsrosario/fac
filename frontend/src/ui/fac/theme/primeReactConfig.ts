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
  reject: "Não",
  choose: "Escolher",
  upload: "Enviar",
  cancel: "Cancelar",
  dayNames: ["domingo", "segunda-feira", "terça-feira", "quarta-feira", "quinta-feira", "sexta-feira", "sábado"],
  dayNamesShort: ["dom", "seg", "ter", "qua", "qui", "sex", "sab"],
  dayNamesMin: ["D", "S", "T", "Q", "Q", "S", "S"],
  monthNames: ["janeiro", "fevereiro", "março", "abril", "maio", "junho", "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"],
  monthNamesShort: ["jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez"],
  today: "Hoje",
  clear: "Limpar",
  aria: {
    close: "Fechar",
    firstPageLabel: "Primeira página",
    prevPageLabel: "Página anterior",
    nextPageLabel: "Página seguinte",
    lastPageLabel: "Última página",
    jumpToPageDropdownLabel: "Selecionar página",
    rowsPerPageLabel: "Linhas por página",
    pageLabel: "Página {page}"
  }
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
