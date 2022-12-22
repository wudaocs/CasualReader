plugins {
    kotlin("jvm") version "1.4.32"
    `kotlin-dsl`
}

dependencies {
    implementation(gradleApi())
}

repositories {
    google()
    mavenCentral()
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}