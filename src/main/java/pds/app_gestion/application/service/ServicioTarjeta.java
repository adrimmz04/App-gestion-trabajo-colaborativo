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

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
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

        if (lista.tienePrerequisitos()) {
            throw new ErrorOperacionDominioException(
                String.format("No se pueden crear tarjetas directamente en '%s' porque tiene prerequisitos", lista.getNombre())
            );
        }
        
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
            tablero.registrarAccion("TARJETA_CREADA",
                String.format("Tarjeta '%s' creada en '%s'", tarjeta.getTitulo(), lista.getNombre()));
            
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
            tablero.registrarAccion("TARJETA_ACTUALIZADA",
                String.format("Descripción actualizada para '%s'", tarjeta.getTitulo()));
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

        boolean estabaCompletada = tarjeta.isCompletada();
        tarjeta.marcarComoCompletada();

        if (!estabaCompletada) {
            tablero.registrarAccion("TARJETA_COMPLETADA",
                String.format("Tarjeta '%s' marcada como completada", tarjeta.getTitulo()));
        }

        moverATarjetasCompletadasSiExiste(tablero, lista, tarjeta);
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

        boolean estabaCompletada = tarjeta.isCompletada();
        tarjeta.marcarComoNoCompletada();

        if (estabaCompletada) {
            tablero.registrarAccion("TARJETA_REABIERTA",
                String.format("Tarjeta '%s' marcada como pendiente", tarjeta.getTitulo()));
        }

        repositorioTablero.guardar(tablero);
        
        return convertirATarjetaResponse(tarjeta);
    }

    /**
     * Caso de uso: Eliminar una tarjeta.
     *
     * @param idTablero ID del tablero
     * @param idLista ID de la lista
     * @param idTarjeta ID de la tarjeta
     * @param emailUsuario email del usuario que solicita
     */
    public void eliminarTarjeta(String idTablero, String idLista, String idTarjeta, String emailUsuario) {
        Tablero tablero = obtenerTablero(idTablero, emailUsuario);
        Lista lista = obtenerLista(tablero, idLista);
        Tarjeta tarjeta = obtenerTarjeta(lista, idTarjeta);

        tablero.registrarAccion("TARJETA_ELIMINADA",
            String.format("Tarjeta '%s' eliminada de '%s'", tarjeta.getTitulo(), lista.getNombre()));
        lista.eliminarTarjeta(tarjeta);
        repositorioTablero.guardar(tablero);
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
            tablero.registrarAccion("ETIQUETA_AGREGADA",
                String.format("Etiqueta '%s' agregada a '%s'", etiqueta.getNombre(), tarjeta.getTitulo()));
            repositorioTablero.guardar(tablero);
            return convertirATarjetaResponse(tarjeta);
        } catch (IllegalArgumentException e) {
            throw new ErrorValidacionException(e.getMessage());
        }
    }

    /**
     * Caso de uso: Obtener tarjetas de una lista filtradas por etiquetas.
     * 
     * @param idTablero ID del tablero
     * @param idLista ID de la lista
     * @param emailUsuario email del usuario que solicita
     * @param nombreEtiquetas conjunto de nombres de etiquetas para filtrar
     * @return lista de tarjetas que contienen todas las etiquetas especificadas
     */
    public java.util.List<TarjetaResponse> obtenerTarjetasPorEtiquetas(String idTablero, String idLista, 
                                                                       String emailUsuario, java.util.Set<String> nombreEtiquetas) {
        if (nombreEtiquetas == null || nombreEtiquetas.isEmpty()) {
            return obtenerTodasLasTarjetas(idTablero, idLista, emailUsuario);
        }
        
        Tablero tablero = obtenerTablero(idTablero, emailUsuario);
        Lista lista = obtenerLista(tablero, idLista);
        
        return lista.getTarjetas().stream()
            .filter(tarjeta -> contieneTodasLasEtiquetas(tarjeta, nombreEtiquetas))
            .map(this::convertirATarjetaResponse)
            .collect(Collectors.toList());
    }

    /**
     * Caso de uso: Obtener todas las tarjetas de una lista.
     * 
     * @param idTablero ID del tablero
     * @param idLista ID de la lista
     * @param emailUsuario email del usuario que solicita
     * @return lista de todas las tarjetas de la lista
     */
    public java.util.List<TarjetaResponse> obtenerTodasLasTarjetas(String idTablero, String idLista, String emailUsuario) {
        Tablero tablero = obtenerTablero(idTablero, emailUsuario);
        Lista lista = obtenerLista(tablero, idLista);
        
        return lista.getTarjetas().stream()
            .map(this::convertirATarjetaResponse)
            .collect(Collectors.toList());
    }

    /**
     * Caso de uso: Obtener todas las etiquetas únicas usadas en las tarjetas de una lista.
     * 
     * @param idTablero ID del tablero
     * @param idLista ID de la lista
     * @param emailUsuario email del usuario que solicita
     * @return conjunto de etiquetas únicas de la lista
     */
    public java.util.Set<EtiquetaResponse> obtenerEtiquetasDeLista(String idTablero, String idLista, String emailUsuario) {
        Tablero tablero = obtenerTablero(idTablero, emailUsuario);
        Lista lista = obtenerLista(tablero, idLista);
        
        return lista.getTarjetas().stream()
            .flatMap(tarjeta -> tarjeta.getEtiquetas().stream())
            .map(e -> EtiquetaResponse.builder()
                .nombre(e.getNombre())
                .color(e.getColor())
                .build())
            .collect(Collectors.toSet());
    }

    /**
     * Verifica si una tarjeta contiene todas las etiquetas especificadas.
     */
    private boolean contieneTodasLasEtiquetas(Tarjeta tarjeta, java.util.Set<String> nombreEtiquetas) {
        return nombreEtiquetas.stream()
            .allMatch(nombre -> tarjeta.getEtiquetas().stream()
                .anyMatch(etiqueta -> etiqueta.getNombre().equalsIgnoreCase(nombre)));
    }

    private void moverATarjetasCompletadasSiExiste(Tablero tablero, Lista listaActual, Tarjeta tarjeta) {
        Optional<Lista> listaCompletadas = tablero.obtenerListas().stream()
            .filter(lista -> !lista.getId().equals(listaActual.getId()))
            .filter(lista -> esListaDeCompletadas(lista.getNombre()))
            .findFirst();

        if (listaCompletadas.isEmpty()) {
            return;
        }

        Lista listaDestino = listaCompletadas.get();
        if (listaDestino.getLimiteMaximo().isPresent()
            && listaDestino.obtenerCantidadTarjetas() >= listaDestino.getLimiteMaximo().get()) {
            return;
        }

        if (listaDestino.tienePrerequisitos() && !listaDestino.cumpleRequisitos(tarjeta)) {
            return;
        }

        listaActual.eliminarTarjeta(tarjeta);
        try {
            listaDestino.agregarTarjeta(tarjeta);
        } catch (IllegalStateException ex) {
            listaActual.agregarTarjeta(tarjeta);
            return;
        }

        tablero.registrarAccion("TARJETA_MOVIDA",
            String.format("Tarjeta '%s' movida de '%s' a '%s'",
                tarjeta.getTitulo(), listaActual.getNombre(), listaDestino.getNombre()));
    }

    private boolean esListaDeCompletadas(String nombreLista) {
        if (nombreLista == null || nombreLista.isBlank()) {
            return false;
        }

        String normalizado = Normalizer.normalize(nombreLista, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT);

        return normalizado.contains("hecho")
            || normalizado.contains("done")
            || normalizado.contains("completad")
            || normalizado.contains("finalizad");
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
