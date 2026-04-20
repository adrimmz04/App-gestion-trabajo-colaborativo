package pds.app_gestion.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

/**
 * DTO para representar una tarjeta en una plantilla YAML.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantillaTarjetaYAML implements Serializable {
    
    /**
     * Título de la tarjeta
     */
    private String titulo;
    
    /**
     * Descripción de la tarjeta
     */
    private String descripcion;
    
    /**
     * Tipo de tarjeta (TAREA o CHECKLIST)
     */
    private String tipo;
    
    /**
     * Etiquetas de la tarjeta
     */
    private List<PlantillaEtiquetaYAML> etiquetas;
    
    /**
     * Indica si la tarjeta comienza completada
     */
    private Boolean completada;
}
