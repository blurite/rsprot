dependencies {
    implementation(platform(rootProject.libs.netty.bom))
    implementation(rootProject.libs.netty.buffer)
    implementation(projects.buffer)
    implementation(rootProject.libs.inline.logger)
}

mavenPublishing {
    pom {
        name = "RsProt Compression"
        description = "Compression methods utilized by the RuneScape client protocol."
    }
}
