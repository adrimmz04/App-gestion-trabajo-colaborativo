package pds.app_gestion.infrastructure.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import pds.app_gestion.domain.Lista;
import pds.app_gestion.domain.Tablero;
import pds.app_gestion.infrastructure.persistence.repository.ConvertidorTableroJPA;
import pds.app_gestion.infrastructure.persistence.repository.RepositorioTableroJPA;
import pds.app_gestion.infrastructure.persistence.repository.RepositorioTableroJpaSpring;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración para el RepositorioTableroJPA.
 * 
 * Utiliza H2 en memoria para validar el adaptador JPA
 * y el round-trip del agregado Tablero.
 */
@DataJpaTest
@Import(ConvertidorTableroJPA.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class RepositorioTableroJPAIntegrationTest {

    @Autowired
    private RepositorioTableroJpaSpring repositorioJpaSpring;

    @Autowired
    private ConvertidorTableroJPA convertidor;

    private RepositorioTableroJPA repositorio;
    private Tablero tablero1;
    private Tablero tablero2;

    @BeforeEach
    void setUp() {
        // Limpiar base de datos antes de cada test
        repositorioJpaSpring.deleteAll();

        // Crear instancia del repositorio
        repositorio = new RepositorioTableroJPA(repositorioJpaSpring, convertidor);

        // Crear tableros de prueba
        tablero1 = new Tablero("tab-1", "Proyecto A", "usuario@example.com");
        tablero1.actualizarDescripcion("Descripción del proyecto A");

        tablero2 = new Tablero("tab-2", "Proyecto B", "usuario@example.com");
        tablero2.actualizarDescripcion("Descripción del proyecto B");
    }

    @Test
    void testGuardarYObtenerTablero() {
        // Guardar tablero
        repositorio.guardar(tablero1);

        // Obtener tablero
        Optional<Tablero> resultado = repositorio.obtenerPorId("tab-1");

        assertTrue(resultado.isPresent());
        assertEquals("Proyecto A", resultado.get().getTitulo());
        assertEquals("usuario@example.com", resultado.get().getPropietarioEmail());
    }

    @Test
    void testObtenerTablerosPorPropietario() {
        // Guardar múltiples tableros
        repositorio.guardar(tablero1);
        repositorio.guardar(tablero2);

        // Obtener tableros del propietario
        var tableros = repositorio.obtenerPorPropietario("usuario@example.com");

        assertEquals(2, tableros.size());
        assertTrue(tableros.stream().anyMatch(t -> t.getTitulo().equals("Proyecto A")));
        assertTrue(tableros.stream().anyMatch(t -> t.getTitulo().equals("Proyecto B")));
    }

    @Test
    void testCompartirTablero() {
        // Guardar tablero
        repositorio.guardar(tablero1);

        // Compartir tablero
        tablero1.compartirCon("otro@example.com");
        repositorio.guardar(tablero1);

        // Obtener tableros compartidos
        var compartidos = repositorio.obtenerCompartidos("otro@example.com");

        assertEquals(1, compartidos.size());
        assertEquals("Proyecto A", compartidos.get(0).getTitulo());
    }

    @Test
    void testExistencia() {
        // Guardar tablero
        repositorio.guardar(tablero1);

        // Verificar existencia
        assertTrue(repositorio.existe("tab-1"));
        assertFalse(repositorio.existe("tab-999"));
    }

    @Test
    void testActualizarTablero() {
        // Guardar tablero
        repositorio.guardar(tablero1);

        // Actualizar titulo y descripción
        tablero1.actualizarTitulo("Proyecto A actualizado");
        tablero1.actualizarDescripcion("Nueva descripción");
        repositorio.guardar(tablero1);

        // Verificar actualización
        var resultado = repositorio.obtenerPorId("tab-1");
        assertTrue(resultado.isPresent());
        assertEquals("Proyecto A actualizado", resultado.get().getTitulo());
        assertEquals("Nueva descripción", resultado.get().getDescripcion());
    }

    @Test
    void testTableroConListas() {
        // Crear tablero con listas
        Tablero tablero = new Tablero("tab-3", "Proyecto C", "usuario@example.com");
        
        Lista lista1 = new Lista("list-1", "Por Hacer");
        Lista lista2 = new Lista("list-2", "En Progreso");
        
        tablero.agregarLista(lista1);
        tablero.agregarLista(lista2);

        // Guardar
        repositorio.guardar(tablero);

        // Obtener y verificar
        var resultado = repositorio.obtenerPorId("tab-3");
        assertTrue(resultado.isPresent());
        assertEquals(2, resultado.get().getListas().size());
    }

    @Test
    void testBloquearDesbloquearTablero() {
        // Guardar tablero
        repositorio.guardar(tablero1);

        // Bloquear
        tablero1.bloquear(30);
        repositorio.guardar(tablero1);

        var resultado = repositorio.obtenerPorId("tab-1");
        assertTrue(resultado.isPresent());
        assertTrue(resultado.get().isBloqueado());

        // Desbloquear
        tablero1.desbloquear();
        repositorio.guardar(tablero1);

        resultado = repositorio.obtenerPorId("tab-1");
        assertTrue(resultado.isPresent());
        assertFalse(resultado.get().isBloqueado());
    }

    @Test
    void testEliminarTablero() {
        // Guardar tablero
        repositorio.guardar(tablero1);
        assertTrue(repositorio.existe("tab-1"));

        // Eliminar
        repositorio.eliminar("tab-1");

        // Verificar que fue eliminado
        assertFalse(repositorio.existe("tab-1"));
        assertTrue(repositorio.obtenerPorId("tab-1").isEmpty());
    }

    @Test
    void testPersistenciaFechaDatos() {
        // Guardar tablero
        tablero1.agregarLista(new Lista("list-1", "Lista 1"));
        repositorio.guardar(tablero1);

        var fechaCreacion = tablero1.getFechaCreacion();

        // Actualizar tablero (no debería cambiar fecha creación)
        tablero1.actualizarDescripcion("Nueva descripción");
        repositorio.guardar(tablero1);

        var resultado = repositorio.obtenerPorId("tab-1");
        assertTrue(resultado.isPresent());
        assertEquals(fechaCreacion, resultado.get().getFechaCreacion());
    }
}
