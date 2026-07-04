plugins {
    id("tanuki.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature:mergerequests:domain"))
            implementation(libs.markdown.renderer.m3)
            implementation(libs.markdown.renderer.coil3)
        }
    }
}

android {
    namespace = "dev.tanuki.feature.mergerequests.presentation"
}
