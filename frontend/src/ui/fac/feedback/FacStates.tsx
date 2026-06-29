import { ReactNode } from "react";

export type FacMessageTone = "information" | "success" | "warning" | "error";

function FacState({
  children,
  description,
  title,
  tone = "neutral"
}: {
  children?: ReactNode;
  description?: string;
  title: string;
  tone?: "neutral" | "loading" | "error";
}) {
  return (
    <section className="fac-state" data-tone={tone}>
      <strong>{title}</strong>
      {description && <p>{description}</p>}
      {children}
    </section>
  );
}

export function FacLoadingState({ description = "A carregar dados." }: { description?: string }) {
  return <FacState description={description} title="A carregar" tone="loading" />;
}

export function FacErrorState({ description = "Nao foi possivel concluir a operacao." }: { description?: string }) {
  return <FacState description={description} title="Erro" tone="error" />;
}

export function FacEmptyState({ description = "Nao existem dados para apresentar." }: { description?: string }) {
  return <FacState description={description} title="Sem dados" />;
}

export function FacMessage({
  children,
  title,
  tone = "information"
}: {
  children: ReactNode;
  title: string;
  tone?: FacMessageTone;
}) {
  const className = tone === "information" ? "fac-ui-message" : `fac-ui-message fac-ui-message-${tone}`;
  const role = tone === "error" ? "alert" : "status";

  return (
    <section className={className} role={role}>
      <strong>{title}</strong>
      <span>{children}</span>
    </section>
  );
}
