import { deleteData, getData, postData, putData } from './api.js';

let usuariosSistema = [];
const rolesDisponibles = ['ADMINISTRADOR', 'OPERADOR', 'BODEGA'];

function errorUsuario(error) {
    try {
        return JSON.parse(error.message).mensaje || error.message;
    } catch {
        return error.message;
    }
}

function renderizarUsuarios() {
    const filtro = document.getElementById('buscarUsuarios').value.trim().toLowerCase();
    const filtrados = usuariosSistema.filter(usuario =>
        usuario.idUsuario.toLowerCase().includes(filtro) ||
        usuario.nombreUsuario.toLowerCase().includes(filtro) ||
        usuario.correoUsuario.toLowerCase().includes(filtro)
    );
    const tbody = document.getElementById('usuariosTbody');
    tbody.replaceChildren();
    if (filtrados.length === 0) {
        const row = tbody.insertRow();
        const cell = row.insertCell();
        cell.colSpan = 5;
        cell.textContent = 'No hay usuarios que coincidan con la búsqueda.';
    } else {
        filtrados.forEach(usuario => {
            const row = tbody.insertRow();
            [
                usuario.idUsuario,
                usuario.nombreUsuario,
                usuario.correoUsuario
            ].forEach(value => {
                row.insertCell().textContent = value;
            });
            const roleCell = row.insertCell();
            const roleSelect = document.createElement('select');
            roleSelect.className = 'users-role-select';
            roleSelect.dataset.rolUsuario = usuario.idUsuario;
            rolesDisponibles.forEach(rol => {
                const option = document.createElement('option');
                option.value = rol;
                option.textContent = rol.charAt(0) + rol.slice(1).toLowerCase();
                option.selected = rol === (usuario.rolUsuario || 'OPERADOR');
                roleSelect.appendChild(option);
            });
            roleCell.appendChild(roleSelect);
            const action = row.insertCell();
            const update = document.createElement('button');
            update.className = 'table-icon-button';
            update.type = 'button';
            update.dataset.actualizarRol = usuario.idUsuario;
            update.setAttribute('aria-label', `Actualizar rol de ${usuario.idUsuario}`);
            update.title = 'Actualizar rol';
            update.innerHTML = '<i class="fas fa-save"></i>';
            action.appendChild(update);
            const remove = document.createElement('button');
            remove.className = 'table-icon-button';
            remove.type = 'button';
            remove.dataset.eliminarUsuario = usuario.idUsuario;
            remove.setAttribute('aria-label', `Eliminar usuario ${usuario.idUsuario}`);
            remove.innerHTML = '<i class="fas fa-trash"></i>';
            action.appendChild(remove);
        });
    }
    document.getElementById('usuariosEstado').textContent =
        `Mostrando ${filtrados.length} de ${usuariosSistema.length} usuarios`;
    document.getElementById('kpiUsuarios').textContent = String(usuariosSistema.length);
    document.getElementById('kpiCorreosUsuarios').textContent =
        String(new Set(usuariosSistema.map(usuario => usuario.correoUsuario.toLowerCase())).size);
}

async function cargarUsuarios() {
    usuariosSistema = await getData('/usuarios');
    renderizarUsuarios();
}

async function crearUsuario(event) {
    event.preventDefault();
    const form = event.currentTarget;
    if (!form.reportValidity()) return;
    const request = Object.fromEntries(new FormData(form).entries());
    try {
        await postData('/usuarios', request);
        form.reset();
        document.getElementById('estadoUsuario').textContent = 'Usuario registrado correctamente.';
        try {
            await cargarUsuarios();
        } catch (error) {
            document.getElementById('usuariosEstado').textContent =
                `Usuario creado, pero no se pudo actualizar el listado: ${errorUsuario(error)}`;
        }
    } catch (error) {
        document.getElementById('estadoUsuario').textContent =
            `No se pudo registrar el usuario: ${errorUsuario(error)}`;
    }
}

async function eliminarUsuario(idUsuario) {
    if (!window.confirm(`¿Eliminar al usuario ${idUsuario}?`)) return;
    try {
        await deleteData(`/usuarios/${encodeURIComponent(idUsuario)}`);
        await cargarUsuarios();
    } catch (error) {
        document.getElementById('usuariosEstado').textContent =
            `No se pudo eliminar el usuario: ${errorUsuario(error)}`;
    }
}

async function actualizarRol(idUsuario) {
    const select = document.querySelector(`[data-rol-usuario="${CSS.escape(idUsuario)}"]`);
    if (!select) return;
    try {
        await putData(`/usuarios/${encodeURIComponent(idUsuario)}/rol`, {
            rolUsuario: select.value
        });
        const sessionUser = JSON.parse(localStorage.getItem('usuario') || 'null');
        if (sessionUser?.idUsuario === idUsuario) {
            localStorage.removeItem('usuario');
            window.alert('Tu rol cambió. Inicia sesión de nuevo para abrir la interfaz correspondiente.');
            window.location.href = '/html/Inicio_Sesion.html';
            return;
        }
        await cargarUsuarios();
        document.getElementById('usuariosEstado').textContent =
            `Rol actualizado para ${idUsuario}. Debe iniciar sesión de nuevo para aplicar el cambio.`;
    } catch (error) {
        document.getElementById('usuariosEstado').textContent =
            `No se pudo actualizar el rol: ${errorUsuario(error)}`;
    }
}

async function inicializarUsuarios() {
    document.getElementById('formUsuario').addEventListener('submit', crearUsuario);
    document.getElementById('buscarUsuarios').addEventListener('input', renderizarUsuarios);
    document.getElementById('usuariosTbody').addEventListener('click', event => {
        const updateButton = event.target.closest('[data-actualizar-rol]');
        if (updateButton) actualizarRol(updateButton.dataset.actualizarRol);
        const button = event.target.closest('[data-eliminar-usuario]');
        if (button) eliminarUsuario(button.dataset.eliminarUsuario);
    });
    try {
        await cargarUsuarios();
    } catch (error) {
        const message = errorUsuario(error);
        document.getElementById('usuariosEstado').textContent = `No se pudieron cargar usuarios: ${message}`;
        document.getElementById('usuariosTbody').textContent = message;
    }
}

window.inicializarUsuarios = inicializarUsuarios;
