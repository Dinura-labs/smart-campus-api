package com.smartcampus.resource;

import com.smartcampus.api.ErrorPayload;
import com.smartcampus.model.Room;
import com.smartcampus.store.CampusStore;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class RoomResource {

    private static final Logger LOG = Logger.getLogger(RoomResource.class.getName());

    private final CampusStore store;

    @Context
    private UriInfo uriInfo;

    @Inject
    public RoomResource(CampusStore store) {
        this.store = store;
    }

    // Handles incoming requests to fetch the list of all available rooms on the campus
    @GET
    public List<Room> listRooms() {
        LOG.fine("Listing all rooms");
        return store.findAllRooms();
    }

    // Process request to create new room
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRoom(Room body) {
        if (body == null || body.getId() == null || body.getId().isBlank()) {
            LOG.warning("Room creation rejected: missing room id");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorPayload(400, "Bad Request", "Room id is required."))
                    .build();
        }
        Room toCreate = new Room(body.getId().trim(), body.getName(), body.getCapacity());
        LOG.info(() -> "Creating room with id " + toCreate.getId());
        return store.createRoom(toCreate)
                .map(room -> Response.created(locationForRoom(room.getId())).entity(room).build())
                .orElseGet(() -> Response.status(Response.Status.CONFLICT)
                        .entity(new ErrorPayload(409, "Conflict", "A room with this id already exists."))
                        .build());
    }

    @GET
    @Path("/{roomId}")
    public Room getRoom(@PathParam("roomId") String roomId) {
        LOG.fine(() -> "Fetching room " + roomId);
        return store.findRoom(roomId).orElseThrow(NotFoundException::new);
    }

    // Handles the deletion of a specific room, returning a "Not Found" error if the room doesn't exist
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        LOG.info(() -> "Deleting room " + roomId);
        boolean removed = store.deleteRoomIfEmpty(roomId);
        if (!removed) {
            LOG.fine(() -> "Room not found for deletion: " + roomId);
            throw new NotFoundException();
        }
        return Response.noContent().build();
    }

    private URI locationForRoom(String roomId) {
        return uriInfo.getAbsolutePathBuilder().path(roomId).build();
    }
}
