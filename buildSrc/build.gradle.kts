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
    implementation(libs.sonarlint.plugin)
    implementation(libs.freefair.aggregate.jacoco.plugin)
    implementation(libs.palantir.checkstyle.plugin)
    implementation(libs.palantir.idea.plugin)
    implementation(libs.graalVm.plugin)
    // JGit for Git operations from versioning plugin
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.10.0.202406032230-r")
}