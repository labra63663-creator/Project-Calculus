plugins {
    id("net.neoforged.moddev")
}

val modId = "projcalc"
val modName = "Project Calculus"

base.archivesName = modId
version = "0.0.1-dev"
group = "br.zuki.supercoole"

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

repositories {
    mavenCentral()
}

dependencies {
    // APIs de tempo de compilação (runtime fornecido pelo jogo/instância)
    compileOnly(files("libs/scc-1.4.6.jar"))
    compileOnly(files("libs/ProjectE-1.21.1-PE1.1.0.jar"))
    compileOnly(files("libs/jei-1.21.1-neoforge-19.64.0.455.jar"))
    compileOnly(files("libs/Jade-1.21.1-NeoForge-15.10.6.jar"))
}

neoForge {
    version = "21.1.238"

    parchment {
        minecraftVersion = "1.21.1"
        mappingsVersion = "2024.11.17"
    }

    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        configureEach {
            gameDirectory = file("run")
        }
        create("client") {
            client()
        }
        create("server") {
            server()
            gameDirectory = file("run/server")
        }
    }
}

tasks {
    jar {
        from(rootProject.file("LICENSE")) { }
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    processResources {
        val props = mapOf("version" to version, "modId" to modId, "modName" to modName)
        inputs.properties(props)
        filesMatching("META-INF/neoforge.mods.toml") {
            expand(props)
        }
    }
}