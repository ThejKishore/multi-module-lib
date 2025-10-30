plugins{
    `java-library`
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

repositories {
    mavenCentral()
    mavenLocal()
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly(libs.findLibrary("junit-launcher").get())
    testRuntimeOnly("com.h2database:h2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}


//    // Disable Spring Boot executable jar for library modules; keep plain jar
tasks.named("bootJar") { enabled = false }
tasks.named("jar") { enabled = true }