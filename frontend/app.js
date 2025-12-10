const API_BASE = localStorage.getItem("muebleshermanos_api") || "http://localhost:8081/api";

const state = {
  muebles: [],
  cotizaciones: [],
  quoteItems: [],
  currentVariaciones: new Map(),
  selectedMuebleId: null,
  editingMuebleId: null,
  editingVariacionId: null,
  variacionesFilter: "ACTIVE",
  filters: {
    search: "",
    estado: "",
    tipo: "",
    order: "precio-desc",
  },
};

let searchDebounce;

const elements = {
  mueblesList: document.getElementById("mueblesList"),
  filterEstado: document.getElementById("filterEstado"),
  filterTipo: document.getElementById("filterTipo"),
  orderMuebles: document.getElementById("orderMuebles"),
  searchMuebles: document.getElementById("searchMuebles"),
  refreshCatalog: document.getElementById("refreshCatalog"),
  muebleForm: document.getElementById("muebleForm"),
  muebleSubmit: document.getElementById("muebleSubmit"),
  cancelEditMueble: document.getElementById("cancelEditMueble"),
  muebleFeedback: document.getElementById("muebleFeedback"),
  cotizacionForm: document.getElementById("cotizacionForm"),
  cotizacionFeedback: document.getElementById("cotizacionFeedback"),
  cotizacionMueble: document.getElementById("cotizacionMueble"),
  cotizacionVariacion: document.getElementById("cotizacionVariacion"),
  cotizacionCantidad: document.getElementById("cotizacionCantidad"),
  cotizacionPreview: document.getElementById("cotizacionPreview"),
  cotizacionesList: document.getElementById("cotizacionesList"),
  addItemButton: document.getElementById("addItem"),
  loadQuotes: document.getElementById("loadQuotes"),
  toast: document.getElementById("toast"),
  statsTotalMuebles: document.getElementById("statsTotalMuebles"),
  statsTotalStock: document.getElementById("statsTotalStock"),
  statsVariacionesActivas: document.getElementById("statsVariacionesActivas"),
  statsVentasConfirmadas: document.getElementById("statsVentasConfirmadas"),
  statsReliquiaNombre: document.getElementById("statsReliquiaNombre"),
  statsReliquiaValor: document.getElementById("statsReliquiaValor"),
  toggleVariaciones: document.getElementById("toggleVariaciones"),
  variationsList: document.getElementById("variationsList"),
  variationsTitle: document.getElementById("variationsTitle"),
  variationsDescription: document.getElementById("variationsDescription"),
  variationsActive: document.getElementById("variationsActive"),
  variationsInactive: document.getElementById("variationsInactive"),
  variationsBasePrice: document.getElementById("variationsBasePrice"),
  variationsStock: document.getElementById("variationsStock"),
  variationsArtwork: document.getElementById("variationsArtwork"),
  variacionForm: document.getElementById("variacionForm"),
  variacionNombre: document.getElementById("variacionNombre"),
  variacionDescripcion: document.getElementById("variacionDescripcion"),
  variacionValorAjuste: document.getElementById("variacionValorAjuste"),
  variacionEstrategia: document.getElementById("variacionEstrategia"),
  variacionActiva: document.getElementById("variacionActiva"),
  variacionFeedback: document.getElementById("variacionFeedback"),
  variacionSubmit: document.getElementById("variacionSubmit"),
  cancelEditVariacion: document.getElementById("cancelEditVariacion"),
};

const formatCurrency = (value) =>
  new Intl.NumberFormat("es-CL", { style: "currency", currency: "CLP", maximumFractionDigits: 0 }).format(value);

const formatNumber = (value) => new Intl.NumberFormat("es-CL").format(value);

const TIPO_LABELS = {
  SILLA: "Silla / Banco",
  SILLON: "Sillón / Trono",
  MESA: "Mesa de festín",
  ESTANTE: "Estante",
  CAJON: "Cofre",
  OTRO: "Otro",
};

const TAMANO_LABELS = {
  GRANDE: "Gran salón",
  MEDIANO: "Mediano",
  PEQUENO: "Compacto",
};

const calculateVariationPrice = (basePrice, variacion) => {
  let price = Number(basePrice);
  if (!variacion) return price;
  const adjustment = Number(variacion.valorAjuste);
  switch (variacion.priceStrategyType) {
    case "ADDITIVE":
      price += adjustment;
      break;
    case "PERCENTAGE":
      price *= 1 + adjustment / 100;
      break;
    default:
      break;
  }
  return price;
};


const fetchJson = async (endpoint, options = {}) => {
  const response = await fetch(`${API_BASE}${endpoint}`, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  const contentType = response.headers.get("content-type") || "";
  if (!response.ok) {
    const body = contentType.includes("application/json") ? await response.json().catch(() => ({})) : {};
    throw new Error(body.message || `Ocurrió un error (${response.status})`);
  }
  if (response.status === 204) {
    return {};
  }
  if (!contentType.includes("application/json")) {
    const text = await response.text();
    return text ? JSON.parse(text) : {};
  }
  return response.json();
};

const showToast = (message, type = "info") => {
  elements.toast.textContent = message;
  elements.toast.className = `toast toast--visible toast--${type}`;
  setTimeout(() => {
    elements.toast.classList.remove("toast--visible");
  }, 2800);
};

const setFeedback = (element, message, isError = false) => {
  element.textContent = message;
  element.style.color = isError ? "#d93025" : "var(--accent)";
  if (message) {
    setTimeout(() => {
      element.textContent = "";
    }, 3200);
  }
};

const resetMuebleForm = () => {
  if (!elements.muebleForm) return;
  elements.muebleForm.reset();
  state.editingMuebleId = null;
  if (elements.muebleSubmit) {
    elements.muebleSubmit.textContent = "Guardar mueble";
  }
  if (elements.cancelEditMueble) {
    elements.cancelEditMueble.style.display = "none";
  }
};

const startMuebleEdit = (mueble) => {
  if (!elements.muebleForm || !mueble) return;
  state.editingMuebleId = mueble.id;
  elements.muebleForm.nombre.value = mueble.nombre;
  elements.muebleForm.tipo.value = mueble.tipo;
  elements.muebleForm.precioBase.value = mueble.precioBase;
  elements.muebleForm.stock.value = mueble.stock;
  elements.muebleForm.estado.value = mueble.estado;
  elements.muebleForm.tamano.value = mueble.tamano;
  elements.muebleForm.material.value = mueble.material;
  if (elements.muebleSubmit) {
    elements.muebleSubmit.textContent = "Actualizar mueble";
  }
  if (elements.cancelEditMueble) {
    elements.cancelEditMueble.style.display = "inline-flex";
  }
  elements.muebleForm.scrollIntoView({ behavior: "smooth", block: "center" });
};

const resetVariacionForm = () => {
  if (!elements.variacionForm) return;
  elements.variacionForm.reset();
  state.editingVariacionId = null;
  if (elements.variacionActiva) {
    elements.variacionActiva.checked = true;
  }
  if (elements.variacionSubmit) {
    elements.variacionSubmit.textContent = "Agregar variación";
  }
  if (elements.cancelEditVariacion) {
    elements.cancelEditVariacion.style.display = "none";
  }
};

const startVariacionEdit = (variacion) => {
  if (!elements.variacionForm || !variacion) return;
  state.editingVariacionId = variacion.id;
  if (elements.variacionNombre) elements.variacionNombre.value = variacion.nombre || "";
  if (elements.variacionDescripcion) elements.variacionDescripcion.value = variacion.descripcion || "";
  if (elements.variacionValorAjuste) elements.variacionValorAjuste.value = variacion.valorAjuste;
  if (elements.variacionEstrategia) elements.variacionEstrategia.value = variacion.priceStrategyType;
  if (elements.variacionActiva) elements.variacionActiva.checked = !!variacion.activa;
  if (elements.variacionSubmit) elements.variacionSubmit.textContent = "Actualizar variación";
  if (elements.cancelEditVariacion) elements.cancelEditVariacion.style.display = "inline-flex";
  elements.variacionForm.scrollIntoView({ behavior: "smooth", block: "center" });
};

const renderMuebles = () => {
  const hasCatalog = !!elements.mueblesList;
  const hasVariationsPanel = !!elements.variationsList;
  const hasQuoteForm = !!elements.cotizacionForm;
  const searchTerm = state.filters.search.trim().toLowerCase();
  const filtered = state.muebles
    .filter((mueble) => {
      const matchesEstado = state.filters.estado ? mueble.estado === state.filters.estado : true;
      const matchesTipo = state.filters.tipo ? mueble.tipo === state.filters.tipo : true;
      const matchesSearch = searchTerm
        ? [mueble.nombre, mueble.material, mueble.tipo]
            .join(" ")
            .toLowerCase()
            .includes(searchTerm)
        : true;
      return matchesEstado && matchesTipo && matchesSearch;
    })
    .sort((a, b) => {
      switch (state.filters.order) {
        case "precio-asc":
          return Number(a.precioBase) - Number(b.precioBase);
        case "precio-desc":
          return Number(b.precioBase) - Number(a.precioBase);
        case "stock-desc":
          return Number(b.stock) - Number(a.stock);
        default:
          return 0;
      }
    });

  if (!filtered.length) {
    if (hasCatalog) {
      elements.mueblesList.innerHTML = `
        <div class="empty-state">
          <span class="empty-state__emoji">🛡️</span>
          <p>No se encontraron reliquias con los criterios seleccionados. Ajusta los filtros o registra un nuevo mueble.</p>
        </div>`;
    }
    state.selectedMuebleId = null;
    renderVariacionesPanel();
    return;
  }

  if (!filtered.some((mueble) => mueble.id === state.selectedMuebleId)) {
    state.selectedMuebleId = filtered[0].id;
  }

  if (hasCatalog) {
    elements.mueblesList.innerHTML = filtered
      .map((mueble) => {
        const tipoLabel = TIPO_LABELS[mueble.tipo] || mueble.tipo;
        const tamanoLabel = TAMANO_LABELS[mueble.tamano] || mueble.tamano;
        const variacionesActivas = mueble.variaciones.filter((variacion) => variacion.activa);
        const variacionesInactivas = mueble.variaciones.length - variacionesActivas.length;
        const variacionesMarkup = mueble.variaciones.length
          ? mueble.variaciones
              .map(
                (variacion) =>
                  `<span class="variation-pill${variacion.activa ? "" : " variation-pill--inactive"}" title="${
                    variacion.descripcion || "Sin descripción"
                  }">
                    ${variacion.nombre} · ${variacion.priceStrategyType}${variacion.activa ? "" : " (inactiva)"}
                  </span>`
              )
              .join("")
          : "<span class='variation-pill'>Sin variaciones</span>";

        const classes = ["mueble-card"];
        if (mueble.stock <= 5) {
          classes.push("mueble-card--alert");
        }
        if (state.selectedMuebleId === mueble.id) {
          classes.push("mueble-card--selected");
        }
        const variationAction = hasVariationsPanel
          ? `<button class="btn btn--primary" data-action="loadVariaciones">
                Ver variaciones
              </button>`
          : `<a class="btn btn--primary" href="runas.html" target="_blank" rel="noopener">
                Ver runas
              </a>`;

        const quoteAction = hasQuoteForm
          ? `<button class="btn btn--ghost" data-action="prepareQuote">
                Llevar al taller de cotización
              </button>`
          : `<a class="btn btn--ghost" href="tributos.html" target="_blank" rel="noopener">
                Llevar al taller
              </a>`;

        return `
          <article class="${classes.join(" ")}" data-id="${mueble.id}">
            <div>
              <h4 class="mueble-card__title">${mueble.nombre}</h4>
              <div class="mueble-card__meta">
                <span>Tipo: <strong>${tipoLabel}</strong></span>
                <span>Tamaño: ${tamanoLabel}</span>
                <span>Material: ${mueble.material}</span>
              </div>
              <div class="mueble-card__meta">
                <span>Precio base: <strong>${formatCurrency(mueble.precioBase)}</strong></span>
                <span>Stock: ${mueble.stock}</span>
                ${mueble.stock <= 5 ? '<span class="badge badge--warning">Stock bajo</span>' : ""}
              </div>
              <div class="mueble-card__meta">
                <span class="badge ${mueble.estado === "ACTIVO" ? "" : "badge--warning"}">${mueble.estado}</span>
                <span class="badge">
                  ${variacionesActivas.length} variaciones activas${
                    variacionesInactivas > 0 ? ` · ${variacionesInactivas} inactivas` : ""
                  }
                </span>
              </div>
          <div class="mueble-card__variaciones">
              ${variacionesMarkup}
            </div>
          </div>
          <div class="mueble-card__actions">
            <button class="btn btn--ghost" data-action="editMueble">
              Editar
            </button>
            <button class="btn btn--ghost" data-action="toggle" data-estado="${mueble.estado}">
              ${mueble.estado === "ACTIVO" ? "Marcar inactivo" : "Activar"}
            </button>
            ${variationAction}
            ${quoteAction}
          </div>
        </article>`;
      })
      .join("");
  }

  renderVariacionesPanel();
};

const updateVariacionesToggleLabel = () => {
  if (!elements.toggleVariaciones) return;
  elements.toggleVariaciones.textContent =
    state.variacionesFilter === "ALL" ? "Mostrar solo activas" : "Mostrar todas las variaciones";
};

const renderVariacionesPanel = () => {
  if (
    !elements.variationsList ||
    !elements.variationsTitle ||
    !elements.variationsDescription ||
    !elements.variationsActive ||
    !elements.variationsInactive ||
    !elements.variationsBasePrice ||
    !elements.variationsStock
  ) {
    updateVariacionesToggleLabel();
    return;
  }

  updateVariacionesToggleLabel();

  const selected = state.muebles.find((mueble) => mueble.id === state.selectedMuebleId);

  if (!selected) {
    elements.variationsTitle.textContent = "Selecciona una reliquia del catálogo";
    elements.variationsDescription.textContent =
      "Cuando elijas un mueble, aquí aparecerán sus detalles, combinaciones rúnicas y valores sugeridos.";
    if (elements.variationsArtwork) {
      elements.variationsArtwork.dataset.type = "NONE";
    }
    if (elements.variationsActive) elements.variationsActive.textContent = "0";
    if (elements.variationsInactive) elements.variationsInactive.textContent = "0";
    if (elements.variationsBasePrice) elements.variationsBasePrice.textContent = "—";
    if (elements.variationsStock) elements.variationsStock.textContent = "—";
    elements.variationsList.innerHTML =
      "<li class='variations__placeholder'>Explora el arsenal y selecciona una reliquia para ver sus runas.</li>";
    return;
  }

  const tipoLabel = TIPO_LABELS[selected.tipo] || selected.tipo;
  const tamanoLabel = TAMANO_LABELS[selected.tamano] || selected.tamano;
  const activas = selected.variaciones.filter((variacion) => variacion.activa);
  const inactivas = selected.variaciones.length - activas.length;
  const listado =
    state.variacionesFilter === "ACTIVE"
      ? selected.variaciones.filter((variacion) => variacion.activa)
      : selected.variaciones;

  elements.variationsTitle.textContent = selected.nombre;
  elements.variationsDescription.textContent = `Tipo: ${tipoLabel}. Tamaño: ${tamanoLabel}. Material: ${selected.material}.`;
  if (elements.variationsArtwork) {
    const artworkType = ["SILLA", "SILLON", "MESA", "ESTANTE", "CAJON"].includes(selected.tipo)
      ? selected.tipo
      : "NONE";
    elements.variationsArtwork.dataset.type = artworkType;
  }
  if (elements.cotizacionMueble && selected.estado === "ACTIVO") {
    const optionExists = Array.from(elements.cotizacionMueble.options).some(
      (option) => Number(option.value) === selected.id
    );
    if (optionExists) {
      elements.cotizacionMueble.value = String(selected.id);
    }
  }
  if (elements.variationsActive) elements.variationsActive.textContent = formatNumber(activas.length);
  if (elements.variationsInactive) elements.variationsInactive.textContent = formatNumber(inactivas);
  if (elements.variationsBasePrice) elements.variationsBasePrice.textContent = formatCurrency(selected.precioBase);
  if (elements.variationsStock) elements.variationsStock.textContent = formatNumber(selected.stock);

  if (!listado.length) {
    const message =
      state.variacionesFilter === "ACTIVE"
        ? "No hay variaciones activas para esta reliquia."
        : "No hay variaciones registradas para esta reliquia.";
    elements.variationsList.innerHTML = `<li class="variations__placeholder">${message}</li>`;
    return;
  }

  elements.variationsList.innerHTML = listado
    .map((variacion) => {
      const tributo = calculateVariationPrice(selected.precioBase, variacion);
      const ajuste =
        variacion.priceStrategyType === "ADDITIVE"
          ? `${Number(variacion.valorAjuste) >= 0 ? "+" : ""}${formatCurrency(Number(variacion.valorAjuste))}`
          : `${variacion.valorAjuste}%`;
      return `
        <li class="variations__item${variacion.activa ? "" : " variations__item--inactive"}">
          <div class="variations__header">
            <strong>${variacion.nombre}</strong>
            <span class="badge ${variacion.activa ? "" : "badge--warning"}">${
        variacion.activa ? "Activa" : "Inactiva"
      }</span>
          </div>
          <p>${variacion.descripcion || "Sin descripción registrada."}</p>
          <div class="variations__meta">
            <span>Estrategia: ${variacion.priceStrategyType}</span>
            <span>Ajuste: ${ajuste}</span>
            <span>Tributo final: ${formatCurrency(tributo)}</span>
          </div>
          <div class="mueble-card__actions">
            <button class="btn btn--ghost" data-var-action="edit" data-variacion-id="${variacion.id}">
              Editar
            </button>
            <button class="btn btn--ghost" data-var-action="toggle" data-variacion-id="${variacion.id}" data-activa="${variacion.activa}">
              ${variacion.activa ? "Desactivar" : "Activar"}
            </button>
            <button class="btn btn--ghost" data-var-action="delete" data-variacion-id="${variacion.id}">
              Eliminar
            </button>
          </div>
        </li>`;
    })
    .join("");
};

const populateSelects = () => {
  if (!elements.cotizacionMueble) return;
  const options = state.muebles
    .filter((mueble) => mueble.estado === "ACTIVO")
    .map((mueble) => `<option value="${mueble.id}">${mueble.nombre}</option>`)
    .join("");

  elements.cotizacionMueble.innerHTML = `<option value="" disabled selected>Selecciona un mueble</option>${options}`;
};

const loadVariaciones = (muebleId) => {
  state.selectedMuebleId = Number(muebleId);
  resetVariacionForm();
  const mueble = state.muebles.find((item) => item.id === Number(muebleId));
  const variaciones = mueble?.variaciones || [];
  state.currentVariaciones.set(muebleId, variaciones);

  if (elements.cotizacionVariacion) {
    const options = [
      `<option value="">Sin variación</option>`,
      ...variaciones.map((variacion) => {
        const ajuste =
          variacion.priceStrategyType === "ADDITIVE"
            ? `${Number(variacion.valorAjuste) >= 0 ? "+" : ""}${formatCurrency(Number(variacion.valorAjuste))}`
            : `${variacion.valorAjuste}%`;
        return `<option value="${variacion.id}">${variacion.nombre} (${ajuste})</option>`;
      }),
    ];
    elements.cotizacionVariacion.innerHTML = options.join("");
  }

  renderVariacionesPanel();
};

const renderQuotePreview = () => {
  if (!elements.cotizacionPreview) return;
  if (!state.quoteItems.length) {
    elements.cotizacionPreview.innerHTML = `<p class="quote-preview__empty">Selecciona una reliquia para comenzar.</p>`;
    return;
  }

  const total = state.quoteItems.reduce((acc, item) => acc + item.subtotal, 0);
  const itemsMarkup = state.quoteItems
    .map((item, index) => {
      const variacion = item.variacion ? ` · ${item.variacion.nombre}` : "";
      return `
        <div class="quote-preview__item">
          <header>
            <strong>${item.mueble.nombre}${variacion}</strong>
            <button class="btn btn--ghost" data-remove="${index}">Quitar</button>
          </header>
          <div class="quote-preview__qty">
            ${item.cantidad} x ${formatCurrency(item.precioUnitario)} = <strong>${formatCurrency(item.subtotal)}</strong>
          </div>
        </div>`;
    })
    .join("");

  elements.cotizacionPreview.innerHTML = `
    <div class="quote-preview__header">
      <span>${state.quoteItems.length} reliquia${state.quoteItems.length > 1 ? "s" : ""}</span>
      <strong>Tributo estimado: ${formatCurrency(total)}</strong>
    </div>
    ${itemsMarkup}
    <div class="quote-preview__cta">
      <button class="btn btn--ghost" data-clear>Vaciar selección</button>
    </div>
  `;
};

const renderCotizaciones = () => {
  if (!elements.cotizacionesList) {
    return;
  }
  if (!state.cotizaciones.length) {
    elements.cotizacionesList.innerHTML = `
      <div class="empty-state">
        <span class="empty-state__emoji">🧾</span>
        <p>No hay cotizaciones registradas.</p>
      </div>`;
    return;
  }

  elements.cotizacionesList.innerHTML = state.cotizaciones
    .map((cotizacion) => {
      const itemsMarkup = cotizacion.items
        .map(
          (item) => `
            <div class="quote-card__item">
              <strong>${item.muebleNombre}</strong>
              ${item.variacionNombre ? `<span class="variation-pill">${item.variacionNombre}</span>` : ""}
              <div class="quote-preview__qty">
                ${item.cantidad} x ${formatCurrency(item.precioUnitario)} = ${formatCurrency(item.subtotal)}
              </div>
            </div>`
        )
        .join("");

      const itemsCount = cotizacion.items.length;
      const statusMessage =
        cotizacion.estado === "CONFIRMADA"
          ? `<div class="quote-card__status">Venta confirmada ✅</div>`
          : cotizacion.estado === "CANCELADA"
            ? `<div class="quote-card__status quote-card__status--cancel">Cotización cancelada</div>`
            : `<div class="quote-card__status quote-card__status--pending">Pendiente de confirmación</div>`;

      return `
        <article class="quote-card" data-id="${cotizacion.id}">
          <div class="quote-card__header">
            <div>
              <span class="badge ${cotizacion.estado === "CREADA" ? "" : "badge--warning"}">${cotizacion.estado}</span>
              <small>${new Date(cotizacion.fechaCreacion).toLocaleString("es-CL")}</small>
            </div>
            <span class="quote-card__meta">${itemsCount} ítem${itemsCount === 1 ? "" : "s"}</span>
          </div>
          <div class="quote-card__items">
            ${itemsMarkup}
          </div>
          ${statusMessage}
          <div class="quote-card__footer">
            <div>Tributo: <span class="quote-card__total">${formatCurrency(cotizacion.total || 0)}</span></div>
            <div class="quote-card__actions">
              ${
                cotizacion.estado === "CREADA"
                  ? `<button class="btn btn--primary" data-action="confirm">Confirmar venta</button>`
                  : ""
              }
              ${
                cotizacion.estado === "CREADA"
                  ? `<button class="btn btn--ghost" data-action="cancel">Cancelar</button>`
                  : ""
              }
            </div>
          </div>
        </article>`;
    })
    .join("");
};

const updateStats = () => {
  if (!elements.statsTotalMuebles) return;
  const totalMuebles = state.muebles.length;
  const totalStock = state.muebles.reduce((acc, mueble) => acc + Number(mueble.stock || 0), 0);
  const variacionesActivas = state.muebles.reduce(
    (acc, mueble) => acc + mueble.variaciones.filter((variacion) => variacion.activa).length,
    0
  );
  const ventasConfirmadas = state.cotizaciones.filter((cotizacion) => cotizacion.estado === "CONFIRMADA").length;
  const reliquiaLegendaria = [...state.muebles]
    .filter((mueble) => mueble.estado === "ACTIVO")
    .sort((a, b) => Number(b.precioBase) - Number(a.precioBase))[0];

  elements.statsTotalMuebles.textContent = formatNumber(totalMuebles);
  elements.statsTotalStock.textContent = formatNumber(totalStock);
  elements.statsVariacionesActivas.textContent = formatNumber(variacionesActivas);
  elements.statsVentasConfirmadas.textContent = formatNumber(ventasConfirmadas);
  if (elements.statsReliquiaNombre) {
    elements.statsReliquiaNombre.textContent = reliquiaLegendaria ? reliquiaLegendaria.nombre : "—";
  }
  if (elements.statsReliquiaValor) {
    elements.statsReliquiaValor.textContent = reliquiaLegendaria ? formatCurrency(reliquiaLegendaria.precioBase) : "—";
  }
};

const loadMuebles = async () => {
  try {
    const data = await fetchJson(`/muebles`);
    state.muebles = data;
    if (!state.selectedMuebleId || !state.muebles.some((mueble) => mueble.id === state.selectedMuebleId)) {
      const firstActive = state.muebles.find((mueble) => mueble.estado === "ACTIVO");
      state.selectedMuebleId = firstActive ? firstActive.id : state.muebles[0]?.id ?? null;
    }
    renderMuebles();
    populateSelects();
    updateStats();
  } catch (error) {
    showToast(error.message, "error");
  }
};

const loadCotizaciones = async () => {
  try {
    state.cotizaciones = await fetchJson("/cotizaciones");
    renderCotizaciones();
    updateStats();
  } catch (error) {
    showToast(error.message, "error");
  }
};

// Event bindings
elements.refreshCatalog?.addEventListener("click", loadMuebles);
elements.filterEstado?.addEventListener("change", (event) => {
  state.filters.estado = event.target.value;
  renderMuebles();
});

elements.filterTipo?.addEventListener("change", (event) => {
  state.filters.tipo = event.target.value;
  renderMuebles();
});

elements.orderMuebles?.addEventListener("change", (event) => {
  state.filters.order = event.target.value;
  renderMuebles();
});

elements.searchMuebles?.addEventListener("input", (event) => {
  clearTimeout(searchDebounce);
  searchDebounce = setTimeout(() => {
    state.filters.search = event.target.value;
    renderMuebles();
  }, 200);
});

elements.loadQuotes?.addEventListener("click", loadCotizaciones);

const smoothScroll = (selector) => {
  const section = document.querySelector(selector);
  if (section) {
    section.scrollIntoView({ behavior: "smooth", block: "start" });
  }
};

elements.toggleVariaciones?.addEventListener("click", () => {
  state.variacionesFilter = state.variacionesFilter === "ACTIVE" ? "ALL" : "ACTIVE";
  updateVariacionesToggleLabel();
  renderVariacionesPanel();
});

elements.muebleForm?.addEventListener("submit", async (event) => {
  event.preventDefault();
  const form = new FormData(event.target);

  const payload = {
    nombre: form.get("nombre"),
    tipo: form.get("tipo"),
    precioBase: Number(form.get("precioBase")),
    stock: Number(form.get("stock")),
    estado: form.get("estado"),
    tamano: form.get("tamano"),
    material: form.get("material"),
  };

  try {
    const isEdit = Boolean(state.editingMuebleId);
    const endpoint = isEdit ? `/muebles/${state.editingMuebleId}` : "/muebles";
    const method = isEdit ? "PUT" : "POST";

    await fetchJson(endpoint, { method, body: JSON.stringify(payload) });
    resetMuebleForm();
    setFeedback(elements.muebleFeedback, isEdit ? "Mueble actualizado" : "Mueble agregado con éxito");
    showToast(isEdit ? "Mueble actualizado" : "Mueble creado");
    loadMuebles();
  } catch (error) {
    setFeedback(elements.muebleFeedback, error.message, true);
  }
});

elements.cancelEditMueble?.addEventListener("click", () => {
  resetMuebleForm();
});

elements.mueblesList?.addEventListener("click", async (event) => {
  const button = event.target.closest("button[data-action]");
  if (!button) return;

  const card = button.closest("[data-id]");
  const id = card?.dataset.id;
  if (!id) return;

  if (button.dataset.action === "editMueble") {
    const mueble = state.muebles.find((item) => item.id === Number(id));
    if (mueble) {
      startMuebleEdit(mueble);
      showToast("Editando mueble seleccionado");
    }
    return;
  }

  if (button.dataset.action === "toggle") {
    const currentEstado = button.dataset.estado;
    const nuevoEstado = currentEstado === "ACTIVO" ? "INACTIVO" : "ACTIVO";
    try {
      await fetchJson(`/muebles/${id}/estado?estado=${nuevoEstado}`, { method: "PATCH" });
      showToast(`Estado cambiado a ${nuevoEstado}`);
      loadMuebles();
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  if (button.dataset.action === "loadVariaciones") {
    state.selectedMuebleId = Number(id);
    state.variacionesFilter = "ACTIVE";
    if (elements.cotizacionMueble) {
      elements.cotizacionMueble.value = id;
    }
    loadVariaciones(id);
    updateVariacionesToggleLabel();
    renderMuebles();
    smoothScroll("#variationsSection");
    showToast("Variaciones cargadas en el taller");
  }

  if (button.dataset.action === "prepareQuote") {
    state.selectedMuebleId = Number(id);
    state.variacionesFilter = "ACTIVE";
    if (elements.cotizacionMueble) {
      elements.cotizacionMueble.value = id;
    }
    loadVariaciones(id);
    updateVariacionesToggleLabel();
    renderMuebles();
    if (elements.cotizacionCantidad) {
      elements.cotizacionCantidad.value = 1;
    }
    if (document.querySelector("#quoteSection")) {
      smoothScroll("#quoteSection");
      showToast("Mueble listo para cotizar");
    } else {
      smoothScroll("#variationsSection");
      showToast("Variaciones cargadas en el taller");
    }
  }
});

elements.variacionForm?.addEventListener("submit", async (event) => {
  event.preventDefault();
  if (!state.selectedMuebleId) {
    setFeedback(elements.variacionFeedback, "Selecciona un mueble para aplicar la variación", true);
    return;
  }

  const nombre = elements.variacionNombre?.value?.trim();
  const valorAjuste = Number(elements.variacionValorAjuste?.value ?? 0);
  const priceStrategyType = elements.variacionEstrategia?.value;
  const descripcion = elements.variacionDescripcion?.value ?? "";
  const activa = elements.variacionActiva?.checked ?? true;

  if (!nombre || Number.isNaN(valorAjuste)) {
    setFeedback(elements.variacionFeedback, "Completa nombre y valor de ajuste", true);
    return;
  }

  const payload = { nombre, descripcion, valorAjuste, priceStrategyType, activa };
  const isEdit = Boolean(state.editingVariacionId);
  const endpoint = isEdit
    ? `/muebles/${state.selectedMuebleId}/variaciones/${state.editingVariacionId}`
    : `/muebles/${state.selectedMuebleId}/variaciones`;
  const method = isEdit ? "PUT" : "POST";

  try {
    await fetchJson(endpoint, { method, body: JSON.stringify(payload) });
    setFeedback(elements.variacionFeedback, isEdit ? "Variación actualizada" : "Variación creada");
    showToast(isEdit ? "Variación actualizada" : "Variación agregada");
    resetVariacionForm();
    loadMuebles();
    loadVariaciones(state.selectedMuebleId);
  } catch (error) {
    setFeedback(elements.variacionFeedback, error.message, true);
  }
});

elements.cancelEditVariacion?.addEventListener("click", () => {
  resetVariacionForm();
});

elements.variationsList?.addEventListener("click", async (event) => {
  const button = event.target.closest("button[data-var-action]");
  if (!button) return;
  const variacionId = Number(button.dataset.variacionId);
  const selectedMueble = state.muebles.find((mueble) => mueble.id === state.selectedMuebleId);
  const variacion = selectedMueble?.variaciones.find((item) => item.id === variacionId);
  if (!variacion || !state.selectedMuebleId) return;

  if (button.dataset.varAction === "edit") {
    startVariacionEdit(variacion);
    return;
  }

  if (button.dataset.varAction === "toggle") {
    const payload = {
      nombre: variacion.nombre,
      descripcion: variacion.descripcion,
      valorAjuste: Number(variacion.valorAjuste),
      priceStrategyType: variacion.priceStrategyType,
      activa: !variacion.activa,
    };
    try {
      await fetchJson(`/muebles/${state.selectedMuebleId}/variaciones/${variacionId}`, {
        method: "PUT",
        body: JSON.stringify(payload),
      });
      resetVariacionForm();
      showToast(payload.activa ? "Variación activada" : "Variación desactivada");
      loadMuebles();
      loadVariaciones(state.selectedMuebleId);
    } catch (error) {
      showToast(error.message, "error");
    }
    return;
  }

  if (button.dataset.varAction === "delete") {
    const confirmed = window.confirm("¿Eliminar esta variación?");
    if (!confirmed) return;
    try {
      await fetchJson(`/muebles/${state.selectedMuebleId}/variaciones/${variacionId}`, { method: "DELETE" });
      if (state.editingVariacionId === variacionId) {
        resetVariacionForm();
      }
      showToast("Variación eliminada");
      loadMuebles();
      loadVariaciones(state.selectedMuebleId);
    } catch (error) {
      showToast(error.message, "error");
    }
  }
});

elements.cotizacionMueble?.addEventListener("change", (event) => {
  state.selectedMuebleId = Number(event.target.value);
  state.variacionesFilter = "ACTIVE";
  loadVariaciones(event.target.value);
  updateVariacionesToggleLabel();
  renderMuebles();
});

elements.addItemButton?.addEventListener("click", () => {
  const muebleId = elements.cotizacionMueble.value;
  const cantidad = Number(elements.cotizacionCantidad.value || 1);
  if (!muebleId) {
    setFeedback(elements.cotizacionFeedback, "Selecciona un mueble primero", true);
    return;
  }

  const mueble = state.muebles.find((item) => item.id === Number(muebleId));
  if (!mueble) {
    setFeedback(elements.cotizacionFeedback, "Mueble no encontrado", true);
    return;
  }

  const variacionId = elements.cotizacionVariacion.value;
  const variaciones = state.currentVariaciones.get(muebleId) || mueble.variaciones || [];
  const variacion = variaciones.find((item) => item.id === Number(variacionId));

  const priceStrategy = variacion?.priceStrategyType;
  let precioUnitario = Number(mueble.precioBase);
  if (variacion) {
    if (priceStrategy === "ADDITIVE") {
      precioUnitario += Number(variacion.valorAjuste);
    } else if (priceStrategy === "PERCENTAGE") {
      precioUnitario *= 1 + Number(variacion.valorAjuste) / 100;
    }
  }

  const subtotal = precioUnitario * cantidad;

  state.quoteItems.push({
    mueble,
    variacion: variacion || null,
    cantidad,
    precioUnitario,
    subtotal,
  });

  renderQuotePreview();
  setFeedback(elements.cotizacionFeedback, "");
});

elements.cotizacionPreview?.addEventListener("click", (event) => {
  const removeBtn = event.target.closest("[data-remove]");
  if (removeBtn) {
    const index = Number(removeBtn.dataset.remove);
    state.quoteItems.splice(index, 1);
    renderQuotePreview();
    return;
  }

  if (event.target.closest("[data-clear]")) {
    state.quoteItems = [];
    renderQuotePreview();
  }
});

elements.cotizacionForm?.addEventListener("submit", async (event) => {
  event.preventDefault();
  if (!state.quoteItems.length) {
    setFeedback(elements.cotizacionFeedback, "Agrega al menos un mueble a la cotización", true);
    return;
  }

  const payload = {
    items: state.quoteItems.map((item) => ({
      muebleId: item.mueble.id,
      variacionId: item.variacion?.id || null,
      cantidad: item.cantidad,
    })),
  };

  try {
    await fetchJson("/cotizaciones", { method: "POST", body: JSON.stringify(payload) });
    showToast("Cotización creada con éxito");
    state.quoteItems = [];
    renderQuotePreview();
    loadCotizaciones();
  } catch (error) {
    setFeedback(elements.cotizacionFeedback, error.message, true);
  }
});

elements.cotizacionesList?.addEventListener("click", async (event) => {
  const button = event.target.closest("button[data-action]");
  if (!button) return;
  const card = button.closest("[data-id]");
  const id = card?.dataset.id;
  if (!id) return;

  try {
    if (button.dataset.action === "confirm") {
      await fetchJson(`/cotizaciones/${id}/confirmar`, { method: "POST" });
      showToast("Cotización confirmada ✨");
    } else if (button.dataset.action === "cancel") {
      await fetchJson(`/cotizaciones/${id}/cancelar`, { method: "POST" });
      showToast("Cotización cancelada");
    }
    loadMuebles();
    loadCotizaciones();
  } catch (error) {
    showToast(error.message, "error");
  }
});

const init = () => {
  if (elements.orderMuebles) {
    elements.orderMuebles.value = state.filters.order;
  }
  if (elements.filterEstado) {
    elements.filterEstado.value = state.filters.estado;
  }
  if (elements.filterTipo) {
    elements.filterTipo.value = state.filters.tipo;
  }
  if (elements.searchMuebles) {
    elements.searchMuebles.value = state.filters.search;
  }
  resetMuebleForm();
  resetVariacionForm();
  updateVariacionesToggleLabel();
  loadMuebles();
  loadCotizaciones();
};

init();
