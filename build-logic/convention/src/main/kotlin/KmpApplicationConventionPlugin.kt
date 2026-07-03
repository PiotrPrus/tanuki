import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention for the application module (:composeApp). KMP + Android application + Compose,
 * and it produces the iOS framework that the Xcode project links against.
 * Module-specific values (applicationId, versionCode/Name) stay in the module build file.
 */
class KmpApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
            apply("com.android.application")
            apply("org.jetbrains.compose")
            apply("org.jetbrains.kotlin.plugin.compose")
            apply("org.jetbrains.kotlin.plugin.serialization")
        }

        extensions.configure<KotlinMultiplatformExtension> {
            androidTarget {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
            listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
                iosTarget.binaries.framework {
                    baseName = "ComposeApp"
                    isStatic = true
                }
            }
        }

        extensions.configure<ApplicationExtension> {
            compileSdk = libs.intVersion("android-compileSdk")
            defaultConfig {
                minSdk = libs.intVersion("android-minSdk")
                targetSdk = libs.intVersion("android-targetSdk")
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
            buildFeatures {
                buildConfig = true
            }
            packaging {
                resources {
                    excludes += "/META-INF/{AL2.0,LGPL2.1}"
                }
            }
        }
    }
}
