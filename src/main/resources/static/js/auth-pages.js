async function solicitarCodigo(url, correo) {
    const response = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ correoUsuario: correo })
    });
    const body = await response.json();
    if (!response.ok) throw new Error(body.mensaje || 'No fue posible enviar el código.');
    return body;
}

async function enviarCodigoDesbloqueo() {
    const button = document.getElementById('btnEnviarCodigo');
    const email = document.getElementById('correoVerificacion');
    const message = document.getElementById('mensajeCodigo');
    const inputs = [...document.querySelectorAll('.verification-code input')];
    let sent = false;
    button.disabled = true;
    message.textContent = 'Solicitando código...';
    try {
        await solicitarCodigo('/api/auth/unlock/request', email.value.trim());
        inputs.forEach(input => { input.value = ''; });
        iniciarTemporizadorCodigo();
        sent = true;
        message.textContent = 'Si la cuenta está registrada y bloqueada, recibirás un código válido por 2 minutos.';
        inputs[0].focus();
    } catch (error) {
        message.textContent = error.message;
    } finally {
        button.disabled = sent;
    }
}

function iniciarTemporizadorCodigo() {
    const timer = document.querySelector('#temporizadorCodigo span');
    const timerWrap = document.getElementById('temporizadorCodigo');
    const verifyButton = document.getElementById('btnVerificarCodigo');
    const sendButton = document.getElementById('btnEnviarCodigo');
    const inputs = [...document.querySelectorAll('.verification-code input')];
    let seconds = 120;
    if (window.authCodeTimer) clearInterval(window.authCodeTimer);

    const render = () => {
        timer.textContent = `${String(Math.floor(seconds / 60)).padStart(2, '0')}:${String(seconds % 60).padStart(2, '0')}`;
        const expired = seconds === 0;
        timerWrap.classList.toggle('is-expired', expired);
        sendButton.disabled = !expired;
        inputs.forEach(input => { input.disabled = expired; });
        verifyButton.disabled = expired || inputs.some(input => input.value === '');
    };
    render();
    window.authCodeTimer = setInterval(() => {
        seconds = Math.max(0, seconds - 1);
        render();
        if (seconds === 0) clearInterval(window.authCodeTimer);
    }, 1000);
}

function mostrarTiempoBloqueo() {
    const panel = document.getElementById('estadoBloqueo');
    const timer = document.querySelector('#temporizadorBloqueo span');
    const message = document.getElementById('mensajeBloqueo');
    const bloqueadaHasta = new URLSearchParams(window.location.search).get('bloqueadaHasta');
    const deadline = Date.parse(bloqueadaHasta || '');
    if (!panel || !timer || !message || !Number.isFinite(deadline)) return;

    panel.hidden = false;
    const render = () => {
        const remaining = Math.max(0, Math.ceil((deadline - Date.now()) / 1000));
        const hours = Math.floor(remaining / 3600);
        const minutes = Math.floor((remaining % 3600) / 60);
        const seconds = remaining % 60;
        timer.textContent = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
        if (remaining === 0) {
            message.textContent = 'El bloqueo temporal terminó. Ya puedes intentar ingresar con tu contraseña.';
            clearInterval(window.authLockTimer);
        }
    };

    if (window.authLockTimer) clearInterval(window.authLockTimer);
    render();
    window.authLockTimer = setInterval(render, 1000);
}

function inicializarVerificacion() {
    const sendButton = document.getElementById('btnEnviarCodigo');
    const verifyButton = document.getElementById('btnVerificarCodigo');
    const message = document.getElementById('mensajeCodigo');
    const inputs = [...document.querySelectorAll('.verification-code input')];
    if (!sendButton || !verifyButton || inputs.length !== 6) return;

    const queryEmail = new URLSearchParams(window.location.search).get('correo');
    if (queryEmail) document.getElementById('correoVerificacion').value = queryEmail;
    mostrarTiempoBloqueo();
    sendButton.addEventListener('click', enviarCodigoDesbloqueo);

    inputs.forEach((input, index) => {
        input.addEventListener('input', () => {
            input.value = input.value.replace(/\D/g, '').slice(-1);
            if (input.value && index < inputs.length - 1) inputs[index + 1].focus();
            verifyButton.disabled = inputs.some(item => item.value === '') || inputs[0].disabled;
        });
        input.addEventListener('keydown', event => {
            if (event.key === 'Backspace' && !input.value && index > 0) inputs[index - 1].focus();
        });
    });

    verifyButton.addEventListener('click', async () => {
        const code = inputs.map(input => input.value).join('');
        verifyButton.disabled = true;
        message.textContent = 'Validando código...';
        try {
            const response = await fetch('/api/auth/unlock/verify', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    correoUsuario: document.getElementById('correoVerificacion').value.trim(),
                    codigo: code
                })
            });
            const result = await response.json();
            if (!response.ok) throw new Error(result.mensaje || 'No fue posible validar el código.');
            localStorage.setItem('usuario', JSON.stringify(result.usuario));
            window.location.href = result.usuario.rolUsuario === 'BODEGA' ? '/bodega/' : '/html/inicio.html';
        } catch (error) {
            message.textContent = error.message;
            verifyButton.disabled = false;
        }
    });
}

function inicializarRestablecimiento() {
    const password = document.getElementById('nuevaContrasena');
    const confirmation = document.getElementById('confirmarContrasena');
    const toggle = document.querySelector('[data-toggle-password]');
    const form = document.getElementById('formRestablecerContrasena');
    const email = document.getElementById('correoRestablecimiento');
    const code = document.getElementById('codigoRestablecimiento');
    const status = document.getElementById('mensajeRestablecimiento');
    if (!password || !confirmation || !toggle || !form || !email || !code || !status) return;
    const queryEmail = new URLSearchParams(window.location.search).get('correo');
    if (queryEmail) email.value = queryEmail;
    code.addEventListener('input', () => {
        code.value = code.value.replace(/\D/g, '').slice(0, 6);
    });

    toggle.addEventListener('click', () => {
        const showPassword = password.type === 'password';
        password.type = showPassword ? 'text' : 'password';
        confirmation.type = showPassword ? 'text' : 'password';
        toggle.setAttribute('aria-label', showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña');
        toggle.innerHTML = `<i class="fas fa-eye${showPassword ? '-slash' : ''}"></i>`;
    });

    function updateRequirements() {
        const criteria = {
            length: password.value.length >= 8,
            uppercase: /[A-Z]/.test(password.value),
            number: /[0-9]/.test(password.value)
        };
        document.querySelectorAll('[data-requirement]').forEach(item => {
            const met = criteria[item.dataset.requirement];
            item.dataset.met = String(met);
            const label = item.textContent.slice(2);
            item.textContent = `${met ? '✓' : '•'} ${label}`;
        });

        const hasConfirmation = confirmation.value.length > 0;
        const matches = password.value.length > 0 && password.value === confirmation.value;
        document.getElementById('mensajeNoCoincide').hidden = !hasConfirmation || matches;
        document.getElementById('mensajeCoincide').hidden = !matches;
    }

    password.addEventListener('input', updateRequirements);
    confirmation.addEventListener('input', updateRequirements);
    form.addEventListener('submit', async event => {
        event.preventDefault();
        if (password.value.length < 8 || !/[A-Z]/.test(password.value) || !/[0-9]/.test(password.value)) {
            status.textContent = 'La contraseña debe tener 8 caracteres, una mayúscula y un número.';
            return;
        }
        if (password.value !== confirmation.value) {
            status.textContent = 'Las contraseñas no coinciden.';
            return;
        }
        status.textContent = 'Actualizando contraseña...';
        const submit = form.querySelector('button[type="submit"]');
        submit.disabled = true;
        try {
            const response = await fetch('/api/auth/password-reset/confirm', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    correoUsuario: email.value.trim(),
                    codigo: code.value.trim(),
                    nuevaContrasena: password.value
                })
            });
            const result = await response.json();
            if (!response.ok) throw new Error(result.mensaje || 'No fue posible restablecer la contraseña.');
            status.textContent = result.mensaje;
            window.setTimeout(() => { window.location.href = '/html/Inicio_Sesion.html'; }, 1200);
        } catch (error) {
            status.textContent = error.message;
            submit.disabled = false;
        }
    });
    updateRequirements();
}

function inicializarSolicitudRestablecimiento() {
    const button = document.getElementById('btnSolicitarRestablecimiento');
    const email = document.getElementById('correoRecuperacion');
    const message = document.getElementById('mensajeRecuperacion');
    const link = document.getElementById('continuarRestablecimiento');
    if (!button || !email || !message || !link) return;

    button.addEventListener('click', async () => {
        button.disabled = true;
        message.textContent = 'Solicitando código...';
        try {
            const result = await solicitarCodigo('/api/auth/password-reset/request', email.value.trim());
            message.textContent = result.mensaje;
            link.href = `/html/restablecer.html?correo=${encodeURIComponent(email.value.trim())}`;
            link.hidden = false;
        } catch (error) {
            message.textContent = error.message;
        } finally {
            button.disabled = false;
        }
    });
}

document.addEventListener('DOMContentLoaded', () => {
    if (document.body.dataset.authPage === 'verificar') inicializarVerificacion();
    if (document.body.dataset.authPage === 'restablecer') inicializarRestablecimiento();
    inicializarSolicitudRestablecimiento();
});