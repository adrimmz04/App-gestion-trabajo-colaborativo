# App Gestión de Trabajo Colaborativo

Aplicación de gestión de trabajo colaborativo orientada a tableros, listas y tarjetas. El proyecto se ha desarrollado en Java con Spring Boot, JavaFX, Maven y persistencia JPA, siguiendo una arquitectura hexagonal con enfoque DDD.

## Objetivo del proyecto

La aplicación permite organizar trabajo colaborativo mediante tableros compartibles, listas de tareas y tarjetas con seguimiento de estado, historial de acciones y reglas de flujo. Además de la funcionalidad base, incorpora varias características opcionales del enunciado para ampliar el comportamiento del sistema.

## Participante

- Adrian Martinez Zamora

## Alcance implementado

### Funcionalidad principal
- Creación, consulta, edición y compartición de tableros.
- Gestión de listas dentro de cada tablero.
- Gestión de tarjetas de tipo tarea y checklist.
- Marcado de tarjetas como completadas y movimiento a lista especial de completadas cuando existe.
- Etiquetas con nombre y color para clasificar tarjetas.
- Historial de acciones del tablero.
- Bloqueo temporal del tablero para impedir nuevas altas y mantener el flujo de trabajo controlado.

### Características opcionales implementadas
- Reglas de listas con límite máximo de tarjetas.
- Prerequisitos entre listas basados en el recorrido real de cada tarjeta.
- Filtrado de tarjetas por etiquetas.
- Plantillas de tablero definidas en YAML, con importación y exportación.
- Compactación automática mediante archivado y eliminación diferida.

## Arquitectura y tecnologías

### Arquitectura
- Arquitectura hexagonal con separación en `domain`, `application`, `infrastructure` y `ui`.
- Agregado principal centrado en `Tablero`, que controla listas, tarjetas e historial.
- Servicios de aplicación para orquestar casos de uso y adaptadores para REST, JavaFX y persistencia.

### Stack técnico
- Java 21
- Spring Boot 3.1.5
- JavaFX 21
- Maven
- JPA / Hibernate
- Flyway
- PostgreSQL para ejecución normal
- H2 en memoria para pruebas y arranque local rápido
- Caffeine para caché
- SnakeYAML para plantillas

## Estado actual

- API REST operativa para los casos de uso principales.
- Interfaz JavaFX funcional como cliente de escritorio del proyecto.
- Persistencia validada con migraciones Flyway y ejecución real sobre PostgreSQL.
- Suite automática validada en verde en el estado final revisado: 169 tests, 0 fallos.

## Estructura del repositorio

```text
app-gestion/
├── src/
│   ├── main/
│   │   ├── java/pds/app_gestion/
│   │   │   ├── domain/
│   │   │   ├── application/
│   │   │   ├── infrastructure/
│   │   │   └── ui/
│   │   └── resources/
│   └── test/
├── docs/
├── pom.xml
├── README.md
└── CREDITOS.md
```

## Documentación del proyecto

La documentación pública del repositorio queda concentrada en estos documentos:

- [docs/INICIO_RAPIDO.md](docs/INICIO_RAPIDO.md)
- [docs/FUNCIONALIDADES_IMPLEMENTADAS.md](docs/FUNCIONALIDADES_IMPLEMENTADAS.md)
- [CREDITOS.md](CREDITOS.md)

## Limitaciones conocidas

- La autenticación por código por correo propuesta como opcional no está implementada.
- No existe un modelo de permisos finos por tarjeta.
- La interfaz JavaFX cubre los flujos relevantes del proyecto, pero no pretende sustituir a un cliente de producto completo.

## Información académica relevante

El enunciado exige que el `README.md` principal del repositorio incluya el nombre de la aplicación, los participantes, las características implementadas y un enlace al fichero de créditos. Esa información queda recogida en este documento y en [CREDITOS.md](CREDITOS.md).# App Gestión de Trabajo Colaborativo

Aplicación de gestión de trabajo colaborativo orientada a tableros, listas y tarjetas. El proyecto se ha desarrollado en Java con Spring Boot, JavaFX, Maven y persistencia JPA, siguiendo una arquitectura hexagonal con enfoque DDD.

## Objetivo del proyecto

La aplicación permite organizar trabajo colaborativo mediante tableros compartibles, listas de tareas y tarjetas con seguimiento de estado, historial de acciones y reglas de flujo. Además de la funcionalidad base, incorpora varias características opcionales del enunciado para ampliar el comportamiento del sistema.

## Participante

- Adrian Martinez Zamora - adrimmz04@gmail.com

## Alcance implementado

### Funcionalidad principal
- Creación, consulta, edición y compartición de tableros.
- Gestión de listas dentro de cada tablero.
- Gestión de tarjetas de tipo tarea y checklist.
- Marcado de tarjetas como completadas y movimiento a lista especial de completadas cuando existe.
- Etiquetas con nombre y color para clasificar tarjetas.
- Historial de acciones del tablero.
- Bloqueo temporal del tablero para impedir nuevas altas y mantener el flujo de trabajo controlado.

### Características opcionales implementadas
- Reglas de listas con límite máximo de tarjetas.
- Prerequisitos entre listas basados en el recorrido real de cada tarjeta.
- Filtrado de tarjetas por etiquetas.
- Plantillas de tablero definidas en YAML, con importación y exportación.
- Compactación automática mediante archivado y eliminación diferida.

## Arquitectura y tecnologías

### Arquitectura
- Arquitectura hexagonal con separación en `domain`, `application`, `infrastructure` y `ui`.
- Agregado principal centrado en `Tablero`, que controla listas, tarjetas e historial.
- Servicios de aplicación para orquestar casos de uso y adaptadores para REST, JavaFX y persistencia.

### Stack técnico
- Java 21
- Spring Boot 3.1.5
- JavaFX 21
- Maven
- JPA / Hibernate
- Flyway
- PostgreSQL para ejecución normal
- H2 en memoria para pruebas y arranque local rápido
- Caffeine para caché
- SnakeYAML para plantillas

## Estado actual

- API REST operativa para los casos de uso principales.
- Interfaz JavaFX funcional como cliente de escritorio del proyecto.
- Persistencia validada con migraciones Flyway y ejecución real sobre PostgreSQL.
- Suite automática validada en verde en el estado final revisado: 169 tests, 0 fallos.

## Estructura del repositorio

```text
app-gestion/
├── src/
│   ├── main/
│   │   ├── java/pds/app_gestion/
│   │   │   ├── domain/
│   │   │   ├── application/
│   │   │   ├── infrastructure/
│   │   │   └── ui/
│   │   └── resources/
│   └── test/
├── docs/
├── pom.xml
├── README.md
└── CREDITOS.md
```

## Documentación del proyecto

La documentación pública del repositorio queda concentrada en estos documentos:

- [docs/INICIO_RAPIDO.md](docs/INICIO_RAPIDO.md)
- [docs/FUNCIONALIDADES_IMPLEMENTADAS.md](docs/FUNCIONALIDADES_IMPLEMENTADAS.md)
- [CREDITOS.md](CREDITOS.md)

## Limitaciones conocidas

- La autenticación por código por correo propuesta como opcional no está implementada.
- No existe un modelo de permisos finos por tarjeta.
- La interfaz JavaFX cubre los flujos relevantes del proyecto, pero no pretende sustituir a un cliente de producto completo.

## Información académica relevante

El enunciado exige que el `README.md` incluya el nombre de la aplicación, los participantes, las características implementadas y un enlace al fichero de créditos. Esa información queda recogida en este documento y en [CREDITOS.md](CREDITOS.md).
