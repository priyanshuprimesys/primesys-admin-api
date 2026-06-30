package com.primesys.adminserviceserver.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.primesys.adminservicemongodb.enums.Permission.*;
import static com.primesys.adminservicemongodb.model.Role.*;
import static org.springframework.http.HttpMethod.*;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeHttpRequests()

                // Public endpoints
                .requestMatchers("/api/v1/auth/**", "/v2/login/details", "/v3/api-docs/**", "/swagger-ui/**",
                        "/v2/chat-bot/**", "/v2/beat/upload-device-no-beat-file")
                .permitAll()
                .requestMatchers("/api/v1/auth/**", "/v2/login/details", "/api/v2/users/details", "/v3/api-docs/**",
                        "/swagger-ui/**", "/v2/chat-bot/**", "/admin-service/v2/chat-bot/**",
                        "/v2/beat/upload-device-no-beat-file", "/v2/beat/add-beat-manual")
                .permitAll()

                // Allow the internal error dispatch so real 4xx/5xx errors surface instead of being
                // masked by a generic 401 (the JWT filter does not run on ERROR dispatches).
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/health").permitAll()

                // Common roles for many endpoints
                .requestMatchers("/v2/division-logins/**", "/v2/packets/**", "/v2/device/**", "/v2/issue/**",
                        "/v2/device-command/**", "/v2/whitelist/**", "/v2/division/**", "/v2/modules/**",
                        "/api/v2/report-permission/**")
                .hasAnyRole(ADMIN.name(), SUB_ADMIN.name())

                .requestMatchers("/v2/beat/**")
                .hasAnyRole(ADMIN.name(), SUB_ADMIN.name(), TRACK_USER.name(), RAIL_SUB_USER.name())

                // Erlang server start/stop — admin and sub-admin (matches other privileged endpoints)
                .requestMatchers("/v2/server/**").hasAnyRole(ADMIN.name(), SUB_ADMIN.name())

                // Fine-grained permissions (authority-based)
                .requestMatchers(GET, "/api/v1/management/**").hasAnyAuthority(ADMIN.name(), SUB_ADMIN.name())

                .requestMatchers(POST, "/api/v1/management/**")
                .hasAnyAuthority(ADMIN_CREATE.name(), MANAGER_CREATE.name())

                .requestMatchers(PUT, "/api/v1/management/**")
                .hasAnyAuthority(ADMIN_UPDATE.name(), MANAGER_UPDATE.name())

                .requestMatchers(DELETE, "/api/v1/management/**")
                .hasAnyAuthority(ADMIN.name(), SUB_ADMIN.name(), ADMIN_DELETE.name(), MANAGER_DELETE.name())

                // All other requests must be authenticated
                .anyRequest().authenticated()

                // Diagnostic logging: distinguish 401 (no/!valid auth) from 403 (authenticated but wrong role) and
                // print the authorities that were actually evaluated at the point of denial.
                .and().exceptionHandling().authenticationEntryPoint((request, response, authException) -> {
                    log.warn("AUTH 401 method={} path={} reason={}", request.getMethod(), request.getServletPath(),
                            authException.getMessage());
                    response.sendError(401, "Unauthorized");
                }).accessDeniedHandler((request, response, accessDeniedException) -> {
                    var auth = SecurityContextHolder.getContext().getAuthentication();
                    log.warn("ACCESS DENIED 403 method={} path={} principal={} authorities={} reason={}",
                            request.getMethod(), request.getServletPath(), auth == null ? null : auth.getName(),
                            auth == null ? null : auth.getAuthorities(), accessDeniedException.getMessage());
                    response.sendError(403, "Forbidden");
                })

                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and().authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .logout().logoutUrl("/api/v1/auth/logout").addLogoutHandler(logoutHandler)
                .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext());

        http.cors();

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedOriginPatterns(List.of("https://*.mykidtrackers.com","http://localhost:*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}