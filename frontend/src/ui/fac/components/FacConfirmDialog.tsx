import { ConfirmDialog, confirmDialog } from "primereact/confirmdialog";

export type FacConfirmOptions = {
  accept: () => void;
  acceptLabel?: string;
  header: string;
  message: string;
  reject?: () => void;
  rejectLabel?: string;
};

export function FacConfirmDialogHost() {
  return <ConfirmDialog className="fac-dialog" maskClassName="fac-dialog-mask" />;
}

export function confirmFacDialog(options: FacConfirmOptions) {
  confirmDialog({
    accept: options.accept,
    acceptLabel: options.acceptLabel ?? "Confirmar",
    header: options.header,
    message: options.message,
    reject: options.reject,
    rejectLabel: options.rejectLabel ?? "Cancelar"
  });
}
