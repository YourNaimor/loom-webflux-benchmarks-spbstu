import org.gradle.api.JavaVersion.VERSION_25
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    java
    id("io.freefair.lombok") version "9.+"
    id("io.spring.dependency-management") version "1.1.+"
    id("org.jetbrains.kotlin.jvm") version "2.2.+"
    id("org.springframework.boot") version "3.5.+"
}

repositories {
    mavenCentral()
}

tasks.bootRun {
    val defaultJvmArgs = listOf("-Xms2g", "-Xmx2g", "-XX:+ExitOnOutOfMemoryError", "-Djdk.tracePinnedThreads=full")
    val extendedArgs = listOf("-XX:+UseCompactObjectHeaders")
    jvmArgs = if (JavaVersion.current().isCompatibleWith(VERSION_25)) defaultJvmArgs + extendedArgs else defaultJvmArgs
}

val javaBytecodeVersion = project.findProperty("java.bytecode.version")?.toString() ?: "21"

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = javaBytecodeVersion
    targetCompatibility = javaBytecodeVersion
}

tasks.withType<KotlinJvmCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(javaBytecodeVersion))
    }
}

dependencies {
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.2.+")
    implementation("io.github.resilience4j:resilience4j-reactor:2.2.+")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql:42.+")
}

val activeProfiles = System.getenv("SPRING_PROFILES_ACTIVE")
if (activeProfiles?.contains("loom-tomcat") == true) {
    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-web")
    }
}
