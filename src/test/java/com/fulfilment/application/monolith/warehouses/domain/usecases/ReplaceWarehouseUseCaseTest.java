package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReplaceWarehouseUseCaseTest {

    @Mock
    private WarehouseStore warehouseStore;
    @Mock
    private LocationResolver locationResolver;
    @Captor
    private ArgumentCaptor<Warehouse> argCaptorForNewWarehouse;
    @Captor
    private ArgumentCaptor<Warehouse> argCaptorForOldWarehouse;
    @InjectMocks
    private ReplaceWarehouseUseCase replaceWarehouseUseCase;

    @Test
    public void givenWarehouseReplacement_whenAllValidationSucceed_thenReturnWarehouseReplaced() {
        var newWarehouse = getNewWarehouse();
        var oldWareHouse = getOldWarehouseSameLocation();

        List<Warehouse> newWarehousesPerLocation = getNewWarehousesAlreadyPresent();
        List<Warehouse> oldWarehousesPerLocation = getOldWarehousesAlreadyPresent();

        var location = new Location("AMS", 3, 350);

        when(locationResolver.resolveByIdentifier(newWarehouse.location)).thenReturn(location);
        when(warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode)).thenReturn(oldWareHouse);
        when(warehouseStore.getAllByLocation(newWarehouse.location)).thenReturn(newWarehousesPerLocation);
        when(warehouseStore.getAllByLocation(oldWareHouse.location)).thenReturn(oldWarehousesPerLocation);

        var warehouseReplaced = replaceWarehouseUseCase.replace(newWarehouse);
        verify(warehouseStore).create(argCaptorForNewWarehouse.capture());
        assertThat(argCaptorForNewWarehouse.getValue(), equalTo(newWarehouse));
        verify(warehouseStore).update(argCaptorForOldWarehouse.capture());
        assertThat(argCaptorForOldWarehouse.getValue(), equalTo(oldWareHouse));
        assertTrue(warehouseReplaced.isRight(), "Validation exception");
        assertThat(warehouseReplaced.get(), equalTo(newWarehouse));
    }

    @Test
    public void givenWarehouse_whenReplacedWithStockGreaterThanCapacity_thenReturnAValidationError() {
        var newWarehouse = getNewWarehouse();
        newWarehouse.stock = newWarehouse.capacity + 1;

        var warehouseReplaced = replaceWarehouseUseCase.replace(newWarehouse);
        assertTrue(warehouseReplaced.isLeft(), "Validation exception");
        assertThat(warehouseReplaced.getLeft(), equalTo(("Cannot create a warehouse with stock greater than capacity")));
    }

    @Test
    public void givenWarehouse_whenReplacedWithLocationNotExisting_thenReturnAValidationError() {
        var newWarehouse = getNewWarehouse();
        when(locationResolver.resolveByIdentifier(newWarehouse.location)).thenReturn(null);

        var warehouseReplaced = replaceWarehouseUseCase.replace(newWarehouse);
        assertTrue(warehouseReplaced.isLeft(), "Validation exception");
        assertThat(warehouseReplaced.getLeft(), equalTo(("Location not found")));
    }

    @Test
    public void givenWarehouse_whenReplacedWithBusinessUnitNotExist_thenReturnAValidationError() {
        var newWarehouse = getNewWarehouse();

        var location = new Location("AMS", 3, 400);

        when(locationResolver.resolveByIdentifier(newWarehouse.location)).thenReturn(location);
        when(warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode)).thenReturn(null);

        var warehouseReplaced = replaceWarehouseUseCase.replace(newWarehouse);
        assertTrue(warehouseReplaced.isLeft(), "Validation exception");
        assertThat(warehouseReplaced.getLeft(), equalTo(("Warehouse to replace not found")));
    }

    @Test
    public void givenWarehouse_whenReplacedWithStockNotMatching_thenReturnAValidationError() {
        var newWarehouse = getNewWarehouse();
        var oldWareHouse = getOldWarehouseSameLocation();
        oldWareHouse.stock = newWarehouse.stock +1;
        var location = new Location("AMS", 3, 400);

        when(locationResolver.resolveByIdentifier(newWarehouse.location)).thenReturn(location);
        when(warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode)).thenReturn(oldWareHouse);

        var warehouseReplaced = replaceWarehouseUseCase.replace(newWarehouse);
        assertTrue(warehouseReplaced.isLeft(), "Validation exception");
        assertThat(warehouseReplaced.getLeft(), equalTo(("Stock match error old warehouse stock 1 but new warehouse stock 0")));
    }

    @Test
    public void givenWarehouse_whenReplacedInTheSameLocationButAddMoreCapacity_thenReturnAValidationError() {
        var newWarehouse = getNewWarehouse();
        var oldWareHouse = getOldWarehouseSameLocation();
        List<Warehouse> newWarehousesPerLocation = getNewWarehousesAlreadyPresent();

        newWarehouse.capacity = 100;
        oldWareHouse.capacity = 80;

        var location = new Location("AMS", 3, 310);
        // in the new warehouse location the current capacity = 300 (200 +100)
        // the delta it will be 20 (100 -80) so it overruns tha maximum capacity
        when(locationResolver.resolveByIdentifier(newWarehouse.location)).thenReturn(location);
        when(warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode)).thenReturn(oldWareHouse);
        when(warehouseStore.getAllByLocation(newWarehouse.location)).thenReturn(newWarehousesPerLocation);

        var warehouseReplaced = replaceWarehouseUseCase.replace(newWarehouse);
        assertTrue(warehouseReplaced.isLeft(), "Validation exception");
        assertThat(warehouseReplaced.getLeft(), equalTo(("Location AMS has reached max capacity")));
    }

    @Test
    public void givenWarehouse_whenReplacedInTheDifferentLocationButAddMoreCapacity_thenReturnAValidationError() {
        var newWarehouse = getNewWarehouse();
        var oldWareHouse = getOldWarehouseSameLocation();
        List<Warehouse> newWarehousesPerLocation = getNewWarehousesAlreadyPresent();

        newWarehouse.capacity = 100;
        oldWareHouse.capacity = 80;
        oldWareHouse.location = "NOT_IMPORTANT";

        var location = new Location("AMS", 3, 380);
        // in the new warehouse location the current capacity = 300 (200 +100)
        // the delta it will be 100 because old location does not matter
        when(locationResolver.resolveByIdentifier(newWarehouse.location)).thenReturn(location);
        when(warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode)).thenReturn(oldWareHouse);
        when(warehouseStore.getAllByLocation(newWarehouse.location)).thenReturn(newWarehousesPerLocation);

        var warehouseReplaced = replaceWarehouseUseCase.replace(newWarehouse);
        assertTrue(warehouseReplaced.isLeft(), "Validation exception");
        assertThat(warehouseReplaced.getLeft(), equalTo(("Location AMS has reached max capacity")));
    }
    @Test
    public void givenWarehouse_whenReplacedInTheDifferentLocationButMoreTheMaxWarehouseCapacity_thenReturnAValidationError() {
        var newWarehouse = getNewWarehouse();
        var oldWareHouse = getOldWarehouseSameLocation();
        oldWareHouse.location = "NOT_IMPORTANT";
        List<Warehouse> newWarehousesPerLocation = getNewWarehousesAlreadyPresent();
        List<Warehouse> oldWarehousesPerLocation = getOldWarehousesAlreadyPresent();

        var location = new Location("AMS", 2, 1000);
        when(locationResolver.resolveByIdentifier(newWarehouse.location)).thenReturn(location);
        when(warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode)).thenReturn(oldWareHouse);
        when(warehouseStore.getAllByLocation(newWarehouse.location)).thenReturn(newWarehousesPerLocation);

        var warehouseReplaced = replaceWarehouseUseCase.replace(newWarehouse);
        assertTrue(warehouseReplaced.isLeft(), "Validation exception");
        assertThat(warehouseReplaced.getLeft(), equalTo(("Location AMS has reached max number of warehouses")));
    }

    @Test
    public void givenWarehouse_whenReplacedInTheDifferentLocationButItIsImpossibleToAccommodateTheCurrentStock_thenReturnAValidationError() {
        var newWarehouse = getNewWarehouse();
        var oldWareHouse = getOldWarehouseSameLocation();
        oldWareHouse.location = "NOT_IMPORTANT";
        List<Warehouse> newWarehousesPerLocation = getNewWarehousesAlreadyPresent();
        List<Warehouse> oldWarehousesPerLocation = getOldWarehousesAlreadyPresent();
        oldWareHouse.capacity = oldWarehousesPerLocation.get(0).capacity; //replace this warehouse
        var location = new Location("AMS", 10, 1000);
        when(locationResolver.resolveByIdentifier(newWarehouse.location)).thenReturn(location);
        when(warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode)).thenReturn(oldWareHouse);
        when(warehouseStore.getAllByLocation(newWarehouse.location)).thenReturn(newWarehousesPerLocation);
        when(warehouseStore.getAllByLocation(oldWareHouse.location)).thenReturn(oldWarehousesPerLocation);
        // current Situation in old warehouse
        // warehouse01 capacity: 50 stock 40
        // warehouse02 capacity: 10 stock 5
        // total stock 45
        // If I replace warehouse 1 then the new total capacity will be 10, so I cannot accommodate the current 45
        var warehouseReplaced = replaceWarehouseUseCase.replace(newWarehouse);
        assertTrue(warehouseReplaced.isLeft(), "Validation exception");
        assertThat(warehouseReplaced.getLeft(), equalTo(("Cannot accommodate the current stock level 45 because the new capacity for location AMS is 10")));
    }

    @Test
    public void givenWarehouse_whenReplacedInTheSameLocationButItIsImpossibleToAccommodateTheCurrentStock_thenReturnAValidationError() {
        var newWarehouse = getNewWarehouse();
        newWarehouse.capacity = 34;
        var oldWareHouse = getOldWarehouseSameLocation();
        List<Warehouse> newWarehousesPerLocation = getNewWarehousesAlreadyPresent();
        List<Warehouse> oldWarehousesPerLocation = getOldWarehousesAlreadyPresent();
        oldWareHouse.capacity = oldWarehousesPerLocation.get(0).capacity; //replace this warehouse
        var location = new Location("AMS", 10, 1000);
        when(locationResolver.resolveByIdentifier(newWarehouse.location)).thenReturn(location);
        when(warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode)).thenReturn(oldWareHouse);
        when(warehouseStore.getAllByLocation(newWarehouse.location)).thenReturn(newWarehousesPerLocation);
        when(warehouseStore.getAllByLocation(oldWareHouse.location)).thenReturn(oldWarehousesPerLocation);
        // current Situation in old warehouse
        // warehouse01 capacity: 50 stock 40
        // warehouse02 capacity: 10 stock 5
        // total stock 45
        // If I replace warehouse 1  with newWarehouse then the new total capacity will be  44 (34 + 10), so I cannot accommodate the current 45
        var warehouseReplaced = replaceWarehouseUseCase.replace(newWarehouse);
        assertTrue(warehouseReplaced.isLeft(), "Validation exception");
        assertThat(warehouseReplaced.getLeft(), equalTo(("Cannot accommodate the current stock level 45 because the new capacity for location AMS is 44")));
    }

    private static List<Warehouse> getNewWarehousesAlreadyPresent() {
        var warehouse01 = new Warehouse();
        warehouse01.capacity = 100;
        var warehouse02 = new Warehouse();
        warehouse02.capacity = 200;
        List<Warehouse> warehousesPerLocation = List.of(warehouse01, warehouse02);
        return warehousesPerLocation;
    }

    private static Warehouse getNewWarehouse() {
        var warehouse = new Warehouse();
        warehouse.location = "AMS";
        warehouse.businessUnitCode = "BU1";
        warehouse.capacity = 100;
        warehouse.stock = 0;
        return warehouse;
    }
    private static List<Warehouse> getOldWarehousesAlreadyPresent() {
        var warehouse01 = new Warehouse();
        warehouse01.capacity = 50;
        warehouse01.stock = 40;

        var warehouse02 = new Warehouse();
        warehouse02.capacity = 10;
        warehouse02.stock = 5;
        List<Warehouse> warehousesPerLocation = List.of(warehouse01, warehouse02);
        return warehousesPerLocation;
    }
    private static Warehouse getOldWarehouseSameLocation() {
        var warehouse = new Warehouse();
        warehouse.location = "AMS";
        warehouse.businessUnitCode = "BU1";
        warehouse.capacity = 90;
        warehouse.stock = 0;
        return warehouse;
    }
}
