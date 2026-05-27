plugins {
    java
    application
    id("com.diffplug.spotless") version "8.4.0"
    id("org.openrewrite.rewrite") version "7.32.2"
    antlr
}

group = "compiler"
version = "1.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // OpenRewrite
    rewrite("org.openrewrite.recipe:rewrite-static-analysis:2.34.1")
    rewrite("org.openrewrite.recipe:rewrite-migrate-java:3.34.1")
    // Antlr
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

        // Imports
        removeUnusedImports()
        forbidModuleImports()
        // Annotations
        formatAnnotations()
        // General
        leadingTabsToSpaces(4)
        trimTrailingWhitespace()
        endWithNewline()
        // Custom
        // separator character is also \s, but listed everything to be sure
        replaceRegex("fixEmptyBodiesIndentation", "\\{[ ]?[ \\t\\n\\x0B\\f\\r]*}", "{ }")
        replaceRegex("fixEmptyLineFirst", "\\{\\r?\\n(\\r?\\n)+", "{\n")
        replaceRegex("fixEmptyLineLast", "\\r?\\n(\\r?\\n)+([ \\t\\x0B\\f\\r]*)}", "\n$2}")
    }
}

rewrite {
    configFile = project.rootProject.file("config/rewrite/rewrite.yml")
    activeRecipe("org.marchionni.java.CustomRules")
    failOnDryRunResults = true
}

tasks.test {
    useJUnitPlatform()
    inputs.files(fileTree("samples") {
        exclude("**/*.asm")
    })
}

// Custom tasks

// lintTasks INCLUDED
tasks.register("ciLint") {
    group = "verification"
    description = "Runs all checks except tests"
    dependsOn("check")
}

// lintTasks EXCLUDED
tasks.register("ciBuild") {
    group = "build"
    description = "CI build without linting tasks"
    dependsOn("assemble", "test")
}

tasks.register("format") {
    group = "formatting"
    description = "Runs OpenRewrite then Spotless"
    dependsOn(tasks.named("spotlessApply"), tasks.named("rewriteRun"))
}

val deleteAsmFiles by tasks.registering(Delete::class) {
    delete(fileTree(projectDir) {
        include("**/*.asm")
    })
}
