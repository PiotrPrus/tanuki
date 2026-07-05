plugins {
    id("tanuki.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature:mergerequests:domain"))
            implementation(compose.materialIconsExtended)
            implementation(libs.coil.compose)
            implementation(libs.markdown.renderer.m3)
            implementation(libs.markdown.renderer.coil3)
        }
        androidMain.dependencies {
            implementation(libs.media3.exoplayer)
            implementation(libs.media3.ui)
        }
    }
}

android {
    namespace = "dev.tanuki.feature.mergerequests.presentation"
}
