// js/inicio.js - Versión definitiva con rutas absolutas

const menuItems = document.querySelectorAll('.menu-item');
const btnSalir = document.getElementById('btnSalir');
const fechaSpan = document.getElementById('fechaActual');
const vistaContainer = document.getElementById('vistaContainer');
const sidebar = document.getElementById('sidebar');
const sidebarBackdrop = document.getElementById('sidebarBackdrop');
const usuarioSesion = JSON.parse(localStorage.getItem('usuario') || 'null');
if (usuarioSesion?.rolUsuario === 'BODEGA') {
    window.location.replace(window.DATRONIX_BODEGA_URL || '/bodega/');
}
if (usuarioSesion?.rolUsuario !== 'ADMINISTRADOR') {
    document.querySelector('.menu-item[data-vista="usuarios"]')?.remove();
}
const breadcrumbByView = {
    principal: 'Administrador/Control/Panel principal',
    ventas: 'Administrador/Ventas/Nueva venta',
    historial: 'Administrador/Ventas/Historial y remisiones',
    clientes: 'Administrador/CRM/Clientes',
    productos: 'Administrador/Inventario/Productos',
    proveedores: 'Administrador/Compras/Proveedores',
    compras: 'Administrador/Compras',
    devoluciones: 'Administrador/Ventas/Devoluciones',
    reportes: 'Administrador/Reportes',
    nomina: 'Administrador/Nómina',
    usuarios: 'Administrador/Usuarios',
    soporte: 'Administrador/Soporte',
    ia: 'Asistente IA'
};
const scriptsPorVista = {
    principal: '/js/dashboard.js',
    ventas: '/js/ventas.js',
    compras: '/js/compras.js',
    devoluciones: '/js/devoluciones.js',
    historial: '/js/historial.js',
    nomina: '/js/nomina.js',
    usuarios: '/js/usuarios.js',
    reportes: '/js/reportes.js',
    soporte: '/js/soporte.js',
    clientes: '/js/clientes.js',
    productos: '/js/productos.js',
    proveedores: '/js/proveedores.js'
};
const inicializadoresPorVista = {
    principal: 'inicializarDashboard'
};

// Este archivo coordina el menú: carga primero el HTML de cada módulo y después su JavaScript.

// ============ RUTAS DE MÓDULOS (ABSOLUTAS) ============
const modulos = {
    principal: { titulo: '📊 Panel de Control', archivo: '/modulos/principal.html' },
    ventas: { titulo: '💰 Módulo de Ventas', archivo: '/modulos/ventas.html' },
    productos: { titulo: '📦 Gestión de Productos', archivo: '/modulos/productos.html' },
    clientes: { titulo: '👥 Gestión de Clientes', archivo: '/modulos/clientes.html' },
    proveedores: { titulo: '🏢 Gestión de Proveedores', archivo: '/modulos/proveedores.html' },
    compras: { titulo: '🛒 Gestión de Compras', archivo: '/modulos/compras.html' },
    nomina: { titulo: '📄 Nómina', archivo: '/modulos/nomina.html' },
    usuarios: { titulo: 'Usuarios', archivo: '/modulos/usuarios.html' },
    devoluciones: { titulo: '↩️ Devoluciones', archivo: '/modulos/devoluciones.html' },
    reportes: { titulo: '📈 Reportes y Estadísticas', archivo: '/modulos/reportes.html' },
    historial: { titulo: '📋 Historial de Ventas', archivo: '/modulos/historial.html' },
    soporte: { titulo: '🛠️ Soporte', archivo: '/modulos/soporte.html' },
    ia: { titulo: '🤖 Asistente IA', archivo: '/modulos/ia.html' }
};

let timeoutId = null;

function actualizarFecha() {
    // Muestra en el encabezado la fecha actual usando el idioma del usuario.
    const ahora = new Date();
    const opciones = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    fechaSpan.textContent = 'Hoy es ' + ahora.toLocaleDateString('es-CO', opciones);
}

function cerrarMenuMovil() {
    sidebar?.classList.remove('is-open');
    sidebarBackdrop?.classList.remove('is-visible');
}

async function cargarVista(vistaId) {
    // Cada cambio de menú reemplaza la vista y prepara el módulo elegido.
    const modulo = modulos[vistaId];
    if (!modulo) {
        vistaContainer.innerHTML = `<div class="placeholder"><p>Módulo no encontrado</p></div>`;
        return;
    }

    // LIMPIAR TIMEOUTS ANTERIORES
    if (timeoutId) {
        clearTimeout(timeoutId);
        timeoutId = null;
    }

    try {
        // 1. Cargar el HTML
        const response = await fetch(modulo.archivo);
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const html = await response.text();
        vistaContainer.innerHTML = html;
        document.querySelector('.top-bar .titulo').textContent = breadcrumbByView[vistaId] || 'Datronix';

        // El HTML se inserta antes de importar el script para que sus elementos ya existan.

        // 2. Actualizar clase activa en el menú
        document.querySelectorAll('.menu-item').forEach(btn => btn.classList.remove('active'));
        const menuBtn = document.querySelector(`.menu-item[data-vista="${vistaId}"]`);
        if (menuBtn) menuBtn.classList.add('active');

        // Los módulos de muestra son HTML estático; solo los CRUD necesitan cargar un inicializador.
        const scriptPath = scriptsPorVista[vistaId];
        if (scriptPath) {
            try {
                await import(scriptPath);
                console.log(`✅ Script ${scriptPath} importado`);
                const initFnName = inicializadoresPorVista[vistaId] ||
                    `inicializar${vistaId.charAt(0).toUpperCase() + vistaId.slice(1)}`;
                timeoutId = setTimeout(() => {
                    if (typeof window[initFnName] === 'function') {
                        window[initFnName]();
                    }
                    timeoutId = null;
                }, 100);
            } catch (err) {
                console.error(`❌ Error al importar ${scriptPath}:`, err);
            }
        }

    } catch (error) {
        vistaContainer.innerHTML = `<div class="placeholder"><p>Error al cargar la vista: ${error.message}</p></div>`;
        console.error('❌ Error cargando vista:', error);
    }
}

// Eventos del menú
menuItems.forEach(btn => {
    btn.addEventListener('click', function () {
        cargarVista(this.dataset.vista);
        cerrarMenuMovil();
    });
});

document.getElementById('btnAbrirMenu')?.addEventListener('click', function () {
    sidebar?.classList.add('is-open');
    sidebarBackdrop?.classList.add('is-visible');
});
sidebarBackdrop?.addEventListener('click', cerrarMenuMovil);
document.addEventListener('click', function (event) {
    const quickLink = event.target.closest('[data-vista].quick-link');
    if (quickLink) cargarVista(quickLink.dataset.vista);
});

const assistantPanel = document.getElementById('assistantPanel');
const assistantToggle = document.getElementById('assistantToggle');
const assistantMessages = document.getElementById('assistantMessages');
const assistantInput = document.getElementById('assistantInput');

const preguntasAsistente = [
    '¿Cómo registro una venta?',
    '¿Qué hago con un crédito vencido?',
    '¿Cómo repongo stock bajo?',
    '¿Dónde veo las remisiones?'
];

const respuestasAsistente = {
    '¿Cómo registro una venta?': 'Ve al módulo Ventas, busca el cliente por cédula, agrega productos al carrito, define dirección de entrega y estado de pago (CONTADO o CRÉDITO) y finaliza. La remisión se genera automáticamente.',
    '¿Qué hago con un crédito vencido?': 'Abre Reportes > Créditos pendientes. Allí ves las ventas con estado CRÉDITO y los días transcurridos para gestionar el cobro.',
    '¿Cómo repongo stock bajo?': 'En Reportes > Stock bajo verás los productos por debajo del mínimo, resaltados en rojo. Contacta al proveedor asociado desde el módulo Proveedores.',
    '¿Dónde veo las remisiones?': 'En Historial de ventas, cada venta tiene el botón Ver detalle con los productos y la remisión generada.'
};

let mensajesAsistente = [{ remitente: 'ia', texto: '¡Hola! Soy el asistente Datronix IA. ¿En qué te ayudo hoy?' }];

function renderizarAsistente() {
    assistantMessages.replaceChildren();
    mensajesAsistente.forEach(mensaje => {
        const bubble = document.createElement('div');
        bubble.className = `assistant-message ${mensaje.remitente === 'ia' ? 'from-ai' : 'from-user'}`;
        bubble.textContent = mensaje.texto;
        assistantMessages.appendChild(bubble);
    });

    const questions = document.createElement('div');
    questions.className = 'assistant-questions';
    preguntasAsistente.forEach(pregunta => {
        const button = document.createElement('button');
        button.className = 'assistant-question';
        button.type = 'button';
        button.textContent = pregunta;
        button.addEventListener('click', () => enviarMensajeAsistente(pregunta));
        questions.appendChild(button);
    });
    assistantMessages.appendChild(questions);
    assistantMessages.scrollTop = assistantMessages.scrollHeight;
}

function enviarMensajeAsistente(texto) {
    const consulta = texto.trim();
    if (!consulta) return;
    mensajesAsistente.push(
        { remitente: 'usuario', texto: consulta },
        { remitente: 'ia', texto: respuestasAsistente[consulta] || 'Estoy en modo demostración. Pronto podré responder consultas sobre tus ventas, inventario y cartera en tiempo real.' }
    );
    assistantInput.value = '';
    renderizarAsistente();
}

function cambiarAsistente(abierto) {
    assistantPanel.hidden = !abierto;
    assistantToggle.setAttribute('aria-expanded', String(abierto));
    assistantToggle.innerHTML = `<i class="fas fa-${abierto ? 'times' : 'robot'}"></i>`;
}

assistantToggle?.addEventListener('click', () => cambiarAsistente(assistantPanel.hidden));
document.querySelector('.btn-ia')?.addEventListener('click', () => cambiarAsistente(true));
document.getElementById('assistantForm')?.addEventListener('submit', event => {
    event.preventDefault();
    enviarMensajeAsistente(assistantInput.value);
});
renderizarAsistente();

btnSalir.addEventListener('click', function () {
    // Solo se abandona la pantalla principal después de pedir confirmación.
    if (confirm('¿Estás seguro de que quieres salir de Datronix?')) {
        localStorage.removeItem('usuario');
        window.location.href = '/html/Inicio_Sesion.html';
    }
});

// Inicialización
document.addEventListener('DOMContentLoaded', function () {
    // Al abrir el panel se muestra la fecha y se carga la vista principal.
    actualizarFecha();
    cargarVista('principal');
});

window.cargarVista = cargarVista;
window.actualizarFecha = actualizarFecha;