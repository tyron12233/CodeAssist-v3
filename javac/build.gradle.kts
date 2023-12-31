plugins {
    id("java-library")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    api(files("libs/nb-javac-1.0-SNAPSHOT-all.jar"))
}

tasks.test {
    useJUnitPlatform()
}