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
    api(projects.protocol.osrs222Common)
    api(projects.protocol.osrs222Model)
    implementation(projects.protocol.osrs222Internal)
    implementation(projects.protocol.osrs222Desktop)
    implementation(projects.protocol.osrs222Shared)
    implementation(projects.buffer)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 222 API"
        description = "The API module for revision 222 OldSchool RuneScape networking, " +
            "offering an all-in-one implementation."
    }
}
