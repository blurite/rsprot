import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

plugins {
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jmh)
    alias(libs.plugins.ktlint)
    `maven-publish`
}

allprojects {
    group = "net.rsprot"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    plugins.withType<KotlinPluginWrapper> {
        dependencies {
            testImplementation(kotlin("test-junit5"))
            testImplementation(libs.junit.api)
            testImplementation(libs.junit.params)
            testRuntimeOnly(libs.junit.engine)
            testRuntimeOnly(libs.junit.launcher)
        }

        tasks.test {
            useJUnitPlatform()
        }

        kotlin {
            jvmToolchain(11)
            explicitApi()
        }
    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "maven-publish")

    plugins.withType<MavenPublishPlugin> {
        configure<PublishingExtension> {
            publications.withType<MavenPublication> {
                groupId = "net.rsprot"
                artifactId = project.name
                version = "1.0-SNAPSHOT"
            }
        }
    }
}

// fixes some weird error with "Entry classpath.index is a duplicate but no duplicate handling strategy has been set"
// see https://github.com/gradle/gradle/issues/17236
gradle.taskGraph.whenReady {
    val duplicateTasks =
        allTasks
            .filter { it.hasProperty("duplicatesStrategy") }
    for (task in duplicateTasks) {
        task.setProperty("duplicatesStrategy", "EXCLUDE")
    }
}
