dependencies {
    api(platform(rootProject.libs.netty.bom))
    api(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.inline.logger)
    api(rootProject.libs.netty.transport)
    api(projects.buffer)
    api(projects.compression)
    api(projects.crypto)
    api(projects.protocol)
    api(projects.protocol.osrs225.osrs225Model)
    api(projects.protocol.osrs225.osrs225Internal)
    api(projects.protocol.osrs225.osrs225Common)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 225 Shared"
        description = "The shared module for revision 225 OldSchool RuneScape networking, " +
            "offering a set of shared classes that do not depend on a specific client."
    }
}
