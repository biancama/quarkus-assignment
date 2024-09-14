package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.vavr.control.Either;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;
  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
      this.locationResolver = locationResolver;
  }

  @Override
  @Transactional
  public Either<String, Warehouse> replace(Warehouse newWarehouse) {
    if (newWarehouse.stock > newWarehouse.capacity) return Either.left("Cannot create a warehouse with stock greater than capacity");
    var location = locationResolver.resolveByIdentifier(newWarehouse.location);
    if (location == null) return Either.left("Location not found");
    var oldWarehouse = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (oldWarehouse == null) return Either.left("Warehouse to replace not found");
    // extra check
    if (oldWarehouse.stock != newWarehouse.stock) return Either.left(String.format("Stock match error old warehouse stock %d but new warehouse stock %d", oldWarehouse.stock, newWarehouse.stock));

    var warehousesPerLocationForNewWarehouse = warehouseStore.getAllByLocation(newWarehouse.location);

    var isInTheSameLocation = newWarehouse.location != null &&  newWarehouse.location.equals(oldWarehouse.location);
    var deltaCapacity = isInTheSameLocation ? newWarehouse.capacity - oldWarehouse.capacity : newWarehouse.capacity;

    if (location.maxCapacity - deltaCapacity < warehousesPerLocationForNewWarehouse.stream().map(w -> w.capacity).reduce(0, Integer::sum)) return Either.left(String.format("Location %s has reached max capacity", location.identification));

    if (!isInTheSameLocation)  {
      //if it is in the same location, then I don't need to check if the system will overrun the max number of warehouses, because I add one and delete one
      if (location.maxNumberOfWarehouses  <= warehousesPerLocationForNewWarehouse.size()) return Either.left(String.format("Location %s has reached max number of warehouses", location.identification));
      // extra check //TODO Capacity Accommodation Ask to Ikea maybe  I did NOT understand
      var warehousesPerLocationForOldWarehouse = warehouseStore.getAllByLocation(oldWarehouse.location);
      var currentStockForOldLocation = warehousesPerLocationForOldWarehouse.stream().map(w -> w.stock).reduce(0, Integer::sum);
      var currentCapacityForOldLocation = warehousesPerLocationForOldWarehouse.stream().map(w -> w.capacity).reduce(0, Integer::sum);
      if (currentCapacityForOldLocation - oldWarehouse.capacity < currentStockForOldLocation) return Either.left(String.format("Cannot accommodate the current stock level %d because the new capacity for location %s is %d", currentStockForOldLocation, location.identification, currentCapacityForOldLocation - oldWarehouse.capacity));
    } else {
      var warehousesPerLocationForOldWarehouse = warehouseStore.getAllByLocation(oldWarehouse.location);
      var currentStockForOldLocation = warehousesPerLocationForOldWarehouse.stream().map(w -> w.stock).reduce(0, Integer::sum);
      var currentCapacityForOldLocation = warehousesPerLocationForOldWarehouse.stream().map(w -> w.capacity).reduce(0, Integer::sum) ;
      if (currentCapacityForOldLocation + newWarehouse.capacity - oldWarehouse.capacity < currentStockForOldLocation) return Either.left(String.format("Cannot accommodate the current stock level %d because the new capacity for location %s is %d", currentStockForOldLocation, location.identification, currentCapacityForOldLocation + newWarehouse.capacity - oldWarehouse.capacity));
    }
    oldWarehouse.archivedAt = LocalDateTime.now();
    warehouseStore.update(oldWarehouse);
    warehouseStore.create(newWarehouse);
    return Either.right(newWarehouse);
  }
}
