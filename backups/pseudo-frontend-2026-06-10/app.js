const state = {
    currentView: "dashboard",
    filter: "",
    comerciais: [],
    pendentes: [],
    financeiros: [],
    mpagamentos: [],
    tiposDocumento: [],
    series: [],
    financeiroDiagnosticos: {},
    contaCorrenteDiagnosticos: {},
    contaCorrenteEstado: "TODOS",
    selectedComercialId: null,
    selectedPendenteId: null,
    selectedFinanceiroId: null
};

const views = {
    dashboard: document.querySelector("#dashboard-view"),
    comerciais: document.querySelector("#comerciais-view"),
    pendentes: document.querySelector("#pendentes-view"),
    financeiros: document.querySelector("#financeiros-view"),
    contaCorrente: document.querySelector("#conta-corrente-view")
};

const titles = {
    dashboard: "Dashboard",
    comerciais: "Documentos comerciais",
    pendentes: "Pendentes",
    financeiros: "Documentos financeiros",
    contaCorrente: "Conta corrente"
};

document.querySelectorAll("[data-view]").forEach((button) => {
    button.addEventListener("click", () => setView(button.dataset.view));
});

document.querySelector("#refresh-button").addEventListener("click", loadAll);
document.querySelector("#global-filter").addEventListener("input", (event) => {
    state.filter = event.target.value.trim().toLowerCase();
    render();
});
document.querySelector("#conta-corrente-estado")?.addEventListener("change", (event) => {
    state.contaCorrenteEstado = event.target.value;
    renderContaCorrente();
});

loadAll();

async function loadAll() {
    showMessage("");
    try {
        const [comerciais, pendentes, financeiros, mpagamentos, tiposDocumento, series] = await Promise.all([
            fetchAllPages("/documentos-comerciais?size=100&sort=id,desc"),
            fetchAllPages("/pendentes?size=100&sort=id,desc"),
            fetchAllPages("/documentos-financeiros?size=100&sort=id,desc"),
            fetchAllPages("/mpagamentos?size=100&sort=id,asc"),
            fetchAllPages("/tipos-documento?size=100&sort=id,asc"),
            fetchAllPages("/series?size=100")
        ]);
        state.comerciais = comerciais;
        state.pendentes = pendentes;
        state.financeiros = financeiros;
        state.mpagamentos = mpagamentos;
        state.tiposDocumento = tiposDocumento;
        state.series = series;
        if (!state.selectedComercialId && comerciais.length > 0) {
            state.selectedComercialId = comerciais[0].id;
        }
        if (!state.selectedPendenteId && pendentes.length > 0) {
            state.selectedPendenteId = pendentes[0].id;
        }
        if (!state.selectedFinanceiroId && financeiros.length > 0) {
            state.selectedFinanceiroId = financeiros[0].id;
        }
        render();
    } catch (error) {
        showMessage("Nao foi possivel carregar dados do backend. Confirma se a aplicacao Spring Boot esta arrancada.");
    }
}

async function fetchAllPages(url, maxPages = 20) {
    const items = [];
    let pageNumber = 0;
    let totalPages = 1;

    while (pageNumber < totalPages && pageNumber < maxPages) {
        const page = await fetchPage(withPage(url, pageNumber));
        items.push(...(page.content || []));
        totalPages = Number(page.totalPages || 1);
        pageNumber += 1;
    }

    return items;
}

async function fetchPage(url) {
    const response = await fetch(url);
    if (!response.ok) {
        throw new Error(`Erro HTTP ${response.status}`);
    }
    return response.json();
}

function withPage(url, pageNumber) {
    const parsed = new URL(url, window.location.origin);
    parsed.searchParams.set("page", pageNumber);
    return `${parsed.pathname}${parsed.search}`;
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
    renderPendenteDetail();
    renderFinanceiroDetail();
    renderContaCorrente();
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
        <tr class="selectable ${pendente.id === state.selectedPendenteId ? "selected" : ""}" data-pendente-id="${escapeHtml(pendente.id)}">
            <td>${escapeHtml(pendente.id)}</td>
            <td>${escapeHtml(referencia(pendente.tipoDocumentoId, pendente.serieDocumento, pendente.numeroDocumento))}</td>
            <td>${pendenteStatus(pendente)}</td>
            <td>${escapeHtml(pendente.clienteId)}</td>
            <td>${escapeHtml(pendente.dataDocumento)}</td>
            <td>${escapeHtml(pendente.dataVencimento)}</td>
            <td>${money(pendente.valorDocumento)} ${escapeHtml(pendente.moedaId)}</td>
            <td>${money(pendente.valorPendente)} ${escapeHtml(pendente.moedaId)}</td>
        </tr>
    `);

    document.querySelectorAll("[data-pendente-id]").forEach((row) => {
        row.addEventListener("click", () => {
            state.selectedPendenteId = Number(row.dataset.pendenteId);
            render();
        });
    });
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
            <td>${status(documento.anulado ? "ANULADO" : "EMITIDO", documento.anulado ? "danger" : "success")}</td>
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
            ensureFinanceiroDiagnostico(state.selectedFinanceiroId);
        });
    });
}

function renderContaCorrente() {
    const body = document.querySelector("#conta-corrente-body");
    if (!body) {
        return;
    }
    const clientes = buildContaCorrente();
    renderContaCorrenteResumo(clientes);
    const rows = filtered(filterContaCorrenteClientes(clientes), (cliente) => [
        cliente.clienteId,
        cliente.nome,
        cliente.totalDocumento,
        cliente.totalPendente,
        cliente.documentos.map((documento) => documento.referencia).join(" ")
    ]);

    if (rows.length === 0) {
        body.innerHTML = `<div class="table-card empty account-empty">Sem movimentos para mostrar.</div>`;
        return;
    }

    body.innerHTML = rows.map((cliente) => `
        <article class="account-card">
            <header class="account-header">
                <div>
                    <p class="eyebrow">Cliente ${escapeHtml(cliente.clienteId)}</p>
                    <h3>${escapeHtml(cliente.nome || cliente.clienteId)}</h3>
                </div>
                <div class="account-totals">
                    <span>Total documentos: <strong>${money(cliente.totalDocumento)} EUR</strong></span>
                    <span>Pendente: <strong>${money(cliente.totalPendente)} EUR</strong></span>
                    <button class="action-link secondary-action" type="button" data-conta-diagnostico="${escapeHtml(cliente.clienteId)}">Diagnostico backend</button>
                </div>
            </header>
            <div class="account-documents">
                ${cliente.documentos.map(renderContaCorrenteDocumento).join("")}
            </div>
        </article>
    `).join("");

    document.querySelectorAll("[data-conta-diagnostico]").forEach((button) => {
        button.addEventListener("click", () => ensureContaCorrenteDiagnostico(Number(button.dataset.contaDiagnostico)));
    });
}

function renderContaCorrenteResumo(clientes) {
    const resumo = contaCorrenteResumo(clientes);
    setText("#cc-total-aberto", resumoContaCorrenteTexto(resumo.ABERTO));
    setText("#cc-total-parcial", resumoContaCorrenteTexto(resumo.PARCIAL));
    setText("#cc-total-vencido", resumoContaCorrenteTexto(resumo.VENCIDO));
    setText("#cc-total-liquidado", resumoContaCorrenteTexto(resumo.LIQUIDADO));
}

function contaCorrenteResumo(clientes) {
    const resumo = {
        ABERTO: { documentos: 0, saldo: 0 },
        PARCIAL: { documentos: 0, saldo: 0 },
        VENCIDO: { documentos: 0, saldo: 0 },
        LIQUIDADO: { documentos: 0, saldo: 0 }
    };
    clientes.flatMap((cliente) => cliente.documentos).forEach((documento) => {
        const item = resumo[documento.estadoKey] || resumo.ABERTO;
        item.documentos += 1;
        item.saldo += documento.valorPendente;
    });
    return resumo;
}

function resumoContaCorrenteTexto(item) {
    return `${item.documentos} docs | ${money(item.saldo)} EUR`;
}

function filterContaCorrenteClientes(clientes) {
    if (state.contaCorrenteEstado === "TODOS") {
        return clientes;
    }
    return clientes
        .map((cliente) => recalculateContaCorrenteCliente({
            ...cliente,
            documentos: cliente.documentos.filter((documento) => documento.estadoKey === state.contaCorrenteEstado)
        }))
        .filter((cliente) => cliente.documentos.length > 0);
}

function recalculateContaCorrenteCliente(cliente) {
    return {
        ...cliente,
        totalDocumento: cliente.documentos.reduce((total, documento) => total + documento.valorDocumento, 0),
        totalPendente: cliente.documentos.reduce((total, documento) => total + documento.valorPendente, 0)
    };
}

function renderContaCorrenteDocumento(documento) {
    return `
        <div class="account-document">
            <div class="account-document-main">
                <div>
                    <strong>${escapeHtml(documento.referencia)}</strong>
                    <span>${escapeHtml(documento.dataDocumento)} | Vence ${escapeHtml(documento.dataVencimento || "")}</span>
                </div>
                <div class="account-values">
                    <span>Documento ${money(documento.valorDocumento)} ${escapeHtml(documento.moedaId)}</span>
                    <span>Recebido ${money(documento.valorRecebidoAtivo)} ${escapeHtml(documento.moedaId)}</span>
                    <span>Pendente ${money(documento.valorPendente)} ${escapeHtml(documento.moedaId)}</span>
                    ${pendenteStatus(documento)}
                </div>
            </div>
            <div class="account-balance">
                <span>Original <strong>${money(documento.valorDocumento)} ${escapeHtml(documento.moedaId)}</strong></span>
                <span>Recebido ativo <strong>${money(documento.valorRecebidoAtivo)} ${escapeHtml(documento.moedaId)}</strong></span>
                <span>Anulado historico <strong>${money(documento.valorRecebidoAnulado)} ${escapeHtml(documento.moedaId)}</strong></span>
                <span>Saldo <strong>${money(documento.valorPendente)} ${escapeHtml(documento.moedaId)}</strong></span>
            </div>
            <div class="account-movements">
                ${documento.movimentos.length === 0
                    ? `<p class="muted">Sem recebimentos associados.</p>`
                    : documento.movimentos.map(renderContaCorrenteMovimento).join("")}
            </div>
        </div>
    `;
}

function renderContaCorrenteMovimento(movimento) {
    return `
        <div class="account-movement ${movimento.anulado ? "cancelled" : ""}">
            <span>${escapeHtml(movimento.referencia)} | ${escapeHtml(movimento.dataEmissao)}</span>
            <strong>${money(movimento.valor)} ${escapeHtml(movimento.moedaId)}</strong>
            ${status(movimento.anulado ? "ANULADO" : "ATIVO", movimento.anulado ? "danger" : "success")}
        </div>
    `;
}

function buildContaCorrente() {
    const documentos = state.pendentes.map((pendente) => {
        const comercial = state.comerciais.find((documento) => documento.id === pendente.documentoComercialId);
        const estado = pendenteStatusInfo(pendente);
        const movimentos = movimentosDoPendente(pendente.id);
        const valoresMovimento = movimentosResumo(movimentos);
        return {
            clienteId: pendente.clienteId,
            nome: comercial?.clienteNome,
            referencia: referencia(pendente.tipoDocumentoId, pendente.serieDocumento, pendente.numeroDocumento),
            estadoKey: estado.key,
            dataDocumento: pendente.dataDocumento,
            dataVencimento: pendente.dataVencimento,
            moedaId: pendente.moedaId,
            valorDocumento: Number(pendente.valorDocumento || 0),
            valorPendente: Number(pendente.valorPendente || 0),
            valorRecebidoAtivo: valoresMovimento.ativo,
            valorRecebidoAnulado: valoresMovimento.anulado,
            movimentos
        };
    });

    return Object.values(documentos.reduce((acc, documento) => {
        const key = documento.clienteId;
        if (!acc[key]) {
            acc[key] = {
                clienteId: documento.clienteId,
                nome: documento.nome,
                totalDocumento: 0,
                totalPendente: 0,
                documentos: []
            };
        }
        acc[key].totalDocumento += documento.valorDocumento;
        acc[key].totalPendente += documento.valorPendente;
        acc[key].documentos.push(documento);
        return acc;
    }, {}));
}

function movimentosDoPendente(pendenteId) {
    return state.financeiros
        .flatMap((documento) => (documento.linhas || [])
            .filter((linha) => linha.pendenteId === pendenteId)
            .map((linha) => ({
                referencia: referencia(documento.tipoDocumentoId, documento.serie, documento.numeroDocumento),
                dataEmissao: documento.dataEmissao,
                valor: Number(linha.valorALiquidar || 0),
                moedaId: linha.moedaId || documento.moedaId,
                anulado: documento.anulado
            })))
        .sort((left, right) => String(left.dataEmissao).localeCompare(String(right.dataEmissao)));
}

function movimentosResumo(movimentos) {
    return movimentos.reduce((acc, movimento) => {
        if (movimento.anulado) {
            acc.anulado += movimento.valor;
        } else {
            acc.ativo += movimento.valor;
        }
        return acc;
    }, { ativo: 0, anulado: 0 });
}

async function ensureContaCorrenteDiagnostico(clienteId) {
    state.contaCorrenteDiagnosticos[clienteId] = { loading: true };
    renderContaCorrenteDiagnostico(clienteId);
    try {
        state.contaCorrenteDiagnosticos[clienteId] = await fetchJson(`/pendentes/conta-corrente/clientes/${clienteId}/diagnostico`);
    } catch (error) {
        state.contaCorrenteDiagnosticos[clienteId] = {
            error: `Nao foi possivel carregar diagnostico: ${error.message}`
        };
    }
    renderContaCorrenteDiagnostico(clienteId);
}

function renderContaCorrenteDiagnostico(clienteId) {
    const panel = document.querySelector("#conta-corrente-diagnostico");
    if (!panel) {
        return;
    }
    const diagnostico = state.contaCorrenteDiagnosticos[clienteId];
    panel.classList.remove("hidden");
    if (!diagnostico || diagnostico.loading) {
        panel.innerHTML = `
            <p class="eyebrow">Diagnostico backend</p>
            <h3>Cliente ${escapeHtml(clienteId)}</h3>
            <p class="muted">A carregar conta corrente calculada no backend...</p>
        `;
        return;
    }
    if (diagnostico.error) {
        panel.innerHTML = `
            <p class="eyebrow">Diagnostico backend</p>
            <h3>Cliente ${escapeHtml(clienteId)}</h3>
            <p class="form-feedback error">${escapeHtml(diagnostico.error)}</p>
        `;
        return;
    }
    panel.innerHTML = `
        <div class="account-diagnostic-header">
            <div>
                <p class="eyebrow">Diagnostico backend</p>
                <h3>${escapeHtml(diagnostico.clienteNome || diagnostico.clienteId)}</h3>
            </div>
            <button class="action-link secondary-action" type="button" data-close-conta-diagnostico>Fechar</button>
        </div>
        ${renderContaCorrenteDiagnosticoAlertas(diagnostico.alertas || [])}
        <div class="account-diagnostic-totals">
            ${(diagnostico.totais || []).map(renderContaCorrenteDiagnosticoTotal).join("")}
        </div>
        <div class="account-diagnostic-documents">
            ${(diagnostico.documentos || []).map(renderContaCorrenteDiagnosticoDocumento).join("")}
        </div>
    `;
    panel.querySelector("[data-close-conta-diagnostico]")?.addEventListener("click", () => {
        panel.classList.add("hidden");
        panel.innerHTML = "";
    });
}

function renderContaCorrenteDiagnosticoAlertas(alertas) {
    if (alertas.length === 0) {
        return `<p class="status success">Sem alertas no diagnostico backend</p>`;
    }
    return `<ul class="warning-list">${alertas.map((alerta) => `<li>${escapeHtml(alerta)}</li>`).join("")}</ul>`;
}

function renderContaCorrenteDiagnosticoTotal(total) {
    return `
        <article class="summary-card">
            <span>${escapeHtml(total.moedaId)}</span>
            <strong>${money(total.valorPendente)} pendente</strong>
            <p class="muted">${escapeHtml(total.documentos)} docs | ${escapeHtml(total.vencidos)} vencidos</p>
            <p class="muted">Recebido ativo ${money(total.valorRecebidoAtivo)} | Anulado ${money(total.valorRecebidoAnulado)}</p>
        </article>
    `;
}

function renderContaCorrenteDiagnosticoDocumento(documento) {
    return `
        <div class="line-item">
            <strong>${escapeHtml(referencia(documento.tipoDocumentoId, documento.serie, documento.numeroDocumento))}</strong>
            <span>${escapeHtml(documento.estado)} | ${escapeHtml(documento.dataDocumento)} | Vence ${escapeHtml(documento.dataVencimento)}</span>
            <span>Original ${money(documento.valorDocumento)} | Recebido ${money(documento.valorRecebidoAtivo)} | Anulado ${money(documento.valorRecebidoAnulado)} | Pendente ${money(documento.valorPendente)} ${escapeHtml(documento.moedaId)}</span>
            <span>${escapeHtml((documento.movimentos || []).length)} movimentos financeiros</span>
        </div>
    `;
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

    const diagnostico = state.financeiroDiagnosticos[documento.id];
    const podeAnular = diagnostico ? diagnostico.podeAnular && !documento.anulado : !documento.anulado;
    const botaoAnular = documento.anulado
        ? "Documento anulado"
        : podeAnular
            ? "Anular documento financeiro"
            : "Anulacao bloqueada";

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
            <button class="action-link danger-action" type="button" data-anular-financeiro="${escapeHtml(documento.id)}" ${podeAnular ? "" : "disabled"}>${escapeHtml(botaoAnular)}</button>
            <a class="action-link" href="/documentos-financeiros/${documento.id}/diagnostico/html" target="_blank" rel="noopener">Diagnostico HTML</a>
            <a class="action-link" href="/documentos-financeiros/${documento.id}/diagnostico" target="_blank" rel="noopener">JSON</a>
        </div>
        ${renderFinanceiroDiagnosticoResumo(diagnostico)}
        <h4>Linhas</h4>
        ${renderFinanceiroLines(documento.linhas || [])}
    `;

    if (podeAnular) {
        panel.querySelector("[data-anular-financeiro]")?.addEventListener("click", () => anularDocumentoFinanceiro(documento));
    }
    ensureFinanceiroDiagnostico(documento.id);
}

function renderPendenteDetail() {
    const panel = document.querySelector("#pendente-detail");
    const pendente = state.pendentes.find((item) => item.id === state.selectedPendenteId);
    if (!pendente) {
        panel.innerHTML = `
            <p class="eyebrow">Objeto aberto</p>
            <h3>Escolhe um pendente</h3>
            <p class="muted">Clica numa linha para ver estado, valores e documento de origem.</p>
        `;
        return;
    }

    const documento = state.comerciais.find((item) => item.id === pendente.documentoComercialId);
    const temSaldo = Number(pendente.valorPendente || 0) > 0;
    panel.innerHTML = `
        <p class="eyebrow">Pendente</p>
        <h3>${escapeHtml(referencia(pendente.tipoDocumentoId, pendente.serieDocumento, pendente.numeroDocumento))}</h3>
        ${detailLine("Estado", pendenteStatusLabel(pendente))}
        ${detailLine("Cliente", pendente.clienteId)}
        ${detailLine("Data documento", pendente.dataDocumento)}
        ${detailLine("Vencimento", pendente.dataVencimento)}
        ${detailLine("Moeda", pendente.moedaId)}
        ${detailLine("Valor documento", `${money(pendente.valorDocumento)} ${pendente.moedaId || ""}`)}
        ${detailLine("Valor pendente", `${money(pendente.valorPendente)} ${pendente.moedaId || ""}`)}
        ${detailLine("Valor liquidado", `${money(valorLiquidado(pendente))} ${pendente.moedaId || ""}`)}
        <div class="detail-actions">
            <button class="action-link" type="button" data-prepare-receipt="${escapeHtml(pendente.id)}" ${temSaldo ? "" : "disabled"}>${temSaldo ? "Preparar recebimento" : "Pendente liquidado"}</button>
            <a class="action-link" href="/documentos-comerciais/${pendente.documentoComercialId}/diagnostico/html" target="_blank" rel="noopener">Diagnostico origem</a>
            <a class="action-link" href="/documentos-comerciais/${pendente.documentoComercialId}/diagnostico" target="_blank" rel="noopener">JSON origem</a>
        </div>
        <div id="recebimento-preview" class="preview-card hidden"></div>
        <h4>Documento de origem</h4>
        ${documento ? renderOrigemComercial(documento) : `<p class="muted">Documento comercial nao carregado nesta pagina.</p>`}
    `;

    const button = panel.querySelector("[data-prepare-receipt]");
    if (temSaldo) {
        button.addEventListener("click", () => renderRecebimentoPreview(pendente, documento));
    }
}

function renderRecebimentoPreview(pendente, documento) {
    const preview = document.querySelector("#recebimento-preview");
    const tipoFinanceiro = defaultTipoFinanceiro();
    const serieFinanceira = defaultSerieFinanceira(tipoFinanceiro?.id);
    const modoPagamento = defaultMPagamento(documento);
    const dataMinima = dataMinimaFinanceira(tipoFinanceiro?.id, serieFinanceira?.serie);
    const dataSugerida = maxDate(todayIso(), dataMinima);
    const valorSugerido = Number(pendente.valorPendente || 0);
    const bloqueios = recebimentoBloqueios(pendente, documento, tipoFinanceiro, serieFinanceira, modoPagamento);

    preview.innerHTML = `
        <p class="eyebrow">Revisao de recebimento</p>
        <h4>Antes de emitir documento financeiro</h4>
        <div class="form-grid">
            ${formField("Cliente", `<input type="text" value="${escapeHtml(pendente.clienteId)}" disabled>`)}
            ${formField("Data emissao", `<input id="recebimento-data" type="date" value="${escapeHtml(dataSugerida)}" min="${escapeHtml(dataMinima || "")}">`)}
            ${formField("Tipo financeiro", select("recebimento-tipo", tiposFinanceiros(), tipoFinanceiro?.id, "id", "descricao"))}
            ${formField("Serie", select("recebimento-serie", seriesFinanceiras(tipoFinanceiro?.id), serieFinanceira?.serie, "serie", "nome"))}
            ${formField("Modo pagamento", select("recebimento-mpagamento", state.mpagamentos, modoPagamento?.id, "id", "nome"))}
            ${formField("Emissor", `<input id="recebimento-emissor" type="text" value="DEMO" maxlength="20">`)}
            ${formField("Valor a liquidar", `<input id="recebimento-valor" type="number" min="0.000001" step="0.000001" value="${valorSugerido.toFixed(6)}">`)}
            ${formField("Desconto valor", `<input id="recebimento-desconto" type="number" min="0" step="0.000001" value="0.000000">`)}
        </div>
        <p id="recebimento-data-regra" class="muted">${renderDataMinimaFinanceira(dataMinima)}</p>
        <div class="detail-actions">
            <button class="action-link secondary-action" type="button" id="refresh-payload">Atualizar JSON</button>
            <button class="action-link confirm-action" type="button" id="emitir-financeiro" ${bloqueios.length ? "disabled" : ""}>Emitir documento financeiro</button>
        </div>
        ${renderBloqueiosRecebimento(bloqueios)}
        <h4>Payload previsto</h4>
        <pre id="recebimento-payload" class="payload-preview"></pre>
        <p id="recebimento-feedback" class="muted">A emissao altera pendentes e cria documento financeiro. Confirma sempre antes de gravar.</p>
    `;
    preview.classList.remove("hidden");

    const refreshButton = preview.querySelector("#refresh-payload");
    refreshButton.addEventListener("click", () => updateRecebimentoPayload(pendente));
    preview.querySelector("#emitir-financeiro")?.addEventListener("click", () => emitirDocumentoFinanceiro(pendente));
    ["recebimento-data", "recebimento-tipo", "recebimento-serie", "recebimento-mpagamento", "recebimento-emissor", "recebimento-valor", "recebimento-desconto"]
        .forEach((id) => preview.querySelector(`#${id}`)?.addEventListener("input", () => updateRecebimentoPayload(pendente)));
    preview.querySelector("#recebimento-tipo")?.addEventListener("change", () => {
        const tipoId = preview.querySelector("#recebimento-tipo").value;
        const serieSelect = preview.querySelector("#recebimento-serie");
        serieSelect.innerHTML = options(seriesFinanceiras(tipoId), defaultSerieFinanceira(tipoId)?.serie, "serie", "nome");
        refreshDataMinimaFinanceira();
        updateRecebimentoPayload(pendente);
    });
    preview.querySelector("#recebimento-serie")?.addEventListener("change", () => {
        refreshDataMinimaFinanceira();
        updateRecebimentoPayload(pendente);
    });
    updateRecebimentoPayload(pendente);
}

function updateRecebimentoPayload(pendente) {
    const payloadElement = document.querySelector("#recebimento-payload");
    if (!payloadElement) {
        return;
    }
    const payload = buildRecebimentoPayload(pendente);
    payloadElement.textContent = JSON.stringify(payload, null, 2);
}

function buildRecebimentoPayload(pendente) {
    return {
        tipoDocumentoId: valueOf("#recebimento-tipo"),
        serie: valueOf("#recebimento-serie"),
        dataEmissao: valueOf("#recebimento-data"),
        clienteId: pendente.clienteId,
        moedaId: pendente.moedaId,
        mPagamentoId: numberOrNull(valueOf("#recebimento-mpagamento")),
        dataHoraOperacao: null,
        emissorId: valueOf("#recebimento-emissor"),
        observacoes: `Recebimento preparado no pseudo frontend para ${referencia(pendente.tipoDocumentoId, pendente.serieDocumento, pendente.numeroDocumento)}`,
        linhas: [
            {
                pendenteId: pendente.id,
                valorALiquidar: decimalString(valueOf("#recebimento-valor")),
                descontoPercentual: null,
                descontoValor: decimalString(valueOf("#recebimento-desconto"))
            }
        ]
    };
}

async function emitirDocumentoFinanceiro(pendente) {
    const feedback = document.querySelector("#recebimento-feedback");
    const payload = buildRecebimentoPayload(pendente);
    const bloqueios = validateRecebimentoPayload(pendente, payload);
    if (bloqueios.length > 0) {
        feedback.className = "form-feedback error";
        feedback.textContent = bloqueios.join(" ");
        return;
    }

    const referenciaOrigem = referencia(pendente.tipoDocumentoId, pendente.serieDocumento, pendente.numeroDocumento);
    const confirmado = window.confirm(`Emitir documento financeiro para liquidar ${money(payload.linhas[0].valorALiquidar)} ${pendente.moedaId} do pendente ${referenciaOrigem}?`);
    if (!confirmado) {
        feedback.className = "form-feedback";
        feedback.textContent = "Emissao cancelada. Nada foi gravado.";
        return;
    }

    feedback.className = "form-feedback";
    feedback.textContent = "A emitir documento financeiro...";

    try {
        const response = await fetch("/documentos-financeiros", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });
        const body = await response.json().catch(() => null);
        if (!response.ok) {
            throw new Error(body?.message || `Erro HTTP ${response.status}`);
        }

        state.selectedFinanceiroId = body.id;
        state.selectedPendenteId = pendente.id;
        await loadAll();
        setView("financeiros");
        showMessage(`Documento financeiro ${referencia(body.tipoDocumentoId, body.serie, body.numeroDocumento)} criado. Pendente atualizado.`, "success");
    } catch (error) {
        feedback.className = "form-feedback error";
        feedback.textContent = `Nao foi possivel emitir: ${error.message}`;
    }
}

async function anularDocumentoFinanceiro(documento) {
    const referenciaDocumento = referencia(documento.tipoDocumentoId, documento.serie, documento.numeroDocumento);
    const confirmado = window.confirm(`Anular ${referenciaDocumento}? Esta operacao faz rollback dos pendentes afetados se nao houver movimentos posteriores.`);
    if (!confirmado) {
        showMessage("Anulacao cancelada. Nada foi gravado.");
        return;
    }

    try {
        const response = await fetch(`/documentos-financeiros/${documento.id}/anular`, {
            method: "POST"
        });
        const body = await response.json().catch(() => null);
        if (!response.ok) {
            throw new Error(body?.message || `Erro HTTP ${response.status}`);
        }

        state.selectedFinanceiroId = body.id;
        await loadAll();
        setView("financeiros");
        showMessage(`Documento financeiro ${referenciaDocumento} anulado. Pendentes repostos quando aplicavel.`, "success");
    } catch (error) {
        showMessage(`Nao foi possivel anular ${referenciaDocumento}: ${error.message}`);
    }
}

async function ensureFinanceiroDiagnostico(documentoId) {
    if (!documentoId || state.financeiroDiagnosticos[documentoId]) {
        return;
    }
    state.financeiroDiagnosticos[documentoId] = { loading: true };
    renderFinanceiroDetail();
    try {
        const diagnostico = await fetchJson(`/documentos-financeiros/${documentoId}/diagnostico`);
        state.financeiroDiagnosticos[documentoId] = diagnostico;
    } catch (error) {
        state.financeiroDiagnosticos[documentoId] = {
            loading: false,
            podeAnular: false,
            alertas: [],
            bloqueios: [`Nao foi possivel carregar diagnostico: ${error.message}`]
        };
    }
    renderFinanceiroDetail();
}

async function fetchJson(url) {
    const response = await fetch(url);
    const body = await response.json().catch(() => null);
    if (!response.ok) {
        throw new Error(body?.message || `Erro HTTP ${response.status}`);
    }
    return body;
}

function recebimentoBloqueios(pendente, documento, tipoFinanceiro, serieFinanceira, modoPagamento) {
    const bloqueios = [];
    if (Number(pendente.valorPendente || 0) <= 0) {
        bloqueios.push("Pendente sem valor em aberto.");
    }
    if (!documento) {
        bloqueios.push("Documento comercial de origem nao esta carregado nesta pagina.");
    }
    if (!tipoFinanceiro) {
        bloqueios.push("Nao foi encontrado tipo de documento financeiro.");
    }
    if (!serieFinanceira) {
        bloqueios.push("Nao foi encontrada serie para o documento financeiro.");
    }
    if (!modoPagamento) {
        bloqueios.push("Modo de pagamento deve ser confirmado.");
    }
    return bloqueios;
}

function validateRecebimentoPayload(pendente, payload) {
    const bloqueios = [];
    const valorALiquidar = Number(payload.linhas[0].valorALiquidar || 0);
    const descontoValor = Number(payload.linhas[0].descontoValor || 0);
    const valorPendente = Number(pendente.valorPendente || 0);
    if (!payload.tipoDocumentoId) {
        bloqueios.push("Escolhe o tipo de documento financeiro.");
    }
    if (!payload.serie) {
        bloqueios.push("Escolhe a serie.");
    }
    if (!payload.dataEmissao) {
        bloqueios.push("Indica a data de emissao.");
    }
    const dataMinima = dataMinimaFinanceira(payload.tipoDocumentoId, payload.serie);
    if (payload.dataEmissao && dataMinima && payload.dataEmissao < dataMinima) {
        bloqueios.push(`A data de emissao nao pode ser anterior a ${dataMinima} para esta serie.`);
    }
    if (!payload.mPagamentoId) {
        bloqueios.push("Confirma o modo de pagamento.");
    }
    if (!payload.emissorId) {
        bloqueios.push("Indica o emissor.");
    }
    if (valorALiquidar <= 0) {
        bloqueios.push("O valor a liquidar deve ser positivo.");
    }
    if (valorALiquidar > valorPendente) {
        bloqueios.push("O valor a liquidar nao pode ultrapassar o valor pendente.");
    }
    if (descontoValor < 0) {
        bloqueios.push("O desconto nao pode ser negativo.");
    }
    if (descontoValor > valorALiquidar) {
        bloqueios.push("O desconto nao pode ser superior ao valor a liquidar.");
    }
    return bloqueios;
}

function tiposFinanceiros() {
    return state.tiposDocumento.filter((tipo) => Number(tipo.areaGestao) === 3);
}

function seriesFinanceiras(tipoDocumentoId) {
    return state.series.filter((serie) => !tipoDocumentoId || serie.tipoDocumentoId === tipoDocumentoId);
}

function defaultTipoFinanceiro() {
    return tiposFinanceiros()[0] || null;
}

function defaultSerieFinanceira(tipoDocumentoId) {
    return seriesFinanceiras(tipoDocumentoId)[0] || null;
}

function dataMinimaFinanceira(tipoDocumentoId, serie) {
    const datas = state.financeiros
        .filter((documento) => documento.tipoDocumentoId === tipoDocumentoId && documento.serie === serie)
        .map((documento) => documento.dataEmissao)
        .filter(Boolean)
        .sort();
    return datas.length === 0 ? "" : datas[datas.length - 1];
}

function refreshDataMinimaFinanceira() {
    const tipoId = valueOf("#recebimento-tipo");
    const serie = valueOf("#recebimento-serie");
    const dataMinima = dataMinimaFinanceira(tipoId, serie);
    const dataInput = document.querySelector("#recebimento-data");
    if (dataInput) {
        dataInput.min = dataMinima || "";
        if (dataMinima && dataInput.value < dataMinima) {
            dataInput.value = dataMinima;
        }
    }
    const regra = document.querySelector("#recebimento-data-regra");
    if (regra) {
        regra.textContent = renderDataMinimaFinanceira(dataMinima);
    }
}

function renderDataMinimaFinanceira(dataMinima) {
    if (!dataMinima) {
        return "Sem data minima detetada para esta serie financeira.";
    }
    return `Data minima desta serie financeira: ${dataMinima}.`;
}

function maxDate(first, second) {
    if (!second) {
        return first;
    }
    return first > second ? first : second;
}

function defaultMPagamento(documento) {
    return state.mpagamentos.find((modo) => modo.id === documento?.mPagamentoId) || state.mpagamentos[0] || null;
}

function formField(label, control) {
    return `
        <label class="form-field">
            <span>${escapeHtml(label)}</span>
            ${control}
        </label>
    `;
}

function select(id, items, selectedValue, valueField, labelField) {
    return `<select id="${escapeHtml(id)}">${options(items, selectedValue, valueField, labelField)}</select>`;
}

function options(items, selectedValue, valueField, labelField) {
    if (!items || items.length === 0) {
        return `<option value="">A confirmar</option>`;
    }
    return items.map((item) => {
        const value = item[valueField];
        const label = item[labelField] ? `${value} - ${item[labelField]}` : value;
        const selected = String(value) === String(selectedValue) ? " selected" : "";
        return `<option value="${escapeHtml(value)}"${selected}>${escapeHtml(label)}</option>`;
    }).join("");
}

function valueOf(selector) {
    return document.querySelector(selector)?.value || "";
}

function numberOrNull(value) {
    return value === "" ? null : Number(value);
}

function decimalString(value) {
    const number = Number(value || 0);
    return number.toFixed(6);
}

function renderBloqueiosRecebimento(bloqueios) {
    if (bloqueios.length === 0) {
        return `<p class="status success">Sem bloqueios visuais neste prototipo</p>`;
    }
    return `<ul class="warning-list">${bloqueios.map((bloqueio) => `<li>${escapeHtml(bloqueio)}</li>`).join("")}</ul>`;
}

function renderOrigemComercial(documento) {
    return `
        <div class="line-list">
            <div class="line-item">
                <strong>${escapeHtml(referencia(documento.tipoDocumentoId, documento.serie, documento.numeroDocumento))}</strong>
                <span>${escapeHtml(documento.clienteNome || documento.clienteId)} | Total ${money(documento.valorTotal)} ${escapeHtml(documento.moedaId)}</span>
            </div>
        </div>
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

function renderFinanceiroDiagnosticoResumo(diagnostico) {
    if (!diagnostico || diagnostico.loading) {
        return `
            <div class="diagnostic-card">
                <strong>Diagnostico de anulacao</strong>
                <p class="muted">A carregar diagnostico...</p>
            </div>
        `;
    }
    const bloqueios = diagnostico.bloqueios || [];
    const alertas = diagnostico.alertas || [];
    const podeAnular = diagnostico.podeAnular && bloqueios.length === 0;
    return `
        <div class="diagnostic-card ${podeAnular ? "ok" : "blocked"}">
            <strong>Diagnostico de anulacao</strong>
            <p>${podeAnular ? "Pode anular este documento financeiro." : "Nao pode anular neste momento."}</p>
            ${renderDiagnosticList("Bloqueios", bloqueios)}
            ${renderDiagnosticList("Alertas", alertas)}
        </div>
    `;
}

function renderDiagnosticList(title, items) {
    if (!items || items.length === 0) {
        return "";
    }
    return `
        <div class="diagnostic-list">
            <span>${escapeHtml(title)}</span>
            <ul>${items.map((item) => `<li>${escapeHtml(item)}</li>`).join("")}</ul>
        </div>
    `;
}

function detailLine(label, value) {
    return `
        <div class="detail-line">
            <span>${escapeHtml(label)}</span>
            <span>${escapeHtml(value)}</span>
        </div>
    `;
}

function pendenteStatus(pendente) {
    const statusInfo = pendenteStatusInfo(pendente);
    return status(statusInfo.label, statusInfo.tone);
}

function pendenteStatusLabel(pendente) {
    return pendenteStatusInfo(pendente).label;
}

function pendenteStatusInfo(pendente) {
    const valorDocumento = Number(pendente.valorDocumento || 0);
    const valorPendente = Number(pendente.valorPendente || 0);
    if (valorPendente <= 0) {
        return { key: "LIQUIDADO", label: "Liquidado", tone: "success" };
    }
    if (pendente.dataVencimento && pendente.dataVencimento < todayIso()) {
        return { key: "VENCIDO", label: "Vencido", tone: "danger" };
    }
    if (valorPendente < valorDocumento) {
        return { key: "PARCIAL", label: "Parcial", tone: "warning" };
    }
    return { key: "ABERTO", label: "Em aberto", tone: "neutral" };
}

function valorLiquidado(pendente) {
    return Number(pendente.valorDocumento || 0) - Number(pendente.valorPendente || 0);
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

function status(label, tone) {
    let className = "";
    if (tone === true || tone === "danger") {
        className = "danger";
    } else if (tone) {
        className = tone;
    }
    return `<span class="status ${className}">${escapeHtml(label)}</span>`;
}

function showMessage(message, tone = "error") {
    const element = document.querySelector("#message");
    element.textContent = message;
    element.classList.toggle("success", tone === "success");
    element.classList.toggle("hidden", !message);
}

function setText(selector, value) {
    const element = document.querySelector(selector);
    if (element) {
        element.textContent = value;
    }
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

function todayIso() {
    return new Date().toISOString().slice(0, 10);
}
