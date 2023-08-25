plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.0.14"
}

group = "dev.matwoess"
version = "0.3.0"

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


val mainClass = "fxui.App"

tasks {
    register("fatJar", Jar::class.java) {
        archiveClassifier.set("all")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest {
            attributes["Main-Class"] = mainClass
        }
        from(configurations.runtimeClasspath.get()
            //.filter { !it.name.startsWith("javafx-") }
            .onEach { println("add from dependencies: ${it.name}") }
            .map { if (it.isDirectory) it else zipTree(it) })
        val sourcesMain = sourceSets.main.get()
        sourcesMain.allSource.forEach { println("add from sources: ${it.name}") }
        from(sourcesMain.output)
    }
}