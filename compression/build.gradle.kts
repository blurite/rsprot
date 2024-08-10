dependencies {
    api(platform(rootProject.libs.netty.bom))
    api(rootProject.libs.netty.buffer)
    api(projects.buffer)
    implementation(rootProject.libs.inline.logger)
}

mavenPublishing {
    pom {
        name = "RsProt Compression"
        description = "Compression methods utilized by the RuneScape client protocol."
    }
}
