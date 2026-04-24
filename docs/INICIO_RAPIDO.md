# Guía de Puesta en Marcha

Este documento describe qué se necesita para ejecutar el proyecto, cómo validarlo localmente y qué modos de arranque están disponibles.

## Requisitos previos

- JDK 21 o superior
- Maven 3.8.1 o superior
- Git
- PostgreSQL local si se quiere usar la configuración estándar
- IDE recomendado: VS Code o IntelliJ IDEA con soporte Java

## Clonado del repositorio

```bash
git clone https://github.com/adrimmz04/App-gestion-trabajo-colaborativo.git
cd App-gestion-trabajo-colaborativo/app-gestion
```

## Validación del proyecto

La suite de tests usa H2 en memoria, por lo que no depende de una base de datos PostgreSQL externa.

```bash
mvn clean test
```

Última validación completa registrada en este entorno:

- 169 tests ejecutados
- 0 fallos
- 0 errores

## Modos de ejecución

### 1. Ejecución estándar con PostgreSQL

La configuración por defecto usa PostgreSQL con estos valores:

- URL: `jdbc:postgresql://localhost:5432/app_gestion`
- Usuario: `postgres`
- Contraseña: `postgres`

Preparación mínima:

```sql
CREATE DATABASE app_gestion;
```

Flyway aplicará automáticamente las migraciones necesarias al arrancar.

Arranque:

```bash
mvn spring-boot:run
```

Resultado esperado:

- API REST disponible en `http://localhost:8080`
- Ventana JavaFX iniciada junto con la aplicación

### 2. Ejecución local rápida con H2

Para desarrollo o demostración rápida sin PostgreSQL, se puede usar el perfil `local`.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local -DskipTests
```

Resultado esperado:

- API REST disponible en `http://localhost:8080`
- Ventana JavaFX iniciada
- Persistencia en H2 en memoria

## Empaquetado

```bash
mvn clean package
```

El artefacto generado queda disponible en `target/`.

## Comandos útiles

```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar todos los tests
mvn clean test

# Empaquetar el proyecto
mvn clean package

# Arrancar con PostgreSQL
mvn spring-boot:run

# Arrancar con H2 en memoria
mvn spring-boot:run -Dspring-boot.run.profiles=local -DskipTests
```

## Componentes disponibles al arrancar

- Backend Spring Boot con endpoints REST.
- Persistencia JPA con migraciones Flyway.
- Cliente JavaFX integrado en el mismo arranque.
- Caché de lectura para consultas de tableros.

## Recomendaciones de uso

- Usar PostgreSQL para validación funcional completa y persistencia real.
- Usar el perfil `local` cuando se necesite una ejecución rápida sin dependencias externas.
- Ejecutar `mvn clean test` antes de cualquier entrega o demostración.

## Referencias

- [README.md](../README.md)
- [FUNCIONALIDADES_IMPLEMENTADAS.md](FUNCIONALIDADES_IMPLEMENTADAS.md)
- [CREDITOS.md](../CREDITOS.md)
