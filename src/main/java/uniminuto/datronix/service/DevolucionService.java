package uniminuto.datronix.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uniminuto.datronix.dto.DevolucionCreateDTO;
import uniminuto.datronix.dto.DevolucionResponseDTO;
import uniminuto.datronix.entity.Devolucion;
import uniminuto.datronix.entity.Producto;
import uniminuto.datronix.exception.ProductoNotFoundException;
import uniminuto.datronix.exception.StockLimiteExcedidoException;
import uniminuto.datronix.mapper.DevolucionMapper;
import uniminuto.datronix.repository.DevolucionRepository;
import uniminuto.datronix.repository.ProductoRepository;

@Service
public class DevolucionService {
    private final DevolucionRepository devolucionRepository;
    private final ProductoRepository productoRepository;

    public DevolucionService(
            DevolucionRepository devolucionRepository,
            ProductoRepository productoRepository) {
        this.devolucionRepository = devolucionRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<DevolucionResponseDTO> listarDevoluciones() {
        return devolucionRepository.findAllByOrderByFechaDevolucionDescIdDevolucionDesc()
                .stream()
                .map(DevolucionMapper::toDto)
                .toList();
    }

    @Transactional
    public DevolucionResponseDTO registrarDevolucion(DevolucionCreateDTO request) {
        Producto producto = productoRepository.findByIdForUpdate(request.getIdProducto())
                .orElseThrow(() -> new ProductoNotFoundException(request.getIdProducto()));
        long stockActual = producto.getStockProducto();
        long cantidad = request.getCantidad();
        if (stockActual > Long.MAX_VALUE - cantidad) {
            throw new StockLimiteExcedidoException(producto.getIdProducto());
        }

        producto.setStockProducto(stockActual + cantidad);
        productoRepository.save(producto);
        Devolucion devolucion = Devolucion.builder()
                .producto(producto)
                .nombreCliente(request.getNombreCliente() == null
                        ? null : request.getNombreCliente().trim())
                .cantidad(cantidad)
                .motivo(request.getMotivo() == null ? null : request.getMotivo().trim())
                .fechaDevolucion(request.getFechaDevolucion())
                .build();
        return DevolucionMapper.toDto(devolucionRepository.save(devolucion));
    }
}
