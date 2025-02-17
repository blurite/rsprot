dependencies {
    api(platform(rootProject.libs.netty.bom))
    api(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.inline.logger)
    api(rootProject.libs.commons.pool2)
    api(projects.buffer)
    api(projects.compression)
    api(projects.crypto)
    api(projects.protocol)
    api(projects.protocol.osrs229.osrs229Internal)
    api(projects.protocol.osrs229.osrs229Common)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 229 Model"
        description = "The model module for revision 229 OldSchool RuneScape networking, " +
            "offering all the model classes to be used by the implementing server."
    }
}
