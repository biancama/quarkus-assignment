package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.vavr.control.Either;

public interface CreateWarehouseOperation {
  Either<String, Warehouse> create(Warehouse warehouse);
}
