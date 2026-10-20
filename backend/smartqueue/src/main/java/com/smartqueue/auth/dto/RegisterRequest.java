
package com.smartqueue.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Name must not be blank")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @NotBlank(message = "Email must not be blank")
        @Email(message = "Invalid email format")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "Password must not be blank")
        @Size(min = 8, message = "Password must contain at least 8 characters")
        String password
) {}
