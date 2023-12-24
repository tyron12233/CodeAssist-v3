plugins {
    id("java-library")
}

group = "com.tyron.code"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.google.truth:truth:1.2.0")

    implementation("com.google.guava:guava:31.0.1-android")
    implementation("me.xdrop:fuzzywuzzy:1.2.0")

    implementation("com.google.auto.value:auto-value-annotations:1.8.2")
    annotationProcessor("com.google.auto.value:auto-value:1.8.2")

    implementation(project(":project"))
    implementation(project(":javac"))

}

tasks.test {
    useJUnitPlatform()
}