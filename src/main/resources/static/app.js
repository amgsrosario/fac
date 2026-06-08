const state = {
    currentView: "dashboard",
    filter: "",
    comerciais: [],
    pendentes: [],
    financeiros: []
};

const views = {
    dashboard: document.querySelector("#dashboard-view"),
    comerciais: document.querySelector("#comerciais-view"),
    pendentes: document.querySelector("#pendentes-view"),
    financeiros: document.querySelector("#financeiros-view")
};

const titles = {
    dashboard: "Dashboard",
    comerciais: "Documentos comerciais",
    pendentes: "Pendentes",
    financeiros: "Documentos financeiros"
};

document.querySelectorAll("[data-view]").forEach((button) => {
    button.addEventListener("click", () => setView(button.dataset.view));
});

document.querySelector("#refresh-button").addEventListener("click", loadAll);
document.querySelector("#global-filter").addEventListener("input", (event) => {
    state.filter = event.target.value.trim().toLowerCase();
    render();
});

loadAll();

async function loadAll() {
    showMessage("");
    try {
        const [comerciais, pendentes, financeiros] = await Promise.all([
            fetchPage("/documentos-comerciais?size=25&sort=id,desc"),
            fetchPage("/pendentes?size=25&sort=id,desc"),
            fetchPage("/documentos-financeiros?size=25&sort=id,desc")
        ]);
        state.comerciais = comerciais;
        state.pendentes = pendentes;
        state.financeiros = financeiros;
        render();
    } catch (error) {
        showMessage("Nao foi possivel carregar dados do backend. Confirma se a aplicacao Spring Boot esta arrancada.");
    }
}

async function fetchPage(url) {
    const response = await fetch(url);
    if (!response.ok) {
        throw new Error(`Erro HTTP ${response.status}`);
    }
    const page = await response.json();
    return page.content || [];
}

function setView(view) {
    state.currentView = view;
    document.querySelector("#screen-title").textContent = titles[view];
    document.querySelectorAll("[data-view]").forEach((button) => {
        button.classList.toggle("active", button.dataset.view === view);
    });
    Object.entries(views).forEach(([key, element]) => {
        element.classList.toggle("active", key === view);
    });
}

function render() {
    document.querySelector("#metric-comerciais").textContent = state.comerciais.length;
    document.querySelector("#metric-pendentes").textContent = state.pendentes.length;
    document.querySelector("#metric-financeiros").textContent = state.financeiros.length;
    renderComerciais();
    renderPendentes();
    renderFinanceiros();
}

function renderComerciais() {
    const rows = filtered(state.comerciais, (documento) => [
        documento.id,
        referencia(documento.tipoDocumentoId, documento.serie, documento.numeroDocumento),
        documento.estado,
        documento.clienteNome,
        documento.valorTotal
    ]);

    renderRows("#comerciais-body", rows, (documento) => `
        <tr>
            <td>${escapeHtml(documento.id)}</td>
            <td>${escapeHtml(referencia(documento.tipoDocumentoId, documento.serie, documento.numeroDocumento))}</td>
            <td>${status(documento.anulado ? "ANULADO" : documento.estado, documento.anulado)}</td>
            <td>${escapeHtml(documento.clienteNome || documento.clienteId)}</td>
            <td>${escapeHtml(documento.dataEmissao)}</td>
            <td>${money(documento.valorTotal)} ${escapeHtml(documento.moedaId)}</td>
            <td><a class="action-link" href="/documentos-comerciais/${documento.id}/diagnostico/html" target="_blank" rel="noopener">Diagnostico</a></td>
        </tr>
    `);
}

function renderPendentes() {
    const rows = filtered(state.pendentes, (pendente) => [
        pendente.id,
        referencia(pendente.tipoDocumentoId, pendente.serieDocumento, pendente.numeroDocumento),
        pendente.clienteId,
        pendente.valorPendente
    ]);

    renderRows("#pendentes-body", rows, (pendente) => `
        <tr>
            <td>${escapeHtml(pendente.id)}</td>
            <td>${escapeHtml(referencia(pendente.tipoDocumentoId, pendente.serieDocumento, pendente.numeroDocumento))}</td>
            <td>${escapeHtml(pendente.clienteId)}</td>
            <td>${escapeHtml(pendente.dataDocumento)}</td>
            <td>${escapeHtml(pendente.dataVencimento)}</td>
            <td>${money(pendente.valorDocumento)} ${escapeHtml(pendente.moedaId)}</td>
            <td>${money(pendente.valorPendente)} ${escapeHtml(pendente.moedaId)}</td>
        </tr>
    `);
}

function renderFinanceiros() {
    const rows = filtered(state.financeiros, (documento) => [
        documento.id,
        referencia(documento.tipoDocumentoId, documento.serie, documento.numeroDocumento),
        documento.clienteId,
        documento.valorPagamentoLiquido
    ]);

    renderRows("#financeiros-body", rows, (documento) => `
        <tr>
            <td>${escapeHtml(documento.id)}</td>
            <td>${escapeHtml(referencia(documento.tipoDocumentoId, documento.serie, documento.numeroDocumento))}</td>
            <td>${escapeHtml(documento.clienteId)}</td>
            <td>${escapeHtml(documento.dataEmissao)}</td>
            <td>${escapeHtml(documento.mPagamentoId)}</td>
            <td>${money(documento.valorPagamentoLiquido)} ${escapeHtml(documento.moedaId)}</td>
            <td><a class="action-link" href="/documentos-financeiros/${documento.id}/diagnostico/html" target="_blank" rel="noopener">Diagnostico</a></td>
        </tr>
    `);
}

function renderRows(selector, rows, template) {
    const body = document.querySelector(selector);
    if (rows.length === 0) {
        body.innerHTML = `<tr><td class="empty" colspan="8">Sem registos para mostrar.</td></tr>`;
        return;
    }
    body.innerHTML = rows.map(template).join("");
}

function filtered(items, fields) {
    if (!state.filter) {
        return items;
    }
    return items.filter((item) => fields(item).some((field) => String(field ?? "").toLowerCase().includes(state.filter)));
}

function referencia(tipo, serie, numero) {
    return `${tipo || ""} ${serie || ""}/${numero ?? "rascunho"}`.trim();
}

function money(value) {
    const number = Number(value || 0);
    return number.toLocaleString("pt-PT", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 6
    });
}

function status(label, danger) {
    return `<span class="status ${danger ? "danger" : ""}">${escapeHtml(label)}</span>`;
}

function showMessage(message) {
    const element = document.querySelector("#message");
    element.textContent = message;
    element.classList.toggle("hidden", !message);
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}
