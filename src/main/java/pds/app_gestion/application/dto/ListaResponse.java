package pds.app_gestion.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListaResponse {
    private String id;
    private String nombre;
    private int totalTarjetas;
    private int tarjetasCompletadas;
    private Integer limiteMaximo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private List<TarjetaResponse> tarjetas;
}
