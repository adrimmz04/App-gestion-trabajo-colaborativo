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

    private ServicioTablero servicioTablero;

    @BeforeEach
    void setUp() {
        servicioTablero = new ServicioTablero(repositorioTablero);
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
}
