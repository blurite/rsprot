plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jmh)
    alias(libs.plugins.allopen)
}

dependencies {
    implementation(libs.netty.buffer)
    implementation(libs.inline.logger)
    implementation(projects.buffer)
    implementation(projects.protocol)
    implementation(projects.protocol.osrs221Internal)
    implementation(projects.protocol.osrs221Shared)
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

sourceSets.create("benchmarks")

kotlin.sourceSets.getByName("benchmarks") {
    dependencies {
        implementation(libs.jmh.runtime)
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
    }
}
