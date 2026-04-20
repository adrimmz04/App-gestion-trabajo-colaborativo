package pds.app_gestion.infrastructure.repository;

import org.springframework.stereotype.Repository;
import pds.app_gestion.domain.Tablero;
import pds.app_gestion.domain.RepositorioTablero;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementación en memoria del repositorio de tableros.
 * 
 * Esta es una implementación temporal para desarrollo y testing.
 * Será reemplazada por una implementación con JPA en la siguiente fase.
 */
@Repository
public class RepositorioTableroEnMemoria implements RepositorioTablero {

    private final Map<String, Tablero> tableros = new ConcurrentHashMap<>();

    @Override
    public void guardar(Tablero tablero) {
        tableros.put(tablero.getId(), tablero);
    }

    @Override
    public Optional<Tablero> obtenerPorId(String id) {
        return Optional.ofNullable(tableros.get(id));
    }

    @Override
    public List<Tablero> obtenerPorPropietario(String propietarioEmail) {
        return tableros.values().stream()
            .filter(t -> t.getPropietarioEmail().equals(propietarioEmail))
            .collect(Collectors.toList());
    }

    @Override
    public List<Tablero> obtenerCompartidos(String emailUsuario) {
        return tableros.values().stream()
            .filter(t -> !t.getPropietarioEmail().equals(emailUsuario) && t.tieneAcceso(emailUsuario))
            .collect(Collectors.toList());
    }

    @Override
    public void eliminar(String id) {
        tableros.remove(id);
    }

    @Override
    public boolean existe(String id) {
        return tableros.containsKey(id);
    }
}
