import { ReactNode } from "react";

export type FacStatusTone = "neutral" | "success" | "warning" | "danger" | "info";

export function FacStatusBadge({
  children,
  className = "",
  tone = "neutral"
}: {
  children: ReactNode;
  className?: string;
  tone?: FacStatusTone;
}) {
  const toneClass = tone === "neutral" ? "" : `fac-status-badge-${tone}`;
  return <span className={`fac-status-badge ${toneClass} ${className}`.trim()}>{children}</span>;
}
