package uniminuto.datronix.exception;

public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException(){

        super("Correo o contraseña incorrectas");
    }

}
