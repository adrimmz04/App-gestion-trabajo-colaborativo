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
        
        // Configurar explícitamente los días para archivar y eliminar
        servicioCompactacion.setDiasParaArchivar(7);
        servicioCompactacion.setDiasParaEliminar(30);
        
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
        // Crear tarjeta completada hace 35 días
        Tarjeta tarjeta = crearTarjetaCompletadaHaceDias(35);
        lista.agregarTarjeta(tarjeta);

        assertThat(lista.getTarjetas().size()).isEqualTo(1);

        // Archivar manualmente y establecer fecha de archivado a hace 31 días
        tarjeta.archivar();
        establecerFechaArchivado(tarjeta, 31);

        servicioCompactacion.compactarTablero(tablero);

        // Debe ser eliminada porque fue archivada hace > 30 días
        assertThat(lista.getTarjetas().size())
            .as("La tarjeta debe ser eliminada después de estar archivada > 30 días")
            .isEqualTo(0);
    }

    @Test
    @DisplayName("No debe eliminar tarjeta archivada hace menos de 30 días")
    void testNoDebeEliminarTarjetaArchivadaReciente() {
        // Crear tarjeta completada hace 15 días
        Tarjeta tarjeta = crearTarjetaCompletadaHaceDias(15);
        lista.agregarTarjeta(tarjeta);

        // Archivar manualmente y establecer fecha de archivado a hace 20 días
        tarjeta.archivar();
        establecerFechaArchivado(tarjeta, 20);

        servicioCompactacion.compactarTablero(tablero);

        // Debe estar archivada pero no eliminada (solo 20 días, menos de 30)
        assertThat(tarjeta.estaArchivada()).isTrue();
        assertThat(lista.getTarjetas().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Debe obtener estadísticas correctas de compactación")
    void testObtenerEstadisticasCompactacion() {
        // Crear tarjetas en diferentes estados
        Tarjeta t1 = crearTarjetaNoCompletada();
        lista.agregarTarjeta(t1);
        
        Tarjeta t2 = crearTarjetaCompletadaHaceDias(2);
        lista.agregarTarjeta(t2);
        
        Tarjeta t3 = crearTarjetaCompletadaHaceDias(10); // Será archivada
        lista.agregarTarjeta(t3);
        
        // t4 será archivada y eliminada
        Tarjeta t4 = crearTarjetaCompletadaHaceDias(9);
        t4.archivar();
        establecerFechaArchivado(t4, 31);
        lista.agregarTarjeta(t4);

        Map<String, Long> estadisticas = servicioCompactacion.obtenerEstadisticasCompactacion(tablero);

        assertThat(estadisticas.get("totalTarjetas")).isEqualTo(4L);
        assertThat(estadisticas.get("tarjetasCompletadas")).isEqualTo(3L);
        assertThat(estadisticas.get("tarjetasParaArchivar")).isEqualTo(1L);
        assertThat(estadisticas.get("tarjetasParaEliminar")).isEqualTo(1L);
    }

    @Test
    @DisplayName("Debe obtener lista de tarjetas para archivar")
    void testObtenerTarjetasParaArchivar() {
        lista.agregarTarjeta(crearTarjetaCompletadaHaceDias(2)); // No (menos de 7 días)
        lista.agregarTarjeta(crearTarjetaCompletadaHaceDias(10)); // Si (más de 7 días)
        lista.agregarTarjeta(crearTarjetaCompletadaHaceDias(15)); // Si (más de 7 días)

        List<Tarjeta> tarjetasParaArchivar = servicioCompactacion.obtenerTarjetasParaArchivar(tablero);

        assertThat(tarjetasParaArchivar).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Debe obtener lista de tarjetas para eliminar")
    void testObtenerTarjetasParaEliminar() {
        // Tarjeta archivada hace 20 días (no debe eliminarse)
        Tarjeta t1 = crearTarjetaCompletadaHaceDias(25);
        t1.archivar();
        establecerFechaArchivado(t1, 20);
        lista.agregarTarjeta(t1);
        
        // Tarjeta archivada hace 35 días (debe eliminarse)
        Tarjeta t2 = crearTarjetaCompletadaHaceDias(40);
        t2.archivar();
        establecerFechaArchivado(t2, 35);
        lista.agregarTarjeta(t2);
        
        // Tarjeta archivada hace 40 días (debe eliminarse)
        Tarjeta t3 = crearTarjetaCompletadaHaceDias(45);
        t3.archivar();
        establecerFechaArchivado(t3, 40);
        lista.agregarTarjeta(t3);

        List<Tarjeta> tarjetasParaEliminar = servicioCompactacion.obtenerTarjetasParaEliminar(tablero);

        assertThat(tarjetasParaEliminar).hasSizeGreaterThanOrEqualTo(2);
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
    @DisplayName("La compactación automática debe consultar todos los tableros")
    void testCompactacionAutomaticaConsultaTodosLosTableros() {
        Tablero tableroUno = new Tablero("tablero-1", "Tablero 1", "usuario1@test.com");
        tableroUno.agregarLista(new Lista("lista-1", "Lista 1"));

        Tablero tableroDos = new Tablero("tablero-2", "Tablero 2", "usuario2@test.com");
        tableroDos.agregarLista(new Lista("lista-2", "Lista 2"));

        when(repositorioMock.obtenerTodos()).thenReturn(List.of(tableroUno, tableroDos));

        servicioCompactacion.ejecutarCompactacionAutomatica();

        verify(repositorioMock, times(1)).obtenerTodos();
        verify(repositorioMock, times(1)).guardar(tableroUno);
        verify(repositorioMock, times(1)).guardar(tableroDos);
        verify(repositorioMock, never()).obtenerCompartidos("");
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
        establecerFechaArchivado(tarjeta, diasDesdeArchivo);
        
        return tarjeta;
    }

    private void establecerFechaArchivado(Tarjeta tarjeta, int dias) {
        try {
            var field = Tarjeta.class.getDeclaredField("fechaArchivado");
            field.setAccessible(true);
            field.set(tarjeta, LocalDateTime.now().minusDays(dias));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
