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

    @Test
    void obtenerTarjetasPorEtiquetasUnicaExitosamente() {
        Tablero tablero = new Tablero("1", "Tablero", "adrian@example.com");
        Lista lista = new Lista("lista-1", "Por hacer");
        
        Tarjeta tarjeta1 = new Tarjeta("t1", "Tarea 1", "");
        tarjeta1.agregarEtiqueta(new Etiqueta("Urgente", "#FF0000"));
        
        Tarjeta tarjeta2 = new Tarjeta("t2", "Tarea 2", "");
        tarjeta2.agregarEtiqueta(new Etiqueta("Baja", "#00FF00"));
        
        Tarjeta tarjeta3 = new Tarjeta("t3", "Tarea 3", "");
        tarjeta3.agregarEtiqueta(new Etiqueta("Urgente", "#FF0000"));
        
        lista.agregarTarjeta(tarjeta1);
        lista.agregarTarjeta(tarjeta2);
        lista.agregarTarjeta(tarjeta3);
        tablero.agregarLista(lista);
        
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        var etiquetas = new java.util.HashSet<String>();
        etiquetas.add("Urgente");
        
        var response = servicioTarjeta.obtenerTarjetasPorEtiquetas("1", "lista-1", "adrian@example.com", etiquetas);

        assertEquals(2, response.size());
        assertTrue(response.stream().anyMatch(t -> t.getId().equals("t1")));
        assertTrue(response.stream().anyMatch(t -> t.getId().equals("t3")));
    }

    @Test
    void obtenerTarjetasPorMultiplesEtiquetasExitosamente() {
        Tablero tablero = new Tablero("1", "Tablero", "adrian@example.com");
        Lista lista = new Lista("lista-1", "Por hacer");
        
        Tarjeta tarjeta1 = new Tarjeta("t1", "Tarea 1", "");
        tarjeta1.agregarEtiqueta(new Etiqueta("Urgente", "#FF0000"));
        tarjeta1.agregarEtiqueta(new Etiqueta("Backend", "#0000FF"));
        
        Tarjeta tarjeta2 = new Tarjeta("t2", "Tarea 2", "");
        tarjeta2.agregarEtiqueta(new Etiqueta("Urgente", "#FF0000"));
        
        Tarjeta tarjeta3 = new Tarjeta("t3", "Tarea 3", "");
        tarjeta3.agregarEtiqueta(new Etiqueta("Backend", "#0000FF"));
        
        lista.agregarTarjeta(tarjeta1);
        lista.agregarTarjeta(tarjeta2);
        lista.agregarTarjeta(tarjeta3);
        tablero.agregarLista(lista);
        
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        var etiquetas = new java.util.HashSet<String>();
        etiquetas.add("Urgente");
        etiquetas.add("Backend");
        
        var response = servicioTarjeta.obtenerTarjetasPorEtiquetas("1", "lista-1", "adrian@example.com", etiquetas);

        assertEquals(1, response.size());
        assertEquals("t1", response.get(0).getId());
    }

    @Test
    void obtenerTodasLasTarjetasExitosamente() {
        Tablero tablero = new Tablero("1", "Tablero", "adrian@example.com");
        Lista lista = new Lista("lista-1", "Por hacer");
        
        Tarjeta tarjeta1 = new Tarjeta("t1", "Tarea 1", "");
        Tarjeta tarjeta2 = new Tarjeta("t2", "Tarea 2", "");
        
        lista.agregarTarjeta(tarjeta1);
        lista.agregarTarjeta(tarjeta2);
        tablero.agregarLista(lista);
        
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        var response = servicioTarjeta.obtenerTodasLasTarjetas("1", "lista-1", "adrian@example.com");

        assertEquals(2, response.size());
    }

    @Test
    void obtenerEtiquetasDeLista() {
        Tablero tablero = new Tablero("1", "Tablero", "adrian@example.com");
        Lista lista = new Lista("lista-1", "Por hacer");
        
        Tarjeta tarjeta1 = new Tarjeta("t1", "Tarea 1", "");
        tarjeta1.agregarEtiqueta(new Etiqueta("Urgente", "#FF0000"));
        tarjeta1.agregarEtiqueta(new Etiqueta("Backend", "#0000FF"));
        
        Tarjeta tarjeta2 = new Tarjeta("t2", "Tarea 2", "");
        tarjeta2.agregarEtiqueta(new Etiqueta("Urgente", "#FF0000"));
        
        lista.agregarTarjeta(tarjeta1);
        lista.agregarTarjeta(tarjeta2);
        tablero.agregarLista(lista);
        
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        var response = servicioTarjeta.obtenerEtiquetasDeLista("1", "lista-1", "adrian@example.com");

        assertEquals(2, response.size());
        assertTrue(response.stream().anyMatch(e -> e.getNombre().equals("Urgente")));
        assertTrue(response.stream().anyMatch(e -> e.getNombre().equals("Backend")));
    }
}
