package dev.borsing.imagetopaint.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
class ItemResourceTest {

    @Test
    void testCreateFindAndDeleteItem() {
        String id =
                given()
                        .contentType(ContentType.JSON)
                        .body("{\"name\":\"Starry Night\"}")
                        .when().post("/items")
                        .then()
                        .statusCode(201)
                        .body("name", is("Starry Night"))
                        .body("id", notNullValue())
                        .extract().path("id");

        given()
                .when().get("/items/" + id)
                .then()
                .statusCode(200)
                .body("name", is("Starry Night"));

        given()
                .when().get("/items")
                .then()
                .statusCode(200)
                .body("size()", is(1));

        given()
                .when().delete("/items/" + id)
                .then()
                .statusCode(204);

        given()
                .when().get("/items/" + id)
                .then()
                .statusCode(404);
    }
}
