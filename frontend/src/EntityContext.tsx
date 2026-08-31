import { ReactNode, RefObject, useEffect, useRef } from "react";

type EntityDetailOverlayProps = {
  children: ReactNode;
  labelledBy: string;
  onClose: () => void;
  open: boolean;
  returnFocusRef: RefObject<HTMLButtonElement | null>;
};

export function EntityDetailOverlay({ children, labelledBy, onClose, open, returnFocusRef }: EntityDetailOverlayProps) {
  const closeButtonRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (!open) return;
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    closeButtonRef.current?.focus();

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") onClose();
    }

    document.addEventListener("keydown", handleKeyDown);
    return () => {
      document.removeEventListener("keydown", handleKeyDown);
      document.body.style.overflow = previousOverflow;
    };
  }, [onClose, open]);

  if (!open) return null;

  function closeAndRestoreFocus() {
    onClose();
    window.requestAnimationFrame(() => returnFocusRef.current?.focus());
  }

  return (
    <div className="fac-entity-overlay" onMouseDown={(event) => { if (event.target === event.currentTarget) closeAndRestoreFocus(); }}>
      <section aria-labelledby={labelledBy} aria-modal="true" className="fac-entity-detail" role="dialog">
        <button aria-label="Fechar detalhe" className="fac-entity-detail-close" onClick={closeAndRestoreFocus} ref={closeButtonRef} type="button">
          <span aria-hidden="true">×</span>
        </button>
        {children}
      </section>
    </div>
  );
}
