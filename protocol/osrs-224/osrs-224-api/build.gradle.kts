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
    api(projects.protocol.osrs224.osrs224Common)
    api(projects.protocol.osrs224.osrs224Model)
    implementation(projects.protocol.osrs224.osrs224Internal)
    implementation(projects.protocol.osrs224.osrs224Desktop)
    implementation(projects.protocol.osrs224.osrs224Shared)
    implementation(projects.buffer)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 224 API"
        description = "The API module for revision 224 OldSchool RuneScape networking, " +
            "offering an all-in-one implementation."
    }
}
