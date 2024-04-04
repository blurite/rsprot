plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.netty.buffer)
    implementation(libs.netty.transport)
    implementation(projects.buffer)
    implementation(projects.crypto)
    implementation(projects.compression)
}
