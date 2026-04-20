package pds.app_gestion.application.exception;

/**
 * Excepción lanzada cuando no hay permisos para realizar una acción.
 */
public class PermisoNegadoException extends AppException {
    public PermisoNegadoException(String usuario, String recurso) {
        super(String.format("Usuario %s no tiene permisos para acceder a %s", usuario, recurso));
    }
}
