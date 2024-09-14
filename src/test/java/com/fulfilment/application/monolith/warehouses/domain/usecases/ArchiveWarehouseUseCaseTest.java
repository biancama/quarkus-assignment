package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ArchiveWarehouseUseCaseTest {
    @Mock
    private WarehouseStore warehouseStore;
    @Captor
    private ArgumentCaptor<Warehouse> argCaptor;
    @InjectMocks
    private ArchiveWarehouseUseCase archiveWarehouseUseCase;

    @Test
    public void givenWarehouse_whenArchivedThenArchivedAtPropertyIsNotNull() {
        var warehouse = new Warehouse();
        assertTrue(warehouse.archivedAt == null);
        archiveWarehouseUseCase.archive(warehouse);

        verify(warehouseStore).update(argCaptor.capture());
        assertTrue(argCaptor.getValue().archivedAt != null);

    }
    @Test
    public void simpleNPETest() {
        archiveWarehouseUseCase.archive(null);
    }
}
