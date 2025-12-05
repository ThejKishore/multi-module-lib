package utility

object Libs {

    const val bootStarterActuator = "org.springframework.boot:spring-boot-starter-actuator"
    const val bootStarterWeb = "org.springframework.boot:spring-boot-starter-webmvc"
    const val bootStarterJdbc = "org.springframework.boot:spring-boot-starter-jdbc"
    const val bootStarterRestClient = "org.springframework.boot:spring-boot-starter-restclient"

    const val bootStarterJpa = "org.springframework.boot:spring-boot-starter-data-jpa"
    const val bootStarterValidation = "org.springframework.boot:spring-boot-starter-validation"
    const val bootStarterCache="org.springframework.boot:spring-boot-starter-cache"
    const val bootStarterSecurity = "org.springframework.boot:spring-boot-starter-security"
    const val oAuth2Client = "org.springframework.boot:spring-boot-starter-security-oauth2-client"
    const val oAuth2ResourceServer = "org.springframework.boot:spring-boot-starter-security-oauth2-resource-server"
    const val bootStarterConfigurationProcessor = "org.springframework.boot:spring-boot-configuration-processor"

    const val lombok = "org.projectlombok:lombok:" + Versions.lombok

    const val h2 = "com.h2database:h2"

    const val micrometerTracing ="org.springframework.boot:spring-boot-micrometer-tracing"
    const val micrometerBridgeBrave = "io.micrometer:micrometer-tracing-bridge-brave"
    const val bootStarterZipkin="org.springframework.boot:spring-boot-starter-zipkin"
    const val bootStarterTelemetry= "org.springframework.boot:spring-boot-starter-opentelemetry"
    const val prometheus = "io.micrometer:micrometer-registry-prometheus"




    const val cloudStarter = "org.springframework.cloud:spring-cloud-starter"
    const val cloudResilience4j = "org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j"
    const val cloudConfigClient = "org.springframework.cloud:spring-cloud-config-client"


    const val  cloudDependencies="org.springframework.cloud:spring-cloud-dependencies:"


    const val yavi = "am.ik.yavi:yavi:"+Versions.yavi

    const val openApiMvc=  "org.springdoc:springdoc-openapi-starter-webmvc-api:"+Versions.openApiVersion
    const val openApiWebUi=  "org.springdoc:springdoc-openapi-starter-webmvc-ui:"+Versions.openApiVersion

    // Utility library used by Spring AOP/CGLIB to instantiate proxies without invoking constructors
    const val objenesis = "org.objenesis:objenesis"
}