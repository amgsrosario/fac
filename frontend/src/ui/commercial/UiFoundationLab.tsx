import { useEffect, useMemo, useRef, useState } from "react";
import { Route, Routes, Navigate } from "react-router-dom";
import {
  DesktopShell,
  FacButton,
  FacDialog,
  FacEmptyState,
  FacErrorState,
  FacInputText,
  FacLoadingState,
  FacMessage,
  FacSelect,
  FacStatusBadge,
  FacToastProvider,
  MobileShell,
  ResponsiveSlot,
  useDeviceClass,
  useFacToast
} from "../fac";

function UiFoundationLabContent() {
  const demoMode = useMemo(() => new URLSearchParams(window.location.search).get("demo"), []);
  const [dialogVisible, setDialogVisible] = useState(false);
  const [confirmVisible, setConfirmVisible] = useState(false);
  const [serviceType, setServiceType] = useState<string | null>("consultoria");
  const demoAppliedRef = useRef(false);
  const { deviceClass } = useDeviceClass();
  const { showToast } = useFacToast();

  useEffect(() => {
    if (demoAppliedRef.current) return;
    demoAppliedRef.current = true;
    if (demoMode === "dialog") setDialogVisible(true);
    if (demoMode === "confirm") setConfirmVisible(true);
    if (demoMode === "toast") {
      window.setTimeout(() => showToast({ detail: "Mensagem FAC demonstrativa.", severity: "success", summary: "Sucesso" }), 350);
    }
  }, [demoMode, showToast]);

  const lab = (
    <>
      <header className="fac-lab-hero">
        <div>
          <div className="fac-lab-brand">
            <span className="fac-lab-brand-mark">FAC</span>
            <span>FAC Commercial Foundation</span>
          </div>
          <p className="fac-eyebrow">FAC UI Lab</p>
          <h1>Fundacao comercial isolada</h1>
          <p className="fac-muted">Modo tecnico sem dados reais, endpoints ou migracao dos ecras existentes.</p>
        </div>
        <FacStatusBadge tone="info">Modo {deviceClass}</FacStatusBadge>
      </header>

      <div className="fac-lab-grid">
        <section className="fac-lab-panel">
          <h2>Comandos</h2>
          <div className="fac-lab-actions">
            <FacButton icon="pi pi-check" label="Primario" variant="primary" />
            <FacButton label="Secundario" variant="secondary" />
            <FacButton label="Ghost" variant="ghost" />
            <FacButton label="Texto" variant="text" />
            <FacButton icon="pi pi-exclamation-triangle" label="Destrutivo" variant="destructive" />
          </div>
          <div className="fac-lab-actions">
            <FacButton label="Abrir dialogo" onClick={() => setDialogVisible(true)} variant="secondary" />
            <FacButton label="Confirmacao" onClick={() => setConfirmVisible(true)} variant="ghost" />
            <FacButton
              label="Mostrar toast"
              onClick={() => showToast({ detail: "Mensagem FAC demonstrativa.", severity: "success", summary: "Sucesso" })}
              variant="primary"
            />
          </div>
        </section>

        <section className="fac-lab-panel">
          <h2>Formulario</h2>
          <div className="fac-lab-form">
            <FacInputText label="Nome" placeholder="Cliente de exemplo" />
            <FacInputText disabled label="Campo desativado" value="Valor preservado" />
            <FacSelect
              label="Tipo de servico"
              onChange={setServiceType}
              openOnMount={demoMode === "select"}
              options={[
                { label: "Consultoria", value: "consultoria" },
                { label: "Manutencao", value: "manutencao" },
                { label: "Formacao", value: "formacao" }
              ]}
              value={serviceType}
            />
          </div>
        </section>

        <section className="fac-lab-panel">
          <h2>Mensagens e estados</h2>
          <div className="fac-lab-inline">
            <FacStatusBadge>Neutro</FacStatusBadge>
            <FacStatusBadge tone="success">Ativo</FacStatusBadge>
            <FacStatusBadge tone="warning">Pendente</FacStatusBadge>
            <FacStatusBadge tone="danger">Bloqueado</FacStatusBadge>
            <FacStatusBadge tone="info">Informativo</FacStatusBadge>
          </div>
          <div className="fac-lab-states">
            <FacMessage title="Informacao">A fundacao comercial usa tokens FAC e comportamento PrimeReact.</FacMessage>
            <FacMessage title="Sucesso" tone="success">Operacao demonstrativa concluida.</FacMessage>
            <FacMessage title="Aviso" tone="warning">Validacao visual ainda limitada ao laboratorio.</FacMessage>
            <FacMessage title="Erro" tone="error">Exemplo de mensagem de erro sem dados reais.</FacMessage>
            <FacLoadingState description="Estado de carregamento isolado." />
            <FacEmptyState description="Estado vazio isolado." />
            <FacErrorState description="Estado de erro isolado." />
          </div>
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

      <section className="fac-lab-panel">
        <p className="fac-eyebrow">Composicao</p>
        <h2>Desktop composition</h2>
        <div className="fac-composition-grid">
          <div>
            <p className="fac-muted">Desktop privilegia navegacao lateral, leitura horizontal, filtros e paineis densos.</p>
            <ul className="fac-composition-list" aria-label="Exemplo desktop">
              <li><span>Documentos abertos</span><strong>24</strong></li>
              <li><span>Recebimentos pendentes</span><strong>8</strong></li>
              <li><span>Alertas de validacao</span><strong>2</strong></li>
            </ul>
          </div>
          <div className="fac-lab-actions">
            <FacButton label="Atualizar" variant="secondary" />
            <FacButton label="Nova acao" variant="primary" />
          </div>
        </div>
      </section>

      <section className="fac-lab-panel">
        <p className="fac-eyebrow">Composicao</p>
        <h2>Mobile composition</h2>
        <p className="fac-muted">Mobile privilegia sequencia, cartoes curtos e uma acao principal facil de tocar.</p>
        <div className="fac-mobile-demo-card">
          <strong>Resumo rapido</strong>
          <span>3 tarefas prioritarias</span>
          <FacStatusBadge tone="warning">Requer atencao</FacStatusBadge>
        </div>
        <div className="fac-mobile-primary-action">
          <FacButton label="Acao principal" variant="primary" />
        </div>
      </section>

      <FacDialog
        footer={<FacButton label="Fechar" onClick={() => setDialogVisible(false)} variant="primary" />}
        header="Dialogo FAC"
        onHide={() => setDialogVisible(false)}
        visible={dialogVisible}
      >
        <p>Dialogo PrimeReact encapsulado pela camada visual FAC.</p>
      </FacDialog>

      <FacDialog
        footer={
          <div className="fac-lab-actions">
            <FacButton label="Cancelar" onClick={() => setConfirmVisible(false)} variant="ghost" />
            <FacButton label="Confirmar" onClick={() => setConfirmVisible(false)} variant="primary" />
          </div>
        }
        header="Confirmacao FAC"
        onHide={() => setConfirmVisible(false)}
        visible={confirmVisible}
      >
        <p>Exemplo visual de confirmacao controlada, sem executar qualquer operacao.</p>
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
