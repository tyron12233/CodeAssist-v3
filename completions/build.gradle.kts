plugins {
    id("java-library")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.google.truth:truth:1.2.0")

    implementation("com.google.guava:guava:31.0.1-android")
    implementation("me.xdrop:fuzzywuzzy:1.2.0")
    implementation("org.slf4j:slf4j-api:2.0.10")
    implementation("org.jetbrains:annotations:24.1.0")

    implementation("com.google.auto.value:auto-value-annotations:1.8.2")
    testImplementation(project(mapOf("path" to ":project-impl")))
    annotationProcessor("com.google.auto.value:auto-value:1.8.2")

    implementation(project(":project"))
    implementation(project(":javac"))

}

tasks.test {
    useJUnitPlatform()
}