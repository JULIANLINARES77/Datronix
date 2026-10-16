function escaparXml(valor) {
    return String(valor ?? '')
        .replace(/[\u0000-\u0008\u000B\u000C\u000E-\u001F\uFFFE\uFFFF]/g, '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&apos;');
}

function nombreHojaExcel(nombre) {
    const nombreValido = [...String(nombre)]
        .map(caracter => '[]:*?/\\'.includes(caracter) ? ' ' : caracter)
        .join('');
    return nombreValido.slice(0, 31) || 'Reporte';
}

function crearCeldaExcel(valor, encabezado = false) {
    const esNumero = typeof valor === 'number' && Number.isFinite(valor);
    const tipo = esNumero ? 'Number' : 'String';
    const estilo = encabezado ? ' ss:StyleID="Header"' : '';
    return `<Cell${estilo}><Data ss:Type="${tipo}">${escaparXml(valor)}</Data></Cell>`;
}

function crearFilaExcel(valores, encabezado = false) {
    return `<Row>${valores.map(valor => crearCeldaExcel(valor, encabezado)).join('')}</Row>`;
}

export function descargarExcel(nombreArchivo, hojas) {
    const contenido = [
        '<?xml version="1.0" encoding="UTF-8"?>',
        '<?mso-application progid="Excel.Sheet"?>',
        '<Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet" ',
        'xmlns:o="urn:schemas-microsoft-com:office:office" ',
        'xmlns:x="urn:schemas-microsoft-com:office:excel" ',
        'xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet" ',
        'xmlns:html="http://www.w3.org/TR/REC-html40">',
        '<Styles><Style ss:ID="Header"><Font ss:Bold="1" ss:Color="#FFFFFF"/>',
        '<Interior ss:Color="#17365D" ss:Pattern="Solid"/></Style></Styles>',
        ...hojas.map(hoja => {
            const filas = hoja.filas.map((fila, indice) => crearFilaExcel(fila, indice === 0)).join('');
            return `<Worksheet ss:Name="${escaparXml(nombreHojaExcel(hoja.nombre))}">` +
                `<Table>${filas}</Table></Worksheet>`;
        }),
        '</Workbook>'
    ].join('');
    const url = URL.createObjectURL(new Blob([contenido], {
        type: 'application/vnd.ms-excel;charset=utf-8'
    }));
    const enlace = document.createElement('a');
    enlace.href = url;
    enlace.download = nombreArchivo;
    enlace.click();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
}
