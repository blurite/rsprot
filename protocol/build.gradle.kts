dependencies {
    implementation(platform(rootProject.libs.netty.bom))
    implementation(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.netty.transport)
    implementation(projects.buffer)
    implementation(projects.crypto)
    implementation(projects.compression)
}

mavenPublishing {
    pom {
        name = "RsProt Protocol"
        description = "Base protocol module for any RuneScape version networking."
    }
}
