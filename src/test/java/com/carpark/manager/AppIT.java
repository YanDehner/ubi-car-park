package com.carpark.manager;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AppIT {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void shouldAdd5CPsWithHighCurrent() {
        // GIVEN
        List<String> cpList = ImmutableList.of("CP01", "CP02", "CP03", "CP04", "CP05");

        String expected1 = "CP01 OCCUPIED 20A\n" +
                "CP02 OCCUPIED 20A\n" +
                "CP03 OCCUPIED 20A\n" +
                "CP04 OCCUPIED 20A\n" +
                "CP05 OCCUPIED 20A\n" +
                "CP06 AVAILABLE\n" +
                "CP07 AVAILABLE\n" +
                "CP08 AVAILABLE\n" +
                "CP09 AVAILABLE\n" +
                "CP10 AVAILABLE\n";

        // WHEN 5 cars plug in
        for (String cp : cpList) {
            restTemplate.put("/cp/plugin/" + cp, HttpEntity.EMPTY);
        }

        // THEN all of them should be fast charging
        ResponseEntity<String> entity = restTemplate.getForEntity("/park/report", String.class);
        assertThat(entity.getBody()).isEqualTo(expected1);


        // WHEN a 6th car plugs in
        String expected2 = "CP01 OCCUPIED 10A\n" +
                "CP02 OCCUPIED 10A\n" +
                "CP03 OCCUPIED 20A\n" +
                "CP04 OCCUPIED 20A\n" +
                "CP05 OCCUPIED 20A\n" +
                "CP06 OCCUPIED 20A\n" +
                "CP07 AVAILABLE\n" +
                "CP08 AVAILABLE\n" +
                "CP09 AVAILABLE\n" +
                "CP10 AVAILABLE\n";

        restTemplate.put("/cp/plugin/" + "CP06", HttpEntity.EMPTY);
        entity = restTemplate.getForEntity("/park/report", String.class);

        // THEN the two first cars should be put to slow, the rest of the plugged in cars to fast charging
        assertThat(entity.getBody()).isEqualTo(expected2);


        // WHEN 10 cars are plugged in
        String expected3 = "CP01 OCCUPIED 10A\n" +
                "CP02 OCCUPIED 10A\n" +
                "CP03 OCCUPIED 10A\n" +
                "CP04 OCCUPIED 10A\n" +
                "CP05 OCCUPIED 10A\n" +
                "CP06 OCCUPIED 10A\n" +
                "CP07 OCCUPIED 10A\n" +
                "CP08 OCCUPIED 10A\n" +
                "CP09 OCCUPIED 10A\n" +
                "CP10 OCCUPIED 10A\n";
        restTemplate.put("/cp/plugin/CP07", HttpEntity.EMPTY);
        restTemplate.put("/cp/plugin/CP08", HttpEntity.EMPTY);
        restTemplate.put("/cp/plugin/CP09", HttpEntity.EMPTY);
        restTemplate.put("/cp/plugin/CP10", HttpEntity.EMPTY);
        entity = restTemplate.getForEntity("/park/report", String.class);

        // THEN all of them should be slow charging
        assertThat(entity.getBody()).isEqualTo(expected3);
    }

    @Test
    public void shouldReturn404OnUnknownCP() throws URISyntaxException {
        // GIVEN
        HttpEntity<String> requestUpdate = new HttpEntity<>("", new HttpHeaders());

        // WHEN
        ResponseEntity<String> responseEntity = restTemplate.exchange(restTemplate.getRootUri() + "/cp/plugin/CP666",
                HttpMethod.PUT, requestUpdate, String.class);

        // THEN
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
