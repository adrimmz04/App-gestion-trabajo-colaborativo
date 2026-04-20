package pds.app_gestion.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

/**
 * DTO para representar una lista en una plantilla YAML.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantillaListaYAML implements Serializable {
    
    /**
     * Nombre de la lista
     */
    private String nombre;
    
    /**
     * Límite máximo de tarjetas en la lista
     */
    private Integer limiteMaximo;
    
    /**
     * IDs de listas previas (prerequisitos)
     */
    private List<String> listasPrevias;
    
    /**
     * Tarjetas de la lista
     */
    private List<PlantillaTarjetaYAML> tarjetas;
}
