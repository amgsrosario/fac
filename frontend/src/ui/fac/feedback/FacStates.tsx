import { ReactNode } from "react";

function FacState({
  children,
  description,
  title
}: {
  children?: ReactNode;
  description?: string;
  title: string;
}) {
  return (
    <section className="fac-state">
      <strong>{title}</strong>
      {description && <p>{description}</p>}
      {children}
    </section>
  );
}

export function FacLoadingState({ description = "A carregar dados." }: { description?: string }) {
  return <FacState description={description} title="A carregar" />;
}

export function FacErrorState({ description = "Nao foi possivel concluir a operacao." }: { description?: string }) {
  return <FacState description={description} title="Erro" />;
}

export function FacEmptyState({ description = "Nao existem dados para apresentar." }: { description?: string }) {
  return <FacState description={description} title="Sem dados" />;
}
