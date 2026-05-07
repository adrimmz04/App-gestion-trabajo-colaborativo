package pds.app_gestion.ui.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pds.app_gestion.application.dto.SolicitarCodigoAccesoRequest;
import pds.app_gestion.application.dto.SolicitarCodigoAccesoResponse;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Integration Tests - ControladorAutenticacion")
class ControladorAutenticacionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Solicitar código y consultar la sesión activa")
    void testSolicitarCodigoYConsultarSesion() throws Exception {
        SolicitarCodigoAccesoRequest request = SolicitarCodigoAccesoRequest.builder()
            .email("autenticado@test.com")
            .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/codigos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("autenticado@test.com"))
            .andExpect(jsonPath("$.codigoDesarrollo").isNotEmpty())
            .andReturn();

        SolicitarCodigoAccesoResponse response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            SolicitarCodigoAccesoResponse.class
        );

        mockMvc.perform(get("/api/v1/auth/sesion")
                .param("codigoAcceso", response.getCodigoDesarrollo()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("autenticado@test.com"))
            .andExpect(jsonPath("$.autenticado").value(true));
    }

    @Test
    @DisplayName("Cerrar sesión invalida el código")
    void testCerrarSesionInvalidaCodigo() throws Exception {
        SolicitarCodigoAccesoRequest request = SolicitarCodigoAccesoRequest.builder()
            .email("logout@test.com")
            .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/codigos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        SolicitarCodigoAccesoResponse response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            SolicitarCodigoAccesoResponse.class
        );

        mockMvc.perform(delete("/api/v1/auth/sesion")
                .param("codigoAcceso", response.getCodigoDesarrollo()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/auth/sesion")
                .param("codigoAcceso", response.getCodigoDesarrollo()))
            .andExpect(status().isUnauthorized());
    }
}