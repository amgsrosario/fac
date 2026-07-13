import { AuthSession } from "../../../api";
import { FacButton } from "../../fac";

export type CommercialNavItem = {
  description: string;
  group: string;
  href: string;
  id: string;
  label: string;
};

export const defaultCommercialNavItems: CommercialNavItem[] = [
  { description: "Faturacao", group: "Vendas", href: "/documentos", id: "documents", label: "Documentos" },
  { description: "Entidades", group: "Dados comerciais", href: "/clientes", id: "customers", label: "Clientes" },
  { description: "Catalogo", group: "Dados comerciais", href: "/artigos", id: "articles", label: "Artigos" }
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
  const groupedItems = items.reduce<Array<{ title: string; items: CommercialNavItem[] }>>((groups, item) => {
    const group = groups.find((current) => current.title === item.group);
    if (group) {
      group.items.push(item);
    } else {
      groups.push({ title: item.group, items: [item] });
    }
    return groups;
  }, []);

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
        {groupedItems.map((group) => (
          <section className="fac-commercial-nav-section" key={group.title}>
            <p>{group.title}</p>
            {group.items.map((item) => (
              <a className={active === item.id ? "active" : ""} href={item.href} key={item.id}>
                <strong>{item.label}</strong>
                <small>{item.description}</small>
              </a>
            ))}
          </section>
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
