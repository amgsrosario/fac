import { useState } from "react";
import { Route, Routes, Navigate } from "react-router-dom";
import {
  DesktopShell,
  FacButton,
  FacDialog,
  FacEmptyState,
  FacErrorState,
  FacInputText,
  FacLoadingState,
  FacSelect,
  FacStatusBadge,
  FacToastProvider,
  MobileShell,
  ResponsiveSlot,
  useDeviceClass,
  useFacToast
} from "../fac";

function UiFoundationLabContent() {
  const [dialogVisible, setDialogVisible] = useState(false);
  const [serviceType, setServiceType] = useState<string | null>("consultoria");
  const { deviceClass } = useDeviceClass();
  const { showToast } = useFacToast();

  const lab = (
    <>
      <p className="fac-eyebrow">FAC UI Lab</p>
      <h1>Fundacao comercial isolada</h1>
      <p className="fac-muted">Modo tecnico sem dados reais, endpoints ou migracao dos ecras existentes.</p>
      <div className="fac-lab-grid">
        <section className="fac-lab-panel">
          <h2>Comandos</h2>
          <div className="fac-inline-actions">
            <FacButton label="Primario" variant="primary" />
            <FacButton label="Secundario" variant="secondary" />
            <FacButton label="Destrutivo" variant="destructive" />
          </div>
          <div className="fac-inline-actions">
            <FacButton label="Abrir dialogo" onClick={() => setDialogVisible(true)} variant="secondary" />
            <FacButton
              label="Mostrar toast"
              onClick={() => showToast({ detail: "Mensagem FAC demonstrativa.", severity: "success", summary: "Sucesso" })}
              variant="primary"
            />
          </div>
        </section>

        <section className="fac-lab-panel">
          <h2>Formulario</h2>
          <FacInputText label="Nome" placeholder="Cliente de exemplo" />
          <FacSelect
            label="Tipo de servico"
            onChange={setServiceType}
            options={[
              { label: "Consultoria", value: "consultoria" },
              { label: "Manutencao", value: "manutencao" },
              { label: "Formacao", value: "formacao" }
            ]}
            value={serviceType}
          />
        </section>

        <section className="fac-lab-panel">
          <h2>Estados</h2>
          <div className="fac-inline-actions">
            <FacStatusBadge>Neutro</FacStatusBadge>
            <FacStatusBadge tone="success">Ativo</FacStatusBadge>
            <FacStatusBadge tone="warning">Pendente</FacStatusBadge>
            <FacStatusBadge tone="danger">Bloqueado</FacStatusBadge>
          </div>
          <FacLoadingState description="Estado de carregamento isolado." />
          <FacEmptyState description="Estado vazio isolado." />
          <FacErrorState description="Estado de erro isolado." />
        </section>

        <section className="fac-lab-panel">
          <h2>Dispositivo</h2>
          <p>Classe atual: <strong>{deviceClass}</strong></p>
          <ResponsiveSlot
            desktop={<FacStatusBadge tone="success">Desktop container</FacStatusBadge>}
            mobile={<FacStatusBadge tone="warning">Mobile container</FacStatusBadge>}
            tablet={<FacStatusBadge>Tablet container</FacStatusBadge>}
          />
        </section>
      </div>

      <FacDialog
        footer={<FacButton label="Fechar" onClick={() => setDialogVisible(false)} variant="primary" />}
        header="Dialogo FAC"
        onHide={() => setDialogVisible(false)}
        visible={dialogVisible}
      >
        <p>Dialogo PrimeReact encapsulado pela camada visual FAC.</p>
      </FacDialog>
    </>
  );

  return (
    <ResponsiveSlot
      desktop={<DesktopShell sidebar={<><strong>FAC</strong><p className="fac-muted">Commercial lab</p></>}>{lab}</DesktopShell>}
      mobile={<MobileShell title="FAC Lab">{lab}</MobileShell>}
      tablet={<DesktopShell sidebar={<><strong>FAC</strong><p className="fac-muted">Tablet lab</p></>}>{lab}</DesktopShell>}
    />
  );
}

export default function UiFoundationLab() {
  return (
    <FacToastProvider>
      <Routes>
        <Route element={<UiFoundationLabContent />} path="/ui-lab" />
        <Route element={<Navigate replace to="/ui-lab" />} path="*" />
      </Routes>
    </FacToastProvider>
  );
}
