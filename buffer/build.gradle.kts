plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jmh)
    alias(libs.plugins.allopen)
}

dependencies {
    implementation(libs.netty.buffer)
    implementation(libs.inline.logger)
    testImplementation(libs.bundles.logging)
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

        register("inlinebitbuf") {
            include("net.rsprot.buffer.InlineBitBufBenchmark")
        }
    }
}
