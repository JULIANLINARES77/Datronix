const config = window.DATRONIX_BODEGA_CONFIG;
const apiBaseUrl = config.apiBaseUrl.replace(/\/$/, '');
const loginScreen = document.getElementById('pantallaLogin');
const warehouseScreen = document.getElementById('pantallaBodega');
const loginMessage = document.getElementById('mensajeLoginBodega');
const statusMessage = document.getElementById('estadoRemisiones');
const ordersList = document.getElementById('listaRemisiones');
const userLabel = document.getElementById('usuarioBodega');

function getSession() {
    let user = null;
    try {
        user = JSON.parse(localStorage.getItem('usuario') || 'null');
    } catch {
        localStorage.removeItem('usuario');
    }
    return { user };
}

async function request(path, options = {}) {
    const response = await fetch(`${apiBaseUrl}${path}`, {
        ...options,
        headers: { 'Content-Type': 'application/json', ...options.headers }
    });
    const text = await response.text();
    let body = null;
    if (text) {
        try {
            body = JSON.parse(text);
        } catch {
            body = { mensaje: text };
        }
    }
    if (!response.ok) {
        if (response.status === 401) {
            localStorage.removeItem('usuario');
            mostrarLogin('No se pudo completar la solicitud. Inicia sesión otra vez.');
        }
        throw new Error(body?.mensaje || `Error ${response.status}`);
    }
    return body;
}

function mostrarLogin(mensaje = '') {
    loginScreen.hidden = false;
    warehouseScreen.hidden = true;
    loginMessage.textContent = mensaje;
}

function mostrarBodega(user) {
    loginScreen.hidden = true;
    warehouseScreen.hidden = false;
    userLabel.textContent = user?.nombreUsuario || user?.correoUsuario || 'Bodega';
}

function formatearFecha(value) {
    if (!value) return 'Sin fecha';
    return new Intl.DateTimeFormat('es-CO', {
        dateStyle: 'medium',
        timeStyle: 'short'
    }).format(new Date(value));
}

function labelEstado(estado) {
    return {
        PENDIENTE: 'Pendiente',
        EN_PREPARACION: 'En preparación',
        DESPACHADA: 'Despachada',
        ENTREGADA: 'Entregada'
    }[estado] || estado;
}

function siguienteEstado(estado) {
    return {
        PENDIENTE: 'EN_PREPARACION',
        EN_PREPARACION: 'DESPACHADA',
        DESPACHADA: 'ENTREGADA'
    }[estado];
}

function crearCampo(label, input) {
    const wrapper = document.createElement('label');
    wrapper.className = 'warehouse-field';
    wrapper.append(document.createTextNode(label), input);
    return wrapper;
}

function crearPedido(remision) {
    const card = document.createElement('article');
    card.className = 'warehouse-order';

    const header = document.createElement('div');
    header.className = 'warehouse-order-header';
    const heading = document.createElement('h3');
    heading.className = 'warehouse-order-title';
    heading.textContent = `Remisión #${remision.idRemision} · Venta #${remision.idVenta}`;
    const badge = document.createElement('span');
    badge.className = 'warehouse-state';
    badge.textContent = labelEstado(remision.estadoBodega);
    header.append(heading, badge);

    const meta = document.createElement('div');
    meta.className = 'warehouse-order-meta';
    const customer = document.createElement('span');
    customer.textContent = `Cliente: ${remision.nombreCliente}`;
    const date = document.createElement('span');
    date.textContent = `Creada: ${formatearFecha(remision.fechaEmision)}`;
    const address = document.createElement('span');
    address.textContent = `Entrega: ${remision.direccionEntrega || 'Sin dirección registrada'}`;
    meta.append(customer, date, address);

    if (remision.observacionesVenta) {
        const salesNotes = document.createElement('p');
        salesNotes.className = 'warehouse-notes';
        salesNotes.textContent = `Indicaciones de la venta: ${remision.observacionesVenta}`;
        card.appendChild(salesNotes);
    }

    const items = document.createElement('ul');
    items.className = 'warehouse-items';
    remision.detalles.forEach(detalle => {
        const item = document.createElement('li');
        const name = document.createElement('span');
        name.textContent = `${detalle.nombreProducto}${detalle.skuProducto ? ` · SKU ${detalle.skuProducto}` : ''}`;
        const quantity = document.createElement('strong');
        quantity.textContent = `× ${detalle.cantidad}`;
        item.append(name, quantity);
        items.appendChild(item);
    });

    card.append(header, meta, items);
    if (remision.observacionesBodega) {
        const notes = document.createElement('p');
        notes.className = 'warehouse-notes';
        notes.textContent = `Observaciones de Bodega: ${remision.observacionesBodega}`;
        card.appendChild(notes);
    }

    const form = document.createElement('form');
    form.className = 'warehouse-update-form';
    form.dataset.remisionId = remision.idRemision;
    const nextState = siguienteEstado(remision.estadoBodega);
    const stateSelect = document.createElement('select');
    stateSelect.name = 'estadoBodega';
    stateSelect.required = true;
    [remision.estadoBodega, nextState].filter(Boolean).forEach(state => {
        const option = document.createElement('option');
        option.value = state;
        option.textContent = labelEstado(state);
        option.selected = state === remision.estadoBodega;
        stateSelect.appendChild(option);
    });
    const notesInput = document.createElement('textarea');
    notesInput.name = 'observacionesBodega';
    notesInput.maxLength = 500;
    notesInput.value = remision.observacionesBodega || '';
    notesInput.placeholder = 'Agregar una observación para el sistema principal';
    const save = document.createElement('button');
    save.className = 'workflow-action';
    save.type = 'submit';
    save.textContent = 'Guardar actualización';
    form.append(
        crearCampo('Estado', stateSelect),
        crearCampo('Observaciones', notesInput),
        save
    );
    card.appendChild(form);
    return card;
}

function renderizarResumen(remisiones) {
    document.getElementById('totalPendientes').textContent =
        String(remisiones.filter(item => item.estadoBodega === 'PENDIENTE').length);
    document.getElementById('totalPreparacion').textContent =
        String(remisiones.filter(item => item.estadoBodega === 'EN_PREPARACION').length);
    document.getElementById('totalDespachadas').textContent =
        String(remisiones.filter(item => item.estadoBodega === 'DESPACHADA').length);
}

async function cargarRemisiones() {
    statusMessage.textContent = 'Cargando remisiones...';
    try {
        const remisiones = await request('/bodega/remisiones');
        renderizarResumen(remisiones);
        ordersList.replaceChildren();
        if (remisiones.length === 0) {
            const empty = document.createElement('p');
            empty.className = 'warehouse-empty';
            empty.textContent = 'No hay pedidos pendientes de preparación o entrega.';
            ordersList.appendChild(empty);
        } else {
            remisiones.forEach(remision => ordersList.appendChild(crearPedido(remision)));
        }
        statusMessage.textContent = `${remisiones.length} remisión(es) pendientes de entrega.`;
    } catch (error) {
        statusMessage.textContent = `No se pudieron cargar las remisiones: ${error.message}`;
    }
}

async function iniciarSesion(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const requestBody = Object.fromEntries(new FormData(form).entries());
    loginMessage.textContent = 'Validando acceso...';
    try {
        const response = await request('/auth/login', {
            method: 'POST',
            body: JSON.stringify(requestBody)
        });
        if (!['BODEGA', 'ADMINISTRADOR'].includes(response.usuario?.rolUsuario)) {
            throw new Error('Esta cuenta no tiene permisos para entrar a Bodega.');
        }
        localStorage.setItem('usuario', JSON.stringify(response.usuario));
        mostrarBodega(response.usuario);
        await cargarRemisiones();
    } catch (error) {
        loginMessage.textContent = error.message;
    }
}

async function actualizarRemision(event) {
    const form = event.target.closest('.warehouse-update-form');
    if (!form) return;
    event.preventDefault();
    const values = Object.fromEntries(new FormData(form).entries());
    const button = form.querySelector('button[type="submit"]');
    button.disabled = true;
    try {
        await request(`/bodega/remisiones/${encodeURIComponent(form.dataset.remisionId)}/estado`, {
            method: 'PATCH',
            body: JSON.stringify(values)
        });
        await cargarRemisiones();
    } catch (error) {
        statusMessage.textContent = `No se pudo actualizar la remisión: ${error.message}`;
        button.disabled = false;
    }
}

document.getElementById('formLoginBodega').addEventListener('submit', iniciarSesion);
document.getElementById('recargarRemisiones').addEventListener('click', cargarRemisiones);
ordersList.addEventListener('submit', actualizarRemision);
document.getElementById('cerrarSesion').addEventListener('click', () => {
    localStorage.removeItem('usuario');
    window.location.replace(config.mainLoginUrl);
});

const session = getSession();
if (['BODEGA', 'ADMINISTRADOR'].includes(session.user?.rolUsuario)) {
    mostrarBodega(session.user);
    cargarRemisiones();
} else {
    mostrarLogin();
}
