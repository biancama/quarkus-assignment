package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.vavr.control.Either;

public interface ReplaceWarehouseOperation {
  Either<String, Warehouse> replace(Warehouse warehouse);
}
