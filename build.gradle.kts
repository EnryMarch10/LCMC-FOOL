plugins {
    java
    antlr
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
//    testImplementation(platform("org.junit:junit-bom:5.10.0"))
//    testImplementation("org.junit.jupiter:junit-jupiter")
//    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Project specific
    antlr("org.antlr:antlr4:4.13.2")
    implementation("org.antlr:antlr4-runtime:4.13.2")
}

tasks.generateGrammarSource {
    maxHeapSize = "64m"
    arguments = arguments + listOf("-visitor", "-long-messages")
}

//tasks.test {
//    useJUnitPlatform()
//}

application {
    mainClass.set("compiler.Test")
}
