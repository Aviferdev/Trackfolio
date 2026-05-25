import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.kotlinSerialization)
    kotlin("native.cocoapods")
}

kotlin {
    jvmToolchain(21)

    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }
            }
        }
    }

    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        version = "1.0"
        summary = "Trackfolio KMP framework"
        homepage = "https://aviferdev.es"
        ios.deploymentTarget = "16.0"
        framework {
            baseName = "ComposeApp"
            isStatic = true
            binaryOption("bundleId", "es.aviferdev.n3to.framework")
        }
        pod("FirebaseCore") { version = "11.9.0" }
        pod("FirebaseAnalytics") { version = "11.9.0" }
        pod("FirebaseCrashlytics") { version = "11.9.0" }
        pod("FirebaseRemoteConfig") { version = "11.9.0" }
        // El plugin CocoaPods solo reconoce "Debug" y "Release" por defecto.
        // Sin este mapeo el task syncFramework falla con
        // "Could not identify build type" al recibir DevDebug/ProdRelease, etc.
        xcodeConfigurationToNativeBuildType["DevDebug"]    = org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.DEBUG
        xcodeConfigurationToNativeBuildType["DevRelease"]  = org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.RELEASE
        xcodeConfigurationToNativeBuildType["ProdDebug"]   = org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.DEBUG
        xcodeConfigurationToNativeBuildType["ProdRelease"] = org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.RELEASE
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
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
            implementation(libs.ktor.client.darwin)
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
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
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
        versionName = "0.0.1"
    }

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
        }
        create("prod") {
            resValue("string", "app_name", "N3to")
            buildConfigField("String", "ENVIRONMENT", "\"prod\"")
            buildConfigField("boolean", "IS_DEBUG", "false")
            buildConfigField("String", "APP_DISPLAY_NAME", "\"N3to\"")
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


afterEvaluate {
    tasks.matching { it.name.startsWith("uploadCrashlyticsMappingFile") && it.name.contains("Dev") }
        .configureEach { enabled = false }
}

apply(from = rootProject.file("gradle/translation-verification.gradle.kts"))

tasks.named("check") {
    dependsOn("verifyTranslations")
}

