dependencies {
    api(platform(rootProject.libs.netty.bom))
    api(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.inline.logger)
    api(rootProject.libs.netty.transport)
    api(projects.buffer)
    api(projects.compression)
    api(projects.crypto)
    api(projects.protocol)
    api(projects.protocol.osrs243.osrs243Model)
    api(projects.protocol.osrs243.osrs243Internal)
    api(projects.protocol.osrs243.osrs243Common)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 243 Shared"
        description = "The shared module for revision 243 OldSchool RuneScape networking, " +
            "offering a set of shared classes that do not depend on a specific client."
    }
}
