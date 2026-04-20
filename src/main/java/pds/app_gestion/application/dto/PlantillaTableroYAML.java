package pds.app_gestion.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

/**
 * DTO para representar una plantilla de tablero en formato YAML.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantillaTableroYAML implements Serializable {
    
    /**
     * Nombre/título de la plantilla
     */
    private String titulo;
    
    /**
     * Descripción de la plantilla
     */
    private String descripcion;
    
    /**
     * Listas de la plantilla
     */
    private List<PlantillaListaYAML> listas;
    
    /**
     * Versión de la plantilla
     */
    private String version;
}
