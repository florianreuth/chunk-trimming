import de.florianreuth.baseproject.setupProject

plugins {
    id("java")
    id("de.florianreuth.baseproject")
}

setupProject()

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.74-stable")
}

tasks {
    jar {
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }
    }

    processResources {
        val projectVersion = project.version
        val projectDescription = project.description
        filesMatching("plugin.yml") {
            expand(mapOf("version" to projectVersion, "description" to projectDescription))
        }
    }
}
