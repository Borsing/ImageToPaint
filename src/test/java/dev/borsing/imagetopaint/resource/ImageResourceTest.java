package dev.borsing.imagetopaint.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class ImageResourceTest {

    @Test
    void grayScaleReturnsTheProcessedImage(@TempDir Path tempDir) throws IOException {
        Path file = writePng(tempDir, 4, 3);

        byte[] responseBytes =
                given()
                        .multiPart("file", file.toFile(), "image/png")
                        .when().post("/images/grayscale")
                        .then()
                        .statusCode(200)
                        .contentType("image/png")
                        .extract().asByteArray();

        BufferedImage responseImage = ImageIO.read(new ByteArrayInputStream(responseBytes));
        assertEquals(4, responseImage.getWidth());
        assertEquals(3, responseImage.getHeight());
    }

    @Test
    void paintReturnsTheProcessedImage(@TempDir Path tempDir) throws IOException {
        Path file = writePng(tempDir, 4, 3);

        byte[] responseBytes =
                given()
                        .multiPart("file", file.toFile(), "image/png")
                        .multiPart("numberOfColors", "10")
                        .when().post("/images/paint")
                        .then()
                        .statusCode(200)
                        .contentType("image/png")
                        .extract().asByteArray();

        BufferedImage responseImage = ImageIO.read(new ByteArrayInputStream(responseBytes));
        assertEquals(4, responseImage.getWidth());
        assertEquals(3, responseImage.getHeight());
    }

    @Test
    void paintUsesDefaultNumberOfColorsWhenOmitted(@TempDir Path tempDir) throws IOException {
        Path file = writePng(tempDir, 4, 3);

        given()
                .multiPart("file", file.toFile(), "image/png")
                .when().post("/images/paint")
                .then()
                .statusCode(200)
                .contentType("image/png");
    }

    @Test
    void paintRejectsNumberOfColorsBelowOne(@TempDir Path tempDir) throws IOException {
        Path file = writePng(tempDir, 4, 3);

        given()
                .multiPart("file", file.toFile(), "image/png")
                .multiPart("numberOfColors", "0")
                .when().post("/images/paint")
                .then()
                .statusCode(400);
    }

    private static Path writePng(Path dir, int width, int height) throws IOException {
        Path file = dir.resolve("valid.png");
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", file.toFile());
        return file;
    }
}