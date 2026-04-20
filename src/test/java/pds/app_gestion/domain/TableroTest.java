package pds.app_gestion.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la clase Tablero.
 */
public class TableroTest {

    private Tablero tablero;
    private Lista lista1;
    private Lista lista2;

    @BeforeEach
    void setUp() {
        tablero = new Tablero("tablero-1", "Mi tablero", "adrian@example.com");
        lista1 = new Lista("lista-1", "Por hacer");
        lista2 = new Lista("lista-2", "Haciendo");
    }

    @Test
    void crearTableroValido() {
        assertEquals("tablero-1", tablero.getId());
        assertEquals("Mi tablero", tablero.getTitulo());
        assertEquals("adrian@example.com", tablero.getPropietarioEmail());
        assertFalse(tablero.isBloqueado());
        assertEquals(0, tablero.getListas().size());
    }

    @Test
    void crearTableroConIdNuloThrows() {
        assertThrows(NullPointerException.class, () -> {
            new Tablero(null, "Título", "email@example.com");
        });
    }

    @Test
    void agregarListaAlTablero() {
        tablero.agregarLista(lista1);
        
        assertEquals(1, tablero.getListas().size());
        assertTrue(tablero.getListas().contains(lista1));
    }

    @Test
    void agregarMultiplesListasAlTablero() {
        tablero.agregarLista(lista1);
        tablero.agregarLista(lista2);
        
        assertEquals(2, tablero.getListas().size());
    }

    @Test
    void agregarListaDuplicadaThrows() {
        tablero.agregarLista(lista1);
        
        assertThrows(IllegalArgumentException.class, () -> {
            tablero.agregarLista(lista1);
        });
    }

    @Test
    void obtenerListaPorId() {
        tablero.agregarLista(lista1);
        
        var result = tablero.obtenerLista("lista-1");
        
        assertTrue(result.isPresent());
        assertEquals(lista1, result.get());
    }

    @Test
    void eliminarLista() {
        tablero.agregarLista(lista1);
        tablero.agregarLista(lista2);
        
        tablero.eliminarLista("lista-1");
        
        assertEquals(1, tablero.getListas().size());
        assertFalse(tablero.getListas().contains(lista1));
    }

    @Test
    void bloquearTablero() {
        assertFalse(tablero.isBloqueado());
        
        tablero.bloquear(10);
        
        assertTrue(tablero.isBloqueado());
        assertTrue(tablero.getFechaDesbloqueo().isPresent());
    }

    @Test
    void desbloquearTablero() {
        tablero.bloquear(10);
        
        tablero.desbloquear();
        
        assertFalse(tablero.isBloqueado());
        assertFalse(tablero.getFechaDesbloqueo().isPresent());
    }

    @Test
    void bloquearTableroConDuracionInvalidaThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            tablero.bloquear(0);
        });
    }

    @Test
    void compartirTableroConUsuario() {
        tablero.compartirCon("usuario@example.com");
        
        assertTrue(tablero.tieneAcceso("usuario@example.com"));
    }

    @Test
    void propietarioSiempreTieneAcceso() {
        assertTrue(tablero.tieneAcceso("adrian@example.com"));
    }

    @Test
    void revocarAccesoDeUsuario() {
        tablero.compartirCon("usuario@example.com");
        
        tablero.revocarAcceso("usuario@example.com");
        
        assertFalse(tablero.tieneAcceso("usuario@example.com"));
    }

    @Test
    void actualizarDescripcion() {
        tablero.actualizarDescripcion("Nueva descripción");
        
        assertEquals("Nueva descripción", tablero.getDescripcion());
    }

    @Test
    void obtenerHistorial() {
        tablero.agregarLista(lista1);
        tablero.bloquear(5);
        
        var historial = tablero.obtenerHistorial();
        
        assertTrue(historial.size() >= 2);
        assertTrue(historial.stream().anyMatch(r -> r.getTipo().equals("TABLERO_CREADO")));
        assertTrue(historial.stream().anyMatch(r -> r.getTipo().equals("LISTA_AÑADIDA")));
    }

    @Test
    void obtenerTotalTarjetas() {
        tablero.agregarLista(lista1);
        tablero.agregarLista(lista2);
        
        lista1.agregarTarjeta(new Tarjeta("t1", "Tarea 1", "Desc 1"));
        lista1.agregarTarjeta(new Tarjeta("t2", "Tarea 2", "Desc 2"));
        lista2.agregarTarjeta(new Tarjeta("t3", "Tarea 3", "Desc 3"));
        
        assertEquals(3, tablero.obtenerTotalTarjetas());
    }

    @Test
    void obtenerTarjetasCompletadas() {
        tablero.agregarLista(lista1);
        
        Tarjeta t1 = new Tarjeta("t1", "Tarea 1", "Desc 1");
        Tarjeta t2 = new Tarjeta("t2", "Tarea 2", "Desc 2");
        lista1.agregarTarjeta(t1);
        lista1.agregarTarjeta(t2);
        
        t1.marcarComoCompletada();
        
        var completadas = tablero.obtenerTarjetasCompletadas();
        
        assertEquals(1, completadas.size());
        assertTrue(completadas.contains(t1));
    }
}
