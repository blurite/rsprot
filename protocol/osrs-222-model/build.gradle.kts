dependencies {
    implementation(platform(rootProject.libs.netty.bom))
    implementation(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.inline.logger)
    implementation(rootProject.libs.commons.pool2)
    implementation(projects.buffer)
    implementation(projects.compression)
    implementation(projects.crypto)
    implementation(projects.protocol)
    implementation(projects.protocol.osrs221Internal)
    implementation(projects.protocol.osrs221Common)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 221 Model"
        description = "The model module for revision 221 OldSchool RuneScape networking, " +
            "offering all the model classes to be used by the implementing server."
    }
}
