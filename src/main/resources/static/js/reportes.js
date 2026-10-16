import { getData } from './api.js';
import { descargarExcel } from './excel-export.js';

let datosReporteActuales = null;

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

function textoPdf(valor) {
    return String(valor ?? '—')
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/[–—]/g, '-')
        .replace(/[^\x20-\x7E]/g, '?');
}

function escaparTextoPdf(valor) {
    return textoPdf(valor).replace(/([\\()])/g, '\\$1');
}

function envolverTextoPdf(texto, maxCaracteres) {
    const palabras = textoPdf(texto).split(/\s+/);
    const lineas = [];
    let linea = '';
    palabras.forEach(palabra => {
        while (palabra.length > maxCaracteres) {
            if (linea) {
                lineas.push(linea);
                linea = '';
            }
            lineas.push(palabra.slice(0, maxCaracteres));
            palabra = palabra.slice(maxCaracteres);
        }
        const candidata = linea ? `${linea} ${palabra}` : palabra;
        if (candidata.length > maxCaracteres) {
            lineas.push(linea);
            linea = palabra;
        } else {
            linea = candidata;
        }
    });
    if (linea || lineas.length === 0) lineas.push(linea);
    return lineas;
}

function construirPdfReporte(panel, boton) {
    const tituloReporte = panel.querySelector('.dashboard-panel-title')?.textContent.trim() || 'Reporte';
    const encabezados = [...panel.querySelectorAll('thead th')].map(celda => celda.textContent.trim());
    const filas = [...panel.querySelectorAll('tbody tr')]
        .map(fila => [...fila.cells].map(celda => celda.textContent.trim()))
        .filter(celdas => celdas.length === encabezados.length);
    const indicadores = [
        `Ingresos del mes: ${document.getElementById('reporteIngresosMes').textContent}`,
        `Ventas realizadas: ${document.getElementById('reporteVentasMes').textContent}`,
        `Cartera en credito: ${document.getElementById('reporteCarteraCredito').textContent}`,
        `Stock critico: ${document.getElementById('reporteStockCritico').textContent}`
    ];
    const fechaExportacion = new Date().toLocaleString('es-CO');
    const paginas = [];
    let comandos = [];
    let y = 790;

    const escribirLinea = (texto, opciones = {}) => {
        const { size = 10, bold = false, indent = 0 } = opciones;
        if (y < 52) {
            paginas.push(comandos.join('\n'));
            comandos = [];
            y = 790;
            comandos.push(`BT /F2 11 Tf 48 ${y} Td (Datronix - ${escaparTextoPdf(tituloReporte)} - continuacion) Tj ET`);
            y -= 24;
        }
        comandos.push(
            `BT /${bold ? 'F2' : 'F1'} ${size} Tf ${48 + indent} ${y} Td ` +
            `(${escaparTextoPdf(texto)}) Tj ET`
        );
        y -= size + 5;
    };

    escribirLinea('DATRONIX - REPORTE', { size: 16, bold: true });
    escribirLinea(tituloReporte, { size: 12, bold: true });
    escribirLinea(`Generado: ${fechaExportacion}`, { size: 9 });
    y -= 5;
    indicadores.forEach(indicador => escribirLinea(indicador, { size: 9 }));
    y -= 5;
    escribirLinea(encabezados.join(' | '), { bold: true });
    escribirLinea('-'.repeat(Math.min(90, Math.max(24, encabezados.join(' | ').length))));

    if (filas.length === 0) {
        const mensaje = panel.querySelector('tbody td')?.textContent.trim() || 'No hay datos para mostrar.';
        escribirLinea(mensaje);
    } else {
        filas.forEach((fila, indice) => {
            const datos = fila.map((valor, columna) => `${encabezados[columna]}: ${valor}`).join(' | ');
            envolverTextoPdf(`${indice + 1}. ${datos}`, 92).forEach(linea => escribirLinea(linea));
            y -= 3;
        });
    }

    if (comandos.length > 0) paginas.push(comandos.join('\n'));
    const objetos = [
        '<< /Type /Catalog /Pages 2 0 R >>',
        '',
        '<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>',
        '<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>'
    ];
    const paginaRefs = [];
    paginas.forEach(contenido => {
        const idPagina = objetos.length + 1;
        const idContenido = idPagina + 1;
        paginaRefs.push(`${idPagina} 0 R`);
        objetos.push(
            `<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] ` +
            `/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents ${idContenido} 0 R >>`
        );
        objetos.push(`<< /Length ${contenido.length} >>\nstream\n${contenido}\nendstream`);
    });
    objetos[1] = `<< /Type /Pages /Kids [${paginaRefs.join(' ')}] /Count ${paginaRefs.length} >>`;

    let pdf = '%PDF-1.4\n';
    const offsets = [0];
    objetos.forEach((objeto, indice) => {
        offsets.push(pdf.length);
        pdf += `${indice + 1} 0 obj\n${objeto}\nendobj\n`;
    });
    const xrefOffset = pdf.length;
    pdf += `xref\n0 ${objetos.length + 1}\n0000000000 65535 f \n`;
    offsets.slice(1).forEach(offset => {
        pdf += `${String(offset).padStart(10, '0')} 00000 n \n`;
    });
    pdf += `trailer\n<< /Size ${objetos.length + 1} /Root 1 0 R >>\n` +
        `startxref\n${xrefOffset}\n%%EOF`;

    const blobUrl = URL.createObjectURL(new Blob([pdf], { type: 'application/pdf' }));
    const enlace = document.createElement('a');
    enlace.href = blobUrl;
    enlace.download = `datronix-reporte-${panel.dataset.reportPanel}-${new Date().toISOString().slice(0, 10)}.pdf`;
    enlace.click();
    setTimeout(() => URL.revokeObjectURL(blobUrl), 1000);
    boton.focus();
}

function exportarReporteExcel(panel, boton) {
    const tipo = panel.dataset.reportPanel;
    const especificacion = {
        stock: {
            nombre: 'Stock bajo',
            encabezados: ['ID Producto', 'Nombre', 'Stock actual', 'Stock mínimo'],
            filas: datosReporteActuales.stockBajo.map(item => [
                String(item.idProducto), item.nombreProducto, Number(item.stockActual), Number(item.stockMinimo)
            ])
        },
        productos: {
            nombre: 'Top productos',
            encabezados: ['ID Producto', 'Producto', 'Total vendido', 'Ingresos generados'],
            filas: datosReporteActuales.productosMasVendidos.map(item => [
                String(item.idProducto), item.nombreProducto,
                Number(item.unidadesVendidas), Number(item.ingresosGenerados)
            ])
        },
        clientes: {
            nombre: 'Clientes frecuentes',
            encabezados: ['Cliente', 'Documento', 'Nº de compras', 'Total invertido', 'Promedio de compra', 'Última compra'],
            filas: datosReporteActuales.clientesFrecuentes.map(item => [
                item.nombreCliente, String(item.idCliente), Number(item.numeroCompras),
                Number(item.totalInvertido), Number(item.promedioCompra), item.ultimaCompra || ''
            ])
        },
        meses: {
            nombre: 'Ventas por mes',
            encabezados: ['Mes', 'Año', 'Nº de ventas', 'Total mes', 'Promedio venta'],
            filas: datosReporteActuales.ventasPorMes.map(item => [
                item.nombreMes, Number(item.anio), Number(item.cantidadVentas),
                Number(item.totalMes), Number(item.promedioVenta)
            ])
        },
        creditos: {
            nombre: 'Créditos pendientes',
            encabezados: ['ID Venta', 'Cliente', 'Fecha', 'Total', 'Días transcurridos', 'Estado'],
            filas: datosReporteActuales.creditosPendientes.map(item => [
                String(item.idVenta), item.nombreCliente, item.fechaVenta,
                Number(item.total), Number(item.diasTranscurridos), 'Pendiente'
            ])
        }
    }[tipo];
    if (!especificacion) return;

    const kpis = [
        ['Indicador', 'Valor'],
        ['Ingresos del mes', document.getElementById('reporteIngresosMes').textContent],
        ['Ventas realizadas', Number(datosReporteActuales.ventasMes)],
        ['Cartera en crédito', document.getElementById('reporteCarteraCredito').textContent],
        ['Stock crítico', Number(datosReporteActuales.productosStockCritico)]
    ];
    descargarExcel(`datronix-reporte-${tipo}-${new Date().toISOString().slice(0, 10)}.xls`, [
        { nombre: 'Resumen', filas: kpis },
        { nombre: especificacion.nombre, filas: [especificacion.encabezados, ...especificacion.filas] }
    ]);
    boton.focus();
}

function inicializarReportes() {
    const tabs = [...document.querySelectorAll('[data-report-tab]')];
    const panels = [...document.querySelectorAll('[data-report-panel]')];
    const botonPdf = document.getElementById('descargarReportePdf');
    const botonExcel = document.getElementById('descargarReporteExcel');
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(item => item.setAttribute('aria-selected', String(item === tab)));
            panels.forEach(panel => {
                panel.hidden = panel.dataset.reportPanel !== tab.dataset.reportTab;
            });
        });
    });
    botonPdf.addEventListener('click', () => {
        const panel = panels.find(item => !item.hidden);
        if (panel) construirPdfReporte(panel, botonPdf);
    });
    botonExcel.addEventListener('click', () => {
        const panel = panels.find(item => !item.hidden);
        if (panel && datosReporteActuales) exportarReporteExcel(panel, botonExcel);
    });
    getData('/reportes/dashboard')
        .then(reporte => {
            datosReporteActuales = reporte;
            renderizarDashboardReporte(reporte);
            botonPdf.disabled = false;
            botonExcel.disabled = false;
        })
        .catch(error => {
            const message = `No se pudieron cargar los reportes: ${errorReporte(error)}`;
            botonPdf.disabled = true;
            botonExcel.disabled = true;
            document.querySelectorAll('[id$="Estado"]').forEach(status => {
                status.textContent = message;
            });
        });
}

window.inicializarReportes = inicializarReportes;
