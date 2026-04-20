package pds.app_gestion.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pds.app_gestion.application.dto.*;
import pds.app_gestion.domain.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ServicioPlantillasTest {

    private ServicioPlantillas servicioPlantillas;

    @BeforeEach
    public void setUp() {
        servicioPlantillas = new ServicioPlantillas();
    }

    @Test
    public void testCrearTableroDesdePlantillaSimple() {
        // Crear plantilla simple
        PlantillaEtiquetaYAML etiqueta = new PlantillaEtiquetaYAML("Tag1", "#FF0000");
        PlantillaTarjetaYAML tarjeta = new PlantillaTarjetaYAML("Tarea", "Descripción", "TAREA", 
                                                                Arrays.asList(etiqueta), false);
        PlantillaListaYAML lista = new PlantillaListaYAML("Lista1", null, new ArrayList<>(), 
                                                          Arrays.asList(tarjeta));
        PlantillaTableroYAML plantilla = new PlantillaTableroYAML("Template", "Desc", 
                                                                  Arrays.asList(lista), "1.0");
        
        // Crear tablero desde plantilla
        Tablero tablero = servicioPlantillas.crearTableroDesdePlantilla("tab1", "Mi Tablero", 
                                                                         "user@test.com", plantilla);
        
        assertNotNull(tablero);
        assertEquals("Mi Tablero", tablero.getTitulo());
        assertEquals("user@test.com", tablero.getPropietarioEmail());
        assertEquals(1, tablero.obtenerListas().size());
    }

    @Test
    public void testCrearTableroConTarjetasYEtiquetas() {
        PlantillaEtiquetaYAML etiqueta1 = new PlantillaEtiquetaYAML("Urgente", "#FF0000");
        PlantillaEtiquetaYAML etiqueta2 = new PlantillaEtiquetaYAML("Bug", "#00FF00");
        
        PlantillaTarjetaYAML tarjeta = new PlantillaTarjetaYAML(
            "Arreglar login", "Bug en autenticación", "TAREA",
            Arrays.asList(etiqueta1, etiqueta2), false
        );
        
        PlantillaListaYAML lista = new PlantillaListaYAML("Bugs", 5, new ArrayList<>(), 
                                                           Arrays.asList(tarjeta));
        
        PlantillaTableroYAML plantilla = new PlantillaTableroYAML("QA Tracker", "Seguimiento de bugs", 
                                                                  Arrays.asList(lista), "1.0");
        
        Tablero tablero = servicioPlantillas.crearTableroDesdePlantilla("qa1", "QA Tablero", 
                                                                         "qa@test.com", plantilla);
        
        Lista listaCreada = tablero.obtenerListas().get(0);
        assertEquals("Bugs", listaCreada.getNombre());
        assertEquals(5, listaCreada.getLimiteMaximo().orElse(0).intValue());
        
        Tarjeta tarjetaCreada = listaCreada.getTarjetas().get(0);
        assertEquals("Arreglar login", tarjetaCreada.getTitulo());
        assertEquals(2, tarjetaCreada.getEtiquetas().size());
    }

    @Test
    public void testCrearTableroConMultiplesListas() {
        PlantillaListaYAML lista1 = new PlantillaListaYAML("TODO", null, new ArrayList<>(), new ArrayList<>());
        PlantillaListaYAML lista2 = new PlantillaListaYAML("DOING", 3, new ArrayList<>(), new ArrayList<>());
        PlantillaListaYAML lista3 = new PlantillaListaYAML("DONE", null, new ArrayList<>(), new ArrayList<>());
        
        PlantillaTableroYAML plantilla = new PlantillaTableroYAML(
            "Kanban", "Tablero Kanban", Arrays.asList(lista1, lista2, lista3), "1.0"
        );
        
        Tablero tablero = servicioPlantillas.crearTableroDesdePlantilla("kanban1", "Mi Kanban", 
                                                                         "dev@test.com", plantilla);
        
        List<Lista> listas = tablero.obtenerListas();
        assertEquals(3, listas.size());
        assertEquals("TODO", listas.get(0).getNombre());
        assertEquals("DOING", listas.get(1).getNombre());
        assertEquals("DONE", listas.get(2).getNombre());
        
        assertTrue(listas.get(1).getLimiteMaximo().isPresent());
        assertEquals(3, listas.get(1).getLimiteMaximo().get().intValue());
    }

    @Test
    public void testCrearTableroConPrerequisitos() {
        PlantillaListaYAML lista1 = new PlantillaListaYAML("TODO", null, new ArrayList<>(), new ArrayList<>());
        PlantillaListaYAML lista2 = new PlantillaListaYAML("REVIEW", null, Arrays.asList("TODO"), new ArrayList<>());
        PlantillaListaYAML lista3 = new PlantillaListaYAML("DONE", null, Arrays.asList("REVIEW"), new ArrayList<>());
        
        PlantillaTableroYAML plantilla = new PlantillaTableroYAML(
            "Workflow", "Workflow con requisitos", Arrays.asList(lista1, lista2, lista3), "1.0"
        );
        
        Tablero tablero = servicioPlantillas.crearTableroDesdePlantilla("workflow1", "Workflow", 
                                                                         "manager@test.com", plantilla);
        
        List<Lista> listas = tablero.obtenerListas();
        
        // Lista 1 sin prerequisitos
        assertFalse(listas.get(0).tienePrerequisitos());
        
        // Lista 2 con prerequisito
        assertTrue(listas.get(1).tienePrerequisitos());
        assertEquals(1, listas.get(1).obtenerListasPrevias().size());
        
        // Lista 3 con prerequisito
        assertTrue(listas.get(2).tienePrerequisitos());
        assertEquals(1, listas.get(2).obtenerListasPrevias().size());
    }

    @Test
    public void testCrearTableroConTarjetasCompletadas() {
        PlantillaTarjetaYAML tarjeta1 = new PlantillaTarjetaYAML("Tarea 1", "Completada", "TAREA", 
                                                                 new ArrayList<>(), true);
        PlantillaTarjetaYAML tarjeta2 = new PlantillaTarjetaYAML("Tarea 2", "Pendiente", "TAREA", 
                                                                 new ArrayList<>(), false);
        
        PlantillaListaYAML lista = new PlantillaListaYAML("DONE", null, new ArrayList<>(), 
                                                           Arrays.asList(tarjeta1, tarjeta2));
        
        PlantillaTableroYAML plantilla = new PlantillaTableroYAML("Template", "", Arrays.asList(lista), "1.0");
        
        Tablero tablero = servicioPlantillas.crearTableroDesdePlantilla("tab1", "Test", "user@test.com", plantilla);
        
        List<Tarjeta> tarjetas = tablero.obtenerListas().get(0).getTarjetas();
        assertTrue(tarjetas.get(0).isCompletada());
        assertFalse(tarjetas.get(1).isCompletada());
    }

    @Test
    public void testObtenerPlantillasEjemplo() {
        List<PlantillaTableroYAML> plantillas = servicioPlantillas.obtenerPlantillasEjemplo();
        
        assertNotNull(plantillas);
        assertTrue(plantillas.size() > 0);
        
        boolean tieneKanban = plantillas.stream()
                .anyMatch(p -> p.getTitulo().contains("Kanban"));
        assertTrue(tieneKanban);
    }

    @Test
    public void testExportarTableroComoYAML() {
        Tablero tablero = new Tablero("tab1", "Test Board", "test@test.com");
        Lista lista = new Lista("list1", "Lista Test");
        Tarjeta tarjeta = new Tarjeta("tar1", "Tarjeta Test", "Descripción", Tarjeta.TipoTarjeta.TAREA);
        
        lista.agregarTarjeta(tarjeta);
        tablero.agregarLista(lista);
        
        String yaml = servicioPlantillas.exportarTableroComoYAML(tablero);
        
        assertNotNull(yaml);
        assertTrue(yaml.length() > 0);
        assertTrue(yaml.contains("Test Board") || yaml.contains("Lista Test") || yaml.contains("Tarjeta Test"));
    }
}

