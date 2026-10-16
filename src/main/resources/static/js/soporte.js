import { getData, postData } from './api.js';

function fechaSoporte(fecha) {
    return fecha ? new Date(fecha).toLocaleString('es-CO') : '—';
}

function mensajeErrorSoporte(error) {
    try {
        return JSON.parse(error.message).mensaje || error.message;
    } catch {
        return error.message;
    }
}

function renderizarTickets(tickets) {
    const tbody = document.getElementById('ticketsSoporteTbody');
    tbody.replaceChildren();
    if (tickets.length === 0) {
        const row = tbody.insertRow();
        const cell = row.insertCell();
        cell.colSpan = 8;
        cell.textContent = 'No hay tickets registrados.';
        return;
    }
    tickets.forEach(ticket => {
        const row = tbody.insertRow();
        [
            ticket.idSoporte,
            ticket.empresa,
            ticket.idRemitente,
            ticket.nombreRemitente || '—',
            ticket.telefonoRemitente,
            ticket.asunto,
            ticket.descripcion,
            fechaSoporte(ticket.fechaCreacion)
        ].forEach(value => {
            row.insertCell().textContent = String(value);
        });
    });
}

async function cargarTicketsSoporte() {
    renderizarTickets(await getData('/soporte'));
}

async function enviarTicketSoporte(event) {
    event.preventDefault();
    const form = event.currentTarget;
    if (!form.reportValidity()) return;
    const data = Object.fromEntries(new FormData(form).entries());
    data.nombreRemitente = data.nombreRemitente.trim() || null;
    try {
        await postData('/soporte', data);
        form.reset();
        const ahora = new Date();
        form.elements.fechaCreacion.value = new Date(
            ahora.getTime() - ahora.getTimezoneOffset() * 60000
        ).toISOString().slice(0, 16);
        document.getElementById('estadoTicketSoporte').textContent = 'Ticket enviado correctamente.';
        try {
            await cargarTicketsSoporte();
        } catch (error) {
            document.getElementById('estadoTicketSoporte').textContent =
                `Ticket enviado, pero no se pudo actualizar el listado: ${mensajeErrorSoporte(error)}`;
        }
    } catch (error) {
        document.getElementById('estadoTicketSoporte').textContent =
            `No se pudo enviar el ticket: ${mensajeErrorSoporte(error)}`;
    }
}

async function inicializarSoporte() {
    const form = document.getElementById('formTicketSoporte');
    const ahora = new Date();
    form.elements.fechaCreacion.value = new Date(
        ahora.getTime() - ahora.getTimezoneOffset() * 60000
    ).toISOString().slice(0, 16);
    form.addEventListener('submit', enviarTicketSoporte);
    try {
        await cargarTicketsSoporte();
    } catch (error) {
        document.getElementById('ticketsSoporteTbody').textContent =
            `No se pudieron cargar los tickets: ${mensajeErrorSoporte(error)}`;
    }
}

window.inicializarSoporte = inicializarSoporte;
