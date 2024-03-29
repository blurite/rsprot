plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.netty.buffer)
    implementation(libs.inline.logger)
    implementation(projects.buffer)
    implementation(projects.compression)
    implementation(projects.protocol)
    implementation(projects.protocol.osrs221Internal)
    implementation(projects.protocol.osrs221Shared)
}
