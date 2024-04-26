// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.lang.System.getenv
import java.net.URI

plugins {
    kotlin("jvm") version "1.9.23" apply false
    kotlin("plugin.serialization") version "1.9.23" apply false
    id("org.cyclonedx.bom") version "1.8.2" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("org.jetbrains.kotlinx.kover") version "0.7.6"
}

subprojects {
    group = "io.github.lmos-ai.arc"
    version = "0.19.0"

    apply(plugin = "org.cyclonedx.bom")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "kotlinx-serialization")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "org.jetbrains.kotlinx.kover")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        withSourcesJar()
        withJavadocJar()
    }

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        debug.set(true)
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += "-Xjsr305=strict"
            freeCompilerArgs += "-Xcontext-receivers"
            jvmTarget = "17"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    configure<PublishingExtension> {
        publications {
            create("Maven", MavenPublication::class.java) {
                from(components["java"])
            }
        }

        repositories {
            maven {
                name = "github"
                url = URI("https://maven.pkg.github.com/lmos-ai/arc")
                credentials {
                    username = findProperty("GITHUB_USER")?.toString() ?: getenv("GITHUB_USER")
                    password = findProperty("GITHUB_TOKEN")?.toString() ?: getenv("GITHUB_TOKEN")
                }
            }
        }
    }

    dependencies {
        val kotlinXVersion = "1.8.0"
        "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:$kotlinXVersion")
        "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinXVersion")
        "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinXVersion")
        "implementation"("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

        // Testing
        "testImplementation"("org.junit.jupiter:junit-jupiter:5.10.2")
        "testImplementation"("org.assertj:assertj-core:3.25.3")
        "testImplementation"("io.mockk:mockk:1.13.10")
    }

    repositories {
        mavenCentral()
        google()
    }
}

dependencies {
    kover(project("arc-scripting"))
    kover(project("arc-azure-client"))
    kover(project("arc-ollama-client"))
    kover(project("arc-gemini-client"))
    kover(project("arc-result"))
    kover(project("arc-reader-pdf"))
    kover(project("arc-reader-html"))
    kover(project("arc-agents"))
    kover(project("arc-spring-boot-starter"))
}

repositories {
    mavenCentral()
}

fun Project.java(configure: Action<JavaPluginExtension>): Unit =
    (this as ExtensionAware).extensions.configure("java", configure)