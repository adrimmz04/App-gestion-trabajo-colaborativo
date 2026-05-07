package pds.app_gestion.domain;

/**
 * Permisos explícitos que el propietario puede asignar sobre una tarjeta.
 */
public enum PermisoTarjeta {
    LECTURA,
    ESCRITURA;

    public boolean permiteLectura() {
        return true;
    }

    public boolean permiteEscritura() {
        return this == ESCRITURA;
    }
}