plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(platform(libs.netty.bom))
    implementation(libs.netty.buffer)
    implementation(libs.netty.transport)
    implementation(libs.netty.handler)
    implementation(libs.netty.native.epoll)
    implementation(libs.netty.native.kqueue)
    implementation(libs.netty.incubator.iouring)
    implementation(projects.buffer)
    implementation(projects.compression)
    implementation(projects.crypto)
    implementation(projects.protocol)
    implementation(projects.protocol.osrs221Model)
    implementation(projects.protocol.osrs221Internal)
    implementation(projects.protocol.osrs221Common)
    implementation(projects.protocol.osrs221Desktop)
    implementation(projects.protocol.osrs221Shared)
}
