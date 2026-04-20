package pds.app_gestion.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO para obtener las reglas de una lista.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReglasListaResponse {
    
    /**
     * Nombre de la lista
     */
    private String nombre;
    
    /**
     * Límite máximo de tarjetas (null si no hay límite)
     */
    private Integer limiteMaximo;
    
    /**
     * Cantidad actual de tarjetas
     */
    private Integer cantidadActual;
    
    /**
     * IDs de listas que deben completarse antes
     */
    private List<String> listasPrevias;
}
