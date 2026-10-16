package uniminuto.datronix.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uniminuto.datronix.dto.CompraCreateDTO;
import uniminuto.datronix.dto.CompraResponseDTO;
import uniminuto.datronix.dto.DetalleCompraRequestDTO;
import uniminuto.datronix.entity.Compra;
import uniminuto.datronix.entity.DetalleCompra;
import uniminuto.datronix.entity.Producto;
import uniminuto.datronix.entity.Proveedor;
import uniminuto.datronix.exception.CompraNotFoundException;
import uniminuto.datronix.exception.CompraTotalExcedidoException;
import uniminuto.datronix.exception.ProductoNotFoundException;
import uniminuto.datronix.exception.ProveedorNotFoundException;
import uniminuto.datronix.exception.StockLimiteExcedidoException;
import uniminuto.datronix.mapper.CompraMapper;
import uniminuto.datronix.repository.CompraRepository;
import uniminuto.datronix.repository.ProductoRepository;
import uniminuto.datronix.repository.ProveedorRepository;

@Service
public class CompraService {
    private static final int MAX_TOTAL_INTEGER_DIGITS = 8;

    private final CompraRepository compraRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;

    public CompraService(
            CompraRepository compraRepository,
            ProveedorRepository proveedorRepository,
            ProductoRepository productoRepository) {
        this.compraRepository = compraRepository;
        this.proveedorRepository = proveedorRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<CompraResponseDTO> listarCompras() {
        return compraRepository.findAllByOrderByFechaCompraDescIdCompraDesc()
                .stream()
                .map(CompraMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompraResponseDTO buscarCompraPorId(Long id) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new CompraNotFoundException(id));
        return CompraMapper.toDto(compra);
    }

    @Transactional
    public CompraResponseDTO registrarCompra(CompraCreateDTO request) {
        String idProveedor = request.getIdProveedor().trim();
        Proveedor proveedor = proveedorRepository.findById(idProveedor)
                .orElseThrow(() -> new ProveedorNotFoundException(idProveedor));

        Compra compra = Compra.builder()
                .proveedor(proveedor)
                .fechaCompra(request.getFechaCompra())
                .totalCompra(BigDecimal.ZERO)
                .build();
        BigDecimal total = BigDecimal.ZERO;

        for (DetalleCompraRequestDTO detalleRequest : request.getDetalles()) {
            Producto producto = productoRepository.findByIdForUpdate(detalleRequest.getIdProducto())
                    .orElseThrow(() -> new ProductoNotFoundException(detalleRequest.getIdProducto()));
            long stockActual = producto.getStockProducto();
            long cantidad = detalleRequest.getCantidad();
            if (stockActual > Long.MAX_VALUE - cantidad) {
                throw new StockLimiteExcedidoException(producto.getIdProducto());
            }

            BigDecimal subtotal = detalleRequest.getPrecioUnitario().multiply(BigDecimal.valueOf(cantidad));
            total = total.add(subtotal);
            validarPrecisionTotal(subtotal);
            validarPrecisionTotal(total);

            producto.setStockProducto(stockActual + cantidad);
            productoRepository.save(producto);
            compra.addDetalle(DetalleCompra.builder()
                    .producto(producto)
                    .cantidad(cantidad)
                    .precioUnitario(detalleRequest.getPrecioUnitario())
                    .build());
        }

        compra.setTotalCompra(total);
        return CompraMapper.toDto(compraRepository.save(compra));
    }

    private void validarPrecisionTotal(BigDecimal importe) {
        if (importe.precision() - importe.scale() > MAX_TOTAL_INTEGER_DIGITS) {
            throw new CompraTotalExcedidoException();
        }
    }
}
