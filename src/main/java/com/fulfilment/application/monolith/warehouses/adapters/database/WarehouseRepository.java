package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.mapper.WarehouseMapper;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.list("archivedAt IS NULL").stream().map(DbWarehouse::toWarehouse).collect(Collectors.toUnmodifiableList());
  }

  @Override
  public List<Warehouse> getAllByLocation(String location) {
    return this.list("location = ?1 AND archivedAt IS NULL", location).stream().map(DbWarehouse::toWarehouse).collect(Collectors.toUnmodifiableList());
  }

  @Override
  public Warehouse create(Warehouse warehouse) {
    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;
    persist(WarehouseMapper.INSTANCE.warehouseDtoToDbWarehouse(warehouse));
    return warehouse;
  }

  @Override
  public void update(Warehouse warehouse) {
    var warehouseDb = list("businessUnitCode = ?1 AND archivedAt IS NULL", warehouse.businessUnitCode).stream().findFirst().orElse(null);
    warehouseDb.archivedAt = LocalDateTime.now();
    persist(warehouseDb);
  }

  @Override
  public void remove(Warehouse warehouse) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'remove'");
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    var warehouse = list("businessUnitCode = ?1 AND archivedAt IS NULL", buCode).stream().findFirst().orElse(null);
    if (warehouse == null) {
      return null;
    } else {
      return warehouse.toWarehouse();
    }
  }
}
