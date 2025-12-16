import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.java)
    alias(libs.plugins.shadow)
    alias(libs.plugins.runpaper)
}

repositories {
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
    maven { url = uri("https://repo.essentialsx.net/releases/") }
    maven { url = uri("https://repo.velocitypowered.com/snapshots/") }
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://repo.glaremasters.me/repository/towny/") }
    maven { url = uri("https://repo.maven.apache.org/maven2/") }
    flatDir { dirs("libs") }
}

dependencies {
    compileOnly(libs.com.zaxxer.hikaricp)
    compileOnly(libs.com.googlecode.json.simple.json.simple)
    compileOnly(libs.commons.lang)
    compileOnly(libs.net.md.v5.bungeecord.api)
    compileOnly(libs.net.dmulloy2.protocollib)
    compileOnly(libs.com.github.retrooper.packetevents.spigot)
    compileOnly(libs.io.papermc.paper.paper.api)
    compileOnly(libs.com.github.placeholderapi.placeholderapi)
    compileOnly(libs.com.github.milkbowl.vaultapi)  { isTransitive = false }
    compileOnly(libs.com.palmergames.bukkit.towny.towny)
    compileOnly(libs.net.essentialsx.essentialsxdiscord) { isTransitive = false }
    compileOnly(libs.com.velocitypowered.velocity.api)
    compileOnly(libs.net.kyori.adventure.api)
    compileOnly(libs.net.kyori.adventure.platform.bukkit)
    compileOnly(fileTree("libs"))
}

group = "mineverse.Aust1n46.chat"
version = "3.8.0"
description = "VentureChat"
java.sourceCompatibility = JavaVersion.VERSION_21

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.named<ProcessResources>("processResources") {
    filteringCharset = "UTF-8"

    val props = mapOf(
        "version" to project.version.toString(),
        "hikari" to libs.versions.com.zaxxer.hikaricp.get(),
        "jsonSimple" to libs.versions.com.googlecode.json.simple.json.simple.get(),
        "commonsLang" to libs.versions.commons.lang.get()
    )

    inputs.properties(props)

    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        exclude(
            "META-INF/versions/**", "module-info.class",
            "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA"
        )
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    build {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion("1.20.4")
        jvmArguments.add("-Dcom.mojang.eula.agree=true")
        jvmArguments.add("-Dnet.kyori.ansi.colorLevel=truecolor")
        jvmArguments.add("-Dfile.encoding=UTF8")
        systemProperty("terminal.jline", false)
        systemProperty("terminal.ansi", true)
    }
}

configurations.all {
    resolutionStrategy {
        force("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
        eachDependency {
            if (requested.group == "org.spigotmc" || requested.group == "org.bukkit") {
                useTarget("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
            }
        }
    }
}