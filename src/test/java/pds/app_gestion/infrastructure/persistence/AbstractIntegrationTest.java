package pds.app_gestion.infrastructure.persistence;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pds.app_gestion.infrastructure.persistence.repository.ConvertidorTableroJPA;

/**
 * Clase base para pruebas de integración con TestContainers + PostgreSQL.
 * 
 * Esta clase configura un contenedor de PostgreSQL que se ejecuta en Docker
 * durante las pruebas de integración, permitiendo probar la persistencia JPA
 * contra una base de datos real.
 */
@Testcontainers
@DataJpaTest
@Import(ConvertidorTableroJPA.class)
public abstract class AbstractIntegrationTest {

    /**
     * Contenedor PostgreSQL que se ejecuta en Docker.
     * Se inicia automáticamente antes de los tests y se detiene después.
     */
    @Container
    protected static final PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("test_gestion_trabajo")
        .withUsername("postgres")
        .withPassword("postgres")
        .withExposedPorts(5432);

    /**
     * Configura dinámicamente las propiedades de Spring Boot
     * para usar la URL, usuario y contraseña del contenedor PostgreSQL.
     */
    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresql::getJdbcUrl);
        registry.add("spring.datasource.username", postgresql::getUsername);
        registry.add("spring.datasource.password", postgresql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQL10Dialect");
    }
}
