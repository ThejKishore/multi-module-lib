plugins{
    `java-library`
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    // Domain model annotations
    api("jakarta.persistence:jakarta.persistence-api:3.1.0")
    api("jakarta.validation:jakarta.validation-api:3.0.2")
    // JSON annotations for controlling serialization
    api("com.fasterxml.jackson.core:jackson-annotations:2.17.2")

    // Testing
    testImplementation("nl.jqno.equalsverifier:equalsverifier:4.2.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}