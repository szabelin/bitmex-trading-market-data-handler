plugins {
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.23"
}


group = "com.lunartrading"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    runtimeOnly("com.h2database:h2")

    implementation("io.reactivex.rxjava3:rxjava")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa") // Optional for JPA-based access
    implementation("org.springframework.boot:spring-boot-starter-web")      // Optional if using REST/WebSocket

    runtimeOnly("com.h2database:h2")
    implementation("org.java-websocket:Java-WebSocket:1.5.3")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.awaitility:awaitility:4.2.0")
    testImplementation("org.assertj:assertj-core:3.27.3")
}


tasks.withType<Test> {
    useJUnitPlatform()
}
