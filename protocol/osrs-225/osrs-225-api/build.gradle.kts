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
    api(projects.protocol.osrs225.osrs225Common)
    api(projects.protocol.osrs225.osrs225Model)
    implementation(projects.protocol.osrs225.osrs225Internal)
    implementation(projects.protocol.osrs225.osrs225Desktop)
    implementation(projects.protocol.osrs225.osrs225Shared)
    implementation(projects.buffer)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 225 API"
        description = "The API module for revision 225 OldSchool RuneScape networking, " +
            "offering an all-in-one implementation."
    }
}
