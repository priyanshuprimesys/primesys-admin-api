package com.primesys.adminserviceserver.modules.auth.dtos;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(@NotBlank(message = "Email is required") String email,
        @NotBlank(message = "Password is required") String password) {
}
