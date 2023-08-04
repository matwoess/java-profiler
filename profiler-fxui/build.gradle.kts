plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.0.14"
}

group = "dev.matwoess"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.mkpaz:atlantafx-base:2.0.1")
    implementation(project(mapOf("path" to ":profiler-tool")))
    implementation(project(mapOf("path" to ":profiler-common")))
}

javafx {
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainModule.set("fxui")
    mainClass.set("fxui.App")
}
