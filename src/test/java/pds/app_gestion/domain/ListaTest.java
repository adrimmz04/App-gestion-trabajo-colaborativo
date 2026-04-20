package pds.app_gestion.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la clase Lista.
 */
public class ListaTest {

    private Lista lista;
    private Tarjeta tarjeta1;
    private Tarjeta tarjeta2;

    @BeforeEach
    void setUp() {
        lista = new Lista("lista-1", "Por hacer");
        tarjeta1 = new Tarjeta("tarjeta-1", "Tarea 1", "Descripción 1");
        tarjeta2 = new Tarjeta("tarjeta-2", "Tarea 2", "Descripción 2");
    }

    @Test
    void crearListaValida() {
        assertEquals("lista-1", lista.getId());
        assertEquals("Por hacer", lista.getNombre());
        assertEquals(0, lista.getTarjetas().size());
    }

    @Test
    void agregarTarjetaALista() {
        lista.agregarTarjeta(tarjeta1);
        
        assertEquals(1, lista.getTarjetas().size());
        assertTrue(lista.getTarjetas().contains(tarjeta1));
    }

    @Test
    void agregarMultiplesTarjetas() {
        lista.agregarTarjeta(tarjeta1);
        lista.agregarTarjeta(tarjeta2);
        
        assertEquals(2, lista.getTarjetas().size());
    }

    @Test
    void agregarTarjetaNulaThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            lista.agregarTarjeta(null);
        });
    }

    @Test
    void eliminarTarjeta() {
        lista.agregarTarjeta(tarjeta1);
        lista.agregarTarjeta(tarjeta2);
        
        lista.eliminarTarjeta(tarjeta1);
        
        assertEquals(1, lista.getTarjetas().size());
        assertFalse(lista.getTarjetas().contains(tarjeta1));
    }

    @Test
    void obtenerTarjetaPorId() {
        lista.agregarTarjeta(tarjeta1);
        
        var result = lista.obtenerTarjeta("tarjeta-1");
        
        assertTrue(result.isPresent());
        assertEquals(tarjeta1, result.get());
    }

    @Test
    void obtenerTarjetaPorIdNoExistente() {
        var result = lista.obtenerTarjeta("no-existe");
        
        assertFalse(result.isPresent());
    }

    @Test
    void establecerLimiteMaximo() {
        lista.establecerLimiteMaximo(5);
        
        lista.agregarTarjeta(tarjeta1);
        lista.agregarTarjeta(tarjeta2);
        
        assertEquals(2, lista.getTarjetas().size());
    }

    @Test
    void superarLimiteMaximoThrows() {
        lista.establecerLimiteMaximo(1);
        lista.agregarTarjeta(tarjeta1);
        
        assertThrows(IllegalStateException.class, () -> {
            lista.agregarTarjeta(tarjeta2);
        });
    }

    @Test
    void establecerLimiteMenorOIgualACeroThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            lista.establecerLimiteMaximo(0);
        });
    }

    @Test
    void obtenerTarjetasCompletadas() {
        lista.agregarTarjeta(tarjeta1);
        lista.agregarTarjeta(tarjeta2);
        
        tarjeta1.marcarComoCompletada();
        
        var completadas = lista.obtenerTarjetasCompletadas();
        
        assertEquals(1, completadas.size());
        assertTrue(completadas.contains(tarjeta1));
    }

    @Test
    void obtenerTarjetasNoCompletadas() {
        lista.agregarTarjeta(tarjeta1);
        lista.agregarTarjeta(tarjeta2);
        
        tarjeta1.marcarComoCompletada();
        
        var noCompletadas = lista.obtenerTarjetasNoCompletadas();
        
        assertEquals(1, noCompletadas.size());
        assertTrue(noCompletadas.contains(tarjeta2));
    }
}
