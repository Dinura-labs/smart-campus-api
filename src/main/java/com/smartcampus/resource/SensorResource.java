package com.smartcampus.resource;

import com.smartcampus.api.ErrorPayload;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.CampusStore;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    private static final Logger LOG = Logger.getLogger(SensorResource.class.getName());

    private final CampusStore store;

    @Context
    private UriInfo uriInfo;

    @Inject
    public SensorResource(CampusStore store) {
        this.store = store;
    }

    // Lists sensors, optionally filtering them by a specific type
    @GET
    public List<Sensor> listSensors(@QueryParam("type") String type) {
        LOG.fine(() -> "Listing sensors with type filter: " + type);
        return store.findAllSensors(type);
    }

    // Validates incoming sensor registration requests, ensuring all required details
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerSensor(Sensor body) {
        if (body == null || body.getId() == null || body.getId().isBlank()) {
            LOG.warning("Sensor registration rejected: missing sensor id");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorPayload(400, "Bad Request", "Sensor id is required."))
                    .build();
        }
        if (body.getRoomId() == null || body.getRoomId().isBlank()) {
            LOG.warning(() -> "Sensor registration rejected for id " + body.getId() + ": missing roomId");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorPayload(400, "Bad Request", "roomId is required in the JSON body."))
                    .build();
        }
        if (body.getType() == null || body.getType().isBlank()) {
            LOG.warning(() -> "Sensor registration rejected for id " + body.getId() + ": missing type");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorPayload(400, "Bad Request", "type is required (e.g. Temperature, CO2, Occupancy)."))
                    .build();
        }
        if (body.getStatus() == null || body.getStatus().isBlank()) {
            body.setStatus("ACTIVE");
            LOG.fine(() -> "Defaulted status to ACTIVE for sensor " + body.getId());
        }
        LOG.info(() -> "Registering sensor " + body.getId() + " in room " + body.getRoomId());
        return store.registerSensor(body)
                .map(sensor -> Response.created(locationForSensor(sensor.getId())).entity(sensor).build())
                .orElseGet(() -> Response.status(Response.Status.CONFLICT)
                        .entity(new ErrorPayload(409, "Conflict", "A sensor with this id already exists."))
                        .build());
    }

    @Path("{sensorId}/readings")
    public SensorReadingResource readings(@PathParam("sensorId") String sensorId) {
        LOG.fine(() -> "Routing to readings resource for sensor " + sensorId);
        return new SensorReadingResource(sensorId, store);
    }

    private URI locationForSensor(String sensorId) {
        return uriInfo.getAbsolutePathBuilder().path(sensorId).build();
    }
}
