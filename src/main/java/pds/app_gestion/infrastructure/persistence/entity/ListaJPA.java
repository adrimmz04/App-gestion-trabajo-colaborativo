package pds.app_gestion.infrastructure.persistence.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

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

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "lista_prerequisitos", joinColumns = @JoinColumn(name = "lista_id"))
    @Column(name = "lista_prerequisito_id", nullable = false)
    private Set<String> listasPrevias = new HashSet<>();

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
