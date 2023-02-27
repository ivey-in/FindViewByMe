plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.10.1"
}

group = "com.jaeger.findviewbyme"
version = "1.4.3"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2021.3.1")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("java"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    runIde {
        ideDir.set(file("/Applications/Android Studio.app/Contents"))
    }
}
