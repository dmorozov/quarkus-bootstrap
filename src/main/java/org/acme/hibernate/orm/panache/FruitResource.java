package org.acme.hibernate.orm.panache;

import static jakarta.ws.rs.core.Response.Status.CREATED;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.mutiny.CompositeException;
import io.smallrye.mutiny.Uni;

@Path("fruits")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class FruitResource {

  private static final Logger LOGGER = Logger.getLogger(FruitResource.class.getName());

  @Inject
  private FruitService fruitService;

  @GET
  public Uni<List<Fruit>> get() {
    return fruitService.listAll();
  }

  @GET
  @Path("{id}")
  public Uni<Fruit> getSingle(Long id) {
    return fruitService.findById(id);
  }

  @POST
  public Uni<Response> create(Fruit fruit) {
    if (fruit == null || fruit.id != null) {
      throw new WebApplicationException("Id was invalidly set on request.", 422);
    }

    return fruitService.createFruit(fruit)
        .replaceWith(Response.ok(fruit).status(CREATED)::build);
  }

  @PUT
  @Path("{id}")
  public Uni<Response> update(Long id, Fruit fruit) {
    if (fruit == null || fruit.name == null) {
      throw new WebApplicationException("Fruit name was not set on request.", 422);
    }

    return fruitService.updateFruit(id, fruit)
        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);
  }

  @DELETE
  @Path("{id}")
  public Uni<Response> delete(Long id) {
    return fruitService.deleteById(id)
        .map(deleted -> deleted
            ? Response.ok().status(NO_CONTENT).build()
            : Response.ok().status(NOT_FOUND).build());
  }

  /**
   * Create a HTTP response from an exception.
   *
   * Response Example:
   *
   * <pre>
   * HTTP/1.1 422 Unprocessable Entity
   * Content-Length: 111
   * Content-Type: application/json
   *
   * {
   *     "code": 422,
   *     "error": "Fruit name was not set on request.",
   *     "exceptionType": "jakarta.ws.rs.WebApplicationException"
   * }
   * </pre>
   */
  @Provider
  public static class ErrorMapper implements ExceptionMapper<Exception> {

    @Inject
    ObjectMapper objectMapper;

    @Override
    public Response toResponse(Exception exception) {
      LOGGER.error("Failed to handle request", exception);

      Throwable throwable = exception;

      int code = 500;
      if (throwable instanceof WebApplicationException) {
        code = ((WebApplicationException) exception).getResponse().getStatus();
      }

      // This is a Mutiny exception and it happens, for example, when we try to insert
      // a new
      // fruit but the name is already in the database
      if (throwable instanceof CompositeException) {
        throwable = ((CompositeException) throwable).getCause();
      }

      ObjectNode exceptionJson = objectMapper.createObjectNode();
      exceptionJson.put("exceptionType", throwable.getClass().getName());
      exceptionJson.put("code", code);

      if (exception.getMessage() != null) {
        exceptionJson.put("error", throwable.getMessage());
      }

      return Response.status(code)
          .entity(exceptionJson)
          .build();
    }

  }
}
