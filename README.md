# ksb-commons

`ksb-commons` is a Kotlin multi-module library that provides shared building blocks for the `io.jrb.labs` ecosystem: core utilities, microservice foundations, client helpers, test support, and a dependency BOM to keep everything aligned.

The project is organized as a Gradle multi-project build and is licensed under the MIT License. See [`LICENSE`](./LICENSE) for details.

---

## Modules

This repository currently contains the following modules:

- **`ksb-commons-core`**  
  Framework-agnostic Kotlin utilities, immutable data containers, and common patterns.

- **`ksb-commons-ms-core`**  
  Shared abstractions for microservices (e.g., Spring Boot / WebFlux infrastructure, outcomes, error handling, metrics/logging hooks).

- **`ksb-commons-ms-client`**  
  Reusable client-side primitives for calling microservices (WebClient helpers, DTOs, error handling).

- **`ksb-commons-test`**  
  Shared test infrastructure (JUnit 5/Jupiter, AssertJ, MockK helpers, fixtures, test data builders).

- **`ksb-dependency-bom`**  
  A BOM (Bill of Materials) that pins versions of all `ksb-*` modules so consumers can import a single platform and avoid version drift.

---

## Getting Started (Local Development)

### Requirements

- JDK 21+ (recommended)
- Git
- Internet access for Gradle dependency resolution  
  (the Gradle wrapper is committed, so a local Gradle install is optional)

### Build & Test

From the repository root:

```bash
./gradlew clean build
```

Run tests only:

```bash
./gradlew test
```

Run tests for a specific module, e.g.:

```bash
./gradlew :ksb-commons-core:test
```

---

## Using ksb-commons in Other Projects

You can consume these modules either as published artifacts (when you start publishing) or via a local composite build.

> Replace `io.jrb.labs` and `<version>` with your actual group and version once the artifacts are published.

### Recommended: Using the BOM (Gradle – Kotlin DSL)

```kotlin
dependencies {
    // Align versions for all ksb-* artifacts
    implementation(platform("io.jrb.labs:ksb-dependency-bom:<version>"))

    // Pick modules as needed
    implementation("io.jrb.labs:ksb-commons-core")
    implementation("io.jrb.labs:ksb-commons-ms-core")
    implementation("io.jrb.labs:ksb-commons-ms-client")
    testImplementation("io.jrb.labs:ksb-commons-test")
}
```

Add your repositories (e.g., Maven Central or GitHub Packages):

```kotlin
repositories {
    mavenCentral()
    // Example: GitHub Packages
    // maven("https://maven.pkg.github.com/brulejr/ksb-commons") {
    //     credentials {
    //         username = findProperty("gpr.user") as String?
    //         password = findProperty("gpr.key") as String?
    //     }
    // }
}
```

### Using the BOM (Maven)

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.jrb.labs</groupId>
      <artifactId>ksb-dependency-bom</artifactId>
      <version>${ksb.version}</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>io.jrb.labs</groupId>
    <artifactId>ksb-commons-core</artifactId>
  </dependency>
  <dependency>
    <groupId>io.jrb.labs</groupId>
    <artifactId>ksb-commons-ms-core</artifactId>
  </dependency>
  <dependency>
    <groupId>io.jrb.labs</groupId>
    <artifactId>ksb-commons-ms-client</artifactId>
  </dependency>
  <dependency>
    <groupId>io.jrb.labs</groupId>
    <artifactId>ksb-commons-test</artifactId>
    <scope>test</scope>
  </dependency>
</dependencies>
```

### Using as a Composite Build (Local Source Dependency)

If you want to develop `ksb-commons` and a consumer project together, you can use Gradle’s composite builds.

In the consuming project’s `settings.gradle.kts`:

```kotlin
includeBuild("../ksb-commons")
```

Then use project dependencies:

```kotlin
dependencies {
    implementation(project(":ksb-commons-core"))
    implementation(project(":ksb-commons-ms-core"))
    implementation(project(":ksb-commons-ms-client"))
    testImplementation(project(":ksb-commons-test"))
}
```

---

## API Documentation (Javadoc / Dokka via GitHub Wiki)

Generated API docs for each module are intended to be stored in the **GitHub Wiki** of this repository. Proposed structure:

- Core: `ksb-commons-core` Javadoc  
  `https://github.com/brulejr/ksb-commons/wiki/ksb-commons-core-Javadoc`

- Microservice core: `ksb-commons-ms-core` Javadoc  
  `https://github.com/brulejr/ksb-commons/wiki/ksb-commons-ms-core-Javadoc`

- Microservice client: `ksb-commons-ms-client` Javadoc  
  `https://github.com/brulejr/ksb-commons/wiki/ksb-commons-ms-client-Javadoc`

- Test utilities: `ksb-commons-test` Javadoc  
  `https://github.com/brulejr/ksb-commons/wiki/ksb-commons-test-Javadoc`

- BOM: `ksb-dependency-bom` overview  
  `https://github.com/brulejr/ksb-commons/wiki/ksb-dependency-bom`

### Generating docs locally

Exact tasks will depend on how you configure documentation (Dokka vs Javadoc), but typical usage might look like:

```bash
# Example if Dokka is configured:
./gradlew :ksb-commons-core:dokkaHtml
./gradlew :ksb-commons-ms-core:dokkaHtml
./gradlew :ksb-commons-ms-client:dokkaHtml
./gradlew :ksb-commons-test:dokkaHtml
```

Then copy the generated HTML into the GitHub wiki clone.

---

## Development Notes & Strategy

### Design Goals

- **Commons-first architecture**  
  Extract reusable patterns and utilities from concrete microservices into this library, keeping individual services thinner.

- **Immutability and type safety**  
  Prefer Kotlin data classes, immutable collections, and sealed class outcomes to model behavior and errors clearly.

- **Framework boundaries**
    - `ksb-commons-core`: no framework-specific dependencies (pure Kotlin/JVM utilities).
    - `ksb-commons-ms-*`: microservice- and framework-oriented code (e.g., Spring Boot, WebFlux, HTTP clients, etc.).

- **Testability**  
  All public APIs should be covered by unit tests, with shared test helpers centralized in `ksb-commons-test`.

- **Version alignment**  
  Use `ksb-dependency-bom` to ensure that all `ksb-*` projects use consistent dependency versions.

### Contribution Guidelines (internal or external)

- Keep modules focused; avoid introducing service-specific business logic into `ksb-commons`.
- Before promoting a pattern into `ksb-commons`, confirm it is:
    - Used (or useful) in multiple projects.
    - General enough to avoid tight coupling to a single microservice.
- For larger refactors or new abstractions, open an issue to discuss the design before implementation.
- Add KDoc/Javadoc and usage examples for new public APIs.

---

## Issues, Bugs & Change Requests

All issues and change requests are tracked via **GitHub Issues** for this repository:

👉 <https://github.com/brulejr/ksb-commons/issues>

When filing an issue:

- Indicate which module(s) are affected (`ksb-commons-core`, `ksb-commons-ms-core`, etc.).
- Describe the problem or feature request clearly.
- Include:
    - Steps to reproduce (for bugs).
    - Expected vs actual behavior.
    - Relevant code snippets and configuration.
    - Version information (if artifacts are published).

For larger feature work:

1. Open an issue to capture the proposal.
2. Iterate on design via comments.
3. Once agreed, open a PR referencing the issue (e.g., “Fixes #42”).

---

## License

This project is licensed under the **MIT License**.  
See [`LICENSE`](./LICENSE) for the full text.