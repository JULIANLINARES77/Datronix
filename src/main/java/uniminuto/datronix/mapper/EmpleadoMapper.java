package uniminuto.datronix.mapper;

import uniminuto.datronix.dto.EmpleadoDTO;
import uniminuto.datronix.entity.Empleado;

public final class EmpleadoMapper {
    private EmpleadoMapper() {
    }

    public static EmpleadoDTO toDto(Empleado empleado) {
        EmpleadoDTO dto = new EmpleadoDTO();
        dto.setIdEmpleado(empleado.getIdEmpleado());
        dto.setNombreEmpleado(empleado.getNombreEmpleado());
        dto.setCargo(empleado.getCargo());
        dto.setCorreoEmpleado(empleado.getCorreoEmpleado());
        dto.setTelefonoEmpleado(empleado.getTelefonoEmpleado());
        dto.setFechaIngreso(empleado.getFechaIngreso());
        dto.setSalarioBase(empleado.getSalarioBase());
        return dto;
    }

    public static Empleado toEntity(EmpleadoDTO dto) {
        return Empleado.builder()
                .idEmpleado(dto.getIdEmpleado().trim())
                .nombreEmpleado(dto.getNombreEmpleado().trim())
                .cargo(dto.getCargo().trim())
                .correoEmpleado(dto.getCorreoEmpleado() == null || dto.getCorreoEmpleado().isBlank()
                        ? null : dto.getCorreoEmpleado().trim())
                .telefonoEmpleado(dto.getTelefonoEmpleado() == null || dto.getTelefonoEmpleado().isBlank()
                        ? null : dto.getTelefonoEmpleado().trim())
                .fechaIngreso(dto.getFechaIngreso())
                .salarioBase(dto.getSalarioBase())
                .build();
    }

    public static void updateEntity(Empleado empleado, EmpleadoDTO dto) {
        empleado.setNombreEmpleado(dto.getNombreEmpleado().trim());
        empleado.setCargo(dto.getCargo().trim());
        empleado.setCorreoEmpleado(dto.getCorreoEmpleado() == null || dto.getCorreoEmpleado().isBlank()
                ? null : dto.getCorreoEmpleado().trim());
        empleado.setTelefonoEmpleado(dto.getTelefonoEmpleado() == null || dto.getTelefonoEmpleado().isBlank()
                ? null : dto.getTelefonoEmpleado().trim());
        empleado.setFechaIngreso(dto.getFechaIngreso());
        empleado.setSalarioBase(dto.getSalarioBase());
    }
}
