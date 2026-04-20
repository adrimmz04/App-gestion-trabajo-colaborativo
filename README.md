# App Gestión de Trabajo Colaborativo

Aplicación web de gestión de trabajo colaborativo a través de tableros de tareas, inspirada en Trello.

## Participantes

- **Adrian Martinez Zamora** (adrimmz04@gmail.com)

## Características implementadas

### Características básicas
- ✅ Crear y modificar tableros
- ✅ Crear listas de tareas dentro de tableros
- ✅ Crear tarjetas en las listas
- ✅ Marcar tarjetas como completadas
- ✅ Etiquetas para clasificar tarjetas
- ✅ Dos tipos de tarjetas: tareas y checklists
- ✅ Historial de acciones en el tablero
- ✅ Bloqueo temporal de tableros
- ✅ Compartición de tableros mediante URL

### Características opcionales
- (Por implementar)

## Tecnologías utilizadas

- **Lenguaje**: Java 17
- **Framework Backend**: Spring Boot 3.1.5
- **Persistencia**: JPA/Hibernate
- **Base de datos**: H2 (desarrollo), PostgreSQL (producción)
- **Interfaz gráfica**: JavaFX
- **Build**: Maven
- **Arquitectura**: Hexagonal (puertos y adaptadores)
- **Diseño**: Domain-Driven Design (DDD)

## Estructura del proyecto

```
app-gestion/
├── src/
│   ├── main/
│   │   ├── java/pds/app_gestion/
│   │   │   ├── domain/          # Modelo de dominio
│   │   │   ├── application/     # Casos de uso y orquestación
│   │   │   ├── infrastructure/  # Persistencia, repositorios, adaptadores
│   │   │   └── ui/              # Controllers REST y UI
│   │   └── resources/           # Configuración y recursos
│   └── test/                    # Pruebas unitarias
├── docs/                        # Documentación
├── pom.xml
└── README.md
```

## Requisitos previos

- JDK 17 o superior
- Maven 3.8.1 o superior
- Git

## Instalación y ejecución

### Backend
```bash
mvn clean install
mvn spring-boot:run
```

### Tests
```bash
mvn test
```

## Documentación detallada

Ver [CREDITOS.md](CREDITOS.md) para información sobre participación y contribuciones.

## Estado del proyecto

- **Última actualización**: Abril 2026
- **Versión**: 1.0.0-SNAPSHOT
- **Estado**: En desarrollo - Fase inicial
