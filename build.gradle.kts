import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.util.Arrays.asList

group = "org.maxlog.example.kotlin.life-game"
version = "1.0-SNAPSHOT"

plugins {
    val kotlinVersion = "1.3.71"
    id("base")
    id("kotlin2js") version (kotlinVersion)
    id("com.github.node-gradle.node") version "2.2.3"
}

val kotlinVersion = "1.3.71"

dependencies {
    implementation(kotlin("stdlib-js", kotlinVersion))
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.6.8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.2")
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
//kotlin.target.browser { }

tasks.withType<Kotlin2JsCompile> {
    kotlinOptions {
        main = "call"
        moduleKind = "umd"
        sourceMap = true
        metaInfo = true
//        outputFile = "${projectDir}/web/js/life.js"
//        sourceMapEmbedSources = "always"
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
    // todo: make DCE
    dependsOn("npmInstall", "assembleJsLib")
}

tasks.register<com.moowork.gradle.node.npm.NpmTask>("start") {
    setArgs(asList("run", "start"))
    dependsOn("bundle")
}

tasks.register<com.moowork.gradle.node.task.NodeTask>("node") {
    version = "8.9.3"
}

task<Copy>("assembleJsLib") {
    configurations.runtimeClasspath.get().resolve().forEach { file: File ->
        from(zipTree(file.absolutePath)) {
            includeEmptyDirs = false
            include { fileTreeElement ->
                val path = fileTreeElement.path
                (path.endsWith(".js") || path.endsWith(".js.map")) && (path.startsWith("META-INF/resources/") ||
                        !path.startsWith("META-INF/"))
            }
        }
    }
    from(tasks.withType<ProcessResources>().map { it.destinationDir })
    into("$buildDir/js")

    dependsOn("classes")
}
