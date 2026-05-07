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
public class SesionAutenticadaResponse {
    private String email;
    private String codigoAcceso;
    private LocalDateTime expiraEn;
    private boolean autenticado;
}