plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jmh)
    alias(libs.plugins.allopen)
}

dependencies {
    implementation(platform(libs.netty.bom))
    implementation(libs.netty.buffer)
    implementation(libs.inline.logger)
    testImplementation(platform(libs.log4j.bom))
    testImplementation(libs.bundles.logging)
    implementation(projects.crypto)
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

sourceSets.create("benchmarks")

kotlin.sourceSets.getByName("benchmarks") {
    dependencies {
        implementation(libs.jmh.runtime)
        val mainSourceSet by sourceSets.main
        implementation(mainSourceSet.output)
        implementation(mainSourceSet.runtimeClasspath)
    }
}

benchmark {
    targets {
        register("benchmarks")
    }

    configurations {
        register("bitbuf") {
            include("net.rsprot.buffer.BitBufBenchmark")
        }

        register("bitbuftransfer") {
            include("net.rsprot.buffer.BitBufTransferBenchmark")
        }

        register("inlinebitbuf") {
            include("net.rsprot.buffer.InlineBitBufBenchmark")
        }
    }
}
