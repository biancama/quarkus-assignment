package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jboss.resteasy.reactive.RestResponse.StatusCode.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StoreEndpointTest {
  final private static String storeName = "storeName"; //TODO Use of Commons Lang to generate random String
  final private static String storeNewName = "storeNewName";
  private static String storeId;

  @Test
  @Order(1)
  public void testGivenABunchOfStoresWhenFetchThenTheyAreReturnedOrderedByName() {
    final String path = "store";

    List<Store> stores = given()
        .when()
        .get(path)
        .then()
        .statusCode(OK)
            .extract().body().as(new TypeRef<>() {
            });
    assertTrue(stores.size() > 0);
    List<String> actualStoreNames = stores.stream().map(s -> s.name).collect(Collectors.toUnmodifiableList());
    List<String> expectedStoreNamesShouldBeOrderedByName = actualStoreNames.stream().sorted().collect(Collectors.toUnmodifiableList());
    assertThat(actualStoreNames, is(expectedStoreNamesShouldBeOrderedByName));
  }
  @Test
  @Order(1)
  public void testGivenABunchOfStoresWhenFetchOneByIdThenItIsReturned() {
    final String path = "store/1";

    given()
            .when()
            .get(path)
            .then()
            .statusCode(OK)
            .body("id", is(1)).body("name", is("TONSTAD")).body("quantityProductsInStock", is(10));

  }
  @Test
  @Order(1)
  public void testGETValidation() {
    final String path = "store/100";

    given()
            .when()
            .get(path)
            .then()
            .statusCode(NOT_FOUND);

  }
  @Test
  @Order(1)
  public void testPOSTValidation() {
    final String path = "store";
    final var store = new Store(storeName);
    store.id = 100L;

    given()
            .body(store)
            .contentType(ContentType.JSON)
            .when()
            .post(path)
            .then()
            .statusCode(422);

  }
  @Test
  @Order(1)
  public void testPUTValidation() {
    final String path = "store/1";
    final var store = new Store();
    store.quantityProductsInStock = 10;
    given().body(store)
            .contentType(ContentType.JSON)
            .when()
            .put(path)
            .then()
            .statusCode(422);

    final String notFoundPath = "store/100";
    final var notFoundStore = new Store(storeNewName);
    notFoundStore.quantityProductsInStock = 10;
    given().body(notFoundStore)
            .contentType(ContentType.JSON)
            .when()
            .put(notFoundPath)
            .then()
            .statusCode(NOT_FOUND);
  }

  @Test
  @Order(1)
  public void testPATCHValidation() {
    final String notFoundPath = "store/100";
    final var notFoundStore = new Store();

    given().body(notFoundStore)
            .contentType(ContentType.JSON)
            .when()
            .patch(notFoundPath)
            .then()
            .statusCode(NOT_FOUND);
  }
  @Test
  @Order(1)
  public void testDELETEValidation() {
    final String notFoundPath = "store/100";
    final var notFoundStore = new Store();

    given().body(notFoundStore)
            .contentType(ContentType.JSON)
            .when()
            .delete(notFoundPath)
            .then()
            .statusCode(NOT_FOUND);
  }
  @Test
  @Order(2)
  public void testGivenAStoreWhenPersistedThenItIsSaved() {
    final String path = "store";
    final var store = new Store(storeName);

    String location = given()
            .body(store)
            .contentType(ContentType.JSON)
            .when()
            .post(path)
            .then()
            .statusCode(CREATED)
            .extract().header("Location");
    // Extracts the Location and stores the id
    assertTrue(location.contains("/store"));
    String[] segments = location.split("/");
    storeId = segments[segments.length - 1];
    assertNotNull(storeId);
  }

  @Test
  @Order(3)
  public void testGivenAStorePreviouslyPersistedThenItIsRetrieved() {

    final String path = "store/" + storeId;

    given()
            .when()
            .get(path)
            .then()
            .statusCode(OK)
            .body("id", is(Integer.valueOf(storeId))).body("name", is(storeName)).body("quantityProductsInStock", is(0));
  }
  @Test
  @Order(4)
  public void testGivenAStorePreviouslyPersistedWhenItIsChangedByPUThenItIsCompletelyChanged() {
    final String path = "store/" + storeId;
    final var store = new Store(storeNewName);
    store.quantityProductsInStock = 10;
    given().body(store)
            .contentType(ContentType.JSON)
            .when()
            .put(path)
            .then()
            .statusCode(OK)
            .body("id", is(Integer.valueOf(storeId))).body("name", is(storeNewName)).body("quantityProductsInStock", is(10));
  }
  @Test
  @Order(5)
  public void testGivenAStorePreviouslyPersistedWhenItIsChangedByPATCHThenItIsPartiallyChanged() {
    final String path = "store/" + storeId;
    final var store = new Store();
    store.quantityProductsInStock = 1;
    given().body(store)
            .contentType(ContentType.JSON)
            .when()
            .patch(path)
            .then()
            .statusCode(OK)
            .body("id", is(Integer.valueOf(storeId))).body("name", is(storeNewName)).body("quantityProductsInStock", is(1));
  }
  @Test
  @Order(6)
  public void testGivenAStorePreviouslyPersistedWhenItIsDeletedThenItIsDeletedFromStore() {
    final String path = "store/" + storeId;
    given()
            .when()
            .delete(path)
            .then()
            .statusCode(NO_CONTENT);
  }
  @Test
  @Order(7)
  public void testGivenAStorePreviouslyDeletedWhenItIsFetchedThenItIsNotFoundFromStore() {
    final String path = "store/" + storeId;
    given()
            .when()
            .get(path)
            .then()
            .statusCode(NOT_FOUND);
  }
}
