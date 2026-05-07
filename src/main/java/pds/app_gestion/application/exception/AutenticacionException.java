package pds.app_gestion.application.exception;

public class AutenticacionException extends AppException {
    public AutenticacionException(String mensaje) {
        super("Error de autenticación: " + mensaje);
    }

    public AutenticacionException(String mensaje, Throwable causa) {
        super("Error de autenticación: " + mensaje, causa);
    }
}