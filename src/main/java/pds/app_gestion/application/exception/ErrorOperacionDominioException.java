package pds.app_gestion.application.exception;

/**
 * Excepción lanzada cuando falla una operación de dominio.
 */
public class ErrorOperacionDominioException extends AppException {
    public ErrorOperacionDominioException(String mensaje) {
        super("Error en operación de dominio: " + mensaje);
    }
}
