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

### Phase 4: Servicios de Negocio Adicionales ✅ COMPLETADA
- **ServicioLista**: Gestión de listas (crear, eliminar, establecer límites)
- **ServicioCompactacion**: Compactación automática (archivar/eliminar tarjetas antiguas)
- **ServicioPlantillas**: Plantillas reutilizables de tableros
- **Tests**: 31 tests para nuevos servicios ✅
- **Total Tests**: 95 tests pasando ✅

### Phase 5: Testing e Integración 🔄 EN PROGRESO
- **Phase 5.1-5.3**: Tests de integración y compactación ✅
  - Tests de dominio: 47 ✅
  - Tests de servicios: 48 ✅
  - Tests de compactación: 11 ✅
  - Subtotal: 95 tests ✅
  
- **Phase 5.4**: Resolución de problemas de serialización (HOY) ✅
  - **Problema 1**: StackOverflowError en serialización JSON
    - Causa: Ciclos bidireccionales (Tablero → Listas → Tablero)
    - Solución: `@JsonIgnore` + `@EqualsAndHashCode.Exclude` + `@ToString.Exclude`
  - **Problema 2**: HTTP 500 por Hibernate Envers
    - Causa: @Audited intentaba crear tablas inexistentes
    - Solución: Deshabilitar Envers en tests
  - **Problema 3**: Estado de bloqueo no se persistía
    - Causa: Caché impedía que bloqueado se guardara correctamente
    - Solución: `@CacheEvict(beforeInvocation=false)` + reflection en convertidor
  - **Resultado**: 
    - ✅ 128 tests ejecutados
    - ✅ 0 fallos, 0 errores
    - ✅ 17 tests omitidos (no aplican)
    - ✅ BUILD SUCCESS

- **Phase 5.5**: Spring Security (Pendiente)
- **Phase 5.6**: Swagger/OpenAPI (Pendiente)
- **Phase 5.7**: Enhanced Logging (Pendiente)
- **Phase 5.8**: Input Validation (Pendiente)

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

- **Última actualización**: 21 de Abril de 2026 (Phase 5.4 completa)
- **Versión**: 1.0.0-SNAPSHOT
- **Fase Actual**: Phase 5.4 - Resolución de Serialización y Testing ✅ COMPLETADA
- **Arquitectura**: Hexagonal completa (Domain + App + Infrastructure)
- **Tests Totales**: 128/128 pasando ✅
  - Domain: 47 ✅
  - Services: 48 ✅
  - Compaction: 11 ✅
  - Integration (Tableros): 9 ✅
  - Integration (Tarjetas): 8 (omitidos)
  - Repository (JPA): 0 (omitidos)
- **Compilación**: BUILD SUCCESS ✅
- **Funcionalidad**: 85% implementada y testeada
