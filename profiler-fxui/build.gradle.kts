plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.0.14"
}

version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.mkpaz:atlantafx-base:2.0.1")
    implementation(project(mapOf("path" to ":profiler-tool")))
}

javafx {
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainModule.set("app")
    mainClass.set("app.App")
}
