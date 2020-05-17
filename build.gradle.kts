import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.util.Arrays.asList

group = "org.maxlog.example.kotlin.life-game"
version = "1.0-SNAPSHOT"

plugins {
    id("base")
    id("org.jetbrains.kotlin.js") version "1.3.71"
    id("com.github.node-gradle.node") version "2.2.3"
}

val kotlinVersion: String by project
val coroutinesVersion: String by project
val htmlVersion: String by project
val nodeVersion: String by project

dependencies {
    implementation(kotlin("stdlib-js", kotlinVersion))
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:$htmlVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutinesVersion")
}
repositories {
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    maven { url = uri("https://dl.bintray.com/devexperts/Maven/") }
}

buildscript {
    repositories {
        mavenCentral()
        jcenter()
        maven {
            url = uri("https://kotlin.bintray.com/kotlinx")
        }
        maven {
            url = uri("https://kotlin.bintray.com/kotlin-dev")
        }
    }
}
kotlin.target.browser { }

tasks.withType<Kotlin2JsCompile> {
    kotlinOptions {
        main = "call"
        moduleKind = "umd"
        sourceMap = true
        metaInfo = true
//        outputFile = "${projectDir}/web/js/life.js"
        sourceMapEmbedSources = "always"
    }
}

tasks.register<Delete>("cleanWeb") {
    delete("web")
    isFollowSymlinks = true
}

tasks.clean {
    dependsOn("cleanWeb")
}

tasks.register<com.moowork.gradle.node.npm.NpmTask>("bundle") {
    setArgs(asList("run", "bundle"))
    dependsOn("browserProductionWebpack")
}

tasks.register<com.moowork.gradle.node.npm.NpmTask>("start") {
    setArgs(asList("run", "start"))
    dependsOn("browserDevelopmentWebpack")
}

tasks.register<com.moowork.gradle.node.task.NodeTask>("node") {
    version = nodeVersion
}
