dependencies {
    implementation(platform(rootProject.libs.netty.bom))
    implementation(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.inline.logger)
    implementation(rootProject.libs.commons.pool2)
    implementation(projects.buffer)
    implementation(projects.compression)
    implementation(projects.crypto)
    implementation(projects.protocol)
    implementation(projects.protocol.osrs222Internal)
    implementation(projects.protocol.osrs222Common)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 222 Model"
        description = "The model module for revision 222 OldSchool RuneScape networking, " +
            "offering all the model classes to be used by the implementing server."
    }
}
