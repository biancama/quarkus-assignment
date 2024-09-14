package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.is;
import static org.jboss.resteasy.reactive.RestResponse.StatusCode.CREATED;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.warehouse.api.beans.Warehouse;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusIntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WarehouseEndpointIT {

  @Test
  @Order(1)
  public void testSimpleListWarehouses() {

    final String path = "warehouse";

    // List all, should have all 3 products the database has initially:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(containsString("MWH.001"), containsString("MWH.012"), containsString("MWH.023"));
  }

  @Test
  @Order(1)
  public void testSimpleFindOfAWarehouse() {

    final String path = "warehouse/MWH.001";

    given()
            .when()
            .get(path)
            .then()
            .statusCode(200)
            .body("businessUnitCode", is("MWH.001")).body("location", is("ZWOLLE-001")).body("stock", is(10));
  }

  @Test
  public void testSimpleFindOfAWarehouseNotFound() {

    final String path = "warehouse/FAKE_BU";

    given()
            .when()
            .get(path)
            .then()
            .statusCode(404);
  }

  @Test
  public void testSimpleCreateWarehouse() {

    final String path = "warehouse";

    final var warehouse = new Warehouse();
    warehouse.setBusinessUnitCode("NEW_BU");
    warehouse.setLocation("EINDHOVEN-001");
    warehouse.setStock(10);
    warehouse.setCapacity(50);

    String warehouseLocation = given()
            .body(warehouse)
            .contentType(ContentType.JSON)
            .when()
            .post(path)
            .then()
            .statusCode(CREATED)
            .extract().header("Location");

    assertTrue(warehouseLocation.contains("/warehouse"));
    String[] segments = warehouseLocation.split("/");
    var warehouseId = segments[segments.length - 1];
    assertNotNull(warehouseLocation);
    assertTrue(warehouseId.equals("NEW_BU"));
  }

  @Test
  @Order(2)
  public void testOneValidationFailingWhenCreateWarehouse() {

    final String path = "warehouse";

    final var warehouse = new Warehouse();
    warehouse.setBusinessUnitCode("MWH.001");
    warehouse.setLocation("EINDHOVEN-001");
    warehouse.setStock(10);
    warehouse.setCapacity(50);

    given()
            .body(warehouse)
            .contentType(ContentType.JSON)
            .when()
            .post(path)
            .then()
            .statusCode(400)
            .body("error", is("Warehouse already exists"));
  }

  @Test
  public void testSimpleCheckingArchivingWarehouses() {

    // Uncomment the following lines to test the WarehouseResourceImpl implementation

     final String path = "warehouse";

     // List all, should have all 3 products the database has initially:
     given()
         .when()
         .get(path)
         .then()
         .statusCode(200)
         .body(
             containsString("MWH.001"),
             containsString("MWH.012"),
             containsString("MWH.023"),
             containsString("ZWOLLE-001"),
             containsString("AMSTERDAM-001"),
             containsString("TILBURG-001"));

     // Archive the ZWOLLE-001:
     given().when().delete(path + "/MWH.001").then().statusCode(204);

     // List all, ZWOLLE-001 should be missing now:
     given()
         .when()
         .get(path)
         .then()
         .statusCode(200)
         .body(
             not(containsString("ZWOLLE-001")),
             containsString("AMSTERDAM-001"),
             containsString("TILBURG-001"));
  }


}
