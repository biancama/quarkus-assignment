package com.fulfilment.application.monolith.stores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import org.jboss.logging.Logger;

@Path("store")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class StoreResource {

  @Inject LegacyStoreManagerGateway legacyStoreManagerGateway;

  private static final Logger LOGGER = Logger.getLogger(StoreResource.class.getName());

  @GET
  public List<Store> get() {
    return Store.listAll(Sort.by("name"));
  }

  @GET
  @Path("{id}")
  public Store getSingle(Long id) {
    Store entity = Store.findById(id);
    if (entity == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    return entity;
  }

  @POST
  public Response create(Store store, @Context UriInfo uriInfo) {
    if (store.id != null) {
      throw new WebApplicationException("Id was invalidly set on request.", 422);
    }
    QuarkusTransaction.requiringNew().run(() -> {
      store.persistAndFlush();  // persist and flush already commit. I shrink the transaction scope
    });
    legacyStoreManagerGateway.createStoreOnLegacySystem(store);

    UriBuilder builder = uriInfo.getAbsolutePathBuilder().path(Long.toString(store.id));
    return Response.created(builder.build()).build();
  }

  @PUT
  @Path("{id}")
  public Store update(Long id, Store updatedStore) {
    if (updatedStore.name == null) {
      throw new WebApplicationException("Store Name was not set on request.", 422);
    }
    Store entity = QuarkusTransaction.requiringNew().call (() -> {
      Store store = Store.findById(id);

      if (store == null) {
        throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
      }

      store.name = updatedStore.name;
      store.quantityProductsInStock = updatedStore.quantityProductsInStock;
      store.persistAndFlush();
      return store;
    });

    legacyStoreManagerGateway.updateStoreOnLegacySystem(updatedStore);

    return entity;
  }

  @PATCH
  @Path("{id}")
  @Transactional
  public Store patch(Long id, Store updatedStore) {
    //TODO Check with Ikea.... I think this is an error because PATCH can partially change an object
//    if (updatedStore.name == null) {
//      throw new WebApplicationException("Store Name was not set on request.", 422);
//    }
    Store entity = QuarkusTransaction.requiringNew().call (() -> {
      Store store = Store.findById(id);

      if (store == null) {
        throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
      }

      if (updatedStore.name != null) { //PATCH can partially change an object
        store.name = updatedStore.name;
      }

      if (updatedStore.quantityProductsInStock != 0) { //PATCH can partially change an object
        store.quantityProductsInStock = updatedStore.quantityProductsInStock;
      }
      return store;
    });
    legacyStoreManagerGateway.updateStoreOnLegacySystem(updatedStore);

    return entity;
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    Store entity = Store.findById(id);
    if (entity == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    entity.delete();
    return Response.status(204).build();
  }

  @Provider
  public static class ErrorMapper implements ExceptionMapper<Exception> {

    @Inject ObjectMapper objectMapper;

    @Override
    public Response toResponse(Exception exception) {
      LOGGER.error("Failed to handle request", exception);

      int code = 500;
      if (exception instanceof WebApplicationException) {
        code = ((WebApplicationException) exception).getResponse().getStatus();
      }

      ObjectNode exceptionJson = objectMapper.createObjectNode();
      exceptionJson.put("exceptionType", exception.getClass().getName());
      exceptionJson.put("code", code);

      if (exception.getMessage() != null) {
        exceptionJson.put("error", exception.getMessage());
      }

      return Response.status(code).entity(exceptionJson).build();
    }
  }
}
