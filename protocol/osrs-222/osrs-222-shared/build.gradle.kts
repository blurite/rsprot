dependencies {
    api(platform(rootProject.libs.netty.bom))
    api(rootProject.libs.netty.buffer)
    api(rootProject.libs.netty.transport)
    api(projects.buffer)
    api(projects.compression)
    api(projects.crypto)
    api(projects.protocol)
    api(projects.protocol.osrs222.osrs222Model)
    api(projects.protocol.osrs222.osrs222Internal)
    api(projects.protocol.osrs222.osrs222Common)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 222 Shared"
        description = "The shared module for revision 222 OldSchool RuneScape networking, " +
            "offering a set of shared classes that do not depend on a specific client."
    }
}
