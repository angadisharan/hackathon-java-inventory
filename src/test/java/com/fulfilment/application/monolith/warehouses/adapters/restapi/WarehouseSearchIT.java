package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for GET /warehouse/search (filter, sort, pagination).
 * Uses seed data from import.sql (MWH.001, MWH.012, MWH.023).
 */
@QuarkusTest
public class WarehouseSearchIT {

  private static final String SEARCH_PATH = "warehouse/search";

  @Test
  public void searchWithNoParamsReturnsActiveWarehouses() {
    given()
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(3))
        .body(containsString("MWH.001"), containsString("MWH.012"), containsString("MWH.023"));
  }

  @Test
  public void searchFilterByLocationReturnsMatchingOnly() {
    given()
        .queryParam("location", "AMSTERDAM-001")
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(1))
        .body(containsString("AMSTERDAM-001"))
        .body(not(containsString("ZWOLLE-001")));
  }

  @Test
  public void searchFilterByCapacityRange() {
    given()
        .queryParam("minCapacity", 40)
        .queryParam("maxCapacity", 60)
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(1));
  }

  @Test
  public void searchWithPaginationReturnsPageSize() {
    given()
        .queryParam("page", 0)
        .queryParam("pageSize", 2)
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("size()", lessThanOrEqualTo(2));
  }

  @Test
  public void searchWithSortByCapacity() {
    given()
        .queryParam("sortBy", "capacity")
        .queryParam("sortOrder", "desc")
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(1));
  }
}
