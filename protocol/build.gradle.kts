plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jmh)
}

dependencies {
    implementation(libs.netty.buffer)
    implementation(projects.buffer)
}
