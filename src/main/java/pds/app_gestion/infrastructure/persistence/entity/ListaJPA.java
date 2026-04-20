package pds.app_gestion.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad JPA para persistir listas en la base de datos.
 * Mapea la entidad Lista del dominio.
 * Anotada con @Audited para mantener historial automático de cambios.
 */
@Entity
@Table(name = "listas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class ListaJPA {

    @Id
    private String id;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(name = "limite_maximo")
    private Integer limiteMaximo;

    @Column(name = "lista_prerequisito_id")
    private String listaPrerrequisitoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tablero_id", nullable = false)
    private TableroJPA tablero;

    @Builder.Default
    @OneToMany(mappedBy = "lista", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<TarjetaJPA> tarjetas = new HashSet<>();
}
