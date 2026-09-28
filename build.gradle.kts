plugins {
    id("java")
    id("base.java")
}

dependencies {
    compileOnly(libs.paper.api)
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
