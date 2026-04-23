package com.smartcampus.resource;

import com.smartcampus.api.DiscoveryPayload;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import java.util.logging.Logger;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    private static final Logger LOG = Logger.getLogger(DiscoveryResource.class.getName());

    @Context
    private UriInfo uriInfo;

    @GET
    public DiscoveryPayload discover() {
        LOG.fine("Serving API discovery document");
        String base = uriInfo.getBaseUri().toString();
        if (!base.endsWith("/")) {
            base = base + "/";
        }
        DiscoveryPayload d = new DiscoveryPayload();
        d.getCollections().put("rooms", base + "rooms");
        d.getCollections().put("sensors", base + "sensors");
        return d;
    }
}
