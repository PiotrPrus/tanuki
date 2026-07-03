import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

// NOTE: these are `internal` on purpose. If they were public they would leak onto
// the classpath of every module that applies a convention plugin and shadow Gradle's
// generated type-safe `libs` accessor (turning it into a bare VersionCatalog with no
// `.ktor`/`.koin`/… groups). Keeping them internal confines them to :build-logic.

/** Access the `libs` version catalog from inside a convention plugin. */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

/** Read an int version (e.g. compileSdk) from the catalog. */
internal fun VersionCatalog.intVersion(alias: String): Int =
    findVersion(alias).get().requiredVersion.toInt()
