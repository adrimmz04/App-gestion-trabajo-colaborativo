# Créditos y Participación

## Adrian Martinez Zamora

El estado actual del repositorio refleja trabajo individual sobre análisis, implementación, persistencia, pruebas y documentación. La evidencia principal de participación está en el historial de commits del proyecto.

### Áreas de contribución

#### Arquitectura y dominio
- Definición de la estructura hexagonal del proyecto.
- Modelado de agregados y reglas principales de tableros, listas y tarjetas.
- Separación en capas `domain`, `application`, `infrastructure` y `ui`.

#### Casos de uso y API REST
- Implementación de servicios de aplicación para tableros, listas, tarjetas, plantillas y compactación.
- Exposición de endpoints REST para operaciones principales del sistema.
- Manejo de errores y DTOs de entrada y salida.

#### Persistencia y base de datos
- Implementación del adaptador JPA del agregado `Tablero`.
- Mapeo de entidades JPA y migraciones Flyway.
- Ajustes posteriores de consistencia para prerequisitos múltiples, archivado de tarjetas, actualización completa de tablero y preservación de fechas.

#### Testing e integración
- Suite unitaria de dominio y servicios.
- Tests de integración de controladores y repositorio.
- Revisión y reactivación de pruebas que habían quedado omitidas en fases anteriores.

#### Documentación
- Redacción y actualización de README, guía de inicio rápido y documentación técnica.
- Mantenimiento del plan interno de correcciones para continuar el trabajo sin perder contexto.

### Evidencias representativas en commits

Algunos commits relevantes para valorar la participación:

- `5919607`: correcciones de persistencia JPA y tests de integración.
- `d3e1107`: correcciones relacionadas con persistencia del estado de bloqueo.
- `1ed2463`: compactación automática con archivado y eliminación.
- `630e7ad`: consolidación de suite de tests en verde.
- `a3b9425`: arreglos de integración.
- `5821a45`: ampliación de documentación.
- `afab88c`: actualización del README.

### Nota sobre evidencias de colaboración

En el estado actual del repositorio, la evidencia más clara y verificable son los commits. No se documentan aquí PRs, issues o discusiones porque no aportan más señal que el propio historial disponible en local.

### Decisiones de diseño relevantes

#### Arquitectura hexagonal
- El dominio no depende de Spring ni de JPA.
- La persistencia y la interfaz se conectan mediante puertos y adaptadores.
- Esta separación facilita pruebas, mantenimiento y sustitución de infraestructura.

#### Enfoque DDD
- `Tablero` actúa como agregado raíz principal.
- `Lista` y `Tarjeta` quedan protegidas por las invariantes del agregado.
- El modelo intenta concentrar reglas de negocio en dominio y usar servicios de aplicación para orquestación.
