plugins {
    id("java")
}

group = "me.pixlent"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("net.minestom:minestom-snapshots:1_21_4-b7c38fd36b")
    implementation("de.articdive:jnoise-pipeline:4.1.0")
    implementation("com.github.CoolLoong:FastNoise2Bindings-Java:0.0.1")
    implementation("org.slf4j:slf4j-simple:2.0.16")

    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
}