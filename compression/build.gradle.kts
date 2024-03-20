plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.netty.buffer)
    implementation(projects.buffer)
    implementation(libs.inline.logger)
}
