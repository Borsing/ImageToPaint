# ImageToPaint

A Quarkus webapp that uploads an image and returns a transformed version of it: grayscale, or a
posterized "paint by numbers" effect obtained by clustering colors with k-means in CIELAB space.

## Endpoints

All three endpoints accept a `multipart/form-data` POST with a `file` part (PNG/JPEG/GIF, magic-byte
sniffed — content-type headers and file extensions are never trusted) and return `image/png`.

| Endpoint            | Extra form fields                          | Effect                                                              |
|----------------------|--------------------------------------------|----------------------------------------------------------------------|
| `POST /images/grayscale` | —                                        | Rec. 709 luma grayscale                                              |
| `POST /images/paint`     | `numberOfColors` (`@Min(1)`, default `6`)  | Reduces the image to `numberOfColors` colors via k-means in CIELAB space |
| `POST /images/values`    | `numberOfValues` (`@Min(1)`, default `6`)  | Grayscale, then quantized to `numberOfValues` shades the same way    |

```shell script
curl -F "file=@photo.png" http://localhost:8080/images/grayscale -o grayscale.png
curl -F "file=@photo.png" -F "numberOfColors=8" http://localhost:8080/images/paint -o paint.png
curl -F "file=@photo.png" -F "numberOfValues=4" http://localhost:8080/images/values -o values.png
```

Uploads are also rejected (before any decoding) if they exceed 40,000,000 declared pixels (a
decompression-bomb guard) or the 10 MB request body cap — see [Configuration](#configuration).

## Architecture

The codebase (`dev.borsing.imagetopaint`) is layered so the image-processing logic never depends on
HTTP or on `java.awt`/`javax.imageio`:

- **`domain`** — pure business logic, no external dependencies: `Image`/`domain.color.Rgb` (an
  immutable pixel matrix), `domain.color.Cielab`/`RgbCielabConverter` (color-space conversion), and
  `domain.filter` (the `ImageFilter` strategy interface plus `GrayScaleFilter`, `PaintingFilter`,
  `ValueScaleFilter`).
- **`adapter`** — the only layer allowed to touch external libraries: `ImageCodec` wraps
  `javax.imageio` (decode/encode/format-sniffing), `BufferedImageConverter` converts between AWT's
  `BufferedImage` and the domain `Image`.
- **`usecase`** — `ImageFilteringFacade` orchestrates one use case per method (`filterToGrayScale`,
  `filterToPaint`, `filterToValues`), wiring the adapter and a domain filter together. It only ever
  sees `BufferedImage` in/out, so the HTTP layer stays unaware the domain model exists.
- **`resource`** — `ImageResource`, a thin JAX-RS controller: decode via `ImageCodec`, delegate to
  `ImageFilteringFacade`, re-encode via `ImageCodec`.
- **`validation`** — `@ValidImage`/`ValidImageValidator`, a custom Bean Validation constraint applied
  to the upload; sniffs the real format via `ImageCodec` rather than trusting client-supplied metadata.

## Configuration

Set in `src/main/resources/application.properties`:

| Property | Default | Purpose |
|---|---|---|
| `quarkus.http.limits.max-body-size` | `10M` | Caps the whole multipart request body |
| `imagetopaint.image.allowed-formats` | `png,jpeg,gif` | Formats accepted by `@ValidImage` after magic-byte sniffing |
| `imagetopaint.image.max-pixels` | `40000000` | Decompression-bomb guard: max declared pixel count |

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