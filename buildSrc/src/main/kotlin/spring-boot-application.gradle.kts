import utility.*
import org.gradle.jvm.tasks.Jar
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins{
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.graalvm.buildtools.native")
    jacoco
    id("consolidatedJacoco")
    id("com.palantir.baseline-checkstyle")
    id("docker")
}

repositories {
    mavenCentral()
    mavenLocal()
}


dependencies {
    // Add each dependency from our arrays individually; Gradle cannot accept Array<String> directly
    Bundle.core.forEach { implementation(it) }
    runtimeOnly(Libs.h2)
    Bundle.testing.forEach { testImplementation(it) }
    testRuntimeOnly(Libs.h2)
}

dependencyManagement {
    imports {
        mavenBom(Libs.cloudDependencies+Versions.springCloud)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(Versions.JDK_VERSION))
    }
}

// Ensure we only produce the executable Spring Boot jar (disable plain jar)
tasks.named<Jar>("jar") {
    enabled = false
}
tasks.named<BootJar>("bootJar") {
    // Produce an artifact named: <project-name>-<version>-boot.jar
    archiveFileName.set("${project.name}-${project.version}-boot.jar")
    enabled = true
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}


// Sonar Check locally
// Aggregate SonarLint across all modules (root + subprojects)
tasks.register("sonarlintAll") {}

gradle.projectsEvaluated {
    tasks.named("sonarlintAll") {
        // After all projects are evaluated, wire dependencies to all SonarLint tasks, excluding this aggregate
        val allProjects = listOf(project) + subprojects
        val sonarlintTasks = allProjects.flatMap { p ->
            p.tasks.matching { it.name.startsWith("sonarlint") && it.name != "sonarlintAll" }.toList()
        }.toSet()
        dependsOn(sonarlintTasks)
    }
}

// Ensure root check runs SonarLint for all modules
tasks.named("check") {
    dependsOn("sonarlintAll")
}
