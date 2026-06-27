import { createContext, ReactNode, useContext, useMemo, useRef } from "react";
import { Toast } from "primereact/toast";
import type { ToastMessage } from "primereact/toast";

type FacToastContextValue = {
  showToast: (message: ToastMessage) => void;
};

const FacToastContext = createContext<FacToastContextValue | null>(null);

export function FacToastProvider({ children }: { children: ReactNode }) {
  const toastRef = useRef<Toast>(null);
  const value = useMemo<FacToastContextValue>(() => ({
    showToast: (message) => toastRef.current?.show(message)
  }), []);

  return (
    <FacToastContext.Provider value={value}>
      <Toast className="fac-toast" ref={toastRef} />
      {children}
    </FacToastContext.Provider>
  );
}

export function useFacToast() {
  const context = useContext(FacToastContext);
  if (!context) {
    throw new Error("useFacToast must be used inside FacToastProvider.");
  }
  return context;
}
