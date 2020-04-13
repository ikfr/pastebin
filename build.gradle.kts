plugins {
    kotlin("jvm") version "1.3.71"
}

group = "com.github.hzqd.ikfr.pastebin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.vertx:vertx-core:3.9.0")
    implementation("io.vertx:vertx-web:3.9.0")
    implementation("org.redisson:redisson:3.12.4")
    implementation("com.google.guava:guava:28.2-jre")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}