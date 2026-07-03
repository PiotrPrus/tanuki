plugins {
    id("tanuki.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature:mergerequests:domain"))
        }
    }
}

android {
    namespace = "dev.tanuki.feature.mergerequests.presentation"
}
