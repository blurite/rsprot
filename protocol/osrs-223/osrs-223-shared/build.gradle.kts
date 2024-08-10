dependencies {
    api(platform(rootProject.libs.netty.bom))
    api(rootProject.libs.netty.buffer)
    api(rootProject.libs.netty.transport)
    api(projects.buffer)
    api(projects.compression)
    api(projects.crypto)
    api(projects.protocol)
    api(projects.protocol.osrs223.osrs223Model)
    api(projects.protocol.osrs223.osrs223Internal)
    api(projects.protocol.osrs223.osrs223Common)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 223 Shared"
        description = "The shared module for revision 223 OldSchool RuneScape networking, " +
            "offering a set of shared classes that do not depend on a specific client."
    }
}
