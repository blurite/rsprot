dependencies {
    api(platform(rootProject.libs.netty.bom))
    api(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.inline.logger)
    api(rootProject.libs.netty.transport)
    api(projects.buffer)
    api(projects.compression)
    api(projects.crypto)
    api(projects.protocol)
    api(projects.protocol.osrs233.osrs233Model)
    api(projects.protocol.osrs233.osrs233Internal)
    api(projects.protocol.osrs233.osrs233Common)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 233 Shared"
        description = "The shared module for revision 233 OldSchool RuneScape networking, " +
            "offering a set of shared classes that do not depend on a specific client."
    }
}
