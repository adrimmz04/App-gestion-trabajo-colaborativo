# Funcionalidades Implementadas

Este documento resume la funcionalidad entregada y explica, de forma breve, cómo se ha resuelto técnicamente dentro del proyecto.

## Resumen general

La aplicación cubre los requisitos obligatorios del enunciado sobre tableros colaborativos y añade varias características opcionales con soporte real en dominio, servicios, persistencia, API REST y cliente JavaFX.

## Funcionalidad base

### Tableros
- Creación, consulta, edición y eliminación de tableros.
- Compartición de tableros con otros usuarios identificados por email.
- Bloqueo y desbloqueo temporal.

Cómo se ha implementado:
- `Tablero` actúa como agregado principal del dominio.
- Los casos de uso se orquestan desde `ServicioTablero`.
- La persistencia se resuelve mediante repositorio JPA y conversiones entre dominio y entidades.

### Listas
- Alta y eliminación de listas dentro de cada tablero.
- Configuración de reglas por lista.
- Movimiento de tarjetas entre listas.

Cómo se ha implementado:
- Cada `Lista` pertenece al agregado `Tablero`.
- `ServicioLista` valida reglas de negocio antes de mover tarjetas o aplicar restricciones.

### Tarjetas
- Creación de tarjetas de tipo tarea y checklist.
- Edición de descripción.
- Marcado como completada y descompletada.
- Soporte de archivado.
- Movimiento automático a una lista especial de completadas cuando existe.

Cómo se ha implementado:
- `Tarjeta` encapsula su estado y sus cambios relevantes.
- `ServicioTarjeta` coordina la lógica de creación, actualización, completado, etiquetas y filtrado.

### Etiquetas
- Etiquetas con nombre y color.
- Asociación de múltiples etiquetas por tarjeta.
- Recuperación y filtrado por etiquetas.

Cómo se ha implementado:
- `Etiqueta` funciona como value object validado.
- Las tarjetas mantienen su conjunto de etiquetas y el filtrado se resuelve en la capa de aplicación.

### Historial de acciones
- Registro de acciones relevantes del tablero, como creación, movimientos o cambios funcionales.

Cómo se ha implementado:
- El agregado `Tablero` conserva un historial basado en registros de acción.
- Los servicios añaden trazas cuando se ejecutan operaciones de negocio relevantes.

### Interfaz y API
- API REST para tableros, listas, tarjetas, reglas y operaciones principales.
- Cliente JavaFX conectado a servicios reales.

Cómo se ha implementado:
- Adaptadores REST en `ui/controller`.
- Cliente JavaFX en `ui/javafx`, integrado con Spring Boot para reutilizar servicios de aplicación.

## Características opcionales implementadas

### 1. Reglas a nivel de lista
- Límite máximo de tarjetas por lista.
- Prerequisitos entre listas.

Cómo se ha hecho:
- `Lista` almacena `limiteMaximo` y `listasPrevias`.
- La validación no depende solo del estado completado: una tarjeta debe haber pasado por las listas requeridas.
- Ese recorrido se persiste mediante `listasVisitadas` en tarjeta y una migración Flyway específica.

### 2. Filtrado de tarjetas por etiquetas
- Recuperación de tarjetas filtradas por etiquetas desde la API y desde la UI.

Cómo se ha hecho:
- `ServicioTarjeta` ofrece operaciones de filtrado.
- La interfaz JavaFX reutiliza ese comportamiento para mostrar subconjuntos de tarjetas según etiquetas seleccionadas.

### 3. Plantillas YAML
- Exportación de tableros a YAML.
- Importación de tableros desde plantillas YAML.
- Disponibilidad de plantillas predefinidas.

Cómo se ha hecho:
- `ServicioPlantillas` gestiona serialización y deserialización con SnakeYAML.
- `ServicioTablero` expone los casos de uso de importación y exportación.

### 4. Compactación automática
- Archivado automático de tarjetas completadas tras un periodo configurable.
- Eliminación diferida de tarjetas archivadas tras otro periodo configurable.

Cómo se ha hecho:
- `ServicioCompactacion` concentra la lógica de compactación.
- La persistencia conserva el estado `archivada`, la fecha de archivado y las fechas necesarias para aplicar reglas temporales.

## Decisiones técnicas relevantes

### Arquitectura hexagonal con DDD
- El dominio no depende de Spring ni de JPA.
- La lógica principal de negocio se concentra en entidades y servicios de aplicación.
- Persistencia, REST y JavaFX actúan como adaptadores alrededor del dominio.

### Persistencia y consistencia
- JPA se utiliza como mecanismo de persistencia del agregado principal.
- Flyway controla la evolución del esquema.
- Se añadió validación específica del esquema migrado para detectar desviaciones entre JPA y base de datos.

### Calidad y pruebas
- El proyecto incluye tests unitarios, de integración REST y de persistencia.
- La última validación completa terminó con 169 tests en verde.
- También se verificó el arranque real con PostgreSQL local.

## Limitaciones conocidas

- No se implementa autenticación por código enviado por correo.
- No hay permisos granulares por tarjeta.
- La interfaz JavaFX cubre el alcance académico del proyecto, pero no todos los escenarios de una aplicación de producto final.

## Referencias

- [README.md](../README.md)
- [INICIO_RAPIDO.md](INICIO_RAPIDO.md)
- [CREDITOS.md](../CREDITOS.md)