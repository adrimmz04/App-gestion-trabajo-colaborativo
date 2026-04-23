# Guía de Inicio Rápido

## Requisitos previos

- JDK 21 o superior
- Maven 3.8.1 o superior
- Git
- PostgreSQL local si se quiere arrancar la aplicación con la configuración por defecto
- IDE recomendado: VS Code o IntelliJ IDEA con soporte Java

## 1. Clonar el repositorio

```bash
git clone https://github.com/adrimmz04/App-gestion-trabajo-colaborativo.git
cd App-gestion-trabajo-colaborativo
```

## 2. Validar el estado del proyecto

Los tests usan H2 en memoria, así que no necesitan PostgreSQL externo.

```bash
mvn clean test
```

Estado validado el 22/04/2026:

- 134 tests ejecutados
- 0 fallos
- 0 errores
- 0 omitidos

## 3. Configurar la base de datos para ejecución normal

La aplicación arranca por defecto contra PostgreSQL con esta configuración:

- URL: `jdbc:postgresql://localhost:5432/app_gestion`
- Usuario: `postgres`
- Contraseña: `postgres`

Ejemplo mínimo en PostgreSQL:

```sql
CREATE DATABASE app_gestion;
```

Flyway aplicará las migraciones al iniciar la aplicación.

## 4. Ejecutar la aplicación

```bash
mvn spring-boot:run
```

Resultado esperado:

- API REST en `http://localhost:8080`
- Arranque de la ventana JavaFX principal

### Alternativa rápida sin PostgreSQL

Si no quieres levantar PostgreSQL para una prueba local, usa el perfil `local`, que arranca con H2 en memoria:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local -DskipTests
```

Resultado esperado:

- API REST en `http://localhost:8080`
- Arranque de la ventana JavaFX principal
- Sin conexión a PostgreSQL ni ejecución de Flyway sobre servidor externo

## 5. Estructura útil del proyecto

```
app-gestion/
├── src/main/java/pds/app_gestion/domain/          # Modelo de dominio
├── src/main/java/pds/app_gestion/application/     # Casos de uso y DTOs
├── src/main/java/pds/app_gestion/infrastructure/  # Persistencia y adaptadores
├── src/main/java/pds/app_gestion/ui/              # REST y JavaFX
├── src/main/resources/db/migration/               # Migraciones Flyway
├── src/test/                                      # Tests unitarios e integración
└── docs/                                          # Documentación del proyecto
```

## Comandos útiles

```bash
# Compilar sin tests
mvn clean compile

# Ejecutar toda la suite
mvn clean test

# Empaquetar el proyecto
mvn clean package

# Ejecutar la aplicación
mvn spring-boot:run
```

## Estado funcional resumido

- Backend Spring Boot operativo
- Persistencia JPA con PostgreSQL en ejecución normal
- H2 aislado para tests
- Reglas de listas, plantillas YAML y compactación automática disponibles
- Interfaz JavaFX presente pero todavía parcial

## Limitaciones actuales

- La autenticación por código por correo del enunciado no está implementada.
- La interfaz JavaFX no sustituye todavía todos los flujos REST.
- El arranque conjunto Spring Boot + JavaFX sigue siendo mejorable.

## Referencias

- [README.md](../README.md)
- [DISEÑO_DOMINIO.md](DISEÑO_DOMINIO.md)
- [PLAN_CORRECCIONES.md](PLAN_CORRECCIONES.md)
- [CREDITOS.md](../CREDITOS.md)
