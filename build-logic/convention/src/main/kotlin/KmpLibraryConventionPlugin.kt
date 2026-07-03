import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Base convention for a Kotlin Multiplatform library module (Android + iOS targets).
 * Used by domain and data layers. Modules must set their own `android.namespace`.
 */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
            apply("com.android.library")
            apply("org.jetbrains.kotlin.plugin.serialization")
        }

        extensions.configure<KotlinMultiplatformExtension> {
            androidTarget {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
            iosArm64()
            iosSimulatorArm64()

            sourceSets.getByName("commonMain").dependencies {
                implementation(libs.findLibrary("kotlinx-coroutines-core").get())
            }
            sourceSets.getByName("commonTest").dependencies {
                implementation(libs.findLibrary("kotlin-test").get())
                implementation(libs.findLibrary("kotlinx-coroutines-test").get())
            }
        }

        extensions.configure<LibraryExtension> {
            compileSdk = libs.intVersion("android-compileSdk")
            defaultConfig {
                minSdk = libs.intVersion("android-minSdk")
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
        }
    }
}
