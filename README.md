# App Gestión de Trabajo Colaborativo

Aplicación de escritorio y API REST para gestionar trabajo colaborativo mediante tableros, listas y tarjetas. El proyecto está implementado en Java con Spring Boot, JavaFX, Maven y persistencia JPA, siguiendo una arquitectura hexagonal con enfoque DDD.

## Objetivo del proyecto

La aplicación cubre los requisitos principales del enunciado sobre tableros colaborativos y añade varias características opcionales con soporte real en dominio, servicios, persistencia, API REST y cliente JavaFX.

## Participante

- Adrian Martinez Zamora - adrian.m.z@um.es

## Funcionalidad implementada

### Funcionalidad principal
- Creación, consulta, edición, eliminación y compartición de tableros.
- Gestión de listas dentro de cada tablero.
- Gestión de tarjetas de tipo tarea y checklist.
- Marcado de tarjetas como completadas y descompletadas.
- Movimiento de tarjetas entre listas.
- Etiquetas con nombre y color para clasificar tarjetas.
- Historial de acciones del tablero.
- Bloqueo temporal del tablero para impedir nuevas altas y controlar el flujo de trabajo.

### Características opcionales implementadas
- Autenticación por código temporal, con modo desarrollo y soporte SMTP real.
- Reglas a nivel de lista: límite máximo de tarjetas y prerequisitos entre listas.
- Filtrado de tarjetas por etiquetas.
- Plantillas YAML con importación y exportación.
- Compactación automática mediante archivado y eliminación diferida.
- Permisos granulares por tarjeta para usuarios compartidos.

## Nota sobre permisos por tarjeta

Los permisos por tarjeta se han implementado con una decisión de compatibilidad: si una tarjeta no tiene permisos explícitos configurados, mantiene el acceso heredado del tablero compartido. Cuando el propietario asigna permisos concretos, se aplica filtrado de lectura y restricción de escritura sobre esa tarjeta.

## Arquitectura y tecnologías

### Arquitectura
- Arquitectura hexagonal con separación en `domain`, `application`, `infrastructure` y `ui`.
- El agregado principal es `Tablero`, que controla listas, tarjetas, historial y reglas de acceso.
- Los servicios de aplicación orquestan los casos de uso y los adaptadores exponen REST, JavaFX y persistencia.

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
- Spring Mail para autenticación por correo

## Estado actual

- API REST operativa para los casos de uso principales.
- Cliente JavaFX funcional integrado con los servicios reales.
- Persistencia validada con entidades JPA y migraciones Flyway.
- El repositorio incluye tests unitarios y de integración para servicios, controladores y persistencia.

## Arranque rápido

### Windows, un solo comando

```powershell
.\start-app.cmd
```

Comportamiento:

- Si existe `app-mail.local.ps1`, arranca con `local-gmail` y envío real de códigos por correo.
- Si no existe, arranca con `local` en modo desarrollo.

### Validación básica

```bash
mvn clean test
```

### Ejecución local rápida con H2

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local -DskipTests
```

## Correo real para autenticación

El proyecto ya incluye integración con Spring Mail para enviar el código temporal por SMTP sin cambiar la lógica principal.

Configuración local mínima en Windows:

```powershell
Copy-Item .\app-mail.local.example.ps1 .\app-mail.local.ps1
```

Después solo hay que completar `app-mail.local.ps1` con una cuenta y credenciales válidas. El fichero está ignorado por git.

Perfiles disponibles:

- `mail`: SMTP genérico mediante variables de entorno.
- `gmail`: configuración preparada para Gmail.
- `outlook`: configuración preparada para Outlook.
- `local-gmail`: H2 local más Gmail real para demostraciones rápidas.

La guía completa de arranque y perfiles está en `docs/INICIO_RAPIDO.md`.

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

- [docs/INICIO_RAPIDO.md](docs/INICIO_RAPIDO.md)
- [docs/FUNCIONALIDADES_IMPLEMENTADAS.md](docs/FUNCIONALIDADES_IMPLEMENTADAS.md)
- [CREDITOS.md](CREDITOS.md)

## Limitaciones conocidas

- El envío real del código por correo requiere configuración SMTP; en local y en tests se usa modo desarrollo.
- La interfaz JavaFX cubre el alcance académico del proyecto, no todos los escenarios de una aplicación de producto completo.
- La validación final de entrega debe hacerse ejecutando `mvn clean test` sobre el estado exacto que se vaya a presentar.

## Información académica relevante

El enunciado exige que el README principal incluya el nombre de la aplicación, los participantes, las características implementadas y una referencia al fichero de créditos. Esa información queda recogida en este documento y en [CREDITOS.md](CREDITOS.md).
