import utility.Libs

plugins{
    id("spring-boot-application")
    id("docker")
    id("versions")
}

// Project version is controlled by gradle.properties 'version' and versioning tasks from the 'versions' plugin
version = (findProperty("version") as String?) ?: "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":example-lib"))
    implementation(project(":example-c-lib"))
    implementation("am.ik.yavi:yavi:0.16.0")
}
