package com.smartcampus.mapper;

import com.smartcampus.api.ErrorPayload;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class ThrowableExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(ThrowableExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException wae) {
            LOG.fine(() -> "Passing through WebApplicationException with status " + wae.getResponse().getStatus());
            return wae.getResponse();
        }
        LOG.log(Level.SEVERE, "Unhandled error", exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorPayload(
                        500,
                        "Internal Server Error",
                        "An unexpected error occurred. Please try again later."))
                .build();
    }
}
