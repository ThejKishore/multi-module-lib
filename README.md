# Multi‑Module Library Project

This repository demonstrates a Gradle multi‑module setup for building and publishing Java libraries. It is configured to work well with Spring’s dependency management while producing plain JARs (not executable Boot JARs), making it suitable for generic Spring‑friendly libraries.

- Java toolchain: 21
- Gradle wrapper: 9.1
- Group: com.tk.learn
- Version: 1.0.1
- Publishing: maven-publish → mavenLocal
- Spring plugins: org.springframework.boot and io.spring.dependency-management are applied to all subprojects; bootJar is disabled to keep modules as libraries.

## Project Structure

```
multi-module/
├─ build.gradle.kts                 # Centralized config for all modules
├─ settings.gradle.kts              # Declares included modules
├─ gradle/libs.versions.toml        # Version catalog for plugins/deps
├─ gradlew, gradlew.bat             # Gradle wrapper scripts
├─ gradle/wrapper/*                 # Wrapper metadata
├─ src/main/java/com/tk/learn/Main.java  # Placeholder (root project has no artifact)
├─ example-lib/
│  ├─ build.gradle.kts              # Inherits root config
│  └─ src/main/java/com/tk/learn/ExampleUtil.java
├─ example-b-lib/
│  ├─ build.gradle.kts              # Inherits root config
│  └─ src/main/java/com/tk/learn/ExampleBUtil.java
└─ example-c-lib/
   ├─ build.gradle.kts              # Inherits root config
   └─ src/main/java/com/tk/learn/ExampleCUtil.java
```

Module overview:
- example-lib: Basic greeting utility.
- example-b-lib: String utilities (uppercase shout).
- example-c-lib: Simple math utilities (add).

Each module produces an artifact whose artifactId equals the module directory name (project.name).

Coordinates for published artifacts (to mavenLocal):
- com.tk.learn:example-lib:1.0.1
- com.tk.learn:example-b-lib:1.0.1
- com.tk.learn:example-c-lib:1.0.1

## Prerequisites

- JDK 21 installed (the build uses the Gradle toolchain to provision JDK 21 if available).
- Internet access to mavenCentral for dependency resolution.
- No additional system installations are required when using the provided Gradle wrapper.

## How to Build

From the repository root:

- Compile and assemble all modules:
  - macOS/Linux: `./gradlew build`
  - Windows: `gradlew.bat build`

- Clean build outputs:
  - `./gradlew clean`

- Run tests (JUnit Platform is enabled by default):
  - `./gradlew test`

Artifacts are created under each module’s `build/libs` directory.

## How to Publish to mavenLocal

This project preconfigures the `maven-publish` plugin for all subprojects. You can publish all modules locally with a single task:

- Publish everything to your local Maven repository (~/.m2/repository):
  - `./gradlew publishAllToMavenLocal`

Alternatively, publish a single module:
- `./gradlew :example-lib:publishToMavenLocal`
- `./gradlew :example-b-lib:publishToMavenLocal`
- `./gradlew :example-c-lib:publishToMavenLocal`

After publishing, artifacts will be available with coordinates such as `com.tk.learn:example-lib:1.0.1`.

## Consuming These Libraries in Another Project

Add mavenLocal to your repositories and declare the dependency you need. For example, in another Gradle project using Kotlin DSL:

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("com.tk.learn:example-lib:1.0.1")
    // or
    implementation("com.tk.learn:example-b-lib:1.0.1")
    implementation("com.tk.learn:example-c-lib:1.0.1")
}
```

If your consumer is a Spring application, the Spring Dependency Management plugin (or the Spring Boot plugin) can be used to harmonize versions. This project already applies both plugins to modules, but it ships plain JARs by disabling `bootJar` and enabling the standard `jar` task.

## Key Build Conventions

- Centralized configuration in root `build.gradle.kts` applies to all subprojects:
  - Applies `java-library`, `maven-publish`, `io.spring.dependency-management`, and `org.springframework.boot`.
  - Sets group and version uniformly.
  - Configures Java toolchain 21, sourcesJar, and javadocJar.
  - Uses JUnit Platform for tests.
  - Disables Spring Boot’s `bootJar`; enables standard `jar` for library packaging.
  - Publishes the Java component as Maven publications (artifactId defaults to module name).

- Version catalog (`gradle/libs.versions.toml`) centralizes plugin and library versions:
  - Spring Boot: 3.3.4
  - Spring Dependency Management: 1.1.7
  - Lombok: 1.18.34
  - JUnit Platform Launcher: 1.10.2

## Common Tasks

- Build all: `./gradlew build`
- Assemble JARs: `./gradlew assemble`
- Run tests: `./gradlew test`
- Publish all to mavenLocal: `./gradlew publishAllToMavenLocal`
- Publish single module: `./gradlew :example-lib:publishToMavenLocal`
- Clean: `./gradlew clean`

## Adding a New Module

1. Create a new directory at the root (e.g., `example-d-lib`).
2. Add a minimal `build.gradle.kts` inside with a comment (the root script configures everything):
   ```
   // Inherits configuration from root (java-library, maven-publish, group/version)
   ```
3. Add your source files under `src/main/java/...`.
4. Include the module in `settings.gradle.kts`:
   ```kotlin
   include(":example-d-lib")
   ```
5. Build and publish as usual (`./gradlew build`, `./gradlew :example-d-lib:publishToMavenLocal`).

## Troubleshooting

- Plugin resolution issues: Ensure you have internet connectivity and that `pluginManagement` in `settings.gradle.kts` includes `gradlePluginPortal()` and `mavenCentral()` (already configured).
- Boot vs. plain JARs: If you accidentally enable `bootJar`, you’ll get executable JARs. For libraries, keep `bootJar` disabled and `jar` enabled (configured by default).
- Java version: If you see toolchain errors, confirm that Gradle can provision JDK 21 or install it locally.

## License

This sample is provided for learning purposes. Add your preferred license text here if distributing.


Add this if jacoco verification needs to capture both the issues

```kotlin
// Aggregate coverage verification across all subprojects
val aggregateJacocoCoverageVerification = tasks.register("aggregateJacocoCoverageVerification", JacocoCoverageVerification::class) {
    group = "Verification"
    description = "Verifies aggregated code coverage across all subprojects"

    // Ensure all tests ran and aggregate report is available
    dependsOn(subprojects.map { it.tasks.withType<Test>() })
    dependsOn(tasks.named("aggregateJacocoReport"))

    // Collect execution data from all subprojects (both .exec and .ec formats)
    val execFiles = subprojects.map { p ->
        p.fileTree(p.layout.buildDirectory).matching {
            include("jacoco/*.exec")
            include("jacoco/*.ec")
        }
    }
    executionData.from(execFiles)

    // Collect compiled class outputs and sources from all Java projects
    val classDirs = subprojects.mapNotNull { p ->
        p.extensions.findByName("sourceSets") as? SourceSetContainer
    }.map { container ->
        container.getByName(SourceSet.MAIN_SOURCE_SET_NAME).output
    }
    classDirectories.from(classDirs)

    val srcDirs = subprojects.mapNotNull { p ->
        p.extensions.findByName("sourceSets") as? SourceSetContainer
    }.flatMap { container ->
        container.getByName(SourceSet.MAIN_SOURCE_SET_NAME).allSource.srcDirs
    }
    sourceDirectories.from(srcDirs)

    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
        rule {
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

```

### Copilot prompt learnings

[co-pilot-learning-site](https://dev.to/anchildress1/github-copilot-everything-you-wanted-to-know-about-reusable-and-experimental-prompts-part-1-iff)

[co-pilot-reference-project](https://github.com/anchildress1/awesome-github-copilot/tree/main)

```shell
./gradlew --no-daemon clean :bootJar -x test
```