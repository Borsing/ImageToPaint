package org.example.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class ImageResourceTest {

    @Test
    void uploadAcceptsAValidPngImage(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("valid.png");
        BufferedImage image = new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", file.toFile());

        given()
                .multiPart("file", file.toFile(), "image/png")
                .when().post("/images")
                .then()
                .statusCode(200)
                .body("filename", is("valid.png"))
                .body("width", is(4))
                .body("height", is(3));
    }
}
