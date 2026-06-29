import { ReactNode } from "react";

export function MobileShell({
  children,
  title = "FAC"
}: {
  children: ReactNode;
  title?: string;
}) {
  return (
    <main className="fac-ui fac-mobile-shell">
      <header className="fac-mobile-bar"><strong>{title}</strong></header>
      <section className="fac-mobile-content">{children}</section>
    </main>
  );
}
