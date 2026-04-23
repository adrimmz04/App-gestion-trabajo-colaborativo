package pds.app_gestion.ui.controller;

import java.util.List;

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
import pds.app_gestion.domain.RepositorioTablero;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests para ControladorTablero.
 * 
 * Prueba todos los endpoints REST relacionados con tableros:
 * - Crear, obtener, actualizar
 * - Compartir y permisos
 * - Bloquear/desbloquear
 * - Gestión de listas
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Integration Tests - ControladorTablero")
class ControladorTableroIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RepositorioTablero repositorioTablero;

    private String idTablero;
    private String emailPropietario = "propietario@test.com";
    private String emailUsuario2 = "usuario2@test.com";

    @BeforeEach
    void setUp() throws Exception {
        // Crear un tablero de prueba
        CrearTableroRequest request = CrearTableroRequest.builder()
            .titulo("Tablero Test")
            .descripcion("Descripción")
            .propietarioEmail(emailPropietario)
            .build();
        
        MvcResult result = mockMvc.perform(post("/api/v1/tableros")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andReturn();

        // Capturar respuesta sin importar el status
        int status = result.getResponse().getStatus();
        String jsonResponse = result.getResponse().getContentAsString();
        
        if (status != 201) {
            System.err.println("[TEST ERROR] POST /api/v1/tableros devolvió status: " + status);
            System.err.println("[TEST ERROR] Response: " + jsonResponse);
            throw new RuntimeException("Error en setUp: status " + status + " - " + jsonResponse);
        }

        TableroResponse response = objectMapper.readValue(jsonResponse, TableroResponse.class);
        idTablero = response.getId();
    }

    @Test
    @DisplayName("Crear tablero con datos válidos")
    void testCrearTableroValido() throws Exception {
        CrearTableroRequest request = new CrearTableroRequest(
            "Nuevo Tablero",
            "Descripción del tablero",
            "nuevo@test.com"
        );

        mockMvc.perform(post("/api/v1/tableros")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.titulo").value("Nuevo Tablero"))
            .andExpect(jsonPath("$.propietarioEmail").value("nuevo@test.com"));
    }

    @Test
    @DisplayName("Obtener tablero por ID con acceso")
    void testObtenerTableroConAcceso() throws Exception {
        mockMvc.perform(get("/api/v1/tableros/{id}", idTablero)
            .param("emailUsuario", emailPropietario))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(idTablero))
            .andExpect(jsonPath("$.titulo").value("Tablero Test"));
    }

    @Test
    @DisplayName("Obtener tablero sin acceso (usuario diferente)")
    void testObtenerTableroSinAcceso() throws Exception {
        mockMvc.perform(get("/api/v1/tableros/{id}", idTablero)
            .param("emailUsuario", "otro@test.com"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("No debe reutilizar respuesta cacheada entre usuarios con distinto permiso")
    void testObtenerTableroNoDebeCompartirCacheEntreUsuarios() throws Exception {
        mockMvc.perform(get("/api/v1/tableros/{id}", idTablero)
            .param("emailUsuario", emailPropietario))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(idTablero));

        mockMvc.perform(get("/api/v1/tableros/{id}", idTablero)
            .param("emailUsuario", "otro@test.com"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Obtener tablero inexistente")
    void testObtenerTableroInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/tableros/{id}", "id-inexistente")
            .param("emailUsuario", emailPropietario))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Obtener historial del tablero")
    void testObtenerHistorialTablero() throws Exception {
        mockMvc.perform(post("/api/v1/tableros/{id}/listas", idTablero)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new CrearListaRequest("Nueva Lista", null))))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/tableros/{id}/historial", idTablero)
            .param("emailUsuario", emailPropietario))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].tipo", hasItem("TABLERO_CREADO")))
            .andExpect(jsonPath("$[*].tipo", hasItem("LISTA_AÑADIDA")));
    }

    @Test
    @DisplayName("Actualizar descripción del tablero")
    void testActualizarTablero() throws Exception {
        ActualizarTableroRequest request = new ActualizarTableroRequest();
        request.setTitulo("Tablero Actualizado");
        request.setDescripcion("Nueva descripción");

        mockMvc.perform(put("/api/v1/tableros/{id}", idTablero)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.titulo").value("Tablero Actualizado"))
            .andExpect(jsonPath("$.descripcion").value("Nueva descripción"));
    }

    @Test
    @DisplayName("Actualizar tablero sin acceso")
    void testActualizarTableroSinAcceso() throws Exception {
        ActualizarTableroRequest request = new ActualizarTableroRequest();
        request.setTitulo("Tablero Test");
        request.setDescripcion("Nueva descripción");

        mockMvc.perform(put("/api/v1/tableros/{id}", idTablero)
            .param("emailUsuario", "otro@test.com")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Eliminar tablero")
    void testEliminarTablero() throws Exception {
        mockMvc.perform(delete("/api/v1/tableros/{id}", idTablero)
            .param("emailUsuario", emailPropietario))
            .andExpect(status().isNoContent());

        assertThat(repositorioTablero.obtenerPorId(idTablero)).isEmpty();
    }

    @Test
    @DisplayName("Listar tableros del propietario")
    void testObtenerTablerosPropietario() throws Exception {
        mockMvc.perform(get("/api/v1/tableros/propietario/{email}", emailPropietario))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[0].propietarioEmail").value(emailPropietario));
    }

    @Test
    @DisplayName("Listar tableros compartidos (vacío inicialmente)")
    void testObtenerTablerosCompartidosVacio() throws Exception {
        mockMvc.perform(get("/api/v1/tableros/compartidos/{email}", emailUsuario2))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Compartir tablero con otro usuario")
    void testCompartirTablero() throws Exception {
        CompartirTableroRequest request = new CompartirTableroRequest(emailUsuario2);

        mockMvc.perform(post("/api/v1/tableros/{id}/compartir", idTablero)
            .param("emailPropietario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        // Verificar que ahora el usuario2 lo ve en compartidos
        mockMvc.perform(get("/api/v1/tableros/compartidos/{email}", emailUsuario2))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(idTablero));
    }

    @Test
    @DisplayName("Compartir tablero sin permiso")
    void testCompartirTableroSinPermiso() throws Exception {
        CompartirTableroRequest request = new CompartirTableroRequest(emailUsuario2);

        mockMvc.perform(post("/api/v1/tableros/{id}/compartir", idTablero)
            .param("emailPropietario", "otro@test.com")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Bloquear tablero por minutos")
    void testBloquearTablero() throws Exception {
        BloquearTableroRequest request = new BloquearTableroRequest(5);

        mockMvc.perform(post("/api/v1/tableros/{id}/bloquear", idTablero)
            .param("emailPropietario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        // Verificar que está bloqueado
        mockMvc.perform(get("/api/v1/tableros/{id}", idTablero)
            .param("emailUsuario", emailPropietario))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bloqueado").value(true));
    }

    @Test
    @DisplayName("Bloquear tablero sin permiso")
    void testBloquearTableroSinPermiso() throws Exception {
        BloquearTableroRequest request = new BloquearTableroRequest(5);

        mockMvc.perform(post("/api/v1/tableros/{id}/bloquear", idTablero)
            .param("emailPropietario", "otro@test.com")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Desbloquear tablero")
    void testDesbloquearTablero() throws Exception {
        // Primero bloquear
        BloquearTableroRequest bloqueoRequest = new BloquearTableroRequest(5);
        mockMvc.perform(post("/api/v1/tableros/{id}/bloquear", idTablero)
            .param("emailPropietario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(bloqueoRequest)))
            .andExpect(status().isNoContent());

        // Luego desbloquear
        mockMvc.perform(post("/api/v1/tableros/{id}/desbloquear", idTablero)
            .param("emailPropietario", emailPropietario))
            .andExpect(status().isNoContent());

        // Verificar que está desbloqueado
        mockMvc.perform(get("/api/v1/tableros/{id}", idTablero)
            .param("emailUsuario", emailPropietario))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bloqueado").value(false));
    }

    @Test
    @DisplayName("Agregar lista a tablero")
    void testAgregarLista() throws Exception {
        CrearListaRequest request = new CrearListaRequest("Nueva Lista", 5);

        MvcResult result = mockMvc.perform(post("/api/v1/tableros/{id}/listas", idTablero)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nombre").value("Nueva Lista"))
            .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        ListaResponse response = objectMapper.readValue(jsonResponse, ListaResponse.class);
        
        assertThat(response.getId()).isNotEmpty();
        assertThat(response.getNombre()).isEqualTo("Nueva Lista");
    }

    @Test
    @DisplayName("El repositorio debe reconstruir listas persistidas al leer un tablero")
    void testRepositorioReconstruyeListasPersistidas() throws Exception {
        CrearListaRequest request = new CrearListaRequest("Nueva Lista", 5);

        mockMvc.perform(post("/api/v1/tableros/{id}/listas", idTablero)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        var tableroPersistido = repositorioTablero.obtenerPorId(idTablero);

        assertThat(tableroPersistido).isPresent();
        assertThat(tableroPersistido.get().getListas()).hasSize(1);
        assertThat(tableroPersistido.get().getListas().get(0).getNombre()).isEqualTo("Nueva Lista");
    }

    @Test
    @DisplayName("Agregar lista sin acceso")
    void testAgregarListaSinAcceso() throws Exception {
        CrearListaRequest request = new CrearListaRequest("Nueva Lista", 5);

        mockMvc.perform(post("/api/v1/tableros/{id}/listas", idTablero)
            .param("emailUsuario", "otro@test.com")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Agregar lista a tablero bloqueado")
    void testAgregarListaEnTableroBloqurado() throws Exception {
        // Bloquear tablero
        BloquearTableroRequest bloqueoRequest = new BloquearTableroRequest(5);
        mockMvc.perform(post("/api/v1/tableros/{id}/bloquear", idTablero)
            .param("emailPropietario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(bloqueoRequest)))
            .andExpect(status().isNoContent());

        // Intentar agregar lista
        CrearListaRequest request = new CrearListaRequest("Nueva Lista", 5);
        mockMvc.perform(post("/api/v1/tableros/{id}/listas", idTablero)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Eliminar lista de tablero")
    void testEliminarLista() throws Exception {
        CrearListaRequest request = new CrearListaRequest("Nueva Lista", 5);

        MvcResult result = mockMvc.perform(post("/api/v1/tableros/{id}/listas", idTablero)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

        ListaResponse listaResponse = objectMapper.readValue(result.getResponse().getContentAsString(), ListaResponse.class);

        mockMvc.perform(delete("/api/v1/tableros/{idTablero}/listas/{idLista}", idTablero, listaResponse.getId())
            .param("emailUsuario", emailPropietario))
            .andExpect(status().isNoContent());

        var tableroPersistido = repositorioTablero.obtenerPorId(idTablero);
        assertThat(tableroPersistido).isPresent();
        assertThat(tableroPersistido.get().getListas()).isEmpty();
    }

    @Test
    @DisplayName("Configurar reglas de una lista")
    void testConfigurarReglasLista() throws Exception {
        MvcResult listaOrigenResult = mockMvc.perform(post("/api/v1/tableros/{id}/listas", idTablero)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new CrearListaRequest("Origen", null))))
            .andExpect(status().isCreated())
            .andReturn();

        MvcResult listaDestinoResult = mockMvc.perform(post("/api/v1/tableros/{id}/listas", idTablero)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new CrearListaRequest("Destino", null))))
            .andExpect(status().isCreated())
            .andReturn();

        ListaResponse listaOrigen = objectMapper.readValue(listaOrigenResult.getResponse().getContentAsString(), ListaResponse.class);
        ListaResponse listaDestino = objectMapper.readValue(listaDestinoResult.getResponse().getContentAsString(), ListaResponse.class);

        ConfigurarReglasListaRequest request = ConfigurarReglasListaRequest.builder()
            .limiteMaximo(3)
            .listasPrevias(List.of(listaOrigen.getId()))
            .build();

        mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/reglas", idTablero, listaDestino.getId())
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.limiteMaximo").value(3))
            .andExpect(jsonPath("$.listasPrevias", hasSize(1)))
            .andExpect(jsonPath("$.listasPrevias[0]").value(listaOrigen.getId()));
    }

    @Test
    @DisplayName("Obtener reglas de una lista")
    void testObtenerReglasLista() throws Exception {
        MvcResult listaOrigenResult = mockMvc.perform(post("/api/v1/tableros/{id}/listas", idTablero)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new CrearListaRequest("Origen", null))))
            .andExpect(status().isCreated())
            .andReturn();

        MvcResult listaDestinoResult = mockMvc.perform(post("/api/v1/tableros/{id}/listas", idTablero)
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new CrearListaRequest("Destino", null))))
            .andExpect(status().isCreated())
            .andReturn();

        ListaResponse listaOrigen = objectMapper.readValue(listaOrigenResult.getResponse().getContentAsString(), ListaResponse.class);
        ListaResponse listaDestino = objectMapper.readValue(listaDestinoResult.getResponse().getContentAsString(), ListaResponse.class);

        mockMvc.perform(post("/api/v1/tableros/{idTablero}/listas/{idLista}/reglas", idTablero, listaDestino.getId())
            .param("emailUsuario", emailPropietario)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                ConfigurarReglasListaRequest.builder()
                    .limiteMaximo(2)
                    .listasPrevias(List.of(listaOrigen.getId()))
                    .build()
            )))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/tableros/{idTablero}/listas/{idLista}/reglas", idTablero, listaDestino.getId())
            .param("emailUsuario", emailPropietario))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Destino"))
            .andExpect(jsonPath("$.limiteMaximo").value(2))
            .andExpect(jsonPath("$.listasPrevias", hasSize(1)))
            .andExpect(jsonPath("$.listasPrevias[0]").value(listaOrigen.getId()));
    }
}
