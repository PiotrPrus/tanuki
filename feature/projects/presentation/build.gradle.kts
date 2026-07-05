plugins {
    id("tanuki.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature:projects:domain"))
            implementation(compose.materialIconsExtended)
            implementation(libs.coil.compose)
            implementation(libs.markdown.renderer.m3)
        }
    }
}

android {
    namespace = "dev.tanuki.feature.projects.presentation"
}
