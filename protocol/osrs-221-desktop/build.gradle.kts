plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.netty.buffer)
    implementation(projects.buffer)
    implementation(projects.protocol)
    implementation(projects.protocol.osrs221Model)
}
