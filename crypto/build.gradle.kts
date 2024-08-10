dependencies {
    api(platform(rootProject.libs.netty.bom))
    api(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.jna.gmp)
    implementation(rootProject.libs.inline.logger)
}

mavenPublishing {
    pom {
        name = "RsProt Crypto"
        description = "Cryptography methods used by the RuneScape client protocol."
    }
}
