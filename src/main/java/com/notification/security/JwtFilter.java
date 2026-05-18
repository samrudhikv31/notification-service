package com.notification.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // skip JWT for these paths
        if (path.equals("/api/users/register") ||
                path.equals("/api/health") ||
                path.startsWith("/ws/") ||
                path.startsWith("/api/test/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(
                "Authorization");

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {
            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                    "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                    "Invalid or expired token");
            return;
        }

        String email = jwtUtil.getEmailFromToken(token);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        email, null, List.of());
        SecurityContextHolder.getContext()
                .setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}