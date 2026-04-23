package pds.app_gestion.ui.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import pds.app_gestion.application.dto.*;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests para ControladorTarjeta.
 * 
 * Prueba todos los endpoints REST relacionados con tarjetas:
 * - Crear, actualizar tarjetas
 * - Marcar como completada/no completada
 * - Gestionar etiquetas
 * - Filtrar por etiquetas
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Integration Tests - ControladorTarjeta")
class ControladorTarjetaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String idTablero;
    private String idLista;
    private String emailPropietario = "propietario@test.com";

    @BeforeEach
    void setUp() throws Exception {
        // Crear tablero
        CrearTableroRequest tableroRequest = new CrearTableroRequest(
            "Tablero Test",
            "Descripción",
            emailPropietario
        );
        
        MvcResult tableroResult = mockMvc.perform(post("/api/v1/tableros")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(tableroRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        String jsonResponse = tableroResult.getResponse().getContentAsString();
        TableroResponse tableroResponse = objectMapper.readValue(jsonResponse, TableroResponse.class);
        idTablero = tableroResponse.getId();

        // Crear lista
        CrearListaRequest listaRequest = new CrearListaRequest("Lista Test", null);
        MvcResult listaResult = mockMvc.perform(post("/api/v1/tableros/{id}/listas", idTablero)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(listaRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        String listaJson = listaResult.getResponse().getContentAsString();
        ListaResponse listaResponse = objectMapper.readValue(listaJson, ListaResponse.class);
        idLista = listaResponse.getId();
    }

    @Test
    @DisplayName("Crear tarjeta tipo TAREA")
    void testCrearTarjetaTarea() throws Exception {
        CrearTarjetaRequest request = new CrearTarjetaRequest();
        request.setTitulo("Implementar feature");
        request.setDescripcion("Desarrollar nueva funcionalidad");
        request.setTipo("TAREA");

        mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas", idTablero, idLista)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.titulo").value("Implementar feature"))
            .andExpect(jsonPath("$.tipo").value("TAREA"))
            .andExpect(jsonPath("$.completada").value(false));
    }

    @Test
    @DisplayName("Crear tarjeta tipo CHECKLIST")
    void testCrearTarjetaChecklist() throws Exception {
        CrearTarjetaRequest request = new CrearTarjetaRequest();
        request.setTitulo("Setup proyecto");
        request.setDescripcion("Checklist de configuración");
        request.setTipo("CHECKLIST");

        mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas", idTablero, idLista)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tipo").value("CHECKLIST"));
    }

    @Test
    @DisplayName("Actualizar descripción de tarjeta")
    void testActualizarTarjeta() throws Exception {
        // Crear tarjeta
        CrearTarjetaRequest crearRequest = new CrearTarjetaRequest();
        crearRequest.setTitulo("Tarjeta Test");
        crearRequest.setDescripcion("Descripción original");
        crearRequest.setTipo("TAREA");

        MvcResult crearResult = mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas", idTablero, idLista)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(crearRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        String jsonResponse = crearResult.getResponse().getContentAsString();
        TarjetaResponse tarjetaResponse = objectMapper.readValue(jsonResponse, TarjetaResponse.class);
        String idTarjeta = tarjetaResponse.getId();

        // Actualizar descripción
        ActualizarTarjetaRequest updateRequest = new ActualizarTarjetaRequest();
        updateRequest.setDescripcion("Nueva descripción");

        mockMvc.perform(put("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{idTarjeta}", 
                idTablero, idLista, idTarjeta)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.descripcion").value("Nueva descripción"));
    }

    @Test
    @DisplayName("Marcar tarjeta como completada")
    void testMarcarComoCompletada() throws Exception {
        // Crear tarjeta
        CrearTarjetaRequest crearRequest = new CrearTarjetaRequest();
        crearRequest.setTitulo("Tarjeta Test");
        crearRequest.setDescripcion("Descripción");
        crearRequest.setTipo("TAREA");

        MvcResult crearResult = mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas", idTablero, idLista)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(crearRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        String jsonResponse = crearResult.getResponse().getContentAsString();
        TarjetaResponse tarjetaResponse = objectMapper.readValue(jsonResponse, TarjetaResponse.class);
        String idTarjeta = tarjetaResponse.getId();

        // Marcar como completada
        mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{idTarjeta}/completar",
                idTablero, idLista, idTarjeta)
            .param("emailUsuario", emailPropietario))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completada").value(true));
    }

    @Test
    @DisplayName("Marcar tarjeta como no completada")
    void testMarcarComoNoCompletada() throws Exception {
        // Crear tarjeta
        CrearTarjetaRequest crearRequest = new CrearTarjetaRequest();
        crearRequest.setTitulo("Tarjeta Test");
        crearRequest.setDescripcion("Descripción");
        crearRequest.setTipo("TAREA");

        MvcResult crearResult = mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas", idTablero, idLista)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(crearRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        String jsonResponse = crearResult.getResponse().getContentAsString();
        TarjetaResponse tarjetaResponse = objectMapper.readValue(jsonResponse, TarjetaResponse.class);
        String idTarjeta = tarjetaResponse.getId();

        // Marcar como completada
        mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{idTarjeta}/completar",
                idTablero, idLista, idTarjeta)
            .param("emailUsuario", emailPropietario))
            .andExpect(status().isOk());

        // Marcar como no completada
        mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{idTarjeta}/descompletar",
                idTablero, idLista, idTarjeta)
            .param("emailUsuario", emailPropietario))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completada").value(false));
    }

    @Test
    @DisplayName("Marcar tarjeta como completada la mueve a una lista Hecho si existe")
    void testMarcarComoCompletadaYMoverAListaHecho() throws Exception {
        CrearListaRequest listaHechoRequest = new CrearListaRequest("Hecho", null);
        MvcResult listaHechoResult = mockMvc.perform(post("/api/v1/tableros/{id}/listas", idTablero)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(listaHechoRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        ListaResponse listaHecho = objectMapper.readValue(
            listaHechoResult.getResponse().getContentAsString(),
            ListaResponse.class
        );

        CrearTarjetaRequest crearRequest = new CrearTarjetaRequest();
        crearRequest.setTitulo("Tarjeta Test");
        crearRequest.setDescripcion("Descripción");
        crearRequest.setTipo("TAREA");

        MvcResult crearResult = mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas", idTablero, idLista)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(crearRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        TarjetaResponse tarjetaResponse = objectMapper.readValue(
            crearResult.getResponse().getContentAsString(),
            TarjetaResponse.class
        );

        mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{idTarjeta}/completar",
                idTablero, idLista, tarjetaResponse.getId())
            .param("emailUsuario", emailPropietario))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completada").value(true));

        mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{idTarjeta}/descompletar",
                idTablero, idLista, tarjetaResponse.getId())
            .param("emailUsuario", emailPropietario))
            .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{idTarjeta}/descompletar",
                idTablero, listaHecho.getId(), tarjetaResponse.getId())
            .param("emailUsuario", emailPropietario))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completada").value(false));
    }

    @Test
    @DisplayName("Agregar etiqueta a tarjeta")
    void testAgregarEtiqueta() throws Exception {
        // Crear tarjeta
        CrearTarjetaRequest crearRequest = new CrearTarjetaRequest();
        crearRequest.setTitulo("Tarjeta Test");
        crearRequest.setDescripcion("Descripción");
        crearRequest.setTipo("TAREA");

        MvcResult crearResult = mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas", idTablero, idLista)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(crearRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        String jsonResponse = crearResult.getResponse().getContentAsString();
        TarjetaResponse tarjetaResponse = objectMapper.readValue(jsonResponse, TarjetaResponse.class);
        String idTarjeta = tarjetaResponse.getId();

        // Agregar etiqueta
        CrearEtiquetaRequest etiquetaRequest = new CrearEtiquetaRequest();
        etiquetaRequest.setNombre("bug");
        etiquetaRequest.setColor("#FF0000");

        mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{idTarjeta}/etiquetas",
                idTablero, idLista, idTarjeta)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(etiquetaRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.etiquetas", hasSize(1)))
            .andExpect(jsonPath("$.etiquetas[0].nombre").value("bug"));
    }

    @Test
    @DisplayName("Agregar múltiples etiquetas a tarjeta")
    void testAgregarMultiplesEtiquetas() throws Exception {
        // Crear tarjeta
        CrearTarjetaRequest crearRequest = new CrearTarjetaRequest();
        crearRequest.setTitulo("Tarjeta Test");
        crearRequest.setDescripcion("Descripción");
        crearRequest.setTipo("TAREA");

        MvcResult crearResult = mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas", idTablero, idLista)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(crearRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        String jsonResponse = crearResult.getResponse().getContentAsString();
        TarjetaResponse tarjetaResponse = objectMapper.readValue(jsonResponse, TarjetaResponse.class);
        String idTarjeta = tarjetaResponse.getId();

        // Agregar primera etiqueta
        CrearEtiquetaRequest etiquetaRequest1 = new CrearEtiquetaRequest();
        etiquetaRequest1.setNombre("bug");
        etiquetaRequest1.setColor("#FF0000");
        mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{idTarjeta}/etiquetas",
                idTablero, idLista, idTarjeta)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(etiquetaRequest1)))
            .andExpect(status().isCreated());

        // Agregar segunda etiqueta
        CrearEtiquetaRequest etiquetaRequest2 = new CrearEtiquetaRequest();
        etiquetaRequest2.setNombre("urgente");
        etiquetaRequest2.setColor("#00FF00");
        mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{idTarjeta}/etiquetas",
                idTablero, idLista, idTarjeta)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(etiquetaRequest2)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.etiquetas", hasSize(2)));
    }

    @Test
    @DisplayName("Eliminar tarjeta")
    void testEliminarTarjeta() throws Exception {
        CrearTarjetaRequest crearRequest = new CrearTarjetaRequest();
        crearRequest.setTitulo("Tarjeta Test");
        crearRequest.setDescripcion("Descripción");
        crearRequest.setTipo("TAREA");

        MvcResult crearResult = mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas", idTablero, idLista)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(crearRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        TarjetaResponse tarjetaResponse = objectMapper.readValue(
            crearResult.getResponse().getContentAsString(),
            TarjetaResponse.class
        );

        mockMvc.perform(delete("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{idTarjeta}",
                idTablero, idLista, tarjetaResponse.getId())
            .param("emailUsuario", emailPropietario))
            .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{idTarjeta}/completar",
                idTablero, idLista, tarjetaResponse.getId())
            .param("emailUsuario", emailPropietario))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Mover tarjeta a otra lista")
    void testMoverTarjeta() throws Exception {
        CrearListaRequest listaRequest = new CrearListaRequest("Lista Destino", null);
        MvcResult listaResult = mockMvc.perform(post("/api/v1/tableros/{id}/listas", idTablero)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(listaRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        ListaResponse listaDestino = objectMapper.readValue(
            listaResult.getResponse().getContentAsString(),
            ListaResponse.class
        );

        CrearTarjetaRequest crearRequest = new CrearTarjetaRequest();
        crearRequest.setTitulo("Tarjeta Test");
        crearRequest.setDescripcion("Descripción");
        crearRequest.setTipo("TAREA");

        MvcResult crearResult = mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas", idTablero, idLista)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(crearRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        TarjetaResponse tarjetaResponse = objectMapper.readValue(
            crearResult.getResponse().getContentAsString(),
            TarjetaResponse.class
        );

        mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{idTarjeta}/mover",
                idTablero, idLista, tarjetaResponse.getId())
            .param("idListaDestino", listaDestino.getId())
            .param("emailUsuario", emailPropietario))
            .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{idTarjeta}/completar",
                idTablero, idLista, tarjetaResponse.getId())
            .param("emailUsuario", emailPropietario))
            .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{idTarjeta}/completar",
                idTablero, listaDestino.getId(), tarjetaResponse.getId())
            .param("emailUsuario", emailPropietario))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completada").value(true));
    }

    @Test
    @DisplayName("Crear tarjeta directamente en una lista con prerequisitos devuelve error")
    void testCrearTarjetaEnListaConPrerequisitos() throws Exception {
        CrearListaRequest listaPreviaRequest = new CrearListaRequest("Analisis", null);
        MvcResult listaPreviaResult = mockMvc.perform(post("/api/v1/tableros/{id}/listas", idTablero)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(listaPreviaRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        ListaResponse listaPrevia = objectMapper.readValue(
            listaPreviaResult.getResponse().getContentAsString(),
            ListaResponse.class
        );

        ConfigurarReglasListaRequest reglasRequest = ConfigurarReglasListaRequest.builder()
            .listasPrevias(java.util.List.of(listaPrevia.getId()))
            .build();

        mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/reglas", idTablero, idLista)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(reglasRequest)))
            .andExpect(status().isOk());

        CrearTarjetaRequest crearRequest = new CrearTarjetaRequest();
        crearRequest.setTitulo("Tarjeta bloqueada");
        crearRequest.setDescripcion("No debería crearse aquí");
        crearRequest.setTipo("TAREA");

        mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas", idTablero, idLista)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(crearRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.mensaje", containsString("prerequisitos")));
    }

    @Test
    @DisplayName("Crear tarjeta sin acceso")
    void testCrearTarjetaSinAcceso() throws Exception {
        CrearTarjetaRequest request = new CrearTarjetaRequest();
        request.setTitulo("Tarjeta Test");
        request.setDescripcion("Descripción");
        request.setTipo("TAREA");

        mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas", idTablero, idLista)
            .param("emailUsuario", "otro@test.com")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }
}
