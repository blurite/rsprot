dependencies {
    implementation(platform(rootProject.libs.netty.bom))
    implementation(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.netty.transport)
    implementation(rootProject.libs.commons.pool2)
    implementation(rootProject.libs.inline.logger)
    implementation(projects.buffer)
    implementation(projects.compression)
    implementation(projects.protocol)
    implementation(projects.protocol.osrs223.osrs223Common)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 223 Internal"
        description = "The internal module for revision 223 OldSchool RuneScape networking, " +
            "offering internal hidden implementations behind the library."
    }
}
