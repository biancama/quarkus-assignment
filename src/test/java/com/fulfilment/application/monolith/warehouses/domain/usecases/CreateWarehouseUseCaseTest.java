package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class CreateWarehouseUseCaseTest {
    @Mock
    private WarehouseStore warehouseStore;
    @Mock
    private LocationResolver locationResolver;

    @InjectMocks
    private CreateWarehouseUseCase createWarehouseUseCase;

    @Test
    public void givenWarehouse_whenAllValidationSucceed_thenReturnWarehouse() {
        var warehouse = getWarehouse();

        List<Warehouse> warehousesPerLocation = getWarehouses();

        var location = new Location("AMS", 3, 1000);

        when(locationResolver.resolveByIdentifier(warehouse.location)).thenReturn(location);
        when(warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode)).thenReturn(null);
        when(warehouseStore.getAllByLocation(warehouse.location)).thenReturn(warehousesPerLocation);
        when(warehouseStore.create(warehouse)).thenReturn(warehouse);

        var warehouseCreated = createWarehouseUseCase.create(warehouse);
        assertTrue(warehouseCreated.isRight(), "Validation exception");
        assertThat(warehouseCreated.get(), equalTo((warehouse)));
    }
    @Test
    public void givenWarehouse_whenBusinessUnitAlreadyExist_thenReturnAValidationError() {
        var warehouse = getWarehouse();

        var location = new Location("AMS", 3, 400);

        when(locationResolver.resolveByIdentifier(warehouse.location)).thenReturn(location);
        when(warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode)).thenReturn(warehouse);

        var warehouseCreated = createWarehouseUseCase.create(warehouse);
        assertTrue(warehouseCreated.isLeft(), "Validation exception");
        assertThat(warehouseCreated.getLeft(), equalTo(("Warehouse already exists")));
    }
    @Test
    public void givenWarehouse_whenMaxNumberOfWarehousesPerLocationIsAlreadyReached_thenReturnAValidationError() {
        var warehouse = getWarehouse();

        List<Warehouse> warehousesPerLocation = getWarehouses();

        var location = new Location("AMS", 2, 400);

        when(locationResolver.resolveByIdentifier(warehouse.location)).thenReturn(location);
        when(warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode)).thenReturn(null);
        when(warehouseStore.getAllByLocation(warehouse.location)).thenReturn(warehousesPerLocation);

        var warehouseCreated = createWarehouseUseCase.create(warehouse);
        assertTrue(warehouseCreated.isLeft(), "Validation exception");
        assertThat(warehouseCreated.getLeft(), equalTo(("Location AMS has reached max number of warehouses")));
    }

    @Test
    public void givenWarehouse_whenMaxCapacityOfWarehousesPerLocationIsAlreadyReached_thenReturnAValidationError() {
        var warehouse = getWarehouse();
        warehouse.capacity = 0;
        warehouse.stock = 0;
        List<Warehouse> warehousesPerLocation = getWarehouses();

        var location = new Location("AMS", 3, 200);

        when(locationResolver.resolveByIdentifier(warehouse.location)).thenReturn(location);
        when(warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode)).thenReturn(null);
        when(warehouseStore.getAllByLocation(warehouse.location)).thenReturn(warehousesPerLocation);

        var warehouseCreated = createWarehouseUseCase.create(warehouse);
        assertTrue(warehouseCreated.isLeft(), "Validation exception");
        assertThat(warehouseCreated.getLeft(), equalTo(("Location AMS has reached max capacity")));
    }

    @Test
    public void givenWarehouse_whenMaxCapacityOfWarehousesPerLocationIsNotReachedButItWillBeReachedWithTheNewWarehosue_thenReturnAValidationError() {
        var warehouse = getWarehouse();

        List<Warehouse> warehousesPerLocation = getWarehouses();

        var location = new Location("AMS", 3, 350);

        when(locationResolver.resolveByIdentifier(warehouse.location)).thenReturn(location);
        when(warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode)).thenReturn(null);
        when(warehouseStore.getAllByLocation(warehouse.location)).thenReturn(warehousesPerLocation);

        var warehouseCreated = createWarehouseUseCase.create(warehouse);
        assertTrue(warehouseCreated.isLeft(), "Validation exception");
        assertThat(warehouseCreated.getLeft(), equalTo(("Location AMS has reached max capacity")));
    }


    @Test
    public void givenWarehouse_whenStockIsGreaterThanCapacity_thenReturnAValidationError() {
        var warehouse = getWarehouse();
        warehouse.stock = warehouse.capacity + 1;
        var warehouseCreated = createWarehouseUseCase.create(warehouse);
        assertTrue(warehouseCreated.isLeft(), "Validation exception");
        assertThat(warehouseCreated.getLeft(), equalTo(("Cannot create a warehouse with stock greater than capacity")));
    }

    private static List<Warehouse> getWarehouses() {
        var warehouse01 = new Warehouse();
        warehouse01.capacity = 100;
        var warehouse02 = new Warehouse();
        warehouse02.capacity = 200;
        List<Warehouse> warehousesPerLocation = List.of(warehouse01, warehouse02);
        return warehousesPerLocation;
    }

    private static Warehouse getWarehouse() {
        var warehouse = new Warehouse();
        warehouse.location = "AMS";
        warehouse.businessUnitCode = "BU1";
        warehouse.capacity = 100;
        warehouse.stock = 0;
        return warehouse;
    }
}
