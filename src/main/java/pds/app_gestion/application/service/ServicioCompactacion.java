package pds.app_gestion.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pds.app_gestion.domain.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio responsable de la compactación automática de datos.
 * 
 * Implementa las políticas de ciclo de vida de tarjetas:
 * - Archivado automático de tarjetas completadas después de X días
 * - Eliminación de tarjetas archivadas después de Y días
 * - Ejecución periódica vía scheduler
 */
@Service
public class ServicioCompactacion {

    private final RepositorioTablero repositorioTablero;
    
    @Value("${compactacion.dias-archivo:7}")
    private int diasParaArchivar;
    
    @Value("${compactacion.dias-eliminacion:30}")
    private int diasParaEliminar;

    public ServicioCompactacion(RepositorioTablero repositorioTablero) {
        this.repositorioTablero = repositorioTablero;
    }

    /**
     * Ejecuta la compactación automática cada 24 horas a las 2 AM.
     * 
     * Políticas aplicadas:
     * - Archiva tarjetas completadas hace más de 7 días (configurable)
     * - Elimina tarjetas archivadas hace más de 30 días (configurable)
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void ejecutarCompactacionAutomatica() {
        System.out.println("[CompactacionAutomatica] Iniciando proceso de compactación a las " + LocalDateTime.now());
        
        List<Tablero> tableros = repositorioTablero.obtenerTodos();
        
        for (Tablero tablero : tableros) {
            compactarTablero(tablero);
        }
        
        System.out.println("[CompactacionAutomatica] Compactación completada a las " + LocalDateTime.now());
    }

    /**
     * Compacta un tablero específico aplicando políticas de archivado y eliminación.
     * 
     * @param tablero tablero a compactar
     */
    public void compactarTablero(Tablero tablero) {
        if (tablero == null || tablero.getListas().isEmpty()) {
            return;
        }

        LocalDateTime ahora = LocalDateTime.now();
        
        for (Lista lista : tablero.getListas()) {
            compactarLista(lista, ahora);
        }

        // Guardar los cambios del tablero
        repositorioTablero.guardar(tablero);
    }

    /**
     * Compacta una lista específica archivando y eliminando tarjetas según políticas.
     * 
     * @param lista lista a compactar
     * @param ahora fecha/hora actual
     */
    private void compactarLista(Lista lista, LocalDateTime ahora) {
        List<Tarjeta> tarjetasActuales = new ArrayList<>(lista.getTarjetas());

        for (Tarjeta tarjeta : tarjetasActuales) {
            // 1. Archivar tarjetas completadas hace más de X días
            if (debeArchivarse(tarjeta, ahora)) {
                tarjeta.archivar();
            }

            // 2. Eliminar tarjetas archivadas hace más de Y días
            if (debeEliminarse(tarjeta, ahora)) {
                lista.eliminarTarjeta(tarjeta);
            }
        }
    }

    /**
     * Determina si una tarjeta debe archivarse.
     * 
     * Criterios:
     * - Debe estar completada
     * - Debe estar completada hace más de X días
     * - No debe estar ya archivada
     * 
     * @param tarjeta tarjeta a evaluar
     * @param ahora fecha/hora actual
     * @return true si debe archivarse
     */
    private boolean debeArchivarse(Tarjeta tarjeta, LocalDateTime ahora) {
        if (tarjeta.estaArchivada() || !tarjeta.isCompletada()) {
            return false;
        }

        LocalDateTime fechaCompletacion = tarjeta.getFechaCompletacion();
        if (fechaCompletacion == null) {
            return false;
        }

        long diasDesdeCompletacion = java.time.temporal.ChronoUnit.DAYS.between(
            fechaCompletacion,
            ahora
        );

        return diasDesdeCompletacion >= diasParaArchivar;
    }

    /**
     * Determina si una tarjeta debe eliminarse.
     * 
     * Criterios:
     * - Debe estar archivada
     * - Debe estar archivada hace más de Y días
     * 
     * @param tarjeta tarjeta a evaluar
     * @param ahora fecha/hora actual
     * @return true si debe eliminarse
     */
    private boolean debeEliminarse(Tarjeta tarjeta, LocalDateTime ahora) {
        if (!tarjeta.estaArchivada()) {
            return false;
        }

        LocalDateTime fechaArchivado = tarjeta.getFechaArchivado();
        if (fechaArchivado == null) {
            return false;
        }

        long diasDesdeArchivado = java.time.temporal.ChronoUnit.DAYS.between(
            fechaArchivado,
            ahora
        );

        return diasDesdeArchivado >= diasParaEliminar;
    }

    /**
     * Obtiene estadísticas de compactación para un tablero.
     * 
     * @param tablero tablero a analizar
     * @return map con estadísticas
     */
    public Map<String, Long> obtenerEstadisticasCompactacion(Tablero tablero) {
        Map<String, Long> estadisticas = new HashMap<>();
        
        if (tablero == null || tablero.getListas().isEmpty()) {
            return estadisticas;
        }

        LocalDateTime ahora = LocalDateTime.now();
        
        long totalTarjetas = 0;
        long tarjetasCompletadas = 0;
        long tarjetasArchivadas = 0;
        long tarjetasParaArchivar = 0;
        long tarjetasParaEliminar = 0;

        for (Lista lista : tablero.getListas()) {
            for (Tarjeta tarjeta : lista.getTarjetas()) {
                totalTarjetas++;
                
                if (tarjeta.isCompletada()) {
                    tarjetasCompletadas++;
                }
                
                if (tarjeta.estaArchivada()) {
                    tarjetasArchivadas++;
                    
                    if (debeEliminarse(tarjeta, ahora)) {
                        tarjetasParaEliminar++;
                    }
                } else if (debeArchivarse(tarjeta, ahora)) {
                    tarjetasParaArchivar++;
                }
            }
        }

        estadisticas.put("totalTarjetas", totalTarjetas);
        estadisticas.put("tarjetasCompletadas", tarjetasCompletadas);
        estadisticas.put("tarjetasArchivadas", tarjetasArchivadas);
        estadisticas.put("tarjetasParaArchivar", tarjetasParaArchivar);
        estadisticas.put("tarjetasParaEliminar", tarjetasParaEliminar);

        return estadisticas;
    }

    /**
     * Obtiene tarjetas de un tablero que están lisas para archivarse.
     * 
     * @param tablero tablero a consultar
     * @return lista de tarjetas que deben archivarse
     */
    public List<Tarjeta> obtenerTarjetasParaArchivar(Tablero tablero) {
        if (tablero == null || tablero.getListas().isEmpty()) {
            return Collections.emptyList();
        }

        LocalDateTime ahora = LocalDateTime.now();
        return tablero.getListas().stream()
            .flatMap(lista -> lista.getTarjetas().stream())
            .filter(tarjeta -> debeArchivarse(tarjeta, ahora))
            .collect(Collectors.toList());
    }

    /**
     * Obtiene tarjetas de un tablero que están listas para eliminarse.
     * 
     * @param tablero tablero a consultar
     * @return lista de tarjetas que deben eliminarse
     */
    public List<Tarjeta> obtenerTarjetasParaEliminar(Tablero tablero) {
        if (tablero == null || tablero.getListas().isEmpty()) {
            return Collections.emptyList();
        }

        LocalDateTime ahora = LocalDateTime.now();
        return tablero.getListas().stream()
            .flatMap(lista -> lista.getTarjetas().stream())
            .filter(tarjeta -> debeEliminarse(tarjeta, ahora))
            .collect(Collectors.toList());
    }

    public int getDiasParaArchivar() {
        return diasParaArchivar;
    }

    public int getDiasParaEliminar() {
        return diasParaEliminar;
    }

    public void setDiasParaArchivar(int dias) {
        this.diasParaArchivar = dias;
    }

    public void setDiasParaEliminar(int dias) {
        this.diasParaEliminar = dias;
    }
}
