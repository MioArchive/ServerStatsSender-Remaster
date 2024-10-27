plugins {
    kotlin("jvm") version "2.1.0-Beta2"
    id("com.github.johnrengelman.shadow") version "8.1.1"

}

group = "com.lent"
version = "1.0.2"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    compileOnly("net.dv8tion:JDA:5.1.2") {
    compileOnly("me.lucko:spark-api:0.1-SNAPSHOT")
    }
}

kotlin {
    jvmToolchain(17)
}

tasks {
    build {
        dependsOn("shadowJar")
    }

    shadowJar {
        minimize() // Use minimization
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
