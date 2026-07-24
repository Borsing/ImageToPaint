# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project state

`dev.borsing.imagetopaint:ImageToPaint` is a Quarkus 3.37.3 webapp (Maven, `packaging=quarkus`), generated via the official
`quarkus-maven-plugin` create goal with the `rest` and `rest-jackson` extensions. Beyond the generated scaffold
(`GreetingResource`, `GET /hello`, and its tests), a real feature is in progress: **image upload and
transformation**.

`POST /images` (`ImageResource`) accepts a multipart file upload (`@RestForm FileUpload`), validates it with the
custom `@ValidImage` Bean Validation constraint, converts it to grayscale via a pluggable `ImageFilter` strategy,
and returns the result as `image/png`. The image pipeline is built around real domain types (`Image`, `RGB`,
`ImageFilter`); only the `Item` CRUD layering (`domain.Item` and its in-memory repository/service/resource) is
leftover scaffold/template, unrelated to images.

## Build/run commands

Use the Maven wrapper (`mvnw` / `mvnw.cmd`), not a system `mvn`, so the pinned Maven version is used:

- Dev mode (live reload, Dev UI at `http://localhost:8080/q/dev/`): `./mvnw quarkus:dev`
- Run all tests: `./mvnw test`
- Run a single test: `./mvnw test -Dtest=GreetingResourceTest`
- Package (produces `target/quarkus-app/quarkus-run.jar`, not an über-jar — deps live in
  `target/quarkus-app/lib/`): `./mvnw package`
- Run the packaged jar: `java -jar target/quarkus-app/quarkus-run.jar`
- Über-jar: `./mvnw package -Dquarkus.package.jar.type=uber-jar`
- Native executable (needs GraalVM, or add `-Dquarkus.native.container-build=true` to build in a container):
  `./mvnw package -Dnative`

## Code quality tools

JaCoCo, Checkstyle (Google style), PMD, SpotBugs, and the Sonar Maven plugin are configured in `pom.xml` but
none of their `check`-style goals are bound to `mvnw verify` — they don't fail a normal build. Run them
explicitly (`./mvnw checkstyle:checkstyle`, `pmd:pmd`, `spotbugs:spotbugs`, `jacoco:report`); see README.md for
the full commands and for starting a local SonarQube server via `docker-compose.sonarqube.yml`. Sonar
consolidates the other tools' reports through the `sonar.java.*.reportPaths` properties in `pom.xml`.

## Architecture

- `packaging=quarkus` in `pom.xml` (not `jar`) — the `quarkus-maven-plugin` build step does the CDI/JAX-RS
  augmentation at build time; extension versions are managed via the imported `quarkus-bom`
  (`quarkus.platform.version` property), so add extensions as `io.quarkus:quarkus-*` dependencies without
  specifying a version.
- JAX-RS resources under `src/main/java/dev/borsing/imagetopaint` are auto-discovered as CDI beans — no manual
  registration.
  `quarkus-rest-jackson` is on the classpath, so `@Produces(MediaType.APPLICATION_JSON)` resources serialize POJOs
  directly. `quarkus-hibernate-validator` backs the custom `@ValidImage` constraint (see below).
- Integration tests (`*IT.java`, e.g. `GreetingResourceIT`, `ImageResourceIT`) run against the packaged artifact
  via `maven-failsafe-plugin` (`skipITs=true` by default, `mvnw verify` to run them); unit-style tests
  (`*Test.java`) run under `quarkus-junit` via `mvnw test` and boot the app in-process. `rest-assured` and
  `quarkus-junit5-mockito` are on the test classpath.
- `src/main/resources/application.properties` sets `quarkus.http.limits.max-body-size=10M`, capping the whole
  multipart request so an oversized upload is rejected before it reaches the resource.
- `src/main/docker/` has Dockerfiles for JVM, legacy-jar, native, and native-micro builds — not wired into any CI
  yet.

### Image pipeline

- `ImageResource` (`POST /images`, multipart in, `image/png` out) reads the uploaded file with `ImageIO.read`,
  delegates to `application.ImageFilteringFacade`, and writes the result back with `ImageIO.write`.
- `ValidImage`/`ValidImageValidator` (in `validation`) is a custom Bean Validation constraint applied to the
  `FileUpload` parameter. It never trusts the client-supplied `Content-Type` or filename extension — it sniffs
  the real format from magic bytes via `ImageIO.getImageReaders`, only accepts `png`/`jpeg`/`gif`, and rejects
  images over 40,000,000 pixels as a decompression-bomb guard (tiny file, huge decoded bitmap).
- `domain.Image` is an immutable record wrapping a `[height][width]` matrix of `domain.RGB` pixels; `RGB` is a
  small value record (`red`/`green`/`blue`, each validated to 0-255) — pixel access is by named component, not by
  array index. The `domain` package (including `domain.filter`) has no dependency on AWT/Quarkus — it's pure.
- `adapter.BufferedImageConverter` is the boundary between AWT's `BufferedImage` and the domain model: it
  converts to/from `Image`/`RGB[][]` (alpha dropped), using a single `getRGB`/`setRGB` call over the whole image
  rather than per-pixel calls. Any class named after an external type/library (AWT, ImageIO...) belongs in
  `adapter`, never in `domain`.
- `domain.filter.ImageFilter` is a strategy interface (`Image filter(Image image)`) for pure, non-mutating image
  transformations. `domain.filter.BlackAndWhiteFilter` is the current implementation, applying Rec. 709 luma
  (`0.2126R + 0.7152G + 0.0722B`) to produce grayscale. Filters are plain domain records rather than CDI beans,
  since future filters (sepia, thresholding, etc.) are expected to carry their own parameters as record
  components.
- `application.ImageFilteringFacade.filterToBlackAndWhite` is the use-case-level facade `ImageResource` talks
  to: it wires `BufferedImageConverter` and `BlackAndWhiteFilter` together, so the HTTP layer only ever sees
  `BufferedImage` in/out and stays unaware the domain model exists. It currently hardcodes which filter runs
  instead of accepting one as a parameter — a known rough edge to revisit once a second filter exists.
- `service.ItemService`/`repository.*Item*`/`domain.Item` are the unrelated `Item` CRUD scaffold — leftover
  template artifacts, not wired into the image pipeline.

## Java version

`pom.xml` sets `maven.compiler.release=25`, matching the JDK on this machine.
