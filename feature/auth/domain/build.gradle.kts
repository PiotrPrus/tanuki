plugins {
    id("tanuki.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
        }
    }
}

android {
    namespace = "dev.tanuki.feature.auth.domain"
}
