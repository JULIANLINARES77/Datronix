import { getData, postData } from './api.js';

let proveedoresCompra = [];
let productosCompra = [];
let comprasRegistradas = [];
let proveedorCompraSeleccionado = null;
let productoCompraSeleccionado = null;
let filasCompraActuales = [];

function formatearPesosCompra(valor) {
    return `$${Number(valor || 0).toLocaleString('es-CO')}`;
}

function mostrarEstadoCompra(mensaje) {
    document.getElementById('purchaseStatus').textContent = mensaje;
}

function mostrarProveedorCompra() {
    const card = document.getElementById('proveedorSeleccionado');
    const message = document.getElementById('proveedorMessage');
    if (!proveedorCompraSeleccionado) {
        card.hidden = true;
        message.hidden = false;
        return;
    }

    card.replaceChildren();
    const name = document.createElement('strong');
    name.textContent = proveedorCompraSeleccionado.nombreEmpresa;
    const details = document.createElement('p');
    details.textContent = `ID: ${proveedorCompraSeleccionado.idProveedor} · Contacto: ${proveedorCompraSeleccionado.contactoProveedor || '—'}`;
    card.append(name, details);
    card.hidden = false;
    message.hidden = true;
}

function buscarProveedorCompra() {
    const termino = document.getElementById('proveedorBusqueda').value.trim().toLowerCase();
    if (!termino) {
        proveedorCompraSeleccionado = null;
        mostrarProveedorCompra();
        mostrarEstadoCompra('Escribe el ID o el nombre de un proveedor.');
        return;
    }
    proveedorCompraSeleccionado = proveedoresCompra.find(proveedor =>
        proveedor.idProveedor.toLowerCase() === termino ||
        proveedor.nombreEmpresa.toLowerCase().includes(termino)
    ) || null;
    mostrarProveedorCompra();
    mostrarEstadoCompra(proveedorCompraSeleccionado
        ? 'Proveedor seleccionado.'
        : 'No se encontró un proveedor con ese ID o nombre.');
}

function mensajeErrorCompra(error) {
    try {
        return JSON.parse(error.message).mensaje || error.message;
    } catch {
        return error.message;
    }
}

function mostrarSugerenciasProducto() {
    const input = document.getElementById('productoBusqueda');
    const lista = document.getElementById('productoSugerencias');
    const termino = input.value.trim().toLowerCase();
    lista.replaceChildren();
    if (!termino || productoCompraSeleccionado) {
        lista.hidden = true;
        return;
    }

    productosCompra
        .filter(producto => producto.nombreProducto.toLowerCase().includes(termino))
        .forEach(producto => {
            const button = document.createElement('button');
            button.className = 'purchase-suggestion';
            button.type = 'button';
            button.setAttribute('role', 'option');
            button.dataset.productoId = String(producto.idProducto);
            button.textContent = `${producto.nombreProducto} — ${formatearPesosCompra(producto.precioCostoProducto)}`;
            lista.appendChild(button);
        });
    lista.hidden = lista.childElementCount === 0;
}

function seleccionarProductoCompra(producto) {
    productoCompraSeleccionado = producto;
    document.getElementById('precioProducto').value = producto.precioCostoProducto;
    document.getElementById('productoBusqueda').value = producto.nombreProducto;
    document.getElementById('productoSugerencias').hidden = true;
}

function renderizarFilasCompra() {
    const tbody = document.getElementById('filasCompra');
    tbody.replaceChildren();

    if (filasCompraActuales.length === 0) {
        const emptyRow = tbody.insertRow();
        const cell = emptyRow.insertCell();
        cell.colSpan = 5;
        cell.className = 'empty-cart';
        cell.textContent = 'Agrega al menos un producto.';
    } else {
        filasCompraActuales.forEach((fila, index) => {
            const row = tbody.insertRow();
            [fila.nombre, fila.cantidad, formatearPesosCompra(fila.precio),
                formatearPesosCompra(fila.cantidad * fila.precio)].forEach(value => {
                row.insertCell().textContent = String(value);
            });
            const actionCell = row.insertCell();
            const removeButton = document.createElement('button');
            removeButton.className = 'table-icon-button';
            removeButton.type = 'button';
            removeButton.dataset.removePurchaseRow = String(index);
            removeButton.setAttribute('aria-label', 'Quitar');
            removeButton.innerHTML = '<i class="fas fa-trash"></i>';
            actionCell.appendChild(removeButton);
        });
    }

    const total = filasCompraActuales.reduce((sum, line) => sum + line.cantidad * line.precio, 0);
    document.getElementById('totalCompra').textContent = formatearPesosCompra(total);
}

function agregarFilaCompra() {
    const cantidad = Number(document.getElementById('cantidadProducto').value);
    const precio = Number(document.getElementById('precioProducto').value);
    if (!productoCompraSeleccionado || !Number.isSafeInteger(cantidad) || cantidad <= 0 ||
        !Number.isFinite(precio) || precio < 0) {
        mostrarEstadoCompra('Selecciona un producto e ingresa una cantidad y un precio válidos.');
        return;
    }

    filasCompraActuales.push({
        productoId: productoCompraSeleccionado.idProducto,
        nombre: productoCompraSeleccionado.nombreProducto,
        cantidad,
        precio
    });
    productoCompraSeleccionado = null;
    document.getElementById('productoBusqueda').value = '';
    document.getElementById('cantidadProducto').value = '1';
    document.getElementById('precioProducto').value = '';
    mostrarEstadoCompra('Producto agregado a la compra.');
    renderizarFilasCompra();
}

function actualizarResumenCompras() {
    const ahora = new Date();
    const mesActual = `${ahora.getFullYear()}-${String(ahora.getMonth() + 1).padStart(2, '0')}`;
    const delMes = comprasRegistradas.filter(compra => compra.fechaCompra.startsWith(mesActual));
    const proveedores = new Set(delMes.map(compra => compra.idProveedor));
    const unidades = delMes.reduce((sum, compra) =>
        sum + compra.detalles.reduce((detailSum, detail) => detailSum + detail.cantidad, 0), 0);
    document.getElementById('kpiComprasMes').textContent = String(delMes.length);
    document.getElementById('kpiTotalComprado').textContent =
        formatearPesosCompra(delMes.reduce((sum, compra) => sum + compra.totalCompra, 0));
    document.getElementById('kpiProveedoresCompra').textContent = String(proveedores.size);
    document.getElementById('kpiUnidadesCompra').textContent = String(unidades);
}

function renderizarCompras() {
    const tbody = document.getElementById('comprasTbody');
    const fecha = document.getElementById('filtroFechaCompra').value;
    const proveedor = document.getElementById('filtroProveedorCompra').value.trim().toLowerCase();
    const filtradas = comprasRegistradas.filter(compra =>
        (!fecha || compra.fechaCompra === fecha) &&
        (!proveedor || compra.nombreEmpresa.toLowerCase().includes(proveedor) ||
            compra.idProveedor.toLowerCase().includes(proveedor))
    );
    tbody.replaceChildren();

    if (filtradas.length === 0) {
        const row = tbody.insertRow();
        const cell = row.insertCell();
        cell.colSpan = 5;
        cell.className = 'empty-cart';
        cell.textContent = 'No hay compras para mostrar.';
        return;
    }

    filtradas.forEach(compra => {
        const row = tbody.insertRow();
        [compra.idCompra, compra.fechaCompra, compra.nombreEmpresa,
            formatearPesosCompra(compra.totalCompra)].forEach(value => {
            row.insertCell().textContent = String(value);
        });
        const actionCell = row.insertCell();
        const detailButton = document.createElement('button');
        detailButton.className = 'purchase-detail-link';
        detailButton.type = 'button';
        detailButton.dataset.purchaseDetail = String(compra.idCompra);
        detailButton.setAttribute('aria-label', 'Ver detalle');
        detailButton.innerHTML = '<i class="fas fa-eye"></i>';
        actionCell.appendChild(detailButton);
    });
}

async function cargarCatalogos() {
    [proveedoresCompra, productosCompra] = await Promise.all([
        getData('/proveedores'),
        getData('/productos')
    ]);
}

async function cargarCompras() {
    comprasRegistradas = await getData('/compras');
    actualizarResumenCompras();
    renderizarCompras();
}

async function mostrarDetalleCompra(idCompra) {
    try {
        const compra = await getData(`/compras/${idCompra}`);
        document.getElementById('tituloCompraDetalle').textContent = `Detalle de compra #${compra.idCompra}`;
        document.getElementById('detalleFecha').textContent = compra.fechaCompra;
        document.getElementById('detalleProveedor').textContent = compra.nombreEmpresa;
        document.getElementById('detalleTotal').textContent = formatearPesosCompra(compra.totalCompra);
        const tbody = document.getElementById('detalleProductos');
        tbody.replaceChildren();
        compra.detalles.forEach(detalle => {
            const row = tbody.insertRow();
            [detalle.nombreProducto, detalle.cantidad, formatearPesosCompra(detalle.precioUnitario),
                formatearPesosCompra(detalle.subtotal)].forEach(value => {
                row.insertCell().textContent = String(value);
            });
        });
        document.getElementById('dialogCompraDetalle').showModal();
    } catch (error) {
        mostrarEstadoCompra(`No se pudo cargar el detalle: ${mensajeErrorCompra(error)}`);
    }
}

async function registrarCompra() {
    if (!proveedorCompraSeleccionado || filasCompraActuales.length === 0) {
        mostrarEstadoCompra('Selecciona un proveedor y agrega al menos un producto.');
        return;
    }

    const request = {
        idProveedor: proveedorCompraSeleccionado.idProveedor,
        fechaCompra: document.getElementById('fechaCompra').value,
        detalles: filasCompraActuales.map(linea => ({
            idProducto: linea.productoId,
            cantidad: linea.cantidad,
            precioUnitario: linea.precio.toFixed(2)
        }))
    };

    try {
        await postData('/compras', request);
    } catch (error) {
        mostrarEstadoCompra(`No se pudo registrar la compra: ${mensajeErrorCompra(error)}`);
        return;
    }

    filasCompraActuales = [];
    productoCompraSeleccionado = null;
    proveedorCompraSeleccionado = null;
    document.getElementById('proveedorBusqueda').value = '';
    mostrarProveedorCompra();
    renderizarFilasCompra();
    try {
        await Promise.all([cargarCatalogos(), cargarCompras()]);
        mostrarEstadoCompra('Compra registrada y existencias actualizadas.');
    } catch (error) {
        mostrarEstadoCompra(`Compra registrada, pero no se pudo actualizar la vista: ${mensajeErrorCompra(error)}`);
    }
}

async function inicializarCompras() {
    const fechaActual = new Date();
    const fechaLocal = new Date(fechaActual.getTime() - fechaActual.getTimezoneOffset() * 60000)
        .toISOString()
        .slice(0, 10);
    document.getElementById('fechaCompra').value = fechaLocal;
    document.getElementById('filtroFechaCompra').value = '';
    document.getElementById('buscarProveedor').addEventListener('click', buscarProveedorCompra);
    document.getElementById('proveedorBusqueda').addEventListener('keydown', event => {
        if (event.key === 'Enter') {
            event.preventDefault();
            buscarProveedorCompra();
        }
    });
    document.getElementById('productoBusqueda').addEventListener('input', mostrarSugerenciasProducto);
    document.getElementById('productoSugerencias').addEventListener('click', event => {
        const button = event.target.closest('[data-producto-id]');
        const producto = productosCompra.find(item => item.idProducto === Number(button?.dataset.productoId));
        if (producto) seleccionarProductoCompra(producto);
    });
    document.getElementById('agregarProductoCompra').addEventListener('click', agregarFilaCompra);
    document.getElementById('registrarCompra').addEventListener('click', registrarCompra);
    document.getElementById('filasCompra').addEventListener('click', event => {
        const button = event.target.closest('[data-remove-purchase-row]');
        if (!button) return;
        filasCompraActuales.splice(Number(button.dataset.removePurchaseRow), 1);
        renderizarFilasCompra();
    });
    document.getElementById('comprasTbody').addEventListener('click', event => {
        const button = event.target.closest('[data-purchase-detail]');
        if (button) mostrarDetalleCompra(Number(button.dataset.purchaseDetail));
    });
    document.getElementById('filtroFechaCompra').addEventListener('change', renderizarCompras);
    document.getElementById('filtroProveedorCompra').addEventListener('input', renderizarCompras);

    const dialog = document.getElementById('dialogCompraDetalle');
    dialog.querySelector('[data-close-dialog]')?.addEventListener('click', () => dialog.close());
    dialog.addEventListener('click', event => {
        if (event.target === dialog) dialog.close();
    });
    renderizarFilasCompra();

    try {
        await Promise.all([cargarCatalogos(), cargarCompras()]);
    } catch (error) {
        const message = mensajeErrorCompra(error);
        mostrarEstadoCompra(`No se pudieron cargar los datos de compras: ${message}`);
        document.getElementById('comprasTbody').textContent =
            `Error al cargar compras: ${message}`;
    }
}

window.inicializarCompras = inicializarCompras;
