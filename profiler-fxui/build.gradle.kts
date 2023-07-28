plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.0.13"
}

version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.mkpaz:atlantafx-base:2.0.1")
}

javafx {
    modules = listOf("javafx.controls", "javafx.fxml")
}