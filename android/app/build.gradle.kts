plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.monkeycode.liquidui"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.monkeycode.liquidui"
        minSdk = 26
        targetSdk = 35
        versionCode = 16
        versionName = "2.7.0"
    }

    signingConfigs {
        create("release") {
            // Keystore is read from environment variables or local.properties.
            // For CI / one-off builds, pass:
            //   -Dorg.gradle.project.RELEASE_STORE_FILE=... -Dorg.gradle.project.RELEASE_STORE_PASSWORD=... -Dorg.gradle.project.RELEASE_KEY_ALIAS=... -Dorg.gradle.project.RELEASE_KEY_PASSWORD=...
            val storeFilePath = System.getProperty("org.gradle.project.RELEASE_STORE_FILE")
                ?: (findProperty("RELEASE_STORE_FILE") as String?)
                ?: "app/debug.keystore"
            storeFile = rootProject.file(storeFilePath)
            storePassword = System.getProperty("org.gradle.project.RELEASE_STORE_PASSWORD")
                ?: (findProperty("RELEASE_STORE_PASSWORD") as String?)
                ?: "android"
            keyAlias = System.getProperty("org.gradle.project.RELEASE_KEY_ALIAS")
                ?: (findProperty("RELEASE_KEY_ALIAS") as String?)
                ?: "androiddebugkey"
            keyPassword = System.getProperty("org.gradle.project.RELEASE_KEY_PASSWORD")
                ?: (findProperty("RELEASE_KEY_PASSWORD") as String?)
                ?: "android"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Lint issues that are not relevant for release builds
            lint {
                disable += listOf("NullSafeMutableLiveData")
            }
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.kyant.backdrop)
    implementation(libs.kyant.shapes)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
}
