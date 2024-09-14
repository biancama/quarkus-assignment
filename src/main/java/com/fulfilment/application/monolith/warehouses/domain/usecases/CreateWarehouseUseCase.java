package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.vavr.control.Either;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  @Transactional
  public Either<String, Warehouse> create(Warehouse warehouse) {
    if (warehouse.stock > warehouse.capacity) return Either.left("Cannot create a warehouse with stock greater than capacity");
    var location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) return Either.left("Location not found");
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) return Either.left("Warehouse already exists");
    var warehousesPerLocation = warehouseStore.getAllByLocation(warehouse.location);
    if (location.maxNumberOfWarehouses <= warehousesPerLocation.size()) return Either.left(String.format("Location %s has reached max number of warehouses", location.identification));
    if (location.maxCapacity - warehouse.capacity < warehousesPerLocation.stream().map(w -> w.capacity).reduce(0, Integer::sum)) return Either.left(String.format("Location %s has reached max capacity", location.identification));
    // if all went well, create the warehouse
    return Either.right(warehouseStore.create(warehouse));
  }
}
