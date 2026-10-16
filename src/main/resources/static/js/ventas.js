import { getData, postData } from './api.js';

let clientesVenta = [];
let productosVenta = [];
let clienteVentaSeleccionado = null;
let productoVentaSeleccionado = null;
let carritoVentaActual = [];

function formatearPesosVenta(valor) {
    return `$${Number(valor || 0).toLocaleString('es-CO')}`;
}

function mensajeErrorVenta(error) {
    try {
        return JSON.parse(error.message).mensaje || error.message;
    } catch {
        return error.message;
    }
}

function mostrarEstadoVenta(mensaje) {
    document.getElementById('estadoVentaMensaje').textContent = mensaje;
}

function mostrarClienteVenta(cliente) {
    clienteVentaSeleccionado = cliente;
    const result = document.getElementById('clienteVentaSeleccionado');
    const suggestions = document.getElementById('clienteVentaSugerencias');
    suggestions.replaceChildren();
    suggestions.hidden = true;
    if (!cliente) {
        result.hidden = true;
        return;
    }
    document.getElementById('nombreClienteVenta').textContent = cliente.nombreCliente;
    document.getElementById('telefonoClienteVenta').textContent = cliente.telefonoCliente ?? '—';
    document.getElementById('direccionClienteVenta').textContent = cliente.direccionCliente || '—';
    document.getElementById('direccionVenta').value = cliente.direccionCliente || '';
    result.hidden = false;
}

function seleccionarClienteVenta(cliente) {
    mostrarClienteVenta(cliente);
    document.getElementById('clienteVentaBusqueda').value = cliente.idCliente;
    document.getElementById('clienteVentaEstado').textContent = 'Cliente seleccionado.';
}

async function buscarClienteVenta() {
    const termino = document.getElementById('clienteVentaBusqueda').value.trim();
    const estado = document.getElementById('clienteVentaEstado');
    if (!termino) {
        mostrarClienteVenta(null);
        estado.textContent = 'Escribe el documento o nombre de un cliente.';
        return;
    }
    try {
        const clientes = await getData(`/clientes/buscar?criterio=${encodeURIComponent(termino)}`);
        clientesVenta = clientes;
        const exacto = clientes.find(cliente => cliente.idCliente.toLowerCase() === termino.toLowerCase());
        if (exacto || clientes.length === 1) {
            seleccionarClienteVenta(exacto || clientes[0]);
            return;
        }
        mostrarClienteVenta(null);
        if (clientes.length === 0) {
            estado.textContent = 'No se encontró un cliente con ese documento o nombre.';
            return;
        }
        const suggestions = document.getElementById('clienteVentaSugerencias');
        clientes.forEach(cliente => {
            const button = document.createElement('button');
            button.className = 'purchase-suggestion';
            button.type = 'button';
            button.dataset.clienteId = cliente.idCliente;
            button.textContent = `${cliente.nombreCliente} — ${cliente.idCliente}`;
            suggestions.appendChild(button);
        });
        suggestions.hidden = false;
        estado.textContent = 'Hay varios resultados; selecciona el cliente correcto.';
    } catch (error) {
        estado.textContent = `No se pudo buscar el cliente: ${mensajeErrorVenta(error)}`;
    }
}

function mostrarSugerenciasProductosVenta() {
    const input = document.getElementById('productoVentaBusqueda');
    const suggestions = document.getElementById('productoVentaSugerencias');
    const termino = input.value.trim().toLowerCase();
    suggestions.replaceChildren();
    if (!termino || productoVentaSeleccionado) {
        suggestions.hidden = true;
        return;
    }

    productosVenta
        .filter(producto => String(producto.idProducto).includes(termino) ||
            producto.nombreProducto.toLowerCase().includes(termino))
        .forEach(producto => {
            const button = document.createElement('button');
            button.className = 'purchase-suggestion';
            button.type = 'button';
            button.setAttribute('role', 'option');
            button.dataset.productoId = String(producto.idProducto);
            button.textContent = `${producto.nombreProducto} · ${formatearPesosVenta(producto.precioVentaProducto)} · Stock: ${producto.stockProducto}`;
            suggestions.appendChild(button);
        });
    suggestions.hidden = suggestions.childElementCount === 0;
}

function renderizarCarritoVenta() {
    const tbody = document.getElementById('carritoVenta');
    tbody.replaceChildren();
    if (carritoVentaActual.length === 0) {
        const row = tbody.insertRow();
        const cell = row.insertCell();
        cell.colSpan = 5;
        cell.className = 'empty-cart';
        cell.textContent = 'Agrega productos al carrito.';
    } else {
        carritoVentaActual.forEach((linea, index) => {
            const row = tbody.insertRow();
            [linea.nombre, linea.cantidad, formatearPesosVenta(linea.precio),
                formatearPesosVenta(linea.cantidad * linea.precio)].forEach(value => {
                row.insertCell().textContent = String(value);
            });
            const cell = row.insertCell();
            const button = document.createElement('button');
            button.className = 'table-icon-button';
            button.type = 'button';
            button.dataset.removeVenta = String(index);
            button.setAttribute('aria-label', 'Quitar');
            button.innerHTML = '<i class="fas fa-trash"></i>';
            cell.appendChild(button);
        });
    }

    const total = carritoVentaActual.reduce((sum, linea) => sum + linea.cantidad * linea.precio, 0);
    const unidades = carritoVentaActual.reduce((sum, linea) => sum + linea.cantidad, 0);
    document.getElementById('resumenItemsVenta').textContent = String(carritoVentaActual.length);
    document.getElementById('resumenUnidadesVenta').textContent = String(unidades);
    document.getElementById('totalVenta').textContent = formatearPesosVenta(total);
}

function agregarProductoVenta() {
    const cantidad = Number(document.getElementById('cantidadVentaProducto').value);
    if (!productoVentaSeleccionado || !Number.isSafeInteger(cantidad) || cantidad <= 0) {
        mostrarEstadoVenta('Selecciona un producto e ingresa una cantidad válida.');
        return;
    }
    const enCarrito = carritoVentaActual
        .filter(linea => linea.producto.idProducto === productoVentaSeleccionado.idProducto)
        .reduce((sum, linea) => sum + linea.cantidad, 0);
    if (cantidad + enCarrito > productoVentaSeleccionado.stockProducto) {
        mostrarEstadoVenta(`Stock insuficiente. Disponible: ${productoVentaSeleccionado.stockProducto - enCarrito}.`);
        return;
    }
    carritoVentaActual.push({
        producto: productoVentaSeleccionado,
        nombre: productoVentaSeleccionado.nombreProducto,
        cantidad,
        precio: Number(productoVentaSeleccionado.precioVentaProducto)
    });
    productoVentaSeleccionado = null;
    document.getElementById('productoVentaBusqueda').value = '';
    document.getElementById('cantidadVentaProducto').value = '1';
    document.getElementById('productoVentaSugerencias').hidden = true;
    mostrarEstadoVenta('Producto agregado al carrito.');
    renderizarCarritoVenta();
}

async function finalizarVenta() {
    if (!clienteVentaSeleccionado || carritoVentaActual.length === 0) {
        mostrarEstadoVenta('Selecciona un cliente y agrega al menos un producto.');
        return;
    }
    const direccion = document.getElementById('direccionVenta').value.trim();
    const fechaVenta = document.getElementById('fechaVenta').value;
    if (!direccion || !fechaVenta) {
        mostrarEstadoVenta('La dirección de entrega y la fecha de venta son obligatorias.');
        return;
    }

    const request = {
        idCliente: clienteVentaSeleccionado.idCliente,
        fechaVenta,
        estadoPago: document.getElementById('estadoPagoVenta').value,
        direccionEntrega: direccion,
        observacionesRemision: document.getElementById('observacionesRemisionVenta').value.trim(),
        detalles: carritoVentaActual.map(linea => ({
            idProducto: linea.producto.idProducto,
            cantidad: linea.cantidad
        }))
    };
    let venta;
    try {
        venta = await postData('/ventas', request);
    } catch (error) {
        mostrarEstadoVenta(`No se pudo registrar la venta: ${mensajeErrorVenta(error)}`);
        return;
    }

    carritoVentaActual = [];
    clienteVentaSeleccionado = null;
    productoVentaSeleccionado = null;
    document.getElementById('clienteVentaBusqueda').value = '';
    document.getElementById('direccionVenta').value = '';
    document.getElementById('observacionesRemisionVenta').value = '';
    document.getElementById('clienteVentaEstado').textContent = 'Busca y selecciona un cliente registrado.';
    mostrarClienteVenta(null);
    renderizarCarritoVenta();
    mostrarEstadoVenta(`Venta #${venta.idVenta} registrada. Remisión #${venta.remision.idRemision} generada.`);

    try {
        productosVenta = await getData('/productos');
    } catch (error) {
        mostrarEstadoVenta(`Venta #${venta.idVenta} y remisión registradas; no se pudo actualizar el catálogo: ${mensajeErrorVenta(error)}`);
    }
}

async function inicializarVentas() {
    const fechaActual = new Date();
    document.getElementById('fechaVenta').value = new Date(
        fechaActual.getTime() - fechaActual.getTimezoneOffset() * 60000
    ).toISOString().slice(0, 10);
    document.getElementById('buscarClienteVenta').addEventListener('click', buscarClienteVenta);
    document.getElementById('clienteVentaBusqueda').addEventListener('keydown', event => {
        if (event.key === 'Enter') {
            event.preventDefault();
            buscarClienteVenta();
        }
    });
    document.getElementById('clienteVentaBusqueda').addEventListener('input', () => {
        mostrarClienteVenta(null);
        document.getElementById('clienteVentaEstado').textContent = 'Busca y selecciona un cliente registrado.';
    });
    document.getElementById('clienteVentaSugerencias').addEventListener('click', event => {
        const button = event.target.closest('[data-cliente-id]');
        const cliente = clientesVenta.find(item => item.idCliente === button?.dataset.clienteId);
        if (cliente) seleccionarClienteVenta(cliente);
    });
    document.getElementById('productoVentaBusqueda').addEventListener('input', () => {
        productoVentaSeleccionado = null;
        mostrarSugerenciasProductosVenta();
    });
    document.getElementById('productoVentaSugerencias').addEventListener('click', event => {
        const button = event.target.closest('[data-producto-id]');
        productoVentaSeleccionado = productosVenta.find(item =>
            item.idProducto === Number(button?.dataset.productoId)) || null;
        if (productoVentaSeleccionado) {
            document.getElementById('productoVentaBusqueda').value = productoVentaSeleccionado.nombreProducto;
            document.getElementById('productoVentaSugerencias').hidden = true;
        }
    });
    document.getElementById('agregarProductoVenta').addEventListener('click', agregarProductoVenta);
    document.getElementById('carritoVenta').addEventListener('click', event => {
        const button = event.target.closest('[data-remove-venta]');
        if (!button) return;
        carritoVentaActual.splice(Number(button.dataset.removeVenta), 1);
        renderizarCarritoVenta();
    });
    document.getElementById('finalizarVenta').addEventListener('click', finalizarVenta);
    renderizarCarritoVenta();

    try {
        [clientesVenta, productosVenta] = await Promise.all([
            getData('/clientes'),
            getData('/productos')
        ]);
    } catch (error) {
        mostrarEstadoVenta(`No se pudieron cargar los datos de ventas: ${mensajeErrorVenta(error)}`);
    }
}

window.inicializarVentas = inicializarVentas;
