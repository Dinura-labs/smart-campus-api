package com.smartcampus.mapper;

import com.smartcampus.api.ErrorPayload;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import java.util.logging.Logger;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    private static final Logger LOG = Logger.getLogger(NotFoundExceptionMapper.class.getName());

    @Override
    public Response toResponse(NotFoundException exception) {
        String msg = exception.getMessage() != null ? exception.getMessage() : "Resource not found.";
        LOG.fine(() -> "Mapping NotFoundException: " + msg);
        return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorPayload(404, "Not Found", msg))
                .build();
    }
}
