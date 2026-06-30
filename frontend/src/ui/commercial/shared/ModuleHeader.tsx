import { ReactNode } from "react";

export function ModuleHeader({
  action,
  children,
  className = "",
  compact = false,
  eyebrow,
  summary,
  subtitle,
  title
}: {
  action?: ReactNode;
  children?: ReactNode;
  className?: string;
  compact?: boolean;
  eyebrow?: string;
  summary?: ReactNode;
  subtitle?: ReactNode;
  title: string;
}) {
  return (
    <header className={`fac-module-header ${compact ? "fac-module-header-compact" : ""} ${className}`.trim()}>
      <div>
        {eyebrow && <p className="fac-eyebrow">{eyebrow}</p>}
        <h1>{title}</h1>
        {!compact && subtitle && <p className="fac-muted">{subtitle}</p>}
        {children}
      </div>
      <div className="fac-module-header-actions">
        {summary}
        {action}
      </div>
    </header>
  );
}
