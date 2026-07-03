import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention for a feature `presentation` module: Compose + Koin + Navigation + lifecycle,
 * plus the shared core modules every feature UI layer depends on.
 * Feature modules never depend on other features (see android-module-structure).
 */
class KmpFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("tanuki.kmp.compose")

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.getByName("commonMain").dependencies {
                implementation(project(":core:domain"))
                implementation(project(":core:presentation"))
                implementation(project(":core:design-system"))

                implementation(libs.findLibrary("koin-core").get())
                implementation(libs.findLibrary("koin-compose").get())
                implementation(libs.findLibrary("koin-compose-viewmodel").get())

                implementation(libs.findLibrary("navigation-compose").get())
            }
        }
    }
}
