package pds.app_gestion.ui.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pds.app_gestion.application.dto.*;
import pds.app_gestion.application.exception.ErrorValidacionException;
import pds.app_gestion.application.service.ServicioAutenticacion;
import pds.app_gestion.application.service.ServicioLista;
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
    private final ServicioLista servicioLista;
    private final ServicioAutenticacion servicioAutenticacion;

    public ControladorTarjeta(ServicioTarjeta servicioTarjeta, ServicioLista servicioLista,
                              ServicioAutenticacion servicioAutenticacion) {
        this.servicioTarjeta = servicioTarjeta;
        this.servicioLista = servicioLista;
        this.servicioAutenticacion = servicioAutenticacion;
    }

    /**
     * POST /api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas
     * Crear una nueva tarjeta en una lista.
     */
    @PostMapping
    public ResponseEntity<TarjetaResponse> crearTarjeta(
            @PathVariable String idTablero,
            @PathVariable String idLista,
            @RequestParam(required = false) String emailUsuario,
            @RequestParam(required = false) String codigoAcceso,
            @RequestBody CrearTarjetaRequest request) {
        TarjetaResponse response = servicioTarjeta.crearTarjeta(
            idTablero,
            idLista,
            resolverEmail(emailUsuario, codigoAcceso),
            request
        );
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
            @RequestParam(required = false) String emailUsuario,
            @RequestParam(required = false) String codigoAcceso,
            @RequestBody ActualizarTarjetaRequest request) {
        TarjetaResponse response = servicioTarjeta.actualizarTarjeta(
            idTablero,
            idLista,
            idTarjeta,
            resolverEmail(emailUsuario, codigoAcceso),
            request
        );
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{idTarjeta}
     * Eliminar una tarjeta.
     */
    @DeleteMapping("/{idTarjeta}")
    public ResponseEntity<Void> eliminarTarjeta(
            @PathVariable String idTablero,
            @PathVariable String idLista,
            @PathVariable String idTarjeta,
            @RequestParam(required = false) String emailUsuario,
            @RequestParam(required = false) String codigoAcceso) {
        servicioTarjeta.eliminarTarjeta(idTablero, idLista, idTarjeta, resolverEmail(emailUsuario, codigoAcceso));
        return ResponseEntity.noContent().build();
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
            @RequestParam(required = false) String emailUsuario,
            @RequestParam(required = false) String codigoAcceso) {
        TarjetaResponse response = servicioTarjeta.marcarComoCompletada(
            idTablero,
            idLista,
            idTarjeta,
            resolverEmail(emailUsuario, codigoAcceso)
        );
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
            @RequestParam(required = false) String emailUsuario,
            @RequestParam(required = false) String codigoAcceso) {
        TarjetaResponse response = servicioTarjeta.marcarComoNoCompletada(
            idTablero,
            idLista,
            idTarjeta,
            resolverEmail(emailUsuario, codigoAcceso)
        );
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
            @RequestParam(required = false) String emailUsuario,
            @RequestParam(required = false) String codigoAcceso,
            @RequestBody CrearEtiquetaRequest request) {
        TarjetaResponse response = servicioTarjeta.agregarEtiqueta(
            idTablero,
            idLista,
            idTarjeta,
            resolverEmail(emailUsuario, codigoAcceso),
            request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{idTarjeta}/mover
     * Mover una tarjeta a otra lista del tablero.
     */
    @PostMapping("/{idTarjeta}/mover")
    public ResponseEntity<Void> moverTarjeta(
            @PathVariable String idTablero,
            @PathVariable String idLista,
            @PathVariable String idTarjeta,
            @RequestParam String idListaDestino,
            @RequestParam(required = false) String emailUsuario,
            @RequestParam(required = false) String codigoAcceso) {
        servicioLista.moverTarjeta(
            idTablero,
            idLista,
            idListaDestino,
            idTarjeta,
            resolverEmail(emailUsuario, codigoAcceso)
        );
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{idTarjeta}/permisos
     * Obtener permisos explícitos de una tarjeta.
     */
    @GetMapping("/{idTarjeta}/permisos")
    public ResponseEntity<PermisosTarjetaResponse> obtenerPermisosTarjeta(
            @PathVariable String idTablero,
            @PathVariable String idLista,
            @PathVariable String idTarjeta,
            @RequestParam(required = false) String emailUsuario,
            @RequestParam(required = false) String codigoAcceso) {
        PermisosTarjetaResponse response = servicioTarjeta.obtenerPermisosTarjeta(
            idTablero,
            idLista,
            idTarjeta,
            resolverEmail(emailUsuario, codigoAcceso)
        );
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{idTarjeta}/permisos
     * Configurar permisos explícitos sobre una tarjeta.
     */
    @PutMapping("/{idTarjeta}/permisos")
    public ResponseEntity<PermisosTarjetaResponse> configurarPermisoTarjeta(
            @PathVariable String idTablero,
            @PathVariable String idLista,
            @PathVariable String idTarjeta,
            @RequestParam(required = false) String emailUsuario,
            @RequestParam(required = false) String codigoAcceso,
            @RequestBody ConfigurarPermisoTarjetaRequest request) {
        PermisosTarjetaResponse response = servicioTarjeta.configurarPermisoTarjeta(
            idTablero,
            idLista,
            idTarjeta,
            resolverEmail(emailUsuario, codigoAcceso),
            request
        );
        return ResponseEntity.ok(response);
    }

    private String resolverEmail(String emailExplicito, String codigoAcceso) {
        if (codigoAcceso != null && !codigoAcceso.isBlank()) {
            return servicioAutenticacion.resolverEmailDesdeCodigo(codigoAcceso);
        }

        if (emailExplicito != null && !emailExplicito.isBlank()) {
            return emailExplicito.trim();
        }

        throw new ErrorValidacionException("Debes indicar emailUsuario o codigoAcceso");
    }
}
