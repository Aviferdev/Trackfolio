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

### RevenueCat (compras in-app)

Se necesita un archivo `composeApp/revenuecat.properties` con las claves de API
de RevenueCat. Este archivo **no está versionado** (está en `.gitignore`).

1. **Copia la plantilla:**

   ```bash
   cp composeApp/revenuecat.properties.example composeApp/revenuecat.properties
   ```

2. **Obtén tus claves en [RevenueCat Dashboard](https://app.revenuecat.com):**
   - Ve a **Project Settings → API Keys**.
   - Las claves disponibles son:

   | Propiedad en `.properties` | Prefijo | Propósito |
   |----------------------------|---------|-----------|
   | `REVENUECAT_ANDROID_SANDBOX` | `test_` | Pruebas Android (`dev` flavor) |
   | `REVENUECAT_ANDROID_PROD` | `goog_` | Producción Google Play (`prod` flavor) |
   | `REVENUECAT_IOS_SANDBOX` | `test_` | Pruebas iOS |
   | `REVENUECAT_IOS_PROD` | `appl_` | Producción App Store |

3. **Edita `composeApp/revenuecat.properties`** y pega cada clave en su
   propiedad correspondiente.

4. **Verifica que el archivo está ignorado por Git:**

   ```bash
   git check-ignore composeApp/revenuecat.properties
   ```

> ⚠️ **Seguridad:** Ninguna clave de RevenueCat debe estar hardcodeada en el
> código o en `build.gradle.kts`. Todas se leen desde
> `revenuecat.properties`, que está excluido del repositorio.

#### CI/CD

En pipelines automatizados, crea el archivo antes del build inyectando los
secretos:

```yaml
# GitHub Actions
- name: Configure RevenueCat secrets
  run: |
    cat > composeApp/revenuecat.properties << 'EOF'
    REVENUECAT_ANDROID_SANDBOX=${{ secrets.REVENUECAT_ANDROID_SANDBOX }}
    REVENUECAT_ANDROID_PROD=${{ secrets.REVENUECAT_ANDROID_PROD }}
    REVENUECAT_IOS_SANDBOX=${{ secrets.REVENUECAT_IOS_SANDBOX }}
    REVENUECAT_IOS_PROD=${{ secrets.REVENUECAT_IOS_PROD }}
    EOF
```

#### iOS (entorno)

Por defecto, la compilación iOS usa el entorno **sandbox**. Para compilar en
**producción**, añade la propiedad de Gradle:

```bash
./gradlew :composeApp:linkReleaseFrameworkIosArm64 -Prevenuecat.ios.env=prod
```

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

## Verificación de traducciones

El proyecto tiene **10 idiomas soportados** (definidos en una lista canónica)
y actualmente **15 claves de string pendientes de traducción** en 5 locales
(de, fr, ja, ru, zh). Una tarea de Gradle verifica automáticamente que:

1. Todos los idiomas de la lista tengan su fichero `strings.xml`
2. Todos contengan las mismas claves que el fichero base (`values/strings.xml`)

Si se añade un nuevo locale a la lista pero se olvida crear el directorio
`values-XX/strings.xml`, la tarea lo reportará como **locale faltante**.

### Uso

```bash
# Ejecutar bajo demanda (muestra el reporte en consola)
./gradlew :composeApp:verifyTranslations

# Se ejecuta automáticamente como parte del lifecycle check
./gradlew :composeApp:check
```

La tarea **no bloquea la compilación**; emite un warning con el detalle de
las incidencias. El reporte completo se escribe en:

```
composeApp/build/reports/translations/missing-keys.txt
```

### Cobertura actual

```
   Locale   Name           Keys    Missing    Coverage
   ------   ----           ----    -------    -------
   es       Español        1208    —          100.0% ★
   en       English        1208    0          100.0% ✅
   de       Deutsch        1205    3          99.8%  ⚠️
   fr       Français       1205    3          99.8%  ⚠️
   it       Italiano       1208    0          100.0% ✅
   ja       日本語            1205    3          99.8%  ⚠️
   ko       한국어            1208    0          100.0% ✅
   pt       Português      1208    0          100.0% ✅
   ru       Русский        1205    3          99.8%  ⚠️
   zh       中文             1205    3          99.8%  ⚠️
```

| Icono | Significado |
|-------|-------------|
| ★ | Locale base |
| ✅ | Traducción completa |
| ⚠️ | Faltan claves |
| ❌ | No existe el fichero `strings.xml` |

### Cómo añadir un nuevo idioma

Edita el mapa `supportedLocales` dentro del `doLast` de la tarea en
`gradle/translation-verification.gradle.kts`:

```kotlin
val supportedLocales = linkedMapOf(
    "es" to "Español",    // ← siempre el primero (base)
    "en" to "English",
    // ...
    "cs" to "Čeština",    // ← nuevo locale
)
```

Después crea el directorio `composeResources/values-cs/` con su
`strings.xml`. La tarea verificará que contenga todas las claves del base.

### Cómo marcar una clave como «no traducible»

Si una clave no necesita traducción (ej: una versión o un nombre propio),
añade el atributo `translatable="false"` en el fichero base:

```xml
<string name="app_version" translatable="false">v1.0.0</string>
```

La tarea ignorará esa clave en sus comprobaciones.

### Implementación

La lógica está en `gradle/translation-verification.gradle.kts` y se aplica
desde `composeApp/build.gradle.kts` mediante:

```kotlin
apply(from = rootProject.file("gradle/translation-verification.gradle.kts"))
```