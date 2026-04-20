# Diseño del Modelo de Dominio

## Introducción

Este documento describe el modelo de dominio de la aplicación de gestión de trabajo colaborativo, las decisiones de diseño tomadas y la justificación de las mismas.

## Arquitectura General

La aplicación sigue un enfoque de **Arquitectura Hexagonal** (también conocida como puertos y adaptadores) combinada con **Domain-Driven Design (DDD)**.

### Capas de la Aplicación

```
┌─────────────────────────────────────────────────────────┐
│         Capa de Presentación (UI)                       │
│  - JavaFX (interfaz gráfica de escritorio)              │
│  - Controladores REST (API)                             │
└─────────────────────────────────────────────────────────┘
                         ↑
┌─────────────────────────────────────────────────────────┐
│      Capa de Aplicación (Application)                   │
│  - Casos de uso (Servicios de aplicación)               │
│  - Orquestación de la lógica de negocio                 │
└─────────────────────────────────────────────────────────┘
                         ↑
┌─────────────────────────────────────────────────────────┐
│      Capa de Dominio (Domain)                           │
│  - Agregados (Tablero, Lista, Tarjeta)                  │
│  - Value Objects (Etiqueta, Posición, etc.)             │
│  - Servicios de Dominio                                 │
│  - Puertos (interfaces de repositorios)                 │
└─────────────────────────────────────────────────────────┘
                         ↑
┌─────────────────────────────────────────────────────────┐
│      Capa de Infraestructura (Infrastructure)           │
│  - Adaptadores de persistencia (JPA)                    │
│  - Implementaciones de repositorios                     │
│  - Configuración externa                                │
└─────────────────────────────────────────────────────────┘
```

## Agregados del Dominio

### 1. Agregado Tablero (Raíz de Agregado)

**Responsabilidad**: Gestionar el ciclo de vida completo de un tablero de tareas.

```
Tablero (Agregado Raíz)
├── id: String
├── titulo: String
├── descripcion: String
├── propietarioEmail: String
├── listas: List<Lista>
├── bloqueado: boolean
├── fechaDesbloqueo: Optional<LocalDateTime>
├── historial: List<RegistroAccion>
└── usuariosCompartidos: Set<String>
```

**Decisiones de Diseño**:

- **Tablero como Raíz de Agregado**: El tablero es la entidad principal que controla todo el agregado. Las listas y tarjetas no pueden existir independientemente del tablero.

- **Email como identificador de propietario**: Simplifica la gestión de usuarios sin necesidad de una tabla de usuarios adicional en la fase inicial.

- **Historial de acciones**: Inmutable dentro del agregado, solo se añaden registros. Permite auditoría completa de cambios.

- **Bloqueo temporal**: Implementado con `fechaDesbloqueo` opcional que permite:
  - Bloquear por duración configurable
  - Desbloqueo automático al tiempo especificado
  - Control manual de desbloqueo

- **Usuarios compartidos**: Set para evitar duplicados de usuarios y facilitar búsquedas rápidas.

### 2. Entidad Lista

```
Lista
├── id: String
├── nombre: String
├── tarjetas: List<Tarjeta>
├── limiteMaximo: Optional<Integer>
├── listasPrevias: List<String>
└── fechaCreacion: LocalDateTime
```

**Decisiones de Diseño**:

- **Orden mediante índice**: Las tarjetas mantienen el orden mediante su posición en la lista.

- **Límite máximo configurable**: Implementado como `Optional<Integer>` para permitir listas sin límite.

- **Listas previas**: Prepara la funcionalidad de requisitos de flujo de trabajo.

### 3. Entidad Tarjeta

```
Tarjeta
├── id: String
├── titulo: String
├── descripcion: String
├── completada: boolean
├── etiquetas: Set<Etiqueta>
├── tipo: TipoTarjeta (TAREA | CHECKLIST)
├── fechaCreacion: LocalDateTime
├── fechaCompletacion: Optional<LocalDateTime>
└── historial de cambios (implícito)
```

**Decisiones de Diseño**:

- **Enum para tipos de tarjeta**: Facilita la extensión futura con nuevos tipos.

- **Timestamps completos**: 
  - `fechaCreacion`: Inmutable, registra cuándo se creó
  - `fechaActualizacion`: Se actualiza con cada cambio
  - `fechaCompletacion`: Se establece al marcar como completada

- **Set para etiquetas**: Evita duplicados de etiquetas en la misma tarjeta.

- **Métodos de negocio**: Las tarjetas conocen cómo cambiarse de estado (completa/incompleta).

## Value Objects

### Etiqueta

```java
Etiqueta {
    nombre: String
    color: String (hexadecimal #RRGGBB)
}
```

**Decisiones de Diseño**:

- **Inmutable**: No puede cambiar después de crearse (true Value Object).
- **Validación de color**: Solo acepta formato hexadecimal válido (#RRGGBB).
- **Validación de nombre**: No puede estar vacío.
- **Implementa Equals/HashCode**: Permite usar Set<Etiqueta> confiablemente.

### Posición

```java
Posicion {
    indice: int
}
```

**Decisiones de Diseño**:

- **Value Object para ordenamiento**: Permite cambiar la estrategia de posicionamiento sin afectar otras partes.
- **Inmutable**: Una vez creada, no cambia.
- **Método `siguiente()`**: Facilita el manejo de reordenamientos.

### RegistroAccion

```java
RegistroAccion {
    tipo: String
    detalles: String
    fecha: LocalDateTime
}
```

**Decisiones de Diseño**:

- **Value Object inmutable**: El historial no se modifica.
- **Tipos de acción configurables**: Permite diferentes tipos sin cambiar la estructura.

## Puertos del Dominio

### RepositorioTablero

```java
interface RepositorioTablero {
    void guardar(Tablero tablero)
    Optional<Tablero> obtenerPorId(String id)
    List<Tablero> obtenerPorPropietario(String email)
    List<Tablero> obtenerCompartidos(String email)
    void eliminar(String id)
    boolean existe(String id)
}
```

**Decisiones de Diseño**:

- **Puerto de dominio**: Define qué debe poder hacer la persistencia sin especificar cómo.
- **Operaciones por propietario y compartidos**: Soporta los casos de uso principales.
- **Optional para consultas unitarias**: Explícita la posibilidad de no encontrar el recurso.

## Invariantes de Dominio

### Tablero

1. **No puede tener dos listas con el mismo ID**
2. **No puede tener listas o tarjetas si está eliminado**
3. **Un tablero bloqueado solo permite mover tarjetas, no crear nuevas** (por implementar en aplicación)

### Lista

1. **No puede exceder el límite máximo de tarjetas** (si está configurado)
2. **No puede tener dos tarjetas con el mismo ID** (por construcción de Java)

### Tarjeta

1. **Una tarjeta debe tener siempre un título**
2. **El tipo de tarjeta es inmutable**
3. **Si está completada, debe tener fecha de completación**

## Patrones Utilizados

### 1. Agregado (DDD)
- Garantiza consistencia dentro del agregado
- El tablero es la raíz de agregado
- Las listas y tarjetas no tienen existencia fuera del tablero

### 2. Value Object
- Etiqueta, Posición, RegistroAccion son Value Objects
- Inmutables y comparables por valor

### 3. Repository Pattern
- Abstracta la persistencia detrás de una interfaz
- RepositorioTablero es un puerto del dominio
- La implementación pertenece a infraestructura

### 4. Factory Pattern (por implementar)
- Para crear tarjetas de diferentes tipos
- TarjetaFactory podría encapsular la lógica de creación

## Extensibilidad Futura

### Características que el modelo soporta:

1. **Reglas de lista**:
   - Ya tenemos `limiteMaximo` y `listasPrevias` en Lista
   - Falta implementar la lógica de validación

2. **Automatización de tarjetas**:
   - El historial permite seguimiento de cambios
   - Podemos añadir un `ServicioAutomatizacion` que reaccione a eventos

3. **Filtrado por etiquetas**:
   - Ya implementado: `Set<Etiqueta>` en Tarjeta
   - Solo falta implementar en la capa de aplicación

4. **Bloqueo temporal**:
   - Ya implementado completamente en Tablero

5. **Historial completo**:
   - Ya implementado con RegistroAccion

## Conclusiones

El modelo de dominio propuesto:

- ✅ Es simple pero extensible
- ✅ Respeta los invariantes de negocio
- ✅ Está desacoplado de detalles técnicos (JPA, Spring, etc.)
- ✅ Facilita las pruebas unitarias
- ✅ Permite futuras características opcionales
- ✅ Sigue principios de DDD
- ✅ Mantiene la lógica de negocio en el dominio
