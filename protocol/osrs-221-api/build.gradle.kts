plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(platform(libs.netty.bom))
    api(libs.netty.buffer)
    api(projects.protocol)
    api(projects.compression)
    api(projects.crypto)
    api(projects.protocol.osrs221Common)
    api(projects.protocol.osrs221Model)
    implementation(projects.protocol.osrs221Internal)
    implementation(projects.protocol.osrs221Desktop)
}
