function inicializarVerificacion() {
    const sendButton = document.getElementById('btnEnviarCodigo');
    const verifyButton = document.getElementById('btnVerificarCodigo');
    const timer = document.querySelector('#temporizadorCodigo span');
    const timerWrap = document.getElementById('temporizadorCodigo');
    const message = document.getElementById('mensajeCodigo');
    const inputs = [...document.querySelectorAll('.verification-code input')];
    if (!sendButton || !verifyButton || inputs.length !== 6) return;

    let sent = false;
    let seconds = 120;
    let intervalId = null;

    function render() {
        const minutes = String(Math.floor(seconds / 60)).padStart(2, '0');
        const remainingSeconds = String(seconds % 60).padStart(2, '0');
        const expired = sent && seconds === 0;
        const complete = inputs.every(input => input.value !== '');
        timer.textContent = `${minutes}:${remainingSeconds}`;
        timerWrap.classList.toggle('is-expired', expired);
        inputs.forEach(input => { input.disabled = !sent || expired; });
        verifyButton.disabled = !complete || expired;
        message.textContent = expired
            ? 'El código expiró. Solicita uno nuevo para continuar.'
            : sent
                ? 'Revisa tu bandeja de entrada y escribe los 6 dígitos.'
                : 'Primero envía el código a tu correo registrado.';
    }

    sendButton.addEventListener('click', () => {
        sent = true;
        seconds = 120;
        inputs.forEach(input => { input.value = ''; });
        if (intervalId) clearInterval(intervalId);
        intervalId = setInterval(() => {
            if (seconds > 0) seconds -= 1;
            render();
            if (seconds === 0) {
                clearInterval(intervalId);
                intervalId = null;
            }
        }, 1000);
        sendButton.textContent = 'Reenviar código';
        render();
        setTimeout(() => inputs[0].focus(), 50);
    });

    inputs.forEach((input, index) => {
        input.addEventListener('input', () => {
            input.value = input.value.replace(/\D/g, '').slice(-1);
            if (input.value && index < inputs.length - 1) inputs[index + 1].focus();
            render();
        });
        input.addEventListener('keydown', event => {
            if (event.key === 'Backspace' && !input.value && index > 0) inputs[index - 1].focus();
        });
    });

    verifyButton.addEventListener('click', () => {
        if (!verifyButton.disabled) window.location.href = '/html/inicio.html';
    });
    render();
}

function inicializarRestablecimiento() {
    const password = document.getElementById('nuevaContrasena');
    const confirmation = document.getElementById('confirmarContrasena');
    const toggle = document.querySelector('[data-toggle-password]');
    if (!password || !confirmation || !toggle) return;

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
    updateRequirements();
}

document.addEventListener('DOMContentLoaded', () => {
    if (document.body.dataset.authPage === 'verificar') inicializarVerificacion();
    if (document.body.dataset.authPage === 'restablecer') inicializarRestablecimiento();
});