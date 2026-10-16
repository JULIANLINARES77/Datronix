// js/api.js - Comunicación con el backend de Spring Boot

// URL base de tu backend (cambia si está en otro puerto)
const API_BASE = '/api';

// Todas las pantallas usan estas funciones para hablar con el backend sin repetir fetch.

// Función para obtener los headers JSON comunes a las peticiones.
function getHeaders() {
    return { 'Content-Type': 'application/json' };
}

// ========== MÉTODOS GENÉRICOS ==========

// GET
export async function getData(endpoint) {
    // Consulta información y convierte la respuesta correcta en datos JavaScript.
    const response = await fetch(`${API_BASE}${endpoint}`, {
        method: 'GET',
        headers: getHeaders()
    });
    if (!response.ok) {
        const error = await response.text();
        throw new Error(error || `Error ${response.status}`);
    }
    return response.json();
}

// POST
export async function postData(endpoint, data) {
    // Envía información nueva y devuelve el registro que el servidor confirma.
    const response = await fetch(`${API_BASE}${endpoint}`, {
        method: 'POST',
        headers: getHeaders(),
        body: JSON.stringify(data)
    });
    if (!response.ok) {
        const error = await response.text();
        throw new Error(error || `Error ${response.status}`);
    }
    return response.json();
}

// PUT
export async function putData(endpoint, data) {
    // Envía los cambios de un registro y devuelve la respuesta del backend.
    const response = await fetch(`${API_BASE}${endpoint}`, {
        method: 'PUT',
        headers: getHeaders(),
        body: JSON.stringify(data)
    });
    if (!response.ok) {
        const error = await response.text();
        throw new Error(error || `Error ${response.status}`);
    }
    return response.json();
}

// DELETE
export async function deleteData(endpoint) {
    // Solicita el borrado; se devuelve la respuesta completa porque normalmente no trae JSON.
    const response = await fetch(`${API_BASE}${endpoint}`, {
        method: 'DELETE',
        headers: getHeaders()
    });
    if (!response.ok) {
        const error = await response.text();
        throw new Error(error || `Error ${response.status}`);
    }
    return response;
}