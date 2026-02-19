# AGENTS.md - Developer Guidelines for Recomendador

## Project Overview

This is a Kotlin/Spring Boot 3.x application with GraphQL (Netflix DGS) for a wine recommendation system based on semantic web technologies. The project uses:
- **Language**: Kotlin 1.9.25
- **Build System**: Gradle 8.14
- **Framework**: Spring Boot 3.5.6
- **API**: GraphQL with Netflix DGS 10.2.1
- **Knowledge Base**: Apache Jena (RDF triples / OWL ontologies)
- **Java Version**: 21

---

## Build Commands

### Run Application
```bash
./gradlew bootRun
```

### Build
```bash
./gradlew build
./gradlew assemble
./gradlew bootBuildImage  # Build OCI image
```

### Testing
```bash
./gradlew test                              # Run all tests
./gradlew test --tests "*.MainApplicationTests"    # Run specific test class
./gradlew test --tests "*.MainApplicationTests.testName"  # Run specific test method
./gradlew bootTestRun                       # Run app with test runtime classpath
```

### Code Generation
```bash
./gradlew generateJava                      # Generate Java types from GraphQL schema
```

### Other Commands
```bash
./gradlew clean
./gradlew dependencies                      # List dependencies
./gradlew dependencies --configuration testRuntimeClasspath  # Check test deps
```

---

## Code Style Guidelines

### General Principles
- Use **Kotlin idioms** - prefer immutable data, expression bodies, and standard library functions
- Follow **Spring Boot conventions** for configuration and structure
- Use **GraphQL DGS** annotations for API development
- Keep functions small and focused (single responsibility)

### Naming Conventions
- **Packages**: lowercase, dot-separated (e.g., `br.uff.ic.recomendador.domain.repositories`)
- **Classes/Interfaces**: PascalCase (e.g., `WineRepository`, `WineRecommendation`)
- **Functions**: camelCase (e.g., `getWineByName`)
- **Constants**: UPPER_SNAKE_CASE
- **Files**: PascalCase matching class name (e.g., `WineRepository.kt`)

### Import Organization
Organize imports in the following order (Kotlin default with IntelliJ):
1. Kotlin standard library (`kotlin.*`)
2. Java standard library (`java.*`, `javax.*`)
3. Third-party libraries (Spring, DGS, etc.)
4. Project imports

Example:
```kotlin
package br.uff.ic.recomendador.domain.repositories

import br.uff.ic.recomendador.domain.models.Name
import br.uff.ic.recomendador.main.codegen.types.Wine
import com.netflix.graphql.dgs.DgsComponent
```

### Types and Type Safety
- Use **explicit types** for function parameters and return types
- Use **data classes** for value objects and DTOs
- Leverage **sealed classes** for union types
- Prefer **nullable types** (`T?`) over Java-style Optional
- Use **val** by default, **var** only when mutation is required

### Formatting
- Use **4 spaces** for indentation (not tabs)
- Maximum line length: **120 characters** (soft guideline)
- Keep blank lines between top-level declarations
- Use trailing commas for multi-line collections

### Architecture Layers
```
src/main/kotlin/br/uff/ic/recomendador/
├── main/           # Application entry point
├── domain/         # Domain models and repository interfaces
│   ├── models/     # Domain entities and value objects
│   ├── repositories/  # Repository interfaces
│   └── scalars/   # GraphQL custom scalars
├── business/       # Business logic (DGS query/mutation handlers)
└── repositories/   # Data access implementations
```

### GraphQL DGS Patterns
- Use `@DgsComponent` for data fetchers
- Use `@DgsQuery` and `@DgsMutation` for query/mutation handlers
- Use `@InputArgument` for GraphQL input parameters
- Define custom scalars with `@DgsScalar` annotation

Example:
```kotlin
@Service
@DgsComponent
class WineRecommendation(
    @param:Autowired private val wineRepository: WineRepository
) {
    @DgsQuery(field = "wine")
    fun getWineByName(@InputArgument name: Name) = wineRepository.getWineByName(name)
}
```

### Error Handling
- Use **custom exceptions** for domain-specific errors
- Let exceptions propagate to the framework layer (Spring/DGS handles HTTP/GraphQL error responses)
- Avoid catching generic `Exception` or `Throwable`
- Use **Result<T>** or **sealed classes** for operations that can fail

### Testing
- Use **JUnit 5** (`org.junit.jupiter.api.Test`)
- Use **SpringBootTest** for integration tests
- Use **kotest** or **kotlin-test** for unit test assertions
- Keep tests in parallel directory structure: `src/test/kotlin/...` mirrors `src/main/kotlin/...`

### Data Access (Apache Jena)
- Define **repository interfaces** in `domain/repositories`
- Implement repositories in `repositories/` package using **Apache Jena**
- Use Jena's `Model`, `QueryExecution`, and `OntModel` for RDF/OWL operations
- Store ontology files (`.rdf`, `.owl`) in `src/main/resources/`
- Use **SPARQL** queries for data retrieval
- Repository implementations typically use `_OwlRepository` suffix (e.g., `WineOwlRepository`)

### Dependencies
- All dependencies are managed in `build.gradle.kts`
- Netflix DGS version is managed via `extra["netflixDgsVersion"]`
- Use BOMs for version management where available

---

## Docker Compose

The project includes `compose.yaml` for local PostgreSQL development:
```bash
docker compose up -d
```

---

## RDF/Ontology Files

Ontology files (`.rdf`, `.owl`) should be stored in `src/main/resources/`. These contain the knowledge base for wine recommendations.

---

## GraphQL Schema

Schema is defined in `src/main/resources/schema/schema.graphql`. After modifying the schema, run:
```bash
./gradlew generateJava
```

This generates Java types in `br.uff.ic.recomendador.main.codegen.types` package.

---

## File Organization

- **Source code**: `src/main/kotlin/`
- **Tests**: `src/test/kotlin/`
- **Resources**: `src/main/resources/`
- **GraphQL Schema**: `src/main/resources/schema/schema.graphql`

---

## Notes for Agents

1. Always regenerate GraphQL types (`./gradlew generateJava`) after modifying `schema.graphql`
2. Run tests before committing: `./gradlew test`
3. Use `bootRun` to test the application locally
4. Custom scalars (like `Name`) need both a Kotlin type in `domain/models` and a `Coercing` implementation in `domain/scalars`
5. The project uses Spring's dependency injection - use `@Autowired` or constructor injection
6. Ontology files (`.rdf`, `.owl`) go in `src/main/resources/`
