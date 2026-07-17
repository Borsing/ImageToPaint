package org.example.resource;

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
    void uploadReturnsTheProcessedImage(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("valid.png");
        BufferedImage image = new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", file.toFile());

        byte[] responseBytes =
                given()
                        .multiPart("file", file.toFile(), "image/png")
                        .when().post("/images")
                        .then()
                        .statusCode(200)
                        .contentType("image/png")
                        .extract().asByteArray();

        BufferedImage responseImage = ImageIO.read(new ByteArrayInputStream(responseBytes));
        assertEquals(4, responseImage.getWidth());
        assertEquals(3, responseImage.getHeight());
    }
}
