package pds.app_gestion.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import java.time.LocalDateTime;

/**
 * Entidad que representa un registro de acción en el historial del tablero.
 * 
 * Mantiene un registro de todas las acciones realizadas en el tablero para
 * propósitos de auditoría y seguimiento de cambios.
 */
@Getter
@ToString
@AllArgsConstructor
public class RegistroAccion {
    private final String tipo;
    private final String detalles;
    private final LocalDateTime fecha;

    /**
     * Obtiene una descripción legible del registro.
     */
    public String obtenerDescripcion() {
        return String.format("[%s] %s - %s", 
            tipo, 
            detalles, 
            fecha.toString());
    }
}
