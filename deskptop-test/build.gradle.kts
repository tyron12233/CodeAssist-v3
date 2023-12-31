plugins {
    application
    kotlin("jvm") version "1.9.22"
    id("org.openjfx.javafxplugin") version "0.1.0"
}

val javaFxVersion = "21"
val javaFxIncludeInDist = System.getProperty("skip.jfx.bundle") == null

javafx {
    version = javaFxVersion
    modules("javafx.controls", "javafx.media")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("io.github.mkpaz:atlantafx-base:2.0.1")

    implementation("io.insert-koin:koin-core:3.5.3")

    implementation("com.github.Col-E:tiwulfx-dock:1.2.3")
    implementation("ch.qos.logback:logback-classic:1.4.11")

    implementation("org.fxmisc.richtext:richtextfx:0.11.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.15.2")
    implementation("com.github.tommyettinger:regexodus:0.1.15")
    implementation("org.kordamp.ikonli:ikonli-javafx:12.3.1")
    implementation("org.kordamp.ikonli:ikonli-carbonicons-pack:12.3.1")

    implementation("com.fifesoft:rsyntaxtextarea:3.3.4")
    implementation("com.fifesoft:autocomplete:3.3.1")
    implementation(project(":project"))
    implementation(project(":project-impl"))
    implementation(project(":completions"))
    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}