package pds.app_gestion.ui.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pds.app_gestion.application.dto.*;
import pds.app_gestion.application.service.ServicioTarjeta;

/**
 * Controlador REST para operaciones con tarjetas.
 * 
 * Expone los endpoints para:
 * - Crear, actualizar tarjetas
 * - Marcar tarjetas como completadas
 * - Gestionar etiquetas en tarjetas
 */
@RestController
@RequestMapping("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas")
public class ControladorTarjeta {

    private final ServicioTarjeta servicioTarjeta;

    public ControladorTarjeta(ServicioTarjeta servicioTarjeta) {
        this.servicioTarjeta = servicioTarjeta;
    }

    /**
     * POST /api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas
     * Crear una nueva tarjeta en una lista.
     */
    @PostMapping
    public ResponseEntity<TarjetaResponse> crearTarjeta(
            @PathVariable String idTablero,
            @PathVariable String idLista,
            @RequestParam String emailUsuario,
            @RequestBody CrearTarjetaRequest request) {
        TarjetaResponse response = servicioTarjeta.crearTarjeta(idTablero, idLista, emailUsuario, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{idTarjeta}
     * Actualizar una tarjeta.
     */
    @PutMapping("/{idTarjeta}")
    public ResponseEntity<TarjetaResponse> actualizarTarjeta(
            @PathVariable String idTablero,
            @PathVariable String idLista,
            @PathVariable String idTarjeta,
            @RequestParam String emailUsuario,
            @RequestBody ActualizarTarjetaRequest request) {
        TarjetaResponse response = servicioTarjeta.actualizarTarjeta(idTablero, idLista, idTarjeta, emailUsuario, request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{idTarjeta}/completar
     * Marcar una tarjeta como completada.
     */
    @PostMapping("/{idTarjeta}/completar")
    public ResponseEntity<TarjetaResponse> marcarComoCompletada(
            @PathVariable String idTablero,
            @PathVariable String idLista,
            @PathVariable String idTarjeta,
            @RequestParam String emailUsuario) {
        TarjetaResponse response = servicioTarjeta.marcarComoCompletada(idTablero, idLista, idTarjeta, emailUsuario);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{idTarjeta}/descompletar
     * Marcar una tarjeta como no completada.
     */
    @PostMapping("/{idTarjeta}/descompletar")
    public ResponseEntity<TarjetaResponse> marcarComoNoCompletada(
            @PathVariable String idTablero,
            @PathVariable String idLista,
            @PathVariable String idTarjeta,
            @RequestParam String emailUsuario) {
        TarjetaResponse response = servicioTarjeta.marcarComoNoCompletada(idTablero, idLista, idTarjeta, emailUsuario);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{idTarjeta}/etiquetas
     * Añadir una etiqueta a una tarjeta.
     */
    @PostMapping("/{idTarjeta}/etiquetas")
    public ResponseEntity<TarjetaResponse> agregarEtiqueta(
            @PathVariable String idTablero,
            @PathVariable String idLista,
            @PathVariable String idTarjeta,
            @RequestParam String emailUsuario,
            @RequestBody CrearEtiquetaRequest request) {
        TarjetaResponse response = servicioTarjeta.agregarEtiqueta(idTablero, idLista, idTarjeta, emailUsuario, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
