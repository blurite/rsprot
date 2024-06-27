dependencies {
    implementation(platform(rootProject.libs.netty.bom))
    api(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.netty.transport)
    implementation(rootProject.libs.netty.handler)
    implementation(rootProject.libs.netty.native.epoll)
    implementation(rootProject.libs.netty.native.kqueue)
    implementation(rootProject.libs.netty.incubator.iouring)
    implementation(rootProject.libs.inline.logger)
    api(projects.protocol)
    api(projects.compression)
    api(projects.crypto)
    api(projects.protocol.osrs223.osrs223Common)
    api(projects.protocol.osrs223.osrs223Model)
    implementation(projects.protocol.osrs223.osrs223Internal)
    implementation(projects.protocol.osrs223.osrs223Desktop)
    implementation(projects.protocol.osrs223.osrs223Shared)
    implementation(projects.buffer)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 223 API"
        description = "The API module for revision 223 OldSchool RuneScape networking, " +
            "offering an all-in-one implementation."
    }
}
