import org.gradle.jvm.application.tasks.CreateStartScripts

plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    application
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

val cliAppName = "local-document-indexer"

application {
    mainClass = "ru.compadre.indexer.AppKt"
    applicationDefaultJvmArgs = listOf(
        "-Dfile.encoding=UTF-8",
        "-Dstdout.encoding=UTF-8",
        "-Dstderr.encoding=UTF-8",
    )
}

tasks.named<CreateStartScripts>("startScripts") {
    applicationName = cliAppName
    defaultJvmOpts = application.applicationDefaultJvmArgs
}

dependencies {
    implementation("com.typesafe:config:1.4.3")
    implementation("io.ktor:ktor-client-core:3.2.1")
    implementation("io.ktor:ktor-client-cio:3.2.1")
    implementation("io.ktor:ktor-client-content-negotiation:3.2.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.2.1")
    implementation("org.xerial:sqlite-jdbc:3.50.3.0")
    implementation("org.apache.pdfbox:pdfbox:3.0.3")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.17")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
