import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

plugins {
    application
    signing
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jmh)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.vanniktech.publish)
    `jvm-test-suite`
    `maven-publish`
}

allprojects {
    group = "net.rsprot"
    version = "1.0.0-ALPHA-20250423"

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
            compilerOptions {
                freeCompilerArgs = listOf("-Xjvm-default=all")
            }
        }
    }
}

private val exclusionRegex = Regex("""osrs-\d+""")
subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "signing")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "jvm-test-suite")

    // Do not publish the 'group' modules, as they got nothing in them.
    if (exclusionRegex.matches(name)) {
        return@subprojects
    }
    apply(plugin = "com.vanniktech.maven.publish")
    publishing {
        repositories {
            // Support for GitHub Packages publishing.
            // We do not directly use this, it is only included to make publishing for forks easier.
            maven {
                name = "GitHubPackages"
                // Change the organization and project URL to match with where you're publishing.
                url = uri("https://maven.pkg.github.com/blurite/rsprot")
                credentials {
                    // The gpr.user and gpr.key properties should be defined where your `gradle.properties`
                    // file is stored, which is typically at `user.home/.gradle/gradle.properties`
                    // IntelliJ does support overriding the Gradle user home in
                    // Build, Execution, Deployment -> Build Tools -> Gradle
                    // So the gradle.properties path may differ. If the file doesn't exist, create it
                    // and fill in the user (GitHub username) and key (GitHub Personal Access Token) as shown below.
                    // Personal access tokens can be generated at https://github.com/settings/tokens
                    // Only the packages:read and packages:write permissions are required.
                    username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                    password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
    mavenPublishing {
        coordinates(
            groupId = project.group.toString(),
            artifactId = project.name,
            version = project.version.toString(),
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

            // Enable GPG signing for all publications.
            // Signing can be skipped for localhost and GitHub packages,
            // it is only required for Maven Central.
            if ("publishAllPublicationsToMavenCentralRepository" in gradle.startParameter.taskNames) {
                signAllPublications()
            }
        }
    }
}

afterEvaluate {
    tasks.getByName("generateMetadataFileForMavenPublication") {
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
