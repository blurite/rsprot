plugins {
    alias(libs.plugins.jmh)
    alias(libs.plugins.allopen)
}

dependencies {
    implementation(rootProject.libs.inline.logger)
    api(platform(rootProject.libs.netty.bom))
    api(rootProject.libs.netty.buffer)
    api(rootProject.libs.netty.transport)
    api(projects.buffer)
    api(projects.compression)
    api(projects.crypto)
    api(projects.protocol)
    api(projects.protocol.osrs230.osrs230Model)
    api(projects.protocol.osrs230.osrs230Internal)
    api(projects.protocol.osrs230.osrs230Common)
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

sourceSets.create("benchmarks")

kotlin.sourceSets.getByName("benchmarks") {
    dependencies {
        implementation(rootProject.libs.jmh.runtime)
        val mainSourceSet by sourceSets.main
        val testSourceSet by sourceSets.test
        val sourceSets = listOf(mainSourceSet, testSourceSet)
        for (sourceSet in sourceSets) {
            implementation(sourceSet.output)
            implementation(sourceSet.runtimeClasspath)
        }
    }
}

benchmark {
    targets {
        register("benchmarks")
    }

    configurations {
        register("PlayerInfoBenchmark") {
            include("net.rsprot.protocol.game.outgoing.info.PlayerInfoBenchmark")
        }
        register("NpcInfoBenchmark") {
            include("net.rsprot.protocol.game.outgoing.info.NpcInfoBenchmark")
        }
    }
}

mavenPublishing {
    pom {
        name = "RsProt OSRS 230 Desktop"
        description = "The desktop module for revision 230 OldSchool RuneScape networking, " +
            "offering encoders and decoders for all packets."
    }
}
