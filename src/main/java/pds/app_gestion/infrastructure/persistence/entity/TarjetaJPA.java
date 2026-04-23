package pds.app_gestion.infrastructure.persistence.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad JPA para persistir tarjetas en la base de datos.
 * Mapea la entidad Tarjeta del dominio.
 * Soporta dos tipos: TAREA y CHECKLIST.
 * Anotada con @Audited para mantener historial automático de cambios.
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

    @Column(nullable = false)
    private boolean archivada;

    @Column(name = "fecha_archivado")
    private LocalDateTime fechaArchivado;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lista_id", nullable = false)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ListaJPA lista;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "tarjetas_etiquetas_nombres", joinColumns = @JoinColumn(name = "tarjeta_id"))
    @Column(name = "etiquetas_nombres")
    private Set<String> etiquetasNombres = new HashSet<>();

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "tarjetas_etiquetas_colores", joinColumns = @JoinColumn(name = "tarjeta_id"))
    @MapKeyColumn(name = "etiquetas_colores_key")
    @Column(name = "etiquetas_colores_value")
    private java.util.Map<String, String> etiquetasColores = new java.util.HashMap<>();

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "tarjetas_listas_visitadas", joinColumns = @JoinColumn(name = "tarjeta_id"))
    @Column(name = "listas_visitadas")
    private Set<String> listasVisitadas = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (fechaActualizacion == null) {
            fechaActualizacion = fechaCreacion;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
