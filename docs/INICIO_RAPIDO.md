# Guía de Inicio Rápido

## Requisitos Previos

- **JDK 17** o superior: [Descargar](https://jdk.java.net/17/)
- **Maven 3.8.1** o superior: [Descargar](https://maven.apache.org/download.cgi)
- **Git** 
- **IDE recomendado**: IntelliJ IDEA o VS Code con extensiones Java

## Instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/adrimmz04/App-gestion-trabajo-colaborativo.git
cd App-gestion-trabajo-colaborativo
```

### 2. Compilar el proyecto

```bash
mvn clean install
```

Esto descargará todas las dependencias necesarias y compilará el proyecto.

### 3. Ejecutar las pruebas

```bash
mvn test
```

Deberían pasar 47 pruebas unitarias del modelo de dominio.

## Estructura del Proyecto

```
app-gestion/
├── src/
│   ├── main/
│   │   ├── java/pds/app_gestion/
│   │   │   ├── domain/          # Modelo de dominio (lógica de negocio)
│   │   │   ├── application/     # Casos de uso (próximamente)
│   │   │   ├── infrastructure/  # Persistencia JPA (próximamente)
│   │   │   └── ui/              # Controladores REST (próximamente)
│   │   └── resources/
│   └── test/
│       ├── java/pds/app_gestion/domain/  # Pruebas del dominio
│       └── resources/
├── docs/
│   └── DISEÑO_DOMINIO.md        # Documentación del diseño
├── pom.xml                      # Configuración de Maven
└── README.md                    # Este archivo
```

## Comandos Útiles

```bash
# Compilar sin ejecutar pruebas
mvn clean compile

# Ejecutar pruebas con salida detallada
mvn test -X

# Limpiar archivos generados
mvn clean

# Ver dependencias del proyecto
mvn dependency:tree

# Empaquetar en JAR
mvn package

# Ejecutar la aplicación Spring Boot (cuando esté lista)
mvn spring-boot:run
```

## Próximos Pasos (Roadmap)

### Fase 1: Modelo de Dominio ✅
- ✅ Entidades: Tablero, Lista, Tarjeta
- ✅ Value Objects: Etiqueta, Posición, RegistroAccion
- ✅ Puertos de dominio: RepositorioTablero
- ✅ 47 pruebas unitarias pasando

### Fase 2: Capa de Aplicación (En progreso)
- [ ] Servicios de aplicación (Casos de uso)
- [ ] Adaptadores de entrada (REST controllers)
- [ ] Configuración de Spring

### Fase 3: Capa de Infraestructura
- [ ] Entidades JPA
- [ ] Repositorios con JPA
- [ ] Base de datos (H2 para desarrollo)

### Fase 4: Interfaz de Usuario
- [ ] Controllers REST
- [ ] Interfaz JavaFX (opcional)

### Fase 5: Características Opcionales
- [ ] Reglas de listas
- [ ] Filtrado por etiquetas
- [ ] Bloqueo temporal (Ya en dominio)
- [ ] Automatización de tarjetas

## Configuración del IDE

### IntelliJ IDEA

1. Abrir proyecto → Seleccionar carpeta raíz
2. Aceptar como proyecto Maven
3. Maven debería detectar automáticamente el `pom.xml`
4. Las pruebas pueden ejecutarse directamente desde el IDE

### VS Code

1. Instalar extensiones:
   - Extension Pack for Java (Microsoft)
   - Test Explorer UI (Heben Shi)

2. Abrir la carpeta del proyecto

3. Las pruebas aparecerán en el explorador de pruebas

## Estándares de Código

### Nomenclatura
- **Clases**: PascalCase (ej: `Tablero`, `ListaTest`)
- **Métodos/variables**: camelCase (ej: `crearTablero()`, `titulo`)
- **Constantes**: UPPER_SNAKE_CASE (ej: `MAX_ITEMS`)

### Documentación
- Todas las clases públicas deben tener Javadoc
- Los métodos públicos deben documentar parámetros y retorno
- Las decisiones de diseño se documentan en los comentarios de clase

### Pruebas
- Una clase de test por cada clase de dominio
- Nombres descriptivos: `testXXXXThrowsException()`, `testXXXXReturnsXX()`
- Mínimo de 1 prueba por método público

### Git
- Los commits siempre en español con mensaje descriptivo
- Formato: `Acción: descripción en gerundio`
- Incluir las razones del cambio en la descripción

## Contacto y Soporte

- **Desarrollador**: Adrian Martinez Zamora
- **Email**: adrimmz04@gmail.com
- **Repositorio**: https://github.com/adrimmz04/App-gestion-trabajo-colaborativo

## Licencia

Este proyecto es parte de la asignatura de Prácticas de Desarrollo de Software (PDS).
