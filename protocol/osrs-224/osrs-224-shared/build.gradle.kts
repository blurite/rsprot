dependencies {
    api(platform(rootProject.libs.netty.bom))
    api(rootProject.libs.netty.buffer)
    api(rootProject.libs.netty.transport)
    api(projects.buffer)
    api(projects.compression)
    api(projects.crypto)
    api(projects.protocol)
    api(projects.protocol.osrs224.osrs224Model)
    api(projects.protocol.osrs224.osrs224Internal)
    api(projects.protocol.osrs224.osrs224Common)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 224 Shared"
        description = "The shared module for revision 224 OldSchool RuneScape networking, " +
            "offering a set of shared classes that do not depend on a specific client."
    }
}
