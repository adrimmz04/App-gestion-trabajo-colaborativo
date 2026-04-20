package pds.app_gestion.application.exception;

/**
 * Excepción lanzada cuando un recurso no es encontrado.
 */
public class RecursoNoEncontradoException extends AppException {
    public RecursoNoEncontradoException(String tipo, String id) {
        super(String.format("%s no encontrado: %s", tipo, id));
    }
}
