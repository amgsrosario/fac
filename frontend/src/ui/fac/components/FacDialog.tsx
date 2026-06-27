import { Dialog, DialogProps } from "primereact/dialog";

export type FacDialogProps = DialogProps;

export function FacDialog({ className = "", maskClassName = "", ...props }: FacDialogProps) {
  return (
    <Dialog
      className={`fac-dialog ${className}`.trim()}
      maskClassName={`fac-dialog-mask ${maskClassName}`.trim()}
      {...props}
    />
  );
}
