import { AuthSession } from "../../../api";
import { FacButton } from "../../fac";

export type CommercialNavItem = {
  description: string;
  href: string;
  id: string;
  label: string;
};

export const defaultCommercialNavItems: CommercialNavItem[] = [
  { description: "Catalogo", href: "/artigos", id: "articles", label: "Artigos" },
  { description: "Documentos", href: "/clientes", id: "customers", label: "Clientes" },
  { description: "Fundacao", href: "/ui-lab", id: "ui-lab", label: "UI Lab" }
];

export function CommercialSidebar({
  active,
  currentUser,
  items = defaultCommercialNavItems,
  onLogout
}: {
  active: string;
  currentUser: AuthSession;
  items?: CommercialNavItem[];
  onLogout: () => void;
}) {
  return (
    <div className="fac-commercial-nav">
      <div className="fac-commercial-brand">
        <span>FAC</span>
        <div>
          <strong>FAC</strong>
          <small>Comercial</small>
        </div>
      </div>
      <nav aria-label="Navegacao comercial">
        {items.map((item) => (
          <a className={active === item.id ? "active" : ""} href={item.href} key={item.id}>
            <strong>{item.label}</strong>
            <small>{item.description}</small>
          </a>
        ))}
      </nav>
      <div className="fac-commercial-user">
        <span>{currentUser.nome}</span>
        <small>{currentUser.papel} - {currentUser.codigo}</small>
        <FacButton label="Sair" onClick={onLogout} variant="text" />
      </div>
    </div>
  );
}
