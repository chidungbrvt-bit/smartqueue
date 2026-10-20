package com.smartqueue.common.exception;

import com.smartqueue.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class GlobalExceptionHandlerValidationTest {
    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    @RestController
    static class TestRegisterController {
        @PostMapping("/api/auth/register")
        public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest register) {
            return ResponseEntity.ok().build();
        }
    }

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new TestRegisterController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator).build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        String json = """
                {
                    "name": "",
                    "email": "invalid-email",
                    "password": ""
                }
                """;
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value("/api/auth/register"))
                .andExpect(jsonPath("$.fieldErrors.name").isArray())
                .andExpect(jsonPath("$.fieldErrors.email").isArray())
                .andExpect(jsonPath("$.fieldErrors.password").isArray());
    }

    @Test
    void shouldReturnAllPasswordErrors() throws Exception {
        String json = """
                {
                  "name": "Alex",
                  "email": "alex@example.com",
                  "password": ""
                }
                """;
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password",
                        hasItems(
                                "Password must not be blank",
                                "Password must contain at least 8 characters"
                        )))
                .andExpect(jsonPath("$.fieldErrors.password", hasSize(2)));
    }

    @Test
    void shouldAcceptValidRequest() throws Exception {
        String json = """
                {
                  "name": "Alex",
                  "email": "alex@example.com",
                  "password": "StrongPass123!"
                }
                """;
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json)).andExpect(status().isOk());
    }


    @Test
    void shouldRejectMissingAndBlankName() throws Exception {
        String missingName = """
            {
              "email": "alex@example.com",
              "password": "StrongPass123!"
            }
            """;
        String blankName = """
            {
              "name": "   ",
              "email": "alex@example.com",
              "password": "StrongPass123!"
            }
            """;
        for (String json : List.of(missingName, blankName)) {
            mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.name", hasItem("Name must not be blank")));
        }
    }

    @Test
    void shouldRespectNameLengthLimit() throws Exception {
        String name100 = "A".repeat(100);
        String name101 = "A".repeat(101);
        String validJson = """
            {
              "name": "%s",
              "email": "alex@example.com",
              "password": "StrongPass123!"
            }
            """.formatted(name100);
        String invalidJson = validJson.replace(name100, name101);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name", hasItem("Name must not exceed 100 characters")));
    }

    @Test
    void shouldRejectEmailLongerThan255Characters() throws Exception {
        String longEmail = "a".repeat(256) + "@example.com";
        String json = """
            {
              "name": "Alex",
              "email": "%s",
              "password": "StrongPass123!"
            }
            """.formatted(longEmail);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email", hasItem("Email must not exceed 255 characters")));
    }

    @Test
    void shouldRespectPasswordMinimumLength() throws Exception {
        String json = """
            {
              "name": "Alex",
              "email": "alex@example.com",
              "password": "%s"
            }
            """;
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.formatted("Pass123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password", hasItem("Password must contain at least 8 characters")));
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.formatted("Pass1234")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotExposePasswordInErrorResponse() throws Exception {
        String secretPassword = "R4re!";
        String json = """
            {
              "name": "Alex",
              "email": "alex@example.com",
              "password": "%s"
            }
            """.formatted(secretPassword);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        not(containsString(secretPassword))
                ));
    }
}
