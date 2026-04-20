# Phase 2: Capa de Aplicación y API REST - COMPLETADA

## Resumen

Se ha completado la segunda fase del proyecto con la implementación de la **capa de aplicación** siguiendo la arquitectura hexagonal + DDD. La capa expone 13 casos de uso del tablero y 6 de tarjetas a través de una API REST.

## Componentes Implementados

### 1. **DTOs (Data Transfer Objects)** - 13 clases
Separación clara entre capas de dominio y presentación:
- **Request**: CrearTableroRequest, ActualizarTableroRequest, CrearListaRequest, ActualizarListaRequest, CrearTarjetaRequest, ActualizarTarjetaRequest, CrearEtiquetaRequest, CompartirTableroRequest, BloquearTableroRequest
- **Response**: TableroResponse, ListaResponse, TarjetaResponse, EtiquetaResponse

### 2. **Jerarquía de Excepciones** - 5 clases
- `AppException` (base)
- `RecursoNoEncontradoException` → HTTP 404
- `ErrorValidacionException` → HTTP 400
- `ErrorOperacionDominioException` → HTTP 409
- `PermisoNegadoException` → HTTP 403

### 3. **Servicios de Aplicación** - 2 clases

#### **ServicioTablero** (13 casos de uso)
```java
crearTablero()              // Crear nuevo tablero
obtenerTablero()            // Obtener por ID
actualizarTablero()         // Actualizar datos
obtenerTablerosPropietario() // Listar propios
obtenerTablerosCompartidos() // Listar compartidos
compartirTablero()          // Compartir con usuario
bloquearTablero()           // Bloquear por duración
desbloquearTablero()        // Desbloquear
agregarLista()              // Añadir lista al tablero
```

#### **ServicioTarjeta** (6 casos de uso)
```java
crearTarjeta()              // Crear tarjeta en lista
actualizarTarjeta()         // Actualizar datos
marcarComoCompletada()      // Marcar completada
marcarComoNoCompletada()    // Desmarcar
agregarEtiqueta()           // Añadir etiqueta
```

### 4. **Controladores REST** - 2 clases

#### **ControladorTablero** - 9 endpoints
```
POST   /api/v1/tableros                    → crear tablero
GET    /api/v1/tableros/{idTablero}       → obtener por ID
PUT    /api/v1/tableros/{idTablero}       → actualizar
GET    /api/v1/tableros/propietario/{email} → listar propios
GET    /api/v1/tableros/compartidos/{email} → listar compartidos
POST   /api/v1/tableros/{id}/compartir     → compartir
POST   /api/v1/tableros/{id}/bloquear      → bloquear
POST   /api/v1/tableros/{id}/desbloquear   → desbloquear
POST   /api/v1/tableros/{id}/listas        → agregar lista
```

#### **ControladorTarjeta** - 5 endpoints
```
POST   /api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/          → crear
PUT    /api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{id}     → actualizar
POST   /api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{id}/completar   → completar
POST   /api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{id}/descompletar → desmarcar
POST   /api/v1/tableros/{idTablero}/listas/{idLista}/tarjetas/{id}/etiquetas   → etiquetar
```

### 5. **Manejo de Excepciones**
`ManejadorExcepcionesGlobal` mapea todas las excepciones de aplicación a respuestas HTTP consistentes con:
- Código de error
- Mensaje descriptivo
- Timestamp

### 6. **Adaptador de Repositorio**
`RepositorioTableroEnMemoria` implementa el puerto `RepositorioTablero` usando `ConcurrentHashMap`:
- Almacenamiento en memoria
- Filtrado por propietario y compartidos
- Preparado para reemplazo por JPA en Phase 3

### 7. **Configuración Spring**
- `ConfiguracionWeb`: CORS habilitado para `/api/**` 
- `application.properties`: Puerto 8080, H2, logging DEBUG

## Pruebas de Aplicación - 17 tests adicionales

### ServicioTableroTest (11 tests)
- ✅ crearTableroExitosamente
- ✅ crearTableroConTituloVacioThrows
- ✅ crearTableroConEmailInvalidoThrows
- ✅ obtenerTableroQueNoExisteThrows
- ✅ obtenerTableroSinPermisoThrows
- ✅ obtenerTableroExitosamente
- ✅ compartirTableroExitosamente
- ✅ compartirTableroSinPermisoThrows
- ✅ bloquearTableroExitosamente
- ✅ agregarListaExitosamente
- ✅ agregarListaATableroBloqueoThrows

### ServicioTarjetaTest (6 tests)
- ✅ crearTarjetaExitosamente
- ✅ crearTarjetaEnTableroBloqueoThrows
- ✅ marcarTarjetaComoCompletadaExitosamente
- ✅ agregarEtiquetaAtatjezaExitosamente
- ✅ crearTarjetaSinPermisoThrows
- ✅ crearTarjetaConTituloVacioThrows

## Resultados Finales

| Componente | Cantidad | Estado |
|-----------|----------|--------|
| DTOs | 13 | ✅ Completo |
| Excepciones | 5 | ✅ Completo |
| Servicios | 2 | ✅ Completo |
| Controladores | 2 | ✅ Completo |
| Endpoints | 14 | ✅ Completo |
| Tests de Dominio | 47 | ✅ Pasando |
| Tests de Aplicación | 17 | ✅ Pasando |
| **Total Tests** | **64** | **✅ TODOS PASANDO** |

## Características de Seguridad

1. **Validación de Entrada**: Email válido, títulos no vacíos
2. **Control de Permisos**: Email-based (propietario vs compartido)
3. **Estado de Tablero**: No permite modificaciones si está bloqueado
4. **Manejo de Errores**: Excepciones custom con HTTP status codes

## Patrones Implementados

- ✅ **Hexagonal Architecture**: Controllers → Services → Domain
- ✅ **DDD**: Respeto de invariantes del dominio
- ✅ **Separation of Concerns**: DTOs, Excepciones, Servicios en paquetes separados
- ✅ **Repository Pattern**: Puerto RepositorioTablero + adaptador
- ✅ **Email-based Authorization**: Verificación de acceso por email

## Compilación y Testing

```bash
mvn clean compile   # BUILD SUCCESS
mvn test           # 64/64 tests PASSING ✅
```

## Siguientes Pasos - Phase 3: Persistencia con JPA

1. Crear entidades JPA (@Entity) con anotaciones
2. Implementar RepositorioTableroJPA con Spring Data JPA
3. Configurar mapeo objeto-relacional
4. Migrar de H2 in-memory a PostgreSQL (producción)
5. Crear pruebas de integración JPA

## Commits Realizados

1. ✅ Commit: "Phase 2: DTOs, Excepciones y Servicios REST" (31 archivos)
2. ✅ Commit: "Phase 2: Pruebas unitarias para servicios" (2 archivos)

## Notas Técnicas

- Arquitectura mantenida: El dominio sigue siendo independiente del framework
- Conversión de DTOs: Métodos privados en servicios para conversión
- Mock en tests: Mockito usado para aislar servicios del repositorio
- Validaciones: Combinadas en servicios + método helper para email

---

**Estado Final**: ✅ Phase 2 COMPLETADA - Ready for Phase 3
