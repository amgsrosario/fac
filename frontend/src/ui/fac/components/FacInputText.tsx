import { InputText, InputTextProps } from "primereact/inputtext";

export type FacInputTextProps = InputTextProps & {
  label?: string;
};

export function FacInputText({ className = "", label, ...props }: FacInputTextProps) {
  const input = <InputText className={`fac-input ${className}`.trim()} {...props} />;
  if (!label) return input;
  return <label className="fac-field-stack"><span>{label}</span>{input}</label>;
}
