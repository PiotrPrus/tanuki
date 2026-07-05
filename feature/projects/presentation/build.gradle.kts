plugins {
    id("tanuki.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature:projects:domain"))
            implementation(compose.materialIconsExtended)
        }
    }
}

android {
    namespace = "dev.tanuki.feature.projects.presentation"
}
