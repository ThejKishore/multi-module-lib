plugins {
    id("name.remal.sonarlint")
}

// ─── SonarLint: wire into check lifecycle ────────────────────────────────────
pluginManager.withPlugin("base") {
    val wireSonarLint: () -> Unit = {
        tasks.named("check").configure {
            tasks.matching { it.name.startsWith("sonarlint") }.forEach { dependsOn(it) }
        }
    }
    if (pluginManager.hasPlugin("name.remal.sonarlint")) {
        wireSonarLint()
    } else {
        pluginManager.withPlugin("name.remal.sonarlint") { wireSonarLint() }
    }
}

// ─── SonarQube: only configure at the root/aggregate project level ────────────
// The consolidatedJacoco plugin (io.freefair.aggregate-jacoco-report) is only applied
// to the root project, so we gate all SonarQube configuration behind its presence.
pluginManager.withPlugin("io.freefair.aggregate-jacoco-report") {
    apply(plugin = "org.sonarqube")

    // After org.sonarqube is applied, configure it via the SonarQubeExtension type
    extensions.configure<org.sonarqube.gradle.SonarExtension>("sonarqube") {
        properties {
            // SonarQube server URL — override via -Dsonar.host.url=... or gradle.properties
            property("sonar.host.url", System.getProperty("sonar.host.url", "http://localhost:9000"))

            // Authentication token — set via -Dsonar.token=... or SONAR_TOKEN env variable
            val sonarToken = System.getProperty("sonar.token")
                ?: System.getenv("SONAR_TOKEN")
                ?: ""
            property("sonar.token", sonarToken)

            // Point SonarQube at the aggregate Jacoco XML report produced by consolidatedJacoco
            property(
                "sonar.coverage.jacoco.xmlReportPaths",
                "${layout.buildDirectory.get()}/reports/jacoco/aggregate/jacoco.xml"
            )

            // Aggregate test-results directories from all subprojects
            property(
                "sonar.junit.reportPaths",
                subprojects.joinToString(",") { sub ->
                    "${sub.layout.buildDirectory.get()}/test-results/test"
                }
            )

            // Aggregate source directories from all subprojects
            property(
                "sonar.sources",
                subprojects.flatMap { sub ->
                    sub.extensions.findByType(SourceSetContainer::class.java)
                        ?.getByName("main")?.allSource?.srcDirs
                        ?.filter { it.exists() }
                        ?: emptySet()
                }.joinToString(",")
            )
        }
    }

    // Ensure SonarQube analysis runs after the aggregate Jacoco report is generated
    tasks.named("sonar") {
        dependsOn(tasks.named("aggregateJacocoReport"))
    }
}
