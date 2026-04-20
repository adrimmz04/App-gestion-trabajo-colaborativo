package pds.app_gestion.application.exception;

/**
 * Excepción lanzada cuando ocurre un error de validación.
 */
public class ErrorValidacionException extends AppException {
    public ErrorValidacionException(String mensaje) {
        super("Error de validación: " + mensaje);
    }
}
