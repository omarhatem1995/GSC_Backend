package com.gsc.gsc.utilities;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        String token   = resolveToken(request);
        String userId  = resolveUserId(token);

        log.info("[REQUEST]  {} {} | userId={} | token={}",
                request.getMethod(), request.getRequestURI(), userId, maskToken(token));

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("[RESPONSE] {} {} | status={} | userId={} | duration={}ms",
                    request.getMethod(), request.getRequestURI(),
                    response.getStatus(), userId, duration);
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && !header.isBlank()) return header;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("Authorization".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private String resolveUserId(String token) {
        if (token == null) return "anonymous";
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return "invalid-token";
        }
    }

    /** Shows first 10 chars + ... + last 4 chars so the token is traceable but not fully exposed */
    private String maskToken(String token) {
        if (token == null) return "-";
        if (token.length() <= 14) return "***";
        return token.substring(0, 10) + "..." + token.substring(token.length() - 4);
    }
}
