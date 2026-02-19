plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.netflix.dgs.codegen") version "7.0.3"
}

group = "br.uff.ic.recomendador"
version = "0.0.1-SNAPSHOT"
description = "Recomendador"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

extra["netflixDgsVersion"] = "10.2.1"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("com.netflix.graphql.dgs:graphql-dgs-spring-graphql-starter")
    implementation("com.netflix.graphql.dgs:graphql-dgs-extended-scalars")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	
	// Apache Jena for RDF/OWL semantic web support
	implementation("org.apache.jena:jena-core:5.4.0")
	implementation("org.apache.jena:jena-arq:5.4.0")
	implementation("org.apache.jena:jena-ontapi:5.4.0")
	
	runtimeOnly("io.micrometer:micrometer-registry-prometheus")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("com.netflix.graphql.dgs:graphql-dgs-spring-graphql-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
	imports {
		mavenBom("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:${property("netflixDgsVersion")}")
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.generateJava {
	packageName = "br.uff.ic.recomendador.main.codegen"
	generateClient = true
    typeMapping = mutableMapOf(
        "Name" to "br.uff.ic.recomendador.domain.models.Name",
    )
}

tasks.withType<Test> {
	useJUnitPlatform()
}
