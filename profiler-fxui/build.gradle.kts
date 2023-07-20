plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.0.13"
}

version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

javafx {
    modules = listOf("javafx.controls", "javafx.fxml")
}