plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(platform(libs.netty.bom))
    implementation(libs.netty.buffer)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
