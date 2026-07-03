plugins {
    id("tanuki.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(project(":core:data"))
            implementation(project(":feature:projects:domain"))

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
        }
    }
}

android {
    namespace = "dev.tanuki.feature.projects.data"
}
