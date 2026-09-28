package uniminuto.datronix.exception;

public class UsuarioCorreoDuplicadoException extends RuntimeException {

    public UsuarioCorreoDuplicadoException(String correo) {

        super("Ya existe un usuario con el correo " + correo);

    }

}

