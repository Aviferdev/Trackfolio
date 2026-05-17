# ADR-0001: Decisiones tecnológicas iniciales

**Estado:** Aprobado
**Fecha:** 2026-05-17
**Decisores:** Equipo Trackfolio

## Contexto

Al iniciar el proyecto Trackfolio, se necesitaba definir el stack tecnológico base
que permitiera desarrollar una aplicación de gestión financiera personal con los
siguientes requisitos:

- Aplicación multiplataforma (Android + iOS) desde un mismo código base.
- UI declarativa y reactiva.
- Arquitectura limpia y testeable (Clean Architecture + MVVM).
- Persistencia local con SQL.
- Inyección de dependencias ligera y nativa de Kotlin.
- Soporte para cifrado, biometría y preferencias de usuario.
- Proyecto 100% Kotlin (sin Java).

## Opciones consideradas

### Opción A — Kotlin Multiplatform + Compose Multiplatform

Stack completo en torno a KMP y Compose Multiplatform.

- **Pros:**
  - Código 100% compartido entre Android e iOS (UI, lógica, datos).
  - Ecosistema moderno de Jetpack (Compose, Navigation, etc.).
  - Rendimiento nativo en ambas plataformas.
  - Comunidad creciente y soporte de JetBrains.
- **Contras:**
  - Ecosistema aún en maduración (especialmente en iOS).
  - Curva de aprendizaje para iOS (SwiftUI vs. Compose).
  - Dependencia de compiladores Kotlin/Native.

### Opción B — Flutter

Framework de Google para UI multiplataforma con Dart.

- **Pros:**
  - Madurez y comunidad grande.
  - Hot reload muy rápido.
  - Widgets personalizables.
- **Contras:**
  - Requiere aprender Dart (el equipo ya conoce Kotlin).
  - Rendimiento inferior en animaciones complejas.
  - Mayor consumo de memoria en iOS.
  - No se integra nativamente con librerías de la plataforma Kotlin.

### Opción C — React Native

Framework de Meta para apps multiplataforma con JavaScript/TypeScript.

- **Pros:**
  - Comunidad muy grande.
  - Amplio ecosistema de librerías.
  - Hot reload.
- **Contras:**
  - Bridge JS-Nativo cuello de botella en rendimiento.
  - Debugging complejo.
  - Requiere JavaScript/TypeScript (el equipo conoce Kotlin).
  - Mantenimiento de dependencias nativas problemático.

## Decisión

Se eligió **Opción A — Kotlin Multiplatform + Compose Multiplatform** porque:

1. **Stack 100% Kotlin:** todo el equipo dominaba Kotlin, eliminando la fricción de
   cambiar de lenguaje.
2. **UI compartida:** Compose Multiplatform permite compartir no solo la lógica de
   negocio sino también la interfaz de usuario, maximizando la reutilización.
3. **Ecosistema Jetpack:** integración natural con las librerías estándar de Android
   (Navigation, Lifecycle, ViewModel) que también funcionan en iOS.
4. **Rendimiento nativo:** al compilar a código nativo (no pasar por un bridge), el
   rendimiento es óptimo en ambas plataformas.
5. **Futuro multiplataforma de Kotlin:** JetBrains y Google apuestan fuerte por este
   stack como el futuro del desarrollo móvil Kotlin.

## Librerías principales elegidas

| Propósito | Librería | Justificación |
|-----------|----------|---------------|
| UI | Compose Multiplatform 1.7.3 | Única opción de UI multiplataforma nativa en Kotlin. |
| DI | Koin 4.0.0 | Ligero, sin reflection, DSL Kotlin, soporte multiplataforma. |
| Persistencia | SQLDelight 2.0.2 | SQL multiplataforma con tipos seguros y generación de código Kotlin. |
| Async | Coroutines 1.9.0 | Estándar de facto en Kotlin para concurrencia. |
| Navegación | Navigation Compose 2.8.0 | Integración directa con Compose, tipado y flexible. |
| Cifrado | AES-256-CBC (AesCrypto) | Algoritmo estándar, implementación manual sin dependencias externas. |

## Consecuencias

### Positivas
- Una sola base de código para Android e iOS.
- UI nativa en ambas plataformas.
- Todo el equipo trabaja en Kotlin, sin cambiar de lenguaje.

### Negativas / Trade-offs
- iOS requiere compilar con Kotlin/Native, lo que alarga los tiempos de compilación.
- Algunas librerías específicas de iOS necesitan `expect`/`actual` (biometría, backup).
- La comunidad de Compose Multiplatform es más pequeña que Flutter o React Native,
  por lo que hay menos recursos y ejemplos.

### Riesgos
- Cambios en el compilador de Kotlin/Native podrían requerir adaptaciones.
- Compose Multiplatform en iOS podría tener bugs o diferencias de comportamiento
  respecto a Android — mitigado con testing en ambas plataformas desde el inicio.

## Referencias

- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Koin](https://insert-koin.io/)
- [SQLDelight](https://cashapp.github.io/sqldelight/2.0.2/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [ADR-0002: Estructura de paquetes por funcionalidad](pendiente de crear)
