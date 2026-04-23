package com.smartcampus.mapper;

import com.smartcampus.api.ErrorPayload;
import com.smartcampus.exception.SensorUnavailableException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import java.util.logging.Logger;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    private static final Logger LOG = Logger.getLogger(SensorUnavailableExceptionMapper.class.getName());

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        LOG.warning(() -> "Mapping sensor-unavailable error: " + exception.getMessage());
        return Response.status(Response.Status.FORBIDDEN)
                .entity(new ErrorPayload(403, "Forbidden", exception.getMessage()))
                .build();
    }
}
