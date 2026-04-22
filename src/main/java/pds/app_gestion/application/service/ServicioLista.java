package pds.app_gestion.application.service;

import org.springframework.stereotype.Service;
import pds.app_gestion.application.dto.ConfigurarReglasListaRequest;
import pds.app_gestion.application.dto.ReglasListaResponse;
import pds.app_gestion.application.exception.ErrorOperacionDominioException;
import pds.app_gestion.application.exception.ErrorValidacionException;
import pds.app_gestion.application.exception.PermisoNegadoException;
import pds.app_gestion.application.exception.RecursoNoEncontradoException;
import pds.app_gestion.domain.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para gestionar listas y sus reglas.
 * 
 * Implementa los casos de uso relacionados con listas:
 * - Configurar límite máximo de items
 * - Configurar lista previas (requisitos)
 * - Mover tarjetas entre listas (con validación de reglas)
 * - Obtener información de reglas
 */
@Service
public class ServicioLista {

    private final RepositorioTablero repositorioTablero;

    public ServicioLista(RepositorioTablero repositorioTablero) {
        this.repositorioTablero = repositorioTablero;
    }

    /**
     * Caso de uso: Configurar reglas de una lista.
     * 
     * @param idTablero ID del tablero
     * @param idLista ID de la lista
     * @param emailUsuario email del usuario que solicita
     * @param request configuración de reglas
     * @return respuesta con las reglas configuradas
     */
    public ReglasListaResponse configurarReglas(String idTablero, String idLista, 
                                               String emailUsuario, ConfigurarReglasListaRequest request) {
        validarConfiguracionReglas(request);
        
        Tablero tablero = obtenerTablero(idTablero, emailUsuario);
        Lista lista = obtenerLista(tablero, idLista);
        
        // Configurar límite máximo
        if (request.getLimiteMaximo() != null) {
            lista.establecerLimiteMaximo(request.getLimiteMaximo());
        }
        
        // Configurar listas previas
        if (request.getListasPrevias() != null) {
            // Limpiar listas previas anteriores
            List<String> listasActuales = new ArrayList<>(lista.obtenerListasPrevias());
            for (String idListaPrevia : listasActuales) {
                lista.eliminarListaPrevia(idListaPrevia);
            }
            
            // Agregar nuevas listas previas
            for (String idListaPrevia : request.getListasPrevias()) {
                if (!idListaPrevia.equals(idLista)) { // No permitir referencia circular
                    lista.agregarListaPrevia(idListaPrevia);
                }
            }
        }
        
        repositorioTablero.guardar(tablero);
        return convertirAReglasListaResponse(lista);
    }

    /**
     * Caso de uso: Obtener las reglas de una lista.
     * 
     * @param idTablero ID del tablero
     * @param idLista ID de la lista
     * @param emailUsuario email del usuario que solicita
     * @return respuesta con las reglas configuradas
     */
    public ReglasListaResponse obtenerReglas(String idTablero, String idLista, String emailUsuario) {
        Tablero tablero = obtenerTablero(idTablero, emailUsuario);
        Lista lista = obtenerLista(tablero, idLista);
        
        return convertirAReglasListaResponse(lista);
    }

    /**
     * Caso de uso: Mover una tarjeta entre listas con validación de reglas.
     * 
     * @param idTablero ID del tablero
     * @param idListaOrigen ID de la lista origen
     * @param idListaDestino ID de la lista destino
     * @param idTarjeta ID de la tarjeta
     * @param emailUsuario email del usuario que solicita
     * @throws ErrorOperacionDominioException si no cumple las reglas
     */
    public void moverTarjeta(String idTablero, String idListaOrigen, String idListaDestino, 
                            String idTarjeta, String emailUsuario) {
        Tablero tablero = obtenerTablero(idTablero, emailUsuario);

        Lista listaOrigen = obtenerLista(tablero, idListaOrigen);
        Lista listaDestino = obtenerLista(tablero, idListaDestino);
        Tarjeta tarjeta = obtenerTarjeta(listaOrigen, idTarjeta);
        
        // Validar requisitos de la lista destino
        if (listaDestino.tienePrerequisitos()) {
            if (!listaDestino.cumpleRequisitos(tarjeta)) {
                String listasRequeridas = String.join(", ", listaDestino.obtenerListasPrevias());
                throw new ErrorOperacionDominioException(
                    String.format("La tarjeta debe estar completada para poder moverla a '%s'",
                        listaDestino.getNombre())
                );
            }
        }
        
        // Validar límite máximo de la lista destino
        if (listaDestino.getLimiteMaximo().isPresent()) {
            int limiteActual = listaDestino.obtenerCantidadTarjetas();
            int limiteMaximo = listaDestino.getLimiteMaximo().get();
            if (limiteActual >= limiteMaximo) {
                throw new ErrorOperacionDominioException(
                    String.format("La lista '%s' ha alcanzado el límite máximo de %d tarjetas",
                        listaDestino.getNombre(), limiteMaximo)
                );
            }
        }
        
        // Realizar el movimiento
        listaOrigen.eliminarTarjeta(tarjeta);
        listaDestino.agregarTarjeta(tarjeta);
        
        repositorioTablero.guardar(tablero);
        tablero.registrarAccion("TARJETA_MOVIDA", 
            String.format("Tarjeta '%s' movida de '%s' a '%s'", 
                tarjeta.getTitulo(), listaOrigen.getNombre(), listaDestino.getNombre()));
    }

    /**
     * Convierte una Lista a ReglasListaResponse.
     */
    private ReglasListaResponse convertirAReglasListaResponse(Lista lista) {
        return ReglasListaResponse.builder()
            .nombre(lista.getNombre())
            .limiteMaximo(lista.getLimiteMaximo().orElse(null))
            .cantidadActual(lista.obtenerCantidadTarjetas())
            .listasPrevias(lista.obtenerListasPrevias())
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
     * Valida la configuración de reglas.
     */
    private void validarConfiguracionReglas(ConfigurarReglasListaRequest request) {
        if (request.getLimiteMaximo() != null && request.getLimiteMaximo() <= 0) {
            throw new ErrorValidacionException("El límite máximo debe ser mayor a 0");
        }
    }
}
