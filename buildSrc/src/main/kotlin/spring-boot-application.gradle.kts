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


dependencies {
    implementation(libs.findLibrary("spring-boot-web").get())
    implementation(libs.findLibrary("spring-boot-data-jpa").get())
    implementation(libs.findLibrary("spring-boot-validation").get())
    runtimeOnly(libs.findLibrary("h2").get())
    testImplementation(libs.findLibrary("spring-boot-test").get())
    testRuntimeOnly(libs.findLibrary("junit-launcher").get())
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}


//Sonar Check locally
// Aggregate SonarLint across all modules (root + subprojects)
tasks.register("sonarlintAll") {}

gradle.projectsEvaluated {
    tasks.named("sonarlintAll") {
        // After all projects are evaluated, wire dependencies to existing SonarLint tasks
        val allProjects = listOf(project) + subprojects
        val sonarlintTasks = allProjects.mapNotNull { p -> p.tasks.findByName("sonarlint") }
        dependsOn(sonarlintTasks)
    }
}

// Ensure root check runs SonarLint for all modules
tasks.named("check") {
    dependsOn("sonarlintAll")
}
