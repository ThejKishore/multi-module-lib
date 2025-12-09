package utility

object  Bundle {
    //core bundle
    val core: Array<String> = arrayOf(
        Libs.bootStarterWeb,
        Libs.bootStarterRestClient,
        Libs.bootStarterActuator,
        Libs.bootStarterJdbc,
        Libs.micrometerBridgeBrave,
        Libs.cloudStarter,
        Libs.cloudResilience4j,
        Libs.openApiMvc,
        Libs.openApiWebUi,

    )



    //core test bundle
     val testing : Array<String> = arrayOf(
        TestLibs.bootMicrometerTest,
        TestLibs.bootJdbcTest,
        TestLibs.bootRestClientTest,
        TestLibs.bootWebmvcTest,
        TestLibs.bootRestClientTest,
        TestLibs.junitPlatformLauncher,
     )

}