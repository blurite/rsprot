dependencies {
    api(platform(rootProject.libs.netty.bom))

    api(rootProject.libs.netty.handler)
    api(rootProject.libs.netty.codec.haproxy)

    implementation(rootProject.libs.inline.logger)
}

mavenPublishing {
    pom {
        name = "RsProt HAProxy"
        description = "Support for the HAProxy protocol, to resolve \"real\" IP addresses behind a proxy"
    }
}
