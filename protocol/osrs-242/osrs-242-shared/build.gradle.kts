dependencies {
    api(platform(rootProject.libs.netty.bom))
    api(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.inline.logger)
    api(rootProject.libs.netty.transport)
    api(projects.buffer)
    api(projects.compression)
    api(projects.crypto)
    api(projects.protocol)
    api(projects.protocol.osrs242.osrs242Model)
    api(projects.protocol.osrs242.osrs242Internal)
    api(projects.protocol.osrs242.osrs242Common)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 242 Shared"
        description = "The shared module for revision 242 OldSchool RuneScape networking, " +
            "offering a set of shared classes that do not depend on a specific client."
    }
}
