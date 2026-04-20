package pds.app_gestion.application.service;

import org.springframework.stereotype.Service;
import pds.app_gestion.application.dto.ActualizarTarjetaRequest;
import pds.app_gestion.application.dto.CrearTarjetaRequest;
import pds.app_gestion.application.dto.CrearEtiquetaRequest;
import pds.app_gestion.application.dto.TarjetaResponse;
import pds.app_gestion.application.dto.EtiquetaResponse;
import pds.app_gestion.application.exception.ErrorOperacionDominioException;
import pds.app_gestion.application.exception.ErrorValidacionException;
import pds.app_gestion.application.exception.PermisoNegadoException;
import pds.app_gestion.application.exception.RecursoNoEncontradoException;
import pds.app_gestion.domain.*;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para gestionar tarjetas.
 * 
 * Implementa los casos de uso relacionados con tarjetas:
 * - Crear tarjeta
 * - Actualizar tarjeta
 * - Marcar tarjeta como completada
 * - Añadir etiqueta a tarjeta
 * - Eliminar etiqueta de tarjeta
 */
@Service
public class ServicioTarjeta {

    private final RepositorioTablero repositorioTablero;

    public ServicioTarjeta(RepositorioTablero repositorioTablero) {
        this.repositorioTablero = repositorioTablero;
    }

    /**
     * Caso de uso: Crear una nueva tarjeta en una lista.
     * 
     * @param idTablero ID del tablero
     * @param idLista ID de la lista
     * @param emailUsuario email del usuario que solicita
     * @param request datos de la tarjeta
     * @return DTO de la tarjeta creada
     */
    public TarjetaResponse crearTarjeta(String idTablero, String idLista, String emailUsuario, CrearTarjetaRequest request) {
        validarCrearTarjeta(request);
        
        Tablero tablero = obtenerTablero(idTablero, emailUsuario);
        Lista lista = obtenerLista(tablero, idLista);
        
        if (tablero.isBloqueado()) {
            throw new ErrorOperacionDominioException("No se pueden crear tarjetas en un tablero bloqueado");
        }
        
        try {
            String idTarjeta = UUID.randomUUID().toString();
            Tarjeta.TipoTarjeta tipo = request.getTipo() != null && request.getTipo().equals("CHECKLIST") 
                ? Tarjeta.TipoTarjeta.CHECKLIST 
                : Tarjeta.TipoTarjeta.TAREA;
            
            Tarjeta tarjeta = new Tarjeta(idTarjeta, request.getTitulo(), request.getDescripcion(), tipo);
            lista.agregarTarjeta(tarjeta);
            
            repositorioTablero.guardar(tablero);
            return convertirATarjetaResponse(tarjeta);
        } catch (IllegalStateException e) {
            throw new ErrorOperacionDominioException(e.getMessage());
        }
    }

    /**
     * Caso de uso: Actualizar una tarjeta.
     * 
     * @param idTablero ID del tablero
     * @param idLista ID de la lista
     * @param idTarjeta ID de la tarjeta
     * @param emailUsuario email del usuario que solicita
     * @param request datos a actualizar
     * @return DTO de la tarjeta actualizada
     */
    public TarjetaResponse actualizarTarjeta(String idTablero, String idLista, String idTarjeta, 
                                            String emailUsuario, ActualizarTarjetaRequest request) {
        Tablero tablero = obtenerTablero(idTablero, emailUsuario);
        Lista lista = obtenerLista(tablero, idLista);
        Tarjeta tarjeta = obtenerTarjeta(lista, idTarjeta);
        
        if (request.getDescripcion() != null) {
            tarjeta.actualizarDescripcion(request.getDescripcion());
        }
        
        repositorioTablero.guardar(tablero);
        return convertirATarjetaResponse(tarjeta);
    }

    /**
     * Caso de uso: Marcar una tarjeta como completada.
     * 
     * @param idTablero ID del tablero
     * @param idLista ID de la lista
     * @param idTarjeta ID de la tarjeta
     * @param emailUsuario email del usuario que solicita
     * @return DTO de la tarjeta actualizada
     */
    public TarjetaResponse marcarComoCompletada(String idTablero, String idLista, String idTarjeta, String emailUsuario) {
        Tablero tablero = obtenerTablero(idTablero, emailUsuario);
        Lista lista = obtenerLista(tablero, idLista);
        Tarjeta tarjeta = obtenerTarjeta(lista, idTarjeta);
        
        tarjeta.marcarComoCompletada();
        repositorioTablero.guardar(tablero);
        
        return convertirATarjetaResponse(tarjeta);
    }

    /**
     * Caso de uso: Marcar una tarjeta como no completada.
     * 
     * @param idTablero ID del tablero
     * @param idLista ID de la lista
     * @param idTarjeta ID de la tarjeta
     * @param emailUsuario email del usuario que solicita
     * @return DTO de la tarjeta actualizada
     */
    public TarjetaResponse marcarComoNoCompletada(String idTablero, String idLista, String idTarjeta, String emailUsuario) {
        Tablero tablero = obtenerTablero(idTablero, emailUsuario);
        Lista lista = obtenerLista(tablero, idLista);
        Tarjeta tarjeta = obtenerTarjeta(lista, idTarjeta);
        
        tarjeta.marcarComoNoCompletada();
        repositorioTablero.guardar(tablero);
        
        return convertirATarjetaResponse(tarjeta);
    }

    /**
     * Caso de uso: Añadir una etiqueta a una tarjeta.
     * 
     * @param idTablero ID del tablero
     * @param idLista ID de la lista
     * @param idTarjeta ID de la tarjeta
     * @param emailUsuario email del usuario que solicita
     * @param request datos de la etiqueta
     * @return DTO de la tarjeta actualizada
     */
    public TarjetaResponse agregarEtiqueta(String idTablero, String idLista, String idTarjeta, 
                                          String emailUsuario, CrearEtiquetaRequest request) {
        Tablero tablero = obtenerTablero(idTablero, emailUsuario);
        Lista lista = obtenerLista(tablero, idLista);
        Tarjeta tarjeta = obtenerTarjeta(lista, idTarjeta);
        
        try {
            Etiqueta etiqueta = new Etiqueta(request.getNombre(), request.getColor());
            tarjeta.agregarEtiqueta(etiqueta);
            repositorioTablero.guardar(tablero);
            return convertirATarjetaResponse(tarjeta);
        } catch (IllegalArgumentException e) {
            throw new ErrorValidacionException(e.getMessage());
        }
    }

    /**
     * Convierte una Tarjeta de dominio a TarjetaResponse.
     */
    private TarjetaResponse convertirATarjetaResponse(Tarjeta tarjeta) {
        return TarjetaResponse.builder()
            .id(tarjeta.getId())
            .titulo(tarjeta.getTitulo())
            .descripcion(tarjeta.getDescripcion())
            .completada(tarjeta.isCompletada())
            .tipo(tarjeta.getTipo().name())
            .etiquetas(tarjeta.getEtiquetas().stream()
                .map(e -> EtiquetaResponse.builder()
                    .nombre(e.getNombre())
                    .color(e.getColor())
                    .build())
                .collect(Collectors.toSet()))
            .fechaCreacion(tarjeta.getFechaCreacion())
            .fechaActualizacion(tarjeta.getFechaActualizacion())
            .fechaCompletacion(tarjeta.getFechaCompletacion())
            .build();
    }

    /**
     * Obtiene un tablero verificando que el usuario tenga acceso.
     */
    private Tablero obtenerTablero(String idTablero, String emailUsuario) {
        Tablero tablero = repositorioTablero.obtenerPorId(idTablero)
            .orElseThrow(() -> new RecursoNoEncontradoException("Tablero", idTablero));
        
        if (!tablero.tieneAcceso(emailUsuario)) {
            throw new PermisoNegadoException(emailUsuario, "tablero");
        }
        
        return tablero;
    }

    /**
     * Obtiene una lista del tablero.
     */
    private Lista obtenerLista(Tablero tablero, String idLista) {
        return tablero.obtenerLista(idLista)
            .orElseThrow(() -> new RecursoNoEncontradoException("Lista", idLista));
    }

    /**
     * Obtiene una tarjeta de la lista.
     */
    private Tarjeta obtenerTarjeta(Lista lista, String idTarjeta) {
        return lista.obtenerTarjeta(idTarjeta)
            .orElseThrow(() -> new RecursoNoEncontradoException("Tarjeta", idTarjeta));
    }

    /**
     * Valida los datos para crear una tarjeta.
     */
    private void validarCrearTarjeta(CrearTarjetaRequest request) {
        if (request.getTitulo() == null || request.getTitulo().trim().isEmpty()) {
            throw new ErrorValidacionException("El título de la tarjeta no puede estar vacío");
        }
    }
}
