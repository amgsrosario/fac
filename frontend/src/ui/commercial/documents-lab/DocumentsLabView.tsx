import { DragEvent, KeyboardEvent, useEffect, useMemo, useState } from "react";
import { AuthSession } from "../../../api";
import { DesktopShell, FacButton, FacInputText, FacSelect, MobileShell, ResponsiveSlot, useDeviceClass } from "../../fac";
import { CommercialSidebar, ModuleHeader } from "../shared";
import "./documents-lab.css";

type LabMode = "complete" | "wizard" | "operator" | "unified" | "candidate";
type WizardStep = "header" | "lines";
type DocumentLineType = "commercial" | "text";
type Density = "comfortable" | "compact";
type Source = "Cliente" | "Alterado neste documento" | "Documento";

type Customer = { id: string; name: string; nif: string; address: string; currency: string; paymentTermId: string; transportId: string };
type Article = { id: string; name: string; unit: string; price: string; vatId: string };
type Warehouse = { id: string; name: string };
type PaymentTerm = { id: string; label: string };
type Transport = { id: string; label: string };
type VatRate = { id: string; label: string; rate: number };
type CompactOption = { code: string; detail?: string; label: string; value: string };

type DocumentLabState = {
  type: string;
  series: string;
  date: string;
  customerId: string;
  currency: string;
  paymentTermId: string;
  transportId: string;
  address: string;
  notes: string;
  defaultWarehouseId: string;
};

type DocumentLine = {
  id: string;
  type: DocumentLineType;
  position: number;
  articleId: string;
  description: string;
  quantity: string;
  unit: string;
  warehouseId: string;
  price: string;
  discount: string;
  vatId: string;
};

type Totals = { subtotal: number; discount: number; vat: number; total: number };

const today = () => new Date().toISOString().slice(0, 10);

const customers: Customer[] = [
  { id: "CLI-001", name: "Casa dos Sabores do Sul, Lda.", nif: "599100029", address: "Rua das Adegas Demo, 19", currency: "EUR", paymentTermId: "P30", transportId: "TRN" },
  { id: "CLI-002", name: "Mercearia Campo Dourado, Lda.", nif: "599100011", address: "Rua do Mercado Demo, 8", currency: "EUR", paymentTermId: "P15", transportId: "PRO" },
  { id: "CLI-003", name: "Sabores de Madrid SL", nif: "ESB9900001", address: "Calle Olivo Demo, 21", currency: "EUR", paymentTermId: "P30", transportId: "INT" },
  { id: "CLI-004", name: "Consumidor Final Demo", nif: "999999990", address: "Venda ao balcao", currency: "EUR", paymentTermId: "P0", transportId: "BAL" },
  { id: "CLI-005", name: "Hotel Monte Claro", nif: "515900100", address: "Estrada do Montado, 4", currency: "EUR", paymentTermId: "P60", transportId: "TRN" }
];

const articles: Article[] = [
  { id: "AZ075", name: "Azeite Virgem Extra 0,75 L", unit: "UN", price: "12.50", vatId: "INT" },
  { id: "AZ5L", name: "Azeite Virgem Extra 5 L", unit: "UN", price: "54.90", vatId: "INT" },
  { id: "VTRES", name: "Vinho Tinto Reserva", unit: "UN", price: "18.90", vatId: "NOR" },
  { id: "VBREG", name: "Vinho Branco Regional", unit: "UN", price: "11.90", vatId: "NOR" },
  { id: "CABAZ", name: "Caixa Presente Alentejana", unit: "UN", price: "39.90", vatId: "NOR" },
  { id: "PREP", name: "Servico de preparacao de cabaz", unit: "HR", price: "7.50", vatId: "NOR" },
  { id: "TRNAC", name: "Transporte nacional", unit: "UN", price: "15.00", vatId: "NOR" },
  { id: "MEL500", name: "Mel do montado 500 g", unit: "UN", price: "6.40", vatId: "RED" },
  { id: "QUEIJO", name: "Queijo curado regional", unit: "KG", price: "16.20", vatId: "RED" },
  { id: "PROVA", name: "Prova comentada de produtos", unit: "UN", price: "24.00", vatId: "NOR" }
];

const warehouses: Warehouse[] = [
  { id: "ARM-SUL", name: "Armazem Sul" },
  { id: "ARM-LOJA", name: "Loja Evora" },
  { id: "ARM-EXP", name: "Expedicao Norte" }
];

const paymentTerms: PaymentTerm[] = [
  { id: "P0", label: "Pronto pagamento" },
  { id: "P15", label: "15 dias" },
  { id: "P30", label: "30 dias" },
  { id: "P60", label: "60 dias" }
];

const transports: Transport[] = [
  { id: "BAL", label: "Entrega ao balcao" },
  { id: "TRN", label: "Transportadora nacional" },
  { id: "PRO", label: "Viatura propria" },
  { id: "INT", label: "Transporte internacional" }
];

const vatRates: VatRate[] = [
  { id: "RED", label: "Reduzida 6%", rate: 0.06 },
  { id: "INT", label: "Intermedia 13%", rate: 0.13 },
  { id: "NOR", label: "Normal 23%", rate: 0.23 }
];

const unitOptions: CompactOption[] = [
  { code: "UNI", label: "Unidade", value: "UNI" },
  { code: "KGM", label: "Quilograma", value: "KGM" },
  { code: "LTR", label: "Litro", value: "LTR" },
  { code: "HOR", label: "Hora", value: "HOR" }
];

const warehouseCodeOptions: CompactOption[] = [
  { code: "001", label: "Armazem principal", value: "ARM-SUL" },
  { code: "002", label: "Armazem secundario", value: "ARM-LOJA" },
  { code: "NOV", label: "Armazem novo", value: "ARM-EXP" }
];

const vatCodeOptions: CompactOption[] = [
  { code: "RED", detail: "6%", label: "Taxa reduzida", value: "RED" },
  { code: "INT", detail: "13%", label: "Taxa intermedia", value: "INT" },
  { code: "NOR", detail: "23%", label: "Taxa normal", value: "NOR" },
  { code: "ISE", detail: "0%", label: "Isento local", value: "ISE" }
];

const initialDocument: DocumentLabState = {
  type: "FT",
  series: "DEMO26",
  date: today(),
  customerId: "",
  currency: "EUR",
  paymentTermId: "P30",
  transportId: "",
  address: "",
  notes: "",
  defaultWarehouseId: "ARM-SUL"
};

const emptyLine = (warehouseId: string): DocumentLine => ({
  id: crypto.randomUUID(),
  type: "commercial",
  position: 0,
  articleId: "",
  description: "",
  quantity: "",
  unit: "",
  warehouseId,
  price: "",
  discount: "",
  vatId: ""
});

export default function DocumentsLabView({ currentUser, onLogout }: { currentUser: AuthSession; onLogout: () => void }) {
  const [mode, setMode] = useState<LabMode>(() => {
    const storedMode = window.localStorage.getItem("fac-doclab-mode");
    return storedMode === "candidate" || storedMode === "operator" || storedMode === "wizard" || storedMode === "unified" ? storedMode : "complete";
  });
  const [wizardStep, setWizardStep] = useState<WizardStep>("header");
  const [density, setDensity] = useState<Density>("comfortable");
  const [document, setDocument] = useState<DocumentLabState>(initialDocument);
  const [lines, setLines] = useState<DocumentLine[]>([]);
  const [draftLine, setDraftLine] = useState<DocumentLine>(() => emptyLine(initialDocument.defaultWarehouseId));
  const [selectedLineId, setSelectedLineId] = useState<string | null>(null);
  const [conditionsOpen, setConditionsOpen] = useState(false);
  const [openedAt] = useState(() => Date.now());
  const [elapsed, setElapsed] = useState(0);
  const [keyboardActions, setKeyboardActions] = useState(0);
  const [clicks, setClicks] = useState(0);
  const [notice, setNotice] = useState("Laboratorio local. Nada e gravado no backend.");
  const [labToolsOpen, setLabToolsOpen] = useState(false);
  const [pendingDeleteId, setPendingDeleteId] = useState<string | null>(null);
  const [wizardTransitions, setWizardTransitions] = useState(0);
  useDeviceClass();

  useEffect(() => {
    const id = window.setInterval(() => setElapsed(Math.floor((Date.now() - openedAt) / 1000)), 1000);
    return () => window.clearInterval(id);
  }, [openedAt]);

  useEffect(() => {
    if (mode !== "candidate" || !labToolsOpen) return;

    function closeCandidateLabOnOutsideClick(event: MouseEvent) {
      const target = event.target as HTMLElement | null;
      if (target?.closest(".fac-doclab-candidate-lab, .fac-doclab-candidate-actions .lab")) return;
      setLabToolsOpen(false);
    }

    window.addEventListener("mousedown", closeCandidateLabOnOutsideClick);
    return () => window.removeEventListener("mousedown", closeCandidateLabOnOutsideClick);
  }, [labToolsOpen, mode]);

  const selectedCustomer = customers.find((customer) => customer.id === document.customerId) ?? null;
  const totals = useMemo(() => calculateTotals(lines), [lines]);
  const filledFields = useMemo(() => countFilledFields(document, lines), [document, lines]);

  function updateDocument(patch: Partial<DocumentLabState>) {
    setDocument((current) => ({ ...current, ...patch }));
    if (patch.defaultWarehouseId && !isLineFilled(draftLine)) {
      setDraftLine((current) => ({ ...current, warehouseId: patch.defaultWarehouseId ?? current.warehouseId }));
    }
  }

  function changeMode(nextMode: LabMode) {
    setMode(nextMode);
    window.localStorage.setItem("fac-doclab-mode", nextMode);
  }

  function changeWizardStep(nextStep: WizardStep) {
    setWizardStep((current) => {
      if (current !== nextStep) setWizardTransitions((value) => value + 1);
      return nextStep;
    });
    if (mode === "candidate") {
      window.scrollTo({ top: 0 });
    }
    if ((mode === "unified" || mode === "candidate") && nextStep === "lines") {
      window.setTimeout(() => focusDraftLine(draftLine.id), 60);
    }
  }

  function resetLabState() {
    setDocument(initialDocument);
    setLines([]);
    setDraftLine(emptyLine(initialDocument.defaultWarehouseId));
    setSelectedLineId(null);
    setConditionsOpen(false);
    setWizardStep("header");
    setPendingDeleteId(null);
    setWizardTransitions(0);
    setNotice("Estado local reposto. Nada foi gravado no backend.");
  }

  function chooseCustomer(customerId: string | null) {
    const customer = customers.find((item) => item.id === customerId);
    setDocument((current) => ({
      ...current,
      customerId: customer?.id ?? "",
      currency: customer?.currency ?? current.currency,
      paymentTermId: customer?.paymentTermId ?? current.paymentTermId,
      transportId: customer?.transportId ?? current.transportId,
      address: customer?.address ?? ""
    }));
  }

  function updateLine(id: string, patch: Partial<DocumentLine>) {
    setLines((current) => current.map((line) => line.id === id ? { ...line, ...patch } : line));
  }

  function updateDraftLine(patch: Partial<DocumentLine>) {
    setDraftLine((current) => ({ ...current, ...patch }));
  }

  function chooseArticle(line: DocumentLine, articleId: string | null) {
    const article = articles.find((item) => item.id === articleId);
    updateLine(line.id, {
      articleId: article?.id ?? "",
      description: article?.name ?? "",
      unit: article?.unit ?? "",
      price: article?.price ?? "",
      vatId: article?.vatId ?? ""
    });
  }

  function chooseDraftArticle(articleId: string | null) {
    const article = articles.find((item) => item.id === articleId);
    setDraftLine((current) => ({
      ...current,
      articleId: article?.id ?? "",
      description: article?.name ?? "",
      unit: article?.unit ?? "",
      price: article?.price ?? "",
      vatId: article?.vatId ?? ""
    }));
  }

  function commitDraftLine(focus = true) {
    if (!isLineFilled(draftLine)) {
      setNotice("A linha ativa continua pronta a receber dados.");
      if (focus) window.setTimeout(() => focusDraftLine(), 40);
      return;
    }
    if (draftLine.type === "text" && !draftLine.description.trim()) {
      setNotice("A linha de texto precisa de descricao antes de ser adicionada.");
      if (focus) window.setTimeout(() => focusDraftLine(), 40);
      return;
    }
    const committed = { ...draftLine, id: crypto.randomUUID(), position: lines.length + 1 };
    const nextDraft = emptyLine(document.defaultWarehouseId);
    setLines((current) => resequenceLines([...current, committed]));
    setDraftLine(nextDraft);
    setSelectedLineId(nextDraft.id);
    setNotice("Linha inserida. Nova linha ativa pronta no fim.");
    if (focus) window.setTimeout(() => focusDraftLine(nextDraft.id), 60);
  }

  function addLine(focus = false) {
    commitDraftLine(focus);
  }

  function duplicateLine(id = selectedLineId) {
    const source = lines.find((line) => line.id === id);
    if (!source) return;
    const copy = { ...source, id: crypto.randomUUID() };
    setLines((current) => {
      const index = current.findIndex((line) => line.id === source.id);
      const next = [...current];
      next.splice(index + 1, 0, copy);
      return resequenceLines(next);
    });
    setSelectedLineId(copy.id);
    setNotice("Linha duplicada. O armazem da linha original foi preservado.");
  }

  function removeLine(id = selectedLineId) {
    if (!id) return;
    const line = lines.find((item) => item.id === id);
    if (!line) return;
    if (isLineFilled(line) && pendingDeleteId !== id) {
      setPendingDeleteId(id);
      setNotice("Linha com dados selecionada. Prima Delete novamente para remover.");
      return;
    }
    setLines((current) => resequenceLines(current.filter((item) => item.id !== id)));
    setPendingDeleteId(null);
    setSelectedLineId(null);
    setNotice("Linha removida.");
  }

  function generateLines(count: number) {
    const generated = Array.from({ length: count }, (_, index) => {
      const article = articles[index % articles.length];
      const warehouse = warehouses[index % warehouses.length];
      return {
        id: crypto.randomUUID(),
        type: index % 9 === 4 && mode === "unified" ? "text" as DocumentLineType : "commercial" as DocumentLineType,
        position: index + 1,
        articleId: article.id,
        description: index % 9 === 4 && mode === "unified" ? `Observacao documental local ${index + 1}` : article.name,
        quantity: index % 9 === 4 && mode === "unified" ? "" : String((index % 4) + 1),
        unit: index % 9 === 4 && mode === "unified" ? "" : article.unit,
        warehouseId: index % 9 === 4 && mode === "unified" ? "" : warehouse.id,
        price: index % 9 === 4 && mode === "unified" ? "" : article.price,
        discount: index % 9 === 4 && mode === "unified" ? "" : index % 7 === 0 ? "5" : "",
        vatId: index % 9 === 4 && mode === "unified" ? "" : article.vatId
      };
    });
    setLines(generated);
    setSelectedLineId(generated[0]?.id ?? null);
    setNotice(`${count} linhas de demonstracao geradas localmente.`);
  }

  function moveLine(id: string, direction: -1 | 1) {
    setLines((current) => {
      const index = current.findIndex((line) => line.id === id);
      const target = index + direction;
      if (index < 0 || target < 0 || target >= current.length) return current;
      const next = [...current];
      const [line] = next.splice(index, 1);
      next.splice(target, 0, line);
      return resequenceLines(next);
    });
    setSelectedLineId(id);
    window.setTimeout(() => focusLineRow(id), 40);
  }

  function reorderLine(id: string, targetIndex: number) {
    setLines((current) => {
      const index = current.findIndex((line) => line.id === id);
      if (index < 0 || targetIndex < 0 || targetIndex >= current.length || index === targetIndex) return current;
      const next = [...current];
      const [line] = next.splice(index, 1);
      next.splice(targetIndex, 0, line);
      return resequenceLines(next);
    });
    setSelectedLineId(id);
  }

  function insertTextLine(afterId?: string | null) {
    const textLine: DocumentLine = { ...emptyLine(""), description: "", id: crypto.randomUUID(), type: "text" };
    setLines((current) => {
      const index = afterId ? current.findIndex((line) => line.id === afterId) : current.length - 1;
      const next = [...current];
      next.splice(index >= 0 ? index + 1 : current.length, 0, textLine);
      return resequenceLines(next);
    });
    setSelectedLineId(textLine.id);
    setNotice("Linha de texto inserida localmente.");
    window.setTimeout(() => focusLineRow(textLine.id), 40);
  }

  function convertLine(id: string, type: DocumentLineType) {
    const source = lines.find((line) => line.id === id);
    if (!source || source.type === type) return;
    if (type === "text" && hasCommercialValues(source) && !window.confirm("Converter para texto vai limpar os valores comerciais desta linha. Continuar?")) return;
    setLines((current) => current.map((line) => {
      if (line.id !== id) return line;
      if (type === "text") return { ...line, articleId: "", discount: "", price: "", quantity: "", type, unit: "", vatId: "", warehouseId: "" };
      return { ...line, type, warehouseId: line.warehouseId || document.defaultWarehouseId };
    }));
    setNotice(type === "text" ? "Linha convertida em texto." : "Linha convertida em comercial. A descricao foi preservada.");
  }

  function handleLabKeyDown(event: KeyboardEvent<HTMLElement>) {
    if (event.key === "Escape") {
      if (labToolsOpen) {
        setLabToolsOpen(false);
        return;
      }
      setConditionsOpen(false);
      setNotice("Painel/pesquisa fechado.");
      return;
    }
    if (event.ctrlKey && event.key.toLowerCase() === "s") {
      event.preventDefault();
      setKeyboardActions((value) => value + 1);
      setNotice("Guardar simulado. Nenhum dado foi persistido.");
      return;
    }
    const hasUnifiedKeyboard = mode === "unified" || mode === "candidate";
    if (hasUnifiedKeyboard && event.altKey && (event.key === "ArrowUp" || event.key === "ArrowDown")) {
      event.preventDefault();
      if (selectedLineId) moveLine(selectedLineId, event.key === "ArrowUp" ? -1 : 1);
      setKeyboardActions((value) => value + 1);
      return;
    }
    if (hasUnifiedKeyboard && event.ctrlKey && event.key === "Enter") {
      event.preventDefault();
      commitDraftLine(true);
      setKeyboardActions((value) => value + 1);
      return;
    }
    if (hasUnifiedKeyboard && event.ctrlKey && event.key.toLowerCase() === "d") {
      event.preventDefault();
      setKeyboardActions((value) => value + 1);
      duplicateLine();
      return;
    }
    if (hasUnifiedKeyboard && event.key === "Delete") {
      event.preventDefault();
      setKeyboardActions((value) => value + 1);
      removeLine();
      return;
    }
    if (hasUnifiedKeyboard && event.key === "Enter") {
      const target = event.target as HTMLElement;
      if (target.matches("textarea") && !target.hasAttribute("data-draft-input")) return;
      if (target.matches("input, select, [role='combobox']")) {
        event.preventDefault();
        setKeyboardActions((value) => value + 1);
        if (target.hasAttribute("data-draft-input") && isLastDraftField(target)) {
          commitDraftLine(true);
          return;
        }
        focusNextGridField(target);
      }
      return;
    }
    if (mode !== "operator") return;
    if (event.ctrlKey && event.key === "Enter") {
      event.preventDefault();
      setKeyboardActions((value) => value + 1);
      commitDraftLine(true);
      return;
    }
    if (event.ctrlKey && event.key.toLowerCase() === "d") {
      event.preventDefault();
      setKeyboardActions((value) => value + 1);
      duplicateLine();
      return;
    }
    if (event.key === "Delete") {
      event.preventDefault();
      setKeyboardActions((value) => value + 1);
      removeLine();
      return;
    }
    if (event.key === "Enter") {
      const target = event.target as HTMLElement;
      if (target.matches("input, textarea, select, [role='combobox']")) {
        event.preventDefault();
        setKeyboardActions((value) => value + 1);
        if (target.hasAttribute("data-draft-input") && isLastDraftField(target)) {
          commitDraftLine(true);
          return;
        }
        focusNextGridField(target);
      }
    }
  }

  const metrics = <LabMetrics clicks={clicks} commercialLineCount={lines.filter((line) => line.type !== "text").length} elapsed={elapsed} filledFields={filledFields} keyboardActions={keyboardActions} lineCount={lines.length} mode={mode} step={mode === "wizard" || mode === "unified" || mode === "candidate" ? wizardStep : undefined} textLineCount={lines.filter((line) => line.type === "text").length} total={totals.total} transitions={wizardTransitions} />;
  const sidebar = <CommercialSidebar active="documents" currentUser={currentUser} onLogout={onLogout} />;
  const content = (
    <section className={`fac-doclab fac-doclab-${mode} ${(mode === "unified" || mode === "candidate") && wizardStep === "lines" ? "fac-doclab-unified-lines-active" : ""} fac-doclab-density-${density}`} onClick={() => setClicks((value) => value + 1)} onKeyDown={handleLabKeyDown}>
      {mode !== "candidate" && <LabHeader density={density} document={document} metrics={metrics} mode={mode} notice={notice} onDensity={setDensity} onGenerate={generateLines} onMode={changeMode} step={mode === "unified" ? wizardStep : undefined} />}
      {mode === "complete" ? (
        <CompleteDocumentWorkspace document={document} draftLine={draftLine} lines={lines} onChooseArticle={chooseArticle} onChooseCustomer={chooseCustomer} onChooseDraftArticle={chooseDraftArticle} onCommitDraftLine={() => commitDraftLine(true)} onDuplicateLine={duplicateLine} onOpenConditions={() => setConditionsOpen(true)} onRemoveLine={removeLine} onUpdateDocument={updateDocument} onUpdateDraftLine={updateDraftLine} onUpdateLine={updateLine} selectedCustomer={selectedCustomer} selectedLineId={selectedLineId} setSelectedLineId={setSelectedLineId} totals={totals} />
      ) : mode === "wizard" ? (
        <CompleteWizardWorkspace document={document} draftLine={draftLine} lines={lines} onChooseArticle={chooseArticle} onChooseCustomer={chooseCustomer} onChooseDraftArticle={chooseDraftArticle} onCommitDraftLine={() => commitDraftLine(true)} onDuplicateLine={duplicateLine} onRemoveLine={removeLine} onSetNotice={setNotice} onStep={changeWizardStep} onUpdateDocument={updateDocument} onUpdateDraftLine={updateDraftLine} onUpdateLine={updateLine} selectedCustomer={selectedCustomer} selectedLineId={selectedLineId} setSelectedLineId={setSelectedLineId} step={wizardStep} totals={totals} />
      ) : mode === "unified" ? (
        <UnifiedDocumentWorkspace document={document} draftLine={draftLine} lines={lines} onChooseArticle={chooseArticle} onChooseCustomer={chooseCustomer} onChooseDraftArticle={chooseDraftArticle} onCommitDraftLine={() => commitDraftLine(true)} onConvertLine={convertLine} onDuplicateLine={duplicateLine} onInsertTextLine={insertTextLine} onMoveLine={moveLine} onRemoveLine={removeLine} onReorderLine={reorderLine} onSetNotice={setNotice} onStep={changeWizardStep} onUpdateDocument={updateDocument} onUpdateDraftLine={updateDraftLine} onUpdateLine={updateLine} selectedCustomer={selectedCustomer} selectedLineId={selectedLineId} setSelectedLineId={setSelectedLineId} step={wizardStep} totals={totals} />
      ) : mode === "candidate" ? (
        <CandidateDocumentWorkspace density={density} document={document} draftLine={draftLine} labToolsOpen={labToolsOpen} lines={lines} metrics={metrics} notice={notice} onChooseArticle={chooseArticle} onChooseCustomer={chooseCustomer} onChooseDraftArticle={chooseDraftArticle} onCommitDraftLine={() => commitDraftLine(true)} onConvertLine={convertLine} onDensity={setDensity} onDuplicateLine={duplicateLine} onGenerate={generateLines} onInsertTextLine={insertTextLine} onMoveLine={moveLine} onMode={changeMode} onRemoveLine={removeLine} onReorderLine={reorderLine} onReset={resetLabState} onSetLabToolsOpen={setLabToolsOpen} onSetNotice={setNotice} onStep={changeWizardStep} onUpdateDocument={updateDocument} onUpdateDraftLine={updateDraftLine} onUpdateLine={updateLine} selectedCustomer={selectedCustomer} selectedLineId={selectedLineId} setSelectedLineId={setSelectedLineId} step={wizardStep} totals={totals} />
      ) : (
        <OperatorDocumentWorkspaceDraft document={document} draftLine={draftLine} lines={lines} onChooseArticle={chooseArticle} onChooseDraftArticle={chooseDraftArticle} onCommitDraftLine={() => commitDraftLine(true)} onDuplicateLine={duplicateLine} onOpenConditions={() => setConditionsOpen(true)} onRemoveLine={removeLine} onUpdateDraftLine={updateDraftLine} onUpdateLine={updateLine} selectedCustomer={selectedCustomer} selectedLineId={selectedLineId} setSelectedLineId={setSelectedLineId} totals={totals} />
      )}
      <DocumentConditionsPanel document={document} onChooseCustomer={chooseCustomer} onClose={() => setConditionsOpen(false)} onUpdateDocument={updateDocument} open={conditionsOpen} selectedCustomer={selectedCustomer} />
    </section>
  );
  const focusedDesktop = <main className="fac-ui fac-doclab-focus-shell"><section className="fac-commercial-content"><div className="fac-commercial-content-inner">{content}</div></section></main>;
  return <ResponsiveSlot desktop={mode === "operator" ? focusedDesktop : <DesktopShell sidebar={sidebar}>{content}</DesktopShell>} mobile={<MobileShell title="FAC Documentos Lab">{content}</MobileShell>} tablet={<DesktopShell sidebar={sidebar}>{content}</DesktopShell>} />;
}

function LabHeader({ density, document, metrics, mode, notice, onDensity, onGenerate, onMode, step }: { density: Density; document: DocumentLabState; metrics: JSX.Element; mode: LabMode; notice: string; onDensity: (density: Density) => void; onGenerate: (count: number) => void; onMode: (mode: LabMode) => void; step?: WizardStep }) {
  const compact = mode === "operator" || (mode === "unified" && step === "lines");
  return (
    <header className="fac-doclab-header">
      <ModuleHeader compact={compact} eyebrow="Documentos Lab" subtitle="Experiencia local para comparar introducao completa e operacao intensiva." summary={metrics} title="UX documental" />
      <div className="fac-doclab-controls" aria-label="Controlos do laboratorio">
        <div className="fac-doclab-segmented" aria-label="Experiencia"><span>Experiencia</span><button className={mode === "complete" ? "active" : ""} onClick={() => onMode("complete")} type="button">Completo atual</button><button className={mode === "wizard" ? "active" : ""} onClick={() => onMode("wizard")} type="button">Completo em duas fases</button><button className={mode === "operator" ? "active" : ""} onClick={() => onMode("operator")} type="button">Operador</button><button className={mode === "unified" ? "active" : ""} onClick={() => onMode("unified")} type="button">Experiencia unificada</button><button className={mode === "candidate" ? "active" : ""} onClick={() => onMode("candidate")} type="button">Estrutura candidata</button></div>
        <div className="fac-doclab-segmented" aria-label="Densidade"><span>Densidade</span><button className={density === "comfortable" ? "active" : ""} onClick={() => onDensity("comfortable")} type="button">Suave</button><button className={density === "compact" ? "active" : ""} onClick={() => onDensity("compact")} type="button">Densa</button></div>
        <div className="fac-doclab-volume">{[1, 20, 50, 100].map((count) => <button key={count} onClick={() => onGenerate(count)} type="button">{count} linha{count > 1 ? "s" : ""}</button>)}</div>
      </div>
      <div className="fac-doclab-notice">{notice}</div>
      <div className="fac-doclab-safe-strip">{document.type} · Serie {document.series} · {formatDate(document.date)} · dados ficticios locais</div>
    </header>
  );
}

type CandidateProps = Omit<UnifiedProps, "onSetNotice"> & {
  density: Density;
  labToolsOpen: boolean;
  metrics: JSX.Element;
  notice: string;
  onDensity: (density: Density) => void;
  onGenerate: (count: number) => void;
  onMode: (mode: LabMode) => void;
  onReset: () => void;
  onSetLabToolsOpen: (open: boolean) => void;
  onSetNotice: (message: string) => void;
};

function CandidateDocumentWorkspace(props: CandidateProps) {
  const { density, document, draftLine, labToolsOpen, lines, metrics, notice, onChooseArticle, onChooseCustomer, onChooseDraftArticle, onCommitDraftLine, onConvertLine, onDensity, onDuplicateLine, onGenerate, onInsertTextLine, onMode, onMoveLine, onRemoveLine, onReorderLine, onReset, onSetLabToolsOpen, onSetNotice, onStep, onUpdateDocument, onUpdateDraftLine, onUpdateLine, selectedCustomer, selectedLineId, setSelectedLineId, step, totals } = props;
  const [validationMessage, setValidationMessage] = useState("");

  function goToHeader() {
    setValidationMessage("");
    onStep("header");
    onSetNotice("Estrutura candidata: cabecalho aberto com estado preservado.");
  }

  function goToLines() {
    const validation = validateWizardHeader(document);
    if (validation) {
      setValidationMessage(validation.message);
      onSetNotice(validation.message);
      window.setTimeout(() => focusWizardField(validation.field), 40);
      return;
    }
    setValidationMessage("");
    onStep("lines");
    onSetNotice("Estrutura candidata: linhas abertas com o LAB recolhido.");
  }

  return (
    <div className="fac-doclab-candidate-shell">
      <CandidateAppBar labToolsOpen={labToolsOpen} onLabToggle={() => onSetLabToolsOpen(!labToolsOpen)} onSetNotice={onSetNotice} />
      {labToolsOpen && <CandidateLabPanel density={density} metrics={metrics} mode="candidate" notice={notice} onClose={() => onSetLabToolsOpen(false)} onDensity={onDensity} onGenerate={onGenerate} onMode={onMode} onReset={onReset} />}
      <main className="fac-doclab-candidate-workspace">
        {step === "header" ? (
          <CandidateHeaderStep document={document} onChooseCustomer={onChooseCustomer} onContinue={goToLines} onUpdateDocument={onUpdateDocument} selectedCustomer={selectedCustomer} validationMessage={validationMessage} />
        ) : (
          <UnifiedLinesStep candidate document={document} draftLine={draftLine} lines={lines} onChooseArticle={onChooseArticle} onChooseDraftArticle={onChooseDraftArticle} onCommitDraftLine={onCommitDraftLine} onConvertLine={onConvertLine} onDuplicateLine={onDuplicateLine} onEditHeader={goToHeader} onInsertTextLine={onInsertTextLine} onMoveLine={onMoveLine} onRemoveLine={onRemoveLine} onReorderLine={onReorderLine} onSetNotice={onSetNotice} onUpdateDraftLine={onUpdateDraftLine} onUpdateLine={onUpdateLine} selectedCustomer={selectedCustomer} selectedLineId={selectedLineId} setSelectedLineId={setSelectedLineId} totals={totals} />
        )}
      </main>
    </div>
  );
}

function CandidateAppBar({ labToolsOpen, onLabToggle, onSetNotice }: { labToolsOpen: boolean; onLabToggle: () => void; onSetNotice: (message: string) => void }) {
  return (
    <header className="fac-doclab-candidate-appbar">
      <div className="fac-doclab-candidate-title"><strong>Documentos</strong><span>Rascunho</span></div>
      <div className="fac-doclab-candidate-actions">
        <button onClick={() => onSetNotice("Guardar simulado localmente. Nenhum endpoint foi chamado.")} type="button">Guardar</button>
        <button className="primary" onClick={() => onSetNotice("Emissao simulada localmente. Nenhum documento real foi criado.")} type="button">Emitir</button>
        <button aria-label="Mais acoes" type="button">...</button>
        <button aria-expanded={labToolsOpen} className="lab" onClick={onLabToggle} type="button">LAB</button>
      </div>
    </header>
  );
}

function CandidateLabPanel({ density, metrics, mode, notice, onClose, onDensity, onGenerate, onMode, onReset }: { density: Density; metrics: JSX.Element; mode: LabMode; notice: string; onClose: () => void; onDensity: (density: Density) => void; onGenerate: (count: number) => void; onMode: (mode: LabMode) => void; onReset: () => void }) {
  return (
    <aside className="fac-doclab-candidate-lab" aria-label="Ferramentas do laboratorio">
      <div className="fac-doclab-candidate-lab-header"><strong>Ferramentas LAB</strong><button aria-label="Fechar ferramentas LAB" onClick={onClose} type="button">X</button></div>
      <div className="fac-doclab-segmented" aria-label="Experiencia"><span>Experiencia</span><button className={mode === "complete" ? "active" : ""} onClick={() => onMode("complete")} type="button">Completo atual</button><button className={mode === "wizard" ? "active" : ""} onClick={() => onMode("wizard")} type="button">Duas fases</button><button className={mode === "operator" ? "active" : ""} onClick={() => onMode("operator")} type="button">Operador</button><button className={mode === "unified" ? "active" : ""} onClick={() => onMode("unified")} type="button">Unificada atual</button><button className={mode === "candidate" ? "active" : ""} onClick={() => onMode("candidate")} type="button">Estrutura candidata</button></div>
      <div className="fac-doclab-segmented" aria-label="Densidade"><span>Densidade</span><button className={density === "comfortable" ? "active" : ""} onClick={() => onDensity("comfortable")} type="button">Suave</button><button className={density === "compact" ? "active" : ""} onClick={() => onDensity("compact")} type="button">Densa</button></div>
      <div className="fac-doclab-volume">{[1, 20, 50, 100].map((count) => <button key={count} onClick={() => onGenerate(count)} type="button">{count} linha{count > 1 ? "s" : ""}</button>)}</div>
      {metrics}
      <div className="fac-doclab-notice">{notice}</div>
      <div className="fac-doclab-safe-strip">Dados ficticios locais. Nada e gravado no backend.</div>
      <button className="fac-doclab-candidate-reset" onClick={onReset} type="button">Reset de estado</button>
    </aside>
  );
}

function CandidateHeaderStep({ document, onChooseCustomer, onContinue, onUpdateDocument, selectedCustomer, validationMessage }: { document: DocumentLabState; onChooseCustomer: (customerId: string | null) => void; onContinue: () => void; onUpdateDocument: (patch: Partial<DocumentLabState>) => void; selectedCustomer: Customer | null; validationMessage: string }) {
  return (
    <section className="fac-doclab-candidate-header">
      <div className="fac-doclab-candidate-phase"><span className="active">1 Cabecalho</span><span>2 Linhas</span></div>
      <div className="fac-doclab-candidate-form">
        <div className="fac-doclab-candidate-section-title"><p className="fac-eyebrow">Cabecalho e condicoes</p><h2>Novo documento comercial</h2></div>
        {validationMessage && <div className="fac-doclab-wizard-validation" role="alert">{validationMessage}</div>}
        <div className="fac-doclab-wizard-fields">
          <label><span>Tipo</span><select data-wizard-field="type" onChange={(event) => onUpdateDocument({ type: event.target.value })} value={document.type}><option value="FT">FT - Fatura</option><option value="NC">NC - Nota de credito</option></select></label>
          <label><span>Serie</span><select data-wizard-field="series" onChange={(event) => onUpdateDocument({ series: event.target.value })} value={document.series}><option value="DEMO26">DEMO26</option><option value="ONLINE26">ONLINE26</option></select></label>
          <label><span>Data</span><input data-wizard-field="date" onChange={(event) => onUpdateDocument({ date: event.target.value })} type="date" value={document.date} /></label>
          <label className="fac-doclab-wizard-field-wide"><span>Cliente</span><select data-wizard-field="customerId" onChange={(event) => onChooseCustomer(event.target.value || null)} value={document.customerId}><option value="">Selecionar cliente</option>{customers.map((customer) => <option key={customer.id} value={customer.id}>{customer.name} - {customer.nif}</option>)}</select></label>
          <label><span>Moeda</span><select data-wizard-field="currency" onChange={(event) => onUpdateDocument({ currency: event.target.value })} value={document.currency}><option value="EUR">EUR</option><option value="USD">USD</option></select></label>
          <label><span>Pagamento</span><select data-wizard-field="paymentTermId" onChange={(event) => onUpdateDocument({ paymentTermId: event.target.value })} value={document.paymentTermId}>{paymentTerms.map((term) => <option key={term.id} value={term.id}>{term.label}</option>)}</select></label>
          <label><span>Transporte</span><select data-wizard-field="transportId" onChange={(event) => onUpdateDocument({ transportId: event.target.value })} value={document.transportId}><option value="">Sem modo definido</option>{transports.map((transport) => <option key={transport.id} value={transport.id}>{transport.label}</option>)}</select></label>
          <label><span>Armazem por defeito</span><select data-wizard-field="defaultWarehouseId" onChange={(event) => onUpdateDocument({ defaultWarehouseId: event.target.value })} value={document.defaultWarehouseId}>{warehouses.map((warehouse) => <option key={warehouse.id} value={warehouse.id}>{warehouse.name}</option>)}</select></label>
          <label className="fac-doclab-wizard-field-wide"><span>Morada</span><textarea data-wizard-field="address" onChange={(event) => onUpdateDocument({ address: event.target.value })} value={document.address} /></label>
          <label className="fac-doclab-wizard-field-wide"><span>Observacoes</span><textarea data-wizard-field="notes" onChange={(event) => onUpdateDocument({ notes: event.target.value })} value={document.notes} /></label>
        </div>
        <div className="fac-doclab-candidate-footer">
          <ConditionOrigins document={document} selectedCustomer={selectedCustomer} />
          <FacButton icon="pi pi-arrow-right" label="Continuar para linhas" onClick={onContinue} variant="primary" />
        </div>
      </div>
    </section>
  );
}

type WorkspaceProps = {
  document: DocumentLabState;
  draftLine: DocumentLine;
  lines: DocumentLine[];
  onChooseArticle: (line: DocumentLine, articleId: string | null) => void;
  onChooseDraftArticle: (articleId: string | null) => void;
  onCommitDraftLine: () => void;
  onChooseCustomer: (customerId: string | null) => void;
  onDuplicateLine: (id?: string | null) => void;
  onOpenConditions: () => void;
  onRemoveLine: (id?: string | null) => void;
  onUpdateDocument: (patch: Partial<DocumentLabState>) => void;
  onUpdateDraftLine: (patch: Partial<DocumentLine>) => void;
  onUpdateLine: (id: string, patch: Partial<DocumentLine>) => void;
  selectedCustomer: Customer | null;
  selectedLineId: string | null;
  setSelectedLineId: (id: string) => void;
  totals: Totals;
};

function CompleteDocumentWorkspace(props: WorkspaceProps) {
  const { document, draftLine, lines, onChooseArticle, onChooseCustomer, onChooseDraftArticle, onCommitDraftLine, onDuplicateLine, onOpenConditions, onRemoveLine, onUpdateDocument, onUpdateDraftLine, onUpdateLine, selectedCustomer, selectedLineId, setSelectedLineId, totals } = props;
  return (
    <div className="fac-doclab-complete-grid">
      <section className="fac-doclab-panel fac-doclab-hero-panel">
        <div className="fac-doclab-panel-title"><div><p className="fac-eyebrow">Cabecalho completo</p><h2>Condicoes e contexto do documento</h2></div><FacButton icon="pi pi-sliders-h" label="Editar condicoes" onClick={onOpenConditions} variant="secondary" /></div>
        <div className="fac-doclab-form-grid">
          <FacSelect label="Tipo" onChange={(value) => onUpdateDocument({ type: value ?? "FT" })} options={[{ label: "FT - Fatura", value: "FT" }, { label: "NC - Nota de credito", value: "NC" }]} value={document.type} />
          <FacSelect label="Serie" onChange={(value) => onUpdateDocument({ series: value ?? "DEMO26" })} options={[{ label: "DEMO26", value: "DEMO26" }, { label: "ONLINE26", value: "ONLINE26" }]} value={document.series} />
          <FacInputText label="Data" onChange={(event) => onUpdateDocument({ date: event.target.value })} type="date" value={document.date} />
          <FacSelect label="Cliente" onChange={onChooseCustomer} options={customers.map((customer) => ({ label: `${customer.name} · ${customer.nif}`, value: customer.id }))} placeholder="Selecionar cliente" value={document.customerId} />
          <FacSelect label="Moeda" onChange={(value) => onUpdateDocument({ currency: value ?? "EUR" })} options={[{ label: "EUR", value: "EUR" }, { label: "USD", value: "USD" }]} value={document.currency} />
          <FacSelect label="Pagamento" onChange={(value) => onUpdateDocument({ paymentTermId: value ?? "" })} options={paymentTerms.map((term) => ({ label: term.label, value: term.id }))} value={document.paymentTermId} />
          <FacSelect label="Transporte" onChange={(value) => onUpdateDocument({ transportId: value ?? "" })} options={transports.map((transport) => ({ label: transport.label, value: transport.id }))} value={document.transportId} />
          <FacSelect label="Armazem por defeito para novas linhas" onChange={(value) => onUpdateDocument({ defaultWarehouseId: value ?? warehouses[0].id })} options={warehouses.map((warehouse) => ({ label: warehouse.name, value: warehouse.id }))} value={document.defaultWarehouseId} />
          <label className="fac-doclab-textarea"><span>Morada</span><textarea onChange={(event) => onUpdateDocument({ address: event.target.value })} value={document.address} /></label>
          <label className="fac-doclab-textarea"><span>Observacoes</span><textarea onChange={(event) => onUpdateDocument({ notes: event.target.value })} value={document.notes} /></label>
        </div>
        <ConditionOrigins document={document} selectedCustomer={selectedCustomer} />
      </section>
      <section className="fac-doclab-panel"><div className="fac-doclab-panel-title"><div><p className="fac-eyebrow">Linhas</p><h2>Linhas comerciais</h2></div></div><DocumentLinesGridDraft draftLine={draftLine} lines={lines} onChooseArticle={onChooseArticle} onChooseDraftArticle={onChooseDraftArticle} onCommitDraftLine={onCommitDraftLine} onDuplicateLine={onDuplicateLine} onRemoveLine={onRemoveLine} onUpdateDraftLine={onUpdateDraftLine} onUpdateLine={onUpdateLine} selectedLineId={selectedLineId} setSelectedLineId={setSelectedLineId} /></section>
      <DocumentTotalsBar totals={totals} />
    </div>
  );
}

type CompleteWizardProps = Omit<WorkspaceProps, "onOpenConditions"> & {
  onSetNotice: (message: string) => void;
  onStep: (step: WizardStep) => void;
  step: WizardStep;
};

function CompleteWizardWorkspace(props: CompleteWizardProps) {
  const { document, draftLine, lines, onChooseArticle, onChooseCustomer, onChooseDraftArticle, onCommitDraftLine, onDuplicateLine, onRemoveLine, onSetNotice, onStep, onUpdateDocument, onUpdateDraftLine, onUpdateLine, selectedCustomer, selectedLineId, setSelectedLineId, step, totals } = props;
  const [validationMessage, setValidationMessage] = useState("");

  function goToHeader() {
    setValidationMessage("");
    onStep("header");
    onSetNotice("Cabecalho aberto para rever condicoes. As linhas foram preservadas.");
  }

  function goToLines() {
    const validation = validateWizardHeader(document);
    if (validation) {
      setValidationMessage(validation.message);
      onSetNotice(validation.message);
      window.setTimeout(() => focusWizardField(validation.field), 40);
      return;
    }
    setValidationMessage("");
    onStep("lines");
    onSetNotice("Fase de linhas aberta. Estado local preservado.");
  }

  function simulateDraft() {
    onSetNotice("Rascunho simulado localmente. Nenhum endpoint foi chamado.");
  }

  function simulateIssue() {
    onSetNotice("Emissao simulada localmente. Nenhum documento real foi criado.");
  }

  return (
    <div className="fac-doclab-wizard">
      <div className="fac-doclab-wizard-steps" aria-label="Fases do wizard documental">
        <button className={step === "header" ? "active" : ""} onClick={goToHeader} type="button"><span>1</span>Condicoes</button>
        <button className={step === "lines" ? "active" : ""} onClick={goToLines} type="button"><span>2</span>Linhas</button>
      </div>
      {step === "header" ? (
        <DocumentHeaderStep document={document} onChooseCustomer={onChooseCustomer} onContinue={goToLines} onUpdateDocument={onUpdateDocument} selectedCustomer={selectedCustomer} validationMessage={validationMessage} />
      ) : (
        <DocumentLinesStep document={document} draftLine={draftLine} lines={lines} onChooseArticle={onChooseArticle} onChooseDraftArticle={onChooseDraftArticle} onCommitDraftLine={onCommitDraftLine} onDuplicateLine={onDuplicateLine} onEditHeader={goToHeader} onIssue={simulateIssue} onRemoveLine={onRemoveLine} onSaveDraft={simulateDraft} onUpdateDraftLine={onUpdateDraftLine} onUpdateLine={onUpdateLine} selectedCustomer={selectedCustomer} selectedLineId={selectedLineId} setSelectedLineId={setSelectedLineId} totals={totals} />
      )}
    </div>
  );
}

function DocumentHeaderStep({ document, onChooseCustomer, onContinue, onUpdateDocument, selectedCustomer, validationMessage }: { document: DocumentLabState; onChooseCustomer: (customerId: string | null) => void; onContinue: () => void; onUpdateDocument: (patch: Partial<DocumentLabState>) => void; selectedCustomer: Customer | null; validationMessage: string }) {
  return (
    <section className="fac-doclab-wizard-header-grid">
      <div className="fac-doclab-wizard-surface">
        <div className="fac-doclab-wizard-title">
          <div><p className="fac-eyebrow">Fase 1</p><h2>Cabecalho e condicoes</h2></div>
          <button className="fac-doclab-wizard-ai" disabled type="button">Assistencia futura</button>
        </div>
        {validationMessage && <div className="fac-doclab-wizard-validation" role="alert">{validationMessage}</div>}
        <div className="fac-doclab-wizard-fields">
          <label><span>Tipo</span><select data-wizard-field="type" onChange={(event) => onUpdateDocument({ type: event.target.value })} value={document.type}><option value="FT">FT - Fatura</option><option value="NC">NC - Nota de credito</option></select></label>
          <label><span>Serie</span><select data-wizard-field="series" onChange={(event) => onUpdateDocument({ series: event.target.value })} value={document.series}><option value="DEMO26">DEMO26</option><option value="ONLINE26">ONLINE26</option></select></label>
          <label><span>Data</span><input data-wizard-field="date" onChange={(event) => onUpdateDocument({ date: event.target.value })} type="date" value={document.date} /></label>
          <label className="fac-doclab-wizard-field-wide"><span>Cliente</span><select data-wizard-field="customerId" onChange={(event) => onChooseCustomer(event.target.value || null)} value={document.customerId}><option value="">Selecionar cliente</option>{customers.map((customer) => <option key={customer.id} value={customer.id}>{customer.name} - {customer.nif}</option>)}</select></label>
          <label><span>Moeda</span><select data-wizard-field="currency" onChange={(event) => onUpdateDocument({ currency: event.target.value })} value={document.currency}><option value="EUR">EUR</option><option value="USD">USD</option></select></label>
          <label><span>Prazo</span><select data-wizard-field="paymentTermId" onChange={(event) => onUpdateDocument({ paymentTermId: event.target.value })} value={document.paymentTermId}>{paymentTerms.map((term) => <option key={term.id} value={term.id}>{term.label}</option>)}</select></label>
          <label><span>Modo de pagamento</span><select data-wizard-field="transportId" onChange={(event) => onUpdateDocument({ transportId: event.target.value })} value={document.transportId}><option value="">Sem modo definido</option>{transports.map((transport) => <option key={transport.id} value={transport.id}>{transport.label}</option>)}</select></label>
          <label><span>Armazem por defeito</span><select data-wizard-field="defaultWarehouseId" onChange={(event) => onUpdateDocument({ defaultWarehouseId: event.target.value })} value={document.defaultWarehouseId}>{warehouses.map((warehouse) => <option key={warehouse.id} value={warehouse.id}>{warehouse.name}</option>)}</select></label>
          <label className="fac-doclab-wizard-field-wide"><span>Morada</span><textarea data-wizard-field="address" onChange={(event) => onUpdateDocument({ address: event.target.value })} value={document.address} /></label>
          <label className="fac-doclab-wizard-field-wide"><span>Observacoes</span><textarea data-wizard-field="notes" onChange={(event) => onUpdateDocument({ notes: event.target.value })} value={document.notes} /></label>
        </div>
        <div className="fac-doclab-wizard-actions">
          <span>Guardar e emitir ficam simulados na fase 2.</span>
          <FacButton icon="pi pi-arrow-right" label="Continuar para linhas" onClick={onContinue} variant="primary" />
        </div>
      </div>
      <aside className="fac-doclab-wizard-summary">
        <div><p className="fac-eyebrow">Resumo</p><h3>{selectedCustomer?.name ?? "Cliente por selecionar"}</h3><span>{document.type} - {document.series} - {formatDate(document.date)}</span></div>
        <ConditionOrigins document={document} selectedCustomer={selectedCustomer} />
        <div className="fac-doclab-wizard-assist"><strong>Assistencia</strong><span>Reservado para sugestoes futuras de condicoes recentes, divergencias e historico do cliente.</span></div>
      </aside>
    </section>
  );
}

function DocumentLinesStep({ document, draftLine, lines, onChooseArticle, onChooseDraftArticle, onCommitDraftLine, onDuplicateLine, onEditHeader, onIssue, onRemoveLine, onSaveDraft, onUpdateDraftLine, onUpdateLine, selectedCustomer, selectedLineId, setSelectedLineId, totals }: { document: DocumentLabState; draftLine: DocumentLine; lines: DocumentLine[]; onChooseArticle: (line: DocumentLine, articleId: string | null) => void; onChooseDraftArticle: (articleId: string | null) => void; onCommitDraftLine: () => void; onDuplicateLine: (id?: string | null) => void; onEditHeader: () => void; onIssue: () => void; onRemoveLine: (id?: string | null) => void; onSaveDraft: () => void; onUpdateDraftLine: (patch: Partial<DocumentLine>) => void; onUpdateLine: (id: string, patch: Partial<DocumentLine>) => void; selectedCustomer: Customer | null; selectedLineId: string | null; setSelectedLineId: (id: string) => void; totals: Totals }) {
  return (
    <div className="fac-doclab-wizard-lines">
      <section className="fac-doclab-wizard-lines-top">
        <div className="fac-doclab-wizard-doc-strip"><strong>{document.type}</strong><span>{document.series}</span><span>{selectedCustomer?.name ?? "Cliente por selecionar"}</span><span>{paymentLabel(document.paymentTermId)}</span><span>{document.currency}</span><span>{warehouseLabel(document.defaultWarehouseId)} para novas linhas</span></div>
        <div className="fac-doclab-wizard-actions">
          <FacButton icon="pi pi-pencil" label="Editar cabecalho" onClick={onEditHeader} variant="secondary" />
          <FacButton icon="pi pi-save" label="Guardar rascunho" onClick={onSaveDraft} variant="ghost" />
          <FacButton icon="pi pi-check" label="Emitir" onClick={onIssue} variant="primary" />
        </div>
      </section>
      <section className="fac-doclab-wizard-grid">
        <div className="fac-doclab-wizard-title">
          <div><p className="fac-eyebrow">Fase 2</p><h2>Linhas e totais</h2></div>
          <button className="fac-doclab-wizard-ai" disabled type="button">Assistencia futura</button>
        </div>
        <DocumentLinesGridDraft draftLine={draftLine} lines={lines} onChooseArticle={onChooseArticle} onChooseDraftArticle={onChooseDraftArticle} onCommitDraftLine={onCommitDraftLine} onDuplicateLine={onDuplicateLine} onRemoveLine={onRemoveLine} onUpdateDraftLine={onUpdateDraftLine} onUpdateLine={onUpdateLine} selectedLineId={selectedLineId} setSelectedLineId={setSelectedLineId} />
      </section>
      <OperatorTotalsStrip totals={totals} />
    </div>
  );
}

type UnifiedProps = Omit<CompleteWizardProps, "onOpenConditions"> & {
  onConvertLine: (id: string, type: DocumentLineType) => void;
  onInsertTextLine: (afterId?: string | null) => void;
  onMoveLine: (id: string, direction: -1 | 1) => void;
  onReorderLine: (id: string, targetIndex: number) => void;
};

function UnifiedDocumentWorkspace(props: UnifiedProps) {
  const { document, draftLine, lines, onChooseArticle, onChooseCustomer, onChooseDraftArticle, onCommitDraftLine, onConvertLine, onDuplicateLine, onInsertTextLine, onMoveLine, onRemoveLine, onReorderLine, onSetNotice, onStep, onUpdateDocument, onUpdateDraftLine, onUpdateLine, selectedCustomer, selectedLineId, setSelectedLineId, step, totals } = props;
  const [validationMessage, setValidationMessage] = useState("");

  function goToHeader() {
    setValidationMessage("");
    onStep("header");
    onSetNotice("Experiencia unificada: cabecalho aberto sem perder linhas ou ordem.");
  }

  function goToLines() {
    const validation = validateWizardHeader(document);
    if (validation) {
      setValidationMessage(validation.message);
      onSetNotice(validation.message);
      window.setTimeout(() => focusWizardField(validation.field), 40);
      return;
    }
    setValidationMessage("");
    onStep("lines");
    onSetNotice("Experiencia unificada: grelha aberta com estado preservado.");
  }

  return (
    <div className="fac-doclab-unified">
      {step === "header" && (
        <div className="fac-doclab-unified-steps" aria-label="Fases da experiencia unificada">
          <button className="active" onClick={goToHeader} type="button"><span>1</span>Cabecalho e condicoes</button>
          <button onClick={goToLines} type="button"><span>2</span>Linhas e totais</button>
        </div>
      )}
      {step === "header" ? (
        <section className="fac-doclab-unified-header">
          <DocumentHeaderStep document={document} onChooseCustomer={onChooseCustomer} onContinue={goToLines} onUpdateDocument={onUpdateDocument} selectedCustomer={selectedCustomer} validationMessage={validationMessage} />
        </section>
      ) : (
        <UnifiedLinesStep document={document} draftLine={draftLine} lines={lines} onChooseArticle={onChooseArticle} onChooseDraftArticle={onChooseDraftArticle} onCommitDraftLine={onCommitDraftLine} onConvertLine={onConvertLine} onDuplicateLine={onDuplicateLine} onEditHeader={goToHeader} onInsertTextLine={onInsertTextLine} onMoveLine={onMoveLine} onRemoveLine={onRemoveLine} onReorderLine={onReorderLine} onSetNotice={onSetNotice} onUpdateDraftLine={onUpdateDraftLine} onUpdateLine={onUpdateLine} selectedCustomer={selectedCustomer} selectedLineId={selectedLineId} setSelectedLineId={setSelectedLineId} totals={totals} />
      )}
    </div>
  );
}

function UnifiedLinesStep({ candidate = false, document, draftLine, lines, onChooseArticle, onChooseDraftArticle, onCommitDraftLine, onConvertLine, onDuplicateLine, onEditHeader, onInsertTextLine, onMoveLine, onRemoveLine, onReorderLine, onSetNotice, onUpdateDraftLine, onUpdateLine, selectedCustomer, selectedLineId, setSelectedLineId, totals }: { candidate?: boolean; document: DocumentLabState; draftLine: DocumentLine; lines: DocumentLine[]; onChooseArticle: (line: DocumentLine, articleId: string | null) => void; onChooseDraftArticle: (articleId: string | null) => void; onCommitDraftLine: () => void; onConvertLine: (id: string, type: DocumentLineType) => void; onDuplicateLine: (id?: string | null) => void; onEditHeader: () => void; onInsertTextLine: (afterId?: string | null) => void; onMoveLine: (id: string, direction: -1 | 1) => void; onRemoveLine: (id?: string | null) => void; onReorderLine: (id: string, targetIndex: number) => void; onSetNotice: (message: string) => void; onUpdateDraftLine: (patch: Partial<DocumentLine>) => void; onUpdateLine: (id: string, patch: Partial<DocumentLine>) => void; selectedCustomer: Customer | null; selectedLineId: string | null; setSelectedLineId: (id: string) => void; totals: Totals }) {
  const [draggedLineId, setDraggedLineId] = useState<string | null>(null);
  const commercialCount = lines.filter((line) => line.type !== "text").length;
  const textCount = lines.filter((line) => line.type === "text").length;

  function handleDrop(event: DragEvent<HTMLTableRowElement>, targetIndex: number) {
    event.preventDefault();
    if (!draggedLineId) return;
    onReorderLine(draggedLineId, targetIndex);
    setDraggedLineId(null);
    onSetNotice("Linha reordenada por arrastar. Totais preservados.");
  }

  return (
    <div className={`fac-doclab-unified-lines ${candidate ? "fac-doclab-candidate-lines" : ""}`}>
      <section className="fac-doclab-unified-lines-toolbar">
        <div className="fac-doclab-unified-toolbar-main">
          <button className="fac-doclab-unified-back" onClick={onEditHeader} type="button">← Cabecalho</button>
          {!candidate && <span className="fac-doclab-unified-lab">LAB</span>}
          <strong>{document.type}</strong>
          <span>{document.series}</span>
          <span>{selectedCustomer?.name ?? "Cliente por selecionar"}</span>
          <span>{paymentLabel(document.paymentTermId)}</span>
          <span>{document.currency}</span>
          <small>{commercialCount} comerciais · {textCount} texto</small>
        </div>
        <div className="fac-doclab-unified-toolbar-actions">
          <button onClick={() => onInsertTextLine(selectedLineId)} type="button">+ Linha de texto</button>
          {!candidate && <button onClick={() => onSetNotice("Rascunho simulado localmente. Nenhum endpoint foi chamado.")} type="button">Guardar rascunho</button>}
          {!candidate && <button className="primary" onClick={() => onSetNotice("Emissao simulada localmente. Nenhum documento real foi criado.")} type="button">Emitir</button>}
        </div>
      </section>
      <section className="fac-doclab-unified-lines-workspace">
        <div className="fac-doclab-unified-wrap">
          <table className="fac-doclab-unified-table">
            <thead><tr><th></th><th>#</th><th>Artigo</th><th>Descricao</th><th>Qtd.</th><th>Un.</th><th>Arm.</th><th>Preco</th><th>Desc.</th><th>IVA</th><th>Total</th><th></th></tr></thead>
            <tbody>
              {lines.map((line, index) => line.type === "text" ? (
                <TextLineRow index={index} isLast={index === lines.length - 1} isSelected={selectedLineId === line.id} key={line.id} line={line} onConvertLine={onConvertLine} onDragStart={setDraggedLineId} onDrop={handleDrop} onDuplicateLine={onDuplicateLine} onMoveLine={onMoveLine} onRemoveLine={onRemoveLine} onSelect={setSelectedLineId} onUpdateLine={onUpdateLine} />
              ) : (
                <CommercialLineRow index={index} isLast={index === lines.length - 1} isSelected={selectedLineId === line.id} key={line.id} line={line} onChooseArticle={onChooseArticle} onConvertLine={onConvertLine} onDragStart={setDraggedLineId} onDrop={handleDrop} onDuplicateLine={onDuplicateLine} onMoveLine={onMoveLine} onRemoveLine={onRemoveLine} onSelect={setSelectedLineId} onUpdateLine={onUpdateLine} />
              ))}
              <UnifiedDraftRow draftLine={draftLine} index={lines.length} onChooseDraftArticle={onChooseDraftArticle} onCommitDraftLine={onCommitDraftLine} onUpdateDraftLine={onUpdateDraftLine} />
            </tbody>
          </table>
        </div>
      </section>
      <div className="fac-doclab-unified-totals-bar"><OperatorTotalsStrip totals={totals} /></div>
    </div>
  );
}

function CommercialLineRow({ index, isLast, isSelected, line, onChooseArticle, onConvertLine, onDragStart, onDrop, onDuplicateLine, onMoveLine, onRemoveLine, onSelect, onUpdateLine }: { index: number; isLast: boolean; isSelected: boolean; line: DocumentLine; onChooseArticle: (line: DocumentLine, articleId: string | null) => void; onConvertLine: (id: string, type: DocumentLineType) => void; onDragStart: (id: string) => void; onDrop: (event: DragEvent<HTMLTableRowElement>, targetIndex: number) => void; onDuplicateLine: (id?: string | null) => void; onMoveLine: (id: string, direction: -1 | 1) => void; onRemoveLine: (id?: string | null) => void; onSelect: (id: string) => void; onUpdateLine: (id: string, patch: Partial<DocumentLine>) => void }) {
  return (
    <tr className={isSelected ? "selected" : ""} data-document-line data-line-id={line.id} draggable onDragOver={(event) => event.preventDefault()} onDragStart={() => onDragStart(line.id)} onDrop={(event) => onDrop(event, index)} onFocus={() => onSelect(line.id)} onMouseDown={() => onSelect(line.id)} tabIndex={0}>
      <td><button aria-label="Arrastar linha" className="fac-doclab-unified-drag" type="button">::</button></td>
      <td>{index + 1}</td>
      <td><select data-grid-input data-line-id={line.id} onChange={(event) => onChooseArticle(line, event.target.value || null)} value={line.articleId}><option value="">Pesquisar</option>{articles.map((article) => <option key={article.id} value={article.id}>{article.id} - {article.name}</option>)}</select></td>
      <td><input data-grid-input data-line-id={line.id} onChange={(event) => onUpdateLine(line.id, { description: event.target.value })} value={line.description} /></td>
      <td><input data-grid-input data-line-id={line.id} inputMode="decimal" onChange={(event) => onUpdateLine(line.id, { quantity: event.target.value })} value={line.quantity} /></td>
      <td><CompactCodeSelect ariaLabel="Detalhe da unidade" items={unitOptions} onChange={(value) => onUpdateLine(line.id, { unit: value })} selectProps={{ "data-grid-input": true, "data-line-id": line.id }} value={compactUnit(line.unit)} /></td>
      <td><CompactCodeSelect ariaLabel="Detalhe do armazem" items={warehouseCodeOptions} onChange={(value) => onUpdateLine(line.id, { warehouseId: value })} selectProps={{ "data-grid-input": true, "data-line-id": line.id }} value={line.warehouseId} /></td>
      <td><input data-grid-input data-line-id={line.id} inputMode="decimal" onChange={(event) => onUpdateLine(line.id, { price: event.target.value })} value={line.price} /></td>
      <td><input data-grid-input data-line-id={line.id} inputMode="decimal" onChange={(event) => onUpdateLine(line.id, { discount: event.target.value })} value={line.discount} /></td>
      <td><CompactCodeSelect ariaLabel="Detalhe do IVA" items={vatCodeOptions} onChange={(value) => onUpdateLine(line.id, { vatId: value })} selectProps={{ "data-grid-input": true, "data-line-id": line.id }} value={line.vatId} /></td>
      <td className="fac-doclab-money">{formatMoney(lineTotal(line))}</td>
      <td><UnifiedLineActions index={index} isLast={isLast} line={line} onConvertLine={onConvertLine} onDuplicateLine={onDuplicateLine} onMoveLine={onMoveLine} onRemoveLine={onRemoveLine} /></td>
    </tr>
  );
}

function TextLineRow({ index, isLast, isSelected, line, onConvertLine, onDragStart, onDrop, onDuplicateLine, onMoveLine, onRemoveLine, onSelect, onUpdateLine }: { index: number; isLast: boolean; isSelected: boolean; line: DocumentLine; onConvertLine: (id: string, type: DocumentLineType) => void; onDragStart: (id: string) => void; onDrop: (event: DragEvent<HTMLTableRowElement>, targetIndex: number) => void; onDuplicateLine: (id?: string | null) => void; onMoveLine: (id: string, direction: -1 | 1) => void; onRemoveLine: (id?: string | null) => void; onSelect: (id: string) => void; onUpdateLine: (id: string, patch: Partial<DocumentLine>) => void }) {
  return (
    <tr className={`fac-doclab-unified-text-row ${isSelected ? "selected" : ""}`} data-document-line data-line-id={line.id} draggable onDragOver={(event) => event.preventDefault()} onDragStart={() => onDragStart(line.id)} onDrop={(event) => onDrop(event, index)} onFocus={() => onSelect(line.id)} onMouseDown={() => onSelect(line.id)} tabIndex={0}>
      <td><button aria-label="Arrastar linha de texto" className="fac-doclab-unified-drag" type="button">::</button></td>
      <td>{index + 1}</td>
      <td colSpan={9}><div className="fac-doclab-unified-text-cell"><span>Texto</span><textarea aria-label="Descricao da linha de texto" data-grid-input data-line-id={line.id} onChange={(event) => onUpdateLine(line.id, { description: event.target.value })} value={line.description} /></div></td>
      <td><UnifiedLineActions index={index} isLast={isLast} line={line} onConvertLine={onConvertLine} onDuplicateLine={onDuplicateLine} onMoveLine={onMoveLine} onRemoveLine={onRemoveLine} /></td>
    </tr>
  );
}

function UnifiedDraftRow({ draftLine, index, onChooseDraftArticle, onCommitDraftLine, onUpdateDraftLine }: { draftLine: DocumentLine; index: number; onChooseDraftArticle: (articleId: string | null) => void; onCommitDraftLine: () => void; onUpdateDraftLine: (patch: Partial<DocumentLine>) => void }) {
  return (
    <tr className="fac-doclab-unified-draft" data-draft-line data-line-id={draftLine.id}>
      <td></td><td>{index + 1}</td>
      <td><select data-draft-input data-grid-input data-line-id={draftLine.id} onChange={(event) => onChooseDraftArticle(event.target.value || null)} value={draftLine.articleId}><option value="">Pesquisar</option>{articles.map((article) => <option key={article.id} value={article.id}>{article.id} - {article.name}</option>)}</select></td>
      <td><input data-draft-input data-grid-input data-line-id={draftLine.id} onChange={(event) => onUpdateDraftLine({ description: event.target.value })} placeholder="Linha comercial ativa" value={draftLine.description} /></td>
      <td><input data-draft-input data-grid-input data-line-id={draftLine.id} inputMode="decimal" onChange={(event) => onUpdateDraftLine({ quantity: event.target.value })} value={draftLine.quantity} /></td>
      <td><CompactCodeSelect ariaLabel="Detalhe da unidade ativa" items={unitOptions} onChange={(value) => onUpdateDraftLine({ unit: value })} selectProps={{ "data-draft-input": true, "data-grid-input": true, "data-line-id": draftLine.id }} value={compactUnit(draftLine.unit)} /></td>
      <td><CompactCodeSelect ariaLabel="Detalhe do armazem ativo" items={warehouseCodeOptions} onChange={(value) => onUpdateDraftLine({ warehouseId: value })} selectProps={{ "data-draft-input": true, "data-grid-input": true, "data-line-id": draftLine.id }} value={draftLine.warehouseId} /></td>
      <td><input data-draft-input data-grid-input data-line-id={draftLine.id} inputMode="decimal" onChange={(event) => onUpdateDraftLine({ price: event.target.value })} value={draftLine.price} /></td>
      <td><input data-draft-input data-grid-input data-line-id={draftLine.id} inputMode="decimal" onChange={(event) => onUpdateDraftLine({ discount: event.target.value })} value={draftLine.discount} /></td>
      <td><CompactCodeSelect ariaLabel="Detalhe do IVA ativo" items={vatCodeOptions} onChange={(value) => onUpdateDraftLine({ vatId: value })} selectProps={{ "data-draft-input": true, "data-grid-input": true, "data-line-id": draftLine.id }} value={draftLine.vatId} /></td>
      <td className="fac-doclab-money">{formatMoney(lineTotal(draftLine))}</td>
      <td><button aria-label="Concluir linha ativa" className="fac-doclab-unified-ok" onClick={onCommitDraftLine} type="button">OK</button></td>
    </tr>
  );
}

function UnifiedLineActions({ index, isLast, line, onConvertLine, onDuplicateLine, onMoveLine, onRemoveLine }: { index: number; isLast: boolean; line: DocumentLine; onConvertLine: (id: string, type: DocumentLineType) => void; onDuplicateLine: (id?: string | null) => void; onMoveLine: (id: string, direction: -1 | 1) => void; onRemoveLine: (id?: string | null) => void }) {
  return (
    <div className="fac-doclab-unified-row-actions">
      <button aria-label="Subir linha" disabled={index === 0} onClick={() => onMoveLine(line.id, -1)} type="button">↑</button>
      <button aria-label="Descer linha" disabled={isLast} onClick={() => onMoveLine(line.id, 1)} type="button">↓</button>
      <button aria-label="Duplicar linha" onClick={() => onDuplicateLine(line.id)} type="button">Dup.</button>
      <button aria-label={line.type === "text" ? "Converter em comercial" : "Converter em texto"} onClick={() => onConvertLine(line.id, line.type === "text" ? "commercial" : "text")} type="button">{line.type === "text" ? "Com." : "Txt"}</button>
      <button aria-label="Remover linha" onClick={() => onRemoveLine(line.id)} type="button">X</button>
    </div>
  );
}

function CompactCodeSelect({ ariaLabel, items, onChange, selectProps, value }: { ariaLabel: string; items: CompactOption[]; onChange: (value: string) => void; selectProps?: Record<string, string | boolean>; value: string }) {
  const [open, setOpen] = useState(false);
  const selected = items.find((item) => item.value === value) ?? items[0];
  const popoverId = `fac-compact-${selected.value}`;
  function handleKeyDown(event: KeyboardEvent<HTMLButtonElement>) {
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      setOpen((current) => !current);
    }
    if (event.key === "Escape") setOpen(false);
  }
  return (
    <div className="fac-doclab-compact-code">
      <select aria-label={ariaLabel.replace("Detalhe", "Selecionar")} onChange={(event) => onChange(event.target.value)} value={selected.value} {...selectProps}>{items.map((item) => <option key={item.value} value={item.value}>{item.code} - {item.label}</option>)}</select>
      <button aria-controls={popoverId} aria-expanded={open} aria-label={ariaLabel} onClick={() => setOpen((current) => !current)} onKeyDown={handleKeyDown} type="button">{selected.code}</button>
      {open && <div className="fac-doclab-compact-popover" id={popoverId} role="dialog"><strong>{selected.code}</strong><span>{selected.label}</span>{selected.detail && <small>{selected.detail}</small>}</div>}
    </div>
  );
}

function OperatorDocumentWorkspaceDraft(props: Omit<WorkspaceProps, "onChooseCustomer" | "onUpdateDocument">) {
  const { document, draftLine, lines, onChooseArticle, onChooseDraftArticle, onCommitDraftLine, onDuplicateLine, onOpenConditions, onRemoveLine, onUpdateDraftLine, onUpdateLine, selectedCustomer, selectedLineId, setSelectedLineId, totals } = props;
  return (
    <div className="fac-doclab-operator-grid">
      <section className="fac-doclab-operator-top">
        <div className="fac-doclab-operator-strip">
          <strong>{document.type}</strong>
          <span>Serie {document.series}</span>
          <span>{selectedCustomer?.name ?? "Cliente por selecionar"}</span>
          <span>{paymentLabel(document.paymentTermId)}</span>
          <span>{document.currency}</span>
          <span>{warehouseLabel(document.defaultWarehouseId)} para novas linhas</span>
        </div>
        <div className="fac-doclab-operator-actions">
          <FacButton icon="pi pi-sliders-h" label="Editar condicoes" onClick={onOpenConditions} variant="secondary" />
          <FacButton icon="pi pi-save" label="Guardar" onClick={() => undefined} variant="ghost" />
          <FacButton icon="pi pi-check" label="Emitir" onClick={() => undefined} variant="primary" />
        </div>
      </section>
      <section className="fac-doclab-panel fac-doclab-grid-panel">
        <div className="fac-doclab-grid-title">
          <div><p className="fac-eyebrow">Operador</p><h2>Grelha de introducao</h2></div>
          <div className="fac-doclab-shortcuts">Enter avanca - Enter no fim conclui - Ctrl+Enter conclui linha ativa - Ctrl+D duplica - Delete protege - Ctrl+S simula guardar - Esc fecha painel</div>
        </div>
        <DocumentLinesGridDraft draftLine={draftLine} lines={lines} onChooseArticle={onChooseArticle} onChooseDraftArticle={onChooseDraftArticle} onCommitDraftLine={onCommitDraftLine} onDuplicateLine={onDuplicateLine} onRemoveLine={onRemoveLine} onUpdateDraftLine={onUpdateDraftLine} onUpdateLine={onUpdateLine} selectedLineId={selectedLineId} setSelectedLineId={setSelectedLineId} />
      </section>
      <OperatorTotalsStrip totals={totals} />
    </div>
  );
}

function OperatorDocumentWorkspace(props: Omit<WorkspaceProps, "onChooseCustomer" | "onUpdateDocument">) {
  const { document, draftLine, lines, onChooseArticle, onChooseDraftArticle, onCommitDraftLine, onDuplicateLine, onOpenConditions, onRemoveLine, onUpdateDraftLine, onUpdateLine, selectedCustomer, selectedLineId, setSelectedLineId, totals } = props;
  const onAddLine = () => undefined;
  return (
    <div className="fac-doclab-operator-grid">
      <section className="fac-doclab-operator-top"><div className="fac-doclab-operator-strip"><strong>{document.type}</strong><span>Serie {document.series}</span><span>{selectedCustomer?.name ?? "Cliente por selecionar"}</span><span>{paymentLabel(document.paymentTermId)}</span><span>{document.currency}</span><span>{warehouseLabel(document.defaultWarehouseId)} para novas linhas</span></div><div className="fac-doclab-operator-actions"><FacButton icon="pi pi-sliders-h" label="Editar condicoes" onClick={onOpenConditions} variant="secondary" /><FacButton icon="pi pi-save" label="Guardar" onClick={() => undefined} variant="ghost" /><FacButton icon="pi pi-check" label="Emitir" onClick={() => undefined} variant="primary" /></div></section>
      <section className="fac-doclab-panel fac-doclab-grid-panel"><div className="fac-doclab-grid-title"><div><p className="fac-eyebrow">Operador</p><h2>Grelha de introducao</h2></div><div className="fac-doclab-shortcuts">Enter avanca · Ctrl+Enter nova linha · Ctrl+D duplica · Delete protege · Ctrl+S simula guardar · Esc fecha painel</div></div><DocumentLinesGridLite lines={lines} onChooseArticle={onChooseArticle} onDuplicateLine={onDuplicateLine} onRemoveLine={onRemoveLine} onUpdateLine={onUpdateLine} selectedLineId={selectedLineId} setSelectedLineId={setSelectedLineId} /><button className="fac-doclab-empty-row" onClick={onAddLine} type="button">+ Linha vazia pronta a receber dados</button></section>
      <DocumentTotalsBar sticky totals={totals} />
    </div>
  );
}

function DocumentConditionsPanel({ document, onChooseCustomer, onClose, onUpdateDocument, open, selectedCustomer }: { document: DocumentLabState; onChooseCustomer: (customerId: string | null) => void; onClose: () => void; onUpdateDocument: (patch: Partial<DocumentLabState>) => void; open: boolean; selectedCustomer: Customer | null }) {
  return (
    <aside className={`fac-doclab-drawer ${open ? "open" : ""}`} aria-hidden={!open}>
      <div className="fac-doclab-drawer-header"><div><p className="fac-eyebrow">Condicoes</p><h2>Editar condicoes do documento</h2></div><button aria-label="Fechar painel" onClick={onClose} type="button">×</button></div>
      <div className="fac-doclab-drawer-body">
        <FacSelect label="Cliente" onChange={onChooseCustomer} options={customers.map((customer) => ({ label: `${customer.name} · ${customer.nif}`, value: customer.id }))} placeholder="Selecionar cliente" value={document.customerId} />
        <FacSelect label="Moeda" onChange={(value) => onUpdateDocument({ currency: value ?? "EUR" })} options={[{ label: "EUR", value: "EUR" }, { label: "USD", value: "USD" }]} value={document.currency} />
        <FacSelect label="Prazo de pagamento" onChange={(value) => onUpdateDocument({ paymentTermId: value ?? "" })} options={paymentTerms.map((term) => ({ label: term.label, value: term.id }))} value={document.paymentTermId} />
        <FacSelect label="Transporte" onChange={(value) => onUpdateDocument({ transportId: value ?? "" })} options={transports.map((transport) => ({ label: transport.label, value: transport.id }))} value={document.transportId} />
        <FacSelect label="Armazem por defeito para novas linhas" onChange={(value) => onUpdateDocument({ defaultWarehouseId: value ?? warehouses[0].id })} options={warehouses.map((warehouse) => ({ label: warehouse.name, value: warehouse.id }))} value={document.defaultWarehouseId} />
        <label className="fac-doclab-textarea"><span>Morada</span><textarea onChange={(event) => onUpdateDocument({ address: event.target.value })} value={document.address} /></label>
        <label className="fac-doclab-textarea"><span>Observacoes</span><textarea onChange={(event) => onUpdateDocument({ notes: event.target.value })} value={document.notes} /></label>
        <ConditionOrigins document={document} selectedCustomer={selectedCustomer} />
      </div>
    </aside>
  );
}

function ConditionOrigins({ document, selectedCustomer }: { document: DocumentLabState; selectedCustomer: Customer | null }) {
  const items = [
    { label: "Prazo", value: paymentLabel(document.paymentTermId), source: origin(document.paymentTermId, selectedCustomer?.paymentTermId) },
    { label: "Moeda", value: document.currency, source: origin(document.currency, selectedCustomer?.currency) },
    { label: "Transporte", value: transportLabel(document.transportId), source: origin(document.transportId, selectedCustomer?.transportId) },
    { label: "Morada", value: document.address || "-", source: origin(document.address, selectedCustomer?.address) },
    { label: "Armazem", value: warehouseLabel(document.defaultWarehouseId), source: "Documento" as Source }
  ];
  return <div className="fac-doclab-origin-grid">{items.map((item) => <div className="fac-doclab-origin-card" data-source={item.source} key={item.label}><span>{item.label}</span><strong>{item.value}</strong><small>{item.source}</small></div>)}</div>;
}

function DocumentLinesGridDraft({ draftLine, lines, onChooseArticle, onChooseDraftArticle, onCommitDraftLine, onDuplicateLine, onRemoveLine, onUpdateDraftLine, onUpdateLine, selectedLineId, setSelectedLineId }: { draftLine: DocumentLine; lines: DocumentLine[]; onChooseArticle: (line: DocumentLine, articleId: string | null) => void; onChooseDraftArticle: (articleId: string | null) => void; onCommitDraftLine: () => void; onDuplicateLine: (id?: string | null) => void; onRemoveLine: (id?: string | null) => void; onUpdateDraftLine: (patch: Partial<DocumentLine>) => void; onUpdateLine: (id: string, patch: Partial<DocumentLine>) => void; selectedLineId: string | null; setSelectedLineId: (id: string) => void }) {
  return (
    <div className="fac-doclab-lines-wrap">
      <table className="fac-doclab-lines">
        <thead><tr><th>#</th><th>Artigo</th><th>Descricao</th><th>Qtd.</th><th>Un.</th><th>Armazem</th><th>Preco</th><th>Desc.</th><th>IVA</th><th>Total</th><th></th></tr></thead>
        <tbody>
          {lines.map((line, index) => (
            <tr className={selectedLineId === line.id ? "selected" : ""} data-document-line data-line-id={line.id} key={line.id} onFocus={() => setSelectedLineId(line.id)} onMouseDown={() => setSelectedLineId(line.id)}>
              <td>{index + 1}</td>
              <td data-line-field="articleId"><select className="fac-doclab-cell" data-grid-input data-line-id={line.id} onChange={(event) => onChooseArticle(line, event.target.value || null)} value={line.articleId}><option value="">Pesquisar</option>{articles.map((article) => <option key={article.id} value={article.id}>{article.id} - {article.name}</option>)}</select></td>
              <td data-line-field="description"><input data-grid-input data-line-id={line.id} onChange={(event) => onUpdateLine(line.id, { description: event.target.value })} value={line.description} /></td>
              <td data-line-field="quantity"><input data-grid-input data-line-id={line.id} inputMode="decimal" onChange={(event) => onUpdateLine(line.id, { quantity: event.target.value })} value={line.quantity} /></td>
              <td data-line-field="unit"><input data-grid-input data-line-id={line.id} onChange={(event) => onUpdateLine(line.id, { unit: event.target.value })} value={line.unit} /></td>
              <td data-line-field="warehouseId"><select className="fac-doclab-cell fac-doclab-warehouse-select" data-grid-input data-line-id={line.id} onChange={(event) => onUpdateLine(line.id, { warehouseId: event.target.value || warehouses[0].id })} value={line.warehouseId}>{warehouses.map((warehouse) => <option key={warehouse.id} value={warehouse.id}>{warehouse.name}</option>)}</select></td>
              <td data-line-field="price"><input data-grid-input data-line-id={line.id} inputMode="decimal" onChange={(event) => onUpdateLine(line.id, { price: event.target.value })} value={line.price} /></td>
              <td data-line-field="discount"><input data-grid-input data-line-id={line.id} inputMode="decimal" onChange={(event) => onUpdateLine(line.id, { discount: event.target.value })} value={line.discount} /></td>
              <td data-line-field="vatId"><select className="fac-doclab-cell" data-grid-input data-line-id={line.id} onChange={(event) => onUpdateLine(line.id, { vatId: event.target.value })} value={line.vatId}><option value="">IVA</option>{vatRates.map((vat) => <option key={vat.id} value={vat.id}>{vat.label}</option>)}</select></td>
              <td className="fac-doclab-money">{formatMoney(lineTotal(line))}</td>
              <td className="fac-doclab-row-actions"><button aria-label="Duplicar linha" onClick={() => onDuplicateLine(line.id)} type="button">Duplicar</button><button aria-label="Remover linha" onClick={() => onRemoveLine(line.id)} type="button">X</button></td>
            </tr>
          ))}
          <tr className="fac-doclab-draft-row selected" data-draft-line data-line-id={draftLine.id} onFocus={() => setSelectedLineId(draftLine.id)} onMouseDown={() => setSelectedLineId(draftLine.id)}>
            <td>{lines.length + 1}</td>
            <td data-line-field="articleId"><select className="fac-doclab-cell" data-draft-input data-grid-input data-line-id={draftLine.id} onChange={(event) => onChooseDraftArticle(event.target.value || null)} value={draftLine.articleId}><option value="">Pesquisar</option>{articles.map((article) => <option key={article.id} value={article.id}>{article.id} - {article.name}</option>)}</select></td>
            <td data-line-field="description"><input data-draft-input data-grid-input data-line-id={draftLine.id} onChange={(event) => onUpdateDraftLine({ description: event.target.value })} placeholder="Linha ativa" value={draftLine.description} /></td>
            <td data-line-field="quantity"><input data-draft-input data-grid-input data-line-id={draftLine.id} inputMode="decimal" onChange={(event) => onUpdateDraftLine({ quantity: event.target.value })} value={draftLine.quantity} /></td>
            <td data-line-field="unit"><input data-draft-input data-grid-input data-line-id={draftLine.id} onChange={(event) => onUpdateDraftLine({ unit: event.target.value })} value={draftLine.unit} /></td>
            <td data-line-field="warehouseId"><select className="fac-doclab-cell fac-doclab-warehouse-select" data-draft-input data-grid-input data-line-id={draftLine.id} onChange={(event) => onUpdateDraftLine({ warehouseId: event.target.value || warehouses[0].id })} value={draftLine.warehouseId}>{warehouses.map((warehouse) => <option key={warehouse.id} value={warehouse.id}>{warehouse.name}</option>)}</select></td>
            <td data-line-field="price"><input data-draft-input data-grid-input data-line-id={draftLine.id} inputMode="decimal" onChange={(event) => onUpdateDraftLine({ price: event.target.value })} value={draftLine.price} /></td>
            <td data-line-field="discount"><input data-draft-input data-grid-input data-line-id={draftLine.id} inputMode="decimal" onChange={(event) => onUpdateDraftLine({ discount: event.target.value })} value={draftLine.discount} /></td>
            <td data-line-field="vatId"><select className="fac-doclab-cell" data-draft-input data-grid-input data-line-id={draftLine.id} onChange={(event) => onUpdateDraftLine({ vatId: event.target.value })} value={draftLine.vatId}><option value="">IVA</option>{vatRates.map((vat) => <option key={vat.id} value={vat.id}>{vat.label}</option>)}</select></td>
            <td className="fac-doclab-money">{formatMoney(lineTotal(draftLine))}</td>
            <td className="fac-doclab-row-actions"><button aria-label="Concluir linha ativa" onClick={onCommitDraftLine} type="button">OK</button></td>
          </tr>
        </tbody>
      </table>
    </div>
  );
}

function DocumentLinesGridLite(props: { lines: DocumentLine[]; onChooseArticle: (line: DocumentLine, articleId: string | null) => void; onDuplicateLine: (id?: string | null) => void; onRemoveLine: (id?: string | null) => void; onUpdateLine: (id: string, patch: Partial<DocumentLine>) => void; selectedLineId: string | null; setSelectedLineId: (id: string) => void }) {
  return <DocumentLinesGrid {...props} />;
}

function DocumentLinesGrid({ lines, onChooseArticle, onDuplicateLine, onRemoveLine, onUpdateLine, selectedLineId, setSelectedLineId }: { lines: DocumentLine[]; onChooseArticle: (line: DocumentLine, articleId: string | null) => void; onDuplicateLine: (id?: string | null) => void; onRemoveLine: (id?: string | null) => void; onUpdateLine: (id: string, patch: Partial<DocumentLine>) => void; selectedLineId: string | null; setSelectedLineId: (id: string) => void }) {
  if (lines.length === 0) return <div className="fac-doclab-empty">Sem linhas. A experiencia abre sem artigo, preco, quantidade, IVA ou desconto preenchidos automaticamente.</div>;
  return (
    <div className="fac-doclab-lines-wrap">
      <table className="fac-doclab-lines">
        <thead><tr><th>#</th><th>Artigo</th><th>Descricao</th><th>Qtd.</th><th>Un.</th><th>Armazem</th><th>Preco</th><th>Desc.</th><th>IVA</th><th>Total</th><th></th></tr></thead>
        <tbody>
          {lines.map((line, index) => (
            <tr className={selectedLineId === line.id ? "selected" : ""} data-document-line data-line-id={line.id} key={line.id} onFocus={() => setSelectedLineId(line.id)} onMouseDown={() => setSelectedLineId(line.id)}>
              <td>{index + 1}</td>
              <td><FacSelect className="fac-doclab-cell" onChange={(value) => onChooseArticle(line, value)} options={articles.map((article) => ({ label: `${article.id} · ${article.name}`, value: article.id }))} placeholder="Pesquisar" value={line.articleId} /></td>
              <td><input data-grid-input data-line-id={line.id} onChange={(event) => onUpdateLine(line.id, { description: event.target.value })} value={line.description} /></td>
              <td><input data-grid-input data-line-id={line.id} inputMode="decimal" onChange={(event) => onUpdateLine(line.id, { quantity: event.target.value })} value={line.quantity} /></td>
              <td><input data-grid-input data-line-id={line.id} onChange={(event) => onUpdateLine(line.id, { unit: event.target.value })} value={line.unit} /></td>
              <td><FacSelect className="fac-doclab-cell" onChange={(value) => onUpdateLine(line.id, { warehouseId: value ?? warehouses[0].id })} options={warehouses.map((warehouse) => ({ label: warehouse.name, value: warehouse.id }))} value={line.warehouseId} /></td>
              <td><input data-grid-input data-line-id={line.id} inputMode="decimal" onChange={(event) => onUpdateLine(line.id, { price: event.target.value })} value={line.price} /></td>
              <td><input data-grid-input data-line-id={line.id} inputMode="decimal" onChange={(event) => onUpdateLine(line.id, { discount: event.target.value })} value={line.discount} /></td>
              <td><FacSelect className="fac-doclab-cell" onChange={(value) => onUpdateLine(line.id, { vatId: value ?? "" })} options={vatRates.map((vat) => ({ label: vat.label, value: vat.id }))} placeholder="IVA" value={line.vatId} /></td>
              <td className="fac-doclab-money">{formatMoney(lineTotal(line))}</td>
              <td className="fac-doclab-row-actions"><button aria-label="Duplicar linha" onClick={() => onDuplicateLine(line.id)} type="button">⧉</button><button aria-label="Remover linha" onClick={() => onRemoveLine(line.id)} type="button">×</button></td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function DocumentTotalsBar({ sticky = false, totals }: { sticky?: boolean; totals: Totals }) {
  return <aside className={`fac-doclab-totals ${sticky ? "sticky" : ""}`}><div><span>Subtotal</span><strong>{formatMoney(totals.subtotal)}</strong></div><div><span>Descontos</span><strong>{formatMoney(totals.discount)}</strong></div><div><span>IVA</span><strong>{formatMoney(totals.vat)}</strong></div><div className="fac-doclab-total-final"><span>Total provisorio</span><strong>{formatMoney(totals.total)}</strong></div></aside>;
}

function OperatorTotalsStrip({ totals }: { totals: Totals }) {
  return (
    <aside className="fac-doclab-totals-strip" aria-label="Totais provisorios">
      <span>Subtotal <strong>{formatMoney(totals.subtotal)}</strong></span>
      <span>Desc. <strong>{formatMoney(totals.discount)}</strong></span>
      <span>IVA <strong>{formatMoney(totals.vat)}</strong></span>
      <span>Total <strong>{formatMoney(totals.total)}</strong></span>
    </aside>
  );
}

function LabMetrics({ clicks, commercialLineCount, elapsed, filledFields, keyboardActions, lineCount, mode, step, textLineCount, total, transitions }: { clicks: number; commercialLineCount: number; elapsed: number; filledFields: number; keyboardActions: number; lineCount: number; mode: LabMode; step?: WizardStep; textLineCount: number; total: number; transitions: number }) {
  const modeLabel = mode === "complete" ? "Completo atual" : mode === "wizard" ? "Duas fases" : mode === "unified" ? "Unificada" : mode === "candidate" ? "Estrutura candidata" : "Operador";
  const stepLabel = step === "header" ? "Condicoes" : step === "lines" ? "Linhas" : "Unica";
  return <div className="fac-doclab-metrics" aria-label="Metricas do laboratorio"><span>{modeLabel}</span><span>Fase {stepLabel}</span><span>{lineCount} linhas</span><span>{commercialLineCount} comerciais</span><span>{textLineCount} texto</span><span>{filledFields} campos</span><span>{formatMoney(total)}</span><span>{elapsed}s</span><span>{transitions} transicoes</span><span>{keyboardActions} teclas</span><span>{clicks} cliques</span></div>;
}

function calculateTotals(lines: DocumentLine[]): Totals {
  return lines.reduce<Totals>((acc, line) => {
    if (line.type === "text") return acc;
    const base = toNumber(line.quantity) * toNumber(line.price);
    const discount = toNumber(line.discount);
    const taxable = Math.max(base - discount, 0);
    const vat = taxable * (vatRates.find((rate) => rate.id === line.vatId)?.rate ?? 0);
    return { subtotal: acc.subtotal + base, discount: acc.discount + discount, vat: acc.vat + vat, total: acc.total + taxable + vat };
  }, { discount: 0, subtotal: 0, total: 0, vat: 0 });
}

function lineTotal(line: DocumentLine) {
  if (line.type === "text") return 0;
  const taxable = Math.max(toNumber(line.quantity) * toNumber(line.price) - toNumber(line.discount), 0);
  return taxable + taxable * (vatRates.find((rate) => rate.id === line.vatId)?.rate ?? 0);
}

function countFilledFields(document: DocumentLabState, lines: DocumentLine[]) {
  return Object.values(document).filter(Boolean).length + lines.flatMap((line) => Object.values(line)).filter(Boolean).length;
}

function isLineFilled(line: DocumentLine) {
  if (line.type === "text") return Boolean(line.description.trim());
  return [line.articleId, line.description, line.quantity, line.unit, line.price, line.discount, line.vatId].some(Boolean);
}

function resequenceLines(lines: DocumentLine[]) {
  return lines.map((line, index) => ({ ...line, position: index + 1 }));
}

function hasCommercialValues(line: DocumentLine) {
  return [line.articleId, line.quantity, line.unit, line.warehouseId, line.price, line.discount, line.vatId].some(Boolean);
}

function origin(value: string, inherited?: string): Source {
  if (!inherited) return "Documento";
  return value === inherited ? "Cliente" : "Alterado neste documento";
}

function validateWizardHeader(document: DocumentLabState): { field: keyof DocumentLabState; message: string } | null {
  if (!document.type) return { field: "type", message: "Escolha o tipo de documento antes de continuar." };
  if (!document.series) return { field: "series", message: "Escolha a serie antes de continuar." };
  if (!document.date) return { field: "date", message: "Indique a data do documento antes de continuar." };
  if (!document.customerId) return { field: "customerId", message: "Selecione o cliente antes de continuar para as linhas." };
  if (!document.currency) return { field: "currency", message: "Escolha a moeda antes de continuar." };
  if (!document.defaultWarehouseId) return { field: "defaultWarehouseId", message: "Escolha o armazem por defeito para novas linhas." };
  return null;
}

function focusWizardField(field: keyof DocumentLabState) {
  document.querySelector<HTMLElement>(`[data-wizard-field="${field}"]`)?.focus();
}

const paymentLabel = (id: string) => paymentTerms.find((term) => term.id === id)?.label ?? "-";
const transportLabel = (id: string) => transports.find((transport) => transport.id === id)?.label ?? "-";
const warehouseLabel = (id: string) => warehouses.find((warehouse) => warehouse.id === id)?.name ?? "-";

function compactUnit(unit: string) {
  if (unit === "UN") return "UNI";
  if (unit === "KG") return "KGM";
  if (unit === "HR") return "HOR";
  return unit || "UNI";
}

function toNumber(value: string) {
  const number = Number(value.replace(",", "."));
  return Number.isFinite(number) ? number : 0;
}

function formatMoney(value: number) {
  return new Intl.NumberFormat("pt-PT", { currency: "EUR", minimumFractionDigits: 2, maximumFractionDigits: 2, style: "currency" }).format(value);
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("pt-PT").format(new Date(`${value}T00:00:00`));
}

function focusFirstLineField(lineId: string) {
  document.querySelector<HTMLInputElement>(`[data-line-id="${lineId}"][data-grid-input]`)?.focus();
}

function focusDraftLine(lineId?: string) {
  const field = lineId
    ? document.querySelector<HTMLElement>(`[data-line-id="${lineId}"][data-draft-input]`)
    : document.querySelector<HTMLElement>("[data-draft-input]");
  field?.focus();
  field?.closest("tr")?.scrollIntoView({ block: "nearest" });
}

function focusLineRow(lineId: string) {
  document.querySelector<HTMLElement>(`[data-line-id="${lineId}"]`)?.focus();
}

function isLastDraftField(target: HTMLElement) {
  const fields = Array.from(document.querySelectorAll<HTMLElement>("[data-draft-input]"));
  return fields.indexOf(target) === fields.length - 1;
}

function focusNextGridField(target: HTMLElement) {
  const fields = Array.from(document.querySelectorAll<HTMLElement>("[data-grid-input]"));
  const index = fields.indexOf(target);
  (fields[index + 1] ?? fields[0])?.focus();
}
