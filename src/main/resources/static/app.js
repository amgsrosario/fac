const state = {
    currentView: "dashboard",
    filter: "",
    comerciais: [],
    pendentes: [],
    financeiros: [],
    selectedComercialId: null,
    selectedFinanceiroId: null
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
        if (!state.selectedComercialId && comerciais.length > 0) {
            state.selectedComercialId = comerciais[0].id;
        }
        if (!state.selectedFinanceiroId && financeiros.length > 0) {
            state.selectedFinanceiroId = financeiros[0].id;
        }
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
    renderComercialDetail();
    renderFinanceiroDetail();
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
        <tr class="selectable ${documento.id === state.selectedComercialId ? "selected" : ""}" data-comercial-id="${escapeHtml(documento.id)}">
            <td>${escapeHtml(documento.id)}</td>
            <td>${escapeHtml(referencia(documento.tipoDocumentoId, documento.serie, documento.numeroDocumento))}</td>
            <td>${status(documento.anulado ? "ANULADO" : documento.estado, documento.anulado)}</td>
            <td>${escapeHtml(documento.clienteNome || documento.clienteId)}</td>
            <td>${escapeHtml(documento.dataEmissao)}</td>
            <td>${money(documento.valorTotal)} ${escapeHtml(documento.moedaId)}</td>
            <td><a class="action-link" href="/documentos-comerciais/${documento.id}/diagnostico/html" target="_blank" rel="noopener">Diagnostico</a></td>
        </tr>
    `);

    document.querySelectorAll("[data-comercial-id]").forEach((row) => {
        row.addEventListener("click", (event) => {
            if (event.target.closest("a")) {
                return;
            }
            state.selectedComercialId = Number(row.dataset.comercialId);
            render();
        });
    });
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
        <tr class="selectable ${documento.id === state.selectedFinanceiroId ? "selected" : ""}" data-financeiro-id="${escapeHtml(documento.id)}">
            <td>${escapeHtml(documento.id)}</td>
            <td>${escapeHtml(referencia(documento.tipoDocumentoId, documento.serie, documento.numeroDocumento))}</td>
            <td>${escapeHtml(documento.clienteId)}</td>
            <td>${escapeHtml(documento.dataEmissao)}</td>
            <td>${escapeHtml(documento.mPagamentoId)}</td>
            <td>${money(documento.valorPagamentoLiquido)} ${escapeHtml(documento.moedaId)}</td>
            <td><a class="action-link" href="/documentos-financeiros/${documento.id}/diagnostico/html" target="_blank" rel="noopener">Diagnostico</a></td>
        </tr>
    `);

    document.querySelectorAll("[data-financeiro-id]").forEach((row) => {
        row.addEventListener("click", (event) => {
            if (event.target.closest("a")) {
                return;
            }
            state.selectedFinanceiroId = Number(row.dataset.financeiroId);
            render();
        });
    });
}

function renderComercialDetail() {
    const panel = document.querySelector("#comercial-detail");
    const documento = state.comerciais.find((item) => item.id === state.selectedComercialId);
    if (!documento) {
        panel.innerHTML = `
            <p class="eyebrow">Objeto aberto</p>
            <h3>Escolhe um documento comercial</h3>
            <p class="muted">Clica numa linha para ver cabecalho, totais e linhas principais.</p>
        `;
        return;
    }

    panel.innerHTML = `
        <p class="eyebrow">Documento comercial</p>
        <h3>${escapeHtml(referencia(documento.tipoDocumentoId, documento.serie, documento.numeroDocumento))}</h3>
        ${detailLine("Estado", documento.anulado ? "ANULADO" : documento.estado)}
        ${detailLine("Cliente", documento.clienteNome || documento.clienteId)}
        ${detailLine("NIF", documento.clienteNif)}
        ${detailLine("Data", documento.dataEmissao)}
        ${detailLine("Vencimento", documento.dataVencimento)}
        ${detailLine("Moeda", documento.moedaId)}
        ${detailLine("Total", `${money(documento.valorTotal)} ${documento.moedaId || ""}`)}
        ${detailLine("IVA", `${money(documento.valorIvaTotal)} ${documento.moedaId || ""}`)}
        ${detailLine("Liquidado", documento.liquidado ? "Sim" : "Nao")}
        <div class="detail-actions">
            <a class="action-link" href="/documentos-comerciais/${documento.id}/diagnostico/html" target="_blank" rel="noopener">Diagnostico HTML</a>
            <a class="action-link" href="/documentos-comerciais/${documento.id}/diagnostico" target="_blank" rel="noopener">JSON</a>
        </div>
        <h4>Linhas</h4>
        ${renderComercialLines(documento.linhas || [])}
    `;
}

function renderFinanceiroDetail() {
    const panel = document.querySelector("#financeiro-detail");
    const documento = state.financeiros.find((item) => item.id === state.selectedFinanceiroId);
    if (!documento) {
        panel.innerHTML = `
            <p class="eyebrow">Objeto aberto</p>
            <h3>Escolhe um documento financeiro</h3>
            <p class="muted">Clica numa linha para ver pagamento, linhas liquidadas e diagnostico.</p>
        `;
        return;
    }

    panel.innerHTML = `
        <p class="eyebrow">Documento financeiro</p>
        <h3>${escapeHtml(referencia(documento.tipoDocumentoId, documento.serie, documento.numeroDocumento))}</h3>
        ${detailLine("Cliente", documento.clienteId)}
        ${detailLine("Data", documento.dataEmissao)}
        ${detailLine("Moeda", documento.moedaId)}
        ${detailLine("Modo pagamento", documento.mPagamentoId)}
        ${detailLine("Bruto", `${money(documento.valorPagamentoBruto)} ${documento.moedaId || ""}`)}
        ${detailLine("Desconto", `${money(documento.valorDescontoFinanceiro)} ${documento.moedaId || ""}`)}
        ${detailLine("Liquido", `${money(documento.valorPagamentoLiquido)} ${documento.moedaId || ""}`)}
        ${detailLine("Anulado", documento.anulado ? "Sim" : "Nao")}
        <div class="detail-actions">
            <a class="action-link" href="/documentos-financeiros/${documento.id}/diagnostico/html" target="_blank" rel="noopener">Diagnostico HTML</a>
            <a class="action-link" href="/documentos-financeiros/${documento.id}/diagnostico" target="_blank" rel="noopener">JSON</a>
        </div>
        <h4>Linhas</h4>
        ${renderFinanceiroLines(documento.linhas || [])}
    `;
}

function renderComercialLines(linhas) {
    if (linhas.length === 0) {
        return `<p class="muted">Sem linhas.</p>`;
    }
    return `<div class="line-list">${linhas.map((linha) => `
        <div class="line-item">
            <strong>${escapeHtml(linha.numeroLinha)}. ${escapeHtml(linha.descricao)}</strong>
            <span>${escapeHtml(linha.artigoId)} | Qtd ${money(linha.quantidade)} | Linha ${money(linha.valorLinha)}</span>
        </div>
    `).join("")}</div>`;
}

function renderFinanceiroLines(linhas) {
    if (linhas.length === 0) {
        return `<p class="muted">Sem linhas.</p>`;
    }
    return `<div class="line-list">${linhas.map((linha) => `
        <div class="line-item">
            <strong>${escapeHtml(linha.tipoDocumentoId)} ${escapeHtml(linha.serieDocumento)}/${escapeHtml(linha.numeroDocumento)}</strong>
            <span>A liquidar ${money(linha.valorALiquidar)} | Novo pendente ${money(linha.novoValorPendente)}</span>
        </div>
    `).join("")}</div>`;
}

function detailLine(label, value) {
    return `
        <div class="detail-line">
            <span>${escapeHtml(label)}</span>
            <span>${escapeHtml(value)}</span>
        </div>
    `;
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
