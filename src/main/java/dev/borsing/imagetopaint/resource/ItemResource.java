package dev.borsing.imagetopaint.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import dev.borsing.imagetopaint.domain.Item;
import dev.borsing.imagetopaint.resource.dto.CreateItemRequest;
import dev.borsing.imagetopaint.service.ItemService;

import java.util.List;
import java.util.UUID;

@Path("/items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ItemResource {

    @Inject
    ItemService itemService;

    @GET
    public List<Item> list() {
        return itemService.findAll();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") UUID id) {
        return itemService.findById(id)
                .map(Response::ok)
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @POST
    public Response create(CreateItemRequest request) {
        Item item = itemService.create(request.name());
        return Response.status(Response.Status.CREATED).entity(item).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        return itemService.delete(id)
                ? Response.noContent().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }
}
