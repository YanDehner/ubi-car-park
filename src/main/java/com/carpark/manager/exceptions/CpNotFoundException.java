package com.carpark.manager.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class CpNotFoundException extends RuntimeException {
    public CpNotFoundException(final String cpName) {
        super("Charging point " + cpName + " is not configured");
    }
}
