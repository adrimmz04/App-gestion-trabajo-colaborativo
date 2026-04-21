# 🧪 CHECKLIST DE TESTING FUNCIONAL - App Gestión de Trabajo Colaborativo

**Objetivo**: Verificar que TODAS las funcionalidades implementadas funcionan correctamente.  
**Fecha inicio**: [Llenar durante testing]  
**Fecha fin**: [Llenar durante testing]

---

## 📋 CÓMO USAR ESTE CHECKLIST

1. **Secciones**: Cada sección agrupa funcionalidades relacionadas
2. **Casos de uso**: Pasos específicos para probar cada feature
3. **Marcas**: ✅ = Funciona | ❌ = Falla | ⚠️ = Parcial | 🔄 = Revisar
4. **Notas**: Registra cualquier comportamiento anómalo

---

## ⚙️ PRE-REQUISITOS

- [ ] Proyecto compilado: `mvn clean package` ✅
- [ ] Base de datos iniciada (PostgreSQL o H2)
- [ ] Aplicación ejecutada (puerto 8080)
- [ ] Acceso a API: http://localhost:8080/api/v1/
- [ ] Postman/curl disponible para API testing
- [ ] UI JavaFX ejecutada (si aplica)

---

# 🎯 GRUPOS DE TESTING

## 1️⃣ DOMAIN MODEL - TABLERO

### 1.1 Crear Tablero
- [ ] Crear tablero con datos válidos → Genera UUID único
- [ ] Crear tablero sin título → **Error** (null)
- [ ] Crear tablero con descrip vacía → OK (permite vacía)
- [ ] Verificar fechaCreacion se asigna automáticamente
- [ ] Verificar listas inicialmente vacía
- [ ] Verificar estado desbloqueado por defecto

**Notas**: _______________________________________________

### 1.2 Agregar/Eliminar Listas
- [ ] Agregar lista válida → Se añade al tablero
- [ ] Obtener lista por ID → Retorna Optional con lista
- [ ] Obtener lista inexistente → Retorna Optional.empty()
- [ ] Eliminar lista existente → Se elimina
- [ ] Eliminar lista inexistente → No lanza error
- [ ] Tablero con 3 listas → obtenerListas() retorna 3

**Notas**: _______________________________________________

### 1.3 Compartir Acceso
- [ ] Compartir con usuario válido → Se añade a usuariosCompartidos
- [ ] Compartir con mismo propietario → ¿Permitido o rechaza?
- [ ] Compartir con usuario duplicado → No añade duplicado
- [ ] Verificar acceso con tieneAcceso() → true para compartidos
- [ ] Revocar acceso → Se elimina de compartidos
- [ ] tieneAcceso() para usuario sin acceso → false

**Notas**: _______________________________________________

### 1.4 Bloqueo/Desbloqueo
- [ ] Bloquear tablero 5 minutos → bloqueado=true, fechaDesbloqueo asignada
- [ ] Desbloquear tablero → bloqueado=false, fechaDesbloqueo=null
- [ ] Bloquear con 0 minutos → ¿Error o minuto?
- [ ] Bloquear con número negativo → **Error** (validar)
- [ ] Verificar desbloqueo automático (pasar 5 min) → ¿Se desbloquea?
- [ ] Desbloquear tablero no bloqueado → Funciona (idempotente)

**Notas**: _______________________________________________

### 1.5 Historial de Acciones
- [ ] Crear tablero → registra "TABLERO_CREADO"
- [ ] Agregar lista → registra "LISTA_AÑADIDA"
- [ ] Modificar tablero → registra acción correspondiente
- [ ] obtenerHistorial() retorna lista de RegistroAccion
- [ ] Cada registro tiene: tipo, detalles, fecha

**Notas**: _______________________________________________

### 1.6 Estadísticas
- [ ] obtenerTotalTarjetas() con 0 tarjetas → 0
- [ ] obtenerTotalTarjetas() con 5 tarjetas → 5
- [ ] obtenerTarjetasCompletadas() solo completadas → Retorna subset

**Notas**: _______________________________________________

---

## 2️⃣ DOMAIN MODEL - LISTA

### 2.1 Crear Lista
- [ ] Crear lista con datos válidos → ID único, nombre guardado
- [ ] Lista sin nombre → **Error** (null)
- [ ] Crear lista sin límite máximo → limiteMaximo = Optional.empty()
- [ ] Crear lista con límite 5 → limiteMaximo = Optional.of(5)
- [ ] Crear lista con límite 0 → **Error** al establecer
- [ ] Crear lista con límite negativo → **Error**

**Notas**: _______________________________________________

### 2.2 Gestionar Tarjetas
- [ ] Agregar tarjeta válida → Se añade a lista
- [ ] Agregar tarjeta null → **Error**
- [ ] obtenerCantidadTarjetas() → Count correcto
- [ ] obtenerTarjeta(id) existente → Optional con tarjeta
- [ ] obtenerTarjeta(id) inexistente → Optional.empty()
- [ ] Eliminar tarjeta → Se elimina, count disminuye

**Notas**: _______________________________________________

### 2.3 Límite Máximo de Tarjetas
- [ ] Agregar tarjeta cuando count < límite → OK
- [ ] Agregar tarjeta cuando count == límite → **Error** (IllegalStateException)
- [ ] Agregar tarjeta cuando count > límite → Rechaza
- [ ] obtenerCantidadTarjetas() == límite → Está en límite

**Notas**: _______________________________________________

### 2.4 Listas Previas (Requisitos)
- [ ] Agregar lista previa válida → Se añade
- [ ] Agregar lista previa duplicada → No añade dos veces
- [ ] Agregar lista previa null → **Error**
- [ ] Agregar lista previa vacía → **Error**
- [ ] obtenerListasPrevias() → Retorna lista inmutable
- [ ] tienePrerequisitos() con 0 previas → false
- [ ] tienePrerequisitos() con 1+ previas → true

**Notas**: _______________________________________________

### 2.5 Requisitos de Tarjeta
- [ ] cumpleRequisitos(tarjeta) sin previas → true
- [ ] cumpleRequisitos(tarjeta completada) con previas → true
- [ ] cumpleRequisitos(tarjeta NO completada) con previas → false
- [ ] Eliminar lista previa → obtenerListasPrevias() no la incluye

**Notas**: _______________________________________________

### 2.6 Filtrado de Tarjetas
- [ ] obtenerTarjetasCompletadas() → Solo completadas
- [ ] obtenerTarjetasNoCompletadas() → Solo no completadas
- [ ] Total = completadas + no completadas

**Notas**: _______________________________________________

---

## 3️⃣ DOMAIN MODEL - TARJETA

### 3.1 Crear Tarjeta
- [ ] Crear tarjeta tipo TAREA → tipo = TAREA
- [ ] Crear tarjeta tipo CHECKLIST → tipo = CHECKLIST
- [ ] Crear tarjeta sin descripción → descripcion = ""
- [ ] Crear tarjeta con descripción → Se guarda correctamente
- [ ] completada = false por defecto
- [ ] archivada = false por defecto

**Notas**: _______________________________________________

### 3.2 Completar/Descompletar
- [ ] marcarComoCompletada() → completada=true, fechaCompletacion asignada
- [ ] marcarComoCompletada() dos veces → Idempotente (no duplica fecha)
- [ ] marcarComoNoCompletada() → completada=false, fechaCompletacion=null
- [ ] marcarComoNoCompletada() sin estar completada → Idempotente
- [ ] isCompletada() → Retorna estado correcto

**Notas**: _______________________________________________

### 3.3 Etiquetas
- [ ] agregarEtiqueta(etiqueta válida) → Se añade
- [ ] agregarEtiqueta(etiqueta null) → No añade (ignora)
- [ ] agregarEtiqueta(etiqueta duplicada) → Set no duplica
- [ ] obtenerEtiquetas() → Retorna set actual
- [ ] eliminarEtiqueta(etiqueta existente) → Se elimina
- [ ] eliminarEtiqueta(etiqueta inexistente) → No lanza error
- [ ] Tarjeta con 3 etiquetas → Set.size() == 3

**Notas**: _______________________________________________

### 3.4 Descripción
- [ ] actualizarDescripcion(texto válido) → Se actualiza
- [ ] actualizarDescripcion(null) → descripcion = ""
- [ ] actualizarDescripcion("") → descripcion = ""
- [ ] actualizarDescripcion(texto muy largo) → Se acepta ¿límite?

**Notas**: _______________________________________________

### 3.5 Archivado
- [ ] archivar() → archivada=true, fechaArchivado asignada
- [ ] archivar() dos veces → Idempotente
- [ ] desarchivar() → archivada=false, fechaArchivado=null
- [ ] estaArchivada() → Retorna estado correcto
- [ ] Tarjeta archivada todavía tiene etiquetas → ¿Se conservan?

**Notas**: _______________________________________________

### 3.6 Timestamps
- [ ] fechaCreacion se asigna en constructor
- [ ] fechaActualizacion se actualiza con cada cambio
- [ ] fechaCompletacion solo se asigna cuando se completa
- [ ] Timestamps son LocalDateTime → Comparables, ordenables

**Notas**: _______________________________________________

---

## 4️⃣ DOMAIN MODEL - ETIQUETA Y OTROS

### 4.1 Etiqueta
- [ ] Crear etiqueta con nombre válido → Se crea
- [ ] Crear etiqueta con nombre vacío → **Error**
- [ ] Crear etiqueta con nombre null → **Error**
- [ ] Crear etiqueta con color válido (#FF0000) → Se acepta
- [ ] Crear etiqueta con color inválido (#GG0000) → **Error** (validar hex)
- [ ] Crear etiqueta con color sin # → ¿Acepta o rechaza?
- [ ] Etiquetas iguales son equals() → true
- [ ] Etiquetas diferentes son equals() → false

**Notas**: _______________________________________________

### 4.2 RegistroAccion
- [ ] Crear registro con tipo y detalles → Se guarda
- [ ] obtenerDescripcion() → "[TIPO] detalles - fecha"
- [ ] Diferentes tipos registrados (TABLERO_CREADO, LISTA_AÑADIDA, etc.)

**Notas**: _______________________________________________

---

## 5️⃣ APPLICATION SERVICES - ServicioTablero

### 5.1 Crear Tablero
- [ ] POST /api/v1/tableros con datos válidos → 201 Created, UUID generado
- [ ] POST sin título → 400 Bad Request
- [ ] POST con email propietario válido → Se guarda
- [ ] Nuevo tablero está desbloqueado
- [ ] Caché se invalida al crear

**Notas**: _______________________________________________

### 5.2 Obtener Tablero
- [ ] GET /api/v1/tableros/{id} con acceso → 200 OK + datos
- [ ] GET sin acceso (email diferente) → 403 Forbidden ¿O 404?
- [ ] GET con ID inexistente → 404 Not Found
- [ ] Resultado está cacheado (verificar con metrics)

**Notas**: _______________________________________________

### 5.3 Actualizar Tablero
- [ ] PUT /api/v1/tableros/{id} con nueva descripción → 200 OK
- [ ] PUT sin acceso → 403 Forbidden
- [ ] PUT con ID inexistente → 404 Not Found
- [ ] Caché se invalida

**Notas**: _______________________________________________

### 5.4 Listar Tableros Propietario
- [ ] GET /api/v1/tableros/propietario/{email} → 200 OK + lista
- [ ] Usuario con 3 tableros → Retorna 3
- [ ] Usuario sin tableros → Retorna lista vacía []
- [ ] Resultado está cacheado

**Notas**: _______________________________________________

### 5.5 Listar Tableros Compartidos
- [ ] GET /api/v1/tableros/compartidos/{email} → 200 OK
- [ ] Usuario con 2 tableros compartidos → Retorna 2
- [ ] Usuario sin compartidos → Retorna []
- [ ] Resultado está cacheado

**Notas**: _______________________________________________

### 5.6 Compartir Tablero
- [ ] POST /api/v1/tableros/{id}/compartir + {emailUsuario} → 204 No Content
- [ ] Compartir con email duplicado → ¿Error o idempotente?
- [ ] Compartir sin permiso → 403 Forbidden
- [ ] Caché se invalida
- [ ] Usuario compartido puede verlo en GET compartidos

**Notas**: _______________________________________________

### 5.7 Bloquear Tablero
- [ ] POST /api/v1/tableros/{id}/bloquear + {5} → 204, bloqueado 5 min
- [ ] Bloquear sin permiso → 403 Forbidden
- [ ] POST listas mientras está bloqueado → ¿Error o permite?

**Notas**: _______________________________________________

### 5.8 Desbloquear Tablero
- [ ] POST /api/v1/tableros/{id}/desbloquear → 204
- [ ] Desbloquear sin permiso → 403 Forbidden
- [ ] Desbloquear ya desbloqueado → 204 (idempotente)

**Notas**: _______________________________________________

### 5.9 Agregar Lista
- [ ] POST /api/v1/tableros/{id}/listas + {nombre} → 201 Created
- [ ] Agregarlista a tablero bloqueado → ¿Error o permite?
- [ ] Agregar lista sin acceso → 403 Forbidden
- [ ] Caché se invalida

**Notas**: _______________________________________________

---

## 6️⃣ APPLICATION SERVICES - ServicioLista

### 6.1 Configurar Reglas
- [ ] POST /api/v1/tableros/{id}/listas/{idL}/reglas + {limiteMaximo: 5} → 204
- [ ] Limitar lista a 5 → límite se aplica
- [ ] Intentar agregar 6ª tarjeta → Error
- [ ] POST + {listasPrevias: ["id1", "id2"]} → Requisitos se guardan

**Notas**: _______________________________________________

### 6.2 Obtener Reglas
- [ ] GET /api/v1/tableros/{id}/listas/{idL}/reglas → 200 OK + {límite, previas}
- [ ] Lista sin reglas → {limiteMaximo: null, listasPrevias: []}

**Notas**: _______________________________________________

### 6.3 Mover Tarjeta (Transferir entre Listas)
- [ ] POST /api/v1/tableros/{id}/listas/{origen}/tarjetas/{idT}/mover?destino={idDest} → 204
- [ ] Mover de lista origen a destino → Tarjeta desaparece de origen, aparece en destino
- [ ] Mover tarjeta incompleta a lista con requisitos → **Error** (debe estar completada)
- [ ] Mover cuando lista destino está llena → **Error** (límite máximo)
- [ ] Mover en tablero bloqueado → **Error**

**Notas**: _______________________________________________

---

## 7️⃣ APPLICATION SERVICES - ServicioTarjeta

### 7.1 Crear Tarjeta
- [ ] POST .../tarjetas + {titulo, tipo: TAREA} → 201 Created, tipo=TAREA
- [ ] POST con tipo: CHECKLIST → 201 Created, tipo=CHECKLIST
- [ ] POST sin título → 400 Bad Request
- [ ] POST a lista llena → 409 Conflict (límite máximo)

**Notas**: _______________________________________________

### 7.2 Actualizar Tarjeta
- [ ] PUT .../tarjetas/{id} + {descripcion} → 200 OK
- [ ] PUT con descripción larga → ¿Límite de caracteres?
- [ ] PUT sin acceso → 403 Forbidden

**Notas**: _______________________________________________

### 7.3 Marcar Completada
- [ ] POST .../tarjetas/{id}/completar → 200 OK, completada=true
- [ ] Verificar fechaCompletacion se registra
- [ ] POST dos veces → Idempotente (sin error)

**Notas**: _______________________________________________

### 7.4 Marcar No Completada
- [ ] POST .../tarjetas/{id}/descompletar → 200 OK, completada=false
- [ ] Verificar fechaCompletacion se limpia
- [ ] POST sin estar completada → Idempotente

**Notas**: _______________________________________________

### 7.5 Agregar Etiqueta
- [ ] POST .../tarjetas/{id}/etiquetas + {nombre, color} → 201 Created
- [ ] Agregar etiqueta duplicada → ¿Error o ignora?
- [ ] Agregar múltiples etiquetas → Tarjeta tiene todas

**Notas**: _______________________________________________

### 7.6 Filtrar por Etiquetas
- [ ] GET .../tarjetas?etiquetas=bug,urgente → 200 OK + tarjetas con AMBAS
- [ ] GET con 0 etiquetas → Retorna todas
- [ ] GET con etiqueta inexistente → Retorna []

**Notas**: _______________________________________________

### 7.7 Obtener Todas las Tarjetas
- [ ] GET .../tarjetas → 200 OK + todas las tarjetas de lista
- [ ] GET lista vacía → Retorna []

**Notas**: _______________________________________________

### 7.8 Obtener Etiquetas Únicas
- [ ] GET .../tarjetas/etiquetas → 200 OK + lista de etiquetas usadas
- [ ] Lista con 3 etiquetas diferentes → Retorna 3

**Notas**: _______________________________________________

---

## 8️⃣ APPLICATION SERVICES - ServicioPlantillas

### 8.1 Exportar a YAML
- [ ] Tablero → YAML válido (parseable)
- [ ] YAML contiene: título, descripción, listas, tarjetas, etiquetas
- [ ] Tarjeta completada → completada=true en YAML
- [ ] Tarjeta con etiquetas → Se incluyen nombre y color

**Notas**: _______________________________________________

### 8.2 Importar YAML
- [ ] YAML válido → Se deserializa sin error
- [ ] YAML con estructura incorrecta → **Error** (exception)
- [ ] PlantillaTableroYAML contiene datos correctos

**Notas**: _______________________________________________

### 8.3 Crear Tablero desde Plantilla
- [ ] Plantilla Kanban → Se crea tablero con 3 listas
- [ ] Lista "En Progreso" tiene limiteMaximo=5
- [ ] Tarjetas de plantilla se copian correctamente
- [ ] Etiquetas de tarjetas se preservan

**Notas**: _______________________________________________

### 8.4 Plantillas Ejemplo
- [ ] obtenerPlantillasEjemplo() → Retorna al menos 1 plantilla
- [ ] Plantilla "Kanban Básico" disponible

**Notas**: _______________________________________________

---

## 9️⃣ APPLICATION SERVICES - ServicioCompactacion

### 9.1 Auto-archivado
- [ ] Tarjeta completada hace 7 días → Se archiva automáticamente (próxima ejecución 2 AM)
- [ ] Tarjeta completada hace 6 días → No se archiva
- [ ] Tarjeta completada hace 10 días → Se archiva
- [ ] Verificar archivada=true y fechaArchivado asignada

**Notas**: _______________________________________________

### 9.2 Auto-eliminación
- [ ] Tarjeta archivada hace 30 días → Se elimina (próxima ejecución)
- [ ] Tarjeta archivada hace 29 días → No se elimina
- [ ] Tarjeta archivada hace 35 días → Se elimina

**Notas**: _______________________________________________

### 9.3 Estadísticas de Compactación
- [ ] obtenerEstadisticasCompactacion(tablero) → Retorna map con stats
- [ ] Incluye: totalTarjetas, completadas, archivadas, paraArchivar, paraEliminar
- [ ] Números correctos para estado actual del tablero

**Notas**: _______________________________________________

### 9.4 Obtener Tarjetas para Archivar
- [ ] obtenerTarjetasParaArchivar() → Retorna solo completadas hace 7+ días
- [ ] Cuenta correcta

**Notas**: _______________________________________________

### 9.5 Obtener Tarjetas para Eliminar
- [ ] obtenerTarjetasParaEliminar() → Retorna solo archivadas hace 30+ días
- [ ] Cuenta correcta

**Notas**: _______________________________________________

### 9.6 Configuración
- [ ] application.properties: compactacion.dias-archivo=7 ✓
- [ ] application.properties: compactacion.dias-eliminacion=30 ✓
- [ ] Cambiar a 3 días archivo → Nuevo valor se respeta

**Notas**: _______________________________________________

---

## 🔟 REST API - SEGURIDAD Y PERMISOS

### 10.1 Permiso Básico de Acceso
- [ ] Usuario A intenta acceder tablero de Usuario B → 403 Forbidden
- [ ] Usuario A accede su propio tablero → 200 OK
- [ ] Usuario con tablero compartido accede → 200 OK

**Notas**: _______________________________________________

### 10.2 Operaciones Restrictivas
- [ ] Usuario no propietario intenta bloquear → 403 Forbidden
- [ ] Propietario intenta bloquear → 204 OK
- [ ] Usuario no propietario intenta compartir → 403 Forbidden

**Notas**: _______________________________________________

### 10.3 Validación de Email
- [ ] Compartir con email "invalid" (sin @) → ¿Error o permite?
- [ ] Compartir con email valido@test.com → 204 OK

**Notas**: _______________________________________________

---

## 1️⃣1️⃣ EXCEPCIONES Y ERRORES

### 11.1 Excepciones Personalizadas
- [ ] RecursoNoEncontradoException → HTTP 404
- [ ] PermisoNegadoException → HTTP 403
- [ ] ErrorValidacionException → HTTP 400
- [ ] ErrorOperacionDominioException → HTTP 409

**Notas**: _______________________________________________

### 11.2 Casos Límite
- [ ] ID vacío "" → Error o acepta?
- [ ] Email vacío "" → Error o acepta?
- [ ] Descripción muy larga (10000 caracteres) → Acepta o límite?
- [ ] Nombre de lista vacío → Error

**Notas**: _______________________________________________

### 11.3 Concurrencia (Opcional)
- [ ] Dos usuarios modifican tablero simultáneamente → ¿Sincronizado?
- [ ] Una operación no interfiere con otra

**Notas**: _______________________________________________

---

## 1️⃣2️⃣ CACHÉ

### 12.1 Verificar Caché Funciona
- [ ] GET tablero primera vez → desde DB
- [ ] GET tablero segunda vez → desde caché (más rápido)
- [ ] POST actualizar tablero → caché se invalida
- [ ] GET tablero tercera vez → DB nuevamente

**Notas**: _______________________________________________

### 12.2 Caché de Diferentes Tableros
- [ ] GET tablero A → cacheado como A
- [ ] GET tablero B → cacheado como B (diferente)
- [ ] No interfieren entre sí

**Notas**: _______________________________________________

---

## 1️⃣3️⃣ UI - JAVAFX (si se ejecuta)

### 13.1 Ventana Principal
- [ ] Abre sin errores
- [ ] Muestra menú: Archivo, Tablero, Ayuda
- [ ] Interfaz responsiva

**Notas**: _______________________________________________

### 13.2 Crear Tablero desde UI
- [ ] Botón "Nuevo Tablero" → Abre diálogo
- [ ] Completa datos → Se crea en BD

**Notas**: _______________________________________________

### 13.3 Exportar Plantilla
- [ ] Menú → Exportar → Diálogo FileChooser
- [ ] Selecciona ubicación → Genera archivo .yaml
- [ ] Archivo contiene YAML válido

**Notas**: _______________________________________________

### 13.4 Importar Plantilla
- [ ] Menú → Importar → Diálogo FileChooser o seleccionar predefinida
- [ ] Selecciona "Kanban Básico" → Se crea tablero con 3 listas

**Notas**: _______________________________________________

### 13.5 Filtrado por Etiquetas
- [ ] Panel de filtro visible
- [ ] Click en etiqueta → Filtra tarjetas
- [ ] Múltiples etiquetas → AND (solo tarjetas con TODAS)

**Notas**: _______________________________________________

### 13.6 Crear/Editar Tarjetas en UI
- [ ] Agregar tarjeta → Se crea
- [ ] Marcar completada → Cambio visual
- [ ] Agregar etiqueta → Se ve en tarjeta

**Notas**: _______________________________________________

---

## 1️⃣4️⃣ FLUJOS DE NEGOCIO COMPLEJOS

### 14.1 Workflow Completo: Kanban
```
1. Crear tablero
2. Agregar 3 listas: "Por Hacer", "En Progreso" (limit 5), "Hecho"
3. Agregar 8 tarjetas a "Por Hacer"
4. Mover 3 a "En Progreso" ✓
5. Intentar mover 4ª a "En Progreso" → Error (limit 5)
6. Mover 2 a "Hecho"
7. Completar tarjetas en "Hecho"
8. Esperar 7 días → Se archivan
9. Esperar 30 días más → Se eliminan
```

- [ ] Paso 1-7 funcionan correctamente
- [ ] Tarjetas se mueven correctamente entre listas
- [ ] Límite máximo se respeta

**Notas**: _______________________________________________

### 14.2 Workflow: Tareas con Requisitos
```
1. Crear 4 listas: "Requisitos", "Tareas", "Revisión", "Completado"
2. Configurar "Revisión" con prerequisito: "Tareas"
3. Crear tarjeta en "Requisitos"
4. Mover a "Tareas" → Debe estar completada para mover a "Revisión"
5. Intentar mover a "Revisión" sin completar → Error
6. Completar tarjeta en "Tareas"
7. Mover a "Revisión" → OK
```

- [ ] Paso 1-4 funcionan
- [ ] Paso 5 rechaza correctamente
- [ ] Paso 6-7 permiten mover

**Notas**: _______________________________________________

### 14.3 Workflow: Compartir y Permisos
```
1. Usuario A crea tablero "Proyecto X"
2. Usuario A comparte con Usuario B
3. Usuario B obtiene acceso (GET compartidos contiene "Proyecto X")
4. Usuario B agrega lista → OK
5. Usuario B bloquea tablero → Error (permisos insuficientes)
6. Usuario A bloquea → OK
7. Usuario B intenta agregar lista → Error (tablero bloqueado)
```

- [ ] Paso 1-4 funcionan
- [ ] Paso 5 rechaza correctamente
- [ ] Paso 7 rechaza

**Notas**: _______________________________________________

### 14.4 Workflow: Plantillas
```
1. Exportar tablero actual → Genera YAML
2. Importar YAML en nueva aplicación → Se recrea igual
3. O usar plantilla "Kanban Básico" → Crea predefinida
4. Verificar listas y límites se preservan
```

- [ ] Paso 1-2 funcionan
- [ ] YAML es idéntico al exportado
- [ ] Plantilla predefinida tiene estructura correcta

**Notas**: _______________________________________________

---

## 1️⃣5️⃣ RENDIMIENTO Y ESCALABILIDAD (Opcional)

### 15.1 Tablero Grande
- [ ] Tablero con 10 listas → Carga rápida
- [ ] Lista con 100 tarjetas → Renderiza sin lag

**Notas**: _______________________________________________

### 15.2 Muchos Usuarios
- [ ] 10 usuarios simultáneos acceden mismo tablero → ¿Race conditions?
- [ ] Cada uno modifica diferentes partes → Independientes

**Notas**: _______________________________________________

### 15.3 Compactación
- [ ] Tablero con 1000 tarjetas archivadas → Compactación rápida
- [ ] Elimina tarjetas sin lentitud notoria

**Notas**: _______________________________________________

---

## 1️⃣6️⃣ AUDITORÍA Y LOGS

### 16.1 Historial de Acciones
- [ ] Crear tablero → "TABLERO_CREADO" en historial
- [ ] Agregar lista → "LISTA_AÑADIDA"
- [ ] Modificar descripción → "DESCRIPCION_ACTUALIZADA"

**Notas**: _______________________________________________

### 16.2 Timestamps
- [ ] Cada acción registra fecha/hora
- [ ] Historial ordenado cronológicamente

**Notas**: _______________________________________________

---

## ✅ RESUMEN FINAL

### Totales de Casos de Prueba

| Sección | Casos | ✅ | ❌ | ⚠️ |
|---------|-------|----|----|-----|
| 1. Tablero | 27 | | | |
| 2. Lista | 22 | | | |
| 3. Tarjeta | 18 | | | |
| 4. Etiqueta/Otros | 11 | | | |
| 5. ServicioTablero | 23 | | | |
| 6. ServicioLista | 10 | | | |
| 7. ServicioTarjeta | 22 | | | |
| 8. ServicioPlantillas | 13 | | | |
| 9. ServicioCompactacion | 18 | | | |
| 10. Seguridad | 8 | | | |
| 11. Excepciones | 8 | | | |
| 12. Caché | 5 | | | |
| 13. UI JavaFX | 16 | | | |
| 14. Flujos Complejos | 4 | | | |
| 15. Rendimiento | 3 | | | |
| 16. Auditoría | 5 | | | |
| **TOTAL** | **232** | | | |

### Estadísticas

- **Total de casos**: 232
- **Completados**: ___ / 232 (__%})
- **Errores encontrados**: ___
- **Funcionalidades con problemas**: ___

### Problemas Encontrados

| ID | Descripción | Gravedad | Estado |
|----|-------------|----------|--------|
| P001 | [Describir problema] | 🔴/🟡/🟢 | Abierto/Cerrado |
| | | | |

### Conclusión

**Fecha de ejecución**: ____________  
**Duración**: ____________  
**Estado General**: ✅ LISTO / ⚠️ REVISAR / ❌ PROBLEMAS  

**Notas finales**:
_____________________________________________________________________________

---

**Generado**: 2026-04-21  
**Versión del Proyecto**: Phase 5.4 (Auto-compaction)
