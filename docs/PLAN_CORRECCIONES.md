# Plan de Correcciones del Proyecto

## Objetivo de este documento

Este documento deja un estado de trabajo persistente para poder continuar el analisis y la correccion del proyecto aunque se pierda el contexto de la conversacion o se agote la ventana de tokens.

Sirve para tres cosas:

1. Saber que partes del proyecto ya han sido auditadas.
2. Saber que problemas estan confirmados, cuales son hipotesis y cuales siguen pendientes de comprobar.
3. Retomar el trabajo por prioridad sin volver a explorar todo el repositorio.

---

## Estado global actual

### Resumen ejecutivo

El proyecto tiene una base buena en arquitectura y modelado, pero no esta completamente cerrado respecto al enunciado. Hay varias piezas que existen en codigo, pero no todas estan bien integradas, persistidas o probadas extremo a extremo.

### Veredicto actual

- Arquitectura: bien encaminada y bastante coherente con DDD + hexagonal.
- Dominio: razonablemente solido.
- Servicios: funcionales en muchos casos; ya se han corregido la caché insegura y la semantica del bloqueo al mover tarjetas.
- Persistencia: sigue siendo la zona mas delicada, pero ya se ha corregido la reconstruccion JPA suficiente para reactivar la integracion de tarjetas.
- JavaFX: existe, pero la UI principal parece mas prototipo que interfaz operativa completa.
- Testing: la capa unitaria esta bien; ya se ha reactivado la integracion de tarjetas y solo quedan omitidos los tests de repositorio con Testcontainers.
- Documentacion: claramente desactualizada y en algunos casos contradice el estado real del proyecto.

---

## Lo que ya esta hecho

### 1. Auditoria del enunciado frente al proyecto

Ya se ha contrastado el enunciado con:

- `README.md`
- `pom.xml`
- `docs/DISEÑO_DOMINIO.md`
- `docs/INICIO_RAPIDO.md`
- clases de `domain/`
- clases de `application/service/`
- controladores REST
- clases JavaFX principales
- entidades y repositorios JPA
- tests unitarios e integracion

### 2. Estado de pruebas ya verificado

Se ha ejecutado `mvn test` y el estado actual verificado es:

- 130 tests ejecutados
- 0 fallos
- 0 errores
- 9 omitidos
- BUILD SUCCESS

Antes de estas correcciones el estado verificado era:

- 128 tests detectados
- 111 tests ejecutados con exito
- 0 fallos
- 0 errores
- 17 omitidos

Tests omitidos actualmente:

- `RepositorioTableroJPAIntegrationTest`

### 3. Correcciones ya aplicadas y validadas

#### 3.1. Cache/autorizacion corregida

Se reprodujo el bug con un test de integracion que demostraba que una lectura autorizada dejaba una respuesta reutilizable para un usuario sin permisos.

Cambio aplicado:

- la clave de cache de `ServicioTablero.obtenerTablero(...)` ahora incluye tablero + usuario

Resultado:

- el test de integracion pasa
- el bypass de cache queda cerrado

Estado: corregido y validado.

#### 3.2. Reconstruccion JPA del agregado mejorada

Se corrigieron dos problemas en la lectura desde persistencia:

- conversion fuera de sesion sobre colecciones perezosas
- reconstruccion parcial del agregado al leer desde JPA

Cambios aplicados:

- metodos de lectura del repositorio JPA marcados como `@Transactional(readOnly = true)`
- reconstruccion de listas, tarjetas, etiquetas e historial en `ConvertidorTableroJPA`
- reactivacion de `ControladorTarjetaIntegrationTest`

Resultado:

- la integracion de tarjetas ya no falla con 500
- la suite de tarjetas vuelve a ejecutarse y pasa

Estado: corregido parcialmente y validado.

Nota:

- sigue pendiente cerrar por completo la persistencia de prerequisitos multiples y archivado de tarjetas

#### 3.3. Semantica del bloqueo corregida

Se alineo `ServicioLista.moverTarjeta(...)` con el enunciado y con la documentacion del dominio.

Cambio aplicado:

- un tablero bloqueado sigue permitiendo mover tarjetas entre listas

Resultado:

- `ServicioListaTest` actualizado y pasando

Estado: corregido y validado.

### 4. Hallazgos tecnicos ya confirmados

#### 4.1. Bug de autorizacion/cache confirmado

`ServicioTablero.obtenerTablero(...)` cachea por `idTablero` pero valida acceso con `emailUsuario`.

Riesgo:

- una respuesta autorizada puede quedar servida desde cache a otro usuario si comparte el mismo ID de tablero y la cache esta activa.

Estado: corregido y validado.

#### 4.2. Reconstruccion incompleta JPA -> dominio confirmada

`ConvertidorTableroJPA.convertirADominio(...)` no reconstruye de forma completa:

- listas
- tarjetas
- historial
- varias reglas derivadas del agregado
- estados persistidos relevantes de listas/tarjetas

Esto debilita la consistencia real del agregado una vez se lee desde BD.

Estado: corregido parcialmente y validado.

#### 4.3. Semantica del bloqueo contradice el enunciado

El enunciado indica que un tablero bloqueado no debe permitir anadir nuevas tarjetas, pero si mover tarjetas entre listas.

Sin embargo `ServicioLista.moverTarjeta(...)` impide mover tarjetas en tablero bloqueado.

Estado: corregido y validado.

#### 4.4. Compactacion automatica incompleta en integracion real

`ServicioCompactacion` usa `repositorioTablero.obtenerCompartidos("")` para localizar tableros a compactar.

Problemas:

- eso no representa "todos los tableros"
- es un atajo incorrecto
- la funcionalidad automatica puede no procesar realmente lo esperado

Estado: confirmado.

#### 4.5. Persistencia incompleta de estados de tarjeta

En dominio existen `archivada` y `fechaArchivado`, pero la entidad JPA no modela esos campos.

Consecuencia:

- la compactacion puede funcionar en tests unitarios
- pero no queda correctamente persistida en una ejecucion real con JPA

Estado: confirmado.

#### 4.6. Persistencia incompleta de reglas de listas previas

En dominio `Lista` soporta multiples `listasPrevias`, pero JPA usa un solo campo `listaPrerrequisitoId`.

Consecuencia:

- el modelo persistente no representa bien la capacidad del dominio
- las reglas de flujo pueden perderse o simplificarse indebidamente al persistir

Estado: confirmado.

#### 4.7. UI JavaFX principal no esta cerrada funcionalmente

La ventana principal contiene:

- datos estaticos en listados
- llamada comentada al servicio para crear tablero
- poca integracion visible con Spring

Consecuencia:

- la existencia de JavaFX es real
- pero la UI principal no parece una implementacion funcional completa del producto

Estado: confirmado.

#### 4.8. Arranque Spring + JavaFX fragil

La aplicacion arranca Spring en hilo separado y luego espera con `Thread.sleep(1000)` antes de lanzar JavaFX.

Consecuencia:

- dependencia temporal fragil
- riesgo de race condition

Estado: confirmado.

#### 4.9. Seguridad/autenticacion del enunciado no implementada

No se ha encontrado autenticacion por codigo de correo, OTP, token temporal ni servicio de email.

La identidad del usuario se simula pasando el email por query param.

Estado: confirmado.

#### 4.10. Modificacion de tablero solo parcial

El DTO de actualizacion acepta `titulo` y `descripcion`, pero el servicio solo actualiza la descripcion.

Estado: confirmado.

#### 4.11. Documentacion desactualizada o inconsistente

Se han detectado inconsistencias entre codigo y documentacion en:

- tipo de aplicacion (web frente a desktop + API)
- version de Java
- numero de tests
- fases ya completadas
- JavaFX marcada como opcional cuando el enunciado la exige

Estado: confirmado.

---

## Lo que todavia NO esta hecho

Esto es la parte importante para continuar sin perder el hilo.

### Bloque 1. Corregir defectos funcionales criticos

Estos son los primeros cambios que deberian hacerse antes de tocar mejoras cosmeticas o documentacion.

#### 1. Corregir bug de cache/autorizacion

Accion esperada:

- cambiar la clave de cache de `obtenerTablero(...)` para que incluya el usuario, o eliminar cache en ese metodo si no hay una estrategia segura clara

Validacion necesaria:

- test unitario o de integracion que pruebe acceso con propietario, compartido y usuario sin permisos
- prueba con cache activa

Estado: corregido y validado.

#### 2. Corregir reconstruccion JPA -> dominio

Accion esperada:

- rehacer `ConvertidorTableroJPA.convertirADominio(...)`
- restaurar listas, tarjetas, historial, etiquetas y demas campos relevantes
- evitar que la lectura desde BD devuelva un agregado incompleto

Validacion necesaria:

- reactivar o arreglar tests de integracion de tarjetas
- ampliar test de repositorio para verificar round-trip completo

Estado: corregido parcialmente y validado.

#### 3. Corregir comportamiento del bloqueo

Accion esperada:

- permitir mover tarjetas aunque el tablero este bloqueado
- seguir impidiendo crear nuevas tarjetas y nuevas listas si esa es la regla definida

Nota:

- antes de editar conviene fijar exactamente la interpretacion del enunciado que se va a seguir
- ahora mismo el enunciado apunta claramente a permitir movimientos durante el bloqueo

Validacion necesaria:

- actualizar tests de `ServicioListaTest`
- anadir test de integracion si existe endpoint o flujo equivalente

Estado: corregido y validado.

### Bloque 2. Cerrar persistencia para reglas y compactacion

#### 4. Persistir correctamente `listasPrevias`

Accion esperada:

- sustituir el modelo JPA simplificado actual por uno que soporte multiples prerequisitos
- revisar migraciones Flyway y el convertidor

Validacion necesaria:

- guardar lista con varios prerequisitos
- leerla desde JPA
- verificar que se reconstruye igual en dominio

Estado: pendiente de implementar.

#### 5. Persistir correctamente `archivada` y `fechaArchivado`

Accion esperada:

- ampliar `TarjetaJPA`
- actualizar migraciones
- actualizar convertidores

Validacion necesaria:

- test de compactacion con persistencia real
- round-trip JPA de tarjeta archivada

Estado: pendiente de implementar.

#### 6. Corregir estrategia de seleccion de tableros para compactacion automatica

Accion esperada:

- crear un metodo de repositorio para recuperar todos los tableros o una estrategia equivalente correcta
- eliminar el uso de `obtenerCompartidos("")`

Validacion necesaria:

- test del scheduler o del metodo de ejecucion automatica
- comprobar que procesa tableros reales guardados

Estado: pendiente de implementar.

### Bloque 3. Reactivar pruebas de integracion relevantes

#### 7. Reactivar `ControladorTarjetaIntegrationTest`

Objetivo:

- eliminar el `@Disabled`
- entender la causa exacta del 500 actual
- dejar los endpoints de tarjetas cubiertos

Resultado actual:

- el `@Disabled` se ha eliminado
- la suite vuelve a ejecutarse
- la integracion de tarjetas pasa completa

Estado: corregido y validado.

#### 8. Reactivar `RepositorioTableroJPAIntegrationTest`

Objetivo:

- dejar la persistencia del agregado verificada con datos reales

Nota:

- si Docker no esta disponible, se puede adaptar el alcance a H2 para no depender de Testcontainers en esta fase

Estado: pendiente de decidir estrategia.

### Bloque 4. Cerrar JavaFX de forma defendible

#### 9. Decidir alcance real de JavaFX

Hay que tomar una decision tecnica antes de invertir mas tiempo:

Opcion A:

- dejar JavaFX como UI minima pero funcional
- conectada a servicios reales
- suficiente para defender el requisito del enunciado

Opcion B:

- asumir que la API REST es la interfaz principal
- y convertir JavaFX en una shell ligera realmente integrada

Ahora mismo la ventana principal parece demasiado estatica para venderla como UI completa.

Estado: pendiente de decision.

#### 10. Arreglar integracion Spring + JavaFX

Accion esperada:

- eliminar dependencia de `Thread.sleep(1000)`
- usar una estrategia de integracion mas limpia entre contexto Spring y JavaFX

Estado: pendiente.

#### 11. Conectar la UI principal a casos de uso reales

Minimo recomendable:

- cargar tableros reales
- crear tableros realmente
- visualizar algun detalle real
- evitar datos de ejemplo hardcodeados en la ventana principal

Estado: pendiente.

### Bloque 5. Documentacion y cierre academico

#### 12. Actualizar README

Debe reflejar:

- estado real del proyecto
- arquitectura real
- tecnologias realmente usadas
- numero actual de tests
- limitaciones conocidas si no se llegan a corregir todas

Estado: pendiente.

#### 13. Actualizar `docs/INICIO_RAPIDO.md`

Debe corregir:

- version de Java
- numero de pruebas
- estado real de las fases
- JavaFX ya no como opcional si se quiere alinear con el enunciado

Estado: pendiente.

#### 14. Actualizar `CREDITOS.md`

Ahora mismo el documento esta anclado en fases antiguas y no refleja el estado real del proyecto.

Estado: pendiente.

#### 15. Preparar memoria de defensa del proyecto

Si el objetivo es la entrega academica, conviene dejar claro:

- que requisitos obligatorios se cumplen
- cuales opcionales estan implementados realmente
- que limitaciones quedan si no se resuelven todos los defectos

Estado: pendiente.

---

## Priorizacion recomendada

### Prioridad P0 - hacer primero

1. Cache/autorizacion. Completado.
2. Convertidor JPA -> dominio. Corregido parcialmente.
3. Semantica del bloqueo. Completado.
4. Reactivar integracion de tarjetas. Completado.

### Prioridad P1 - hacer despues

5. Persistencia de `listasPrevias`.
6. Persistencia de archivado/fechaArchivado.
7. Compactacion automatica real.
8. Tests de repositorio.

### Prioridad P2 - cierre y presentacion

9. JavaFX funcional minima.
10. Arranque Spring + JavaFX.
11. README.
12. INICIO_RAPIDO.
13. CREDITOS.

---

## Estrategia de trabajo recomendada

### Fase A. Arreglar lo que cambia el comportamiento real

Orden sugerido:

1. `ServicioTablero` cache/autorizacion.
2. `ConvertidorTableroJPA` y entidades JPA relacionadas.
3. `ServicioLista` bloqueo.
4. `ControladorTarjetaIntegrationTest`.

### Fase B. Cerrar persistencia y automatismos

Orden sugerido:

5. prerequisitos de listas.
6. archivado de tarjetas.
7. compactacion automatica.
8. pruebas de repositorio.

### Fase C. Cierre de producto y entrega

Orden sugerido:

9. JavaFX minima funcional.
10. documentacion.
11. repaso final contra enunciado.

---

## Riesgos importantes a vigilar durante las correcciones

### Riesgo 1. Cambios en persistencia que rompan tests existentes

Al corregir el convertidor y el modelo JPA pueden caer tests de controladores o servicios que hasta ahora pasaban porque trabajaban con un agregado reconstruido de forma simplificada.

Mitigacion:

- hacer cambios pequenos
- validar tras cada bloque

### Riesgo 2. Migraciones Flyway inconsistentes con entidades JPA

Si se anaden campos o relaciones nuevas, hay que revisar tambien los scripts SQL.

Mitigacion:

- no tocar JPA sin revisar `db/migration/`

### Riesgo 3. JavaFX absorba tiempo sin mejorar la nota real

La UI puede consumir mucho esfuerzo si se intenta cerrar demasiado.

Mitigacion:

- apuntar a una interfaz minima pero real
- no intentar rehacer todo el frontend

### Riesgo 4. Corregir demasiado sin actualizar la narrativa del proyecto

Si el codigo mejora pero README, docs y creditos siguen mal, la entrega pierde fuerza.

Mitigacion:

- reservar una fase final solo para documentacion

---

## Punto exacto de reanudacion

Si se pierde el contexto, continuar desde aqui:

### Siguiente objetivo recomendado

Empezar por cerrar la persistencia que sigue incompleta en JPA.

### Motivo

Las correcciones mas urgentes ya estan aplicadas. El siguiente bloque con mayor retorno tecnico es terminar la persistencia de reglas y archivado, porque es donde sigue habiendo desajuste real entre dominio y base de datos.

### Despues de eso

Ir a documentacion y JavaFX solo despues de cerrar esas dos persistencias.

### Orden exacto para retomar

1. Revisar modelo JPA de `ListaJPA` y migraciones asociadas para soportar multiples `listasPrevias`.
2. Ajustar `ConvertidorTableroJPA` para persistir y reconstruir correctamente esos prerequisitos.
3. Ampliar tests de integracion o repositorio para verificar round-trip de prerequisitos.
4. Revisar `TarjetaJPA` y migraciones para anadir `archivada` y `fechaArchivado`.
5. Ajustar `ServicioCompactacion` y su lectura/escritura real con persistencia.
6. Ejecutar `mvn test`.
7. Solo despues, pasar a README, INICIO_RAPIDO y JavaFX.

---

## Checklist resumido de seguimiento

### Confirmado hecho

- auditoria del proyecto completada
- comparacion contra enunciado completada
- zonas de riesgo identificadas
- bug de cache/autorizacion corregido y validado
- reconstruccion JPA suficiente para listas/tarjetas corregida y validada
- integracion de tarjetas reactivada y validada
- semantica del bloqueo corregida y validada
- estado de tests actual verificado
- prioridades definidas

### Pendiente de implementar

- persistencia de prerequisitos multiples
- persistencia de archivado
- compactacion automatica real
- decidir y cerrar alcance JavaFX
- actualizar documentacion

---

## Criterio de cierre

Se podra considerar este plan cerrado cuando se cumpla lo siguiente:

1. No haya defectos funcionales criticos abiertos en cache, bloqueo y persistencia.
2. Los tests de integracion importantes no esten desactivados sin justificacion fuerte.
3. La documentacion refleje el estado real del proyecto.
4. La defensa del proyecto frente al enunciado sea consistente y sin contradicciones obvias.
