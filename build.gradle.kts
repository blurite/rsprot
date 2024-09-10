import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

val ossrhUsername: String? by ext
val ossrhPassword: String? by ext

plugins {
    application
    signing
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jmh)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.vanniktech.publish)
    alias(libs.plugins.dokka)
    `jvm-test-suite`
}

allprojects {
    group = "net.rsprot"
    version = "1.0.0-ALPHA-20240910"

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
            testImplementation(rootProject.libs.junit.api)
            testImplementation(rootProject.libs.junit.params)
            testRuntimeOnly(rootProject.libs.junit.engine)
            testRuntimeOnly(rootProject.libs.junit.launcher)
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

private val exclusionRegex = Regex("""osrs-\d+""")
subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "signing")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "jvm-test-suite")

    // Do not publish the 'group' modules, as they got nothing in them.
    if (exclusionRegex.matches(name)) {
        return@subprojects
    }
    apply(plugin = "com.vanniktech.maven.publish")
    mavenPublishing {
        coordinates(
            groupId = project.group.toString(),
            artifactId = project.name,
            version = project.version.toString(),
        )

        configure(
            KotlinJvm(
                javadocJar = JavadocJar.Dokka("dokkaHtml"),
                sourcesJar = true,
            ),
        )

        pom {
            url = "https://github.com/blurite/rsprot"
            inceptionYear = "2024"

            licenses {
                license {
                    name = "MIT"
                    url = "https://github.com/blurite/rsprot/blob/master/LICENSE"
                }
            }

            organization {
                name = "Blurite"
                url = "https://github.com/blurite"
            }

            scm {
                connection = "scm:git:git://github.com/blurite/rsprot.git"
                developerConnection = "scm:git:ssh://git@github.com/blurite/rsprot.git"
                url = "https://github.com/blurite/rsprot"
            }

            developers {
                developer {
                    name = "Kris"
                    url = "https://github.com/Z-Kris"
                }
            }

            // Configure publishing to Maven Central
            publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

            // Enable GPG signing for all publications
            signAllPublications()
        }
    }
}

afterEvaluate {
    tasks.getByName("generateMetadataFileForMavenPublication") {
        dependsOn(tasks.getByName("dokkaJavadocJar"))
        dependsOn(tasks.kotlinSourcesJar)
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
