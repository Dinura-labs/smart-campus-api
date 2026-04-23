package com.smartcampus.mapper;

import com.smartcampus.api.ErrorPayload;
import com.smartcampus.exception.RoomNotEmptyException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import java.util.logging.Logger;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    private static final Logger LOG = Logger.getLogger(RoomNotEmptyExceptionMapper.class.getName());

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        LOG.warning(() -> "Mapping room-not-empty error: " + exception.getMessage());
        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorPayload(409, "Conflict", exception.getMessage()))
                .build();
    }
}
