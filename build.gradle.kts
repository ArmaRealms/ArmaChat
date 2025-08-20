import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.java)
    alias(libs.plugins.shadow)
    alias(libs.plugins.runpaper)
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://jitpack.io")
    }

    maven {
        url = uri("https://repo.dmulloy2.net/repository/public/")
    }

    maven {
        url = uri("https://repo.essentialsx.net/releases/")
    }

    maven {
        url = uri("https://repo.velocitypowered.com/snapshots/")
    }

    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.glaremasters.me/repository/towny/")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
    flatDir {
        dirs("libs")
    }
}

dependencies {
    implementation(libs.com.zaxxer.hikaricp)
    implementation(libs.net.md.v5.bungeecord.api)
    implementation(libs.com.googlecode.json.simple.json.simple)
    implementation(libs.commons.lang)
    testImplementation(libs.io.papermc.paper.paper.api)
    testImplementation(libs.junit.junit)
    testImplementation(libs.org.mockito.mockito.core)
    testImplementation(libs.org.mockito.mockito.inline)
    testImplementation(libs.org.mockbukkit.mockbukkit)
    testImplementation(libs.net.dmulloy2.protocollib)
    testImplementation(libs.com.github.milkbowl.vaultapi)
    compileOnly(libs.net.dmulloy2.protocollib)
    compileOnly(libs.io.papermc.paper.paper.api)
    compileOnly(libs.com.github.placeholderapi.placeholderapi)
    compileOnly(libs.com.github.milkbowl.vaultapi)
    compileOnly(libs.com.palmergames.bukkit.towny.towny)
    compileOnly(libs.net.essentialsx.essentialsxdiscord)
    compileOnly(libs.com.velocitypowered.velocity.api)
    compileOnly(libs.net.md.v5.bungeecord.api)
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
    // garante reprodutibilidade e evita capturas do script
    filteringCharset = "UTF-8"

    // crie o mapa aqui dentro, para não capturar o script
    val props = mapOf("version" to project.version.toString())

    // declare como inputs para o cache
    inputs.properties(props)

    // aplique só no plugin.yml do main resources
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks {
    withType<ShadowJar> {
        relocate("com.zaxxer.hikari", "shaded.com.zaxxer.hikari")
        relocate("net.md_5.bungee.config", "shaded.net.md_5.bungee.config")
        archiveClassifier.set("")
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
