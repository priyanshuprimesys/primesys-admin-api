package com.primesys.adminserviceserver.modules.auth.mapper;

import com.primesys.adminserviceserver.modules.auth.dtos.AuthResponse;

public final class AuthMapper {

    public static AuthResponse toDTO(String accessToken, String refreshToken) {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setRefreshToken(refreshToken);
        return authResponse;
    }
}
