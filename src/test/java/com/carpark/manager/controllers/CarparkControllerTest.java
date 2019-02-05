package com.carpark.manager.controllers;

import com.carpark.manager.service.RequestHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class CarparkControllerTest {

    private static final String CP_NAME = "CP01";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private RequestHandler requestHandler;


    @Test
    public void shouldReturnSuccessOnPlugIn() throws Exception {
        // GIVEN

        // WHEN
        final ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put("/cp/plugin/" + CP_NAME));

        // THEN
        resultActions
                .andDo(print())
                .andExpect(status().is2xxSuccessful());
    }


    @Test
    public void shouldReturnSuccessOnPlugOff() throws Exception {
        // GIVEN

        // WHEN
        final ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put("/cp/plugoff/" + CP_NAME));

        // THEN
        resultActions
                .andDo(print())
                .andExpect(status().is2xxSuccessful());
    }


}