import { getData } from './api.js';

function dineroReporte(valor) {
    return `$${Number(valor || 0).toLocaleString('es-CO')}`;
}

function fechaReporte(fecha) {
    return fecha ? new Date(`${fecha}T00:00:00`).toLocaleDateString('es-CO') : '—';
}

function errorReporte(error) {
    try {
        return JSON.parse(error.message).mensaje || error.message;
    } catch {
        return error.message;
    }
}

function agregarFila(tbody, valores) {
    const row = tbody.insertRow();
    valores.forEach(valor => {
        row.insertCell().textContent = String(valor ?? '—');
    });
    return row;
}

function mostrarVacio(tbody, columnas, mensaje) {
    const row = tbody.insertRow();
    const cell = row.insertCell();
    cell.colSpan = columnas;
    cell.textContent = mensaje;
}

function renderizarDashboardReporte(reporte) {
    document.getElementById('reporteIngresosMes').textContent = dineroReporte(reporte.ingresosMes);
    document.getElementById('reporteVentasMes').textContent = String(reporte.ventasMes);
    document.getElementById('reporteCarteraCredito').textContent = dineroReporte(reporte.carteraCredito);
    document.getElementById('reporteStockCritico').textContent = String(reporte.productosStockCritico);

    const stock = document.getElementById('reporteStockTbody');
    stock.replaceChildren();
    if (reporte.stockBajo.length === 0) {
        mostrarVacio(stock, 4, 'No hay productos con stock crítico.');
    } else {
        reporte.stockBajo.forEach(producto => {
            const row = agregarFila(stock, [
                producto.idProducto,
                producto.nombreProducto,
                producto.stockActual,
                producto.stockMinimo
            ]);
            row.cells[1].className = 'report-danger';
            row.cells[2].className = 'report-danger';
        });
    }
    document.getElementById('reporteStockEstado').textContent =
        `${reporte.stockBajo.length} productos con stock crítico`;

    const productos = document.getElementById('reporteProductosTbody');
    productos.replaceChildren();
    if (reporte.productosMasVendidos.length === 0) {
        mostrarVacio(productos, 4, 'Aún no hay productos vendidos.');
    } else {
        reporte.productosMasVendidos.forEach(producto => agregarFila(productos, [
            producto.idProducto,
            producto.nombreProducto,
            producto.unidadesVendidas,
            dineroReporte(producto.ingresosGenerados)
        ]));
    }
    document.getElementById('reporteProductosEstado').textContent =
        `Mostrando ${reporte.productosMasVendidos.length} productos`;

    const clientes = document.getElementById('reporteClientesTbody');
    clientes.replaceChildren();
    if (reporte.clientesFrecuentes.length === 0) {
        mostrarVacio(clientes, 5, 'Aún no hay ventas para mostrar clientes frecuentes.');
    } else {
        reporte.clientesFrecuentes.forEach(cliente => agregarFila(clientes, [
            `${cliente.nombreCliente} (${cliente.idCliente})`,
            cliente.numeroCompras,
            dineroReporte(cliente.totalInvertido),
            dineroReporte(cliente.promedioCompra),
            fechaReporte(cliente.ultimaCompra)
        ]));
    }
    document.getElementById('reporteClientesEstado').textContent =
        `Mostrando ${reporte.clientesFrecuentes.length} clientes`;

    const meses = document.getElementById('reporteMesesTbody');
    meses.replaceChildren();
    reporte.ventasPorMes.forEach(mes => agregarFila(meses, [
        mes.nombreMes,
        mes.anio,
        mes.cantidadVentas,
        dineroReporte(mes.totalMes),
        dineroReporte(mes.promedioVenta)
    ]));
    document.getElementById('reporteMesesEstado').textContent =
        `Período actual: ${reporte.periodoActual}`;

    const creditos = document.getElementById('reporteCreditosTbody');
    creditos.replaceChildren();
    if (reporte.creditosPendientes.length === 0) {
        mostrarVacio(creditos, 6, 'No hay ventas registradas a crédito.');
    } else {
        reporte.creditosPendientes.forEach(credito => {
            const row = agregarFila(creditos, [
                credito.idVenta,
                credito.nombreCliente,
                fechaReporte(credito.fechaVenta),
                dineroReporte(credito.total),
                credito.diasTranscurridos,
                'Pendiente'
            ]);
            row.cells[5].className = 'report-state on-time';
        });
    }
    document.getElementById('reporteCreditosEstado').textContent =
        `${reporte.creditosPendientes.length} ventas marcadas a crédito`;
}

function inicializarReportes() {
    const tabs = [...document.querySelectorAll('[data-report-tab]')];
    const panels = [...document.querySelectorAll('[data-report-panel]')];
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(item => item.setAttribute('aria-selected', String(item === tab)));
            panels.forEach(panel => {
                panel.hidden = panel.dataset.reportPanel !== tab.dataset.reportTab;
            });
        });
    });
    getData('/reportes/dashboard')
        .then(renderizarDashboardReporte)
        .catch(error => {
            const message = `No se pudieron cargar los reportes: ${errorReporte(error)}`;
            document.querySelectorAll('[id$="Estado"]').forEach(status => {
                status.textContent = message;
            });
        });
}

window.inicializarReportes = inicializarReportes;
