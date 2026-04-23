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
 * Entidad JPA para persismir tableros en la base de datos.
 * Mapea el agregado Tablero del dominio.
 * 
 * Anotada con @Audited para mantener historial automático de cambios.
 */
@Entity
@Table(name = "tableros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableroJPA {

    @Id
    private String id;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Column(nullable = false, length = 255)
    private String propietarioEmail;

    @Column(nullable = false)
    private boolean bloqueado;

    @Column(name = "fecha_desbloqueo")
    private LocalDateTime fechaDesbloqueo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "tableros_usuarios_compartidos", joinColumns = @JoinColumn(name = "tablero_id"))
    @Column(name = "usuarios_compartidos")
    private Set<String> usuariosCompartidos = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "tablero", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<ListaJPA> listas = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "tablero", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<RegistroAccionJPA> historialAcciones = new HashSet<>();

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
