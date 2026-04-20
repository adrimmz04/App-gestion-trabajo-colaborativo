package pds.app_gestion.application.exception;

/**
 * Excepción base para excepciones de aplicación.
 */
public class AppException extends RuntimeException {
    public AppException(String mensaje) {
        super(mensaje);
    }

    public AppException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
