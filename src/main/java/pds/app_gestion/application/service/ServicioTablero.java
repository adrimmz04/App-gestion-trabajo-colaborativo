package pds.app_gestion.application.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import pds.app_gestion.application.dto.*;
import pds.app_gestion.application.exception.ErrorOperacionDominioException;
import pds.app_gestion.application.exception.ErrorValidacionException;
import pds.app_gestion.application.exception.PermisoNegadoException;
import pds.app_gestion.application.exception.RecursoNoEncontradoException;
import pds.app_gestion.domain.*;
import pds.app_gestion.infrastructure.cache.CacheService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para gestionar tableros.
 * 
 * Implementa los casos de uso relacionados con tableros:
 * - Crear tablero
 * - Obtener tablero
 * - Actualizar tablero
 * - Eliminar tablero
 * - Compartir tablero
 * - Bloquear/desbloquear tablero
 * - Gestionar listas y tarjetas
 */
@Service
public class ServicioTablero {

    private final RepositorioTablero repositorioTablero;
    private final CacheService cacheService;

    public ServicioTablero(RepositorioTablero repositorioTablero, CacheService cacheService) {
        this.repositorioTablero = repositorioTablero;
        this.cacheService = cacheService;
    }

    /**
     * Caso de uso: Crear un nuevo tablero.
     * 
     * @param request datos para crear el tablero
     * @return DTO del tablero creado
     */
    @CacheEvict(cacheNames = {"tablerosPropietario"}, allEntries = true)
    public TableroResponse crearTablero(CrearTableroRequest request) {
        validarCrearTablero(request);
        
        String id = UUID.randomUUID().toString();
        Tablero tablero = new Tablero(id, request.getTitulo(), request.getPropietarioEmail());
        
        if (request.getDescripcion() != null && !request.getDescripcion().isEmpty()) {
            tablero.actualizarDescripcion(request.getDescripcion());
        }
        
        repositorioTablero.guardar(tablero);
        return convertirATableroResponse(tablero);
    }

    /**
     * Caso de uso: Obtener un tablero por su ID.
     * 
     * @param idTablero ID del tablero
     * @param emailUsuario email del usuario que solicita (para verificar permisos)
     * @return DTO del tablero
     */
    @Cacheable(cacheNames = "tableros", key = "#idTablero + ':' + #emailUsuario")
    public TableroResponse obtenerTablero(String idTablero, String emailUsuario) {
        Tablero tablero = repositorioTablero.obtenerPorId(idTablero)
            .orElseThrow(() -> new RecursoNoEncontradoException("Tablero", idTablero));
        
        if (!tablero.tieneAcceso(emailUsuario)) {
            throw new PermisoNegadoException(emailUsuario, "tablero " + idTablero);
        }
        
        return convertirATableroResponse(tablero);
    }

    /**
     * Caso de uso: Actualizar un tablero.
     * 
     * @param idTablero ID del tablero
     * @param emailUsuario email del usuario que solicita
     * @param request datos para actualizar
     * @return DTO del tablero actualizado
     */
    @CacheEvict(cacheNames = {"tableros", "tablerosPropietario"}, allEntries = true)
    public TableroResponse actualizarTablero(String idTablero, String emailUsuario, ActualizarTableroRequest request) {
        Tablero tablero = repositorioTablero.obtenerPorId(idTablero)
            .orElseThrow(() -> new RecursoNoEncontradoException("Tablero", idTablero));
        
        if (!tablero.getPropietarioEmail().equals(emailUsuario)) {
            throw new PermisoNegadoException(emailUsuario, "editar tablero");
        }
        
        if (request.getDescripcion() != null) {
            tablero.actualizarDescripcion(request.getDescripcion());
        }
        
        repositorioTablero.guardar(tablero);
        return convertirATableroResponse(tablero);
    }

    /**
     * Caso de uso: Obtener tableros del propietario.
     * 
     * @param emailPropietario email del propietario
     * @return lista de DTOs de tableros
     */
    @Cacheable(cacheNames = "tablerosPropietario", key = "#emailPropietario")
    public List<TableroResponse> obtenerTablerosPropietario(String emailPropietario) {
        return repositorioTablero.obtenerPorPropietario(emailPropietario)
            .stream()
            .map(this::convertirATableroResponse)
            .collect(Collectors.toList());
    }

    /**
     * Caso de uso: Obtener tableros compartidos con un usuario.
     * 
     * @param emailUsuario email del usuario
     * @return lista de DTOs de tableros compartidos
     */
    @Cacheable(cacheNames = "tablerosCompartidos", key = "#emailUsuario")
    public List<TableroResponse> obtenerTablerosCompartidos(String emailUsuario) {
        return repositorioTablero.obtenerCompartidos(emailUsuario)
            .stream()
            .map(this::convertirATableroResponse)
            .collect(Collectors.toList());
    }

    /**
     * Caso de uso: Compartir un tablero con otro usuario.
     * 
     * @param idTablero ID del tablero
     * @param emailPropietario email del propietario
     * @param emailUsuario email del usuario con el que compartir
     */
    @CacheEvict(cacheNames = {"tableros", "tablerosCompartidos"}, allEntries = true)
    public void compartirTablero(String idTablero, String emailPropietario, String emailUsuario) {
        Tablero tablero = repositorioTablero.obtenerPorId(idTablero)
            .orElseThrow(() -> new RecursoNoEncontradoException("Tablero", idTablero));
        
        if (!tablero.getPropietarioEmail().equals(emailPropietario)) {
            throw new PermisoNegadoException(emailPropietario, "compartir tablero");
        }
        
        if (!emailUsuario.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ErrorValidacionException("Email inválido: " + emailUsuario);
        }
        
        tablero.compartirCon(emailUsuario);
        repositorioTablero.guardar(tablero);
    }

    /**
     * Caso de uso: Bloquear un tablero.
     * 
     * @param idTablero ID del tablero
     * @param emailPropietario email del propietario
     * @param request datos para bloquear (duración en minutos)
     */
    @CacheEvict(cacheNames = {"tableros", "tablerosPropietario"}, allEntries = true, beforeInvocation = false)
    public void bloquearTablero(String idTablero, String emailPropietario, BloquearTableroRequest request) {
        Tablero tablero = repositorioTablero.obtenerPorId(idTablero)
            .orElseThrow(() -> new RecursoNoEncontradoException("Tablero", idTablero));
        
        if (!tablero.getPropietarioEmail().equals(emailPropietario)) {
            throw new PermisoNegadoException(emailPropietario, "bloquear tablero");
        }
        
        try {
            tablero.bloquear(request.getDuracionMinutos());
            repositorioTablero.guardar(tablero);
        } catch (IllegalArgumentException e) {
            throw new ErrorOperacionDominioException(e.getMessage());
        }
    }

    /**
     * Caso de uso: Desbloquear un tablero.
     * 
     * @param idTablero ID del tablero
     * @param emailPropietario email del propietario
     */
    @CacheEvict(cacheNames = {"tableros", "tablerosPropietario"}, allEntries = true, beforeInvocation = false)
    public void desbloquearTablero(String idTablero, String emailPropietario) {
        Tablero tablero = repositorioTablero.obtenerPorId(idTablero)
            .orElseThrow(() -> new RecursoNoEncontradoException("Tablero", idTablero));
        
        if (!tablero.getPropietarioEmail().equals(emailPropietario)) {
            throw new PermisoNegadoException(emailPropietario, "desbloquear tablero");
        }
        
        tablero.desbloquear();
        repositorioTablero.guardar(tablero);
    }

    /**
     * Caso de uso: Añadir una lista a un tablero.
     * 
     * @param idTablero ID del tablero
     * @param emailUsuario email del usuario que solicita
     * @param request datos de la lista
     * @return DTO de la lista creada
     */
    @CacheEvict(cacheNames = "tableros", allEntries = true)
    public ListaResponse agregarLista(String idTablero, String emailUsuario, CrearListaRequest request) {
        Tablero tablero = repositorioTablero.obtenerPorId(idTablero)
            .orElseThrow(() -> new RecursoNoEncontradoException("Tablero", idTablero));
        
        if (!tablero.tieneAcceso(emailUsuario)) {
            throw new PermisoNegadoException(emailUsuario, "añadir lista");
        }
        
        if (tablero.isBloqueado()) {
            throw new ErrorOperacionDominioException("No se pueden añadir listas a un tablero bloqueado");
        }
        
        try {
            String idLista = UUID.randomUUID().toString();
            Lista lista = new Lista(idLista, request.getNombre());
            
            if (request.getLimiteMaximo() != null) {
                lista.establecerLimiteMaximo(request.getLimiteMaximo());
            }
            
            tablero.agregarLista(lista);
            repositorioTablero.guardar(tablero);
            
            return convertirAListaResponse(lista);
        } catch (IllegalArgumentException e) {
            throw new ErrorOperacionDominioException(e.getMessage());
        }
    }

    /**
     * Convierte un Tablero de dominio a TableroResponse.
     */
    private TableroResponse convertirATableroResponse(Tablero tablero) {
        return TableroResponse.builder()
            .id(tablero.getId())
            .titulo(tablero.getTitulo())
            .descripcion(tablero.getDescripcion())
            .propietarioEmail(tablero.getPropietarioEmail())
            .bloqueado(tablero.isBloqueado())
            .fechaDesbloqueo(tablero.getFechaDesbloqueo().orElse(null))
            .totalTarjetas(tablero.obtenerTotalTarjetas())
            .tarjetasCompletadas(tablero.obtenerTarjetasCompletadas().size())
            .fechaCreacion(tablero.getFechaCreacion())
            .fechaActualizacion(tablero.getFechaActualizacion())
            .usuariosCompartidos(tablero.getUsuariosCompartidos())
            .build();
    }

    /**
     * Convierte una Lista de dominio a ListaResponse.
     */
    private ListaResponse convertirAListaResponse(Lista lista) {
        return ListaResponse.builder()
            .id(lista.getId())
            .nombre(lista.getNombre())
            .totalTarjetas(lista.obtenerCantidadTarjetas())
            .tarjetasCompletadas(lista.obtenerTarjetasCompletadas().size())
            .limiteMaximo(lista.getLimiteMaximo().orElse(null))
            .fechaCreacion(lista.getFechaCreacion())
            .fechaActualizacion(lista.getFechaActualizacion())
            .tarjetas(lista.getTarjetas().stream()
                .map(this::convertirATarjetaResponse)
                .collect(Collectors.toList()))
            .build();
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
     * Valida los datos para crear un tablero.
     */
    private void validarCrearTablero(CrearTableroRequest request) {
        if (request.getTitulo() == null || request.getTitulo().trim().isEmpty()) {
            throw new ErrorValidacionException("El título del tablero no puede estar vacío");
        }
        
        if (request.getPropietarioEmail() == null || !request.getPropietarioEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ErrorValidacionException("Email inválido: " + request.getPropietarioEmail());
        }
    }
}
