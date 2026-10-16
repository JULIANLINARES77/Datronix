function validarUsuario() {
    // Este es el recorrido completo del formulario: validar, consultar el servidor y entrar al panel.
    // Obtener valores del formulario
    const correoUsuario = document.getElementById('correoUsuario').value.trim();
    const contrasenaUsuario = document.getElementById('contrasenaUsuario').value.trim();

    // Validar que no estén vacíos
    if (!correoUsuario || !contrasenaUsuario) {
        alert('Por favor, complete todos los campos.');
        return;
    }

    // Crear objeto JSON para enviar al backend
    const DatosLogin = {
        correoUsuario: correoUsuario,
        contrasenaUsuario: contrasenaUsuario
    };

    // Enviar datos al backend mediante POST
    fetch('/api/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(DatosLogin)
    })
    .then(response => {
        if (response.ok) {
            // Las credenciales fueron aceptadas y el servidor devuelve los datos del usuario.
            return response.json(); // ✅ CORREGIDO: agregar paréntesis
        } else {
            // Si el login falla, leer el mensaje de error del backend
            return response.text().then(text => {
                throw new Error(text);
            });
        }
    })
    .then(data => {
        const rolUsuario = data.usuario?.rolUsuario || 'OPERADOR';
        if (rolUsuario === 'BODEGA') {
            localStorage.setItem('usuario', JSON.stringify(data.usuario));
            window.location.href = window.DATRONIX_BODEGA_URL || '/bodega/';
            return;
        }
        localStorage.setItem('usuario', JSON.stringify(data.usuario));
        // El saludo confirma visualmente el acceso antes de cambiar de pantalla.
        // Login exitoso: mostrar mensaje con el nombre del usuario (si existe)
        const nombreUsuario = data.usuario?.nombreUsuario || data.usuario?.correoUsuario || 'Usuario';
        alert('¡Bienvenido, ' + nombreUsuario + '!');
        
        // ✅ Redirigir a la página principal (ajusta la ruta según tu estructura)
        window.location.href = '/html/inicio.html';
    })
    .catch(error => {
        // Mostrar error al usuario
        alert('Error: ' + error.message);
    });
}