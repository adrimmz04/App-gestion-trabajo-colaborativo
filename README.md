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

## Fases de desarrollo

### Phase 1: Modelo de Dominio ✅ COMPLETADA
- Entidades de dominio: Tablero, Lista, Tarjeta
- Objetos de valor: Etiqueta, Posicion, RegistroAccion
- Puerto: RepositorioTablero
- **Tests**: 47 tests pasando ✅
- **Documentación**: [DISEÑO_DOMINIO.md](DISEÑO_DOMINIO.md), [INICIO_RAPIDO.md](INICIO_RAPIDO.md)

### Phase 2: Capa de Aplicación ✅ COMPLETADA
- **Servicios de Aplicación**: 2 servicios (13 + 6 casos de uso)
  - ServicioTablero: Gestión de tableros (crear, compartir, bloquear, etc.)
  - ServicioTarjeta: Gestión de tarjetas (crear, completar, etiquetar)
- **Controladores REST**: 2 controladores con 14 endpoints
  - GET/POST/PUT para tableros, listas y tarjetas
  - Autenticación basada en email
- **DTOs**: 13 clases (Request/Response)
- **Excepciones**: 5 clases con mapeo HTTP
- **Tests**: 17 tests para servicios ✅
- **Total Tests**: 64 tests pasando ✅
- **Documentación**: [PHASE2_RESUMEN.md](PHASE2_RESUMEN.md)

### Phase 3: Persistencia con JPA (PRÓXIMA)
- [ ] Entidades JPA con anotaciones
- [ ] RepositorioTableroJPA (Spring Data JPA)
- [ ] Configuración PostgreSQL
- [ ] Pruebas de integración

## API REST (Phase 2)

### Endpoints de Tableros
```
POST   /api/v1/tableros              - Crear tablero
GET    /api/v1/tableros/{id}         - Obtener tablero
PUT    /api/v1/tableros/{id}         - Actualizar tablero
POST   /api/v1/tableros/{id}/compartir    - Compartir tablero
POST   /api/v1/tableros/{id}/bloquear     - Bloquear tablero
POST   /api/v1/tableros/{id}/listas       - Agregar lista
```

### Endpoints de Tarjetas
```
POST   /api/v1/tableros/{id}/listas/{id}/tarjetas         - Crear tarjeta
PUT    /api/v1/tableros/{id}/listas/{id}/tarjetas/{id}    - Actualizar tarjeta
POST   /api/v1/tableros/{id}/listas/{id}/tarjetas/{id}/completar - Completar
POST   /api/v1/tableros/{id}/listas/{id}/tarjetas/{id}/etiquetas - Etiquetar
```

## Estado del proyecto

- **Última actualización**: Abril 2026
- **Versión**: 1.0.0-SNAPSHOT
- **Fase Actual**: Phase 2 - Capa de Aplicación ✅ COMPLETADA
- **Próximo**: Phase 3 - Persistencia con JPA
- **Tests**: 64/64 pasando ✅
- **Compilación**: BUILD SUCCESS ✅
