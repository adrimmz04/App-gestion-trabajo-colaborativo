package pds.app_gestion.domain;

import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Agregado raíz que representa un tablero de tareas.
 * 
 * Un tablero contiene listas de tareas con sus tarjetas asociadas.
 * Puede bloquearse temporalmente para permitir solo movimientos entre listas.
 * Mantiene un historial de todas las acciones realizadas.
 * 
 * Esta es la raíz del agregado Tablero. Las listas y tarjetas son parte del agregado.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Tablero {
    @EqualsAndHashCode.Include
    private final String id;
    
    private final String titulo;
    private String descripcion;
    private final String propietarioEmail;
    private final List<Lista> listas;
    private boolean bloqueado;
    private Optional<LocalDateTime> fechaDesbloqueo;
    private final LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private final List<RegistroAccion> historial;
    private final Set<String> usuariosCompartidos;

    /**
     * Crea un nuevo tablero.
     * 
     * @param id identificador único del tablero
     * @param titulo título del tablero
     * @param propietarioEmail email del usuario propietario
     */
    public Tablero(String id, String titulo, String propietarioEmail) {
        this.id = Objects.requireNonNull(id, "El ID del tablero no puede ser nulo");
        this.titulo = Objects.requireNonNull(titulo, "El título del tablero no puede ser nulo");
        this.propietarioEmail = Objects.requireNonNull(propietarioEmail, "El email del propietario no puede ser nulo");
        this.descripcion = "";
        this.listas = new ArrayList<>();
        this.bloqueado = false;
        this.fechaDesbloqueo = Optional.empty();
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.historial = new ArrayList<>();
        this.usuariosCompartidos = new HashSet<>();
        
        // Registrar creación en el historial
        registrarAccion("TABLERO_CREADO", "Tablero creado: " + titulo);
    }

    /**
     * Añade una lista al tablero.
     * 
     * @param lista lista a añadir
     */
    public void agregarLista(Lista lista) {
        if (lista == null) {
            throw new IllegalArgumentException("La lista no puede ser nula");
        }
        
        if (listas.stream().anyMatch(l -> l.getId().equals(lista.getId()))) {
            throw new IllegalArgumentException("Ya existe una lista con ese ID");
        }
        
        listas.add(lista);
        fechaActualizacion = LocalDateTime.now();
        registrarAccion("LISTA_AÑADIDA", "Lista añadida: " + lista.getNombre());
    }

    /**
     * Elimina una lista del tablero.
     * 
     * @param idLista ID de la lista a eliminar
     */
    public void eliminarLista(String idLista) {
        Lista lista = listas.stream()
            .filter(l -> l.getId().equals(idLista))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Lista no encontrada: " + idLista));
        
        listas.remove(lista);
        fechaActualizacion = LocalDateTime.now();
        registrarAccion("LISTA_ELIMINADA", "Lista eliminada: " + lista.getNombre());
    }

    /**
     * Obtiene una lista por su ID.
     * 
     * @param idLista ID de la lista
     * @return Optional con la lista si existe
     */
    public Optional<Lista> obtenerLista(String idLista) {
        return listas.stream()
            .filter(l -> l.getId().equals(idLista))
            .findFirst();
    }

    /**
     * Obtiene todas las listas del tablero.
     * 
     * @return lista inmutable de todas las listas
     */
    public List<Lista> obtenerListas() {
        return Collections.unmodifiableList(listas);
    }

    /**
     * Bloquea el tablero temporalmente.
     * 
     * Cuando un tablero está bloqueado, solo se pueden mover tarjetas entre listas,
     * no se pueden añadir nuevas tarjetas.
     * 
     * @param duracionMinutos número de minutos que el tablero permanecerá bloqueado
     */
    public void bloquear(int duracionMinutos) {
        if (duracionMinutos <= 0) {
            throw new IllegalArgumentException("La duración del bloqueo debe ser mayor a 0");
        }
        
        this.bloqueado = true;
        this.fechaDesbloqueo = Optional.of(LocalDateTime.now().plusMinutes(duracionMinutos));
        fechaActualizacion = LocalDateTime.now();
        registrarAccion("TABLERO_BLOQUEADO", "Tablero bloqueado por " + duracionMinutos + " minutos");
    }

    /**
     * Desbloquea el tablero.
     */
    public void desbloquear() {
        if (this.bloqueado) {
            this.bloqueado = false;
            this.fechaDesbloqueo = Optional.empty();
            fechaActualizacion = LocalDateTime.now();
            registrarAccion("TABLERO_DESBLOQUEADO", "Tablero desbloqueado");
        }
    }

    /**
     * Verifica si el tablero debería estar desbloqueado automáticamente.
     */
    public void verificarDesbloqueoAutomatico() {
        if (bloqueado && fechaDesbloqueo.isPresent() && 
            LocalDateTime.now().isAfter(fechaDesbloqueo.get())) {
            desbloquear();
        }
    }

    /**
     * Comparte el tablero con otro usuario.
     * 
     * @param emailUsuario email del usuario con el que se comparte
     */
    public void compartirCon(String emailUsuario) {
        if (emailUsuario == null || emailUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El email del usuario no puede estar vacío");
        }
        
        if (!emailUsuario.equals(propietarioEmail)) {
            usuariosCompartidos.add(emailUsuario);
            fechaActualizacion = LocalDateTime.now();
            registrarAccion("TABLERO_COMPARTIDO", "Tablero compartido con: " + emailUsuario);
        }
    }

    /**
     * Revoca el acceso del tablero a un usuario.
     * 
     * @param emailUsuario email del usuario
     */
    public void revocarAcceso(String emailUsuario) {
        if (usuariosCompartidos.remove(emailUsuario)) {
            fechaActualizacion = LocalDateTime.now();
            registrarAccion("ACCESO_REVOCADO", "Acceso revocado a: " + emailUsuario);
        }
    }

    /**
     * Verifica si un usuario tiene acceso al tablero.
     * 
     * @param emailUsuario email del usuario a verificar
     */
    public boolean tieneAcceso(String emailUsuario) {
        return emailUsuario.equals(propietarioEmail) || usuariosCompartidos.contains(emailUsuario);
    }

    /**
     * Actualiza la descripción del tablero.
     * 
     * @param nuevaDescripcion nueva descripción
     */
    public void actualizarDescripcion(String nuevaDescripcion) {
        this.descripcion = nuevaDescripcion != null ? nuevaDescripcion : "";
        fechaActualizacion = LocalDateTime.now();
        registrarAccion("DESCRIPCION_ACTUALIZADA", "Descripción del tablero actualizada");
    }

    /**
     * Obtiene el historial completo de acciones.
     */
    public List<RegistroAccion> obtenerHistorial() {
        return new ArrayList<>(historial);
    }

    /**
     * Registra una acción en el historial del tablero.
     * 
     * @param tipo tipo de acción
     * @param detalles detalles de la acción
     */
    public void registrarAccion(String tipo, String detalles) {
        historial.add(new RegistroAccion(tipo, detalles, LocalDateTime.now()));
    }

    /**
     * Obtiene el número total de tarjetas en el tablero.
     */
    public int obtenerTotalTarjetas() {
        return listas.stream()
            .mapToInt(Lista::obtenerCantidadTarjetas)
            .sum();
    }

    /**
     * Obtiene todas las tarjetas completadas en el tablero.
     */
    public List<Tarjeta> obtenerTarjetasCompletadas() {
        return listas.stream()
            .flatMap(l -> l.obtenerTarjetasCompletadas().stream())
            .toList();
    }
}
