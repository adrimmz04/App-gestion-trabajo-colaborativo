package pds.app_gestion.infrastructure.cache;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * Servicio para gestionar operaciones de caché.
 * 
 * Proporciona métodos para limpiar cachés y invalidar entradas
 * cuando el estado de los datos cambia.
 */
@Service
public class CacheService {

    private final CacheManager cacheManager;

    public CacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Limpia el caché de tableros.
     * Debe llamarse cuando se crea, actualiza o elimina un tablero.
     */
    public void limpiarCacheTableros() {
        var cache = cacheManager.getCache("tableros");
        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * Limpia el caché de tableros por propietario.
     * Debe llamarse cuando se crea o elimina un tablero.
     */
    public void limpiarCacheTablerosPropietario() {
        var cache = cacheManager.getCache("tablerosPropietario");
        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * Limpia el caché de tableros compartidos.
     * Debe llamarse cuando se comparte o revoca acceso a un tablero.
     */
    public void limpiarCacheTablerosCompartidos() {
        var cache = cacheManager.getCache("tablerosCompartidos");
        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * Limpia todos los cachés.
     * Debe llamarse como último recurso o en operaciones de mantenimiento.
     */
    public void limpiarTodosCaches() {
        limpiarCacheTableros();
        limpiarCacheTablerosPropietario();
        limpiarCacheTablerosCompartidos();
    }

    /**
     * Invalida una entrada específica del caché de tableros.
     * 
     * @param tableroid ID del tablero a invalidar
     */
    public void invalidarTablero(String tableroid) {
        var cache = cacheManager.getCache("tableros");
        if (cache != null) {
            cache.evict(tableroid);
        }
    }

    /**
     * Invalida las entradas de caché para un propietario específico.
     * 
     * @param emailPropietario Email del propietario
     */
    public void invalidarTablerosPropietario(String emailPropietario) {
        var cache = cacheManager.getCache("tablerosPropietario");
        if (cache != null) {
            cache.evict(emailPropietario);
        }
    }

    /**
     * Invalida las entradas de caché para un usuario específico.
     * 
     * @param emailUsuario Email del usuario
     */
    public void invalidarTablerosCompartidos(String emailUsuario) {
        var cache = cacheManager.getCache("tablerosCompartidos");
        if (cache != null) {
            cache.evict(emailUsuario);
        }
    }
}
