plugins {
    id("java")
}

version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

tasks.test {
    useJUnitPlatform()
}
