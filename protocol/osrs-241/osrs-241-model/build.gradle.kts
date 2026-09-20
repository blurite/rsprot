dependencies {
    api(platform(rootProject.libs.netty.bom))
    api(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.inline.logger)
    api(rootProject.libs.commons.pool2)
    api(projects.buffer)
    api(projects.compression)
    api(projects.crypto)
    api(projects.protocol)
    api(projects.protocol.osrs241.osrs241Internal)
    api(projects.protocol.osrs241.osrs241Common)
    implementation(libs.fastutil)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 241 Model"
        description = "The model module for revision 241 OldSchool RuneScape networking, " +
            "offering all the model classes to be used by the implementing server."
    }
}
