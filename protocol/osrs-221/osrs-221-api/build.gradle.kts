dependencies {
    implementation(platform(rootProject.libs.netty.bom))
    api(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.netty.transport)
    implementation(rootProject.libs.netty.handler)
    implementation(rootProject.libs.netty.native.epoll)
    implementation(rootProject.libs.netty.native.kqueue)
    implementation(rootProject.libs.netty.incubator.iouring)
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
        implementation(variantOf(rootProject.libs.netty.incubator.iouring) { classifier(classifier) })
    }
    implementation(rootProject.libs.inline.logger)
    api(projects.protocol)
    api(projects.compression)
    api(projects.crypto)
    api(projects.protocol.osrs221.osrs221Common)
    api(projects.protocol.osrs221.osrs221Model)
    implementation(projects.protocol.osrs221.osrs221Internal)
    implementation(projects.protocol.osrs221.osrs221Desktop)
    implementation(projects.protocol.osrs221.osrs221Shared)
    implementation(projects.buffer)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 221 API"
        description = "The API module for revision 221 OldSchool RuneScape networking, " +
            "offering an all-in-one implementation."
    }
}
