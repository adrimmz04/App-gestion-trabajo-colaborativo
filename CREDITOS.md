# Créditos y Participación

## Adrian Martinez Zamora

### Contribuciones

#### Fase 1: Configuración y Estructura Inicial
- **Descripción**: Creación de la estructura base del proyecto con arquitectura hexagonal y DDD
- **Commits**:
  - Configuración inicial del pom.xml con dependencias
  - Creación de estructura de carpetas hexagonal
  - Documentación inicial (README.md, CREDITOS.md)

#### Fase 2: Modelo de Dominio (En progreso)
- **Descripción**: Implementación de las entidades y agregados del dominio
- **Componentes**:
  - Agregado Tablero con sus raíces
  - Agregado Lista
  - Agregado Tarjeta con polimorfismo
  - Value Objects (Etiqueta, Posición, etc.)
- **Commits**:
  - Creación de entidades del dominio
  - Implementación de Repositorios de dominio
  - Pruebas unitarias del modelo

#### Fase 3: Casos de Uso (Por iniciar)
- **Descripción**: Implementación de servicios de aplicación
- **Casos de uso a implementar**:
  - Crear/modificar tablero
  - Crear/mover tarjeta
  - Completar tarjeta
  - Agregar etiquetas

#### Fase 4: Persistencia (Por iniciar)
- **Descripción**: Implementación de repositorios con JPA
- **Componentes a desarrollar**:
  - Entidades JPA
  - Repositorios de datos
  - Migraciones de base de datos

#### Fase 5: API REST (Por iniciar)
- **Descripción**: Controladores REST para exponer funcionalidad
- **Endpoints a implementar**:
  - Gestión de tableros
  - Gestión de listas
  - Gestión de tarjetas

#### Fase 6: Interfaz JavaFX (Por iniciar)
- **Descripción**: Interfaz gráfica de escritorio
- **Vistas a desarrollar**:
  - Pantalla principal
  - Edición de tableros
  - Vista de tarjetas

### Decisiones de diseño

#### Arquitectura Hexagonal
La aplicación sigue el patrón de arquitectura hexagonal (puertos y adaptadores), permitiendo:
- Independencia de frameworks
- Facilidad para testing
- Separación clara de responsabilidades
- Facilidad para cambiar bases de datos o interfaces

#### Domain-Driven Design (DDD)
Implementación de DDD con:
- **Agregados**: Tablero, Lista, Tarjeta
- **Value Objects**: Etiqueta, Posición, Color
- **Repositorios de dominio**: Interfaces sin detalles de persistencia
- **Servicios de dominio**: Lógica de negocio compleja

### Referencias

- Proyecto base inspirado en ejemplos hexagonales de la asignatura
- Patrón de estructura Maven estándar
- Principios de Clean Code y SOLID
