plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(platform(libs.netty.bom))
    implementation(libs.netty.buffer)
    implementation(libs.netty.transport)
    implementation(projects.buffer)
    implementation(projects.compression)
    implementation(projects.crypto)
    implementation(projects.protocol)
    implementation(projects.protocol.osrs221Model)
    implementation(projects.protocol.osrs221Internal)
    implementation(projects.protocol.osrs221Common)
}