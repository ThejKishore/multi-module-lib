* [gradle_plugin_refernce](https://github.com/ThejKishore/cheatsheet/blob/cd1ea4dbc649044da45254a3b75268eb23efc372/gradle_plugins.md?plain=1#L118)

* [docker_gradle_refernce](https://github.com/ThejKishore/cheatsheet/blob/cd1ea4dbc649044da45254a3b75268eb23efc372/docker.gradle#L12)


*[git_details_gradle_plugin](https://github.com/palantir/gradle-git-version)

*[]()


implementation("org.springframework.boot:spring-boot-micrometer-tracing")
implementation("org.springframework.boot:spring-boot-starter-actuator")
implementation("org.springframework.boot:spring-boot-starter-jdbc")
implementation("org.springframework.boot:spring-boot-starter-restclient")
implementation("org.springframework.boot:spring-boot-starter-security-oauth2-client")
implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
implementation("org.springframework.boot:spring-boot-starter-webmvc")
implementation("io.micrometer:micrometer-tracing-bridge-brave")
implementation("org.springframework.cloud:spring-cloud-starter")
implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
compileOnly("org.projectlombok:lombok")
runtimeOnly("com.h2database:h2")
annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
annotationProcessor("org.projectlombok:lombok")
testImplementation("org.springframework.boot:spring-boot-micrometer-tracing-test")
testImplementation("org.springframework.boot:spring-boot-starter-jdbc-test")
testImplementation("org.springframework.boot:spring-boot-starter-restclient-test")
testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-client-test")
testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
testRuntimeOnly("org.junit.platform:junit-platform-launcher")