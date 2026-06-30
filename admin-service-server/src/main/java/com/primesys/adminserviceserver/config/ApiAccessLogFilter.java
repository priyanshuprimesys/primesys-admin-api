package com.primesys.adminserviceserver.config;

import com.primesys.adminservicemongodb.entity.ApiAccessLogEntity;
import com.primesys.adminservicemongodb.repository.ApiAccessLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApiAccessLogFilter extends OncePerRequestFilter {

    private final ApiAccessLogRepository apiAccessLogRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            saveLog(request, response, System.currentTimeMillis() - startTime);
        }
    }

    private void saveLog(HttpServletRequest request, HttpServletResponse response, long responseTimeMs) {
        try {
            String username = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
                username = auth.getName();
            }

            apiAccessLogRepository.save(ApiAccessLogEntity.builder().ipAddress(resolveIp(request))
                    .userAgent(request.getHeader("User-Agent")).method(request.getMethod()).uri(request.getRequestURI())
                    .username(username).statusCode(response.getStatus()).responseTimeMs(responseTimeMs)
                    .timestamp(System.currentTimeMillis() / 1000).createdAt(new Date()).build());
        } catch (Exception e) {
            log.error("Failed to save API access log", e);
        }
    }

    private String resolveIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For may contain a comma-separated chain — first is the original client
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
