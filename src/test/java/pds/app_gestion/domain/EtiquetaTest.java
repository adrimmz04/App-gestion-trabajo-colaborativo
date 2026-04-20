package pds.app_gestion.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la clase Etiqueta.
 */
public class EtiquetaTest {

    @Test
    void crearEtiquetaValida() {
        Etiqueta etiqueta = new Etiqueta("Urgente", "#FF5733");
        
        assertEquals("Urgente", etiqueta.getNombre());
        assertEquals("#FF5733", etiqueta.getColor());
    }

    @Test
    void crearEtiquetaConNombreNuloThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Etiqueta(null, "#FF5733");
        });
    }

    @Test
    void crearEtiquetaConNombreVacioThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Etiqueta("", "#FF5733");
        });
    }

    @Test
    void crearEtiquetaConColorInvalidoThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Etiqueta("Urgente", "FF5733");  // Sin #
        });
    }

    @Test
    void crearEtiquetaConColorNuloThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Etiqueta("Urgente", null);
        });
    }

    @Test
    void dosEtiquetasConMismosValoresSonIguales() {
        Etiqueta etiqueta1 = new Etiqueta("Urgente", "#FF5733");
        Etiqueta etiqueta2 = new Etiqueta("Urgente", "#FF5733");
        
        assertEquals(etiqueta1, etiqueta2);
    }

    @Test
    void dosEtiquetasConDistintosValoresNoSonIguales() {
        Etiqueta etiqueta1 = new Etiqueta("Urgente", "#FF5733");
        Etiqueta etiqueta2 = new Etiqueta("Urgente", "#00FF00");
        
        assertNotEquals(etiqueta1, etiqueta2);
    }
}
