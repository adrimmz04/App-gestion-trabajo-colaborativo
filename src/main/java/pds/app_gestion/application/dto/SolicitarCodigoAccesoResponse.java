package pds.app_gestion.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitarCodigoAccesoResponse {
    private String email;
    private LocalDateTime expiraEn;
    private String modoEntrega;
    private String mensaje;
    private String codigoDesarrollo;
}