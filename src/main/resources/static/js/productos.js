// js/productos.js - CRUD completo (sin auto-inicialización)

import { getData, postData, putData, deleteData } from './api.js';

let productosData = [];
let proveedoresData = [];
let productoEditando = null;

function mensajeErrorProducto(error) {
    try {
        return JSON.parse(error.message).mensaje || error.message;
    } catch {
        return error.message;
    }
}

console.log('✅ productos.js cargado');

// ============ INICIALIZACIÓN ============
function inicializarProductos() {
    // Carga primero las opciones relacionadas y luego prepara la tabla y sus botones.
    console.log('✅ inicializarProductos ejecutado');
    cargarProveedores();
    cargarProductos();
    configurarEventosProductos();
    const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    const nombreModulo = document.getElementById('nombreUsuarioModulo');
    if (nombreModulo) nombreModulo.textContent = usuario.nombreUsuario || 'Usuario';
}

// ============ CARGAR PROVEEDORES PARA SELECTS ============
async function cargarProveedores() {
    // Los proveedores son necesarios para que los formularios puedan elegir una relación válida.
    try {
        proveedoresData = await getData('/proveedores');
        llenarSelectProveedores('selectProveedor');
        llenarSelectProveedores('selectProveedorEdit');
    } catch (error) {
        console.error('❌ Error al cargar proveedores:', error);
    }
}

function llenarSelectProveedores(idSelect) {
    // Reconstruye un selector con los proveedores que llegaron desde la API.
    const select = document.getElementById(idSelect);
    if (!select) return;
    const valorActual = select.value;
    select.innerHTML = '<option value="">Seleccionar proveedor...</option>';
    proveedoresData.forEach(p => {
        const option = document.createElement('option');
        option.value = p.idProveedor; // NIT como String
        option.textContent = p.nombreEmpresa || p.contactoProveedor || p.idProveedor;
        select.appendChild(option);
    });
    if (valorActual) select.value = valorActual;
}

// ============ CARGAR PRODUCTOS ============
async function cargarProductos() {
    // Trae la información actual y la convierte en filas visibles.
    console.log('✅ cargarProductos ejecutado');
    try {
        productosData = await getData('/productos');
        console.log('✅ Datos recibidos:', productosData);
        renderizarTabla(productosData);
    } catch (error) {
        console.error('❌ Error al cargar productos:', error);
        document.getElementById('tbodyProductos').textContent =
            `Error al cargar productos: ${error.message}`;
    }
}

// ============ RENDERIZAR TABLA ============
function renderizarTabla(data) {
    // Presenta los productos en la tabla y deja cada fila marcada con su ID.
    const tbody = document.getElementById('tbodyProductos');
    if (!tbody) {
        console.error('❌ No se encontró el elemento tbodyProductos');
        return;
    }
    if (!data || data.length === 0) {
        tbody.innerHTML = `<tr><td colspan="14" class="text-center">No hay productos registrados</td></tr>`;
        return;
    }
    const escapar = valor => String(valor ?? '-').replace(/[&<>"']/g, caracter => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    })[caracter]);
    tbody.innerHTML = data.map(p => `
        <tr data-id="${escapar(p.idProducto)}">
            <td>${escapar(p.idProducto)}</td>
            <td>${escapar(p.nombreProducto)}</td>
            <td>${escapar(p.categoriaProducto)}</td>
            <td>${escapar(p.marcaProducto)}</td>
            <td>${escapar(p.modeloProducto)}</td>
            <td>${escapar(p.descripcionProducto)}</td>
            <td>${escapar(p.especificacionesProducto)}</td>
            <td>${escapar(p.skuProducto)}</td>
            <td>${escapar(p.precioCostoProducto)}</td>
            <td>${escapar(p.precioVentaProducto)}</td>
            <td>${escapar(p.stockProducto)}</td>
            <td>${escapar(p.stockMinimoProducto)}</td>
            <td>${escapar(p.idProveedor)}</td>
            <td><button class="table-icon-button" type="button" aria-label="Editar"><i class="fas fa-pencil-alt"></i></button> <button class="table-icon-button" type="button" aria-label="Eliminar"><i class="fas fa-trash"></i></button></td>
        </tr>
    `).join('');
}

// ============ BÚSQUEDA ============
function buscarProductos() {
    // Filtra en memoria por ID o nombre para encontrar rápidamente un producto.
    const termino = document.getElementById('txtBuscarProducto').value.trim().toLowerCase();
    if (!termino) {
        renderizarTabla(productosData);
        return;
    }
    const filtrados = productosData.filter(p =>
        p.idProducto.toString().includes(termino) ||
        p.nombreProducto.toLowerCase().includes(termino) ||
        (p.skuProducto && p.skuProducto.toLowerCase().includes(termino))
    );
    renderizarTabla(filtrados);
}

// ============ MODALES ============
function abrirModalAgregar() {
    document.getElementById('modalAgregarProducto').classList.add('active');
    limpiarFormularioAgregar();
    cargarProveedores();
    // El ID se genera automáticamente, no hay campo que enfocar.
}

function cerrarModalAgregar() {
    document.getElementById('modalAgregarProducto').classList.remove('active');
}

function abrirModalEditar() {
    // Copia al formulario el producto correspondiente a la fila seleccionada.
    const selectedRow = document.querySelector('#tablaProductos tbody tr.selected');
    if (!selectedRow) {
        alert('Selecciona un producto de la tabla');
        return;
    }
    const id = selectedRow.dataset.id;
    const producto = productosData.find(p => String(p.idProducto) === id);
    if (!producto) {
        alert('Producto no encontrado');
        return;
    }
    productoEditando = producto;
    cargarProveedores().then(() => {
        document.getElementById('txtIdProductoEdit').value = producto.idProducto;
        document.getElementById('txtNombreProductoEdit').value = producto.nombreProducto;
        document.getElementById('txtSkuProductoEdit').value = producto.skuProducto || '';
        document.getElementById('txtCategoriaProductoEdit').value = producto.categoriaProducto || '';
        document.getElementById('txtMarcaProductoEdit').value = producto.marcaProducto || '';
        document.getElementById('txtModeloProductoEdit').value = producto.modeloProducto || '';
        document.getElementById('txtPrecioCostoEdit').value = producto.precioCostoProducto;
        document.getElementById('txtPrecioVentaEdit').value = producto.precioVentaProducto;
        document.getElementById('txtStockProductoEdit').value = producto.stockProducto;
        document.getElementById('txtStockMinimoEdit').value = producto.stockMinimoProducto;
        // Asignamos el proveedor usando el campo plano idProveedor del DTO
        if (producto.idProveedor) {
            document.getElementById('selectProveedorEdit').value = producto.idProveedor;
        }
        document.getElementById('modalEditarProducto').classList.add('active');
    });
}

function cerrarModalEditar() {
    document.getElementById('modalEditarProducto').classList.remove('active');
    productoEditando = null;
}

// ============ CRUD ============
async function guardarProducto() {
    // Lee y valida el formulario antes de enviar un nuevo producto al backend.
    // El ID no se lee porque lo genera la base de datos.
    const nombre = document.getElementById('txtNombreProducto').value.trim();
    const sku = document.getElementById('txtSkuProducto').value.trim();
    const categoria = document.getElementById('txtCategoriaProducto').value.trim();
    const marca = document.getElementById('txtMarcaProducto').value.trim();
    const modelo = document.getElementById('txtModeloProducto').value.trim();
    const precioCosto = parseFloat(document.getElementById('txtPrecioCosto').value);
    const precioVenta = parseFloat(document.getElementById('txtPrecioVenta').value);
    const stock = parseInt(document.getElementById('txtStockProducto').value);
    const stockMinimo = parseInt(document.getElementById('txtStockMinimo').value);
    // El idProveedor es un NIT String, no se parsea.
    const idProveedor = document.getElementById('selectProveedor').value;

    if (!nombre || isNaN(precioCosto) || isNaN(precioVenta) || isNaN(stock) || isNaN(stockMinimo) || !idProveedor) {
        alert('Todos los campos obligatorios deben estar llenos (Nombre, Precios, Stock, Stock Mínimo y Proveedor).');
        return;
    }

    const nuevoProducto = {
        // No enviamos idProducto; el backend lo genera automáticamente.
        nombreProducto: nombre,
        categoriaProducto: categoria || null,
        marcaProducto: marca || null,
        modeloProducto: modelo || null,
        precioCostoProducto: precioCosto,
        precioVentaProducto: precioVenta,
        stockProducto: stock,
        stockMinimoProducto: stockMinimo,
        skuProducto: sku || null,
        idProveedor: idProveedor   // Campo plano, como String
    };

    try {
        const creado = await postData('/productos', nuevoProducto);
        productosData.push(creado);
        renderizarTabla(productosData);
        cerrarModalAgregar();
        alert('✅ Producto guardado exitosamente');
    } catch (error) {
        alert('❌ Error al guardar: ' + error.message);
    }
}

async function guardarProductoEdit() {
    // Envía los cambios del producto seleccionado y refresca la tabla local.
    const id = document.getElementById('txtIdProductoEdit').value;
    const nombre = document.getElementById('txtNombreProductoEdit').value.trim();
    const sku = document.getElementById('txtSkuProductoEdit').value.trim();
    const categoria = document.getElementById('txtCategoriaProductoEdit').value.trim();
    const marca = document.getElementById('txtMarcaProductoEdit').value.trim();
    const modelo = document.getElementById('txtModeloProductoEdit').value.trim();
    const precioCosto = parseFloat(document.getElementById('txtPrecioCostoEdit').value);
    const precioVenta = parseFloat(document.getElementById('txtPrecioVentaEdit').value);
    const stock = parseInt(document.getElementById('txtStockProductoEdit').value);
    const stockMinimo = parseInt(document.getElementById('txtStockMinimoEdit').value);
    // El idProveedor es un NIT String, no se parsea.
    const idProveedor = document.getElementById('selectProveedorEdit').value;

    if (!nombre || isNaN(precioCosto) || isNaN(precioVenta) || isNaN(stock) || isNaN(stockMinimo) || !idProveedor) {
        alert('Nombre, Precios, Stock, Stock Mínimo y Proveedor son obligatorios.');
        return;
    }

    const productoActualizado = {
        idProducto: id,
        nombreProducto: nombre,
        categoriaProducto: categoria || null,
        marcaProducto: marca || null,
        modeloProducto: modelo || null,
        precioCostoProducto: precioCosto,
        precioVentaProducto: precioVenta,
        stockProducto: stock,
        stockMinimoProducto: stockMinimo,
        skuProducto: sku || null,
        idProveedor: idProveedor   // Campo plano, como String
    };

    try {
        const actualizado = await putData(`/productos/${id}`, productoActualizado);
        const index = productosData.findIndex(p => String(p.idProducto) === id);
        if (index !== -1) {
            productosData[index] = actualizado;
        }
        renderizarTabla(productosData);
        cerrarModalEditar();
        alert('✅ Producto actualizado');
    } catch (error) {
        alert('❌ Error al actualizar: ' + error.message);
    }
}

async function eliminarProducto() {
    // Confirma la intención y, si la API responde bien, retira la fila de la pantalla.
    const selectedRow = document.querySelector('#tablaProductos tbody tr.selected');
    if (!selectedRow) {
        alert('Selecciona un producto para eliminar');
        return;
    }
    const id = selectedRow.dataset.id;
    if (!confirm(`¿Eliminar el producto con ID ${id}?`)) return;

    try {
        await deleteData(`/productos/${id}`);
        productosData = productosData.filter(p => String(p.idProducto) !== id);
        renderizarTabla(productosData);
        alert('🗑 Producto eliminado');
    } catch (error) {
        alert('❌ ' + mensajeErrorProducto(error));
    }
}

// ============ LIMPIAR FORMULARIOS ============
function limpiarFormularioAgregar() {
    document.getElementById('txtNombreProducto').value = '';
    document.getElementById('txtSkuProducto').value = '';
    document.getElementById('txtCategoriaProducto').value = '';
    document.getElementById('txtMarcaProducto').value = '';
    document.getElementById('txtModeloProducto').value = '';
    document.getElementById('txtPrecioCosto').value = '';
    document.getElementById('txtPrecioVenta').value = '';
    document.getElementById('txtStockProducto').value = '';
    document.getElementById('txtStockMinimo').value = '';
    document.getElementById('selectProveedor').value = '';
}

function limpiarFormularioEditar() {
    document.getElementById('txtIdProductoEdit').value = '';
    document.getElementById('txtNombreProductoEdit').value = '';
    document.getElementById('txtSkuProductoEdit').value = '';
    document.getElementById('txtCategoriaProductoEdit').value = '';
    document.getElementById('txtMarcaProductoEdit').value = '';
    document.getElementById('txtModeloProductoEdit').value = '';
    document.getElementById('txtPrecioCostoEdit').value = '';
    document.getElementById('txtPrecioVentaEdit').value = '';
    document.getElementById('txtStockProductoEdit').value = '';
    document.getElementById('txtStockMinimoEdit').value = '';
    document.getElementById('selectProveedorEdit').value = '';
}

// ============ CONFIGURAR EVENTOS ============
function configurarEventosProductos() {
    // Deja conectados búsqueda, formularios, selección y acciones de la tabla.
    console.log('✅ configurarEventosProductos ejecutado');
    const btnBuscar = document.getElementById('btnBuscarProducto');
    if (btnBuscar) {
        btnBuscar.addEventListener('click', buscarProductos);
        console.log('✅ Evento buscar asignado');
    } else {
        console.error('❌ btnBuscarProducto no encontrado');
    }

    const txtBuscar = document.getElementById('txtBuscarProducto');
    if (txtBuscar) {
        txtBuscar.addEventListener('keyup', (e) => {
            if (e.key === 'Enter') buscarProductos();
        });
        console.log('✅ Evento keyup asignado');
    } else {
        console.error('❌ txtBuscarProducto no encontrado');
    }

    const btnActualizar = document.getElementById('btnActualizarProducto');
    if (btnActualizar) {
        btnActualizar.addEventListener('click', cargarProductos);
        console.log('✅ Evento actualizar asignado');
    } else {
        console.error('❌ btnActualizarProducto no encontrado');
    }

    const btnAgregar = document.getElementById('btnAgregarProducto');
    if (btnAgregar) {
        btnAgregar.addEventListener('click', abrirModalAgregar);
        console.log('✅ Evento agregar asignado');
    } else {
        console.error('❌ btnAgregarProducto no encontrado');
    }

    document.getElementById('closeAgregarProducto')?.addEventListener('click', cerrarModalAgregar);
    document.getElementById('btnVolverProducto')?.addEventListener('click', cerrarModalAgregar);
    document.getElementById('btnGuardarProducto')?.addEventListener('click', guardarProducto);
    document.getElementById('btnLimpiarProducto')?.addEventListener('click', limpiarFormularioAgregar);

    document.getElementById('btnModificarProducto')?.addEventListener('click', abrirModalEditar);
    document.getElementById('closeEditarProducto')?.addEventListener('click', cerrarModalEditar);
    document.getElementById('btnVolverProductoEdit')?.addEventListener('click', cerrarModalEditar);
    document.getElementById('btnGuardarProductoEdit')?.addEventListener('click', guardarProductoEdit);
    document.getElementById('btnLimpiarProductoEdit')?.addEventListener('click', limpiarFormularioEditar);

    document.getElementById('btnEliminarProducto')?.addEventListener('click', eliminarProducto);

    document.addEventListener('click', function (e) {
        const tr = e.target.closest('#tablaProductos tbody tr');
        if (tr) {
            document.querySelectorAll('#tablaProductos tbody tr').forEach(row => row.classList.remove('selected'));
            tr.classList.add('selected');
        }
    });
}

// ============ EXPONER FUNCIÓN DE INICIALIZACIÓN ============
window.inicializarProductos = inicializarProductos;