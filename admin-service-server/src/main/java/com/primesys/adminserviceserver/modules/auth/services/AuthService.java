package com.primesys.adminserviceserver.modules.auth.services;

import com.primesys.adminserviceserver.modules.auth.dtos.AuthResponse;

public interface AuthService {
    AuthResponse authenticate(String email, String password);
}
