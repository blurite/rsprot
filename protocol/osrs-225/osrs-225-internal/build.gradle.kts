dependencies {
    api(platform(rootProject.libs.netty.bom))
    api(rootProject.libs.netty.buffer)
    api(rootProject.libs.netty.transport)
    api(rootProject.libs.commons.pool2)
    implementation(rootProject.libs.inline.logger)
    api(projects.buffer)
    api(projects.compression)
    api(projects.crypto)
    api(projects.protocol)
    api(projects.protocol.osrs225.osrs225Common)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 225 Internal"
        description = "The internal module for revision 225 OldSchool RuneScape networking, " +
            "offering internal hidden implementations behind the library."
    }
}
