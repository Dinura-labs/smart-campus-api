package com.smartcampus.resource;

import com.smartcampus.model.SensorReading;
import com.smartcampus.store.CampusStore;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.List;
import java.util.logging.Logger;

@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private static final Logger LOG = Logger.getLogger(SensorReadingResource.class.getName());

    private final String sensorId;
    private final CampusStore store;

    public SensorReadingResource(String sensorId, CampusStore store) {
        this.sensorId = sensorId;
        this.store = store;
    }

    @GET
    public List<SensorReading> listReadings() {
        LOG.fine(() -> "Listing readings for sensor " + sensorId);
        store.findSensor(sensorId).orElseThrow(NotFoundException::new);
        return store.findReadings(sensorId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading body) {
        if (body == null) {
            LOG.warning(() -> "Reading creation rejected for sensor " + sensorId + ": missing payload");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        LOG.info(() -> "Adding reading for sensor " + sensorId);
        SensorReading saved = store.appendReading(sensorId, body);
        return Response.status(Response.Status.CREATED).entity(saved).build();
    }
}
