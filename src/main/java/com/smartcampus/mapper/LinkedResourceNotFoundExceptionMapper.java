package com.smartcampus.mapper;

import com.smartcampus.api.ErrorPayload;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import java.util.logging.Logger;

@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    private static final Logger LOG = Logger.getLogger(LinkedResourceNotFoundExceptionMapper.class.getName());

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        LOG.warning(() -> "Mapping linked resource error: " + exception.getMessage());
        return Response.status(422)
                .entity(new ErrorPayload(422, "Unprocessable Entity", exception.getMessage()))
                .build();
    }
}
