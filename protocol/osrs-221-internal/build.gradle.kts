plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(platform(libs.netty.bom))
    implementation(libs.netty.buffer)
    implementation(libs.netty.transport)
    implementation(libs.commons.pool2)
    implementation(libs.inline.logger)
    implementation(projects.buffer)
    implementation(projects.compression)
    implementation(projects.protocol)
    implementation(projects.protocol.osrs221Shared)
}
