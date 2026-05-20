# Trackfolio

Aplicación de gestión financiera personal construida con **Kotlin Multiplatform** y **Compose Multiplatform**,
compartiendo UI y lógica de negocio entre Android e iOS.

## Stack tecnológico

| Propósito | Tecnología |
|-----------|-----------|
| Lenguaje | Kotlin 2.3.21 |
| UI | Compose Multiplatform 1.7.3 |
| DI | Koin 4.0.0 |
| Persistencia | SQLDelight 2.0.2 |
| Async | Coroutines 1.9.0 |
| Navegación | Navigation Compose 2.8.0-alpha13 |
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

---

## Requisitos previos

| Herramienta | Versión mínima | Instalación |
|-------------|---------------|-------------|
| Xcode | 15.0 | Mac App Store |
| CocoaPods | 1.14 | `sudo gem install cocoapods` |
| JDK | 21 | `brew install --cask temurin@21` |
| Android Studio / IntelliJ | 2024.x | jetbrains.com |

> **CocoaPods es obligatorio para iOS.** El plugin `kotlin("native.cocoapods")`
> lo usa para enlazar los SDKs de Firebase (Analytics, Crashlytics, Remote Config)
> dentro del framework KMP durante la compilación.

---

## Configuraciones de build

Ambas plataformas tienen **4 configuraciones equivalentes** que combinan entorno (`dev`/`prod`)
con tipo de compilación (`debug`/`release`).

### Android — Product flavors × build types

| Variante | Entorno | Bundle ID | Optimización | App icon |
|----------|---------|-----------|--------------|----------|
| `devDebug` | Dev (sandbox) | `…n3to.dev` | Sin minify | Debug |
| `devRelease` | Dev (sandbox) | `…n3to.dev` | Minify + R8 | Debug |
| `prodDebug` | Prod | `…n3to` | Sin minify | Release |
| `prodRelease` | Prod | `…n3to` | Minify + R8 | Release |

La variable `BuildConfig.ENVIRONMENT` expone `"dev"` o `"prod"` en tiempo de ejecución.
La clave de RevenueCat se inyecta por flavor desde `revenuecat.properties`.

### iOS — Xcode build configurations × schemes

Equivalencia directa con Android. Selecciona el scheme en Xcode →
**Product → Scheme** o en el selector de la toolbar.

| Scheme / Configuración | Entorno | Bundle ID | Optimización Swift | App icon |
|------------------------|---------|-----------|-------------------|----------|
| `DevDebug` | Dev (sandbox) | `…n3to.dev` | `-Onone` + `DEBUG=1` | Debug |
| `DevRelease` | Dev (sandbox) | `…n3to.dev` | `wholemodule` | Debug |
| `ProdDebug` | Prod | `…n3to` | `-Onone` + `DEBUG=1` | Release |
| `ProdRelease` | Prod | `…n3to` | `wholemodule` | Release |

Cada configuración:
- Lee su bundle ID y nombre de app desde el xcconfig correspondiente
  (`Configuration/DevDebug.xcconfig`, etc.)
- Copia automáticamente el `GoogleService-Info.plist` correcto al bundle
  (build phase **"Copy Firebase Config"**)
- Llama al Gradle task `syncFramework` (vía CocoaPods) con el tipo de build
  y el entorno de RevenueCat adecuado (`sandbox` o `prod`)

El objeto `AppConfig` (KMP `expect/actual`) expone `environment`, `isDebug` y
`revenueCatApiKey` en código compartido con el mismo valor que en Android:

```kotlin
// commonMain — igual en ambas plataformas
AppConfig.environment    // "dev" | "prod"
AppConfig.isDebug        // true | false
AppConfig.revenueCatApiKey
```

---

## Ficheros sensibles (excluidos del repositorio)

Los ficheros con credenciales están en `.gitignore`. A continuación se indica
cómo obtenerlos y dónde colocarlos antes de compilar.

### Firebase

#### Android

Se necesitan dos ficheros `google-services.json` (uno por flavor):

1. **Crea dos aplicaciones Android en Firebase Console**:
   - **Dev**: paquete `es.aviferdev.n3to.dev`  → `composeApp/src/dev/google-services.json`
   - **Prod**: paquete `es.aviferdev.n3to`      → `composeApp/src/prod/google-services.json`

2. **Habilita** Analytics, Crashlytics y Remote Config en ambas.

3. **Descarga** el `google-services.json` de cada aplicación y colócalo en la ruta indicada.

#### iOS

Se necesitan dos ficheros `GoogleService-Info.plist` (uno por entorno):

1. **Crea dos aplicaciones iOS en Firebase Console**
   (pueden estar en los mismos proyectos Firebase que Android):
   - **Dev**: bundle ID `es.aviferdev.n3to.dev`  → `iosApp/Configuration/Firebase/Dev/GoogleService-Info.plist`
   - **Prod**: bundle ID `es.aviferdev.n3to`      → `iosApp/Configuration/Firebase/Prod/GoogleService-Info.plist`

2. **Habilita** Analytics, Crashlytics y Remote Config en ambas.

3. **Descarga** el `GoogleService-Info.plist` de cada aplicación y colócalo en la ruta indicada.
   El fichero `.example` muestra la estructura esperada:

   ```bash
   cat iosApp/Configuration/Firebase/Dev/GoogleService-Info.plist.example
   ```

4. El build phase **"Copy Firebase Config"** selecciona automáticamente el plist correcto
   según el scheme activo. Si el fichero no existe al compilar, Xcode muestra un error
   con la ruta exacta donde colocarlo.

   | Scheme Xcode | Plist copiado |
   |--------------|---------------|
   | `DevDebug` / `DevRelease` | `Firebase/Dev/GoogleService-Info.plist` |
   | `ProdDebug` / `ProdRelease` | `Firebase/Prod/GoogleService-Info.plist` |

> La inicialización de Firebase (`FIRApp.configure()`) se ejecuta automáticamente
> al arrancar la app desde el código KMP compartido, igual que el plugin
> `google-services` lo hace en Android.

### Firma de release

#### Android

1. **Genera un keystore** (por ejemplo, `n3to-release.jks`) y colócalo en `composeApp/`.

2. **Crea** `composeApp/keystore.properties`:

   ```properties
   storeFile=n3to-release.jks
   storePassword=tu_password_del_keystore
   keyAlias=tu_alias
   keyPassword=tu_password_del_alias
   ```

   > `storeFile` es una ruta relativa a `composeApp/`.

#### iOS

Equivalente al `keystore.properties` de Android:

1. **Crea** `iosApp/Configuration/signing.xcconfig` desde la plantilla:

   ```bash
   cp iosApp/Configuration/signing.xcconfig.example iosApp/Configuration/signing.xcconfig
   ```

2. **Edita** el fichero y sustituye `REEMPLAZAR_CON_TEAM_ID` por tu
   Apple Developer Team ID (10 caracteres). Lo encuentras en
   [developer.apple.com → Membership](https://developer.apple.com/account).

3. **Verifica que está ignorado por Git:**

   ```bash
   git check-ignore iosApp/Configuration/signing.xcconfig
   ```

   Los xcconfigs de entorno (`Dev.xcconfig`, `Prod.xcconfig`) incluyen este fichero
   con `#include?`, por lo que si no existe la firma cae al valor vacío sin romper
   el build local de debug.

### RevenueCat (compras in-app)

Un único fichero `composeApp/revenuecat.properties` contiene las claves para ambas
plataformas. No está versionado (`.gitignore`).

1. **Copia la plantilla:**

   ```bash
   cp composeApp/revenuecat.properties.example composeApp/revenuecat.properties
   ```

2. **Obtén tus claves en [RevenueCat Dashboard](https://app.revenuecat.com):**
   (Project Settings → API Keys)

   | Propiedad | Prefijo | Plataforma / Entorno |
   |-----------|---------|----------------------|
   | `REVENUECAT_ANDROID_SANDBOX` | `test_` | Android — flavor `dev` |
   | `REVENUECAT_ANDROID_PROD`    | `goog_` | Android — flavor `prod` |
   | `REVENUECAT_IOS_SANDBOX`     | `test_` | iOS — configuraciones `Dev*` |
   | `REVENUECAT_IOS_PROD`        | `appl_` | iOS — configuraciones `Prod*` |

3. **Edita `revenuecat.properties`** y pega cada clave.

4. **Verifica que está ignorado por Git:**

   ```bash
   git check-ignore composeApp/revenuecat.properties
   ```

**Android:** las claves se inyectan vía `buildConfigField` en cada flavor.

**iOS:** el build phase de Xcode llama al Gradle task correspondiente con
`-Prevenuecat.ios.env=sandbox` o `-Prevenuecat.ios.env=prod` según el scheme,
que genera `AppConfig.ios.kt` con la clave correcta antes de compilar el framework.
No es necesario pasar este parámetro manualmente.

> ⚠️ Ninguna clave debe estar hardcodeada en el código ni en los ficheros de
> configuración versionados. Todas se leen en tiempo de build desde
> `revenuecat.properties`.

#### CI/CD

```yaml
# GitHub Actions — crear revenuecat.properties antes del build
- name: Configure secrets
  run: |
    cat > composeApp/revenuecat.properties << 'EOF'
    REVENUECAT_ANDROID_SANDBOX=${{ secrets.REVENUECAT_ANDROID_SANDBOX }}
    REVENUECAT_ANDROID_PROD=${{ secrets.REVENUECAT_ANDROID_PROD }}
    REVENUECAT_IOS_SANDBOX=${{ secrets.REVENUECAT_IOS_SANDBOX }}
    REVENUECAT_IOS_PROD=${{ secrets.REVENUECAT_IOS_PROD }}
    EOF

# Para iOS también se necesitan los GoogleService-Info.plist
- name: Configure Firebase iOS
  run: |
    echo "${{ secrets.GOOGLE_SERVICE_INFO_DEV }}" > \
      iosApp/Configuration/Firebase/Dev/GoogleService-Info.plist
    echo "${{ secrets.GOOGLE_SERVICE_INFO_PROD }}" > \
      iosApp/Configuration/Firebase/Prod/GoogleService-Info.plist
```

---

## Build

### Android

| Comando | Resultado |
|---------|-----------|
| `./gradlew :composeApp:assembleDevDebug` | APK debug (dev flavor) |
| `./gradlew :composeApp:assembleDevRelease` | APK release firmado (dev flavor) |
| `./gradlew :composeApp:assembleProdDebug` | APK debug (prod flavor) |
| `./gradlew :composeApp:assembleProdRelease` | APK release firmado (prod flavor) |
| `./gradlew :composeApp:build` | Build completo |

### iOS

#### Primera vez (setup inicial)

Tras clonar el repositorio ejecuta estos dos comandos **una sola vez**:

```bash
# 1. Genera el framework vacío que necesita el podspec antes de pod install
./gradlew :composeApp:generateDummyFramework

# 2. Instala los pods de Firebase y el framework KMP
cd iosApp && pod install
```

Esto crea `iosApp/Pods/` e `iosApp/iosApp.xcworkspace`.

> Repite solo `pod install` (desde `iosApp/`) si cambian las versiones de pods
> en `build.gradle.kts` o si añades nuevas dependencias de CocoaPods.

#### Abrir el proyecto en Xcode

Abre siempre el **workspace**, no el `.xcodeproj`:

```bash
open iosApp/iosApp.xcworkspace
```

Si abres `iosApp.xcodeproj` directamente, los Pods no estarán disponibles y
el build fallará con errores de frameworks no encontrados.

#### Compilación normal

Compila desde **Xcode** seleccionando el scheme deseado (`DevDebug`, `ProdRelease`, etc.).
El build phase de CocoaPods llama automáticamente al task `syncFramework` de Gradle,
que construye el framework KMP con la configuración correcta.

Los Firebase SDKs quedan enlazados **estáticamente** dentro de `ComposeApp.framework`
(`isStatic = true`), integrados a través de los pods declarados en `build.gradle.kts`.

Para archivar y distribuir, selecciona el scheme `ProdRelease` en Xcode y usa
**Product → Archive**.

---

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
