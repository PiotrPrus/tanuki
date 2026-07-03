plugins {
    id("tanuki.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
        }
    }
}

android {
    namespace = "dev.tanuki.core.presentation"
}
