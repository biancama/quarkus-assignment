package com.fulfilment.application.monolith.warehouses.repository;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class WarehouseRepositoryTest {

    @Inject
    private WarehouseRepository warehouseRepository;

    @Test
    @TestTransaction
    public void testFindByBusinessUnitCode() {
        var actualWarehouse = warehouseRepository.findByBusinessUnitCode("MWH.001");
        assertThat(actualWarehouse, notNullValue());

        var notExistingWarehouse = warehouseRepository.findByBusinessUnitCode("FAKE_BUSINESS_UNIT_CODE");
        assertThat(notExistingWarehouse, nullValue());
    }
    @Test
    @TestTransaction
    public void testCreateWarehouse() {
       var warehouse = new Warehouse();
       final String businessUnitCode =  "NEW_BU";
       warehouse.businessUnitCode =  businessUnitCode;
       warehouse.stock = 10;
       warehouse.capacity= 20;
       warehouse.location = "Amsterdam";
       warehouse.archivedAt = LocalDateTime.now();

       warehouseRepository.create(warehouse);

        var actualWarehouse = warehouseRepository.findByBusinessUnitCode(businessUnitCode);
        assertThat(actualWarehouse, notNullValue());
        assertThat(actualWarehouse.stock, is(10));
        assertThat(actualWarehouse.archivedAt, nullValue());

    }
}
