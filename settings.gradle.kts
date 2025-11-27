pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "multi-module"

include(":example-lib", ":example-c-lib")
