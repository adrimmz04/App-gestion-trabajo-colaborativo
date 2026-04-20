package pds.app_gestion.domain;

import java.util.Optional;

/**
 * Interfaz del repositorio de Tableros.
 * 
 * Define el puerto (interfaz) que debe implementar cualquier adaptador
 * de persistencia para trabajar con los tableros.
 * 
 * Esta es la definición del puerto, los detalles de persistencia se implementan
 * en el adaptador de infraestructura.
 */
public interface RepositorioTablero {
    /**
     * Guarda un tablero en el repositorio.
     * 
     * @param tablero tablero a guardar
     */
    void guardar(Tablero tablero);

    /**
     * Obtiene un tablero por su ID.
     * 
     * @param id ID del tablero
     * @return Optional con el tablero si existe
     */
    Optional<Tablero> obtenerPorId(String id);

    /**
     * Obtiene todos los tableros del propietario.
     * 
     * @param propietarioEmail email del propietario
     * @return lista de tableros del propietario
     */
    java.util.List<Tablero> obtenerPorPropietario(String propietarioEmail);

    /**
     * Obtiene todos los tableros compartidos con un usuario.
     * 
     * @param emailUsuario email del usuario
     * @return lista de tableros compartidos con el usuario
     */
    java.util.List<Tablero> obtenerCompartidos(String emailUsuario);

    /**
     * Elimina un tablero del repositorio.
     * 
     * @param id ID del tablero a eliminar
     */
    void eliminar(String id);

    /**
     * Verifica si existe un tablero con el ID especificado.
     * 
     * @param id ID del tablero
     * @return true si existe, false en caso contrario
     */
    boolean existe(String id);
}
