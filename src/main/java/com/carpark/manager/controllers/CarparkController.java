package com.carpark.manager.controllers;

import com.carpark.manager.domain.ChargingPoint;
import com.carpark.manager.service.RequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;


@RestController
public class CarparkController {

    private final RequestHandler requestHandler;

    @Autowired
    public CarparkController(final RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    private static String formatCpListToString(final List<ChargingPoint> cpList) {
        final StringBuilder sb = new StringBuilder();
        cpList.stream().sorted(Comparator.comparing(ChargingPoint::getName)).forEach(cp -> sb.append(cp.toString()).append("\n"));
        return sb.toString();
    }

    @RequestMapping(value = "/cp/plugin/{cpName}", method = RequestMethod.PUT, produces = TEXT_PLAIN_VALUE)
    @ResponseBody
    public HttpEntity<String> aCarPluggedIn(@PathVariable final String cpName) {
        requestHandler.plugIn(cpName);
        return ResponseEntity.ok().body("OK, successfully plugged in " + cpName);
    }

    @RequestMapping(value = "/cp/plugoff/{cpName}", method = RequestMethod.PUT, produces = TEXT_PLAIN_VALUE)
    @ResponseBody
    public HttpEntity<String> aCarPluggedOff(@PathVariable final String cpName) {
        requestHandler.plugOff(cpName);
        return ResponseEntity.ok().body("OK, successfully plugged off " + cpName);
    }

    @RequestMapping(value = "/cp/current/{cpName}", method = RequestMethod.GET, produces = TEXT_PLAIN_VALUE)
    @ResponseBody
    public HttpEntity<Integer> getAllowedCurrent(@PathVariable final String cpName) {
        return ResponseEntity.ok(requestHandler.getAllowedCurrent(cpName));
    }

    @RequestMapping(value = "/park/report", method = RequestMethod.GET, produces = TEXT_PLAIN_VALUE)
    @ResponseBody
    public HttpEntity<String> getReport() {
        return ResponseEntity.ok(formatCpListToString(requestHandler.getChargingPoints()));
    }

}
