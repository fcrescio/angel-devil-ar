plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.angelmirror"
    compileSdk = 35

    signingConfigs {
        getByName("debug") {
            storeFile = file("keystores/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    defaultConfig {
        applicationId = "com.angelmirror"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.01.00"))
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("com.google.ar:core:1.49.0")
    implementation("io.github.sceneview:arsceneview:2.3.2")
    implementation("com.google.android.filament:filament-android:1.68.2")
    implementation("com.google.android.filament:filament-utils-android:1.68.2")
    implementation("com.google.android.filament:gltfio-android:1.68.2")

    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
}

configurations.configureEach {
    resolutionStrategy {
        // SceneView 2.3.2 is the first 2.x release compiled against the static
        // Filament Utils.init API used by 1.68.2. Keep AndroidX Core pinned
        // until the project intentionally moves to compileSdk 36 / AGP 8.9+.
        force("androidx.core:core:1.15.0")
        force("androidx.core:core-ktx:1.15.0")
        force("org.jetbrains.kotlin:kotlin-stdlib:2.3.0")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.3.0")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.3.0")
    }
}
