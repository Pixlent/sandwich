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
    implementation("net.minestom:minestom-snapshots:${property("minestomVersion")}")
    implementation("de.articdive:jnoise-pipeline:${property("jnoisePipelineVersion")}")
    implementation("com.github.CoolLoong:FastNoise2Bindings-Java:${property("fastNoise2BindingsJavaVersion")}")
    implementation("org.slf4j:slf4j-simple:${property("slf4jSimpleVersion")}")

    compileOnly("org.projectlombok:lombok:${property("lombokVersion")}")
    annotationProcessor("org.projectlombok:lombok:${property("lombokVersion")}")
}