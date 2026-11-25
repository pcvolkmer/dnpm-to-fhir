import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("java")
    id("java-library")
    id("com.gradleup.shadow") version "9.6.1"
    id("com.diffplug.spotless") version "8.10.1"
    id("net.ltgt.errorprone") version "5.1.1"
    id("maven-publish")
}

group = "dev.pcvolkmer.onco"
version = "0.1.1" // x-release-please-version

val mtbModel = "0.6.4"

// Min versions as required by Onkostar
val commonsCli ="1.10.0"
val commonsIo = "2.22.0"
val slf4j = "2.0.17"
val hapiFhirVersion = "7.6.1"

val toFhirVersion = "0.2.21"

// Test dependencies
val junit = "5.14.4"
val assertj = "3.27.7"
val approvaltests = "31.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    maven {
        url = uri("https://git.dnpm.dev/api/packages/public-snapshots/maven")
    }
    maven {
        url = uri("https://git.dnpm.dev/api/packages/public/maven")
    }
    mavenCentral()
}

sourceSets {
    create("snapshotTest") {
        java.srcDir("src/snapshotTest/java")

        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output

        compileClasspath += sourceSets.test.get().output
        runtimeClasspath += sourceSets.test.get().output
    }
}

val snapshotTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
    extendsFrom(configurations.testImplementation.get())
    extendsFrom(configurations.runtimeOnly.get())
    extendsFrom(configurations.testRuntimeOnly.get())
}

configurations {
    snapshotTestImplementation.extendsFrom(configurations.testImplementation.get())
}

dependencies {
    api("dev.pcvolkmer.mv64e:mtb-model:$mtbModel")

    implementation("commons-cli:commons-cli:$commonsCli")
    implementation("commons-io:commons-io:$commonsIo")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-base:${hapiFhirVersion}")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:${hapiFhirVersion}")
    implementation("io.github.diz-uker:to-fhir:${toFhirVersion}")
    implementation("org.slf4j:slf4j-api:$slf4j")
    implementation("org.jspecify:jspecify:1.0.0")

    errorprone("com.google.errorprone:error_prone_core:2.50.0")
    errorprone("com.uber.nullaway:nullaway:0.14.1")

    testImplementation(platform("org.junit:junit-bom:$junit"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:$assertj")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    snapshotTestImplementation("com.approvaltests:approvaltests:$approvaltests")
}

// Include dependencies in resulting JAR file
tasks.jar {
    manifest {
        attributes["Main-Class"] = "dev.pcvolkmer.onco.datamapper.fhir.MappingApplication"
    }
}

// Build fat JAR using task build
tasks.build.get().dependsOn(tasks.shadowJar)

tasks.register<Test>("snapshotTest") {
    description = "Runs integration tests"
    group = "verification"

    testClassesDirs = sourceSets["snapshotTest"].output.classesDirs
    classpath = sourceSets["snapshotTest"].runtimeClasspath

    shouldRunAfter("test")
}

tasks.withType<Test> {
    testLogging {
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
    useJUnitPlatform()
    dependsOn(tasks.spotlessCheck)

    systemProperty("user.timezone", "UTC")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.errorprone {
        disableAllChecks = true
        option("NullAway:OnlyNullMarked", "true")
        error("NullAway")
    }
}

spotless {
    java {
        importOrder()
        removeUnusedImports()
        googleJavaFormat()
    }
}

publishing {
    repositories {
        mavenLocal()
        maven {
            name = "GitDnpmDev"

            val releasesRepoUrl = uri("https://git.dnpm.dev/api/packages/public/maven")
            val snapshotsRepoUrl = uri("https://git.dnpm.dev/api/packages/public-snapshots/maven")
            url = if (version.toString().endsWith("SNAPSHOT"))
                snapshotsRepoUrl
            else
                releasesRepoUrl

            credentials(HttpHeaderCredentials::class) {
                name = "Authorization"
                value = "token ${properties["dnpm_dev_token"] ?: ""}"
            }

            authentication {
                create<HttpHeaderAuthentication>("header")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            artifact(tasks.jar)
        }
    }
}
