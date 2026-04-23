# Memoria de Defensa

## Objetivo

Este documento resume el estado real del proyecto de cara a la entrega academica. Sirve como guia de defensa para explicar que requisitos del enunciado se cumplen, que caracteristicas opcionales estan implementadas, que evidencia tecnica existe y que limitaciones siguen abiertas.

## Resumen ejecutivo

El proyecto entrega una aplicacion de gestion de trabajo colaborativo basada en tableros, listas y tarjetas, implementada en Java con Spring Boot, JPA, Maven y una interfaz JavaFX minima funcional. La base tecnica esta estabilizada: la persistencia critica ya esta corregida, la documentacion publica esta alineada y la suite de pruebas valida las rutas principales del sistema.

Estado verificado:

- `mvn clean test`
- 134 tests ejecutados
- 0 fallos
- 0 errores
- 0 omitidos

## Cumplimiento de requisitos obligatorios

### Java

Cumplido.

- Proyecto en Java 21.
- Compilacion Maven alineada con la version declarada.

### Arquitectura Hexagonal con DDD

Cumplido.

- Separacion en capas `domain`, `application`, `infrastructure` y `ui`.
- El dominio mantiene reglas principales del agregado `Tablero`.
- La persistencia y la interfaz se conectan mediante puertos y adaptadores.

### Backend con Spring Boot

Cumplido.

- Servicios de aplicacion y controladores REST operativos.
- Manejo de errores y DTOs de entrada y salida.

### Persistencia con JPA

Cumplido.

- Adaptador JPA del agregado `Tablero`.
- Migraciones Flyway.
- Soporte para PostgreSQL en ejecucion normal y H2 en tests.
- Round-trip principal del agregado validado tras las correcciones recientes.

### Maven

Cumplido.

- Construccion, tests y empaquetado gestionados con Maven.

### Interfaz JavaFX

Cumplido en alcance minimo funcional.

- Arranque integrado con Spring sin `Thread.sleep`.
- Carga de tableros reales por email.
- Creacion de tableros desde la ventana principal.
- Apertura de detalle de tablero, listado de listas, alta de listas y navegacion al detalle de tarjetas por lista.

### Pruebas de software

Cumplido.

- Tests unitarios y de integracion activos.
- Sin suites relevantes deshabilitadas en el estado actual.

## Funcionalidad principal del enunciado

### Tableros, listas y tarjetas

Cumplido.

- Crear y modificar tableros.
- Crear listas en un tablero.
- Crear y gestionar tarjetas.

### Tarjetas completadas

Cumplido.

- Las tarjetas pueden marcarse como completadas.
- El sistema conserva este estado y lo usa en compactacion y estadisticas.

### Etiquetas

Cumplido.

- Las tarjetas soportan etiquetas con nombre y color.

### Dos tipos de tarjeta

Cumplido.

- Tarjetas de tarea y de checklist.

### Historial de acciones

Cumplido.

- El agregado `Tablero` registra acciones relevantes sobre su estado.

### Bloqueo temporal del tablero

Cumplido.

- El tablero bloqueado impide altas nuevas y mantiene la semantica correcta de movimientos entre listas.

### Creacion y comparticion por email/URL

Cumplido en el modelo actual del proyecto.

- Cualquier usuario identificado por email puede crear tableros.
- El acceso a tableros se basa en su identificador y comparticion con otros usuarios.

## Caracteristicas opcionales implementadas

El enunciado pide al menos dos opcionales y para la maxima nota al menos cuatro. En el estado actual del codigo hay evidencia clara de cuatro lineas opcionales implementadas:

### 1. Reglas a nivel de lista

Cumplido.

- Limite maximo de tarjetas por lista.
- Prerequisitos entre listas (`listasPrevias`).
- Persistencia corregida y validada para prerequisitos multiples.

### 2. Filtrado de tarjetas por etiquetas

Cumplido.

- Soporte en servicio de tarjetas para recuperar tarjetas filtradas por etiquetas.
- Soporte en JavaFX mediante `FiltroEtiquetasPanel` y `PanelDetallesTablero`.

### 3. Plantillas YAML

Cumplido.

- Servicio especifico para crear y reutilizar plantillas.
- Uso de SnakeYAML en el proyecto.

### 4. Compactacion automatica

Cumplido.

- Archivado y eliminacion diferida de tarjetas.
- Ejecucion automatica y estadisticas de compactacion.
- Persistencia corregida para `archivada` y `fechaArchivado`.

## Evidencia tecnica relevante

### Calidad y estabilidad

- Persistencia critica corregida para prerequisitos multiples, archivado de tarjetas y fechas de creacion.
- Test de repositorio JPA reactivado sin depender de Docker en este entorno.
- Cache de lectura de tableros corregida para no mezclar permisos entre usuarios.
- Actualizacion de tablero corregida para aplicar titulo y descripcion.

### Pruebas activas

- Tests unitarios de dominio.
- Tests de servicios de aplicacion.
- Tests de integracion REST.
- Tests de persistencia enfocados.
- Suite completa en verde con `mvn clean test`.

## Limitaciones conocidas

### Autenticacion basada en codigo por correo

No implementada.

- El enunciado la propone como opcional.
- El proyecto actual identifica al usuario por email en las peticiones.

### Permisos finos por tarjeta

No implementados.

- Existe comparticion de tableros, pero no control granular de lectura/escritura por tarjeta.

### JavaFX

Implementada como interfaz minima funcional, no como cliente de escritorio completo.

- La defensa debe presentar JavaFX como cumplimiento del requisito de interfaz, no como una UX cerrada de producto final.

## Argumento de defensa recomendado

La defensa mas solida no es afirmar que el proyecto esta totalmente cerrado en todos los frentes, sino mostrar que:

1. Los requisitos obligatorios estan cubiertos por una base tecnica coherente.
2. La persistencia, que era la zona mas delicada, esta ya estabilizada en los casos importantes.
3. Hay al menos cuatro opcionales con evidencia en codigo.
4. La documentacion y las pruebas ya no contradicen el estado real del repositorio.
5. Las limitaciones abiertas se reconocen de forma explicita y no invalidan el nucleo entregado.

## Guion breve de demostracion

Un orden razonable para enseñar el proyecto seria:

1. Mostrar la arquitectura general del repositorio.
2. Ejecutar `mvn clean test` o enseñar el ultimo estado validado.
3. Enseñar creacion y consulta de tableros.
4. Enseñar listas, tarjetas, etiquetas y bloqueo.
5. Mencionar las opcionales implementadas: reglas de listas, filtrado por etiquetas, plantillas YAML y compactacion automatica.
6. Mostrar JavaFX como cliente minimo funcional conectado a servicios reales.
7. Cerrar explicando limitaciones conocidas sin ocultarlas.

## Referencias utiles

- [README.md](../README.md)
- [INICIO_RAPIDO.md](INICIO_RAPIDO.md)
- [DISEÑO_DOMINIO.md](DISEÑO_DOMINIO.md)
- [PLAN_CORRECCIONES.md](PLAN_CORRECCIONES.md)
- [CREDITOS.md](../CREDITOS.md)
