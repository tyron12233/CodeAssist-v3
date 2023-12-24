plugins {
    id("java")
}

group = "org.example"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("com.formdev:flatlaf:3.2.5")

    implementation("com.fifesoft:rsyntaxtextarea:3.3.4")
    implementation("com.fifesoft:autocomplete:3.3.1")
    implementation(project(":project"))
    implementation(project(":completions"))
}

tasks.test {
    useJUnitPlatform()
}