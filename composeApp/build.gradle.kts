plugins {
    id("tanuki.kmp.application")
    alias(libs.plugins.firebaseAppDistribution)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Feature UIs
            implementation(project(":feature:auth:presentation"))
            implementation(project(":feature:mergerequests:presentation"))
            implementation(project(":feature:projects:presentation"))
            // Feature data + core (wired into Koin at app startup)
            implementation(project(":feature:auth:domain"))
            implementation(project(":feature:auth:data"))
            implementation(project(":feature:mergerequests:data"))
            implementation(project(":feature:projects:domain"))
            implementation(project(":feature:projects:data"))
            implementation(project(":core:domain"))
            implementation(project(":core:data"))
            implementation(project(":core:presentation"))
            implementation(project(":core:design-system"))

            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // DI + navigation
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.navigation.compose)

            // Image loading (authenticated ImageLoader wired at startup)
            implementation(libs.ktor.client.core)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "dev.tanuki"

    // SemVer. Defaults live in gradle.properties; CI overrides them from the release tag
    // (e.g. `-Ptanuki.versionName=0.1.0 -Ptanuki.versionCode=42`).
    val tanukiVersionName = (findProperty("tanuki.versionName") as String?) ?: "0.1.0"
    val tanukiVersionCode = (findProperty("tanuki.versionCode") as String?)?.toIntOrNull() ?: 1

    defaultConfig {
        applicationId = "dev.tanuki"
        versionCode = tanukiVersionCode
        versionName = tanukiVersionName
    }

    signingConfigs {
        create("release") {
            // Populated from CI secrets; left unset locally (release then falls back to debug).
            System.getenv("ANDROID_KEYSTORE_FILE")?.let { path ->
                storeFile = file(path)
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            // Sign with the release keystore when CI provides it; otherwise use the debug
            // signing config so `assembleRelease` still works on a fresh checkout.
            signingConfig = if (System.getenv("ANDROID_KEYSTORE_FILE") != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }
}

// Firebase App Distribution — configured entirely from env vars so nothing secret is
// committed. Only the `appDistributionUploadRelease` task reads these; normal builds ignore them.
firebaseAppDistribution {
    appId = System.getenv("FIREBASE_APP_ID") ?: ""
    serviceCredentialsFile = System.getenv("FIREBASE_SERVICE_ACCOUNT_FILE") ?: ""
    groups = System.getenv("FIREBASE_TESTER_GROUPS") ?: "internal-testers"
    artifactType = "APK"
    releaseNotes = System.getenv("FIREBASE_RELEASE_NOTES") ?: "Internal test build"
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

