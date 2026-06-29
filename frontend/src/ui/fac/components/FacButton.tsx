import { Button, ButtonProps } from "primereact/button";

export type FacButtonVariant = "primary" | "secondary" | "ghost" | "destructive" | "text";

export type FacButtonProps = Omit<ButtonProps, "severity" | "text" | "outlined"> & {
  variant?: FacButtonVariant;
};

export function FacButton({ className = "", variant = "secondary", ...props }: FacButtonProps) {
  return <Button className={`fac-button fac-button-${variant} ${className}`.trim()} {...props} />;
}
