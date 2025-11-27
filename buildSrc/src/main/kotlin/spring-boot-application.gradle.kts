plugins{
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    jacoco
    id("consolidatedJacoco")
    id("com.palantir.baseline-checkstyle")
    id("docker")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

repositories {
    mavenCentral()
    mavenLocal()
}

extra["springCloudVersion"] = "2025.1.0"

dependencies {
    implementation(libs.findBundle("core-spring-boot").get())
    runtimeOnly(libs.findLibrary("h2").get())
    testImplementation(libs.findBundle("core-testing").get())
    testRuntimeOnly(libs.findLibrary("h2").get())
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
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
