package com.carpark.manager.service;

import com.carpark.manager.domain.ChargingPoint;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


public class RequestHandlerTest {

    private static final int MAX_CURRENT = 40;
    private static final int HIGH_CURRENT = 20;
    private static final int LOW_CURRENT = 10;
    private final List<String> chargingPointList = ImmutableList.of("CP1", "CP2", "CP3", "CP4");
    private final FileSystemPersister statePersister = mock(FileSystemPersister.class);
    private RequestHandler requestHandler;

    @Before
    public void setUp() {
        requestHandler = new RequestHandler(chargingPointList, MAX_CURRENT, HIGH_CURRENT, LOW_CURRENT, statePersister);
    }


    @Test
    public void shouldAddCPsWithHighCurrent() {
        // WHEN
        requestHandler.plugIn("CP1");
        requestHandler.plugIn("CP2");

        // THEN
        assertThat(requestHandler.getAllowedCurrent("CP1")).isEqualTo(HIGH_CURRENT);
        assertThat(requestHandler.getAllowedCurrent("CP2")).isEqualTo(HIGH_CURRENT);
    }

    @Test
    public void shouldAddCPWithHighCurrentAndResetOldestToLowCurrent() {
        // WHEN
        requestHandler.plugIn("CP1");
        requestHandler.plugIn("CP2");
        requestHandler.plugIn("CP3");

        // THEN
        assertThat(requestHandler.getAllowedCurrent("CP1")).isEqualTo(LOW_CURRENT);
        assertThat(requestHandler.getAllowedCurrent("CP2")).isEqualTo(LOW_CURRENT);
        assertThat(requestHandler.getAllowedCurrent("CP3")).isEqualTo(HIGH_CURRENT);
    }

    @Test
    public void shouldResetToHighCurrentAgain() {
        // WHEN
        requestHandler.plugIn("CP1");
        requestHandler.plugIn("CP2");
        requestHandler.plugIn("CP3");
        requestHandler.plugOff("CP2");

        // THEN
        assertThat(requestHandler.getAllowedCurrent("CP1")).isEqualTo(HIGH_CURRENT);
        assertThat(requestHandler.getAllowedCurrent("CP3")).isEqualTo(HIGH_CURRENT);
        assertThat(requestHandler.getAllowedCurrent("CP2")).isEqualTo(0);
    }

    @Test
    public void shouldSetAllCPsToLowCurrent() {
        // WHEN
        chargingPointList.forEach(cp -> requestHandler.plugIn(cp));

        // THEN
        chargingPointList.forEach(cp -> assertThat(requestHandler.getAllowedCurrent(cp)).isEqualTo(LOW_CURRENT));
    }

    @Test
    public void shouldReturnListOfCPs() {
        // GIVEN
        List<String> cpToPlugInList = ImmutableList.of("CP1", "CP2", "CP3");
        cpToPlugInList.forEach(cp -> requestHandler.plugIn(cp));

        // WHEN
        List<ChargingPoint> actualChargingPoints = requestHandler.getChargingPoints();

        // THEN
        for (String cp : cpToPlugInList) {
            assertThat(actualChargingPoints.stream()
                    .filter(x -> x.getName().equals(cp)).findFirst().get()
                    .isPlugged())
                    .isTrue();
        }
        assertThat(actualChargingPoints.stream()
                .filter(cp -> cp.getName().equals("CP4")).findFirst().get()
                .isPlugged())
                .isFalse();
    }

    @Test
    public void shouldDoNothingWhenCPplugsInMoreThanOnce() {
        // WHEN
        requestHandler.plugIn("CP1");
        requestHandler.plugIn("CP1");

        // THEN
        assertThat(requestHandler.getAllowedCurrent("CP1")).isEqualTo(HIGH_CURRENT);
        assertThat(requestHandler.getAllowedCurrent("CP2")).isEqualTo(0);
    }

}