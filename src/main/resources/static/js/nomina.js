import { deleteData, getData, postData, putData } from './api.js';

let empleadosNomina = [];
let pagosNomina = [];
let empleadoEnEdicion = null;

function dineroNomina(valor) {
    return `$${Number(valor || 0).toLocaleString('es-CO')}`;
}

function mensajeErrorNomina(error) {
    try {
        return JSON.parse(error.message).mensaje || error.message;
    } catch {
        return error.message;
    }
}

function fechaNomina(fecha) {
    return fecha ? new Date(`${fecha}T00:00:00`).toLocaleDateString('es-CO') : '—';
}

function periodoActualNomina() {
    const hoy = new Date();
    return `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}`;
}

function actualizarResumenNomina() {
    const periodo = periodoActualNomina();
    const pagosMes = pagosNomina.filter(pago => pago.periodo === periodo);
    document.getElementById('kpiEmpleados').textContent = String(empleadosNomina.length);
    document.getElementById('kpiPeriodoNomina').textContent = periodo;
    document.getElementById('kpiTotalNomina').textContent =
        dineroNomina(pagosMes.reduce((total, pago) => total + Number(pago.salarioPagado), 0));
}

function renderizarEmpleados() {
    const tbody = document.getElementById('empleadosTbody');
    tbody.replaceChildren();
    if (empleadosNomina.length === 0) {
        const row = tbody.insertRow();
        const cell = row.insertCell();
        cell.colSpan = 8;
        cell.textContent = 'No hay empleados registrados.';
    } else {
        empleadosNomina.forEach(empleado => {
            const row = tbody.insertRow();
            [
                empleado.idEmpleado,
                empleado.nombreEmpleado,
                empleado.cargo,
                empleado.correoEmpleado || '—',
                empleado.telefonoEmpleado || '—',
                fechaNomina(empleado.fechaIngreso),
                dineroNomina(empleado.salarioBase)
            ].forEach(value => {
                row.insertCell().textContent = String(value);
            });
            const actions = row.insertCell();
            const edit = document.createElement('button');
            edit.className = 'table-icon-button';
            edit.type = 'button';
            edit.dataset.editarEmpleado = empleado.idEmpleado;
            edit.setAttribute('aria-label', 'Editar');
            edit.innerHTML = '<i class="fas fa-pencil-alt"></i>';
            const remove = document.createElement('button');
            remove.className = 'table-icon-button';
            remove.type = 'button';
            remove.dataset.eliminarEmpleado = empleado.idEmpleado;
            remove.setAttribute('aria-label', 'Eliminar');
            remove.innerHTML = '<i class="fas fa-trash"></i>';
            actions.append(edit, remove);
        });
    }
    document.getElementById('empleadosEstado').textContent = `${empleadosNomina.length} empleados`;
}

function renderizarPagos() {
    const tbody = document.getElementById('nominaTbody');
    tbody.replaceChildren();
    if (pagosNomina.length === 0) {
        const row = tbody.insertRow();
        const cell = row.insertCell();
        cell.colSpan = 6;
        cell.textContent = 'No hay pagos de nómina registrados.';
    } else {
        pagosNomina.forEach(pago => {
            const row = tbody.insertRow();
            [
                pago.idNomina,
                pago.idEmpleado,
                pago.nombreEmpleado,
                pago.periodo,
                fechaNomina(pago.fechaPago),
                dineroNomina(pago.salarioPagado)
            ].forEach(value => {
                row.insertCell().textContent = String(value);
            });
        });
    }
    document.getElementById('nominaEstado').textContent = `${pagosNomina.length} pagos registrados`;
}

function actualizarSelectorEmpleados() {
    const select = document.querySelector('#formPagoNomina [name="idEmpleado"]');
    select.replaceChildren(new Option('Seleccionar...', ''));
    empleadosNomina.forEach(empleado => {
        select.add(new Option(`${empleado.nombreEmpleado} — ${empleado.idEmpleado}`, empleado.idEmpleado));
    });
}

async function cargarNomina() {
    [empleadosNomina, pagosNomina] = await Promise.all([
        getData('/empleados'),
        getData('/nomina')
    ]);
    actualizarResumenNomina();
    renderizarEmpleados();
    renderizarPagos();
    actualizarSelectorEmpleados();
}

function prepararFormularioEmpleado(empleado) {
    const form = document.getElementById('formEmpleado');
    form.reset();
    empleadoEnEdicion = empleado?.idEmpleado || null;
    document.getElementById('tituloDialogEmpleado').textContent =
        empleado ? 'Editar empleado' : 'Agregar empleado';
    form.elements.idEmpleado.readOnly = Boolean(empleado);
    document.getElementById('formEmpleadoEstado').textContent = '';
    if (empleado) {
        Object.entries(empleado).forEach(([key, value]) => {
            if (form.elements[key]) form.elements[key].value = value ?? '';
        });
    }
    document.getElementById('dialogEmpleado').showModal();
}

async function guardarEmpleado(event) {
    event.preventDefault();
    const form = event.currentTarget;
    if (!form.reportValidity()) return;
    const data = Object.fromEntries(new FormData(form).entries());
    data.salarioBase = Number(data.salarioBase);
    data.correoEmpleado = data.correoEmpleado.trim() || null;
    data.telefonoEmpleado = data.telefonoEmpleado.trim() || null;
    try {
        if (empleadoEnEdicion) {
            await putData(`/empleados/${encodeURIComponent(empleadoEnEdicion)}`, data);
        } else {
            await postData('/empleados', data);
        }
    } catch (error) {
        document.getElementById('formEmpleadoEstado').textContent =
            `No se pudo guardar el empleado: ${mensajeErrorNomina(error)}`;
        return;
    }

    document.getElementById('dialogEmpleado').close();
    empleadoEnEdicion = null;
    try {
        await cargarNomina();
    } catch (error) {
        document.getElementById('empleadosEstado').textContent =
            `Empleado guardado, pero no se pudo actualizar la vista: ${mensajeErrorNomina(error)}`;
    }
}

async function eliminarEmpleado(idEmpleado) {
    if (!window.confirm(`¿Eliminar al empleado ${idEmpleado}?`)) return;
    try {
        await deleteData(`/empleados/${encodeURIComponent(idEmpleado)}`);
        await cargarNomina();
    } catch (error) {
        document.getElementById('empleadosEstado').textContent =
            `No se pudo eliminar el empleado: ${mensajeErrorNomina(error)}`;
    }
}

async function guardarPago(event) {
    event.preventDefault();
    const form = event.currentTarget;
    if (!form.reportValidity()) return;
    const data = Object.fromEntries(new FormData(form).entries());
    data.salarioPagado = Number(data.salarioPagado);
    try {
        await postData('/nomina', data);
    } catch (error) {
        document.getElementById('formPagoEstado').textContent =
            `No se pudo registrar el pago: ${mensajeErrorNomina(error)}`;
        return;
    }
    document.getElementById('dialogPago').close();
    form.reset();
    try {
        await cargarNomina();
    } catch (error) {
        document.getElementById('nominaEstado').textContent =
            `Pago registrado, pero no se pudo actualizar la vista: ${mensajeErrorNomina(error)}`;
    }
}

function inicializarNomina() {
    const hoy = new Date();
    const fechaLocal = new Date(hoy.getTime() - hoy.getTimezoneOffset() * 60000)
        .toISOString().slice(0, 10);
    const formPago = document.getElementById('formPagoNomina');
    formPago.elements.fechaPago.value = fechaLocal;
    formPago.elements.periodo.value = periodoActualNomina();
    document.querySelectorAll('[data-open-dialog]').forEach(button => {
        button.addEventListener('click', () => {
            if (button.dataset.openDialog === 'dialogEmpleado') prepararFormularioEmpleado(null);
            if (button.dataset.openDialog === 'dialogPago') {
                formPago.reset();
                formPago.elements.fechaPago.value = fechaLocal;
                formPago.elements.periodo.value = periodoActualNomina();
                document.getElementById('formPagoEstado').textContent = '';
                document.getElementById('dialogPago').showModal();
            }
        });
    });
    document.querySelectorAll('.native-dialog').forEach(dialog => {
        dialog.querySelectorAll('[data-close-dialog]').forEach(button => {
            button.addEventListener('click', () => dialog.close());
        });
        dialog.addEventListener('click', event => {
            if (event.target === dialog) dialog.close();
        });
    });
    document.getElementById('formEmpleado').addEventListener('submit', guardarEmpleado);
    formPago.addEventListener('submit', guardarPago);
    formPago.elements.idEmpleado.addEventListener('change', () => {
        const empleado = empleadosNomina.find(item => item.idEmpleado === formPago.elements.idEmpleado.value);
        if (empleado && !formPago.elements.salarioPagado.value) {
            formPago.elements.salarioPagado.value = empleado.salarioBase;
        }
    });
    document.getElementById('empleadosTbody').addEventListener('click', event => {
        const edit = event.target.closest('[data-editar-empleado]');
        const remove = event.target.closest('[data-eliminar-empleado]');
        if (edit) {
            const empleado = empleadosNomina.find(item => item.idEmpleado === edit.dataset.editarEmpleado);
            if (empleado) prepararFormularioEmpleado(empleado);
        } else if (remove) {
            eliminarEmpleado(remove.dataset.eliminarEmpleado);
        }
    });

    cargarNomina().catch(error => {
        const message = mensajeErrorNomina(error);
        document.getElementById('empleadosEstado').textContent = `No se pudieron cargar empleados: ${message}`;
        document.getElementById('nominaEstado').textContent = `No se pudieron cargar pagos: ${message}`;
    });
}

window.inicializarNomina = inicializarNomina;
