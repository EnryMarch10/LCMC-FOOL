plugins {
    java
    application
    id("com.diffplug.spotless") version "8.3.0"
    antlr
}

group = "compiler"
version = "1.0"

repositories {
    mavenCentral()
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Project specific
    antlr("org.antlr:antlr4:4.13.2")
    implementation("org.antlr:antlr4-runtime:4.13.2")
}

tasks.generateGrammarSource {
    maxHeapSize = "64m"
    arguments = arguments + listOf("-visitor", "-long-messages")
}

application {
    mainClass.set("compiler.Test")
}

spotless {
    java {
        // Exclude generated code
        targetExclude("build/**")

        // Palantir Java Format - A modern, lambda-friendly, 120 character Java formatter.
        palantirJavaFormat().formatJavadoc(true)

        // Import management
        removeUnusedImports()
        importOrder()

        // Whitespace rules
        leadingTabsToSpaces(4)
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.test {
    useJUnitPlatform()
    forkEvery = 1
    maxParallelForks = 1
}

// Custom tasks

val deleteAsmFiles by tasks.registering(Delete::class) {
    delete(fileTree(projectDir) {
        include("**/*.asm")
    })
}

tasks.named("clean") {
    dependsOn(deleteAsmFiles)
}

tasks.named("test") {
    dependsOn(deleteAsmFiles)
}

tasks.register("cleanAssembly") {
    group = "build"
    description = "Deletes all generated .asm files"
    dependsOn(deleteAsmFiles)
}
