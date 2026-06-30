package com.primesys.adminserviceserver.modules.auth.services.impl;

import com.primesys.adminservicemongodb.entity.DivisionLoginEntity;
import com.primesys.adminservicemongodb.repository.DivisionLoginRepository;
import com.primesys.adminservicemongodb.repository.TokenRepository;
import com.primesys.adminserviceserver.modules.auth.dtos.AuthResponse;
import com.primesys.adminserviceserver.modules.auth.mapper.AuthMapper;
import com.primesys.adminserviceserver.modules.auth.services.AuthService;
import com.primesys.adminserviceserver.request.AuthenticationRequest;
import com.primesys.adminserviceserver.response.AuthenticationResponse;
import com.primesys.adminserviceserver.service.impl.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final DivisionLoginRepository repository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        DivisionLoginEntity user = repository.findByUserName(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        // revokeAllUserTokens(user);
        // saveUserToken(user, jwtToken);
        return AuthMapper.toDTO(jwtToken, refreshToken);
    }
}
