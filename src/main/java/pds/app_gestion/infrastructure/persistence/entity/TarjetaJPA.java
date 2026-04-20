package pds.app_gestion.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad JPA para persistir tarjetas en la base de datos.
 * Mapea la entidad Tarjeta del dominio.
 * Soporta dos tipos: TAREA y CHECKLIST.
 */
@Entity
@Table(name = "tarjetas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarjetaJPA {

    @Id
    private String id;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TipoTarjetaJPA tipo;

    @Column(nullable = false)
    private boolean completada;

    @Column(name = "fecha_completacion")
    private LocalDateTime fechaCompletacion;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lista_id", nullable = false)
    private ListaJPA lista;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "tarjeta_etiquetas", joinColumns = @JoinColumn(name = "tarjeta_id"))
    @Column(name = "etiqueta_nombre")
    private Set<String> etiquetasNombres = new HashSet<>();

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "tarjeta_etiquetas_colores", joinColumns = @JoinColumn(name = "tarjeta_id"))
    @MapKeyColumn(name = "etiqueta_nombre")
    @Column(name = "color")
    private java.util.Map<String, String> etiquetasColores = new java.util.HashMap<>();

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
