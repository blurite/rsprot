dependencies {
    implementation(platform(rootProject.libs.netty.bom))
    implementation(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.netty.transport)
    implementation(projects.buffer)
    implementation(projects.crypto)
    implementation(projects.compression)
}

testing {
    suites {
        register("konsistTest", JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation(libs.konsist)
            }
        }
    }
}

tasks.named("check") {
    dependsOn(testing.suites.named("konsistTest"))
}

mavenPublishing {
    pom {
        name = "RsProt Protocol"
        description = "Base protocol module for any RuneScape version networking."
    }
}
