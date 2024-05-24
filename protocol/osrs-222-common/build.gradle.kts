dependencies {
    implementation(platform(rootProject.libs.netty.bom))
    implementation(rootProject.libs.netty.buffer)
    implementation(projects.protocol)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 221 Common"
        description = "The common module for revision 221 OldSchool RuneScape networking, offering " +
            "common classes for all the modules to depend on."
    }
}
