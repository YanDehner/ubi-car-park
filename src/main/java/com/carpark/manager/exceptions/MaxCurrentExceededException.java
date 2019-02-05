package com.carpark.manager.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Max. current limit exceeded")
public class MaxCurrentExceededException extends IllegalStateException {
    public MaxCurrentExceededException(final String message) {
        super(message);
    }
}
