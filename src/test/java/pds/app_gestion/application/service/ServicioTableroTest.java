package pds.app_gestion.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pds.app_gestion.application.dto.*;
import pds.app_gestion.application.exception.ErrorValidacionException;
import pds.app_gestion.application.exception.PermisoNegadoException;
import pds.app_gestion.application.exception.RecursoNoEncontradoException;
import pds.app_gestion.domain.*;
import pds.app_gestion.infrastructure.cache.CacheService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el servicio de aplicación ServicioTablero.
 */
@ExtendWith(MockitoExtension.class)
public class ServicioTableroTest {

    @Mock
    private RepositorioTablero repositorioTablero;

    @Mock
    private CacheService cacheService;

    @Mock
    private ServicioPlantillas servicioPlantillas;

    private ServicioTablero servicioTablero;

    @BeforeEach
    void setUp() {
        servicioTablero = new ServicioTablero(repositorioTablero, cacheService, servicioPlantillas);
    }

    @Test
    void crearTableroExitosamente() {
        CrearTableroRequest request = CrearTableroRequest.builder()
            .titulo("Mi tablero")
            .descripcion("Descripción del tablero")
            .propietarioEmail("adrian@example.com")
            .build();

        TableroResponse response = servicioTablero.crearTablero(request);

        assertNotNull(response);
        assertEquals("Mi tablero", response.getTitulo());
        assertEquals("Descripción del tablero", response.getDescripcion());
        assertEquals("adrian@example.com", response.getPropietarioEmail());
        assertFalse(response.isBloqueado());
        
        verify(repositorioTablero, times(1)).guardar(any(Tablero.class));
    }

    @Test
    void importarTableroDesdePlantillaExitosamente() {
        Tablero tablero = new Tablero("1", "Tablero plantilla", "adrian@example.com");
        Lista lista = new Lista("lista-1", "Por hacer");
        lista.agregarTarjeta(new Tarjeta("tarjeta-1", "Tarea importada", ""));
        tablero.agregarLista(lista);

        TableroResponse response = servicioTablero.importarTableroDesdePlantilla(tablero, "adrian@example.com");

        assertEquals("Tablero plantilla", response.getTitulo());
        assertEquals("adrian@example.com", response.getPropietarioEmail());
        assertEquals(1, response.getTotalTarjetas());
        verify(repositorioTablero).guardar(tablero);
    }

    @Test
    void crearTableroConTituloVacioThrows() {
        CrearTableroRequest request = CrearTableroRequest.builder()
            .titulo("")
            .descripcion("Descripción")
            .propietarioEmail("adrian@example.com")
            .build();

        assertThrows(ErrorValidacionException.class, () -> {
            servicioTablero.crearTablero(request);
        });
    }

    @Test
    void crearTableroConEmailInvalidoThrows() {
        CrearTableroRequest request = CrearTableroRequest.builder()
            .titulo("Tablero")
            .descripcion("Descripción")
            .propietarioEmail("email-invalido")
            .build();

        assertThrows(ErrorValidacionException.class, () -> {
            servicioTablero.crearTablero(request);
        });
    }

    @Test
    void obtenerTableroQueNoExisteThrows() {
        when(repositorioTablero.obtenerPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> {
            servicioTablero.obtenerTablero("no-existe", "adrian@example.com");
        });
    }

    @Test
    void obtenerTableroSinPermisoThrows() {
        Tablero tablero = new Tablero("1", "Tablero", "propietario@example.com");
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        assertThrows(PermisoNegadoException.class, () -> {
            servicioTablero.obtenerTablero("1", "otro@example.com");
        });
    }

    @Test
    void obtenerTableroExitosamente() {
        Tablero tablero = new Tablero("1", "Tablero", "adrian@example.com");
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        TableroResponse response = servicioTablero.obtenerTablero("1", "adrian@example.com");

        assertNotNull(response);
        assertEquals("1", response.getId());
        assertEquals("Tablero", response.getTitulo());
    }

    @Test
    void exportarTableroComoPlantillaExitosamente() {
        Tablero tablero = new Tablero("1", "Tablero", "adrian@example.com");
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));
        when(servicioPlantillas.exportarTableroComoYAML(tablero)).thenReturn("titulo: Tablero");

        String yaml = servicioTablero.exportarTableroComoPlantilla("1", "adrian@example.com");

        assertEquals("titulo: Tablero", yaml);
        verify(servicioPlantillas).exportarTableroComoYAML(tablero);
    }

    @Test
    void exportarTableroComoPlantillaSinPermisoThrows() {
        Tablero tablero = new Tablero("1", "Tablero", "propietario@example.com");
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        assertThrows(PermisoNegadoException.class, () ->
            servicioTablero.exportarTableroComoPlantilla("1", "otro@example.com")
        );
    }

    @Test
    void obtenerListasExitosamente() {
        Tablero tablero = new Tablero("1", "Tablero", "adrian@example.com");
        tablero.agregarLista(new Lista("lista-1", "Pendientes"));
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        var listas = servicioTablero.obtenerListas("1", "adrian@example.com");

        assertEquals(1, listas.size());
        assertEquals("Pendientes", listas.get(0).getNombre());
    }

    @Test
    void actualizarTableroTituloYDescripcionExitosamente() {
        Tablero tablero = new Tablero("1", "Tablero", "adrian@example.com");
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        ActualizarTableroRequest request = ActualizarTableroRequest.builder()
            .titulo("Tablero actualizado")
            .descripcion("Nueva descripción")
            .build();

        TableroResponse response = servicioTablero.actualizarTablero("1", "adrian@example.com", request);

        assertEquals("Tablero actualizado", response.getTitulo());
        assertEquals("Nueva descripción", response.getDescripcion());
        verify(repositorioTablero).guardar(tablero);
    }

    @Test
    void eliminarTableroExitosamente() {
        Tablero tablero = new Tablero("1", "Tablero", "adrian@example.com");
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        servicioTablero.eliminarTablero("1", "adrian@example.com");

        verify(repositorioTablero).eliminar("1");
    }

    @Test
    void compartirTableroExitosamente() {
        Tablero tablero = new Tablero("1", "Tablero", "propietario@example.com");
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        servicioTablero.compartirTablero("1", "propietario@example.com", "usuario@example.com");

        assertTrue(tablero.tieneAcceso("usuario@example.com"));
        verify(repositorioTablero, times(1)).guardar(tablero);
    }

    @Test
    void compartirTableroSinPermisoThrows() {
        Tablero tablero = new Tablero("1", "Tablero", "propietario@example.com");
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        assertThrows(PermisoNegadoException.class, () -> {
            servicioTablero.compartirTablero("1", "otro@example.com", "usuario@example.com");
        });
    }

    @Test
    void bloquearTableroExitosamente() {
        Tablero tablero = new Tablero("1", "Tablero", "propietario@example.com");
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        BloquearTableroRequest request = new BloquearTableroRequest(10);
        servicioTablero.bloquearTablero("1", "propietario@example.com", request);

        assertTrue(tablero.isBloqueado());
        verify(repositorioTablero, times(1)).guardar(tablero);
    }

    @Test
    void agregarListaExitosamente() {
        Tablero tablero = new Tablero("1", "Tablero", "adrian@example.com");
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        CrearListaRequest request = CrearListaRequest.builder()
            .nombre("Mi lista")
            .limiteMaximo(null)
            .build();

        ListaResponse response = servicioTablero.agregarLista("1", "adrian@example.com", request);

        assertNotNull(response);
        assertEquals("Mi lista", response.getNombre());
        assertEquals(1, tablero.getListas().size());
        verify(repositorioTablero, times(1)).guardar(tablero);
    }

    @Test
    void agregarListaATableroBloqueoThrows() {
        Tablero tablero = new Tablero("1", "Tablero", "adrian@example.com");
        tablero.bloquear(10);
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        CrearListaRequest request = CrearListaRequest.builder()
            .nombre("Mi lista")
            .build();

        assertThrows(Exception.class, () -> {
            servicioTablero.agregarLista("1", "adrian@example.com", request);
        });
    }

    @Test
    void eliminarListaExitosamente() {
        Tablero tablero = new Tablero("1", "Tablero", "adrian@example.com");
        Lista lista = new Lista("lista-1", "Mi lista");
        tablero.agregarLista(lista);
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        servicioTablero.eliminarLista("1", "lista-1", "adrian@example.com");

        assertTrue(tablero.getListas().isEmpty());
        verify(repositorioTablero).guardar(tablero);
    }
}
