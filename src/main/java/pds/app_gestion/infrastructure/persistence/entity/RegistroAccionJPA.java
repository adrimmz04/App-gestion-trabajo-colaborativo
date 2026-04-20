package pds.app_gestion.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad JPA para persistir el historial de acciones en tableros.
 * Mapea el objeto de valor RegistroAccion del dominio.
 */
@Entity
@Table(name = "registros_acciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroAccionJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(nullable = false, length = 500)
    private String detalles;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tablero_id", nullable = false)
    private TableroJPA tablero;

    @PrePersist
    protected void onCreate() {
        fecha = LocalDateTime.now();
    }
}
