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
            implementation(libs.sqldelight.android.driver)
            implementation(libs.koin.android)
            implementation(libs.androidx.biometric)
            implementation(libs.pdfbox.android)
            implementation(libs.firebase.analytics.ktx)
            implementation(libs.firebase.crashlytics.ktx)
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
            // implementation(libs.revenuecat.purchases.kmp) // TODO: Descomentar cuando RevenueCat esté disponible
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
            resValue("string", "app_name", "Trackfolio DEV")
            buildConfigField("String", "ENVIRONMENT", "\"dev\"")
            buildConfigField("boolean", "IS_DEBUG", "true")
            buildConfigField("String", "APP_DISPLAY_NAME", "\"Trackfolio DEV\"")
            buildConfigField("String", "REVENUECAT_API_KEY", "\"INSERT_REVENUECAT_DEV_KEY\"")
        }
        create("prod") {
            resValue("string", "app_name", "Trackfolio")
            buildConfigField("String", "ENVIRONMENT", "\"prod\"")
            buildConfigField("boolean", "IS_DEBUG", "false")
            buildConfigField("String", "APP_DISPLAY_NAME", "\"Trackfolio\"")
            buildConfigField("String", "REVENUECAT_API_KEY", "\"INSERT_REVENUECAT_PROD_KEY\"")
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


