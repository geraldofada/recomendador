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
	
	// OWL API + HermiT Reasoner
	// Exclude owlapi-distribution:4.1.3 (HermiT's transitive dep) to avoid version conflict
	// with owlapi 4.5.26 modular jars — mixing both causes ServiceLoader failures at runtime.
	implementation("net.sourceforge.owlapi:org.semanticweb.hermit:1.3.8.413") {
		exclude(group = "net.sourceforge.owlapi", module = "owlapi-distribution")
	}
	implementation("net.sourceforge.owlapi:owlapi-api:4.5.26")
	implementation("net.sourceforge.owlapi:owlapi-apibinding:4.5.26")
	implementation("net.sourceforge.owlapi:owlapi-rio:4.5.26")

	// Openllet (Pellet fork with Jena integration)
	implementation("com.github.galigator.openllet:openllet-jena:2.6.5")
	
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

// HermiT uses owlapi-distribution 4.1.3 which depends on Guice 4.0 + CGLIB.
// Guice 4.0's CGLIB requires reflective access to ClassLoader.defineClass,
// which is blocked by the Java 9+ module system. This opens it for Java 21.
val javaOpenArgs = listOf("--add-opens", "java.base/java.lang=ALL-UNNAMED")

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    jvmArgs(javaOpenArgs)
}

tasks.withType<Test> {
    jvmArgs(javaOpenArgs)
	useJUnitPlatform()
}
