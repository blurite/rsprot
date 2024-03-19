import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

rootProject.name = "rsprot"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    "buffer",
    "protocol",
    "crypto",
)

includeSubprojects(":protocol")

fun includeSubprojects(projectName: String) {
    val projectPath = project(projectName).projectDir.toPath()
    try {
        Files.walk(projectPath).filter(Files::isDirectory).forEach {
            searchSubproject(projectName, projectPath, it)
        }
    } catch (e: IOException) {
        System.err.println("Failed to walk plugin dir, skipping")
    }
}

fun searchSubproject(
    projectName: String,
    projectPath: Path,
    subprojectPath: Path,
) {
    val isMissingBuildFile = Files.notExists(subprojectPath.resolve("build.gradle.kts"))
    if (isMissingBuildFile) return
    val relativePath = projectPath.relativize(subprojectPath)
    val subprojectName = relativePath.toString().replace(File.separator, ":")
    include("$projectName:$subprojectName")
}
