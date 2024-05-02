plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(platform(libs.netty.bom))
    api(libs.netty.buffer)
    implementation(libs.netty.transport)
    implementation(libs.netty.handler)
    implementation(libs.netty.native.epoll)
    implementation(libs.netty.native.kqueue)
    implementation(libs.netty.incubator.iouring)
    implementation(libs.inline.logger)
    api(projects.protocol)
    api(projects.compression)
    api(projects.crypto)
    api(projects.protocol.osrs221Common)
    api(projects.protocol.osrs221Model)
    implementation(projects.protocol.osrs221Internal)
    implementation(projects.protocol.osrs221Desktop)
    implementation(projects.protocol.osrs221Shared)
    implementation(projects.buffer)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
