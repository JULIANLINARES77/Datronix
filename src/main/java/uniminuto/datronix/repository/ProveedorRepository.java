package uniminuto.datronix.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import uniminuto.datronix.entity.Proveedor;

// Spring Data crea las operaciones básicas para proveedores.
public interface ProveedorRepository extends JpaRepository<Proveedor, String> {
    boolean existsByNombreEmpresaIgnoreCase(String nombreEmpresa);
    boolean existsByNombreEmpresaIgnoreCaseAndIdProveedorNot(String nombreEmpresa, String idProveedor);
    boolean existsByTelefonoProveedor(Long telefonoProveedor);
    boolean existsByTelefonoProveedorAndIdProveedorNot(Long telefonoProveedor, String idProveedor);
    boolean existsByEmailProveedorIgnoreCase(String emailProveedor);
    boolean existsByEmailProveedorIgnoreCaseAndIdProveedorNot(String emailProveedor, String idProveedor);
}
