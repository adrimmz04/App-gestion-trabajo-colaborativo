package pds.app_gestion.ui.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pds.app_gestion.application.dto.*;
import pds.app_gestion.application.exception.ErrorValidacionException;
import pds.app_gestion.application.service.ServicioAutenticacion;
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
    private final ServicioAutenticacion servicioAutenticacion;

    public ControladorTablero(ServicioTablero servicioTablero, ServicioLista servicioLista,
                              ServicioAutenticacion servicioAutenticacion) {
        this.servicioTablero = servicioTablero;
        this.servicioLista = servicioLista;
        this.servicioAutenticacion = servicioAutenticacion;
    }

    /**
     * POST /api/v1/tableros
     * Crear un nuevo tablero.
     */
    @PostMapping
    public ResponseEntity<TableroResponse> crearTablero(
            @RequestParam(required = false) String codigoAcceso,
            @RequestBody CrearTableroRequest request) {
        if (codigoAcceso != null && !codigoAcceso.isBlank()) {
            request.setPropietarioEmail(servicioAutenticacion.resolverEmailDesdeCodigo(codigoAcceso));
        }
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
            @RequestParam(required = false) String emailUsuario,
            @RequestParam(required = false) String codigoAcceso) {
        TableroResponse response = servicioTablero.obtenerTablero(idTablero, resolverEmail(emailUsuario, codigoAcceso, "emailUsuario"));
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/tableros/{idTablero}/historial
     * Obtener el historial de acciones del tablero.
     */
    @GetMapping("/{idTablero}/historial")
    public ResponseEntity<List<RegistroAccionResponse>> obtenerHistorialTablero(
            @PathVariable String idTablero,
            @RequestParam(required = false) String emailUsuario,
            @RequestParam(required = false) String codigoAcceso) {
        List<RegistroAccionResponse> response = servicioTablero.obtenerHistorialTablero(
            idTablero,
            resolverEmail(emailUsuario, codigoAcceso, "emailUsuario")
        );
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/v1/tableros/{idTablero}
     * Actualizar un tablero.
     */
    @PutMapping("/{idTablero}")
    public ResponseEntity<TableroResponse> actualizarTablero(
            @PathVariable String idTablero,
            @RequestParam(required = false) String emailUsuario,
            @RequestParam(required = false) String codigoAcceso,
            @RequestBody ActualizarTableroRequest request) {
        TableroResponse response = servicioTablero.actualizarTablero(
            idTablero,
            resolverEmail(emailUsuario, codigoAcceso, "emailUsuario"),
            request
        );
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/v1/tableros/{idTablero}
     * Eliminar un tablero.
     */
    @DeleteMapping("/{idTablero}")
    public ResponseEntity<Void> eliminarTablero(
            @PathVariable String idTablero,
            @RequestParam(required = false) String emailUsuario,
            @RequestParam(required = false) String codigoAcceso) {
        servicioTablero.eliminarTablero(idTablero, resolverEmail(emailUsuario, codigoAcceso, "emailUsuario"));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/propietario")
    public ResponseEntity<List<TableroResponse>> obtenerTablerosPropietarioAutenticado(
            @RequestParam String codigoAcceso) {
        String emailUsuario = servicioAutenticacion.resolverEmailDesdeCodigo(codigoAcceso);
        return ResponseEntity.ok(servicioTablero.obtenerTablerosPropietario(emailUsuario));
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

    @GetMapping("/compartidos")
    public ResponseEntity<List<TableroResponse>> obtenerTablerosCompartidosAutenticado(
            @RequestParam String codigoAcceso) {
        String emailUsuario = servicioAutenticacion.resolverEmailDesdeCodigo(codigoAcceso);
        return ResponseEntity.ok(servicioTablero.obtenerTablerosCompartidos(emailUsuario));
    }

    /**
     * POST /api/v1/tableros/{idTablero}/compartir
     * Compartir un tablero con otro usuario.
     */
    @PostMapping("/{idTablero}/compartir")
    public ResponseEntity<Void> compartirTablero(
            @PathVariable String idTablero,
            @RequestParam(required = false) String emailPropietario,
            @RequestParam(required = false) String codigoAcceso,
            @RequestBody CompartirTableroRequest request) {
        servicioTablero.compartirTablero(
            idTablero,
            resolverEmail(emailPropietario, codigoAcceso, "emailPropietario"),
            request.getEmailUsuario()
        );
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/v1/tableros/{idTablero}/bloquear
     * Bloquear un tablero.
     */
    @PostMapping("/{idTablero}/bloquear")
    public ResponseEntity<Void> bloquearTablero(
            @PathVariable String idTablero,
            @RequestParam(required = false) String emailPropietario,
            @RequestParam(required = false) String codigoAcceso,
            @RequestBody BloquearTableroRequest request) {
        servicioTablero.bloquearTablero(
            idTablero,
            resolverEmail(emailPropietario, codigoAcceso, "emailPropietario"),
            request
        );
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/v1/tableros/{idTablero}/desbloquear
     * Desbloquear un tablero.
     */
    @PostMapping("/{idTablero}/desbloquear")
    public ResponseEntity<Void> desbloquearTablero(
            @PathVariable String idTablero,
            @RequestParam(required = false) String emailPropietario,
            @RequestParam(required = false) String codigoAcceso) {
        servicioTablero.desbloquearTablero(idTablero, resolverEmail(emailPropietario, codigoAcceso, "emailPropietario"));
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/v1/tableros/{idTablero}/listas
     * Añadir una lista a un tablero.
     */
    @PostMapping("/{idTablero}/listas")
    public ResponseEntity<ListaResponse> agregarLista(
            @PathVariable String idTablero,
            @RequestParam(required = false) String emailUsuario,
            @RequestParam(required = false) String codigoAcceso,
            @RequestBody CrearListaRequest request) {
        ListaResponse response = servicioTablero.agregarLista(
            idTablero,
            resolverEmail(emailUsuario, codigoAcceso, "emailUsuario"),
            request
        );
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
            @RequestParam(required = false) String emailUsuario,
            @RequestParam(required = false) String codigoAcceso) {
        servicioTablero.eliminarLista(idTablero, idLista, resolverEmail(emailUsuario, codigoAcceso, "emailUsuario"));
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
            @RequestParam(required = false) String emailUsuario,
            @RequestParam(required = false) String codigoAcceso) {
        ReglasListaResponse response = servicioLista.obtenerReglas(
            idTablero,
            idLista,
            resolverEmail(emailUsuario, codigoAcceso, "emailUsuario")
        );
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
            @RequestParam(required = false) String emailUsuario,
            @RequestParam(required = false) String codigoAcceso,
            @RequestBody ConfigurarReglasListaRequest request) {
        ReglasListaResponse response = servicioLista.configurarReglas(
            idTablero,
            idLista,
            resolverEmail(emailUsuario, codigoAcceso, "emailUsuario"),
            request
        );
        return ResponseEntity.ok(response);
    }

    private String resolverEmail(String emailExplicito, String codigoAcceso, String nombreParametro) {
        if (codigoAcceso != null && !codigoAcceso.isBlank()) {
            return servicioAutenticacion.resolverEmailDesdeCodigo(codigoAcceso);
        }

        if (emailExplicito != null && !emailExplicito.isBlank()) {
            return emailExplicito.trim();
        }

        throw new ErrorValidacionException("Debes indicar " + nombreParametro + " o codigoAcceso");
    }
}
