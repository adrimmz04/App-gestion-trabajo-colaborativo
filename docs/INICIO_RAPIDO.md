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

### 0. Arranque recomendado en Windows con un solo comando

```powershell
.\start-app.cmd
```

Comportamiento:

- Si existe `app-mail.local.ps1`, la aplicación arranca con `local,gmail` y envío real de códigos por correo.
- Si no existe ese fichero, arranca con `local` y usa el modo desarrollo actual.


Configuración inicial solo la primera vez si quieres correo real:

```powershell
Copy-Item .\app-mail.local.example.ps1 .\app-mail.local.ps1
```

Rellena `app-mail.local.ps1` con tu Gmail real y tu contraseña de aplicación. Después, para abrir la app, solo necesitarás volver a ejecutar `.\start-app.cmd`.

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

### 3. Ejecución con correo real por SMTP

La lógica de autenticación ya soporta envío real de correo. Para no mezclar credenciales con el repositorio, el perfil `mail` toma la configuración desde variables de entorno.

### 3.1. Opción rápida con Gmail

Si quieres una prueba real sin depender de otro proveedor, usa el perfil `gmail`.

```bash
set APP_MAIL_USERNAME=tu_cuenta@gmail.com
set APP_MAIL_PASSWORD=tu_contrasena_de_aplicacion
set APP_MAIL_FROM=tu_cuenta@gmail.com
set APP_MAIL_SENDER_NAME=APP-GESTION
mvn spring-boot:run "-Dspring-boot.run.profiles=gmail"
```

Para una demo rápida con H2 en memoria:

```bash
set APP_MAIL_USERNAME=tu_cuenta@gmail.com
set APP_MAIL_PASSWORD=tu_contrasena_de_aplicacion
set APP_MAIL_FROM=tu_cuenta@gmail.com
set APP_MAIL_SENDER_NAME=APP-GESTION
mvn spring-boot:run "-Dspring-boot.run.profiles=local,gmail" "-DskipTests"
```

Notas específicas de Gmail:

- La cuenta remitente debe existir realmente.
- Usa contraseña de aplicación, no la contraseña normal de Gmail.
- El perfil `gmail` valida la conexión SMTP al arrancar para detectar enseguida credenciales erróneas.
- Si el envío se queda bloqueado resolviendo el host local en Windows, puedes fijar `APP_MAIL_SMTP_LOCALHOST=localhost`.

### 3.2. Opción rápida con Outlook

Si prefieres usar una cuenta Outlook personal, usa el perfil `outlook`.

```bash
set APP_MAIL_USERNAME=tu_cuenta@outlook.com
set APP_MAIL_PASSWORD=tu_contrasena_o_contrasena_de_aplicacion
set APP_MAIL_FROM=tu_cuenta@outlook.com
set APP_MAIL_SENDER_NAME=APP-GESTION
mvn spring-boot:run "-Dspring-boot.run.profiles=outlook"
```

Para una demo rápida con H2 en memoria:

```bash
set APP_MAIL_USERNAME=tu_cuenta@outlook.com
set APP_MAIL_PASSWORD=tu_contrasena_o_contrasena_de_aplicacion
set APP_MAIL_FROM=tu_cuenta@outlook.com
set APP_MAIL_SENDER_NAME=APP-GESTION
mvn spring-boot:run "-Dspring-boot.run.profiles=local,outlook" "-DskipTests"
```

Notas específicas de Outlook:

- El perfil `outlook` usa `smtp-mail.outlook.com` por el puerto `587`.
- Si la cuenta Microsoft tiene verificación en dos pasos, puede requerir contraseña de aplicación.
- Igual que en otros perfiles, puedes fijar `APP_MAIL_SMTP_LOCALHOST=localhost` si Windows da problemas de resolución local.

Ejemplo de arranque en Windows con un proveedor SMTP real:

```bash
set APP_MAIL_HOST=smtp-relay.brevo.com
set APP_MAIL_PORT=587
set APP_MAIL_USERNAME=tu-usuario-smtp
set APP_MAIL_PASSWORD=tu-clave-smtp
set APP_MAIL_FROM=no-reply@tu-dominio.com
set APP_MAIL_SENDER_NAME=APP-GESTION
mvn spring-boot:run "-Dspring-boot.run.profiles=mail"
```

Resultado esperado:

- API REST disponible en `http://localhost:8080`
- Ventana JavaFX iniciada
- El código temporal se envía por correo real y ya no se expone en la respuesta de desarrollo
- El remitente visible del mensaje puede configurarse, por ejemplo `APP-GESTION <tu-correo-remitente>`

Notas:

- El perfil `mail` está definido en `src/main/resources/application-mail.properties`.
- El perfil `gmail` está definido en `src/main/resources/application-gmail.properties`.
- El perfil `outlook` está definido en `src/main/resources/application-outlook.properties`.
- Si quieres mantener PostgreSQL y correo real a la vez, puedes arrancar con `-Dspring-boot.run.profiles=mail` sobre la configuración estándar.
- Si quieres H2 y correo real para una demo rápida, puedes usar `-Dspring-boot.run.profiles=local,mail`.

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

# Arrancar con SMTP real
set APP_MAIL_HOST=smtp-relay.brevo.com
set APP_MAIL_PORT=587
set APP_MAIL_USERNAME=tu-usuario-smtp
set APP_MAIL_PASSWORD=tu-clave-smtp
set APP_MAIL_FROM=no-reply@tu-dominio.com
set APP_MAIL_SENDER_NAME=APP-GESTION
mvn spring-boot:run "-Dspring-boot.run.profiles=mail"

# Arrancar con Gmail
set APP_MAIL_USERNAME=tu_cuenta@gmail.com
set APP_MAIL_PASSWORD=tu_contrasena_de_aplicacion
set APP_MAIL_FROM=tu_cuenta@gmail.com
set APP_MAIL_SENDER_NAME=APP-GESTION
mvn spring-boot:run "-Dspring-boot.run.profiles=gmail"

# Arrancar con Outlook
set APP_MAIL_USERNAME=tu_cuenta@outlook.com
set APP_MAIL_PASSWORD=tu_contrasena_o_contrasena_de_aplicacion
set APP_MAIL_FROM=tu_cuenta@outlook.com
set APP_MAIL_SENDER_NAME=APP-GESTION
mvn spring-boot:run "-Dspring-boot.run.profiles=outlook"
```

## Componentes disponibles al arrancar

- Backend Spring Boot con endpoints REST.
- Persistencia JPA con migraciones Flyway.
- Cliente JavaFX integrado en el mismo arranque.
- Caché de lectura para consultas de tableros.

## Recomendaciones de uso

- Usar PostgreSQL para validación funcional completa y persistencia real.
- Usar el perfil `local` cuando se necesite una ejecución rápida sin dependencias externas.
- Usar el perfil `mail` cuando se quiera demostrar la autenticación por correo con un proveedor SMTP real.
- Ejecutar `mvn clean test` antes de cualquier entrega o demostración.

## Referencias

- [README.md](../README.md)
- [FUNCIONALIDADES_IMPLEMENTADAS.md](FUNCIONALIDADES_IMPLEMENTADAS.md)
- [CREDITOS.md](../CREDITOS.md)
