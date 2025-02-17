dependencies {
    api(platform(rootProject.libs.netty.bom))
    api(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.inline.logger)
    api(rootProject.libs.netty.transport)
    api(projects.buffer)
    api(projects.compression)
    api(projects.crypto)
    api(projects.protocol)
    api(projects.protocol.osrs229.osrs229Model)
    api(projects.protocol.osrs229.osrs229Internal)
    api(projects.protocol.osrs229.osrs229Common)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 229 Shared"
        description = "The shared module for revision 229 OldSchool RuneScape networking, " +
            "offering a set of shared classes that do not depend on a specific client."
    }
}
