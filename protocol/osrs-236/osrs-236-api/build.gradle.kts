dependencies {
    implementation(platform(rootProject.libs.netty.bom))
    api(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.netty.transport)
    implementation(rootProject.libs.netty.handler)
    implementation(rootProject.libs.netty.native.epoll)
    implementation(rootProject.libs.netty.native.kqueue)
    implementation(rootProject.libs.netty.iouring)
    implementation(rootProject.libs.netty.native.macos.dns.resolver)
    val epollClassifiers = listOf("linux-aarch_64", "linux-x86_64", "linux-riscv64")
    val kqueueClassifiers = listOf("osx-x86_64")
    val iouringClassifiers = listOf("linux-aarch_64", "linux-x86_64")
    for (classifier in epollClassifiers) {
        implementation(variantOf(rootProject.libs.netty.native.epoll) { classifier(classifier) })
    }
    for (classifier in kqueueClassifiers) {
        implementation(variantOf(rootProject.libs.netty.native.kqueue) { classifier(classifier) })
    }
    for (classifier in iouringClassifiers) {
        implementation(variantOf(rootProject.libs.netty.iouring) { classifier(classifier) })
    }
    implementation(rootProject.libs.inline.logger)
    api(projects.protocol)
    api(projects.compression)
    api(projects.crypto)
    api(projects.protocol.osrs236.osrs236Common)
    api(projects.protocol.osrs236.osrs236Model)
    implementation(projects.protocol.osrs236.osrs236Internal)
    implementation(projects.protocol.osrs236.osrs236Desktop)
    implementation(projects.protocol.osrs236.osrs236Shared)
    implementation(projects.buffer)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 236 API"
        description = "The API module for revision 236 OldSchool RuneScape networking, " +
            "offering an all-in-one implementation."
    }
}
