# ksb-commons-core

`ksb-commons-core` is the **framework-agnostic** foundation of the `ksb-commons` ecosystem.

It provides Kotlin utilities and core abstractions that can be reused across multiple services and libraries without pulling in any specific web or microservice framework.

Typical content includes:

- Immutable, schema-flexible data containers (e.g., `GenericData`-style envelopes).
- Type-safe facades over generic data structures.
- Common sealed-class outcome types.
- General-purpose extensions and helpers for the Kotlin/JVM standard library.
- Small, focused utilities that are not tied to Spring, Quarkus, or other frameworks.

This module is safe to use in any JVM/Kotlin project, including CLI tools, libraries, and microservices.

---

## Usage

### Gradle (Kotlin DSL)

With the BOM (recommended):

```kotlin
dependencies {
    implementation(platform("io.jrb.labs:ksb-dependency-bom:<version>"))
    implementation("io.jrb.labs:ksb-commons-core")
}
```

Without the BOM:

```kotlin
dependencies {
    implementation("io.jrb.labs:ksb-commons-core:<version>")
}
```

### Maven

```xml
<dependency>
  <groupId>io.jrb.labs</groupId>
  <artifactId>ksb-commons-core</artifactId>
  <version>${ksb.version}</version>
</dependency>
```

> Replace `io.jrb.labs` and `<version>` / `${ksb.version}` with the actual coordinates used when publishing.

---

## What this module is for

Use `ksb-commons-core` when you want to:

- Share data model building blocks across multiple projects (e.g., generic containers with `id`, `type`, `timestamp`, and `attributes`).
- Provide **type-safe facades** that sit on top of generic data containers but still serialize/deserialize cleanly.
- Define **sealed outcome types** used by services, workflows, or repositories (e.g., `Success`, `Failure`, `NotFound`, etc.).
- Centralize small, broadly useful Kotlin utilities (extensions, operators, helpers).

This module intentionally **does not** contain framework-specific code. Anything that depends on Spring, WebFlux, HTTP clients, or other infrastructure should live in one of the `ksb-commons-ms-*` modules instead.

---

## API Documentation

Generated API documentation for this module is intended to be published in the GitHub Wiki:

- **ksb-commons-core Javadoc / Dokka**  
  https://github.com/brulejr/ksb-commons/wiki/ksb-commons-core-Javadoc

If the page is not yet available, you can generate docs locally.

### Generating docs locally (example)

Exact tasks depend on your documentation setup (Dokka vs Javadoc), but typical usage might look like:

```bash
# Run tests and build artifacts
./gradlew :ksb-commons-core:build

# Example if Dokka is enabled
./gradlew :ksb-commons-core:dokkaHtml
```

Then copy the generated HTML into a clone of the GitHub wiki and push.

---

## Design Principles

Some of the key design principles for `ksb-commons-core`:

### 1. Immutability by default

- Prefer Kotlin data classes with `val` properties.
- Use copy operations (`copy(...)`) for modifications instead of mutable setters.
- Avoid mutable collections in public APIs; wrap them if internal mutation is required.

### 2. Schema-flexible containers

- Use a small, stable envelope (e.g., `id`, `type`, `timestamp`) plus an `attributes` map.
- Keep serialization/deserialization robust via Jackson configuration (e.g., `@JsonAnyGetter`, `@JsonAnySetter`, custom serializers/deserializers).
- Build **facades** around these containers to expose domain-specific views without losing generic handling downstream.

### 3. Type-safe facades

- Facades should:
    - Hold a delegate to a generic container.
    - Provide strongly typed getters and builder-style methods.
    - Hide the underlying implementation details for most consumers, but still allow access when needed (`unwrap()` / `delegate()`).

### 4. Sealed outcomes

- Return results as sealed classes instead of throwing exceptions for normal control flow.
- Examples:
    - `Success<T>`
    - `NotFound`
    - `ValidationError`
    - `Failure(cause: Throwable)`
- This makes consumption more explicit and safer with `when` expressions.

### 5. Minimal dependencies

- Keep third-party dependencies small and stable.
- Avoid adding frameworks or heavy libraries; they belong in higher-level modules.

---

## Development Notes

When contributing new functionality to `ksb-commons-core`:

1. **Ask if it is truly core.**  
   If the code depends on a particular framework or microservice pattern, it likely belongs in `ksb-commons-ms-core` or a service-specific module instead.

2. **Keep APIs small and focused.**  
   Prefer a small number of well-designed types over large “kitchen sink” utility classes.

3. **Document with KDoc/Javadoc.**  
   Add short explanations and examples for new public types and functions.

4. **Cover with tests.**  
   All public APIs should have unit tests, ideally using the shared helpers from `ksb-commons-test` where appropriate.

5. **Maintain compatibility.**  
   Be mindful of changes that might break existing consumers. Favor additive changes and deprecations over abrupt removals.

---

## Example: Using a generic data container + facade

Here’s a high-level conceptual example (simplified):

```kotlin
// Generic container (conceptual)
data class GenericData(
    val id: UUID,
    val type: String,
    val timestamp: Instant,
    val attributes: Map<String, Any?>
)

// A facade for a specific use case
data class TodoRequest(private val delegate: GenericData) {

    val id: UUID get() = delegate.id
    val title: String get() = delegate.attributes["title"] as String
    val completed: Boolean get() = delegate.attributes["completed"] as? Boolean ?: false

    fun unwrap(): GenericData = delegate

    companion object {
        fun create(title: String, completed: Boolean = false): TodoRequest {
            val data = GenericData(
                id = UUID.randomUUID(),
                type = "todo",
                timestamp = Instant.now(),
                attributes = mapOf(
                    "title" to title,
                    "completed" to completed
                )
            )
            return TodoRequest(data)
        }
    }
}
```

The actual implementation details may differ, but this pattern is typical of what `ksb-commons-core` is designed to support.

---

## Issues & Change Requests

If you find a bug or want to propose a new core abstraction:

👉 Open an issue in the main repository:  
https://github.com/brulejr/ksb-commons/issues

Please include:

- The version of `ksb-commons-core` you are using.
- A description of your use case and why it belongs in **core**.
- Any relevant code examples or sketches of the proposed API.
- Notes on potential impact to existing consumers (if any).

---

## License

`ksb-commons-core` is part of the `ksb-commons` project and is licensed under the **MIT License**.

See the root [`LICENSE`](../LICENSE) file for the full license text.