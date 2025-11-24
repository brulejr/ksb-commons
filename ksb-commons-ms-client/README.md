# ksb-commons-ms-client

`ksb-commons-ms-client` provides shared **client-side primitives** for calling microservices in the `ksb-commons` / `io.jrb.labs` ecosystem.

Where `ksb-commons-ms-core` focuses on implementing services, this module focuses on **consuming** them:

- Web / HTTP client wrappers (e.g., around WebClient or other HTTP stacks).
- Shared DTOs and response wrappers for common patterns.
- Standardized error handling and retry behavior for outbound calls.
- Utilities to integrate with service discovery, configuration, or auth (where applicable).

Use this module in any JVM/Kotlin project (service or tool) that needs to talk to your microservices in a consistent and reusable way.

---

## Usage

### Gradle (Kotlin DSL)

With the BOM (recommended):

```kotlin
dependencies {
    implementation(platform("io.jrb.labs:ksb-dependency-bom:<version>"))
    implementation("io.jrb.labs:ksb-commons-ms-client")
}
```

Without the BOM:

```kotlin
dependencies {
    implementation("io.jrb.labs:ksb-commons-ms-client:<version>")
}
```

### Maven

```xml
<dependency>
  <groupId>io.jrb.labs</groupId>
  <artifactId>ksb-commons-ms-client</artifactId>
  <version>${ksb.version}</version>
</dependency>
```

> Replace `io.jrb.labs` and `<version>` / `${ksb.version}` with your actual published coordinates.

---

## What this module is for

Use `ksb-commons-ms-client` when you want to:

- Share common **client configuration**:
    - Timeouts, codecs, base URLs, headers.
    - Auth/token handling (if centralized).
- Standardize **error handling** on the client side:
    - Map HTTP errors to shared exception types or sealed outcomes.
    - Provide consistent logging and metrics for remote calls.
- Encourage **strong typing**:
    - Typed client interfaces for common patterns (CRUD, workflows, etc.).
    - Avoid scattering ad-hoc `WebClient`/`HttpClient` calls across codebases.

Typical examples of content here:

- A base client interface or abstract class for calling JSON APIs.
- Utility functions to:
    - Build a preconfigured HTTP client.
    - Parse responses into shared outcome types.
    - Instrument calls with metrics and logging.

Service-specific clients can **depend on** this module and add their own interfaces and DTOs in their respective repositories.

---

## Relationship to other modules

- **`ksb-commons-core`**  
  Framework-agnostic utilities and data abstractions. Provides underlying types and helpers but does not know about HTTP.

- **`ksb-commons-ms-core`**  
  Used by **service implementations** (controllers/handlers, workflows, error handling).

- **`ksb-commons-ms-client`** (this module)  
  Used by **service consumers** (other services, CLI tools, integration services) to call microservices in a standardized way.

---

## API Documentation

Generated API documentation for this module is intended to be published in the GitHub Wiki:

- **ksb-commons-ms-client Javadoc / Dokka**  
  https://github.com/brulejr/ksb-commons/wiki/ksb-commons-ms-client-Javadoc

If the page is not yet available, you can generate docs locally.

### Generating docs locally (example)

Exact tasks depend on your documentation setup (Dokka vs Javadoc), but typical usage might look like:

```bash
# Build and run tests
./gradlew :ksb-commons-ms-client:build

# Example if Dokka is enabled
./gradlew :ksb-commons-ms-client:dokkaHtml
```

Then copy the generated HTML into your GitHub wiki clone and push.

---

## Design Principles

### 1. Strongly-typed clients

- Prefer **interfaces** that clearly model remote service capabilities:
  ```kotlin
  interface DeviceServiceClient {
      suspend fun getDevice(id: UUID): ServiceClientOutcome<DeviceDto>
      suspend fun listDevices(): ServiceClientOutcome<List<DeviceDto>>
  }
  ```
- Hide raw HTTP details (URLs, headers, codecs) behind these interfaces.

### 2. Shared error and outcome handling

- Represent results using a shared outcome type (conceptually):

  ```kotlin
  sealed interface ServiceClientOutcome<out T> {
      data class Ok<T>(val value: T) : ServiceClientOutcome<T>
      data class NotFound(val message: String? = null) : ServiceClientOutcome<Nothing>
      data class BadRequest(val errors: List<String>) : ServiceClientOutcome<Nothing>
      data class Failure(val cause: Throwable) : ServiceClientOutcome<Nothing>
  }
  ```

- Provide mappers from HTTP status codes / response bodies into these outcomes.
- Avoid throwing exceptions for expected-flow conditions (like 404); use outcomes instead.

### 3. Centralized configuration

- Provide helpers for constructing clients with:
    - Shared timeouts.
    - Consistent JSON configuration.
    - Optional tracing / logging headers.
- Allow per-service customization via configuration properties where needed.

### 4. Observability for remote calls

- Encourage logging and metrics for:
    - Request latency.
    - Error rates by endpoint.
    - Timeouts and retries.
- Provide extension functions or wrappers that automatically record this data.

---

## Conceptual Example

A simplified conceptual outline of how a client built on this module might look:

```kotlin
class DefaultDeviceServiceClient(
    private val http: WebClient // or any HTTP client you standardize on
) : DeviceServiceClient {

    override suspend fun getDevice(id: UUID): ServiceClientOutcome<DeviceDto> {
        return try {
            val response = http
                .get()
                .uri("/devices/{id}", id)
                .retrieve()
                .toEntity<DeviceDto>()
                .awaitSingle()

            when (response.statusCode) {
                HttpStatus.OK -> ServiceClientOutcome.Ok(response.body!!)
                HttpStatus.NOT_FOUND -> ServiceClientOutcome.NotFound("Device $id not found")
                HttpStatus.BAD_REQUEST -> ServiceClientOutcome.BadRequest(listOf("Bad request"))
                else -> ServiceClientOutcome.Failure(
                    IllegalStateException("Unexpected status: ${response.statusCode}")
                )
            }
        } catch (ex: Exception) {
            ServiceClientOutcome.Failure(ex)
        }
    }
}
```

The actual implementation details can vary, but this illustrates the kind of structure `ksb-commons-ms-client` is designed to support.

---

## Development Notes

When contributing to `ksb-commons-ms-client`:

1. **Focus on shared patterns**  
   Only promote functionality here if it is useful across multiple services or tooling projects.

2. **Avoid service-specific logic**  
   Service-specific client code (endpoints, DTOs tied to a single service) should live in that service’s repository, depending on this module for shared building blocks.

3. **Document clearly**  
   For new public types and helpers, add KDoc/Javadoc that explains:
    - Intended usage.
    - Any assumptions about the underlying HTTP client or framework.
    - Configuration and extension points.

4. **Test thoroughly**
    - Use `ksb-commons-test` utilities where relevant.
    - Cover success, failure, and edge cases (e.g., timeouts, invalid payloads).

5. **Maintain compatibility**
    - Introduce breaking changes cautiously.
    - Coordinate versioning via `ksb-dependency-bom`.

---

## Issues & Change Requests

If you encounter a bug or want to propose a new shared client pattern:

👉 Open an issue in the main repository:  
https://github.com/brulejr/ksb-commons/issues

Please include:

- The version of `ksb-commons-ms-client` you are using.
- The microservice(s) you are calling.
- Existing code snippets that show the repeated pattern or problem.
- Any relevant framework details (e.g., Spring WebFlux, WebClient, coroutines/reactor).

---

## License

`ksb-commons-ms-client` is part of the `ksb-commons` project and is licensed under the **MIT License**.

See the root [`LICENSE`](../LICENSE) file for the full license text.