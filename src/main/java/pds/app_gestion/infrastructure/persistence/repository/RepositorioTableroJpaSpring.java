package pds.app_gestion.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pds.app_gestion.infrastructure.persistence.entity.TableroJPA;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para Tablero.
 * Proporciona métodos de acceso a datos usando Spring Data JPA.
 */
@Repository
public interface RepositorioTableroJpaSpring extends JpaRepository<TableroJPA, String> {

    /**
     * Encuentra todos los tableros cuyo propietario es el email especificado.
     */
    List<TableroJPA> findByPropietarioEmail(String email);

    /**
     * Encuentra todos los tableros compartidos con el email especificado.
     */
    @Query("SELECT t FROM TableroJPA t WHERE :email MEMBER OF t.usuariosCompartidos")
    List<TableroJPA> findTablerosCompartidosCon(@Param("email") String email);

    /**
     * Verifica si existe un tablero con el ID especificado.
     */
    boolean existsById(String id);
}
