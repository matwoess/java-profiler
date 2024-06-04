plugins {
    java
}

group = property("group") ?: ""
version = property("commonVersion") ?: "unknown"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
tasks.compileJava {
    options.release = 17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(mapOf("path" to ":profiler-common")))
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.github.stefanbirkner:system-lambda:1.2.1")
}

tasks.test {
    useJUnitPlatform()
    // TODO: remove as soon as replacement API https://bugs.openjdk.org/browse/JDK-8199704 is available
    jvmArgs("-Djava.security.manager=allow")
}

val mainClass = "tool.cli.Main"

tasks {
    register("fatJar", Jar::class.java) {
        archiveBaseName.set("profiler")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest {
            attributes["Main-Class"] = mainClass
        }
        from(configurations.runtimeClasspath.get()
            .onEach { println("add from dependencies: ${it.name}") }
            .map { if (it.isDirectory) it else zipTree(it) })
        val sourcesMain = sourceSets.main.get()
        from(sourcesMain.output)
    }
}