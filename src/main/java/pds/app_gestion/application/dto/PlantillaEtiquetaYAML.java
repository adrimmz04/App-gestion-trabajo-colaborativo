package pds.app_gestion.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * DTO para representar una etiqueta en una plantilla YAML.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantillaEtiquetaYAML implements Serializable {
    
    /**
     * Nombre de la etiqueta
     */
    private String nombre;
    
    /**
     * Color hexadecimal de la etiqueta
     */
    private String color;
}
