package uniminuto.datronix.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uniminuto.datronix.entity.Cliente;
import uniminuto.datronix.entity.Compra;
import uniminuto.datronix.entity.Devolucion;
import uniminuto.datronix.entity.DetalleCompra;
import uniminuto.datronix.entity.DetalleVenta;
import uniminuto.datronix.entity.Empleado;
import uniminuto.datronix.entity.Nomina;
import uniminuto.datronix.entity.Producto;
import uniminuto.datronix.entity.Proveedor;
import uniminuto.datronix.entity.Remision;
import uniminuto.datronix.entity.Soporte;
import uniminuto.datronix.entity.Usuario;
import uniminuto.datronix.entity.Venta;
import uniminuto.datronix.repository.ClienteRepository;
import uniminuto.datronix.repository.CompraRepository;
import uniminuto.datronix.repository.DevolucionRepository;
import uniminuto.datronix.repository.EmpleadoRepository;
import uniminuto.datronix.repository.NominaRepository;
import uniminuto.datronix.repository.ProductoRepository;
import uniminuto.datronix.repository.ProveedorRepository;
import uniminuto.datronix.repository.RemisionRepository;
import uniminuto.datronix.repository.SoporteRepository;
import uniminuto.datronix.repository.UsuarioRepository;
import uniminuto.datronix.repository.VentaRepository;

@Service
public class TestDataSeeder {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestDataSeeder.class);
    private static final int ROWS_PER_TABLE = 50;
    private static final String TEST_PASSWORD = "Prueba123!";

    private final ClienteRepository clienteRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final CompraRepository compraRepository;
    private final VentaRepository ventaRepository;
    private final RemisionRepository remisionRepository;
    private final DevolucionRepository devolucionRepository;
    private final EmpleadoRepository empleadoRepository;
    private final NominaRepository nominaRepository;
    private final SoporteRepository soporteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public TestDataSeeder(
            ClienteRepository clienteRepository,
            ProveedorRepository proveedorRepository,
            ProductoRepository productoRepository,
            CompraRepository compraRepository,
            VentaRepository ventaRepository,
            RemisionRepository remisionRepository,
            DevolucionRepository devolucionRepository,
            EmpleadoRepository empleadoRepository,
            NominaRepository nominaRepository,
            SoporteRepository soporteRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate) {
        this.clienteRepository = clienteRepository;
        this.proveedorRepository = proveedorRepository;
        this.productoRepository = productoRepository;
        this.compraRepository = compraRepository;
        this.ventaRepository = ventaRepository;
        this.remisionRepository = remisionRepository;
        this.devolucionRepository = devolucionRepository;
        this.empleadoRepository = empleadoRepository;
        this.nominaRepository = nominaRepository;
        this.soporteRepository = soporteRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void seed() {
        if (clienteRepository.existsById(clienteId(1))) {
            LOGGER.info("Prueba de datos ya cargada; no se insertaron duplicados.");
            return;
        }
        Map<String, Long> countsBefore = contarFilas();

        List<Cliente> clientes = crearClientes();
        List<Proveedor> proveedores = crearProveedores();
        List<Empleado> empleados = crearEmpleados();
        List<Usuario> usuarios = crearUsuarios();
        List<Producto> productos = crearProductos(proveedores);
        List<Compra> compras = crearCompras(proveedores, productos);
        List<Venta> ventas = crearVentas(clientes, productos);
        List<Remision> remisiones = crearRemisiones(clientes, ventas);
        List<Devolucion> devoluciones = crearDevoluciones(clientes, productos);
        List<Nomina> nominas = crearNominas(empleados);
        List<Soporte> tickets = crearTickets();

        clienteRepository.saveAllAndFlush(clientes);
        proveedorRepository.saveAllAndFlush(proveedores);
        empleadoRepository.saveAllAndFlush(empleados);
        usuarioRepository.saveAllAndFlush(usuarios);
        productoRepository.saveAllAndFlush(productos);
        compraRepository.saveAllAndFlush(compras);
        ventaRepository.saveAllAndFlush(ventas);
        remisionRepository.saveAllAndFlush(remisiones);
        devolucionRepository.saveAllAndFlush(devoluciones);
        nominaRepository.saveAllAndFlush(nominas);
        soporteRepository.saveAllAndFlush(tickets);

        Map<String, Long> insertedRows = new LinkedHashMap<>();
        contarFilas().forEach((table, count) -> {
            long inserted = count - countsBefore.get(table);
            if (inserted != ROWS_PER_TABLE) {
                throw new IllegalStateException(
                        "Se esperaban 50 registros nuevos en " + table + ", pero se insertaron " + inserted);
            }
            insertedRows.put(table, inserted);
        });
        LOGGER.info("Conteo validado: {} filas nuevas en cada tabla: {}", ROWS_PER_TABLE, insertedRows);
        LOGGER.info("Datos de prueba cargados: 50 filas en cada una de las 13 tablas de negocio.");
        LOGGER.info(
                "Usuarios de prueba: prueba.usuario.001@example.test y prueba.usuario.002@example.test; contraseña: {}",
                TEST_PASSWORD);
    }

    private Map<String, Long> contarFilas() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String table : List.of(
                "cliente", "proveedor", "empleado", "usuario", "producto", "compra", "detalle_compra",
                "venta", "detalle_venta", "remision", "devolucion", "nomina", "soporte")) {
            Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
            counts.put(table, count);
        }
        return counts;
    }

    private List<Cliente> crearClientes() {
        List<Cliente> rows = new ArrayList<>(ROWS_PER_TABLE);
        for (int i = 1; i <= ROWS_PER_TABLE; i++) {
            rows.add(Cliente.builder()
                    .idCliente(clienteId(i))
                    .nombreCliente("Cliente de prueba " + numero(i))
                    .telefonoCliente(3000000000L + i)
                    .emailCliente("cliente." + numero(i) + "@example.test")
                    .direccionCliente("Dirección de prueba " + i)
                    .tipoCliente(i % 2 == 0 ? "EMPRESA" : "PERSONA")
                    .build());
        }
        return rows;
    }

    private List<Proveedor> crearProveedores() {
        List<Proveedor> rows = new ArrayList<>(ROWS_PER_TABLE);
        for (int i = 1; i <= ROWS_PER_TABLE; i++) {
            rows.add(Proveedor.builder()
                    .idProveedor(proveedorId(i))
                    .nombreEmpresa("Proveedor de prueba " + numero(i))
                    .contactoProveedor("Contacto de prueba " + i)
                    .telefonoProveedor(3100000000L + i)
                    .emailProveedor("proveedor." + numero(i) + "@example.test")
                    .direccionProveedor("Dirección proveedor " + i)
                    .build());
        }
        return rows;
    }

    private List<Empleado> crearEmpleados() {
        List<Empleado> rows = new ArrayList<>(ROWS_PER_TABLE);
        for (int i = 1; i <= ROWS_PER_TABLE; i++) {
            rows.add(Empleado.builder()
                    .idEmpleado(empleadoId(i))
                    .nombreEmpleado("Empleado de prueba " + numero(i))
                    .cargo(i % 2 == 0 ? "Auxiliar" : "Asesor")
                    .correoEmpleado("empleado." + numero(i) + "@example.test")
                    .telefonoEmpleado("320000" + String.format("%04d", i))
                    .fechaIngreso(LocalDate.now().minusDays(i))
                    .salarioBase(new BigDecimal("1800000.00").add(BigDecimal.valueOf(i * 1000L)))
                    .build());
        }
        return rows;
    }

    private List<Usuario> crearUsuarios() {
        List<Usuario> rows = new ArrayList<>(ROWS_PER_TABLE);
        String hashedPassword = passwordEncoder.encode(TEST_PASSWORD);
        for (int i = 1; i <= ROWS_PER_TABLE; i++) {
            rows.add(Usuario.builder()
                    .idUsuario(String.format("TESTUSR%06d", i))
                    .nombreUsuario("Usuario de prueba " + numero(i))
                    .correoUsuario("prueba.usuario." + numero(i) + "@example.test")
                    .contrasenaUsuario(hashedPassword)
                    .rolUsuario(i == 1 ? "ADMINISTRADOR" : i == 2 ? "BODEGA" : "OPERADOR")
                    .build());
        }
        return rows;
    }

    private List<Producto> crearProductos(List<Proveedor> proveedores) {
        List<Producto> rows = new ArrayList<>(ROWS_PER_TABLE);
        for (int i = 1; i <= ROWS_PER_TABLE; i++) {
            rows.add(Producto.builder()
                    .nombreProducto("Producto de prueba " + numero(i))
                    .categoriaProducto("Pruebas")
                    .marcaProducto("Datronix")
                    .modeloProducto("TEST-" + numero(i))
                    .descripcionProducto("Registro sintético para validar el sistema")
                    .especificacionesProducto("Dato de prueba; no corresponde a una referencia comercial")
                    .precioCostoProducto(new BigDecimal("50000.00").add(BigDecimal.valueOf(i * 100L)))
                    .precioVentaProducto(new BigDecimal("75000.00").add(BigDecimal.valueOf(i * 100L)))
                    .stockProducto(100L)
                    .stockMinimoProducto(5L)
                    .proveedor(proveedores.get(i - 1))
                    .skuProducto("TEST-SKU-" + numero(i))
                    .build());
        }
        return rows;
    }

    private List<Compra> crearCompras(List<Proveedor> proveedores, List<Producto> productos) {
        List<Compra> rows = new ArrayList<>(ROWS_PER_TABLE);
        for (int i = 0; i < ROWS_PER_TABLE; i++) {
            Producto producto = productos.get(i);
            BigDecimal precio = producto.getPrecioCostoProducto();
            Compra compra = Compra.builder()
                    .proveedor(proveedores.get(i))
                    .fechaCompra(LocalDate.now().minusDays(i))
                    .totalCompra(precio)
                    .build();
            compra.addDetalle(DetalleCompra.builder()
                    .producto(producto)
                    .cantidad(1L)
                    .precioUnitario(precio)
                    .build());
            rows.add(compra);
        }
        return rows;
    }

    private List<Venta> crearVentas(List<Cliente> clientes, List<Producto> productos) {
        List<Venta> rows = new ArrayList<>(ROWS_PER_TABLE);
        for (int i = 0; i < ROWS_PER_TABLE; i++) {
            Producto producto = productos.get(i);
            BigDecimal precio = producto.getPrecioVentaProducto();
            producto.setStockProducto(99L);
            Venta venta = Venta.builder()
                    .cliente(clientes.get(i))
                    .fechaVenta(LocalDate.now().minusDays(i))
                    .totalVenta(precio)
                    .estadoPago(i % 2 == 0 ? "CONTADO" : "CREDITO")
                    .direccionEntrega("Dirección de entrega de prueba " + (i + 1))
                    .build();
            venta.addDetalle(DetalleVenta.builder()
                    .producto(producto)
                    .cantidad(1L)
                    .precioUnitario(precio)
                    .descuentoItem(BigDecimal.ZERO)
                    .subtotalItem(precio)
                    .build());
            rows.add(venta);
        }
        return rows;
    }

    private List<Remision> crearRemisiones(List<Cliente> clientes, List<Venta> ventas) {
        List<Remision> rows = new ArrayList<>(ROWS_PER_TABLE);
        for (int i = 0; i < ROWS_PER_TABLE; i++) {
            Venta venta = ventas.get(i);
            rows.add(Remision.builder()
                    .venta(venta)
                    .cliente(clientes.get(i))
                    .fechaEmision(venta.getFechaVenta().atStartOfDay())
                    .totalRemision(venta.getTotalVenta())
                    .observaciones("Remisión sintética de prueba " + numero(i + 1))
                    .build());
        }
        return rows;
    }

    private List<Devolucion> crearDevoluciones(List<Cliente> clientes, List<Producto> productos) {
        List<Devolucion> rows = new ArrayList<>(ROWS_PER_TABLE);
        for (int i = 0; i < ROWS_PER_TABLE; i++) {
            Producto producto = productos.get(i);
            producto.setStockProducto(100L);
            rows.add(Devolucion.builder()
                    .producto(producto)
                    .nombreCliente(clientes.get(i).getNombreCliente())
                    .cantidad(1L)
                    .motivo("Registro sintético de prueba")
                    .fechaDevolucion(LocalDateTime.now().minusDays(i))
                    .build());
        }
        return rows;
    }

    private List<Nomina> crearNominas(List<Empleado> empleados) {
        List<Nomina> rows = new ArrayList<>(ROWS_PER_TABLE);
        LocalDate hoy = LocalDate.now();
        String periodo = hoy.toString().substring(0, 7);
        for (int i = 0; i < ROWS_PER_TABLE; i++) {
            Empleado empleado = empleados.get(i);
            rows.add(Nomina.builder()
                    .empleado(empleado)
                    .fechaPago(hoy.minusDays(i))
                    .salarioPagado(empleado.getSalarioBase())
                    .periodo(periodo)
                    .build());
        }
        return rows;
    }

    private List<Soporte> crearTickets() {
        List<Soporte> rows = new ArrayList<>(ROWS_PER_TABLE);
        for (int i = 1; i <= ROWS_PER_TABLE; i++) {
            rows.add(Soporte.builder()
                    .empresa("Empresa de prueba " + numero(i))
                    .idRemitente(String.format("TESTSUP%06d", i))
                    .nombreRemitente("Remitente de prueba " + i)
                    .telefonoRemitente("300000" + String.format("%04d", i))
                    .asunto("Ticket sintético de prueba " + numero(i))
                    .descripcion("Solicitud ficticia para validar el módulo de soporte.")
                    .fechaCreacion(LocalDateTime.now().minusDays(i))
                    .build());
        }
        return rows;
    }

    private String clienteId(int i) {
        return "TEST-CLI-" + numero(i);
    }

    private String proveedorId(int i) {
        return "TEST-PROV-" + numero(i);
    }

    private String empleadoId(int i) {
        return String.format("TESTEMP%06d", i);
    }

    private String numero(int i) {
        return String.format("%03d", i);
    }
}
