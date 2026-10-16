package uniminuto.datronix.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uniminuto.datronix.dto.DetalleVentaRequestDTO;
import uniminuto.datronix.dto.VentaCreateDTO;
import uniminuto.datronix.dto.VentaResponseDTO;
import uniminuto.datronix.dto.VentaRecienteResponseDTO;
import uniminuto.datronix.entity.Cliente;
import uniminuto.datronix.entity.DetalleVenta;
import uniminuto.datronix.entity.Producto;
import uniminuto.datronix.entity.Remision;
import uniminuto.datronix.entity.Venta;
import uniminuto.datronix.exception.ClienteNotFoundException;
import uniminuto.datronix.exception.ProductoNotFoundException;
import uniminuto.datronix.exception.StockInsuficienteException;
import uniminuto.datronix.exception.VentaNotFoundException;
import uniminuto.datronix.exception.VentaTotalExcedidoException;
import uniminuto.datronix.mapper.VentaMapper;
import uniminuto.datronix.repository.ClienteRepository;
import uniminuto.datronix.repository.ProductoRepository;
import uniminuto.datronix.repository.RemisionRepository;
import uniminuto.datronix.repository.VentaRepository;

@Service
public class VentaService {
    private static final int MAX_TOTAL_INTEGER_DIGITS = 8;

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final RemisionRepository remisionRepository;

    public VentaService(
            VentaRepository ventaRepository,
            ClienteRepository clienteRepository,
            ProductoRepository productoRepository,
            RemisionRepository remisionRepository) {
        this.ventaRepository = ventaRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.remisionRepository = remisionRepository;
    }

    @Transactional(readOnly = true)
    public List<VentaResponseDTO> listarVentas() {
        List<Venta> ventas = ventaRepository.findAllWithDetails();
        if (ventas.isEmpty()) {
            return List.of();
        }
        List<Long> ids = ventas.stream().map(Venta::getIdVenta).toList();
        Map<Long, Remision> remisiones = remisionRepository.findByVentaIds(ids).stream()
                .collect(Collectors.toMap(remision -> remision.getVenta().getIdVenta(), Function.identity()));
        return ventas.stream()
                .map(venta -> VentaMapper.toDto(venta, remisiones.get(venta.getIdVenta())))
                .toList();
    }

    @Transactional(readOnly = true)
    public VentaResponseDTO buscarVentaPorId(Long id) {
        Venta venta = ventaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new VentaNotFoundException(id));
        Remision remision = remisionRepository.findByVentaIds(List.of(id)).stream()
                .findFirst()
                .orElse(null);
        return VentaMapper.toDto(venta, remision);
    }

    @Transactional(readOnly = true)
    public List<VentaRecienteResponseDTO> listarVentasRecientes() {
        return ventaRepository.findLatestSales(PageRequest.of(0, 5)).stream()
                .map(venta -> VentaRecienteResponseDTO.builder()
                        .idVenta(venta.getIdVenta())
                        .nombreCliente(venta.getCliente() == null ? null : venta.getCliente().getNombreCliente())
                        .fechaVenta(venta.getFechaVenta())
                        .totalVenta(venta.getTotalVenta())
                        .estadoPago(venta.getEstadoPago())
                        .build())
                .toList();
    }

    @Transactional
    public VentaResponseDTO registrarVenta(VentaCreateDTO request) {
        String idCliente = request.getIdCliente().trim();
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ClienteNotFoundException(idCliente));

        Venta venta = Venta.builder()
                .cliente(cliente)
                .fechaVenta(request.getFechaVenta())
                .estadoPago(request.getEstadoPago())
                .direccionEntrega(request.getDireccionEntrega().trim())
                .totalVenta(BigDecimal.ZERO)
                .build();
        BigDecimal total = BigDecimal.ZERO;

        for (DetalleVentaRequestDTO detalleRequest : request.getDetalles()) {
            Producto producto = productoRepository.findByIdForUpdate(detalleRequest.getIdProducto())
                    .orElseThrow(() -> new ProductoNotFoundException(detalleRequest.getIdProducto()));
            long stockActual = producto.getStockProducto();
            long cantidad = detalleRequest.getCantidad();
            if (stockActual < cantidad) {
                throw new StockInsuficienteException(producto.getIdProducto());
            }

            BigDecimal precioUnitario = producto.getPrecioVentaProducto();
            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
            total = total.add(subtotal);
            validarPrecisionTotal(subtotal);
            validarPrecisionTotal(total);

            producto.setStockProducto(stockActual - cantidad);
            productoRepository.save(producto);
            venta.addDetalle(DetalleVenta.builder()
                    .producto(producto)
                    .cantidad(cantidad)
                    .precioUnitario(precioUnitario)
                    .descuentoItem(BigDecimal.ZERO)
                    .subtotalItem(subtotal)
                    .build());
        }

        venta.setTotalVenta(total);
        Venta guardada = ventaRepository.save(venta);
        Remision remision = remisionRepository.save(Remision.builder()
                .venta(guardada)
                .cliente(cliente)
                .fechaEmision(LocalDateTime.now())
                .totalRemision(total)
                .observaciones(request.getObservacionesRemision() == null
                        ? null : request.getObservacionesRemision().trim())
                .build());
        return VentaMapper.toDto(guardada, remision);
    }

    private void validarPrecisionTotal(BigDecimal importe) {
        if (importe.precision() - importe.scale() > MAX_TOTAL_INTEGER_DIGITS) {
            throw new VentaTotalExcedidoException();
        }
    }
}
