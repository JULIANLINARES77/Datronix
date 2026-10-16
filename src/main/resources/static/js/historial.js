import { getData } from './api.js';

let ventasHistorial = [];

function dineroHistorial(valor) {
    return `$${Number(valor || 0).toLocaleString('es-CO')}`;
}

function fechaHistorial(fecha) {
    if (!fecha) return '—';
    return new Date(`${fecha}T00:00:00`).toLocaleDateString('es-CO');
}

function estadoBodegaHistorial(estado) {
    return {
        PENDIENTE: 'Pendiente',
        EN_PREPARACION: 'En preparación',
        DESPACHADA: 'Despachada',
        ENTREGADA: 'Entregada'
    }[estado] || 'Pendiente';
}

function mensajeErrorHistorial(error) {
    try {
        return JSON.parse(error.message).mensaje || error.message;
    } catch {
        return error.message;
    }
}

function actualizarKpisHistorial() {
    const ahora = new Date();
    const mes = `${ahora.getFullYear()}-${String(ahora.getMonth() + 1).padStart(2, '0')}`;
    const ventasMes = ventasHistorial.filter(venta => venta.fechaVenta.startsWith(mes));
    const contado = ventasMes.filter(venta => venta.estadoPago === 'CONTADO').length;
    const credito = ventasMes.filter(venta => venta.estadoPago === 'CREDITO').length;
    const cantidad = ventasMes.length || 1;
    document.getElementById('kpiVentasMes').textContent = String(ventasMes.length);
    document.getElementById('kpiTotalFacturado').textContent =
        dineroHistorial(ventasMes.reduce((total, venta) => total + Number(venta.totalVenta), 0));
    document.getElementById('kpiVentasContado').textContent = `${Math.round(contado * 100 / cantidad)}%`;
    document.getElementById('kpiVentasCredito').textContent = `${Math.round(credito * 100 / cantidad)}%`;
}

function renderizarHistorial() {
    const tbody = document.getElementById('historialVentasTbody');
    const filtro = document.getElementById('buscarHistorialVentas').value.trim().toLowerCase();
    const filtradas = ventasHistorial.filter(venta =>
        String(venta.idVenta).includes(filtro) ||
        String(venta.nombreCliente || '').toLowerCase().includes(filtro) ||
        String(venta.idCliente || '').toLowerCase().includes(filtro)
    );
    tbody.replaceChildren();
    if (filtradas.length === 0) {
        const row = tbody.insertRow();
        const cell = row.insertCell();
        cell.colSpan = 9;
        cell.textContent = 'No hay ventas que coincidan con la búsqueda.';
    } else {
        filtradas.forEach(venta => {
            const row = tbody.insertRow();
            [
                venta.idVenta,
                venta.nombreCliente,
                fechaHistorial(venta.fechaVenta),
                dineroHistorial(venta.totalVenta)
            ].forEach(value => {
                row.insertCell().textContent = String(value);
            });
            const statusCell = row.insertCell();
            const status = document.createElement('span');
            status.className = `payment-status ${venta.estadoPago === 'CONTADO' ? 'paid' : 'credit'}`;
            status.textContent = venta.estadoPago;
            statusCell.appendChild(status);
            row.insertCell().textContent = venta.direccionEntrega;
            row.insertCell().textContent = venta.remision?.idRemision ?? '—';
            row.insertCell().textContent = venta.remision
                ? estadoBodegaHistorial(venta.remision.estadoBodega)
                : '—';
            const detailCell = row.insertCell();
            const button = document.createElement('button');
            button.className = 'workflow-action secondary history-detail-button';
            button.type = 'button';
            button.dataset.ventaId = String(venta.idVenta);
            button.innerHTML = '<i class="fas fa-eye"></i> Ver detalle';
            detailCell.appendChild(button);
        });
    }
    document.getElementById('historialVentasEstado').textContent =
        `Mostrando ${filtradas.length} de ${ventasHistorial.length} ventas`;
}

async function mostrarDetalleVenta(idVenta) {
    const dialog = document.getElementById('dialogVentaDetalle');
    try {
        const venta = await getData(`/ventas/${idVenta}`);
        document.getElementById('tituloVentaDetalle').textContent =
            `Venta #${venta.idVenta} — ${venta.nombreCliente}`;
        document.getElementById('detalleVentaFecha').textContent = fechaHistorial(venta.fechaVenta);
        document.getElementById('detalleVentaPago').textContent = venta.estadoPago;
        document.getElementById('detalleVentaDireccion').textContent = venta.direccionEntrega;
        const tbody = document.getElementById('detalleVentaProductos');
        tbody.replaceChildren();
        venta.detalles.forEach(detalle => {
            const row = tbody.insertRow();
            [
                detalle.idDetalleVenta,
                detalle.idProducto,
                detalle.nombreProducto,
                detalle.cantidad,
                dineroHistorial(detalle.precioUnitario),
                dineroHistorial(detalle.descuento),
                dineroHistorial(detalle.subtotal)
            ].forEach(value => {
                row.insertCell().textContent = String(value);
            });
        });
        const remision = venta.remision;
        document.getElementById('detalleRemisionTitulo').textContent =
            remision ? `Remisión #${remision.idRemision}` : 'Sin remisión registrada';
        document.getElementById('detalleRemisionResumen').textContent = remision
            ? `Emitida el ${new Date(remision.fechaEmision).toLocaleString('es-CO')} · Total ${dineroHistorial(remision.totalRemision)}`
            : '';
        document.getElementById('detalleRemisionEstado').textContent = remision
            ? `Estado de Bodega: ${estadoBodegaHistorial(remision.estadoBodega)}`
            : '';
        document.getElementById('detalleRemisionObservaciones').textContent = remision
            ? [
                remision.observaciones ? `Observaciones: ${remision.observaciones}` : '',
                remision.observacionesBodega
                    ? `Observaciones de Bodega: ${remision.observacionesBodega}` : ''
            ].filter(Boolean).join(' · ')
            : '';
        document.getElementById('imprimirRemisionVenta').disabled = !remision;
        dialog.showModal();
    } catch (error) {
        document.getElementById('historialVentasEstado').textContent =
            `No se pudo cargar el detalle: ${mensajeErrorHistorial(error)}`;
    }
}

async function inicializarHistorial() {
    const dialog = document.getElementById('dialogVentaDetalle');
    dialog.querySelector('[data-close-dialog]')?.addEventListener('click', () => dialog.close());
    dialog.addEventListener('click', event => {
        if (event.target === dialog) dialog.close();
    });
    document.getElementById('buscarHistorialVentas').addEventListener('input', renderizarHistorial);
    document.getElementById('historialVentasTbody').addEventListener('click', event => {
        const button = event.target.closest('[data-venta-id]');
        if (button) mostrarDetalleVenta(Number(button.dataset.ventaId));
    });
    document.getElementById('imprimirRemisionVenta').addEventListener('click', () => window.print());
    try {
        ventasHistorial = await getData('/ventas');
        actualizarKpisHistorial();
        renderizarHistorial();
    } catch (error) {
        document.getElementById('historialVentasEstado').textContent =
            `No se pudo cargar el historial: ${mensajeErrorHistorial(error)}`;
    }
}

window.inicializarHistorial = inicializarHistorial;
