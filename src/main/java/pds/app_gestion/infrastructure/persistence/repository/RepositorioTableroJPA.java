package pds.app_gestion.infrastructure.persistence.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pds.app_gestion.domain.RepositorioTablero;
import pds.app_gestion.domain.Tablero;
import pds.app_gestion.infrastructure.persistence.entity.TableroJPA;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador JPA que implementa el puerto RepositorioTablero del dominio.
 * Utiliza Spring Data JPA para la persistencia.
 * 
 * Este es un ejemplo de hexagonal architecture:
 * - El dominio define el puerto (RepositorioTablero)
 * - La infraestructura proporciona el adaptador (RepositorioTableroJPA)
 * - El adaptador convierte entre objetos de dominio y JPA
 */
@Repository
public class RepositorioTableroJPA implements RepositorioTablero {

    private final RepositorioTableroJpaSpring jpaRepository;
    private final ConvertidorTableroJPA convertidor;

    public RepositorioTableroJPA(RepositorioTableroJpaSpring jpaRepository, 
                                ConvertidorTableroJPA convertidor) {
        this.jpaRepository = jpaRepository;
        this.convertidor = convertidor;
    }

    @Override
    public void guardar(Tablero tablero) {
        TableroJPA jpa = convertidor.convertirAJPA(tablero);
        // Preservar fechas de creación si ya existe
        if (jpaRepository.existsById(tablero.getId())) {
            Optional<TableroJPA> existente = jpaRepository.findById(tablero.getId());
            if (existente.isPresent()) {
                jpa.setFechaCreacion(existente.get().getFechaCreacion());
            }
        }
        jpaRepository.save(jpa);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tablero> obtenerPorId(String id) {
        return jpaRepository.findById(id)
            .map(convertidor::convertirADominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tablero> obtenerPorPropietario(String email) {
        return jpaRepository.findByPropietarioEmail(email).stream()
            .map(convertidor::convertirADominio)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tablero> obtenerCompartidos(String email) {
        return jpaRepository.findTablerosCompartidosCon(email).stream()
            .map(convertidor::convertirADominio)
            .collect(Collectors.toList());
    }

    @Override
    public void eliminar(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existe(String id) {
        return jpaRepository.existsById(id);
    }
}
