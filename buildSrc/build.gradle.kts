plugins{
    `kotlin-dsl`
}

repositories{
    gradlePluginPortal()
    mavenCentral()
}


dependencies{
    implementation(libs.spring.boot.plugin)
    implementation(libs.spring.dependency.plugin)
}