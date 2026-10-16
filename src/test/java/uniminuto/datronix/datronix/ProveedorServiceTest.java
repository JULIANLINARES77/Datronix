package uniminuto.datronix.datronix;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import uniminuto.datronix.dto.ProveedorDTO;
import uniminuto.datronix.entity.Proveedor;
import uniminuto.datronix.exception.ProveedorEmailDuplicadoException;
import uniminuto.datronix.repository.ProveedorRepository;
import uniminuto.datronix.service.ProveedorService;

class ProveedorServiceTest {
    private final ProveedorRepository repository = Mockito.mock(ProveedorRepository.class);
    private final ProveedorService service = new ProveedorService(repository);

    @Test
    void updatingSupplierRejectsEmailUsedByAnotherSupplier() {
        Proveedor existente = Proveedor.builder()
                .idProveedor("900000001")
                .nombreEmpresa("Empresa Uno")
                .telefonoProveedor(3001234567L)
                .emailProveedor("uno@empresa.co")
                .build();
        ProveedorDTO cambio = ProveedorDTO.builder()
                .idProveedor("900000001")
                .nombreEmpresa("Empresa Uno")
                .telefonoProveedor(3001234567L)
                .emailProveedor("DOS@EMPRESA.CO")
                .build();

        when(repository.findById("900000001")).thenReturn(Optional.of(existente));
        when(repository.existsByNombreEmpresaIgnoreCaseAndIdProveedorNot("Empresa Uno", "900000001"))
                .thenReturn(false);
        when(repository.existsByTelefonoProveedorAndIdProveedorNot(3001234567L, "900000001"))
                .thenReturn(false);
        when(repository.existsByEmailProveedorIgnoreCaseAndIdProveedorNot("dos@empresa.co", "900000001"))
                .thenReturn(true);

        assertThrows(
                ProveedorEmailDuplicadoException.class,
                () -> service.actualizarProveedor("900000001", cambio));
        verify(repository, never()).save(any(Proveedor.class));
    }
}
