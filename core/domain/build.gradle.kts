plugins {
    id("tanuki.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
        }
    }
}

android {
    namespace = "dev.tanuki.core.domain"
}
