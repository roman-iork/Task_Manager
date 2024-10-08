plugins {
	id ("org.springframework.boot") version "3.3.2"
	id ("io.spring.dependency-management") version "1.1.6"
	id ("checkstyle")
	id ("jacoco")
	id ("application")
	id ("io.sentry.jvm.gradle") version "4.11.0"
}

application {
	mainClass.set("hexlet.code.AppApplication")
}

group = "hexlet.code"
version = "0.0.1-SNAPSHOT"

repositories {
	mavenCentral()
}

dependencies {
	implementation ("org.springframework.boot:spring-boot-starter-actuator")
	implementation ("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation ("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation ("org.springframework.boot:spring-boot-starter-validation")
	implementation ("org.springframework.boot:spring-boot-starter-web")

	implementation("org.openapitools:jackson-databind-nullable:0.2.6")
	implementation("org.mapstruct:mapstruct:1.6.0")
	implementation("net.datafaker:datafaker:2.3.0")
	implementation("org.instancio:instancio-junit:5.0.1")
	implementation ("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
	implementation ("org.projectlombok:lombok")

	runtimeOnly ("com.h2database:h2")
	runtimeOnly ("org.postgresql:postgresql:42.7.3")
	developmentOnly ("org.springframework.boot:spring-boot-devtools")
	annotationProcessor ("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor ("org.projectlombok:lombok")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.6.0")

	testImplementation("net.javacrumbs.json-unit:json-unit-assertj:3.4.1")
	testImplementation ("org.springframework.boot:spring-boot-starter-test")
	testImplementation ("org.springframework.security:spring-security-test")
	testRuntimeOnly ("org.junit.platform:junit-platform-launcher")
}

tasks.test {
	useJUnitPlatform()
}

tasks.jacocoTestReport { reports { xml.required.set(true) } }
