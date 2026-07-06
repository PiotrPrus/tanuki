plugins {
    id("tanuki.kmp.application")
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

    defaultConfig {
        applicationId = "dev.tanuki"
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

