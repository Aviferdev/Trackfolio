# Trackfolio

Aplicación de gestión financiera personal construida con **Kotlin Multiplatform** y **Compose Multiplatform**,
compartiendo UI y lógica de negocio entre Android e iOS.

## Stack tecnológico

| Propósito | Tecnología |
|-----------|-----------|
| Lenguaje | Kotlin 2.1.0 |
| UI | Compose Multiplatform 1.7.3 |
| DI | Koin 4.0.0 |
| Persistencia | SQLDelight 2.0.2 |
| Async | Coroutines 1.9.0 |
| Navegación | Navigation Compose 2.8.0 |
| Min SDK / Compile SDK | 24 / 34 |

Ver [ADR-0001](./docs/adr/0001-decisiones-tecnologicas-iniciales.md) para la justificación
completa del stack.

## Arquitectura

Clean Architecture + MVVM:

```
core/      → Infraestructura transversal (seguridad, cifrado, preferencias)
domain/    → Modelos, interfaces de repositorio, casos de uso, calculadoras
data/      → SQLDelight, datasources, implementaciones de repositorios
ui/        → Componibles, ViewModels, navegación, tema
```

Ver [AGENTS.md](./AGENTS.md) para la estructura detallada de paquetes y convenciones.

## Documentación de decisiones (ADR)

Las decisiones importantes de arquitectura y diseño se documentan como
**Architecture Decision Records** en [docs/adr/](./docs/adr/).

Cada ADR explica el contexto, las opciones consideradas y la justificación
de la decisión, para que cualquier desarrollador futuro entienda el «por qué»
detrás del código.

📖 [Índice de ADRs](./docs/adr/README.md)

## Ficheros sensibles (excluidos del repositorio)

Por seguridad, los siguientes ficheros con credenciales y claves están excluidos
del control de versiones mediante `.gitignore`. A continuación se indica cómo
generarlos u obtenerlos.

### Firebase (Android)

Se necesitan dos ficheros `google-services.json` (uno por flavor):

1. **Crea dos proyectos/aplicaciones en Firebase Console**:
   - **Dev**: paquete `es.aviferdev.n3to.dev`  → `composeApp/src/dev/google-services.json`
   - **Prod**: paquete `es.aviferdev.n3to`      → `composeApp/src/prod/google-services.json`

2. **Habilita** Analytics, Crashlytics y Remote Config en ambos.

3. **Descarga** el `google-services.json` de cada aplicación y colócalo en la
   ruta indicada.

### Firebase (iOS)

Si se añade Firebase en el futuro, descarga `GoogleService-Info.plist` para
cada bundle identifier e intégralo en el proyecto de Xcode.

### Firma de release (Android)

1. **Genera un keystore** (por ejemplo, `n3to-release.jks`) y colócalo en
   `composeApp/`.

2. **Crea** `composeApp/keystore.properties` con el siguiente contenido:

   ```properties
   storeFile=n3to-release.jks
   storePassword=tu_password_del_keystore
   keyAlias=tu_alias
   keyPassword=tu_password_del_alias
   ```

> **Nota:** `storeFile` es una ruta relativa a `composeApp/`.

## Build

### Android

| Comando | Resultado |
|---------|-----------|
| `./gradlew :composeApp:assembleDevDebug` | Debug (dev flavor) |
| `./gradlew :composeApp:assembleDevRelease` | Release (dev flavor) |
| `./gradlew :composeApp:assembleProdRelease` | Release (prod flavor) |
| `./gradlew :composeApp:build` | Build completo |

### iOS

```bash
./gradlew :composeApp:compileKotlinIosArm64
```