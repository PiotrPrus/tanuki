import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * KMP library + Compose Multiplatform. Used by presentation and design-system modules.
 * Applies the base library convention, the Compose plugin, and the Compose compiler,
 * then adds the shared Compose UI dependencies to commonMain.
 */
class KmpComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("tanuki.kmp.library")
            apply("org.jetbrains.compose")
            apply("org.jetbrains.kotlin.plugin.compose")
        }

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.getByName("commonMain").dependencies {
                implementation(libs.findLibrary("compose-runtime").get())
                implementation(libs.findLibrary("compose-foundation").get())
                implementation(libs.findLibrary("compose-material3").get())
                implementation(libs.findLibrary("compose-ui").get())
                implementation(libs.findLibrary("compose-components-resources").get())
                implementation(libs.findLibrary("compose-uiToolingPreview").get())
                implementation(libs.findLibrary("androidx-lifecycle-viewmodelCompose").get())
                implementation(libs.findLibrary("androidx-lifecycle-runtimeCompose").get())
            }
        }
    }
}
