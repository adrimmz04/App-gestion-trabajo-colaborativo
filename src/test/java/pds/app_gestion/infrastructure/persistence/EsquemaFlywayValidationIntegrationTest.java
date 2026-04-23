package pds.app_gestion.infrastructure.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-flyway-test.properties")
@DisplayName("Integration Tests - Esquema Flyway y validación JPA")
class EsquemaFlywayValidationIntegrationTest {

    @Test
    @DisplayName("Debe arrancar con Flyway activo y el esquema validado por Hibernate")
    void debeArrancarConFlywayYEsquemaValidado() {
        // Si el contexto arranca, Flyway aplicó las migraciones y Hibernate validó el esquema.
    }
}