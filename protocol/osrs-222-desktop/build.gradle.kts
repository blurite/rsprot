plugins {
    alias(libs.plugins.jmh)
    alias(libs.plugins.allopen)
}

dependencies {
    implementation(platform(rootProject.libs.netty.bom))
    implementation(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.netty.transport)
    implementation(projects.buffer)
    implementation(projects.compression)
    implementation(projects.crypto)
    implementation(projects.protocol)
    implementation(projects.protocol.osrs222Model)
    implementation(projects.protocol.osrs222Internal)
    implementation(projects.protocol.osrs222Common)
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
        name = "RsProt OSRS 222 Desktop"
        description = "The desktop module for revision 222 OldSchool RuneScape networking, " +
            "offering encoders and decoders for all packets."
    }
}
