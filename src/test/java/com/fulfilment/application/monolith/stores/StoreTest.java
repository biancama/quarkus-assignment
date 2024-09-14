package com.fulfilment.application.monolith.stores;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@QuarkusTest
public class StoreTest {
    @Inject
    private StoreResource storeResource;
    @InjectMock
    private LegacyStoreManagerGateway legacyStoreManagerGateway;
    @InjectMock
    private Session session;
    @Test
    public void testGivenAFailureOnStorePersistThenThereIsNoInteractWithLegacyStoreManagerGateway() throws URISyntaxException {
        var uriInfo = mock(UriInfo.class);
        var absolutePathBuilder = mock(UriBuilder.class);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(absolutePathBuilder);
        when(absolutePathBuilder.path(anyString())).thenReturn(absolutePathBuilder);
        doThrow(new RuntimeException("Boom !!!")).when(session).persist(any(Store.class));
        var store = new Store();

        assertThrows(RuntimeException.class, () -> {
            storeResource.create(store, uriInfo);
        });
        verifyNoInteractions(legacyStoreManagerGateway);
    }


    @Test
    public void testGivenASuccessfullOnStorePersistThenThereIsOneInteractWithLegacyStoreManagerGateway() throws URISyntaxException {
        var uriInfo = mock(UriInfo.class);
        var absolutePathBuilder = mock(UriBuilder.class);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(absolutePathBuilder);
        when(absolutePathBuilder.path(anyString())).thenReturn(absolutePathBuilder);
        doAnswer(invocationOnMock -> {
            Store store = (Store) invocationOnMock.getArguments()[0];
            store.id = 1L;
            return null;
        }).when(session).persist(any(Store.class));
        var store = new Store();

        storeResource.create(store, uriInfo);
        verify(legacyStoreManagerGateway).createStoreOnLegacySystem(store);
    }

}
