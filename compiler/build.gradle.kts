plugins {
    id("java-library")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(project(":project"))
    implementation("com.google.guava:guava:31.0.1-android")
}

tasks.test {
    useJUnitPlatform()
}