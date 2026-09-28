pluginManagement {
    includeBuild("build-logic")
}

plugins {
    id("base.settings")
}

dependencyResolutionManagement {
    repositories {
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "chunk-trimming"
