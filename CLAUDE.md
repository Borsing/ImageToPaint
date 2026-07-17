# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project state

`org.example:ImageToPaint` is a Quarkus 3.37.3 webapp backbone (Maven, `packaging=quarkus`), generated via the
official `quarkus-maven-plugin` create goal with the `rest` and `rest-jackson` extensions. Beyond the generated
scaffold — one sample JAX-RS resource (`GreetingResource`, `GET /hello`) and its tests — there's a layered
skeleton (`domain` / `repository` / `service` / `resource`) built around an example `Item` object with an
in-memory repository, meant as a template to copy for real business objects.

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
- JAX-RS resources under `src/main/java/org/example` are auto-discovered as CDI beans — no manual registration.
  `quarkus-rest-jackson` is on the classpath, so `@Produces(MediaType.APPLICATION_JSON)` resources serialize POJOs
  directly.
- Integration tests (`*IT.java`, e.g. `GreetingResourceIT`) run against the packaged artifact via
  `maven-failsafe-plugin` (`skipITs=true` by default, `mvnw verify` to run them); unit-style tests (`*Test.java`)
  run under `quarkus-junit` via `mvnw test` and boot the app in-process.
- `src/main/resources/application.properties` is the Quarkus config file (currently empty).
- `src/main/docker/` has Dockerfiles for JVM, legacy-jar, native, and native-micro builds — not wired into any CI
  yet.

## Java version

`pom.xml` sets `maven.compiler.release=25`, matching the JDK on this machine.
