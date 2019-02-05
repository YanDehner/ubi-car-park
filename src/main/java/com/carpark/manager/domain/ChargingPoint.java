package com.carpark.manager.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representation of a charging point with its statuses.
 */
public class ChargingPoint {
    private final String name;
    private boolean plugged = false;
    private int current = 0;
    private long plugInTimestamp = 0L;

    /**
     * Constructor
     *
     * @param name Name of the CP
     */
    public ChargingPoint(final String name) {
        this.name = name;
    }

    @JsonCreator
    private ChargingPoint(@JsonProperty("name") final String name, @JsonProperty("plugged") final boolean plugged,
                          @JsonProperty("current") final int current, @JsonProperty("plugInTimestamp") final long plugInTimestamp) {
        this.name = name;
        this.plugged = plugged;
        this.current = current;
        this.plugInTimestamp = plugInTimestamp;
    }

    /**
     * Creates a copy of this CharginPoint
     *
     * @return a copy ot this CP with the same statuses
     */
    public ChargingPoint copy() {
        return new ChargingPoint(name, plugged, current, plugInTimestamp);
    }

    public String getName() {
        return name;
    }

    public boolean isPlugged() {
        return plugged;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(final int current) {
        this.current = current;
    }

    public long getPlugInTimestamp() {
        return plugInTimestamp;
    }

    /**
     * Sets plugged to true and sets the current timestamp
     */
    public void plugIn() {
        plugged = true;
        plugInTimestamp = System.currentTimeMillis();
    }

    /**
     * Sets plugged to false, timestamp to 0 and current to 0
     */
    public void plugOff() {
        plugged = false;
        plugInTimestamp = 0;
        current = 0;
    }

    public String toString() {
        return name + " " + (plugged ? "OCCUPIED" : "AVAILABLE") + (plugged ? " " + current + "A" : "");
    }
}
