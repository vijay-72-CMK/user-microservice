package com.raswanth.userservice.service.impl;

import com.raswanth.userservice.service.JWTService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
public class JWTServiceImpl implements JWTService {

    @Value("${jwt.expiration-time}")
    private long jwtExpirationTimeSeconds;
    private static final String secretKey = "bfc80827f07c171a3e1f0661a1abfd52ab1fec954283772e19c1a673efa58e41";

    @Override
    public String generateToken(UserDetails userDetails) {
        Instant expirationInstant = Instant.now().plus(jwtExpirationTimeSeconds, ChronoUnit.SECONDS);
        Date expirationTime = Date.from(expirationInstant);

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        List<String> authorityNames = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return Jwts.builder().subject((userDetails.getUsername()))
                .claim("authorities", authorityNames)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expirationTime)
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
    }

    private Key getSignKey() {
        byte[] key = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(key);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUserName(token);
        return username.equals(userDetails.getUsername()) && isTokenNotExpired(token);
    }

    public boolean isTokenNotExpired(String token) {
        return !extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
