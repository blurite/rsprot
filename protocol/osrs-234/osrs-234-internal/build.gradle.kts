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
    api(projects.protocol.osrs234.osrs234Common)
    implementation(rootProject.libs.fastutil)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 234 Internal"
        description = "The internal module for revision 234 OldSchool RuneScape networking, " +
            "offering internal hidden implementations behind the library."
    }
}
