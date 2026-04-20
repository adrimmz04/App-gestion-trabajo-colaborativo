package pds.app_gestion.domain;

import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Entidad que representa una lista de tareas dentro de un tablero.
 * 
 * Una lista contiene tarjetas y puede tener restricciones opcionales:
 * - Límite máximo de items
 * - Requisitos de listas previas para poder añadir tarjetas
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Lista {
    @EqualsAndHashCode.Include
    private final String id;
    
    private final String nombre;
    private final List<Tarjeta> tarjetas;
    private Optional<Integer> limiteMaximo;
    private final List<String> listasPrevias;
    private final LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    /**
     * Crea una nueva lista de tareas.
     * 
     * @param id identificador único de la lista
     * @param nombre nombre de la lista
     */
    public Lista(String id, String nombre) {
        this.id = Objects.requireNonNull(id, "El ID de la lista no puede ser nulo");
        this.nombre = Objects.requireNonNull(nombre, "El nombre de la lista no puede ser nulo");
        this.tarjetas = new ArrayList<>();
        this.limiteMaximo = Optional.empty();
        this.listasPrevias = new ArrayList<>();
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Añade una tarjeta a la lista.
     * 
     * @param tarjeta tarjeta a añadir
     * @throws IllegalStateException si se ha alcanzado el límite máximo de tarjetas
     */
    public void agregarTarjeta(Tarjeta tarjeta) {
        if (tarjeta == null) {
            throw new IllegalArgumentException("La tarjeta no puede ser nula");
        }
        
        if (limiteMaximo.isPresent() && tarjetas.size() >= limiteMaximo.get()) {
            throw new IllegalStateException(
                String.format("La lista '%s' ha alcanzado el límite máximo de %d tarjetas", 
                    nombre, limiteMaximo.get())
            );
        }
        
        tarjetas.add(tarjeta);
        fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Elimina una tarjeta de la lista.
     * 
     * @param tarjeta tarjeta a eliminar
     */
    public void eliminarTarjeta(Tarjeta tarjeta) {
        if (tarjeta != null && tarjetas.remove(tarjeta)) {
            fechaActualizacion = LocalDateTime.now();
        }
    }

    /**
     * Obtiene una tarjeta por su ID.
     * 
     * @param idTarjeta ID de la tarjeta
     * @return Optional con la tarjeta si existe
     */
    public Optional<Tarjeta> obtenerTarjeta(String idTarjeta) {
        return tarjetas.stream()
            .filter(t -> t.getId().equals(idTarjeta))
            .findFirst();
    }

    /**
     * Establece el límite máximo de tarjetas en la lista.
     * 
     * @param limite número máximo de tarjetas (null para sin límite)
     */
    public void establecerLimiteMaximo(Integer limite) {
        if (limite != null && limite <= 0) {
            throw new IllegalArgumentException("El límite debe ser mayor a 0");
        }
        this.limiteMaximo = Optional.ofNullable(limite);
        fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Obtiene el número total de tarjetas en la lista.
     */
    public int obtenerCantidadTarjetas() {
        return tarjetas.size();
    }

    /**
     * Obtiene las tarjetas completadas.
     */
    public List<Tarjeta> obtenerTarjetasCompletadas() {
        return tarjetas.stream()
            .filter(Tarjeta::isCompletada)
            .toList();
    }

    /**
     * Obtiene las tarjetas no completadas.
     */
    public List<Tarjeta> obtenerTarjetasNoCompletadas() {
        return tarjetas.stream()
            .filter(t -> !t.isCompletada())
            .toList();
    }
}
