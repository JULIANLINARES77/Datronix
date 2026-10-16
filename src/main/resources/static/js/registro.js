import { postData } from './api.js';

document.getElementById('formRegistro').addEventListener('submit', async event => {
    event.preventDefault();
    const form = event.currentTarget;
    if (!form.reportValidity()) return;
    const estado = document.getElementById('estadoRegistro');
    estado.textContent = 'Creando cuenta...';
    try {
        await postData('/usuarios', Object.fromEntries(new FormData(form).entries()));
        estado.textContent = 'Cuenta creada. Redirigiendo al inicio de sesión...';
        window.setTimeout(() => {
            window.location.href = '/html/Inicio_Sesion.html';
        }, 900);
    } catch (error) {
        let mensaje = error.message;
        try {
            mensaje = JSON.parse(error.message).mensaje || error.message;
        } catch {
            // El servidor puede responder con un mensaje que no sea JSON.
        }
        estado.textContent = `No se pudo crear la cuenta: ${mensaje}`;
    }
});
