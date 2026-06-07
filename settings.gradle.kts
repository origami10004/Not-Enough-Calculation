pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        exclusiveContent {
            forRepository {
                maven {
                    name = "GTNH Maven"
                    url = uri("https://nexus.gtnewhorizons.com/repository/public/")
                }
            }
            filter {
                includeGroupAndSubgroups("com.gtnewhorizons")
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}