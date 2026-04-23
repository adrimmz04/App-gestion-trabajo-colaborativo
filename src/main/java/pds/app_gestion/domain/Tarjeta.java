package pds.app_gestion.domain;

import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Entidad que representa una tarjeta en una lista de tareas.
 * 
 * Una tarjeta puede ser de dos tipos:
 * - Tarjeta de tareas: contiene una descripción de tarea
 * - Tarjeta de checklist: contiene un conjunto de elementos de verificación
 * 
 * Las tarjetas pueden estar etiquetadas y completadas.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Tarjeta {
    @EqualsAndHashCode.Include
    private final String id;
    
    private final String titulo;
    private String descripcion;
    private boolean completada;
    private final Set<Etiqueta> etiquetas;
    private final Set<String> listasVisitadas;
    private final TipoTarjeta tipo;
    private final LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private LocalDateTime fechaCompletacion;
    private boolean archivada;
    private LocalDateTime fechaArchivado;

    /**
     * Crea una nueva tarjeta de tipo tarea.
     * 
     * @param id identificador único de la tarjeta
     * @param titulo título de la tarjeta
     * @param descripcion descripción de la tarea
     */
    public Tarjeta(String id, String titulo, String descripcion) {
        this.id = Objects.requireNonNull(id, "El ID de la tarjeta no puede ser nulo");
        this.titulo = Objects.requireNonNull(titulo, "El título de la tarjeta no puede ser nulo");
        this.descripcion = descripcion != null ? descripcion : "";
        this.completada = false;
        this.etiquetas = new HashSet<>();
        this.listasVisitadas = new HashSet<>();
        this.tipo = TipoTarjeta.TAREA;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.archivada = false;
        this.fechaArchivado = null;
    }

    /**
     * Crea una nueva tarjeta con tipo especificado.
     * 
     * @param id identificador único de la tarjeta
     * @param titulo título de la tarjeta
     * @param descripcion descripción de la tarjeta
     * @param tipo tipo de tarjeta (TAREA o CHECKLIST)
     */
    public Tarjeta(String id, String titulo, String descripcion, TipoTarjeta tipo) {
        this.id = Objects.requireNonNull(id, "El ID de la tarjeta no puede ser nulo");
        this.titulo = Objects.requireNonNull(titulo, "El título de la tarjeta no puede ser nulo");
        this.descripcion = descripcion != null ? descripcion : "";
        this.completada = false;
        this.etiquetas = new HashSet<>();
        this.listasVisitadas = new HashSet<>();
        this.tipo = Objects.requireNonNull(tipo, "El tipo de tarjeta no puede ser nulo");
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.archivada = false;
        this.fechaArchivado = null;
    }

    /**
     * Marca la tarjeta como completada.
     */
    public void marcarComoCompletada() {
        if (!this.completada) {
            this.completada = true;
            this.fechaCompletacion = LocalDateTime.now();
            this.fechaActualizacion = LocalDateTime.now();
        }
    }

    /**
     * Marca la tarjeta como no completada.
     */
    public void marcarComoNoCompletada() {
        if (this.completada) {
            this.completada = false;
            this.fechaCompletacion = null;
            this.fechaActualizacion = LocalDateTime.now();
        }
    }

    /**
     * Añade una etiqueta a la tarjeta.
     * 
     * @param etiqueta etiqueta a añadir
     */
    public void agregarEtiqueta(Etiqueta etiqueta) {
        if (etiqueta != null) {
            this.etiquetas.add(etiqueta);
            this.fechaActualizacion = LocalDateTime.now();
        }
    }

    /**
     * Elimina una etiqueta de la tarjeta.
     * 
     * @param etiqueta etiqueta a eliminar
     */
    public void eliminarEtiqueta(Etiqueta etiqueta) {
        if (etiqueta != null) {
            this.etiquetas.remove(etiqueta);
            this.fechaActualizacion = LocalDateTime.now();
        }
    }

    /**
     * Actualiza la descripción de la tarjeta.
     * 
     * @param nuevaDescripcion nueva descripción
     */
    public void actualizarDescripcion(String nuevaDescripcion) {
        this.descripcion = nuevaDescripcion != null ? nuevaDescripcion : "";
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Registra que la tarjeta ha pasado por una lista del tablero.
     *
     * @param idLista identificador de la lista
     */
    public void registrarPasoPorLista(String idLista) {
        if (idLista == null || idLista.isBlank()) {
            throw new IllegalArgumentException("El ID de la lista no puede estar vacío");
        }

        listasVisitadas.add(idLista);
        fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Indica si la tarjeta ha pasado por una lista concreta.
     *
     * @param idLista identificador de la lista
     * @return true si ya pasó por la lista
     */
    public boolean haPasadoPorLista(String idLista) {
        return listasVisitadas.contains(idLista);
    }

    /**
     * Archiva la tarjeta.
     */
    public void archivar() {
        if (!this.archivada) {
            this.archivada = true;
            this.fechaArchivado = LocalDateTime.now();
            this.fechaActualizacion = LocalDateTime.now();
        }
    }

    /**
     * Desarchiva la tarjeta.
     */
    public void desarchivar() {
        if (this.archivada) {
            this.archivada = false;
            this.fechaArchivado = null;
            this.fechaActualizacion = LocalDateTime.now();
        }
    }

    /**
     * Comprueba si la tarjeta está archivada.
     * 
     * @return true si está archivada, false en caso contrario
     */
    public boolean estaArchivada() {
        return this.archivada;
    }

    /**
     * Enum que representa los tipos de tarjeta disponibles.
     */
    public enum TipoTarjeta {
        TAREA("Tarea"),
        CHECKLIST("Checklist");

        private final String descripcion;

        TipoTarjeta(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }
}
