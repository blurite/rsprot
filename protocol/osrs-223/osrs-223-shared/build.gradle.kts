dependencies {
    implementation(platform(rootProject.libs.netty.bom))
    implementation(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.netty.transport)
    implementation(projects.buffer)
    implementation(projects.compression)
    implementation(projects.crypto)
    implementation(projects.protocol)
    implementation(projects.protocol.osrs223.osrs223Model)
    implementation(projects.protocol.osrs223.osrs223Internal)
    implementation(projects.protocol.osrs223.osrs223Common)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 223 Shared"
        description = "The shared module for revision 223 OldSchool RuneScape networking, " +
            "offering a set of shared classes that do not depend on a specific client."
    }
}
