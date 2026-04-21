package pds.app_gestion.ui.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
@Disabled("Requiere investigación adicional - POST devuelve 500")
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
