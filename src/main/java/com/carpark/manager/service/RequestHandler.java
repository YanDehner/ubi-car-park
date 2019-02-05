package com.carpark.manager.service;

import com.carpark.manager.domain.ChargingPoint;
import com.carpark.manager.exceptions.CpNotFoundException;
import com.carpark.manager.exceptions.MaxCurrentExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handles the plug in and plug off requests of th CPs distributing the available current among them. It gives those
 * CPs which have a car plugged in later a higher priority, trying to give the the most possible CPs the current
 * for fast charging.
 */
public class RequestHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);
    private final Map<String, ChargingPoint> chargingPoints;
    private final int highCurrent;
    private final int lowCurrent;
    private final int maxCurrent;
    private final FileSystemPersister statePersister;

    /**
     * Constructor
     *
     * @param chargingPoints List of CP names to manage
     * @param maxCurrent     max. available current in total in Ampere
     * @param highCurrent    max. current per CP in Ampere for fast charging
     * @param lowCurrent     max. current per CP in Ampere for slow charging
     */
    @Autowired
    public RequestHandler(final List<String> chargingPoints, final int maxCurrent, final int highCurrent, final int lowCurrent,
                          final FileSystemPersister statePersister) {
        checkNotNull(chargingPoints, "chargingPoints must not be null");
        checkArgument(maxCurrent > 0, "maxCurrent has to be greater 0");
        checkArgument(highCurrent > 0, "highCurrent has to be greater 0");
        checkArgument(lowCurrent > 0, "lowCurrent has to be greater 0");
        checkArgument(highCurrent > lowCurrent, "highCurrent has to be greater than lowCurrent");
        this.maxCurrent = maxCurrent;
        this.highCurrent = highCurrent;
        this.lowCurrent = lowCurrent;
        this.statePersister = checkNotNull(statePersister, "statePersister must not be null");
        this.chargingPoints = getOrCreatehargingPoints(chargingPoints);

    }

    /**
     * Tries to read the old state from {@link FileSystemPersister}. If not available creates a new Map from the config.
     *
     * @param nameList List of CP-names from the application config
     * @return A {@link Map} of CP-name to {@link ChargingPoint}
     * @throws IllegalStateException if the list of CP-names is different from the one recovered from {@link FileSystemPersister}
     */
    private Map<String, ChargingPoint> getOrCreatehargingPoints(final List<String> nameList) {
        final Map<String, ChargingPoint> chargingPoints = statePersister.readState().orElseGet(() -> nameList.stream().collect(Collectors.toMap(n -> n, ChargingPoint::new)));

        if (!equalIgnoreOrder(nameList, chargingPoints.keySet())) {
            LOGGER.error("The configuration seems to have changed since the last run.");
            throw new IllegalStateException("Configuration does not match recovered state, please delete state file");
        }
        return chargingPoints;
    }

    /**
     * Check if two {@link Collection}s are equal ignoring the order of the content.
     *
     * @param a the one {@link Collection}s to check
     * @param b the other {@link Collection}s to check
     * @return true if the contents are equal ignoring their order
     */
    private boolean equalIgnoreOrder(final Collection<String> a, final Collection<String> b) {
        return a.containsAll(b) && a.size() == b.size();
    }


    /**
     * Handles message that a car plugged in at a CP redistributing the currents if necessary.
     *
     * @param cpName Name of the CP to plug in (must be one of the configuration)
     * @throws CpNotFoundException         if the given name is not configured.
     * @throws MaxCurrentExceededException if the max. current would be exceeded.
     */
    synchronized public void plugIn(final String cpName) {
        LOGGER.info("Received plug-in-message for {}", cpName);
        checkCP(cpName);

        final ChargingPoint chargingPoint = chargingPoints.get(cpName);
        if (!chargingPoint.isPlugged()) {

            throttleByLoadingTime();

            if (currentSum() + lowCurrent > maxCurrent) {
                throw new MaxCurrentExceededException("Max current not sufficient. Can't add another car.");
            }

            chargingPoint.setCurrent(currentSum() + highCurrent <= maxCurrent ? highCurrent : lowCurrent);
            chargingPoint.plugIn();
        }
        statePersister.safeState(getChargingPoints());
    }


    /**
     * Handles a plug-off-message resetting the given CP and redistributing the currents if possible.
     *
     * @param cpName Name of the CP to reset (must be one of the configuration).
     * @throws CpNotFoundException if the given name is not configured.
     */
    synchronized public void plugOff(final String cpName) {
        LOGGER.info("Received plug-off-message for {}", cpName);
        checkCP(cpName);

        final ChargingPoint chargingPoint = chargingPoints.get(cpName);
        if (chargingPoint.isPlugged()) {
            chargingPoint.plugOff();
            // set the youngest CPs to fast charging if possible
            boostByLoadingTime();
        }
        statePersister.safeState(getChargingPoints());
    }

    /**
     * Sets the current of CPs which are loading the longest time to slow charging current.
     */
    private void throttleByLoadingTime() {
        // order plugged CPs by charging time ascending
        final List<ChargingPoint> chargingPointsSortedByAge = this.chargingPoints.values().stream()
                .filter(cp -> cp.isPlugged() && cp.getCurrent() > lowCurrent)
                .sorted(Comparator.comparingLong(ChargingPoint::getPlugInTimestamp))
                .collect(Collectors.toList());

        for (ChargingPoint cp : chargingPointsSortedByAge) {
            if (currentSum() + highCurrent > maxCurrent) {
                LOGGER.debug("Throttling {} to {}A", cp.getName(), lowCurrent);
                cp.setCurrent(lowCurrent);
            } else {
                break;
            }
        }
    }

    /**
     * Sets then current of CPs which are loading the shortest time to high charging current.
     */
    private void boostByLoadingTime() {
        // order plugged CPs by charging time descending
        final List<ChargingPoint> chargingPointsSortedByAge = this.chargingPoints.values().stream()
                .filter(cp -> cp.isPlugged() && cp.getCurrent() < highCurrent)
                .sorted(Comparator.comparingLong(ChargingPoint::getPlugInTimestamp))
                .collect(Collectors.toList());

        for (ChargingPoint cp : chargingPointsSortedByAge) {
            if (currentSum() - lowCurrent + highCurrent <= maxCurrent) {
                LOGGER.info("Boosting {} to {}A", cp.getName(), highCurrent);
                cp.setCurrent(highCurrent);
            } else {
                break;
            }
        }
    }

    /**
     * Returns the allowed current for the given CP name.
     *
     * @param cpName Name of the CP
     * @return the current in Ampere
     * @throws IllegalArgumentException if the given name is not configured.
     */
    public int getAllowedCurrent(final String cpName) {
        checkCP(cpName);
        return chargingPoints.get(cpName).getCurrent();
    }

    /**
     * Returns the actual List of CPs with there statuses.
     *
     * @return List of CPs with their statuses
     */
    public List<ChargingPoint> getChargingPoints() {
        return chargingPoints.values().stream().map(ChargingPoint::copy).collect(Collectors.toList());
    }

    private int currentSum() {
        return chargingPoints.values().stream()
                .filter(ChargingPoint::isPlugged)
                .mapToInt(ChargingPoint::getCurrent)
                .sum();
    }

    /**
     * Makes sure, that the requested CP is configured.
     *
     * @param cpName name of the charging point
     * @throws CpNotFoundException if the given name cannot be found in the configured list of CPs
     */
    private void checkCP(final String cpName) {
        if (!chargingPoints.containsKey(cpName)) {
            LOGGER.error("CP {} is not configured", cpName);
            throw new CpNotFoundException(cpName);
        }
    }

}
