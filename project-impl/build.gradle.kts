plugins {
    id("java")
}


dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(files("libs/tomlconfig-0.2.4.jar"))
    implementation("com.google.guava:guava:31.0.1-android")
    implementation("org.slf4j:slf4j-api:2.0.10")
    implementation("org.jetbrains:annotations:24.1.0")
    implementation("org.ow2.asm:asm:9.6")



    implementation(project(":project"))
}

tasks.test {
    useJUnitPlatform()
}