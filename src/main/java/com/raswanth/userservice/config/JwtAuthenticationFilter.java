package com.raswanth.userservice.config;

import com.raswanth.userservice.service.JWTService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwt = null;
        final String userId;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("accessToken")) {
                    jwt = cookie.getValue();
                }
            }
        }
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        userId = jwtService.extractUserName(jwt);

        if (StringUtils.hasLength(userId) && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtService.isTokenNotExpired(jwt)) {
                Claims claims = jwtService.extractAllClaims(jwt);
                List<String> authorityStrings = (List<String>) claims.get("authorities");
                Collection<? extends GrantedAuthority> authorities;
                if (authorityStrings != null) {
                    authorities = authorityStrings.stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList();

                    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

                    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                            userId, null, authorities
                    );

                    token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    securityContext.setAuthentication(token);
                    SecurityContextHolder.setContext(securityContext);


                }
            }
            filterChain.doFilter(request, response);
        }
    }
}
