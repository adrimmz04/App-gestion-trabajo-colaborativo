package pds.app_gestion.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuración de caché con Caffeine.
 * 
 * Habilita el caché a nivel de aplicación utilizando Caffeine,
 * una librería de caché de alta performance en memoria.
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    /**
     * Configura el CacheManager con Caffeine.
     * 
     * @return CacheManager configurado
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "tableros",
            "tablerosPropietario",
            "tablerosCompartidos"
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
            // Tiempo de expiración: 10 minutos después del último acceso
            .expireAfterAccess(10, TimeUnit.MINUTES)
            // Tamaño máximo del caché: 1000 entradas
            .maximumSize(1000)
            // Habilitar estadísticas para monitoreo
            .recordStats());

        return cacheManager;
    }
}
