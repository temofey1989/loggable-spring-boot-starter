import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

import java.time.Duration

plugins {
    kotlin("jvm") version "1.6.0"
    `java-library`
    `maven-publish`
    signing
    jacoco
    id("org.sonarqube") version "3.3"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.springframework.boot") version "2.6.1"
}

group = "io.justdevit.spring"
version = "0.1.0"

repositories {
    mavenCentral()
}

java.sourceCompatibility = JavaVersion.VERSION_11

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("net.logstash.logback:logstash-logback-encoder:7.0.1")

    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    testCompileOnly("org.junit.jupiter:junit-jupiter:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.assertj:assertj-core:3.21.0")
    testImplementation("io.mockk:mockk:1.12.1")
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = java.sourceCompatibility.toString()
        }
    }

    test {
        useJUnitPlatform()
        testLogging {
            events(PASSED, FAILED, SKIPPED)
        }
    }

    jar {
        enabled = true
        archiveClassifier.set("")
    }

    bootJar {
        enabled = false
    }

    jacocoTestReport {
        reports {
            xml.required.set(true)
        }
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "temofey1989_logging-spring-boot-starter")
        property("sonar.organization", "temofey1989")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("${project.group}:${project.name}")
                description.set("Logging abstraction for Spring Boot application.")
                url.set("https://github.com/temofey1989/logging-spring-boot-starter")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("temofey1989")
                        name.set("Artyom Gornostayev")
                        email.set("temofey1989@gmail.com")
                    }
                }
                scm {
                    url.set("https://github.com/temofey1989/logging-spring-boot-starter")
                    connection.set("scm:git:git://github.com/temofey1989/logging-spring-boot-starter.git")
                    developerConnection.set("scm:git:ssh://github.com:temofey1989/logging-spring-boot-starter.git")
                }
            }
            suppressPomMetadataWarningsFor("runtimeElements")
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["maven"])
}

nexusPublishing {
    transitionCheckOptions {
        maxRetries.set(100)
        delayBetween.set(Duration.ofSeconds(5))
    }
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}
