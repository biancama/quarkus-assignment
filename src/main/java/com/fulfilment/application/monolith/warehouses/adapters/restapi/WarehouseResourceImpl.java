package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.mapper.WarehouseMapper;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject private WarehouseRepository warehouseRepository;
  @Inject private CreateWarehouseOperation createWarehouseOperation;
  @Context
  private UriInfo uriInfo;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  public Response createANewWarehouseUnit(@NotNull Warehouse data) {
    var persistedWarehouse = createWarehouseOperation.create(WarehouseMapper.INSTANCE.pojoWarehouseToWarehouse(data));
    if (persistedWarehouse.isRight()) {
      UriBuilder builder = uriInfo.getAbsolutePathBuilder().path(persistedWarehouse.get().businessUnitCode);
      return Response.created(builder.build()).build();
    } else {
      throw new WebApplicationException(persistedWarehouse.getLeft(), 400);
    }
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
   var warehouse = warehouseRepository.findByBusinessUnitCode(id);
   if (warehouse != null) {
      return WarehouseMapper.INSTANCE.warehouseToPojoWarehouse(warehouse);
   } else {
     throw new WebApplicationException("Warehouse with id of " + id + " does not exist.", 404);
   }
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'archiveAWarehouseUnitByID'");
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(
          String businessUnitCode, @NotNull Warehouse data) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException(
            "Unimplemented method 'replaceTheCurrentActiveWarehouse'");
  }

  private Warehouse toWarehouseResponse(
          com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }
}
