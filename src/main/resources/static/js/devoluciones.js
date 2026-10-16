import { getData, postData } from './api.js';

let productosDevolucion = [];
let devolucionesRegistradas = [];
let ventasDevolucion = [];
let productoDevolucionSeleccionado = null;

function errorDevolucion(error) {
    try {
        return JSON.parse(error.message).mensaje || error.message;
    } catch {
        return error.message;
    }
}

function fechaLocalDevolucion(fecha) {
    return new Date(fecha).toLocaleString('es-CO');
}

function mesActualDevolucion() {
    const hoy = new Date();
    return `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}`;
}

function actualizarKpisDevolucion() {
    const mes = mesActualDevolucion();
    const devolucionesMes = devolucionesRegistradas.filter(item =>
        item.fechaDevolucion.startsWith(mes));
    const unidadesDevueltas = devolucionesMes.reduce((total, item) => total + item.cantidad, 0);
    const unidadesVendidas = ventasDevolucion
        .filter(venta => venta.fechaVenta.startsWith(mes))
        .reduce((total, venta) => total + venta.detalles.reduce(
            (subtotal, detalle) => subtotal + detalle.cantidad, 0), 0);
    const tasa = unidadesVendidas === 0 ? 0 : unidadesDevueltas * 100 / unidadesVendidas;
    document.getElementById('kpiDevolucionesMes').textContent = String(devolucionesMes.length);
    document.getElementById('kpiUnidadesDevueltas').textContent = String(unidadesDevueltas);
    document.getElementById('kpiTasaDevolucion').textContent = `${tasa.toFixed(1)}%`;
}

function renderizarDevoluciones() {
    const tbody = document.getElementById('devolucionesTbody');
    tbody.replaceChildren();
    if (devolucionesRegistradas.length === 0) {
        const row = tbody.insertRow();
        const cell = row.insertCell();
        cell.colSpan = 6;
        cell.className = 'empty-cart';
        cell.textContent = 'No hay devoluciones registradas.';
        return;
    }
    devolucionesRegistradas.forEach(devolucion => {
        const row = tbody.insertRow();
        [
            devolucion.idDevolucion,
            devolucion.nombreProducto,
            devolucion.nombreCliente || '—',
            devolucion.cantidad,
            devolucion.motivo || '—',
            fechaLocalDevolucion(devolucion.fechaDevolucion)
        ].forEach(value => {
            row.insertCell().textContent = String(value);
        });
    });
}

function mostrarSugerenciasDevolucion() {
    const termino = document.getElementById('productoDevolucionBusqueda').value.trim().toLowerCase();
    const suggestions = document.getElementById('productoDevolucionSugerencias');
    suggestions.replaceChildren();
    if (!termino || productoDevolucionSeleccionado) {
        suggestions.hidden = true;
        return;
    }
    productosDevolucion
        .filter(producto => String(producto.idProducto).includes(termino) ||
            producto.nombreProducto.toLowerCase().includes(termino))
        .forEach(producto => {
            const button = document.createElement('button');
            button.className = 'purchase-suggestion';
            button.type = 'button';
            button.dataset.productoId = String(producto.idProducto);
            button.textContent = `${producto.nombreProducto} · Stock actual: ${producto.stockProducto}`;
            suggestions.appendChild(button);
        });
    suggestions.hidden = suggestions.childElementCount === 0;
}

async function cargarDevoluciones() {
    [productosDevolucion, devolucionesRegistradas, ventasDevolucion] = await Promise.all([
        getData('/productos'),
        getData('/devoluciones'),
        getData('/ventas')
    ]);
    actualizarKpisDevolucion();
    renderizarDevoluciones();
}

async function registrarDevolucion() {
    const cantidad = Number(document.getElementById('cantidadDevolucion').value);
    const fechaDevolucion = document.getElementById('fechaDevolucion').value;
    if (!productoDevolucionSeleccionado || !Number.isSafeInteger(cantidad) || cantidad <= 0 ||
        !fechaDevolucion) {
        document.getElementById('estadoDevolucion').textContent =
            'Selecciona un producto e ingresa una cantidad y fecha válidas.';
        return;
    }
    const request = {
        idProducto: productoDevolucionSeleccionado.idProducto,
        nombreCliente: document.getElementById('nombreClienteDevolucion').value.trim(),
        cantidad,
        motivo: document.getElementById('motivoDevolucion').value.trim(),
        fechaDevolucion
    };
    try {
        const response = await postData('/devoluciones', request);
        document.getElementById('estadoDevolucion').textContent =
            `Devolución #${response.idDevolucion} registrada; existencias actualizadas.`;
        document.getElementById('productoDevolucionBusqueda').value = '';
        document.getElementById('nombreClienteDevolucion').value = '';
        document.getElementById('motivoDevolucion').value = '';
        document.getElementById('cantidadDevolucion').value = '1';
        productoDevolucionSeleccionado = null;
        document.getElementById('productoDevolucionSugerencias').hidden = true;
        try {
            await cargarDevoluciones();
        } catch (error) {
            document.getElementById('estadoDevolucion').textContent =
                `Devolución registrada, pero no se pudo actualizar la vista: ${errorDevolucion(error)}`;
        }
    } catch (error) {
        document.getElementById('estadoDevolucion').textContent =
            `No se pudo registrar la devolución: ${errorDevolucion(error)}`;
    }
}

async function inicializarDevoluciones() {
    const ahora = new Date();
    document.getElementById('fechaDevolucion').value = new Date(
        ahora.getTime() - ahora.getTimezoneOffset() * 60000
    ).toISOString().slice(0, 16);
    document.getElementById('productoDevolucionBusqueda').addEventListener('input', () => {
        productoDevolucionSeleccionado = null;
        mostrarSugerenciasDevolucion();
    });
    document.getElementById('productoDevolucionSugerencias').addEventListener('click', event => {
        const button = event.target.closest('[data-producto-id]');
        productoDevolucionSeleccionado = productosDevolucion.find(producto =>
            String(producto.idProducto) === button?.dataset.productoId) || null;
        if (productoDevolucionSeleccionado) {
            document.getElementById('productoDevolucionBusqueda').value =
                productoDevolucionSeleccionado.nombreProducto;
            document.getElementById('productoDevolucionSugerencias').hidden = true;
        }
    });
    document.getElementById('registrarDevolucion').addEventListener('click', registrarDevolucion);
    try {
        await cargarDevoluciones();
    } catch (error) {
        document.getElementById('estadoDevolucion').textContent =
            `No se pudieron cargar los datos: ${errorDevolucion(error)}`;
    }
}

window.inicializarDevoluciones = inicializarDevoluciones;
