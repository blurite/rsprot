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
