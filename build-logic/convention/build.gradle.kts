import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "dev.tanuki.buildlogic"

// Build-logic itself targets JDK 11 bytecode (matches the app's jvmTarget).
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    // Gradle plugin artifacts we apply from our convention plugins (compileOnly:
    // they're on the build classpath, applied at runtime in consumer projects).
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.kotlinSerialization.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.composeCompiler.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "tanuki.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("kmpCompose") {
            id = "tanuki.kmp.compose"
            implementationClass = "KmpComposeConventionPlugin"
        }
        register("kmpFeature") {
            id = "tanuki.kmp.feature"
            implementationClass = "KmpFeatureConventionPlugin"
        }
        register("kmpApplication") {
            id = "tanuki.kmp.application"
            implementationClass = "KmpApplicationConventionPlugin"
        }
    }
}
