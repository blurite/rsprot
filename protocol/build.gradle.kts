plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(platform(libs.netty.bom))
    implementation(libs.netty.buffer)
    implementation(libs.netty.transport)
    implementation(projects.buffer)
    implementation(projects.crypto)
    implementation(projects.compression)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
