package pds.app_gestion.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import pds.app_gestion.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests para ServicioCompactacion.
 * 
 * Valida:
 * - Archivado automático de tarjetas completadas hace > 7 días
 * - Eliminación de tarjetas archivadas hace > 30 días
 * - Estadísticas de compactación
 * - Obtención de tarjetas para archivación/eliminación
 */
@DisplayName("Servicio de Compactación Automática")
class ServicioCompactacionTest {

    private ServicioCompactacion servicioCompactacion;
    private RepositorioTablero repositorioMock;
    private Tablero tablero;
    private Lista lista;

    @BeforeEach
    void setUp() {
        repositorioMock = mock(RepositorioTablero.class);
        servicioCompactacion = new ServicioCompactacion(repositorioMock);
        
        // Crear tablero con una lista
        tablero = new Tablero("id-tablero-1", "Tablero Test", "usuario@test.com");
        lista = new Lista("id-lista-1", "Lista Test");
        tablero.agregarLista(lista);
    }

    @Test
    @DisplayName("Debe archivar tarjeta completada hace más de 7 días")
    void testDebeArchivarTarjetaCompletadaAntiguaYNoArchivar() {
        // Crear tarjeta completada hace 10 días
        Tarjeta tarjeta = crearTarjetaCompletadaHaceDias(10);
        lista.agregarTarjeta(tarjeta);

        servicioCompactacion.compactarTablero(tablero);

        assertThat(tarjeta.estaArchivada())
            .as("La tarjeta debe estar archivada después de compactación")
            .isTrue();
    }

    @Test
    @DisplayName("No debe archivar tarjeta completada hace menos de 7 días")
    void testNoDebeArchivarTarjetaCompletadaReciente() {
        // Crear tarjeta completada hace 3 días
        Tarjeta tarjeta = crearTarjetaCompletadaHaceDias(3);
        lista.agregarTarjeta(tarjeta);

        servicioCompactacion.compactarTablero(tablero);

        assertThat(tarjeta.estaArchivada())
            .as("La tarjeta no debe estar archivada si fue completada hace menos de 7 días")
            .isFalse();
    }

    @Test
    @DisplayName("Debe eliminar tarjeta archivada hace más de 30 días")
    void testDebeEliminarTarjetaArchivadaAntigua() {
        // Crear tarjeta completada hace 35 días (será archivada después de 7)
        Tarjeta tarjeta = crearTarjetaCompletadaHaceDias(35);
        lista.agregarTarjeta(tarjeta);

        assertThat(lista.getTarjetas().size()).isEqualTo(1);

        servicioCompactacion.compactarTablero(tablero);

        // Debe ser eliminada porque fue completada hace > 30 días
        // (archivada después de 7 días, luego eliminada después de otros 30)
        assertThat(lista.getTarjetas().size())
            .as("La tarjeta debe ser eliminada después de estar archivada > 30 días")
            .isEqualTo(0);
    }

    @Test
    @DisplayName("No debe eliminar tarjeta archivada hace menos de 30 días")
    void testNoDebeEliminarTarjetaArchivadaReciente() {
        // Crear tarjeta completada hace 20 días
        Tarjeta tarjeta = crearTarjetaCompletadaHaceDias(20);
        lista.agregarTarjeta(tarjeta);

        servicioCompactacion.compactarTablero(tablero);

        // Debe estar archivada pero no eliminada
        assertThat(tarjeta.estaArchivada()).isTrue();
        assertThat(lista.getTarjetas().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Debe obtener estadísticas correctas de compactación")
    void testObtenerEstadisticasCompactacion() {
        // Crear 5 tarjetas en diferentes estados
        lista.agregarTarjeta(crearTarjetaNoCompletada()); // Completada: No
        lista.agregarTarjeta(crearTarjetaCompletadaHaceDias(2)); // Completada: No archivada
        lista.agregarTarjeta(crearTarjetaCompletadaHaceDias(10)); // Completada: Será archivada
        lista.agregarTarjeta(crearTarjetaCompletadaHaceDias(35)); // Completada: Será eliminada
        lista.agregarTarjeta(crearTarjetaArchivada(15)); // Archivada: Reciente

        Map<String, Long> estadisticas = servicioCompactacion.obtenerEstadisticasCompactacion(tablero);

        assertThat(estadisticas.get("totalTarjetas")).isEqualTo(5L);
        assertThat(estadisticas.get("tarjetasCompletadas")).isGreaterThanOrEqualTo(4L);
        assertThat(estadisticas.get("tarjetasParaArchivar")).isEqualTo(1L);
        assertThat(estadisticas.get("tarjetasParaEliminar")).isEqualTo(1L);
    }

    @Test
    @DisplayName("Debe obtener lista de tarjetas para archivar")
    void testObtenerTarjetasParaArchivar() {
        lista.agregarTarjeta(crearTarjetaCompletadaHaceDias(2)); // No
        lista.agregarTarjeta(crearTarjetaCompletadaHaceDias(10)); // Si
        lista.agregarTarjeta(crearTarjetaCompletadaHaceDias(15)); // Si

        List<Tarjeta> tarjetasParaArchivar = servicioCompactacion.obtenerTarjetasParaArchivar(tablero);

        assertThat(tarjetasParaArchivar).hasSize(2);
    }

    @Test
    @DisplayName("Debe obtener lista de tarjetas para eliminar")
    void testObtenerTarjetasParaEliminar() {
        lista.agregarTarjeta(crearTarjetaArchivada(20)); // No
        lista.agregarTarjeta(crearTarjetaArchivada(35)); // Si
        lista.agregarTarjeta(crearTarjetaArchivada(40)); // Si

        List<Tarjeta> tarjetasParaEliminar = servicioCompactacion.obtenerTarjetasParaEliminar(tablero);

        assertThat(tarjetasParaEliminar).hasSize(2);
    }

    @Test
    @DisplayName("Debe permitir configurar días para archivado")
    void testConfigurarDiasParaArchivar() {
        servicioCompactacion.setDiasParaArchivar(14);
        assertThat(servicioCompactacion.getDiasParaArchivar()).isEqualTo(14);
    }

    @Test
    @DisplayName("Debe permitir configurar días para eliminación")
    void testConfigurarDiasParaEliminar() {
        servicioCompactacion.setDiasParaEliminar(60);
        assertThat(servicioCompactacion.getDiasParaEliminar()).isEqualTo(60);
    }

    @Test
    @DisplayName("No debe procesar tablero nulo")
    void testNoDebeProcesoTableroNulo() {
        assertThatNoException().isThrownBy(() -> 
            servicioCompactacion.compactarTablero(null)
        );
    }

    @Test
    @DisplayName("No debe procesar tablero sin listas")
    void testNoDebeProcesoTableroSinListas() {
        Tablero tableroVacio = new Tablero("id", "Vacío", "usuario@test.com");
        
        assertThatNoException().isThrownBy(() -> 
            servicioCompactacion.compactarTablero(tableroVacio)
        );
    }

    // Métodos auxiliares para crear tarjetas en diferentes estados

    private Tarjeta crearTarjetaCompletadaHaceDias(int dias) {
        Tarjeta tarjeta = new Tarjeta(
            UUID.randomUUID().toString(),
            "Tarjeta completada hace " + dias + " días",
            "Descripción"
        );
        tarjeta.marcarComoCompletada();
        
        // Manipular fecha de completación mediante reflexión (por testing)
        try {
            var field = Tarjeta.class.getDeclaredField("fechaCompletacion");
            field.setAccessible(true);
            field.set(tarjeta, LocalDateTime.now().minusDays(dias));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        
        return tarjeta;
    }

    private Tarjeta crearTarjetaNoCompletada() {
        return new Tarjeta(
            UUID.randomUUID().toString(),
            "Tarjeta no completada",
            "Descripción"
        );
    }

    private Tarjeta crearTarjetaArchivada(int diasDesdeArchivo) {
        Tarjeta tarjeta = crearTarjetaCompletadaHaceDias(diasDesdeArchivo + 8); // +8 para superar los 7 días
        tarjeta.archivar();
        
        // Manipular fecha de archivado mediante reflexión
        try {
            var field = Tarjeta.class.getDeclaredField("fechaArchivado");
            field.setAccessible(true);
            field.set(tarjeta, LocalDateTime.now().minusDays(diasDesdeArchivo));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        
        return tarjeta;
    }
}
