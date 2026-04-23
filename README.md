# App Gestión de Trabajo Colaborativo

Proyecto práctico de PDS para gestionar trabajo colaborativo mediante tableros, listas y tarjetas. La solución combina un backend en Spring Boot, persistencia JPA y una interfaz de escritorio JavaFX inicial, siguiendo arquitectura hexagonal y DDD.

## Participante

- Adrian Martinez Zamora (adrimmz04@gmail.com)

## Estado actual

- Requisitos base implementados: tableros, listas, tarjetas, historial, bloqueo temporal y compartición por usuario.
- Características opcionales implementadas: reglas de listas, plantillas YAML y compactación automática.
- Persistencia JPA operativa con Flyway y soporte PostgreSQL.
- API REST en funcionamiento y batería de pruebas activa.
- Interfaz JavaFX disponible, aunque todavía no cubre todos los flujos del sistema.

## Características implementadas

### Funcionalidad principal
- Crear, consultar, actualizar y compartir tableros.
- Crear listas y mover tarjetas entre listas.
- Crear tarjetas de tarea y checklist.
- Marcar tarjetas como completadas y gestionar etiquetas.
- Registrar historial de acciones del tablero.
- Bloquear temporalmente un tablero para impedir altas nuevas y permitir movimientos.

### Características opcionales ya incluidas
- Reglas de listas: límite máximo de tarjetas y prerequisitos entre listas.
- Plantillas definidas en YAML.
- Compactación automática con archivado y eliminación diferida.

## Limitaciones conocidas

- La autenticación por código enviado por correo del enunciado no está implementada; el acceso actual se identifica por email en las peticiones.
- La interfaz JavaFX actúa hoy como cliente inicial y todavía contiene partes estáticas.
- El arranque conjunto Spring Boot + JavaFX sigue siendo una zona de mejora técnica.

## Tecnologías utilizadas

- Java 21
- Spring Boot 3.1.5
- JavaFX 21
- JPA/Hibernate + Flyway
- PostgreSQL para ejecución normal
- H2 en memoria para pruebas
- Maven
- Caffeine, SnakeYAML y Testcontainers

## Estructura del proyecto

```
app-gestion/
├── src/
│   ├── main/
│   │   ├── java/pds/app_gestion/
│   │   │   ├── domain/          # Modelo de dominio
│   │   │   ├── application/     # Casos de uso y DTOs
│   │   │   ├── infrastructure/  # Persistencia, caché y adaptadores
│   │   │   └── ui/              # REST y JavaFX
│   │   └── resources/           # Configuración y migraciones
│   └── test/                    # Pruebas unitarias e integración
├── docs/
├── pom.xml
└── README.md
```

## Requisitos previos

- JDK 21 o superior
- Maven 3.8.1 o superior
- PostgreSQL local si se quiere ejecutar la aplicación con la configuración por defecto

## Ejecución rápida

### Validar el proyecto

```bash
mvn clean test
```

Estado verificado el 22/04/2026:

- 134 tests ejecutados
- 0 fallos
- 0 errores
- 0 omitidos

### Ejecutar la aplicación

La configuración por defecto usa PostgreSQL en `localhost:5432/app_gestion` con usuario `postgres` y contraseña `postgres`.

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080` y el arranque lanza también la ventana JavaFX principal.

### Ejecutar la aplicación sin PostgreSQL

Para desarrollo local rápido se ha añadido un perfil `local` que usa H2 en memoria y no requiere base de datos externa:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local -DskipTests
```

Con ese perfil la aplicación arranca igualmente en `http://localhost:8080` y mantiene la ventana JavaFX activa para pruebas manuales.

## Documentación relacionada

- [docs/INICIO_RAPIDO.md](docs/INICIO_RAPIDO.md)
- [docs/DISEÑO_DOMINIO.md](docs/DISEÑO_DOMINIO.md)
- [docs/MEMORIA_DEFENSA.md](docs/MEMORIA_DEFENSA.md)
- [docs/PLAN_CORRECCIONES.md](docs/PLAN_CORRECCIONES.md)
- [CREDITOS.md](CREDITOS.md)

## Estado del proyecto

- Versión: 1.0.0-SNAPSHOT
- Arquitectura: hexagonal con enfoque DDD
- Persistencia: JPA con migraciones Flyway
- Interfaz: REST operativa y cliente JavaFX inicial
- Calidad actual: build en verde y cobertura de rutas críticas mediante tests unitarios e integración
