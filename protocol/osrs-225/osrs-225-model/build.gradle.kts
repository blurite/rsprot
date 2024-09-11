dependencies {
    api(platform(rootProject.libs.netty.bom))
    api(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.inline.logger)
    api(rootProject.libs.commons.pool2)
    api(projects.buffer)
    api(projects.compression)
    api(projects.crypto)
    api(projects.protocol)
    api(projects.protocol.osrs225.osrs225Internal)
    api(projects.protocol.osrs225.osrs225Common)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 225 Model"
        description = "The model module for revision 225 OldSchool RuneScape networking, " +
            "offering all the model classes to be used by the implementing server."
    }
}
