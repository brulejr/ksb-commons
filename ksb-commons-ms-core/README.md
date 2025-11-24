# ksb-commons-ms-core

`ksb-commons-ms-core` provides shared **microservice infrastructure** for the `ksb-commons` ecosystem, with a focus on JVM/Kotlin services built on modern frameworks (e.g., Spring Boot / WebFlux).

Where `ksb-commons-core` is strictly framework-agnostic, this module is allowed to depend on microservice frameworks and is the home for:

- Common service-layer abstractions and patterns.
- Shared error handling and outcome models for HTTP / messaging.
- Structured logging and metrics hooks.
- Cross-cutting concerns that should be consistent across multiple services.

Use this module when you want to standardize how services behave, while keeping business logic in your individual microservice projects.

---

## Usage

### Gradle (Kotlin DSL)

With the BOM (recommended):

```kotlin
dependencies {
    implementation(platform("io.jrb.labs:ksb-dependency-bom:<version>"))
    implementation("io.jrb.labs:ksb-commons-ms-core")
}
```

Without the BOM:

```kotlin
dependencies {
    implementation("io.jrb.labs:ksb-commons-ms-core:<version>")
}
```

### Maven

```xml
<dependency>
  <groupId>io.jrb.labs</groupId>
  <artifactId>ksb-commons-ms-core</artifactId>
  <version>${ksb.version}</version>
</dependency>
```

> Replace `io.jrb.labs` and `<version>` / `${ksb.version}` with your actual published coordinates.

---

## What this module is for

Use `ksb-commons-ms-core` when you want to:

- Share **service-level patterns** between microservices:
    - Standardized controller / handler patterns.
    - Workflow or use-case orchestration templates.
    - Common sealed outcome types for HTTP responses.
- Centralize **error handling and exception mapping**:
    - Map domain/service exceptions to standard HTTP responses.
    - Provide consistent error payloads across services.
- Introduce **standard observability**:
    - Structured logging helpers.
    - Metric naming conventions and wrappers.
    - Hooks for tracing (if used).

Typical examples of content here:

- Base classes or interfaces for REST endpoints, handlers, or workflows.
- Shared configuration for things like:
    - HTTP error responses.
    - Validation handling.
    - Standard response wrappers.
- Infrastructure-focussed utilities that *do* depend on frameworks (Spring Boot / WebFlux, etc.).

---

## Relationship to other modules

- **`ksb-commons-core`**  
  Framework-agnostic utilities and data abstractions. Does **not** know about HTTP, controllers, or microservice infrastructure.

- **`ksb-commons-ms-core`** (this module)  
  Builds on top of `ksb-commons-core` and microservice frameworks to define **how services behave** (boundaries, responses, logging, metrics).

- **`ksb-commons-ms-client`**  
  Focused on **calling** services (clients), whereas `ksb-commons-ms-core` is primarily about **implementing** them.

---

## API Documentation

Generated API documentation for this module is intended to be published in the GitHub Wiki:

- **ksb-commons-ms-core Javadoc / Dokka**  
  https://github.com/brulejr/ksb-commons/wiki/ksb-commons-ms-core-Javadoc

If the page is not yet available, you can generate docs locally.

### Generating docs locally (example)

Exact tasks depend on your documentation setup (Dokka vs Javadoc), but typical usage might look like:

```bash
# Build and run tests
./gradlew :ksb-commons-ms-core:build

# Example if Dokka is enabled
./gradlew :ksb-commons-ms-core:dokkaHtml
```

Then copy the generated HTML into your local GitHub wiki clone and push.

---

## Design Principles

### 1. Clear service boundaries

- Public APIs should represent service boundaries clearly:
    - Request/response models should be well-defined.
    - Sealed outcome types should capture **success** vs **failure** in a structured way.
- Error handling should be standardized:
    - Central exception handlers.
    - Consistent error payloads and HTTP status codes.

### 2. Infrastructure, not business logic

- Only cross-cutting infrastructure and patterns belong here.
- Do **not** add service-specific domain logic.
- If a pattern is only used by one microservice, keep it in that service until it proves reusable.

### 3. Observability first

- Encourage logging and metrics for:
    - Incoming requests / messages.
    - Workflow/step execution.
    - Errors and retries.
- Provide helpers that make it easy to:
    - Add structured log fields (e.g., correlation IDs, request IDs).
    - Define metrics with consistent naming patterns.

### 4. Minimal but intentional framework usage

- Use microservice frameworks (e.g., Spring Boot / WebFlux) where it makes sense.
- Keep abstractions reasonably thin and opinionated, but not overly complex.
- Prefer composable helpers over large monolithic “base classes,” unless there is a clear convention.

---

## Example: Standardized service outcome (conceptual)

A typical pattern for HTTP endpoints might be:

```kotlin
sealed interface ServiceOutcome<out T> {
    data class Ok<T>(val value: T) : ServiceOutcome<T>
    data class NotFound(val message: String? = null) : ServiceOutcome<Nothing>
    data class ValidationError(val errors: List<String>) : ServiceOutcome<Nothing>
    data class Failure(val cause: Throwable) : ServiceOutcome<Nothing>
}
```

Then a common controller helper could map this to HTTP responses:

```kotlin
fun <T> ServiceOutcome<T>.toResponseEntity(): ResponseEntity<Any> =
    when (this) {
        is ServiceOutcome.Ok -> ResponseEntity.ok(value)
        is ServiceOutcome.NotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(message ?: "Not found")
        is ServiceOutcome.ValidationError -> ResponseEntity.badRequest().body(mapOf("errors" to errors))
        is ServiceOutcome.Failure -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error")
    }
```

The real implementation will likely be more sophisticated, but this illustrates the type of pattern `ksb-commons-ms-core` is designed to host.

---

## Development Notes

When adding or updating functionality in `ksb-commons-ms-core`:

1. **Verify reusability**  
   Is this pattern used (or likely to be used) in multiple services? If not, keep it in the service until it proves reusable.

2. **Minimize hard coupling**  
   Avoid coupling tightly to a specific domain. Favor more generic abstractions (e.g., generic workflows, generic service outcomes).

3. **Document behavior**  
   For public APIs, add KDoc/Javadoc that clarifies:
    - How the abstraction should be used.
    - What frameworks it expects (e.g., Spring Boot, WebFlux).
    - Any required configuration (properties, beans, etc.).

4. **Test thoroughly**
    - Use `ksb-commons-test` helpers where appropriate.
    - Cover success, failure, and edge cases for service outcomes and error mapping.

5. **Maintain compatibility**
    - Introduce breaking changes carefully and, if possible, with deprecation paths.
    - Coordinate changes with the BOM (`ksb-dependency-bom`) so versions stay aligned.

---

## Issues & Change Requests

If you encounter issues or want to propose new microservice-level abstractions:

👉 Open an issue in the main repository:  
https://github.com/brulejr/ksb-commons/issues

Please include:

- The version of `ksb-commons-ms-core` you are using.
- A description of the microservice pattern or problem you are trying to solve.
- Code examples from existing services that motivated the change.
- Notes on which frameworks are involved (e.g., Spring Boot 3.x, WebFlux, Reactor, etc.).

---

## License

`ksb-commons-ms-core` is part of the `ksb-commons` project and is licensed under the **MIT License**.

See the root [`LICENSE`](../LICENSE) file for the full license text.