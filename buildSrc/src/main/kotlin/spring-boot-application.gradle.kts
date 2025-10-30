plugins{
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
repositories {
    mavenCentral()
    mavenLocal()
}




dependencies {
    implementation(project(":example-lib"))
    implementation(project(":example-b-lib"))
    implementation(project(":example-c-lib"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    runtimeOnly("com.h2database:h2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
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
