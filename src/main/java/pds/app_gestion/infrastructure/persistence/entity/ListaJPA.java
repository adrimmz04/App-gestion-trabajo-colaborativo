package pds.app_gestion.infrastructure.persistence.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TableroJPA tablero;

    @Builder.Default
    @OneToMany(mappedBy = "lista", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<TarjetaJPA> tarjetas = new HashSet<>();
}
