import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

plugins {
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jmh)
}

allprojects {
    group = "net.rsprot"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
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
            jvmToolchain(8)
            explicitApi()
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
