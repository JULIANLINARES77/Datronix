import bcrypt from 'bcryptjs';
import { readFile } from 'node:fs/promises';
import path from 'node:path';
import process from 'node:process';
import { createInterface } from 'node:readline/promises';
import { stdin, stdout } from 'node:process';
import pg from 'pg';

const { Client } = pg;
const root = process.cwd();
const propertiesPath = path.join(root, 'src', 'main', 'resources', 'application.properties');
const sampleCount = 50;
const sampleStartDate = new Date(Date.UTC(2026, 0, 1));
const sampleUser = {
    id: '1123805963',
    name: 'Julian Linares',
    email: 'linaresjulian77@gmail.com',
    role: 'ADMINISTRADOR'
};
const testTables = [
    'cliente',
    'proveedor',
    'empleado',
    'usuario',
    'producto',
    'compra',
    'detalle_compra',
    'venta',
    'detalle_venta',
    'remision',
    'devolucion',
    'nomina',
    'soporte'
];

function readProperties(contents) {
    const properties = new Map();
    for (const sourceLine of contents.split(/\r?\n/u)) {
        const line = sourceLine.trimStart();
        if (!line || line.startsWith('#') || line.startsWith('!')) continue;
        const separator = line.search(/[=:]/u);
        if (separator < 0) continue;
        properties.set(line.slice(0, separator).trim(), line.slice(separator + 1).trim());
    }
    return properties;
}

function resolveProperty(properties, name) {
    let value = properties.get(name);
    if (value === undefined) throw new Error(`Falta la propiedad ${name} en application.properties.`);
    if (name === 'spring.datasource.password' && process.env.SPRING_DATASOURCE_PASSWORD) {
        return process.env.SPRING_DATASOURCE_PASSWORD;
    }
    const placeholder = value.match(/^\$\{([^}:]+)(?::([^}]*))?\}$/u);
    if (placeholder) {
        const [, variable, fallback] = placeholder;
        value = process.env[variable] ?? fallback;
        if (value === undefined || value.length === 0) {
            throw new Error(`La propiedad ${name} requiere la variable de entorno ${variable}.`);
        }
    }
    return value;
}

async function createDatabaseClient() {
    const properties = readProperties(await readFile(propertiesPath, 'utf8'));
    const jdbcUrl = resolveProperty(properties, 'spring.datasource.url')
        .replaceAll('${APPDATA}', process.env.APPDATA ?? '');
    if (!jdbcUrl.startsWith('jdbc:postgresql://')) {
        throw new Error('El sembrador requiere una URL JDBC de PostgreSQL/CockroachDB.');
    }

    const url = new URL(jdbcUrl.slice('jdbc:'.length));
    const certificatePath = url.searchParams.get('sslrootcert');
    if (url.searchParams.get('sslmode') !== 'verify-full' || !certificatePath) {
        throw new Error('La conexión requiere sslmode=verify-full y sslrootcert en la URL.');
    }
    const certificate = await readFile(certificatePath, 'utf8');
    const password = resolveProperty(properties, 'spring.datasource.password');
    return new Client({
        host: url.hostname,
        port: Number(url.port || 5432),
        database: decodeURIComponent(url.pathname.slice(1)),
        user: resolveProperty(properties, 'spring.datasource.username'),
        password,
        ssl: { ca: certificate, rejectUnauthorized: true },
        application_name: 'DatronixTestDataSeeder'
    });
}

function dateAt(index) {
    const date = new Date(sampleStartDate);
    date.setUTCDate(date.getUTCDate() + index);
    return date.toISOString().slice(0, 10);
}

function numberLabel(index) {
    return String(index + 1).padStart(3, '0');
}

function identifier(prefix, index) {
    return `DTRX-TEST-${prefix}-${numberLabel(index)}`;
}

function rowsToSql(rows, width, start = 1) {
    return rows.map((_, rowIndex) => {
        const parameters = Array.from(
            { length: width },
            (_, columnIndex) => `$${start + rowIndex * width + columnIndex}`
        );
        return `(${parameters.join(', ')})`;
    }).join(', ');
}

async function insertMany(client, table, columns, rows, conflictTarget, returning = '') {
    if (rows.length === 0) return [];
    const values = rows.flat();
    const result = await client.query(
        `INSERT INTO ${table} (${columns.join(', ')}) VALUES ${rowsToSql(rows, columns.length)} `
        + `ON CONFLICT (${conflictTarget}) DO NOTHING${returning ? ` RETURNING ${returning}` : ''}`,
        values
    );
    return result.rows;
}

async function assertSchema(client) {
    const result = await client.query(
        `SELECT table_name FROM information_schema.tables
         WHERE table_schema = current_schema() AND table_name = ANY($1::text[])`,
        [testTables]
    );
    const existing = new Set(result.rows.map(row => row.table_name));
    const missing = testTables.filter(table => !existing.has(table));
    if (missing.length > 0) {
        throw new Error(
            `Faltan tablas de prueba (${missing.join(', ')}). Inicia Spring Boot y deja que JPA cree el esquema.`
        );
    }
}

async function verifyConnection(client) {
    const result = await client.query(
        'SELECT current_database() AS database_name, current_user AS user_name, now() AS server_time'
    );
    for (let attempt = 0; attempt < 3; attempt += 1) {
        await client.query('SELECT 1 AS connection_check');
    }
    console.log(
        `Conexión verificada: base=${result.rows[0].database_name}, `
        + `usuario=${result.rows[0].user_name}, consultas consecutivas=3.`
    );
}

async function askForUserPassword() {
    if (process.env.DATRONIX_TEST_USER_PASSWORD) {
        return process.env.DATRONIX_TEST_USER_PASSWORD;
    }
    const prompt = createInterface({ input: stdin, output: stdout });
    try {
        const password = await prompt.question(
            `Contraseña para el único usuario de prueba ${sampleUser.email}: `
        );
        if (!password) throw new Error('La contraseña del usuario de prueba no puede estar vacía.');
        const confirmation = await prompt.question('Confirma la contraseña: ');
        if (password !== confirmation) throw new Error('Las contraseñas ingresadas no coinciden.');
        return password;
    } finally {
        prompt.close();
    }
}

async function ensureReferenceRows(client, counts) {
    const clients = Array.from({ length: sampleCount }, (_, index) => [
        identifier('CLI', index),
        `Cliente de prueba ${numberLabel(index)}`,
        3000000000 + index + 1,
        `cliente.${numberLabel(index)}@example.test`,
        `Dirección de prueba ${numberLabel(index)}`,
        index % 2 === 0 ? 'PERSONA' : 'EMPRESA'
    ]);
    const providers = Array.from({ length: sampleCount }, (_, index) => [
        identifier('PROV', index),
        `Proveedor de prueba ${numberLabel(index)}`,
        `Contacto de prueba ${numberLabel(index)}`,
        3100000000 + index + 1,
        `proveedor.${numberLabel(index)}@example.test`,
        `Dirección proveedor ${numberLabel(index)}`
    ]);
    const employees = Array.from({ length: sampleCount }, (_, index) => [
        identifier('EMP', index),
        `Empleado de prueba ${numberLabel(index)}`,
        index % 2 === 0 ? 'Auxiliar' : 'Asesor',
        `empleado.${numberLabel(index)}@example.test`,
        `320000${String(index + 1).padStart(4, '0')}`,
        dateAt(index),
        (1800000 + (index + 1) * 1000).toFixed(2)
    ]);

    counts.cliente = (await insertMany(
        client,
        'cliente',
        ['id_cliente', 'nombre_cliente', 'telefono_cliente', 'email_cliente', 'direccion_cliente', 'tipo_cliente'],
        clients,
        'id_cliente'
    )).length;
    counts.proveedor = (await insertMany(
        client,
        'proveedor',
        ['id_proveedor', 'nombre_empresa', 'nombre_proveedor', 'telefono_proveedor', 'email_proveedor',
            'direccion_proveedor'],
        providers,
        'id_proveedor'
    )).length;
    counts.empleado = (await insertMany(
        client,
        'empleado',
        ['id_empleado', 'nombre_empleado', 'cargo', 'correo_empleado', 'telefono_empleado', 'fecha_ingreso',
            'salario_base'],
        employees,
        'id_empleado'
    )).length;

    const customerRows = await client.query(
        'SELECT id_cliente FROM cliente WHERE id_cliente = ANY($1::text[])',
        [clients.map(row => row[0])]
    );
    const providerRows = await client.query(
        'SELECT id_proveedor FROM proveedor WHERE id_proveedor = ANY($1::text[])',
        [providers.map(row => row[0])]
    );
    const employeeRows = await client.query(
        'SELECT id_empleado FROM empleado WHERE id_empleado = ANY($1::text[])',
        [employees.map(row => row[0])]
    );
    if (customerRows.rowCount !== sampleCount || providerRows.rowCount !== sampleCount
        || employeeRows.rowCount !== sampleCount) {
        throw new Error('No se pudieron confirmar todos los clientes, proveedores y empleados de prueba.');
    }
    return {
        clients,
        providers,
        employees,
        clientIds: clients.map(row => row[0]),
        providerIds: providers.map(row => row[0]),
        employeeIds: employees.map(row => row[0])
    };
}

async function ensureSingleUser(client, password, counts) {
    const rows = await client.query(
        `SELECT idusuario, correousuario, nombreusuario, contrasenausuario, rol_usuario
         FROM usuario
         WHERE idusuario = $1 OR lower(correousuario) = lower($2)
         FOR UPDATE`,
        [sampleUser.id, sampleUser.email]
    );
    const byId = rows.rows.find(row => row.idusuario === sampleUser.id);
    const byEmail = rows.rows.find(row => row.correousuario.toLowerCase() === sampleUser.email.toLowerCase());
    if ((byId && byId !== byEmail) || (byEmail && byEmail.idusuario !== sampleUser.id)) {
        throw new Error('El documento o correo del usuario de prueba ya pertenece a otra cuenta; no se modificó.');
    }

    const passwordHash = await bcrypt.hash(password, 12);
    if (!byEmail) {
        await client.query(
            `INSERT INTO usuario
             (idusuario, nombreusuario, correousuario, contrasenausuario, rol_usuario)
             VALUES ($1, $2, $3, $4, $5)`,
            [sampleUser.id, sampleUser.name, sampleUser.email, passwordHash, sampleUser.role]
        );
        counts.usuario = 1;
        return;
    }

    const matches = await bcrypt.compare(password, byEmail.contrasenausuario);
    if (byEmail.nombreusuario !== sampleUser.name || byEmail.rol_usuario !== sampleUser.role || !matches) {
        await client.query(
            `UPDATE usuario
             SET nombreusuario = $2, contrasenausuario = $3, rol_usuario = $4
             WHERE idusuario = $1`,
            [sampleUser.id, sampleUser.name, passwordHash, sampleUser.role]
        );
    }
    counts.usuario = 0;
}

async function ensureProducts(client, counts) {
    const rows = Array.from({ length: sampleCount }, (_, index) => [
        `Producto de prueba ${numberLabel(index)}`,
        'Pruebas',
        'Datronix',
        `TEST-${numberLabel(index)}`,
        'Registro sintético de prueba; no corresponde a una referencia comercial.',
        'Datos de prueba',
        (50000 + (index + 1) * 100).toFixed(2),
        (75000 + (index + 1) * 100).toFixed(2),
        100,
        5,
        identifier('PROV', index),
        identifier('SKU', index)
    ]);
    const values = rows.flat();
    const result = await client.query(
        `INSERT INTO producto
         (nombre_producto, categoria_producto, marca_producto, modelo_producto, descripcion_producto,
          especificaciones_producto, precio_costo_producto, precio_venta_producto, stock_producto,
          stock_minimo_producto, id_proveedor, sku_producto)
         VALUES ${rowsToSql(rows, rows[0].length)}
         ON CONFLICT (sku_producto) DO NOTHING
         RETURNING id_producto`,
        values
    );
    counts.producto = result.rowCount;

    const products = await client.query(
        'SELECT id_producto, sku_producto, precio_costo_producto, precio_venta_producto '
        + 'FROM producto WHERE sku_producto = ANY($1::text[])',
        [rows.map(row => row[11])]
    );
    if (products.rowCount !== sampleCount) {
        throw new Error('No se pudieron confirmar los 50 productos sintéticos.');
    }
    return new Map(products.rows.map(row => [row.sku_producto, row]));
}

async function ensurePurchases(client, products, providerIds, counts) {
    const dates = Array.from({ length: sampleCount }, (_, index) => dateAt(index));
    const found = await client.query(
        `SELECT id_compra, id_proveedor, to_char(fecha_compra, 'YYYY-MM-DD') AS fecha_key
         FROM compra
         WHERE id_proveedor = ANY($1::text[]) AND fecha_compra BETWEEN $2::date AND $3::date`,
        [providerIds, dates[0], dates[dates.length - 1]]
    );
    const dateByProvider = new Map(providerIds.map((id, index) => [id, dates[index]]));
    const purchaseByKey = new Map(
        found.rows.map(row => [`${row.id_proveedor}:${row.fecha_key}`, row])
    );
    const missing = Array.from({ length: sampleCount }, (_, index) => {
        const key = `${providerIds[index]}:${dates[index]}`;
        if (purchaseByKey.has(key)) return null;
        const product = products.get(identifier('SKU', index));
        return [
            providerIds[index],
            dates[index],
            product.precio_costo_producto.toString()
        ];
    }).filter(Boolean);
    if (missing.length > 0) {
        const inserted = await client.query(
            `INSERT INTO compra (id_proveedor, fecha_compra, total_compra)
             VALUES ${rowsToSql(missing, 3)}
             RETURNING id_compra, id_proveedor, fecha_compra`,
            missing.flat()
        );
        counts.compra = inserted.rowCount;
        for (const row of inserted.rows) {
            const date = dateByProvider.get(row.id_proveedor);
            purchaseByKey.set(`${row.id_proveedor}:${date}`, row);
        }
    } else {
        counts.compra = 0;
    }
    if (purchaseByKey.size < sampleCount) throw new Error('No se pudieron confirmar las compras de prueba.');

    const purchases = Array.from({ length: sampleCount }, (_, index) =>
        purchaseByKey.get(`${providerIds[index]}:${dates[index]}`));
    const existingDetails = await client.query(
        'SELECT id_compra, id_producto FROM detalle_compra WHERE id_compra = ANY($1::bigint[])',
        [purchases.map(row => row.id_compra)]
    );
    const detailKeys = new Set(existingDetails.rows.map(row => `${row.id_compra}:${row.id_producto}`));
    const missingDetails = purchases.flatMap((purchase, index) => {
        const product = products.get(identifier('SKU', index));
        const key = `${purchase.id_compra}:${product.id_producto}`;
        return detailKeys.has(key) ? [] : [[purchase.id_compra, product.id_producto, 1, product.precio_costo_producto]];
    });
    const insertedDetails = await insertMany(
        client,
        'detalle_compra',
        ['id_compra', 'id_producto', 'cantidad', 'precio_unitario'],
        missingDetails,
        'id_detalle_compra',
        'id_producto'
    );
    counts.detalle_compra = insertedDetails.length;
    if (insertedDetails.length > 0) {
        const stock = await client.query(
            'UPDATE producto SET stock_producto = stock_producto + 1 '
            + 'WHERE id_producto = ANY($1::bigint[])',
            [insertedDetails.map(row => row.id_producto)]
        );
        if (stock.rowCount !== insertedDetails.length) {
            throw new Error('No se pudo actualizar el inventario tras las compras de prueba.');
        }
    }
    return purchases;
}

async function ensureSales(client, products, clientIds, counts) {
    const dates = Array.from({ length: sampleCount }, (_, index) => dateAt(index));
    const found = await client.query(
        `SELECT id_venta, id_cliente, to_char(fecha_venta, 'YYYY-MM-DD') AS fecha_key, total_venta
         FROM venta
         WHERE id_cliente = ANY($1::text[]) AND fecha_venta BETWEEN $2::date AND $3::date`,
        [clientIds, dates[0], dates[dates.length - 1]]
    );
    const dateByClient = new Map(clientIds.map((id, index) => [id, dates[index]]));
    const saleByKey = new Map(
        found.rows.map(row => [`${row.id_cliente}:${row.fecha_key}`, row])
    );
    const missing = Array.from({ length: sampleCount }, (_, index) => {
        const key = `${clientIds[index]}:${dates[index]}`;
        if (saleByKey.has(key)) return null;
        const product = products.get(identifier('SKU', index));
        return [
            clientIds[index],
            dates[index],
            product.precio_venta_producto.toString(),
            index % 2 === 0 ? 'CONTADO' : 'CREDITO',
            `Dirección de entrega de prueba ${numberLabel(index)}`
        ];
    }).filter(Boolean);
    if (missing.length > 0) {
        const inserted = await client.query(
            `INSERT INTO venta (id_cliente, fecha_venta, total_venta, estado_pago, direccion_entrega)
             VALUES ${rowsToSql(missing, 5)}
             RETURNING id_venta, id_cliente, fecha_venta`,
            missing.flat()
        );
        counts.venta = inserted.rowCount;
        for (const row of inserted.rows) {
            const index = clientIds.indexOf(row.id_cliente);
            const product = products.get(identifier('SKU', index));
            saleByKey.set(`${row.id_cliente}:${dateByClient.get(row.id_cliente)}`, {
                ...row,
                fecha_key: dateByClient.get(row.id_cliente),
                total_venta: product.precio_venta_producto
            });
        }
    } else {
        counts.venta = 0;
    }
    if (saleByKey.size < sampleCount) throw new Error('No se pudieron confirmar las ventas de prueba.');

    const sales = Array.from({ length: sampleCount }, (_, index) =>
        saleByKey.get(`${clientIds[index]}:${dates[index]}`));
    const existingDetails = await client.query(
        'SELECT id_venta, id_producto FROM detalle_venta WHERE id_venta = ANY($1::bigint[])',
        [sales.map(row => row.id_venta)]
    );
    const detailKeys = new Set(existingDetails.rows.map(row => `${row.id_venta}:${row.id_producto}`));
    const missingDetails = sales.flatMap((sale, index) => {
        const product = products.get(identifier('SKU', index));
        const key = `${sale.id_venta}:${product.id_producto}`;
        return detailKeys.has(key) ? [] : [[
            sale.id_venta, product.id_producto, 1, product.precio_venta_producto, 0,
            product.precio_venta_producto
        ]];
    });
    const insertedDetails = await insertMany(
        client,
        'detalle_venta',
        ['id_venta', 'id_producto', 'cantidad', 'precio_unitario', 'descuento_item', 'subtotal_item'],
        missingDetails,
        'id_detalle_venta',
        'id_producto'
    );
    counts.detalle_venta = insertedDetails.length;
    if (insertedDetails.length > 0) {
        const stock = await client.query(
            'UPDATE producto SET stock_producto = stock_producto - 1 '
            + 'WHERE id_producto = ANY($1::bigint[]) AND stock_producto >= 1',
            [insertedDetails.map(row => row.id_producto)]
        );
        if (stock.rowCount !== insertedDetails.length) {
            throw new Error('Inventario insuficiente para insertar los detalles de venta de prueba.');
        }
    }
    return sales;
}

async function ensureRemissions(client, sales, clientIds, counts) {
    const saleIds = sales.map(row => row.id_venta);
    const existing = await client.query(
        'SELECT id_venta FROM remision WHERE id_venta = ANY($1::bigint[])',
        [saleIds]
    );
    const existingIds = new Set(existing.rows.map(row => String(row.id_venta)));
    const missing = sales.flatMap((sale, index) => {
        if (existingIds.has(String(sale.id_venta))) return [];
        return [[
            sale.id_venta,
            clientIds[index],
            `${sale.fecha_key} 12:00:00`,
            sale.total_venta.toString(),
            `Remisión de prueba Datronix #${numberLabel(index)}`
        ]];
    });
    const inserted = await insertMany(
        client,
        'remision',
        ['id_venta', 'id_cliente', 'fecha_emision', 'total_remision', 'observaciones'],
        missing,
        'id_venta'
    );
    counts.remision = inserted.length;
}

async function ensureReturns(client, products, clients, counts) {
    const productIds = Array.from(products.values()).map(row => row.id_producto);
    const dates = Array.from({ length: sampleCount }, (_, index) => `${dateAt(index)} 12:00:00`);
    const found = await client.query(
        `SELECT id_producto, to_char(fecha_devolucion, 'YYYY-MM-DD HH24:MI') AS fecha_key
         FROM devolucion
         WHERE id_producto = ANY($1::bigint[])
           AND fecha_devolucion BETWEEN $2::timestamp AND $3::timestamp`,
        [productIds, dates[0], dates[dates.length - 1]]
    );
    const returnKeys = new Set(found.rows.map(row => `${row.id_producto}:${row.fecha_key}`));
    const missing = Array.from({ length: sampleCount }, (_, index) => {
        const product = products.get(identifier('SKU', index));
        const key = `${product.id_producto}:${dates[index].slice(0, 16)}`;
        if (returnKeys.has(key)) return null;
        return [
            product.id_producto,
            `Cliente de prueba ${numberLabel(index)}`,
            1,
            `Devolución de prueba Datronix #${numberLabel(index)}`,
            dates[index]
        ];
    }).filter(Boolean);
    const inserted = await insertMany(
        client,
        'devolucion',
        ['id_producto', 'nombre_cliente', 'cantidad', 'motivo', 'fecha_devolucion'],
        missing,
        'id_devolucion',
        'id_producto'
    );
    counts.devolucion = inserted.length;
    if (inserted.length > 0) {
        const stock = await client.query(
            'UPDATE producto SET stock_producto = stock_producto + 1 '
            + 'WHERE id_producto = ANY($1::bigint[])',
            [inserted.map(row => row.id_producto)]
        );
        if (stock.rowCount !== inserted.length) {
            throw new Error('No se pudo actualizar el inventario tras las devoluciones de prueba.');
        }
    }
}

async function ensurePayroll(client, employeeIds, counts) {
    const period = '2026-01';
    const found = await client.query(
        'SELECT id_empleado FROM nomina WHERE id_empleado = ANY($1::text[]) AND periodo = $2',
        [employeeIds, period]
    );
    const present = new Set(found.rows.map(row => row.id_empleado));
    const missing = Array.from({ length: sampleCount }, (_, index) => {
        const employeeId = employeeIds[index];
        return present.has(employeeId)
            ? null
            : [employeeId, '2026-01-28', (1800000 + (index + 1) * 1000).toFixed(2), period];
    }).filter(Boolean);
    const inserted = await insertMany(
        client,
        'nomina',
        ['id_empleado', 'fecha_pago', 'salario_pagado', 'periodo'],
        missing,
        'id_nomina'
    );
    counts.nomina = inserted.length;
}

async function ensureSupportTickets(client, counts) {
    const subjects = Array.from(
        { length: sampleCount },
        (_, index) => `Ticket de prueba Datronix #${numberLabel(index)}`
    );
    const found = await client.query('SELECT asunto FROM soporte WHERE asunto = ANY($1::text[])', [subjects]);
    const present = new Set(found.rows.map(row => row.asunto));
    const missing = Array.from({ length: sampleCount }, (_, index) => {
        const subject = subjects[index];
        if (present.has(subject)) return null;
        return [
            `Empresa de prueba ${numberLabel(index)}`,
            identifier('SUP', index),
            `Remitente de prueba ${numberLabel(index)}`,
            `300000${String(index + 1).padStart(4, '0')}`,
            subject,
            'Solicitud ficticia para validar el módulo de soporte.',
            `${dateAt(index)} 12:00:00`
        ];
    }).filter(Boolean);
    const inserted = await insertMany(
        client,
        'soporte',
        ['empresa', 'id_remitente', 'nombre_remitente', 'telefono_remitente', 'asunto', 'descripcion',
            'fecha_creacion'],
        missing,
        'id_soporte'
    );
    counts.soporte = inserted.length;
}

async function verifySeed(client) {
    const checks = [
        ['cliente', 'SELECT COUNT(*)::int AS count FROM cliente WHERE id_cliente LIKE $1', ['DTRX-TEST-CLI-%']],
        ['proveedor', 'SELECT COUNT(*)::int AS count FROM proveedor WHERE id_proveedor LIKE $1', ['DTRX-TEST-PROV-%']],
        ['empleado', 'SELECT COUNT(*)::int AS count FROM empleado WHERE id_empleado LIKE $1', ['DTRX-TEST-EMP-%']],
        ['usuario', 'SELECT COUNT(*)::int AS count FROM usuario WHERE idusuario = $1 AND lower(correousuario) = lower($2)',
            [sampleUser.id, sampleUser.email]],
        ['producto', 'SELECT COUNT(*)::int AS count FROM producto WHERE sku_producto LIKE $1', ['DTRX-TEST-SKU-%']],
        ['compra', `SELECT COUNT(*)::int AS count FROM compra
                    WHERE id_proveedor LIKE $1 AND fecha_compra BETWEEN $2::date AND $3::date`,
            ['DTRX-TEST-PROV-%', dateAt(0), dateAt(sampleCount - 1)]],
        ['detalle_compra', `SELECT COUNT(*)::int AS count FROM detalle_compra d
                            JOIN producto p ON p.id_producto = d.id_producto
                            WHERE p.sku_producto LIKE $1`,
            ['DTRX-TEST-SKU-%']],
        ['venta', `SELECT COUNT(*)::int AS count FROM venta
                   WHERE id_cliente LIKE $1 AND fecha_venta BETWEEN $2::date AND $3::date`,
            ['DTRX-TEST-CLI-%', dateAt(0), dateAt(sampleCount - 1)]],
        ['detalle_venta', `SELECT COUNT(*)::int AS count FROM detalle_venta d
                           JOIN producto p ON p.id_producto = d.id_producto
                           WHERE p.sku_producto LIKE $1`,
            ['DTRX-TEST-SKU-%']],
        ['remision', `SELECT COUNT(*)::int AS count FROM remision
                      WHERE observaciones LIKE $1`,
            ['Remisión de prueba Datronix #%']],
        ['devolucion', `SELECT COUNT(*)::int AS count FROM devolucion
                        WHERE motivo LIKE $1`,
            ['Devolución de prueba Datronix #%']],
        ['nomina', `SELECT COUNT(*)::int AS count FROM nomina n
                    JOIN empleado e ON e.id_empleado = n.id_empleado
                    WHERE e.id_empleado LIKE $1 AND n.periodo = $2`,
            ['DTRX-TEST-EMP-%', '2026-01']],
        ['soporte', 'SELECT COUNT(*)::int AS count FROM soporte WHERE asunto LIKE $1',
            ['Ticket de prueba Datronix #%']]
    ];
    const counts = {};
    for (const [table, sql, parameters] of checks) {
        const result = await client.query(sql, parameters);
        counts[table] = Number(result.rows[0].count);
        const expected = table === 'usuario' ? 1 : sampleCount;
        if (counts[table] !== expected) {
            throw new Error(
                `Validación de ${table} falló: se esperaban ${expected} filas marcadas y se encontraron ${counts[table]}.`
            );
        }
    }
    return counts;
}

async function seed(client) {
    const password = await askForUserPassword();
    const counts = {};
    await client.query('BEGIN');
    try {
        await client.query('SET TRANSACTION ISOLATION LEVEL SERIALIZABLE');
        const references = await ensureReferenceRows(client, counts);
        await ensureSingleUser(client, password, counts);
        const products = await ensureProducts(client, counts);
        await ensurePurchases(client, products, references.providerIds, counts);
        const sales = await ensureSales(client, products, references.clientIds, counts);
        await ensureRemissions(client, sales, references.clientIds, counts);
        await ensureReturns(client, products, references.clients, counts);
        await ensurePayroll(client, references.employeeIds, counts);
        await ensureSupportTickets(client, counts);
        const verified = await verifySeed(client);
        await client.query('COMMIT');
        console.log('Carga de datos sintéticos verificada; no se eliminaron ni reemplazaron registros existentes.');
        console.log(`Usuario de prueba verificado: ${sampleUser.name} (${sampleUser.email}), documento ${sampleUser.id}.`);
        console.log(`Filas nuevas por tabla: ${JSON.stringify(counts)}`);
        console.log(`Filas sintéticas verificadas por tabla: ${JSON.stringify(verified)}`);
        console.log('No se insertaron códigos en email_verification_code: esa tabla contiene códigos temporales de seguridad.');
    } catch (error) {
        await client.query('ROLLBACK');
        throw error;
    }
}

async function main() {
    const client = await createDatabaseClient();
    try {
        await client.connect();
        await verifyConnection(client);
        await assertSchema(client);
        if (process.argv.includes('--check')) {
            console.log('Esquema verificado. Modo --check no realizó escrituras.');
            return;
        }
        await seed(client);
    } finally {
        await client.end();
    }
}

main().catch(error => {
    const code = error.code ? ` (código ${error.code})` : '';
    console.error(`No se completó la carga de prueba${code}: ${error.message}`);
    process.exitCode = 1;
});
