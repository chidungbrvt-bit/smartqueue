package com.smartqueue.health.controller;

import com.smartqueue.common.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthController.class)
@Import(SecurityConfig.class)
public class HealthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowAnonymousHealthRequest() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Service is running"))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void shouldRejectAnonymousPrivateRequest() throws Exception {
        mockMvc.perform(get("/api/private-probe")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotExposeOtherHealthPaths() throws Exception {
        mockMvc.perform(get("/api/health/extra")).andExpect(status().isUnauthorized());
    }
}
