plugins {
    id("java-library")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(project(":javac"))

    implementation("com.google.auto.value:auto-value-annotations:1.8.2")
    annotationProcessor("com.google.auto.value:auto-value:1.8.2")

    implementation("org.slf4j:slf4j-api:2.0.10")
    implementation("ch.qos.logback:logback-classic:1.4.11")

    implementation("org.jetbrains:annotations:24.1.0")

    implementation("org.ow2.asm:asm:9.6")

    implementation("com.moandjiezana.toml:toml4j:0.7.2")

    implementation("com.google.guava:guava:31.0.1-android")
}

tasks.test {
    useJUnitPlatform()
}