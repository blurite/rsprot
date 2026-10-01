dependencies {
    api(platform(rootProject.libs.netty.bom))
    api(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.inline.logger)
    api(rootProject.libs.netty.transport)
    api(projects.buffer)
    api(projects.compression)
    api(projects.crypto)
    api(projects.protocol)
    api(projects.protocol.osrs241.osrs241Model)
    api(projects.protocol.osrs241.osrs241Internal)
    api(projects.protocol.osrs241.osrs241Common)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 241 Shared"
        description = "The shared module for revision 241 OldSchool RuneScape networking, " +
            "offering a set of shared classes that do not depend on a specific client."
    }
}
