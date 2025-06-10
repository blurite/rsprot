dependencies {
    api(platform(rootProject.libs.netty.bom))
    api(rootProject.libs.netty.buffer)
    api(projects.protocol)
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 231 Common"
        description = "The common module for revision 231 OldSchool RuneScape networking, offering " +
            "common classes for all the modules to depend on."
    }
}
