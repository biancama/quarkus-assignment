package com.fulfilment.application.monolith.warehouses.mapper;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface WarehouseMapper {

    WarehouseMapper INSTANCE = Mappers.getMapper(WarehouseMapper.class);

    @Mapping(target = "id", ignore = true)
    DbWarehouse warehouseDtoToDbWarehouse(Warehouse warehouse);
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    Warehouse pojoWarehouseToWarehouse(com.warehouse.api.beans.Warehouse warehouse);
    @Mapping(target = "id", ignore = true)
    com.warehouse.api.beans.Warehouse warehouseToPojoWarehouse(Warehouse warehouse);
}
