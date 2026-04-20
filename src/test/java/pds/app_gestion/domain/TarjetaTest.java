package pds.app_gestion.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la clase Tarjeta.
 */
public class TarjetaTest {

    private Tarjeta tarjeta;

    @BeforeEach
    void setUp() {
        tarjeta = new Tarjeta("1", "Tarea importante", "Descripción de la tarea");
    }

    @Test
    void crearTarjetaValida() {
        assertEquals("1", tarjeta.getId());
        assertEquals("Tarea importante", tarjeta.getTitulo());
        assertEquals("Descripción de la tarea", tarjeta.getDescripcion());
        assertFalse(tarjeta.isCompletada());
        assertEquals(Tarjeta.TipoTarjeta.TAREA, tarjeta.getTipo());
    }

    @Test
    void crearTarjetaConIdNuloThrows() {
        assertThrows(NullPointerException.class, () -> {
            new Tarjeta(null, "Título", "Descripción");
        });
    }

    @Test
    void crearTarjetaConTituloNuloThrows() {
        assertThrows(NullPointerException.class, () -> {
            new Tarjeta("1", null, "Descripción");
        });
    }

    @Test
    void marcarTarjetaComoCompletada() {
        assertFalse(tarjeta.isCompletada());
        assertNull(tarjeta.getFechaCompletacion());
        
        tarjeta.marcarComoCompletada();
        
        assertTrue(tarjeta.isCompletada());
        assertNotNull(tarjeta.getFechaCompletacion());
    }

    @Test
    void marcarTarjetaComoNoCompletada() {
        tarjeta.marcarComoCompletada();
        assertTrue(tarjeta.isCompletada());
        
        tarjeta.marcarComoNoCompletada();
        
        assertFalse(tarjeta.isCompletada());
        assertNull(tarjeta.getFechaCompletacion());
    }

    @Test
    void agregarEtiquetaATarjeta() {
        Etiqueta etiqueta = new Etiqueta("Urgente", "#FF5733");
        
        tarjeta.agregarEtiqueta(etiqueta);
        
        assertTrue(tarjeta.getEtiquetas().contains(etiqueta));
        assertEquals(1, tarjeta.getEtiquetas().size());
    }

    @Test
    void eliminarEtiquetaDeTarjeta() {
        Etiqueta etiqueta = new Etiqueta("Urgente", "#FF5733");
        tarjeta.agregarEtiqueta(etiqueta);
        
        tarjeta.eliminarEtiqueta(etiqueta);
        
        assertFalse(tarjeta.getEtiquetas().contains(etiqueta));
        assertEquals(0, tarjeta.getEtiquetas().size());
    }

    @Test
    void actualizarDescripcionDeTarjeta() {
        tarjeta.actualizarDescripcion("Nueva descripción");
        
        assertEquals("Nueva descripción", tarjeta.getDescripcion());
    }

    @Test
    void crearTarjetaDeChecklist() {
        Tarjeta tarjetaChecklist = new Tarjeta("2", "Checklist", "Descripción", Tarjeta.TipoTarjeta.CHECKLIST);
        
        assertEquals(Tarjeta.TipoTarjeta.CHECKLIST, tarjetaChecklist.getTipo());
    }

    @Test
    void dosIdeasConMismoIdSonIguales() {
        Tarjeta otraTarjeta = new Tarjeta("1", "Otro título", "Otra descripción");
        
        assertEquals(tarjeta, otraTarjeta);
    }

    @Test
    void dosIdeasConDistintoIdNoSonIguales() {
        Tarjeta otraTarjeta = new Tarjeta("2", "Tarea importante", "Descripción de la tarea");
        
        assertNotEquals(tarjeta, otraTarjeta);
    }
}
