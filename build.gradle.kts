group = "org.maxlog.example.kotlin.life-game"
version = "1.0-SNAPSHOT"

apply plugin: "base"
apply plugin: "kotlin-platform-js"
apply plugin: "kotlin-dce-js"
apply plugin: "com.github.node-gradle.node"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-js:$kotlin_version")
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:$html_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutines_version")
}
repositories {
    jcenter()
    maven { url = "https://kotlin.bintray.com/kotlinx" }
    maven { url = "https://dl.bintray.com/devexperts/Maven/" }
}

buildscript {
    repositories {
        mavenCentral()
        jcenter()
        maven { url = "https://kotlin.bintray.com/kotlinx" }
        maven { url = "https://kotlin.bintray.com/kotlin-dev" }
        maven { url = "https://plugins.gradle.org/m2/" }
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:$bintray_version")
        classpath("com.github.node-gradle:gradle-node-plugin:$gradle_node_version")
    }
}

compileKotlin2Js {
    kotlinOptions {
        main = "call"
        moduleKind = "umd"
        sourceMap = true
        metaInfo = true
//        outputFile = "${projectDir}/web/js/life.js"
//        sourceMapEmbedSources = "always"
    }
}

task cleanWeb(type: Delete) {
  delete("web")
  followSymlinks = true
}

clean.dependsOn cleanWeb


node {
    version = "$node_version"
    npmVersion = "$npm_version"
    download = true
}

task bundle(type: NpmTask, dependsOn: [npmInstall, runDceKotlinJs]) {
    args = ["run", "bundle"]
}

task start(type: NpmTask, dependsOn: bundle) {
    args = ["run", "start"]
}

clean.dependsOn(gradle.includedBuilds.collect { it.task(":clean") })
