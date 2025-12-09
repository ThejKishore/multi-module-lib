package utility


object TestLibs {

    //  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    //  testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    //  testImplementation("org.springframework.boot:spring-boot-starter-jdbc-test")
    //  testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    //  testRuntimeOnly("org.junit.platform:junit-platform-launcher")


    //Taken care by spring dependency management
    const val bootMicrometerTest = "org.springframework.boot:spring-boot-micrometer-tracing-test"
    const val bootJdbcTest = "org.springframework.boot:spring-boot-starter-jdbc-test"
    const val bootRestClientTest = "org.springframework.boot:spring-boot-starter-restclient-test"
    const val bootOAuth2ClientTest = "org.springframework.boot:spring-boot-starter-security-oauth2-client-test"
    const val bootOauth2ResourceServerTest = "org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test"
    const val bootWebmvcTest = "org.springframework.boot:spring-boot-starter-webmvc-test"
    const val bootTestAutoConfigure = "org.springframework.boot:spring-boot-test-autoconfigure"


    const val junitPlatformLauncher = "org.junit.platform:junit-platform-launcher:"+Versions.junitJupiterPlatformVersion
    const val equalsVerifier = "nl.jqno.equalsverifier:equalsverifier:" + Versions.equalsVerifer
    const val junitJupiterApi = "org.junit.jupiter:junit-jupiter-api:"+Versions.junitJupiterVersion
    const val junitJupiterEngine = "org.junit.jupiter:junit-jupiter-engine:"+Versions.junitJupiterVersion

}