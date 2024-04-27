plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(platform(libs.netty.bom))
    implementation(libs.netty.buffer)
    implementation(projects.buffer)
    implementation(libs.inline.logger)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
