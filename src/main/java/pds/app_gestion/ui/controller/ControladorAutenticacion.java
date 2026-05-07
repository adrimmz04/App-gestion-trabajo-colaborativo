package pds.app_gestion.ui.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pds.app_gestion.application.dto.SesionAutenticadaResponse;
import pds.app_gestion.application.dto.SolicitarCodigoAccesoRequest;
import pds.app_gestion.application.dto.SolicitarCodigoAccesoResponse;
import pds.app_gestion.application.service.ServicioAutenticacion;

@RestController
@RequestMapping("/api/v1/auth")
public class ControladorAutenticacion {

    private final ServicioAutenticacion servicioAutenticacion;

    public ControladorAutenticacion(ServicioAutenticacion servicioAutenticacion) {
        this.servicioAutenticacion = servicioAutenticacion;
    }

    @PostMapping("/codigos")
    public ResponseEntity<SolicitarCodigoAccesoResponse> solicitarCodigo(@RequestBody SolicitarCodigoAccesoRequest request) {
        return ResponseEntity.ok(servicioAutenticacion.solicitarCodigo(request));
    }

    @GetMapping("/sesion")
    public ResponseEntity<SesionAutenticadaResponse> obtenerSesion(@RequestParam String codigoAcceso) {
        return ResponseEntity.ok(servicioAutenticacion.obtenerSesionActiva(codigoAcceso));
    }

    @DeleteMapping("/sesion")
    public ResponseEntity<Void> cerrarSesion(@RequestParam String codigoAcceso) {
        servicioAutenticacion.cerrarSesion(codigoAcceso);
        return ResponseEntity.noContent().build();
    }
}