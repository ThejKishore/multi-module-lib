import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency) apply false
}

// Centralized configuration for all subprojects (modules)
subprojects {
    apply(plugin = "java-library")
    plugins.apply("maven-publish")
    plugins.apply("io.spring.dependency-management")
    plugins.apply("org.springframework.boot")

    group = "com.tk.learn"
    version = "1.0.1"

    repositories {
        mavenCentral()
    }

    // Configure Java toolchain and additional artifacts via JavaPluginExtension
    extensions.configure(org.gradle.api.plugins.JavaPluginExtension::class.java) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        withSourcesJar()
        withJavadocJar()
    }

    // Ensure JUnit Platform for tests
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    // Disable Spring Boot executable jar for library modules; keep plain jar
    tasks.named("bootJar") { enabled = false }
    tasks.named("jar") { enabled = true }

    // Configure publishing to use the Java component; artifactId defaults to project.name (module name)
    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
            }
        }
    }
}

// Convenience task to publish all modules to mavenLocal in one command
tasks.register("publishAllToMavenLocal") {
    dependsOn(subprojects.map { it.tasks.named("publishToMavenLocal") })
}