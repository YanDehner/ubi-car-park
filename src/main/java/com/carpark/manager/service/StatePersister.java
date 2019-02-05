package com.carpark.manager.service;

import com.carpark.manager.domain.ChargingPoint;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Persists the current state of the application and can read it back.
 */
public interface StatePersister {

    /**
     * Read the old state of thes application back if possible. If no state can be recovered, an empty Optional is
     * retured.
     *
     * @return Optional that is filled with the state if any was found to recover from
     */
    Optional<Map<String, ChargingPoint>> readState();

    /**
     * Persists the current state of the application regarding the charging points.
     *
     * @param currentState List of the curren CPs
     */
    void safeState(final List<ChargingPoint> currentState);
}
