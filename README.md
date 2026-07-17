# ImageToPaint

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/ImageToPaint-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Code quality tools

Static analysis and coverage are wired into the Maven build but not bound to `mvnw verify` (so a style
violation never breaks a normal build). Run them on demand:

> **_NOTE:_** PMD's and SpotBugs's bundled analysis engines lag behind very recent JDKs. `pom.xml` pins
> `pmd-core`/`pmd-java` to 7.26.0 (overriding the plugin's bundled 7.7.0) and `spotbugs-maven-plugin` to
> 4.10.3.0 — without these overrides both tools fail outright against Java 25 (`maven.compiler.release=25`)
> bytecode with `Unsupported class file major version 69`.

```shell script
./mvnw test                 # also collects JaCoCo coverage data via the surefire/failsafe agent
./mvnw jacoco:report         # HTML+XML coverage report -> target/site/jacoco/
./mvnw checkstyle:checkstyle # Google style checks -> target/checkstyle-result.xml
./mvnw pmd:pmd                # PMD static analysis -> target/pmd.xml
./mvnw spotbugs:spotbugs     # SpotBugs bytecode analysis -> target/spotbugsXml.xml
```

### SonarQube

A local SonarQube server (Community Edition, backed by Postgres) can be started with:

```shell script
docker compose -f docker-compose.sonarqube.yml up -d
```

It's available at <http://localhost:9000> (default login `admin`/`admin`, changed on first login). Create a
project token there, then run the analysis — it picks up the Checkstyle/PMD/SpotBugs/JaCoCo reports above
automatically via the `sonar.java.*.reportPaths` properties in `pom.xml`:

```shell script
./mvnw verify checkstyle:checkstyle pmd:pmd spotbugs:spotbugs sonar:sonar -Dsonar.token=<your-token>
```

Never commit a Sonar token — always pass it via `-Dsonar.token` or the `SONAR_TOKEN` env var.

## Related Guides

- REST ([guide](https://quarkus.io/guides/rest)): Build RESTful web services and APIs using Jakarta REST (formerly JAX-RS)
- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
