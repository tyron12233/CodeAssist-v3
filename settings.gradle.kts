plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "CodeAssist"
include("completions")
include("project")
include("compiler")
include("deskptop-test")
include("javac")
include("project-impl")
