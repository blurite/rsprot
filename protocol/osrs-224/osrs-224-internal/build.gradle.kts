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
    api(projects.protocol.osrs224.osrs224Common)
    implementation(rootProject.libs.fastutil)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 224 Internal"
        description = "The internal module for revision 224 OldSchool RuneScape networking, " +
            "offering internal hidden implementations behind the library."
    }
}
