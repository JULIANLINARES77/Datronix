import { getData } from './api.js';

function dineroDashboard(valor) {
    return `$${Number(valor || 0).toLocaleString('es-CO')}`;
}

function fechaDashboard(fecha) {
    return fecha ? new Date(`${fecha}T00:00:00`).toLocaleDateString('es-CO') : '—';
}

function renderizarIndicadores(reporte) {
    document.getElementById('dashboardIngresos').textContent = dineroDashboard(reporte.ingresosMes);
    document.getElementById('dashboardPeriodo').textContent = `Período ${reporte.periodoActual}`;
    const variacion = reporte.variacionIngresosPorcentual;
    document.getElementById('dashboardVariacionIngresos').textContent =
        variacion === null || variacion === undefined
        ? 'Sin base para comparar con el mes anterior.'
        : `${variacion > 0 ? '+' : ''}${Number(variacion).toLocaleString('es-CO', {
            maximumFractionDigits: 1
        })}% vs. mes anterior`;
    document.getElementById('dashboardStockCritico').textContent = String(reporte.productosStockCritico);
    document.getElementById('dashboardCartera').textContent = dineroDashboard(reporte.carteraCredito);
    document.getElementById('dashboardVentas').textContent = String(reporte.ventasMes);

    const chart = document.getElementById('dashboardGraficoVentas');
    chart.replaceChildren();
    const meses = [...reporte.ventasPorMes].reverse();
    const maximo = Math.max(0, ...meses.map(mes => Number(mes.totalMes)));
    meses.forEach(mes => {
        const column = document.createElement('div');
        column.className = 'sales-chart-column';
        const bar = document.createElement('div');
        bar.className = 'sales-chart-bar';
        bar.style.setProperty('--bar-height', `${maximo === 0 ? 0 : Number(mes.totalMes) * 100 / maximo}%`);
        bar.setAttribute('title', `${mes.nombreMes} ${mes.anio}: ${dineroDashboard(mes.totalMes)}`);
        const label = document.createElement('span');
        label.className = 'sales-chart-label';
        label.textContent = mes.nombreMes.slice(0, 3);
        column.append(bar, label);
        chart.appendChild(column);
    });
    chart.setAttribute('aria-label', 'Ventas mensuales de los últimos doce meses.');
    document.getElementById('dashboardEstado').textContent =
        `Indicadores actualizados para ${reporte.periodoActual}.`;
}

function renderizarUltimasVentas(ventas) {
    const tbody = document.getElementById('dashboardUltimasVentas');
    tbody.replaceChildren();
    if (ventas.length === 0) {
        const row = tbody.insertRow();
        const cell = row.insertCell();
        cell.colSpan = 5;
        cell.textContent = 'No hay ventas registradas.';
    } else {
        ventas.forEach(venta => {
            const row = tbody.insertRow();
            [
                venta.idVenta,
                venta.nombreCliente || '—',
                fechaDashboard(venta.fechaVenta),
                dineroDashboard(venta.totalVenta)
            ].forEach(value => {
                row.insertCell().textContent = String(value);
            });
            const cell = row.insertCell();
            const status = document.createElement('span');
            status.className = `payment-status ${venta.estadoPago === 'CONTADO' ? 'paid' : 'credit'}`;
            status.textContent = venta.estadoPago || '—';
            cell.appendChild(status);
        });
    }
    document.getElementById('dashboardVentasEstado').textContent =
        `Mostrando ${ventas.length} ventas recientes`;
}

async function inicializarDashboard() {
    const [reporte, ventas] = await Promise.allSettled([
        getData('/reportes/dashboard'),
        getData('/ventas/recientes')
    ]);
    if (reporte.status === 'fulfilled') {
        renderizarIndicadores(reporte.value);
    } else {
        document.getElementById('dashboardEstado').textContent =
            `No se pudieron cargar los indicadores: ${reporte.reason.message}`;
    }
    if (ventas.status === 'fulfilled') {
        renderizarUltimasVentas(ventas.value);
    } else {
        document.getElementById('dashboardVentasEstado').textContent =
            `No se pudieron cargar las ventas recientes: ${ventas.reason.message}`;
    }
}

window.inicializarDashboard = inicializarDashboard;
