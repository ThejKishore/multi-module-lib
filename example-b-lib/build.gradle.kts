plugins{
    java
    id("spring-boot-library")
}

dependencies {
    add("implementation", project(":example-lib"))
}