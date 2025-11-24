# ksb-commons-test

`ksb-commons-test` provides reusable test utilities for the `ksb-commons` ecosystem and other `io.jrb.labs` projects.

It is intended to standardize and simplify testing across services and libraries by collecting common JUnit, assertion, and mocking helpers in one place.

Typical content includes:

- JUnit 5 (Jupiter) extensions and base classes.
- AssertJ helpers and fluent assertion utilities.
- MockK helpers for Kotlin-friendly mocking.
- Test data builders and fixtures for shared models.
- Utility functions to support Spring / ApplicationContext-based tests (e.g., `ApplicationContextRunner` helpers), where applicable.

> This module is **test-only**: it is meant to be added as a `testImplementation` (or Maven `test` scope) dependency.

---

## Usage

### Gradle (Kotlin DSL)

With the BOM (recommended):

```kotlin
dependencies {
    testImplementation(platform("io.jrb.labs:ksb-dependency-bom:<version>"))
    testImplementation("io.jrb.labs:ksb-commons-test")
}
```

Without the BOM:

```kotlin
dependencies {
    testImplementation("io.jrb.labs:ksb-commons-test:<version>")
}
```

### Maven

```xml
<dependency>
  <groupId>io.jrb.labs</groupId>
  <artifactId>ksb-commons-test</artifactId>
  <version>${ksb.version}</version>
  <scope>test</scope>
</dependency>
```

> Replace `io.jrb.labs` and `<version>` / `${ksb.version}` with the actual coordinates you publish.

---

## What this module is for

Use `ksb-commons-test` when you want to:

- Share common test setup across multiple services and libraries.
- Avoid duplicating test helpers (e.g., for `GenericData`, facades, or sealed outcome assertions).
- Keep a consistent testing stack and patterns across the `io.jrb.labs` ecosystem.

Examples of what might live here:

- **JUnit 5 extensions**  
  E.g. extensions that automatically configure logging, load common configuration, or set up shared resources.

- **AssertJ utilities**  
  Custom `Assertions` for domain types (e.g., helpers that deeply compare containers, outcomes, or change trees).

- **MockK helpers**  
  Shortcuts for common mocking scenarios, especially in coroutine or reactive contexts.

- **Spring test helpers** (if used)  
  Functions or base classes that simplify testing Spring Boot components, `ApplicationContextRunner` scenarios, etc.

---

## API Documentation

Generated API documentation for this module is intended to be published in the GitHub Wiki:

- **ksb-commons-test Javadoc / Dokka**  
  https://github.com/brulejr/ksb-commons/wiki/ksb-commons-test-Javadoc

If the page is not yet available, you can generate docs locally.

### Generating docs locally (example)

Exact tasks depend on your documentation setup (Dokka vs Javadoc), but typical usage might look like:

```bash
# Run tests and build artifacts
./gradlew :ksb-commons-test:build

# Example if Dokka is used
./gradlew :ksb-commons-test:dokkaHtml
```

Then copy the generated HTML into your local clone of the GitHub wiki and push.

---

## Development Notes & Conventions

- This module should **not** contain production code; it is only for testing utilities.
- Keep dependencies appropriate for test scope (JUnit, AssertJ, MockK, Spring test libraries, etc.).
- When adding new helpers:
    - Ensure they are **generic and reusable**, not tied to a single service.
    - Prefer small, focused utilities over large, monolithic base classes.
    - Provide KDoc / Javadoc with small usage examples where possible.
- Try to avoid introducing heavy dependencies that are only useful to one project; those should stay local to that project’s tests.

---

## How to add a new shared test utility

1. Identify a repeated pattern across two or more projects (e.g., the same assertion or base test class).
2. Extract the shared logic into `ksb-commons-test`.
3. Add KDoc / Javadoc and at least one self-test to validate the helper itself.
4. Update the affected projects to use the new helper instead of duplicated code.

---

## Issues & Change Requests

If you encounter a bug in `ksb-commons-test` or have ideas for new shared utilities:

👉 Open an issue in the main repository:  
https://github.com/brulejr/ksb-commons/issues

Please include:

- The version of `ksb-commons-test` you are using.
- A short description of the test pattern or problem.
- Example test code (before/after, if proposing a new helper).
- Any external libraries involved (e.g., Spring Boot, WebFlux, MongoDB, etc.).

---

## License

`ksb-commons-test` is part of the `ksb-commons` project and is licensed under the **MIT License**.

See the root [`LICENSE`](../LICENSE) for the full license text.