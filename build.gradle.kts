allprojects {
    group = "com.tyron.code"
    version = "1.0-SNAPSHOT"
}

subprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven {
            url = uri("https://jitpack.io")
        }
    }
}