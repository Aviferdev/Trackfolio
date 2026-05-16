# ─── Kotlin ────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod
-dontnote kotlinx.serialization.AnnotationsKt

# ─── Coroutines ────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ─── SQLDelight ────────────────────────────────────────
-keep class es.aviferdev.n3to.data.database.** { *; }

# ─── Koin ──────────────────────────────────────────────
-keep class org.koin.** { *; }
-keep class * extends org.koin.core.module.Module { *; }

# ─── Compose Multiplatform ─────────────────────────────
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# ─── PDFBox (Android) ──────────────────────────────────
-keep class com.tom_roush.pdfbox.** { *; }
-dontwarn com.tom_roush.pdfbox.**

# ─── Biometric ─────────────────────────────────────────
-keep class androidx.biometric.** { *; }

# ─── Navigation Compose ────────────────────────────────
-keep class androidx.navigation.** { *; }

# ─── UUID ──────────────────────────────────────────────
-keep class com.benasher44.uuid.** { *; }

# ─── Keep data classes (usados por SQLDelight/Serialization) ──
-keep class es.aviferdev.n3to.domain.model.** { *; }

# ─── AndroidX Lifecycle ────────────────────────────────
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class androidx.lifecycle.** { *; }

# ─── AppConfig (por si se usa reflexión) ───────────────
-keep class es.aviferdev.n3to.core.AppConfig { *; }
