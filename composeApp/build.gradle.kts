import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

kotlin {
    jvmToolchain(21)

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
                freeCompilerArgs += "-Xexpect-actual-classes"
            }
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            binaryOption("bundleId", "es.aviferdev.n3to.framework")
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.browser)
            implementation(libs.androidx.core.splashscreen)
            implementation(libs.sqldelight.android.driver)
            implementation(libs.koin.android)
            implementation(libs.androidx.biometric)
            implementation(libs.pdfbox.android)
            implementation(libs.firebase.analytics.ktx)
            implementation(libs.firebase.crashlytics.ktx)
            implementation(libs.firebase.config.ktx)
            implementation(libs.firebase.firestore.ktx)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.coroutines.core)
            implementation(libs.uuid)
            implementation(libs.kotlinx.datetime)
            implementation(libs.navigation.compose)
            implementation(libs.revenuecat.purchases.kmp)
        }
    }
}

sqldelight {
    databases {
        create("N3toDatabase") {
            packageName.set("es.aviferdev.n3to.data.database")
            version = 1
        }
    }
}

android {
    namespace = "es.aviferdev.n3to"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "es.aviferdev.n3to"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
    }

    // ─── RevenueCat secrets (archivo NO versionado) ───────────
    val revenuecatPropertiesFile = file("revenuecat.properties")
    val revenuecatProperties = if (revenuecatPropertiesFile.exists()) {
        Properties().apply { load(revenuecatPropertiesFile.inputStream()) }
    } else {
        null
    }
    fun revenuecatProp(key: String): String =
        revenuecatProperties?.getProperty(key)?.takeIf { it.isNotBlank() } ?: ""

    // ─── Signing (lectura de keystore.properties) ──────────────
    val keystorePropertiesFile = file("keystore.properties")
    val keystoreProperties = if (keystorePropertiesFile.exists()) {
        Properties().apply { load(keystorePropertiesFile.inputStream()) }
    } else {
        null
    }

    fun prop(key: String): String? = keystoreProperties?.getProperty(key)

    signingConfigs {
        create("release") {
            prop("storeFile")?.let { storeFile = file(it) }
            prop("storePassword")?.let { storePassword = it }
            prop("keyAlias")?.let { keyAlias = it }
            prop("keyPassword")?.let { keyPassword = it }
        }
    }

    // ─── Flavors (entornos) ───────────────────────────────────
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "N3to DEV")
            buildConfigField("String", "ENVIRONMENT", "\"dev\"")
            buildConfigField("boolean", "IS_DEBUG", "true")
            buildConfigField("String", "APP_DISPLAY_NAME", "\"N3to DEV\"")
            buildConfigField("String", "REVENUECAT_API_KEY",
                "\"${revenuecatProp("REVENUECAT_ANDROID_SANDBOX")}\"")
        }
        create("prod") {
            resValue("string", "app_name", "N3to")
            buildConfigField("String", "ENVIRONMENT", "\"prod\"")
            buildConfigField("boolean", "IS_DEBUG", "false")
            buildConfigField("String", "APP_DISPLAY_NAME", "\"N3to\"")
            buildConfigField("String", "REVENUECAT_API_KEY",
                "\"${revenuecatProp("REVENUECAT_ANDROID_PROD")}\"")
        }
    }

    // ─── Build Types ───────────────────────────────────────────
    buildTypes {
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (keystoreProperties != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    lint {
        checkReleaseBuilds = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// ─── Generar AppConfig.ios.kt desde revenuecat.properties ──
val generateIosAppConfig by tasks.registering {
    group = "ios"
    description = "Genera AppConfig.ios.kt desde revenuecat.properties"

    val sourceRoot = layout.buildDirectory.dir("generated/iosAppConfig")
    val outputFile = sourceRoot.map { it.file("es/aviferdev/n3to/core/AppConfig.ios.kt") }

    outputs.dir(sourceRoot)

    doLast {
        val env = project.findProperty("revenuecat.ios.env") as? String
            ?: System.getenv("REVENUECAT_IOS_ENV")
            ?: "sandbox"

        val rcPropFile = file("revenuecat.properties")
        val props = if (rcPropFile.exists()) {
            Properties().apply { load(rcPropFile.inputStream()) }
        } else {
            Properties()
        }

        fun prop(key: String): String =
            props.getProperty(key)?.takeIf { it.isNotBlank() } ?: ""

        val keyProp = if (env == "prod") "REVENUECAT_IOS_PROD" else "REVENUECAT_IOS_SANDBOX"
        val revenueCatApiKey = prop(keyProp)

        val isDebug = env != "prod"
        val environment = if (isDebug) "dev" else "prod"
        val appDisplayName = if (isDebug) "N3to DEV" else "N3to"

        outputFile.get().asFile.parentFile.mkdirs()

        outputFile.get().asFile.writeText("""
            package es.aviferdev.n3to.core

            /**
             * Implementación iOS de AppConfig.
             * GENERADO AUTOMÁTICAMENTE por la tarea generateIosAppConfig.
             * NO modificar manualmente.
             *
             * Entorno iOS: $env
             */
            actual object AppConfig {
                actual val environment: String = "$environment"
                actual val isDebug: Boolean = $isDebug
                actual val appDisplayName: String = "$appDisplayName"
                actual val revenueCatApiKey: String = "$revenueCatApiKey"
            }
        """.trimIndent())
    }
}

// Enganchar la generación antes de compilar Kotlin para iOS
tasks.matching { it.name.startsWith("compileKotlinIos") }.configureEach {
    dependsOn(generateIosAppConfig)
}

// Añadir el directorio generado a los source sets de iOS
kotlin.sourceSets {
    val iosMain by getting {
        kotlin.srcDir(layout.buildDirectory.dir("generated/iosAppConfig"))
    }
}

afterEvaluate {
    tasks.matching { it.name.startsWith("uploadCrashlyticsMappingFile") && it.name.contains("Dev") }
        .configureEach { enabled = false }
}

