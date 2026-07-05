plugins {
    id("tanuki.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature:auth:domain"))
        }
    }
}

android {
    namespace = "dev.tanuki.feature.auth.presentation"
}

compose.resources {
    publicResClass = true
    packageOfResClass = "dev.tanuki.feature.auth.presentation.resources"
}
