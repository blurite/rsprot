dependencies {
    api(platform(rootProject.libs.netty.bom))
    api(rootProject.libs.netty.buffer)
    api(projects.protocol)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 224 Common"
        description = "The common module for revision 224 OldSchool RuneScape networking, offering " +
            "common classes for all the modules to depend on."
    }
}