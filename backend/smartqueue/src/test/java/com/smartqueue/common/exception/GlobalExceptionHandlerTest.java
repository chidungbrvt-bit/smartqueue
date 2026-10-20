package com.smartqueue.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class GlobalExceptionHandlerTest {
    @RestController
    static class TestController {
        @GetMapping("/test/missing")
        public String missing() {
            throw new ResourceNotFoundException("Resource not found");
        }
    }

    @Test
    void shouldReturn404WhenResourceNotFound() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        mockMvc.perform(get("/test/missing")).andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.path").value("/test/missing"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }
}
