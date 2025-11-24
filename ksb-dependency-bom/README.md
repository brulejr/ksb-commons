# ksb-dependency-bom

`ksb-dependency-bom` is the Bill of Materials (BOM) module for the `ksb-commons` ecosystem.

It centralizes the versions of all `ksb-*` artifacts so that consumers can import a single platform and get consistent, compatible versions across multiple modules without having to repeat version numbers.

Typical artifacts managed by this BOM include:

- `ksb-commons-core`
- `ksb-commons-ms-core`
- `ksb-commons-ms-client`
- `ksb-commons-test`

> **Note:** Replace the group ID and version placeholders below with your actual coordinates once you publish to a Maven repository.

---

## Why use this BOM?

When you depend on multiple `ksb-*` modules, managing individual versions quickly becomes error‑prone. Using the BOM you can:

- Align all `ksb-*` dependency versions in one place.
- Upgrade or roll back by changing a **single** version.
- Avoid subtle incompatibilities caused by mixing versions.

---

## Usage

### Gradle (Kotlin DSL)

In your Gradle build, import the platform and then depend on the artifacts you need without specifying versions:

```kotlin
dependencies {
    // Import the ksb dependency BOM
    implementation(platform("io.jrb.labs:ksb-dependency-bom:<version>"))

    // Now you can use ksb artifacts without explicit versions
    implementation("io.jrb.labs:ksb-commons-core")
    implementation("io.jrb.labs:ksb-commons-ms-core")
    implementation("io.jrb.labs:ksb-commons-ms-client")
    testImplementation("io.jrb.labs:ksb-commons-test")
}
```

Configure repositories as appropriate (e.g., Maven Central, GitHub Packages):

```kotlin
repositories {
    mavenCentral()
    // Example for GitHub Packages (uncomment and configure as needed):
    // maven("https://maven.pkg.github.com/brulejr/ksb-commons") {
    //     credentials {
    //         username = findProperty("gpr.user") as String?
    //         password = findProperty("gpr.key") as String?
    //     }
    // }
}
```

### Maven

Import the BOM using `<dependencyManagement>` and then declare dependencies without versions:

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
```

Then in your `<dependencies>` section:

```xml
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

---

## Versioning Strategy

The `ksb-dependency-bom` follows the same versioning as the rest of the `ksb-commons` ecosystem.

- **Major** version changes may reflect:
    - Breaking changes in underlying `ksb-*` modules.
    - Significant upgrades of core dependencies (e.g., Spring Boot, Kotlin, JDK) that may require consumer changes.
- **Minor** versions typically add new modules or dependency updates in a backward‑compatible way.
- **Patch** versions represent small bugfixes or non‑breaking dependency bumps.

When in doubt:

- Check the release notes/changelog for this repository.
- Treat a bump in the BOM version like a coordinated upgrade of all `ksb-*` modules.

---

## API Documentation

The BOM itself has no runtime API, but its role and the set of managed modules are documented in the GitHub Wiki:

- **BOM overview:**  
  https://github.com/brulejr/ksb-commons/wiki/ksb-dependency-bom

You can also inspect the generated POM for the BOM artifact in your local cache or Maven repository browser to see the exact versions it manages.

---

## Development Notes

- This module is a pure BOM/POM module; it should **not** contain any production code.
- When you add or remove modules in the `ksb-commons` project, update this BOM to keep it authoritative.
- Keep dependency versions consistent across modules by always updating them through this BOM first and then aligning each module’s build file as needed.

Typical changes include:

- Adding new `<dependency>` entries in the BOM for new `ksb-*` artifacts.
- Updating versions of shared third‑party libraries (if you choose to manage them centrally here).

---

## Issues & Change Requests

For bugs, questions, or requests related to `ksb-dependency-bom` (e.g., missing modules, version alignment, or upgrade plans), please open an issue in the main repository:

👉 https://github.com/brulejr/ksb-commons/issues

When filing an issue, include:

- The version of `ksb-dependency-bom` you’re using.
- Example snippets of your Gradle or Maven configuration.
- Any error messages or conflicts you’re seeing.

---

## License

This module is part of the `ksb-commons` project and is licensed under the **MIT License**.

See the root [`LICENSE`](../LICENSE) file for details.