package pds.app_gestion.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pds.app_gestion.application.dto.ConfigurarReglasListaRequest;
import pds.app_gestion.application.exception.ErrorOperacionDominioException;
import pds.app_gestion.application.exception.PermisoNegadoException;
import pds.app_gestion.application.exception.RecursoNoEncontradoException;
import pds.app_gestion.domain.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el servicio de aplicación ServicioLista.
 */
@ExtendWith(MockitoExtension.class)
public class ServicioListaTest {

    @Mock
    private RepositorioTablero repositorioTablero;

    private ServicioLista servicioLista;

    @BeforeEach
    void setUp() {
        servicioLista = new ServicioLista(repositorioTablero);
    }

    @Test
    void configurarLimiteMáximoExitosamente() {
        Tablero tablero = new Tablero("1", "Tablero", "adrian@example.com");
        Lista lista = new Lista("lista-1", "Por hacer");
        tablero.agregarLista(lista);
        
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        ConfigurarReglasListaRequest request = ConfigurarReglasListaRequest.builder()
            .limiteMaximo(5)
            .build();

        var response = servicioLista.configurarReglas("1", "lista-1", "adrian@example.com", request);

        assertEquals(5, response.getLimiteMaximo());
        assertEquals(0, response.getCantidadActual());
        verify(repositorioTablero, times(1)).guardar(tablero);
    }

    @Test
    void configurarListasPreviasExitosamente() {
        Tablero tablero = new Tablero("1", "Tablero", "adrian@example.com");
        Lista listaPrevia = new Lista("lista-previa", "Hecho");
        Lista listaActual = new Lista("lista-actual", "En progreso");
        tablero.agregarLista(listaPrevia);
        tablero.agregarLista(listaActual);
        
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        ConfigurarReglasListaRequest request = ConfigurarReglasListaRequest.builder()
            .listasPrevias(Arrays.asList("lista-previa"))
            .build();

        var response = servicioLista.configurarReglas("1", "lista-actual", "adrian@example.com", request);

        assertTrue(response.getListasPrevias().contains("lista-previa"));
        verify(repositorioTablero, times(1)).guardar(tablero);
    }

    @Test
    void moverTarjetaSinRequisitosExitosamente() {
        Tablero tablero = new Tablero("1", "Tablero", "adrian@example.com");
        Lista listaTodo = new Lista("lista-todo", "Por hacer");
        Lista listaHecho = new Lista("lista-hecho", "Hecho");
        
        Tarjeta tarjeta = new Tarjeta("tarjeta-1", "Tarea 1", "");
        listaTodo.agregarTarjeta(tarjeta);
        
        tablero.agregarLista(listaTodo);
        tablero.agregarLista(listaHecho);
        
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        servicioLista.moverTarjeta("1", "lista-todo", "lista-hecho", "tarjeta-1", "adrian@example.com");

        assertTrue(listaHecho.obtenerTarjeta("tarjeta-1").isPresent());
        assertTrue(listaTodo.obtenerTarjeta("tarjeta-1").isEmpty());
        verify(repositorioTablero, times(1)).guardar(tablero);
    }

    @Test
    void moverTarjetaQueNoaCumpleRequisitosThrows() {
        Tablero tablero = new Tablero("1", "Tablero", "adrian@example.com");
        Lista listaPrevia = new Lista("lista-previa", "Prerrequisito");
        Lista listaDestino = new Lista("lista-destino", "Destino");
        listaDestino.agregarListaPrevia("lista-previa");
        
        Tarjeta tarjeta = new Tarjeta("tarjeta-1", "Tarea 1", "");
        // La tarjeta NO está completada
        listaPrevia.agregarTarjeta(tarjeta);
        
        tablero.agregarLista(listaPrevia);
        tablero.agregarLista(listaDestino);
        
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        // Debe lanzar excepción porque la tarjeta no está completada
        assertThrows(ErrorOperacionDominioException.class, () -> {
            servicioLista.moverTarjeta("1", "lista-previa", "lista-destino", "tarjeta-1", "adrian@example.com");
        });
    }

    @Test
    void moverTarjetaCompletadaConRequisitosExitosamente() {
        Tablero tablero = new Tablero("1", "Tablero", "adrian@example.com");
        Lista listaPrevia = new Lista("lista-previa", "Prerrequisito");
        Lista listaDestino = new Lista("lista-destino", "Destino");
        listaDestino.agregarListaPrevia("lista-previa");
        
        Tarjeta tarjeta = new Tarjeta("tarjeta-1", "Tarea 1", "");
        tarjeta.marcarComoCompletada();  // Completar la tarjeta
        listaPrevia.agregarTarjeta(tarjeta);
        
        tablero.agregarLista(listaPrevia);
        tablero.agregarLista(listaDestino);
        
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        // Debe permitir el movimiento porque la tarjeta está completada
        servicioLista.moverTarjeta("1", "lista-previa", "lista-destino", "tarjeta-1", "adrian@example.com");

        assertTrue(listaDestino.obtenerTarjeta("tarjeta-1").isPresent());
        assertTrue(listaPrevia.obtenerTarjeta("tarjeta-1").isEmpty());
        verify(repositorioTablero, times(1)).guardar(tablero);
    }

    @Test
    void moverTarjetaExcedeLimiteMáximoThrows() {
        Tablero tablero = new Tablero("1", "Tablero", "adrian@example.com");
        Lista listaOrigen = new Lista("lista-origen", "Origen");
        Lista listaDestino = new Lista("lista-destino", "Destino");
        listaDestino.establecerLimiteMaximo(1);
        
        Tarjeta tarjeta1 = new Tarjeta("t1", "Tarea 1", "");
        Tarjeta tarjeta2 = new Tarjeta("t2", "Tarea 2", "");
        
        listaDestino.agregarTarjeta(tarjeta1);
        listaOrigen.agregarTarjeta(tarjeta2);
        
        tablero.agregarLista(listaOrigen);
        tablero.agregarLista(listaDestino);
        
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        assertThrows(ErrorOperacionDominioException.class, () -> {
            servicioLista.moverTarjeta("1", "lista-origen", "lista-destino", "t2", "adrian@example.com");
        });
    }

    @Test
    void moverTarjetaEnTableroBloquedoExitosamente() {
        Tablero tablero = new Tablero("1", "Tablero", "adrian@example.com");
        Lista listaOrigen = new Lista("lista-origen", "Origen");
        Lista listaDestino = new Lista("lista-destino", "Destino");
        
        Tarjeta tarjeta = new Tarjeta("tarjeta-1", "Tarea 1", "");
        listaOrigen.agregarTarjeta(tarjeta);
        
        tablero.agregarLista(listaOrigen);
        tablero.agregarLista(listaDestino);
        tablero.bloquear(10);
        
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        servicioLista.moverTarjeta("1", "lista-origen", "lista-destino", "tarjeta-1", "adrian@example.com");

        assertTrue(listaDestino.obtenerTarjeta("tarjeta-1").isPresent());
        assertTrue(listaOrigen.obtenerTarjeta("tarjeta-1").isEmpty());
        verify(repositorioTablero, times(1)).guardar(tablero);
    }

    @Test
    void obtenerReglasListaExitosamente() {
        Tablero tablero = new Tablero("1", "Tablero", "adrian@example.com");
        Lista lista = new Lista("lista-1", "Por hacer");
        lista.establecerLimiteMaximo(10);
        lista.agregarListaPrevia("lista-previa");
        
        Tarjeta tarjeta1 = new Tarjeta("t1", "Tarea 1", "");
        Tarjeta tarjeta2 = new Tarjeta("t2", "Tarea 2", "");
        lista.agregarTarjeta(tarjeta1);
        lista.agregarTarjeta(tarjeta2);
        
        tablero.agregarLista(lista);
        
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        var response = servicioLista.obtenerReglas("1", "lista-1", "adrian@example.com");

        assertEquals("Por hacer", response.getNombre());
        assertEquals(10, response.getLimiteMaximo());
        assertEquals(2, response.getCantidadActual());
        assertTrue(response.getListasPrevias().contains("lista-previa"));
    }

    @Test
    void configurarReglaSinPermisosThrows() {
        Tablero tablero = new Tablero("1", "Tablero", "propietario@example.com");
        Lista lista = new Lista("lista-1", "Por hacer");
        tablero.agregarLista(lista);
        
        when(repositorioTablero.obtenerPorId("1")).thenReturn(Optional.of(tablero));

        ConfigurarReglasListaRequest request = ConfigurarReglasListaRequest.builder()
            .limiteMaximo(5)
            .build();

        assertThrows(PermisoNegadoException.class, () -> {
            servicioLista.configurarReglas("1", "lista-1", "otro@example.com", request);
        });
    }
}
