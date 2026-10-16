// js/clientes.js - CRUD completo (sin auto-inicialización)

import { getData, postData, putData, deleteData } from './api.js';

let clientesData = [];
let clienteEditando = null;

console.log('✅ clientes.js cargado');

// ============ INICIALIZACIÓN ============
function inicializarClientes() {
    // Prepara la pantalla: carga registros, conecta botones y muestra el usuario actual.
    console.log('✅ inicializarClientes ejecutado');
    cargarClientes();
    configurarEventosClientes();
    const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    const nombreModulo = document.getElementById('nombreUsuarioModulo');
    if (nombreModulo) nombreModulo.textContent = usuario.nombreUsuario || 'Usuario';
}

// ============ CARGAR CLIENTES ============
async function cargarClientes() {
    // La lista local se reemplaza con la información más reciente del backend.
    console.log('✅ cargarClientes ejecutado');
    try {
        clientesData = await getData('/clientes');
        console.log('✅ Datos recibidos:', clientesData);
        renderizarTabla(clientesData);
    } catch (error) {
        console.error('❌ Error al cargar clientes:', error);
        document.getElementById('tbodyClientes').textContent = `Error al cargar clientes: ${error.message}`;
    }
}

// ============ RENDERIZAR TABLA ============
function renderizarTabla(data) {
    // Convierte los datos recibidos en filas visibles dentro de la tabla.
    const tbody = document.getElementById('tbodyClientes');
    if (!tbody) {
        console.error('❌ No se encontró el elemento tbodyClientes');
        return;
    }
    if (!data || data.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-center">No hay clientes registrados</td></tr>`;
        return;
    }
    const escapar = valor => String(valor ?? '-').replace(/[&<>"']/g, caracter => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    })[caracter]);
    tbody.innerHTML = data.map(c => `
        <tr data-id="${escapar(c.idCliente)}">
            <td>${escapar(c.idCliente)}</td>
            <td>${escapar(c.nombreCliente)}</td>
            <td>${escapar(c.telefonoCliente)}</td>
            <td>${escapar(c.emailCliente)}</td>
            <td>${escapar(c.direccionCliente)}</td>
            <td>${escapar(c.tipoCliente)}</td>
            <td><button class="table-icon-button" type="button" aria-label="Editar"><i class="fas fa-pencil-alt"></i></button> <button class="table-icon-button" type="button" aria-label="Eliminar"><i class="fas fa-trash"></i></button></td>
        </tr>
    `).join('');
}

// ============ BÚSQUEDA ============
async function buscarClientes() {
    const termino = document.getElementById('txtBuscarCliente').value.trim().toLowerCase();
    if (!termino) {
        renderizarTabla(clientesData);
        return;
    }
    try {
        const resultados = await getData(`/clientes/buscar?criterio=${encodeURIComponent(termino)}`);
        renderizarTabla(resultados);
    } catch (error) {
        const tbody = document.getElementById('tbodyClientes');
        tbody.textContent = `Error al buscar clientes: ${error.message}`;
    }
}

// ============ MODALES ============
function abrirModalAgregar() {
    document.getElementById('modalAgregarCliente').classList.add('active');
    limpiarFormularioAgregar();
    document.getElementById('txtIdCliente').focus();
}

function cerrarModalAgregar() {
    document.getElementById('modalAgregarCliente').classList.remove('active');
}

function abrirModalEditar() {
    // La fila seleccionada decide qué cliente se copia al formulario de edición.
    const selectedRow = document.querySelector('#tablaClientes tbody tr.selected');
    if (!selectedRow) {
        alert('Selecciona un cliente de la tabla');
        return;
    }
    const id = selectedRow.dataset.id;
    const cliente = clientesData.find(c => c.idCliente === id);
    if (!cliente) {
        alert('Cliente no encontrado');
        return;
    }
    clienteEditando = cliente;
    document.getElementById('txtIdClienteEdit').value = cliente.idCliente;
    document.getElementById('txtNombreClienteEdit').value = cliente.nombreCliente;
    document.getElementById('txtTelefonoClienteEdit').value = cliente.telefonoCliente;
    document.getElementById('txtGmailClienteEdit').value = cliente.emailCliente || '';
    document.getElementById('txtDireccionClienteEdit').value = cliente.direccionCliente || '';
    document.getElementById('modalEditarCliente').classList.add('active');
}

function cerrarModalEditar() {
    document.getElementById('modalEditarCliente').classList.remove('active');
    clienteEditando = null;
}

// ============ CRUD ============
async function guardarCliente() {
    // Lee el formulario, valida lo indispensable y solicita crear el cliente.
    const id = document.getElementById('txtIdCliente').value.trim();
    const nombre = document.getElementById('txtNombreCliente').value.trim();
    const telefono = parseInt(document.getElementById('txtTelefonoCliente').value);
    const email = document.getElementById('txtGmailCliente').value.trim();
    const direccion = document.getElementById('txtDireccionCliente').value.trim();

    if (!id || !nombre || isNaN(telefono)) {
        alert('ID, Nombre y Teléfono son obligatorios');
        return;
    }

    const nuevoCliente = { 
        idCliente: id, 
        nombreCliente: nombre, 
        telefonoCliente: telefono, 
        emailCliente: email, 
        direccionCliente: direccion 
    };

    try {
        const creado = await postData('/clientes', nuevoCliente);
        clientesData.push(creado);
        renderizarTabla(clientesData);
        cerrarModalAgregar();
        alert('✅ Cliente guardado exitosamente');
    } catch (error) {
        alert('❌ Error al guardar: ' + error.message);
    }
}

async function guardarClienteEdit() {
    // Envía los valores editados y actualiza la copia local cuando el servidor responde.
    const id = document.getElementById('txtIdClienteEdit').value.trim();
    const nombre = document.getElementById('txtNombreClienteEdit').value.trim();
    const telefono = parseInt(document.getElementById('txtTelefonoClienteEdit').value);
    const email = document.getElementById('txtGmailClienteEdit').value.trim();
    const direccion = document.getElementById('txtDireccionClienteEdit').value.trim();

    if (!nombre || isNaN(telefono)) {
        alert('Nombre y Teléfono son obligatorios');
        return;
    }

    const clienteActualizado = { 
        idCliente: id, 
        nombreCliente: nombre, 
        telefonoCliente: telefono, 
        emailCliente: email, 
        direccionCliente: direccion 
    };

    try {
        await putData(`/clientes/${id}`, clienteActualizado);
        const index = clientesData.findIndex(c => c.idCliente === id);
        if (index !== -1) {
            clientesData[index] = clienteActualizado;
        }
        renderizarTabla(clientesData);
        cerrarModalEditar();
        alert('✅ Cliente actualizado');
    } catch (error) {
        alert('❌ Error al actualizar: ' + error.message);
    }
}

async function eliminarCliente() {
    // Solo elimina la fila elegida después de pedir confirmación a la persona usuaria.
    const selectedRow = document.querySelector('#tablaClientes tbody tr.selected');
    if (!selectedRow) {
        alert('Selecciona un cliente para eliminar');
        return;
    }
    const id = selectedRow.dataset.id;
    if (!confirm(`¿Eliminar al cliente ${id}?`)) return;

    try {
        await deleteData(`/clientes/${id}`);
        clientesData = clientesData.filter(c => c.idCliente !== id);
        renderizarTabla(clientesData);
        alert('🗑 Cliente eliminado');
    } catch (error) {
        alert('❌ Error al eliminar: ' + error.message);
    }
}

// ============ LIMPIAR FORMULARIOS ============
function limpiarFormularioAgregar() {
    document.getElementById('txtIdCliente').value = '';
    document.getElementById('txtNombreCliente').value = '';
    document.getElementById('txtTelefonoCliente').value = '';
    document.getElementById('txtGmailCliente').value = '';
    document.getElementById('txtDireccionCliente').value = '';
}

function limpiarFormularioEditar() {
    document.getElementById('txtIdClienteEdit').value = '';
    document.getElementById('txtNombreClienteEdit').value = '';
    document.getElementById('txtTelefonoClienteEdit').value = '';
    document.getElementById('txtGmailClienteEdit').value = '';
    document.getElementById('txtDireccionClienteEdit').value = '';
}

// ============ CONFIGURAR EVENTOS ============
function configurarEventosClientes() {
    // Conecta cada botón y cada interacción de la tabla con su acción correspondiente.
    console.log('✅ configurarEventosClientes ejecutado');
    // Buscar
    const btnBuscar = document.getElementById('btnBuscarCliente');
    if (btnBuscar) {
        btnBuscar.addEventListener('click', buscarClientes);
        console.log('✅ Evento buscar asignado');
    } else {
        console.error('❌ btnBuscarCliente no encontrado');
    }

    const txtBuscar = document.getElementById('txtBuscarCliente');
    if (txtBuscar) {
        txtBuscar.addEventListener('keyup', (e) => {
            if (e.key === 'Enter') buscarClientes();
        });
        console.log('✅ Evento keyup asignado');
    } else {
        console.error('❌ txtBuscarCliente no encontrado');
    }

    // Actualizar
    const btnActualizar = document.getElementById('btnActualizarCliente');
    if (btnActualizar) {
        btnActualizar.addEventListener('click', cargarClientes);
        console.log('✅ Evento actualizar asignado');
    } else {
        console.error('❌ btnActualizarCliente no encontrado');
    }

    // Agregar
    const btnAgregar = document.getElementById('btnAgregarCliente');
    if (btnAgregar) {
        btnAgregar.addEventListener('click', abrirModalAgregar);
        console.log('✅ Evento agregar asignado');
    } else {
        console.error('❌ btnAgregarCliente no encontrado');
    }

    document.getElementById('closeAgregarCliente')?.addEventListener('click', cerrarModalAgregar);
    document.getElementById('btnVolverCliente')?.addEventListener('click', cerrarModalAgregar);
    document.getElementById('btnGuardarCliente')?.addEventListener('click', guardarCliente);
    document.getElementById('btnLimpiarCliente')?.addEventListener('click', limpiarFormularioAgregar);

    // Editar
    document.getElementById('btnModificarCliente')?.addEventListener('click', abrirModalEditar);
    document.getElementById('closeEditarCliente')?.addEventListener('click', cerrarModalEditar);
    document.getElementById('btnVolverClienteEdit')?.addEventListener('click', cerrarModalEditar);
    document.getElementById('btnGuardarClienteEdit')?.addEventListener('click', guardarClienteEdit);
    document.getElementById('btnLimpiarClienteEdit')?.addEventListener('click', limpiarFormularioEditar);

    // Eliminar
    document.getElementById('btnEliminarCliente')?.addEventListener('click', eliminarCliente);

    // Selección de fila
    document.addEventListener('click', function(e) {
        const tr = e.target.closest('#tablaClientes tbody tr');
        if (tr) {
            document.querySelectorAll('#tablaClientes tbody tr').forEach(row => row.classList.remove('selected'));
            tr.classList.add('selected');
        }
    });
}

// ============ EXPONER FUNCIÓN DE INICIALIZACIÓN ============
window.inicializarClientes = inicializarClientes;