import { ReactNode } from "react";

export function DesktopShell({
  children,
  sidebar
}: {
  children: ReactNode;
  sidebar?: ReactNode;
}) {
  return (
    <main className="fac-ui fac-commercial-shell">
      <aside className="fac-commercial-sidebar">
        {sidebar ?? <strong>FAC</strong>}
      </aside>
      <section className="fac-commercial-content">
        <div className="fac-commercial-content-inner">{children}</div>
      </section>
    </main>
  );
}
