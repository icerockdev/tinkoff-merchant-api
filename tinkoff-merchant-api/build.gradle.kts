/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("kotlin-kapt")
    id("maven-publish")
    id("java-library")
}

apply(plugin = "java")
apply(plugin = "kotlin")

group = "com.icerockdev.service"
version = "0.2.0"

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

dependencies {
    // Logging
    implementation("ch.qos.logback:logback-classic:${properties["logback_version"]}")

    // Ktor
    implementation("io.ktor:ktor-client-core:${properties["ktor_version"]}")
    implementation("io.ktor:ktor-client-json:${properties["ktor_version"]}")
    implementation("io.ktor:ktor-client-apache:${properties["ktor_version"]}")
    implementation("io.ktor:ktor-client-jackson:${properties["ktor_version"]}")
    implementation("io.ktor:ktor-client-logging-jvm:${properties["ktor_version"]}")

    // tests
    testImplementation("io.ktor:ktor-server-tests:${properties["ktor_version"]}")
    testImplementation("io.ktor:ktor-client-mock:${properties["ktor_version"]}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

repositories {
    mavenCentral()
}

publishing {
    repositories.maven("https://api.bintray.com/maven/icerockdev/backend/tinkoff-merchant-api/;publish=1") {
        name = "bintray"

        credentials {
            username = System.getProperty("BINTRAY_USER")
            password = System.getProperty("BINTRAY_KEY")
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}
