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
- Servicios: funcionales en muchos casos; ya se han corregido la cache insegura, la semantica del bloqueo al mover tarjetas y la seleccion de tableros para compactacion automatica.
- Persistencia: sigue siendo la zona mas delicada, pero ya quedan cubiertos el round-trip principal del agregado, los prerequisitos multiples de listas, el archivado de tarjetas y la preservacion de fechas de creacion.
- JavaFX: ya dispone de una integracion minima funcional para crear y cargar tableros por email, pero todavia no cubre el producto completo.
- Testing: la capa unitaria e integracion relevante estan en verde; ya se han reactivado tanto la integracion de tarjetas como la del repositorio JPA y no quedan tests omitidos tras `mvn clean test`.
- Documentacion: la documentacion publica principal ya esta alineada con el estado real; queda pendiente el cierre final segun el alcance definitivo de JavaFX y autenticacion.

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

Se ha ejecutado `mvn clean test` y el estado actual verificado es:

- 134 tests ejecutados
- 0 fallos
- 0 errores
- 0 omitidos
- BUILD SUCCESS

Antes de estas correcciones el estado verificado era:

- 128 tests detectados
- 111 tests ejecutados con exito
- 0 fallos
- 0 errores
- 17 omitidos

Tests omitidos actualmente:

- ninguno

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
- reactivacion de `RepositorioTableroJPAIntegrationTest` sobre H2 para validar el round-trip del agregado sin depender de Docker
- preservacion de `fechaCreacion` al persistir por primera vez en `TableroJPA` y `TarjetaJPA`

Resultado:

- la integracion de tarjetas ya no falla con 500
- la suite de tarjetas vuelve a ejecutarse y pasa
- la suite de repositorio vuelve a ejecutarse y pasa

Estado: corregido y validado en las rutas actualmente cubiertas.

#### 3.3. Semantica del bloqueo corregida

Se alineo `ServicioLista.moverTarjeta(...)` con el enunciado y con la documentacion del dominio.

Cambio aplicado:

- un tablero bloqueado sigue permitiendo mover tarjetas entre listas

Resultado:

- `ServicioListaTest` actualizado y pasando

Estado: corregido y validado.

#### 3.4. Persistencia de `listasPrevias` corregida

Se cerro la divergencia entre dominio y JPA para prerequisitos multiples de listas.

Cambio aplicado:

- `ListaJPA` ahora persiste `listasPrevias` mediante coleccion dedicada
- se anadio migracion Flyway para `lista_prerequisitos`
- el convertidor JPA ya reconstruye todos los prerequisitos al volver a dominio

Resultado:

- el round-trip JPA conserva multiples prerequisitos
- `ServicioListaPersistenceIntegrationTest` cubre el caso y pasa

Estado: corregido y validado.

#### 3.5. Persistencia de archivado de tarjetas corregida

Se alineo el modelo JPA de tarjetas con el estado real del dominio.

Cambio aplicado:

- `TarjetaJPA` ahora persiste `archivada` y `fechaArchivado`
- se anadio migracion Flyway para las columnas nuevas
- el convertidor JPA ya reconstruye ese estado al volver a dominio

Resultado:

- el round-trip de tarjetas archivadas deja de perder informacion
- `ServicioListaPersistenceIntegrationTest` cubre el caso y pasa

Estado: corregido y validado.

#### 3.6. Compactacion automatica corregida

Se elimino el atajo incorrecto que intentaba descubrir todos los tableros usando `obtenerCompartidos("")`.

Cambio aplicado:

- el puerto `RepositorioTablero` expone `obtenerTodos()`
- las implementaciones JPA y en memoria lo soportan
- `ServicioCompactacion` usa ya esa via explicita

Resultado:

- la compactacion automatica opera sobre el conjunto correcto de tableros
- `ServicioCompactacionTest` cubre el caso y pasa

Estado: corregido y validado.

#### 3.7. Actualizacion completa de tablero corregida

Se cerro la divergencia entre el DTO de actualizacion y la mutacion real del agregado.

Cambio aplicado:

- `ServicioTablero.actualizarTablero(...)` ya aplica tanto `titulo` como `descripcion`
- el agregado `Tablero` expone mutacion explicita de titulo
- se reforzaron tests de servicio, controlador e integracion JPA para exigir el cambio de titulo

Resultado:

- la actualizacion de tablero deja de ignorar silenciosamente el titulo
- el cambio queda cubierto a nivel de servicio, API e infraestructura

Estado: corregido y validado.

#### 3.8. Configuracion de compilacion Java alineada

Se elimino la discrepancia entre la version declarada del proyecto y la del compilador Maven.

Cambio aplicado:

- `maven-compiler-plugin` usa ya las propiedades globales de Java 21 en lugar de fijar 17 manualmente

Resultado:

- la compilacion queda alineada con el `java.version` declarado por el proyecto
- la validacion sigue pasando tras la alineacion

Estado: corregido y validado.

#### 3.9. Documentacion publica alineada

Se actualizo la documentacion visible del proyecto para que deje de describir fases y cifras obsoletas.

Cambio aplicado:

- `README.md` ahora refleja Java 21, las caracteristicas realmente implementadas y el estado actual de la suite
- `docs/INICIO_RAPIDO.md` documenta la ejecucion real con PostgreSQL por defecto y H2 para tests
- `CREDITOS.md` deja evidencia de participacion basada en commits representativos

Resultado:

- desaparecen referencias antiguas a Java 17, fases por iniciar y recuentos de tests obsoletos
- la documentacion publica vuelve a ser defendible frente al estado real del repositorio

Estado: corregido y validado.

#### 3.10. Integracion Spring + JavaFX estabilizada

Se elimino la dependencia temporal fragil del arranque conjunto.

Cambio aplicado:

- `Application.main(...)` deja de arrancar Spring en un hilo con `Thread.sleep(1000)`
- `VentanaPrincipal` inicializa el contexto Spring en `init()` y lo cierra en `stop()`

Resultado:

- el arranque deja de depender de una espera fija
- la integracion entre ciclo de vida JavaFX y contexto Spring queda mas limpia y compilable

Estado: corregido y validado.

#### 3.11. JavaFX minima conectada a casos de uso reales

Se redujo la parte mas artificial de la ventana principal sin intentar cerrar aun toda la experiencia de escritorio.

Cambio aplicado:

- `VentanaPrincipal` deja de mostrar listados fijos por defecto
- se puede cargar el contexto de un usuario por email y recuperar tableros reales
- el dialogo de creacion usa ya `ServicioTablero` para persistir tableros reales
- la UI permite abrir un detalle real del tablero, listar sus listas y anadir nuevas listas
- desde el detalle se puede navegar al panel real de tarjetas por lista
- `PanelDetallesTablero` permite ya crear tarjetas desde la propia vista de lista

Resultado:

- la UI JavaFX deja de ser una demo puramente estatica
- queda un cliente minimo funcional para alta y consulta de tableros, listas y tarjetas

Estado: corregido en el alcance minimo actual y validado por compilacion.

Estado: corregido parcialmente y validado por compilacion.

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

Estado: corregido y validado en las rutas actualmente cubiertas.

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

Estado: corregido y validado.

#### 4.5. Persistencia incompleta de estados de tarjeta

En dominio existen `archivada` y `fechaArchivado`, pero la entidad JPA no modela esos campos.

Consecuencia:

- la compactacion puede funcionar en tests unitarios
- pero no queda correctamente persistida en una ejecucion real con JPA

Estado: corregido y validado.

#### 4.6. Persistencia incompleta de reglas de listas previas

En dominio `Lista` soporta multiples `listasPrevias`, pero JPA usa un solo campo `listaPrerrequisitoId`.

Consecuencia:

- el modelo persistente no representa bien la capacidad del dominio
- las reglas de flujo pueden perderse o simplificarse indebidamente al persistir

Estado: corregido y validado.

#### 4.7. UI JavaFX principal no esta cerrada funcionalmente

La ventana principal contiene:

- integracion minima con Spring para cargar y crear tableros por email
- flujos reales para abrir detalle de tablero, listar listas, anadir listas y navegar al detalle de tarjetas por lista
- y la experiencia general sigue lejos de una interfaz de producto completa

Consecuencia:

- la existencia de JavaFX es real
- pero la UI principal aun no parece una implementacion funcional completa del producto

Estado: corregido parcialmente; queda deuda de cierre de producto.

#### 4.8. Arranque Spring + JavaFX fragil

La aplicacion arrancaba Spring en hilo separado y luego esperaba con `Thread.sleep(1000)` antes de lanzar JavaFX.

Consecuencia:

- dependencia temporal fragil
- riesgo de race condition

Estado: corregido y validado.

#### 4.9. Seguridad/autenticacion del enunciado no implementada

No se ha encontrado autenticacion por codigo de correo, OTP, token temporal ni servicio de email.

La identidad del usuario se simula pasando el email por query param.

Estado: confirmado.

#### 4.10. Modificacion de tablero solo parcial

El DTO de actualizacion acepta `titulo` y `descripcion`, pero el servicio solo actualiza la descripcion.

Estado: corregido y validado.

#### 4.11. Documentacion desactualizada o inconsistente

Se han detectado inconsistencias entre codigo y documentacion en:

- tipo de aplicacion (web frente a desktop + API)
- version de Java
- numero de tests
- fases ya completadas
- JavaFX marcada como opcional cuando el enunciado la exige

Estado: corregido y validado en README, inicio rapido y creditos.

---

## Estado por bloques para continuar

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

Estado: corregido y validado en las rutas actualmente cubiertas.

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

Estado: corregido y validado.

#### 5. Persistir correctamente `archivada` y `fechaArchivado`

Accion esperada:

- ampliar `TarjetaJPA`
- actualizar migraciones
- actualizar convertidores

Validacion necesaria:

- test de compactacion con persistencia real
- round-trip JPA de tarjeta archivada

Estado: corregido y validado.

#### 6. Corregir estrategia de seleccion de tableros para compactacion automatica

Accion esperada:

- crear un metodo de repositorio para recuperar todos los tableros o una estrategia equivalente correcta
- eliminar el uso de `obtenerCompartidos("")`

Validacion necesaria:

- test del scheduler o del metodo de ejecucion automatica
- comprobar que procesa tableros reales guardados

Estado: corregido y validado.

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

Estado: corregido y validado.

Resultado actual:

- la suite se ha adaptado a H2 para no depender de Docker en este entorno
- `mvn clean test` ya no deja pruebas omitidas

### Bloque 5. Cierre academico y entrega

#### 12. Actualizar README

Objetivo:

- reflejar el estado real del proyecto, las tecnologias usadas, el estado de pruebas y las limitaciones actuales

Estado: corregido y validado.

#### 13. Actualizar `docs/INICIO_RAPIDO.md`

Objetivo:

- dejar instrucciones coherentes con Java 21, PostgreSQL por defecto y H2 para tests

Estado: corregido y validado.

#### 14. Actualizar `CREDITOS.md`

Objetivo:

- reflejar la participacion real y apoyarla en commits representativos

Estado: corregido y validado.

#### 15. Preparar memoria de defensa del proyecto

Objetivo:

- dejar una narrativa de entrega con requisitos, opcionales, evidencias y limitaciones

Estado: corregido y validado.

---

## Priorizacion final recomendada

### Cierre ya completado

1. Cache/autorizacion.
2. Persistencia JPA critica y round-trip principal del agregado.
3. Semantica del bloqueo.
4. Persistencia de prerequisitos multiples.
5. Persistencia de archivado de tarjetas.
6. Compactacion automatica real.
7. Tests de repositorio.
8. JavaFX minima funcional.
9. Documentacion publica.

### Siguiente paso con mejor retorno

10. repaso final contra el enunciado y preparacion de commit de entrega.

### Desarrollo adicional solo si compensa

11. autenticacion basada en codigo por correo.
12. ampliaciones extra de JavaFX o permisos finos.

---

## Riesgos residuales

### Riesgo 1. Sobreprometer el alcance de JavaFX

Mitigacion:

- defender JavaFX como interfaz minima funcional, no como cliente completo de producto.

### Riesgo 2. Confundir opcionales implementadas con opcionales pendientes

Mitigacion:

- basar la defensa en evidencia observable en codigo, pruebas y documentacion actualizada.

### Riesgo 3. Abrir ahora una feature grande y desestabilizar la entrega

Mitigacion:

- priorizar cierre academico y commit antes de tocar autenticacion por correo.

---

## Punto exacto de reanudacion

Si se pierde el contexto, continuar desde aqui:

### Siguiente objetivo recomendado

Usar `docs/MEMORIA_DEFENSA.md` como base del repaso final de entrega y del commit de cierre.

### Motivo

El proyecto ya esta en estado defendible. Lo que mas retorno da ahora es consolidar la narrativa academica y evitar contradicciones entre codigo, documentacion y demostracion.

### Despues de eso

1. revisar el diff final,
2. ejecutar una validacion final si hace falta,
3. preparar commit,
4. solo si queda margen, decidir si compensa abrir autenticacion por correo.

---

## Checklist resumido de seguimiento

### Confirmado hecho

- auditoria del proyecto completada
- comparacion contra enunciado completada
- defectos criticos de cache, bloqueo y persistencia corregidos
- integraciones relevantes activas y en verde
- JavaFX minima funcional cerrada en el alcance elegido
- documentacion publica alineada con el estado real
- build limpio con `mvn clean test`

### Pendiente inmediata

- repaso final de cierre y commit

### Pendiente solo si se quiere seguir ampliando

- autenticacion del enunciado
- ampliaciones de interfaz y permisos avanzados

---

## Criterio de cierre

Se podra considerar este plan cerrado cuando se cumpla lo siguiente:

1. No haya defectos funcionales criticos abiertos en cache, bloqueo y persistencia.
2. Los tests relevantes sigan activos y en verde.
3. La documentacion publica y la memoria de defensa reflejen el estado real del proyecto.
4. La defensa del proyecto frente al enunciado sea consistente y sin contradicciones obvias.
