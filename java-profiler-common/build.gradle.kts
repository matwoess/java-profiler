import com.vanniktech.maven.publish.SonatypeHost

plugins {
    java
    id("com.vanniktech.maven.publish") version "0.30.0"
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
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    repositories {
        maven {
            name = "githubPackages"
            url = uri("https://maven.pkg.github.com/matwoess/java-profiler")
            credentials(PasswordCredentials::class)
        }
    }
}

mavenPublishing {
    signAllPublications()
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    pom {
        name.set("java-profiler-common")
        description.set("Provides common functions for both the profiler tool and its JavaFX UI module.")
        inceptionYear.set("2023")
        url.set("https://github.com/matwoess/java-profiler/")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("matwoess")
                name.set("Mathias Wöß")
                url.set("https://github.com/matwoess/")
            }
        }
        scm {
            url.set("https://github.com/matwoess/java-profiler/")
            connection.set("scm:git:git://github.com/matwoess/java-profiler.git")
            developerConnection.set("scm:git:ssh://git@github.com/matwoess/java-profiler.git")
        }
    }
}