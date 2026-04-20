package pds.app_gestion.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarjetaResponse {
    private String id;
    private String titulo;
    private String descripcion;
    private boolean completada;
    private String tipo;
    private Set<EtiquetaResponse> etiquetas;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private LocalDateTime fechaCompletacion;
}
