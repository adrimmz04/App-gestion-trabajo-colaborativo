package pds.app_gestion.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO para configurar reglas en una lista.
 * 
 * Permite especificar:
 * - Límite máximo de items
 * - Listas previas que deben completarse antes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigurarReglasListaRequest {
    
    /**
     * Límite máximo de tarjetas en la lista (null sin límite)
     */
    private Integer limiteMaximo;
    
    /**
     * IDs de listas que deben completarse antes de mover tarjetas a esta lista
     */
    private List<String> listasPrevias;
}
