package uniminuto.datronix.exception;

public class UsuarioIdDuplicadoException extends RuntimeException {

    public UsuarioIdDuplicadoException(String id) {

        super("Ya existe un usuario con el documento " + id);

    }

}
