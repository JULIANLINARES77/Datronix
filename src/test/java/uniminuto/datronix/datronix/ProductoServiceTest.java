package uniminuto.datronix.datronix;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import uniminuto.datronix.dto.ProductoDTO;
import uniminuto.datronix.entity.Producto;
import uniminuto.datronix.entity.Proveedor;
import uniminuto.datronix.exception.ProductoSkuDuplicadoException;
import uniminuto.datronix.repository.ProductoRepository;
import uniminuto.datronix.repository.ProveedorRepository;
import uniminuto.datronix.service.ProductoService;

class ProductoServiceTest {
    private final ProductoRepository productoRepository = Mockito.mock(ProductoRepository.class);
    private final ProveedorRepository proveedorRepository = Mockito.mock(ProveedorRepository.class);
    private final ProductoService service = new ProductoService(productoRepository, proveedorRepository);

    @Test
    void updateRejectsSkuOwnedByAnotherProduct() {
        Producto existente = Producto.builder()
                .idProducto(12L)
                .nombreProducto("Teclado")
                .skuProducto("SKU-1")
                .build();
        ProductoDTO cambio = ProductoDTO.builder()
                .idProducto(12L)
                .nombreProducto("Teclado")
                .precioCostoProducto(new java.math.BigDecimal("10.00"))
                .precioVentaProducto(new java.math.BigDecimal("20.00"))
                .stockProducto(3L)
                .stockMinimoProducto(1L)
                .idProveedor("PROV-1")
                .skuProducto("sku-2")
                .build();

        when(productoRepository.findById(12L)).thenReturn(Optional.of(existente));
        when(productoRepository.existsBySkuProductoIgnoreCaseAndIdProductoNot("SKU-2", 12L))
                .thenReturn(true);

        assertThrows(ProductoSkuDuplicadoException.class, () -> service.actualizarProducto(12L, cambio));
        verify(proveedorRepository, never()).findById(any());
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void createResolvesSupplierAndNormalizesSku() {
        Proveedor proveedor = Proveedor.builder()
                .idProveedor("PROV-1")
                .nombreEmpresa("Proveedor Uno")
                .telefonoProveedor(3001234567L)
                .build();
        ProductoDTO nuevo = ProductoDTO.builder()
                .idProducto(999L)
                .nombreProducto("  Teclado  ")
                .precioCostoProducto(new java.math.BigDecimal("10.00"))
                .precioVentaProducto(new java.math.BigDecimal("20.00"))
                .stockProducto(3L)
                .stockMinimoProducto(1L)
                .idProveedor(" PROV-1 ")
                .skuProducto(" sku-1 ")
                .build();
        when(productoRepository.existsBySkuProductoIgnoreCase("SKU-1")).thenReturn(false);
        when(proveedorRepository.findById("PROV-1")).thenReturn(Optional.of(proveedor));
        AtomicBoolean entityReadyToInsert = new AtomicBoolean(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto producto = invocation.getArgument(0);
            entityReadyToInsert.set(producto.getIdProducto() == null
                    && producto.getNombreProducto().equals("Teclado")
                    && producto.getSkuProducto().equals("SKU-1")
                    && producto.getProveedor() == proveedor);
            producto.setIdProducto(1L);
            return producto;
        });

        service.guardarProducto(nuevo);

        assertTrue(entityReadyToInsert.get());
        verify(productoRepository).save(any(Producto.class));
    }
}
