dependencies {
    implementation(platform(rootProject.libs.netty.bom))
    implementation(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.netty.transport)
    implementation(projects.buffer)
    implementation(projects.compression)
    implementation(projects.crypto)
    implementation(projects.protocol)
    implementation(projects.protocol.osrs222Model)
    implementation(projects.protocol.osrs222Internal)
    implementation(projects.protocol.osrs222Common)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 222 Shared"
        description = "The shared module for revision 222 OldSchool RuneScape networking, " +
            "offering a set of shared classes that do not depend on a specific client."
    }
}
