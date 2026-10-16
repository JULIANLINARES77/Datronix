package uniminuto.datronix.service;

import uniminuto.datronix.dto.ProductoDTO;
import uniminuto.datronix.entity.Producto;
import uniminuto.datronix.entity.Proveedor;
import uniminuto.datronix.exception.ProductoNotFoundException;
import uniminuto.datronix.exception.ProductoSkuDuplicadoException;
import uniminuto.datronix.exception.ProveedorNotFoundException;
import uniminuto.datronix.mapper.ProductoMapper;
import uniminuto.datronix.repository.ProductoRepository;
import uniminuto.datronix.repository.ProveedorRepository;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
// Reúne el flujo de productos y resuelve la relación entre cada producto y su proveedor.
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;

    public ProductoService(ProductoRepository productoRepository, ProveedorRepository proveedorRepository) {
        this.productoRepository = productoRepository;
        this.proveedorRepository = proveedorRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarProductos() {
        // Lee los productos y convierte cada entidad al formato que consume el frontend.
        return productoRepository.findAll()
                .stream()
                .map(ProductoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductoDTO buscarProductoPorId(Long id) {
        // Si el producto no existe, se lanza una excepción específica para devolver un error claro.
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException(id));
        return ProductoMapper.toDto(producto);
    }

    @Transactional
    public ProductoDTO guardarProducto(ProductoDTO dto) {
        dto.setIdProducto(null);
        normalizar(dto);
        validarSku(dto.getSkuProducto(), null);

        Proveedor proveedor = proveedorRepository.findById(dto.getIdProveedor())
                .orElseThrow(() -> new ProveedorNotFoundException(dto.getIdProveedor()));

        Producto producto = ProductoMapper.toEntity(dto, proveedor);
        Producto guardado = productoRepository.save(producto);
        return ProductoMapper.toDto(guardado);
    }

    @Transactional
    public ProductoDTO actualizarProducto(Long id, ProductoDTO dto) {
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException(id));
        normalizar(dto);
        validarSku(dto.getSkuProducto(), id);

        Proveedor proveedor = proveedorRepository.findById(dto.getIdProveedor())
                .orElseThrow(() -> new ProveedorNotFoundException(dto.getIdProveedor()));
        productoExistente.setNombreProducto(dto.getNombreProducto());
        productoExistente.setCategoriaProducto(dto.getCategoriaProducto());
        productoExistente.setMarcaProducto(dto.getMarcaProducto());
        productoExistente.setModeloProducto(dto.getModeloProducto());
        if (dto.getDescripcionProducto() != null) {
            productoExistente.setDescripcionProducto(dto.getDescripcionProducto());
        }
        if (dto.getEspecificacionesProducto() != null) {
            productoExistente.setEspecificacionesProducto(dto.getEspecificacionesProducto());
        }
        productoExistente.setPrecioCostoProducto(dto.getPrecioCostoProducto());
        productoExistente.setPrecioVentaProducto(dto.getPrecioVentaProducto());
        productoExistente.setStockProducto(dto.getStockProducto());
        productoExistente.setStockMinimoProducto(dto.getStockMinimoProducto());
        productoExistente.setSkuProducto(dto.getSkuProducto());
        productoExistente.setProveedor(proveedor);

        Producto actualizado = productoRepository.save(productoExistente);
        return ProductoMapper.toDto(actualizado);
    }

    private void validarSku(String sku, Long idProductoExcluido) {
        if (sku == null) {
            return;
        }
        boolean duplicado = idProductoExcluido == null
                ? productoRepository.existsBySkuProductoIgnoreCase(sku)
                : productoRepository.existsBySkuProductoIgnoreCaseAndIdProductoNot(sku, idProductoExcluido);
        if (duplicado) {
            throw new ProductoSkuDuplicadoException(sku);
        }
    }

    private void normalizar(ProductoDTO dto) {
        dto.setNombreProducto(dto.getNombreProducto().trim());
        dto.setCategoriaProducto(limpiarOpcional(dto.getCategoriaProducto()));
        dto.setMarcaProducto(limpiarOpcional(dto.getMarcaProducto()));
        dto.setModeloProducto(limpiarOpcional(dto.getModeloProducto()));
        dto.setDescripcionProducto(limpiarOpcional(dto.getDescripcionProducto()));
        dto.setEspecificacionesProducto(limpiarOpcional(dto.getEspecificacionesProducto()));
        dto.setIdProveedor(dto.getIdProveedor().trim());
        String sku = limpiarOpcional(dto.getSkuProducto());
        dto.setSkuProducto(sku == null ? null : sku.toUpperCase(Locale.ROOT));
    }

    private String limpiarOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    public void eliminarProducto(Long id) {
        // Primero se confirma la existencia para que un ID inválido produzca un mensaje útil.
        if (!productoRepository.existsById(id)) {
            throw new ProductoNotFoundException(id);
        }
        productoRepository.deleteById(id);
    }
}