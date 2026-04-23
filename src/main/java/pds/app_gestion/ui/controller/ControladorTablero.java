package pds.app_gestion.ui.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pds.app_gestion.application.dto.*;
import pds.app_gestion.application.service.ServicioLista;
import pds.app_gestion.application.service.ServicioTablero;

import java.util.List;

/**
 * Controlador REST para operaciones con tableros.
 * 
 * Expone los endpoints para:
 * - Crear, obtener, actualizar y eliminar tableros
 * - Compartir tableros
 * - Bloquear/desbloquear tableros
 * - Gestionar listas dentro de tableros
 */
@RestController
@RequestMapping("/api/v1/tableros")
public class ControladorTablero {

    private final ServicioTablero servicioTablero;
    private final ServicioLista servicioLista;

    public ControladorTablero(ServicioTablero servicioTablero, ServicioLista servicioLista) {
        this.servicioTablero = servicioTablero;
        this.servicioLista = servicioLista;
    }

    /**
     * POST /api/v1/tableros
     * Crear un nuevo tablero.
     */
    @PostMapping
    public ResponseEntity<TableroResponse> crearTablero(@RequestBody CrearTableroRequest request) {
        TableroResponse response = servicioTablero.crearTablero(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/tableros/{idTablero}
     * Obtener un tablero por ID.
     * 
     * @param idTablero ID del tablero
     * @param emailUsuario email del usuario (por query parameter)
     */
    @GetMapping("/{idTablero}")
    public ResponseEntity<TableroResponse> obtenerTablero(
            @PathVariable String idTablero,
            @RequestParam String emailUsuario) {
        TableroResponse response = servicioTablero.obtenerTablero(idTablero, emailUsuario);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/tableros/{idTablero}/historial
     * Obtener el historial de acciones del tablero.
     */
    @GetMapping("/{idTablero}/historial")
    public ResponseEntity<List<RegistroAccionResponse>> obtenerHistorialTablero(
            @PathVariable String idTablero,
            @RequestParam String emailUsuario) {
        List<RegistroAccionResponse> response = servicioTablero.obtenerHistorialTablero(idTablero, emailUsuario);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/v1/tableros/{idTablero}
     * Actualizar un tablero.
     */
    @PutMapping("/{idTablero}")
    public ResponseEntity<TableroResponse> actualizarTablero(
            @PathVariable String idTablero,
            @RequestParam String emailUsuario,
            @RequestBody ActualizarTableroRequest request) {
        TableroResponse response = servicioTablero.actualizarTablero(idTablero, emailUsuario, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/v1/tableros/{idTablero}
     * Eliminar un tablero.
     */
    @DeleteMapping("/{idTablero}")
    public ResponseEntity<Void> eliminarTablero(
            @PathVariable String idTablero,
            @RequestParam String emailUsuario) {
        servicioTablero.eliminarTablero(idTablero, emailUsuario);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/tableros/propietario/{emailPropietario}
     * Obtener tableros del propietario.
     */
    @GetMapping("/propietario/{emailPropietario}")
    public ResponseEntity<List<TableroResponse>> obtenerTablerosPropietario(
            @PathVariable String emailPropietario) {
        List<TableroResponse> response = servicioTablero.obtenerTablerosPropietario(emailPropietario);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/tableros/compartidos/{emailUsuario}
     * Obtener tableros compartidos con un usuario.
     */
    @GetMapping("/compartidos/{emailUsuario}")
    public ResponseEntity<List<TableroResponse>> obtenerTablerosCompartidos(
            @PathVariable String emailUsuario) {
        List<TableroResponse> response = servicioTablero.obtenerTablerosCompartidos(emailUsuario);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/tableros/{idTablero}/compartir
     * Compartir un tablero con otro usuario.
     */
    @PostMapping("/{idTablero}/compartir")
    public ResponseEntity<Void> compartirTablero(
            @PathVariable String idTablero,
            @RequestParam String emailPropietario,
            @RequestBody CompartirTableroRequest request) {
        servicioTablero.compartirTablero(idTablero, emailPropietario, request.getEmailUsuario());
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/v1/tableros/{idTablero}/bloquear
     * Bloquear un tablero.
     */
    @PostMapping("/{idTablero}/bloquear")
    public ResponseEntity<Void> bloquearTablero(
            @PathVariable String idTablero,
            @RequestParam String emailPropietario,
            @RequestBody BloquearTableroRequest request) {
        servicioTablero.bloquearTablero(idTablero, emailPropietario, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/v1/tableros/{idTablero}/desbloquear
     * Desbloquear un tablero.
     */
    @PostMapping("/{idTablero}/desbloquear")
    public ResponseEntity<Void> desbloquearTablero(
            @PathVariable String idTablero,
            @RequestParam String emailPropietario) {
        servicioTablero.desbloquearTablero(idTablero, emailPropietario);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/v1/tableros/{idTablero}/listas
     * Añadir una lista a un tablero.
     */
    @PostMapping("/{idTablero}/listas")
    public ResponseEntity<ListaResponse> agregarLista(
            @PathVariable String idTablero,
            @RequestParam String emailUsuario,
            @RequestBody CrearListaRequest request) {
        ListaResponse response = servicioTablero.agregarLista(idTablero, emailUsuario, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * DELETE /api/v1/tableros/{idTablero}/listas/{idLista}
     * Eliminar una lista de un tablero.
     */
    @DeleteMapping("/{idTablero}/listas/{idLista}")
    public ResponseEntity<Void> eliminarLista(
            @PathVariable String idTablero,
            @PathVariable String idLista,
            @RequestParam String emailUsuario) {
        servicioTablero.eliminarLista(idTablero, idLista, emailUsuario);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/tableros/{idTablero}/listas/{idLista}/reglas
     * Obtener las reglas configuradas de una lista.
     */
    @GetMapping("/{idTablero}/listas/{idLista}/reglas")
    public ResponseEntity<ReglasListaResponse> obtenerReglasLista(
            @PathVariable String idTablero,
            @PathVariable String idLista,
            @RequestParam String emailUsuario) {
        ReglasListaResponse response = servicioLista.obtenerReglas(idTablero, idLista, emailUsuario);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/tableros/{idTablero}/listas/{idLista}/reglas
     * Configurar reglas para una lista.
     */
    @PostMapping("/{idTablero}/listas/{idLista}/reglas")
    public ResponseEntity<ReglasListaResponse> configurarReglasLista(
            @PathVariable String idTablero,
            @PathVariable String idLista,
            @RequestParam String emailUsuario,
            @RequestBody ConfigurarReglasListaRequest request) {
        ReglasListaResponse response = servicioLista.configurarReglas(idTablero, idLista, emailUsuario, request);
        return ResponseEntity.ok(response);
    }
}
