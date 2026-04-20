package pds.app_gestion.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pds.app_gestion.application.dto.*;
import pds.app_gestion.application.exception.ErrorOperacionDominioException;
import pds.app_gestion.application.exception.PermisoNegadoException;
import pds.app_gestion.application.exception.RecursoNoEncontradoException;
import pds.app_gestion.domain.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el servicio de aplicación ServicioTarjeta.
 */
@ExtendWith(MockitoExtension.class)
public class ServicioTarjetaTest {

    @Mock
    private RepositorioTablero repositorioTablero;

    private ServicioTarjeta servicioTarjeta;

    @BeforeEach
    void setUp() {
        servicioTarjeta = new ServicioTarjeta(repositorioTablero);
    }

    @Test
    void crearTarjetaExitosamente() {
        Tablero tablero = new Tablero("1", "Tablero", "adrian@example.com");
        Lista lista = new Lista("lista-1", "Por hacer");
        tablero.agregarLista(lista);
        
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        CrearTarjetaRequest request = CrearTarjetaRequest.builder()
            .titulo("Nueva tarea")
            .descripcion("Descripción")
            .tipo("TAREA")
            .build();

        TarjetaResponse response = servicioTarjeta.crearTarjeta("1", "lista-1", "adrian@example.com", request);

        assertNotNull(response);
        assertEquals("Nueva tarea", response.getTitulo());
        assertEquals("Descripción", response.getDescripcion());
        assertEquals("TAREA", response.getTipo());
        assertFalse(response.isCompletada());
        
        verify(repositorioTablero, times(1)).guardar(tablero);
    }

    @Test
    void crearTarjetaEnTableroBloqueoThrows() {
        Tablero tablero = new Tablero("1", "Tablero", "adrian@example.com");
        Lista lista = new Lista("lista-1", "Por hacer");
        tablero.agregarLista(lista);
        tablero.bloquear(10);
        
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        CrearTarjetaRequest request = CrearTarjetaRequest.builder()
            .titulo("Nueva tarea")
            .descripcion("Descripción")
            .build();

        assertThrows(ErrorOperacionDominioException.class, () -> {
            servicioTarjeta.crearTarjeta("1", "lista-1", "adrian@example.com", request);
        });
    }

    @Test
    void marcarTarjetaComoCompletadaExitosamente() {
        Tablero tablero = new Tablero("1", "Tablero", "adrian@example.com");
        Lista lista = new Lista("lista-1", "Por hacer");
        Tarjeta tarjeta = new Tarjeta("tarjeta-1", "Tarea", "Descripción");
        lista.agregarTarjeta(tarjeta);
        tablero.agregarLista(lista);
        
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        TarjetaResponse response = servicioTarjeta.marcarComoCompletada("1", "lista-1", "tarjeta-1", "adrian@example.com");

        assertNotNull(response);
        assertTrue(response.isCompletada());
        assertNotNull(response.getFechaCompletacion());
        verify(repositorioTablero, times(1)).guardar(tablero);
    }

    @Test
    void agregarEtiquetaAtatjezaExitosamente() {
        Tablero tablero = new Tablero("1", "Tablero", "adrian@example.com");
        Lista lista = new Lista("lista-1", "Por hacer");
        Tarjeta tarjeta = new Tarjeta("tarjeta-1", "Tarea", "Descripción");
        lista.agregarTarjeta(tarjeta);
        tablero.agregarLista(lista);
        
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        CrearEtiquetaRequest request = CrearEtiquetaRequest.builder()
            .nombre("Urgente")
            .color("#FF5733")
            .build();

        TarjetaResponse response = servicioTarjeta.agregarEtiqueta("1", "lista-1", "tarjeta-1", "adrian@example.com", request);

        assertNotNull(response);
        assertEquals(1, response.getEtiquetas().size());
        verify(repositorioTablero, times(1)).guardar(tablero);
    }

    @Test
    void crearTarjetaSinPermisoThrows() {
        Tablero tablero = new Tablero("1", "Tablero", "propietario@example.com");
        Lista lista = new Lista("lista-1", "Por hacer");
        tablero.agregarLista(lista);
        
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        CrearTarjetaRequest request = CrearTarjetaRequest.builder()
            .titulo("Nueva tarea")
            .build();

        assertThrows(PermisoNegadoException.class, () -> {
            servicioTarjeta.crearTarjeta("1", "lista-1", "otro@example.com", request);
        });
    }

    @Test
    void crearTarjetaConTituloVacioThrows() {
        CrearTarjetaRequest request = CrearTarjetaRequest.builder()
            .titulo("")
            .build();

        assertThrows(Exception.class, () -> {
            servicioTarjeta.crearTarjeta("1", "lista-1", "adrian@example.com", request);
        });
    }
}
