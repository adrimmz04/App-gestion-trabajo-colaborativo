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
public class TableroResponse {
    private String id;
    private String titulo;
    private String descripcion;
    private String propietarioEmail;
    private boolean bloqueado;
    private LocalDateTime fechaDesbloqueo;
    private int totalTarjetas;
    private int tarjetasCompletadas;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Set<String> usuariosCompartidos;
}
