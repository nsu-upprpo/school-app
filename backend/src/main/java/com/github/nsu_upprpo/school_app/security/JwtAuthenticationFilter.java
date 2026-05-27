package com.github.nsu_upprpo.school_app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String JWT_ERROR_ATTR = "jwt.error";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String header = request.getHeader("Authorization");

        if (header == null) {
            log.debug("No Authorization header [uri={}]", uri);
            filterChain.doFilter(request, response);
            return;
        }
        if (!header.startsWith("Bearer ")) {
            reject(request, uri, "Authorization header is not a Bearer token");
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            Claims claims = jwtTokenProvider.parse(token);
            String type = claims.get("type", String.class);
            if (!"access".equals(type)) {
                reject(request, uri, "expected access token, got " + type);
                filterChain.doFilter(request, response);
                return;
            }

            String userId = claims.getSubject();
            try {
                UserDetails userDetails = userDetailsService.loadByUserId(UUID.fromString(userId));
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT authenticated [userId={}, uri={}]", userId, uri);
            } catch (UsernameNotFoundException ex) {
                reject(request, uri, "user from token does not exist: " + userId);
            } catch (IllegalArgumentException ex) {
                reject(request, uri, "malformed user id in token: " + userId);
            }
        } catch (JwtException ex) {
            reject(request, uri, ex.getClass().getSimpleName() + ": " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            reject(request, uri, "malformed JWT");
        }

        filterChain.doFilter(request, response);
    }

    private void reject(HttpServletRequest request, String uri, String reason) {
        log.warn("JWT rejected [reason={}, uri={}]", reason, uri);
        request.setAttribute(JWT_ERROR_ATTR, reason);
    }
}
